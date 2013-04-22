/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.ejbca.core.ejb.signer;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.CreateException;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.operator.OperatorCreationException;
import org.cesecore.audit.log.SecurityEventsLoggerSessionLocal;
import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authorization.AuthorizationDeniedException;
import org.cesecore.authorization.control.AccessControlSessionLocal;
import org.cesecore.authorization.control.CryptoTokenRules;
import org.cesecore.certificates.ca.CADoesntExistsException;
import org.cesecore.certificates.ca.CaSessionLocal;
import org.cesecore.certificates.certificate.CertificateConstants;
import org.cesecore.certificates.certificate.CertificateStoreSessionLocal;
import org.cesecore.certificates.util.AlgorithmTools;
import org.cesecore.internal.InternalResources;
import org.cesecore.jndi.JndiConstants;
import org.cesecore.keys.token.CryptoToken;
import org.cesecore.keys.token.CryptoTokenManagementSessionLocal;
import org.cesecore.keys.token.CryptoTokenOfflineException;
import org.cesecore.keys.token.KeyPairInfo;
import org.cesecore.keys.util.KeyTools;
import org.cesecore.util.CertTools;

/**
 * Generic Management implementation for InternalKeyBindings.
 * 
 * @version $Id$
 */
@Stateless(mappedName = JndiConstants.APP_JNDI_PREFIX + "InternalKeyBindingMgmtSessionRemote")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class InternalKeyBindingMgmtSessionBean implements InternalKeyBindingMgmtSessionLocal, InternalKeyBindingMgmtSessionRemote {

    private static final Logger log = Logger.getLogger(InternalKeyBindingMgmtSessionBean.class);
    private static final InternalResources intres = InternalResources.getInstance();

    @EJB
    private AccessControlSessionLocal accessControlSessionSession;
    @EJB
    private SecurityEventsLoggerSessionLocal securityEventsLoggerSession;
    @EJB
    private CryptoTokenManagementSessionLocal cryptoTokenManagementSession;
    @EJB
    private CaSessionLocal caSession;
    @EJB
    private CertificateStoreSessionLocal certificateStoreSession;
    @EJB
    private InternalKeyBindingDataSessionLocal internalKeyBindingDataSession;


    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public Map<String, List<String>> getAvailableTypesAndPropertyKeys(AuthenticationToken authenticationToken) {
        return InternalKeyBindingFactory.INSTANCE.getAvailableTypesAndPropertyKeys();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public List<Integer> getInternalKeyBindingIds(AuthenticationToken authenticationToken, String internalKeyBindingType) {
        final List<Integer> allIds = internalKeyBindingDataSession.getIds(internalKeyBindingType);
        final List<Integer> authorizedIds = new ArrayList<Integer>();
        for (final Integer current : allIds) {
            if (accessControlSessionSession.isAuthorizedNoLogging(authenticationToken, InternalKeyBindingRules.VIEW.resource()+"/"+current.toString())) {
                authorizedIds.add(current);
            }
        }
        return authorizedIds;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public List<InternalKeyBindingInfo> getInternalKeyBindingInfos(AuthenticationToken authenticationToken, String internalKeyBindingType) {
        final List<Integer> authorizedIds = getInternalKeyBindingIds(authenticationToken, internalKeyBindingType);
        final List<InternalKeyBindingInfo> authorizedInternalKeyBindingInfos = new ArrayList<InternalKeyBindingInfo>(authorizedIds.size());
        for (final Integer current : authorizedIds) {
            final InternalKeyBinding internalKeyBindingInstance = internalKeyBindingDataSession.getInternalKeyBinding(current.intValue());
            authorizedInternalKeyBindingInfos.add(new InternalKeyBindingInfo(internalKeyBindingInstance));
        }
        return authorizedInternalKeyBindingInfos;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public InternalKeyBinding getInternalKeyBinding(AuthenticationToken authenticationToken, int id) throws AuthorizationDeniedException {
        if (!accessControlSessionSession.isAuthorized(authenticationToken, InternalKeyBindingRules.VIEW.resource()+"/"+id)) {
            final String msg = intres.getLocalizedMessage("authorization.notuathorizedtoresource", InternalKeyBindingRules.VIEW.resource(), authenticationToken.toString());
            throw new AuthorizationDeniedException(msg);
        }
        return internalKeyBindingDataSession.getInternalKeyBinding(id);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public InternalKeyBindingInfo getInternalKeyBindingInfo(AuthenticationToken authenticationToken, int id) throws AuthorizationDeniedException {
        if (!accessControlSessionSession.isAuthorized(authenticationToken, InternalKeyBindingRules.VIEW.resource()+"/"+id)) {
            final String msg = intres.getLocalizedMessage("authorization.notuathorizedtoresource", InternalKeyBindingRules.VIEW.resource(), authenticationToken.toString());
            throw new AuthorizationDeniedException(msg);
        }
        return new InternalKeyBindingInfo(internalKeyBindingDataSession.getInternalKeyBinding(id));
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public Integer getIdFromName(String internalKeyBindingName) {
        if (internalKeyBindingName==null) {
            return null;
        }
        final Map<String, Integer> cachedNameToIdMap = internalKeyBindingDataSession.getCachedNameToIdMap();
        Integer internalKeyBindingId = cachedNameToIdMap.get(internalKeyBindingName);
        if (internalKeyBindingId == null) {
            // Ok.. so it's not in the cache.. look for it the hard way..
            for (final Integer currentId : internalKeyBindingDataSession.getIds(null)) {
                // Don't lookup CryptoTokens we already have in the id to name cache
                if (!cachedNameToIdMap.keySet().contains(currentId)) {
                    final InternalKeyBinding current = internalKeyBindingDataSession.getInternalKeyBinding(currentId.intValue());
                    final String currentName = current == null ? null : current.getName();
                    if (internalKeyBindingName.equals(currentName)) {
                        internalKeyBindingId = currentId;
                        break;
                    }
                }
            }
        }
        return internalKeyBindingId;
    }

    @Override
    public int createInternalKeyBinding(AuthenticationToken authenticationToken, String type, String name, InternalKeyBindingStatus status, String certificateId,
            int cryptoTokenId, String keyPairAlias, Map<Object, Object> dataMap) throws AuthorizationDeniedException, CryptoTokenOfflineException,
            InternalKeyBindingNameInUseException {
        if (!accessControlSessionSession.isAuthorized(authenticationToken, InternalKeyBindingRules.MODIFY.resource(),
                CryptoTokenRules.USE.resource() + "/" + cryptoTokenId)) {
            final String msg = intres.getLocalizedMessage("authorization.notuathorizedtoresource", InternalKeyBindingRules.MODIFY.resource(), authenticationToken.toString());
            throw new AuthorizationDeniedException(msg);
        }
        // Convert supplied properties using a prefix to ensure that the caller can't mess with internal ones
        final LinkedHashMap<Object,Object> initDataMap = new LinkedHashMap<Object,Object>();
        if (dataMap != null) {
            for (final Entry<Object,Object> entry: dataMap.entrySet()) {
                String key = String.valueOf(entry.getKey());
                if (key.startsWith(InternalKeyBindingBase.SUBCLASS_PREFIX)) {
                    initDataMap.put(key, entry.getValue());
                } else {
                    initDataMap.put(InternalKeyBindingBase.SUBCLASS_PREFIX + key, entry.getValue());
                }
            }
        }
        // Check that CryptoToken and alias exists (and that the user is authorized to see it)
        final KeyPairInfo keyPairInfo = cryptoTokenManagementSession.getKeyPairInfo(authenticationToken, cryptoTokenId, keyPairAlias);
        if (keyPairInfo == null) {
            throw new CryptoTokenOfflineException("Unable to access keyPair with alias " + keyPairAlias + " in CryptoToken with id " + cryptoTokenId);
        }
        // If we created a new InternalKeyBinding without a certificate reference (e.g. will be uploaded later) we will not allow the status to be anything but "DISABLED"
        if (certificateId == null || certificateStoreSession.findCertificateByFingerprint(certificateId) == null) {
            status = InternalKeyBindingStatus.DISABLED;
        }
        // Finally, try to create an instance of this type and persist it
        final InternalKeyBinding internalKeyBinding = InternalKeyBindingFactory.INSTANCE.create(type, 0, name, status, certificateId, cryptoTokenId, keyPairAlias, initDataMap);
        return internalKeyBindingDataSession.mergeInternalKeyBinding(internalKeyBinding);
    }

    @Override
    public int persistInternalKeyBinding(AuthenticationToken authenticationToken, InternalKeyBinding internalKeyBinding)
            throws AuthorizationDeniedException, InternalKeyBindingNameInUseException {
        if (!accessControlSessionSession.isAuthorized(authenticationToken, InternalKeyBindingRules.MODIFY.resource()+"/"+internalKeyBinding.getId(),
                CryptoTokenRules.USE.resource()+"/"+internalKeyBinding.getCryptoTokenId())) {
            final String msg = intres.getLocalizedMessage("authorization.notuathorizedtoresource", InternalKeyBindingRules.MODIFY.resource(), authenticationToken.toString());
            throw new AuthorizationDeniedException(msg);
        }
        return internalKeyBindingDataSession.mergeInternalKeyBinding(internalKeyBinding);
    }

    @Override
    public boolean deleteInternalKeyBinding(AuthenticationToken authenticationToken, int internalKeyBindingId) throws AuthorizationDeniedException {
        if (!accessControlSessionSession.isAuthorized(authenticationToken, InternalKeyBindingRules.DELETE.resource() + "/" + internalKeyBindingId)) {
            final String msg = intres.getLocalizedMessage("authorization.notuathorizedtoresource", InternalKeyBindingRules.DELETE.resource(), authenticationToken.toString());
            throw new AuthorizationDeniedException(msg);
        }
        return internalKeyBindingDataSession.removeInternalKeyBinding(internalKeyBindingId);
    }

    @Override
    public String generateNextKeyPair(AuthenticationToken authenticationToken, int internalKeyBindingId) throws AuthorizationDeniedException,
            CryptoTokenOfflineException, InvalidKeyException, InvalidAlgorithmParameterException {
        if (!accessControlSessionSession.isAuthorized(authenticationToken, InternalKeyBindingRules.MODIFY.resource() + "/" + internalKeyBindingId)) {
            final String msg = intres.getLocalizedMessage("authorization.notuathorizedtoresource", InternalKeyBindingRules.MODIFY.resource(), authenticationToken.toString());
            throw new AuthorizationDeniedException(msg);
        }
        final InternalKeyBinding internalKeyBinding = internalKeyBindingDataSession.getInternalKeyBinding(internalKeyBindingId);
        final int cryptoTokenId = internalKeyBinding.getCryptoTokenId();
        final String currentKeyPairAlias = internalKeyBinding.getKeyPairAlias();
        internalKeyBinding.generateNextKeyPairAlias();
        final String nextKeyPairAlias = internalKeyBinding.getNextKeyPairAlias();
        cryptoTokenManagementSession.createKeyPairWithSameKeySpec(authenticationToken, cryptoTokenId, currentKeyPairAlias, nextKeyPairAlias);
        try {
            internalKeyBindingDataSession.mergeInternalKeyBinding(internalKeyBinding);
        } catch (InternalKeyBindingNameInUseException e) {
            // This would be very strange if it happened, since we use the same name and id as for the existing one
            throw new RuntimeException(e);
        }
        return nextKeyPairAlias;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public byte[] getNextPublicKeyForInternalKeyBinding(AuthenticationToken authenticationToken, int internalKeyBindingId) throws AuthorizationDeniedException, CryptoTokenOfflineException {
        if (!accessControlSessionSession.isAuthorized(authenticationToken, InternalKeyBindingRules.VIEW.resource() + "/" + internalKeyBindingId)) {
            final String msg = intres.getLocalizedMessage("authorization.notuathorizedtoresource", InternalKeyBindingRules.VIEW.resource(), authenticationToken.toString());
            throw new AuthorizationDeniedException(msg);
        }
        final InternalKeyBinding internalKeyBinding = internalKeyBindingDataSession.getInternalKeyBinding(internalKeyBindingId);
        final int cryptoTokenId = internalKeyBinding.getCryptoTokenId();
        final String nextKeyPairAlias = internalKeyBinding.getNextKeyPairAlias();
        final String keyPairAlias;
        if (nextKeyPairAlias == null) {
            keyPairAlias = internalKeyBinding.getKeyPairAlias();
        } else {
            keyPairAlias = nextKeyPairAlias;
        }
        final PublicKey publicKey = cryptoTokenManagementSession.getPublicKey(authenticationToken, cryptoTokenId, keyPairAlias);
        return publicKey.getEncoded();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Override
    public byte[] generateCsrForNextKey(AuthenticationToken authenticationToken, int internalKeyBindingId) throws AuthorizationDeniedException, CryptoTokenOfflineException {
        if (!accessControlSessionSession.isAuthorized(authenticationToken, InternalKeyBindingRules.VIEW.resource() + "/" + internalKeyBindingId)) {
            final String msg = intres.getLocalizedMessage("authorization.notuathorizedtoresource", InternalKeyBindingRules.VIEW.resource(), authenticationToken.toString());
            throw new AuthorizationDeniedException(msg);
        }
        final InternalKeyBinding internalKeyBinding = internalKeyBindingDataSession.getInternalKeyBinding(internalKeyBindingId);
        final int cryptoTokenId = internalKeyBinding.getCryptoTokenId();
        final String nextKeyPairAlias = internalKeyBinding.getNextKeyPairAlias();
        final String keyPairAlias;
        if (nextKeyPairAlias == null) {
            keyPairAlias = internalKeyBinding.getKeyPairAlias();
        } else {
            keyPairAlias = nextKeyPairAlias;
        }
        final PublicKey publicKey = cryptoTokenManagementSession.getPublicKey(authenticationToken, cryptoTokenId, keyPairAlias);
        // Chose first available signature algorithm
        final Collection<String> availableSignatureAlgorithms = AlgorithmTools.getSignatureAlgorithms(publicKey);
        final String signatureAlgorithm = availableSignatureAlgorithms.iterator().next();
        final CryptoToken cryptoToken = cryptoTokenManagementSession.getCryptoToken(cryptoTokenId);
        final PrivateKey privateKey = cryptoToken.getPrivateKey(keyPairAlias);
        final X500Name x500Name = CertTools.stringToBcX500Name("CN=Should be ignore by CA");
        final String providerName = cryptoToken.getSignProviderName();
        try {
            return CertTools.genPKCS10CertificationRequest(signatureAlgorithm, x500Name, publicKey, new DERSet(), privateKey, providerName).getEncoded();
        } catch (OperatorCreationException e) {
            log.info("CSR generation failed. internalKeyBindingId=" + internalKeyBindingId + ", cryptoTokenId=" + cryptoTokenId + ", keyPairAlias=" + keyPairAlias + ". " + e.getMessage());
        } catch (IOException e) {
            log.info("CSR generation failed. internalKeyBindingId=" + internalKeyBindingId + ", cryptoTokenId=" + cryptoTokenId + ", keyPairAlias=" + keyPairAlias + ". " + e.getMessage());
        }
        return null;
    }

    @Override
    public String updateCertificateForInternalKeyBinding(AuthenticationToken authenticationToken, int internalKeyBindingId)
            throws AuthorizationDeniedException, CertificateImportException {
        if (!accessControlSessionSession.isAuthorized(authenticationToken, InternalKeyBindingRules.MODIFY.resource() + "/" + internalKeyBindingId)) {
            final String msg = intres.getLocalizedMessage("authorization.notuathorizedtoresource", InternalKeyBindingRules.MODIFY.resource(), authenticationToken.toString());
            throw new AuthorizationDeniedException(msg);
        }
        final InternalKeyBinding internalKeyBinding = internalKeyBindingDataSession.getInternalKeyBinding(internalKeyBindingId);
        final int cryptoTokenId = internalKeyBinding.getCryptoTokenId();
        final String nextKeyPairAlias = internalKeyBinding.getNextKeyPairAlias();
        if (log.isDebugEnabled()) {
            log.debug("nextKeyPairAlias: " + nextKeyPairAlias);
        }
        boolean updated = false;
        if (nextKeyPairAlias != null) {
            // If a nextKeyPairAlias is present we assume that this is the one we want to find a certificate for
            PublicKey nextPublicKey;
            try {
                nextPublicKey = cryptoTokenManagementSession.getPublicKey(authenticationToken, cryptoTokenId, nextKeyPairAlias);
            } catch (CryptoTokenOfflineException e) {
                throw new CertificateImportException("Operation is not available when CryptoToken is offline.", e);
            }
            if (nextPublicKey != null) {
                final byte[] subjectKeyId = KeyTools.createSubjectKeyId(nextPublicKey).getKeyIdentifier();
                final Certificate certificate = certificateStoreSession.findMostRecentlyUpdatedActiveCertificate(subjectKeyId);
                if (certificate == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("No certificate found for " + nextKeyPairAlias);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Certificate found for " + nextKeyPairAlias);
                    }
                    // Verify that this is an accepted type of certificate to import for the current implementation
                    assertCertificateIsOkToImport(certificate, internalKeyBinding);
                    // If current key matches next public key -> import and update nextKey + certificateId
                    String fingerprint = CertTools.getFingerprintAsString(certificate);
                    if (!fingerprint.equals(internalKeyBinding.getCertificateId())) {
                        internalKeyBinding.updateCertificateIdAndCurrentKeyAlias(fingerprint);
                        updated = true;
                        if (log.isDebugEnabled()) {
                            log.debug("New certificate with fingerprint " + fingerprint + " matching " + nextKeyPairAlias + " will be used.");
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("The latest available certificate was already in use.");
                        }
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("There was no public key for the referenced alias " + nextKeyPairAlias);
                }
            }
        }
        if (!updated) {
            // We failed to find a matching certificate for the next key, so we instead try to do the same for the current key pair
            final String currentKeyPairAlias = internalKeyBinding.getKeyPairAlias();
            log.debug("currentKeyPairAlias: " + currentKeyPairAlias);
            PublicKey currentPublicKey;
            try {
                currentPublicKey = cryptoTokenManagementSession.getPublicKey(authenticationToken, cryptoTokenId, currentKeyPairAlias);
            } catch (CryptoTokenOfflineException e) {
                throw new CertificateImportException("Operation is not available when CryptoToken is offline.", e);
            }
            if (currentPublicKey != null) {
                final byte[] subjectKeyId = KeyTools.createSubjectKeyId(currentPublicKey).getKeyIdentifier();
                final Certificate certificate = certificateStoreSession.findMostRecentlyUpdatedActiveCertificate(subjectKeyId);
                if (certificate == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("No certificate found for " + currentKeyPairAlias);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Certificate found for " + currentKeyPairAlias);
                    }
                    // Verify that this is an accepted type of certificate to import for the current implementation
                    assertCertificateIsOkToImport(certificate, internalKeyBinding);
                    final String fingerprint = CertTools.getFingerprintAsString(certificate);
                    if (!fingerprint.equals(internalKeyBinding.getCertificateId())) {
                        internalKeyBinding.setCertificateId(fingerprint);
                        updated = true;
                        if (log.isDebugEnabled()) {
                            log.debug("Certificate with fingerprint " + fingerprint + " matching " + currentKeyPairAlias + " will be used.");
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("The latest available certificate was already in use.");
                        }
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("There was no public key for the referenced alias " + currentKeyPairAlias);
                }
            }
        }
        if (updated) {
            try {
                internalKeyBindingDataSession.mergeInternalKeyBinding(internalKeyBinding);
            } catch (InternalKeyBindingNameInUseException e) {
                // This would be very strange if it happened, since we use the same name and id as for the existing one
                throw new CertificateImportException(e);
            }
            if (log.isDebugEnabled()) {
                log.debug("No certificate found for " + nextKeyPairAlias);
            }
            return internalKeyBinding.getCertificateId();
        }
        return null;
    }


    @Override
    public boolean setStatus(AuthenticationToken authenticationToken, int internalKeyBindingId, InternalKeyBindingStatus status) throws AuthorizationDeniedException {
        final InternalKeyBinding internalKeyBinding = getInternalKeyBinding(authenticationToken, internalKeyBindingId);
        if (status == internalKeyBinding.getStatus()) {
            return false;
        }
        internalKeyBinding.setStatus(status);
        try {
            persistInternalKeyBinding(authenticationToken, internalKeyBinding);
        } catch (InternalKeyBindingNameInUseException e) {
            // This would be very strange if it happened, since we use the same name and id as for the existing one
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public void importCertificateForInternalKeyBinding(AuthenticationToken authenticationToken, int internalKeyBindingId, byte[] derEncodedCertificate)
            throws AuthorizationDeniedException, CertificateImportException {
        if (!accessControlSessionSession.isAuthorized(authenticationToken, InternalKeyBindingRules.MODIFY.resource() + "/" + internalKeyBindingId)) {
            final String msg = intres.getLocalizedMessage("authorization.notuathorizedtoresource", InternalKeyBindingRules.MODIFY.resource(), authenticationToken.toString());
            throw new AuthorizationDeniedException(msg);
        }
        final InternalKeyBinding internalKeyBinding = internalKeyBindingDataSession.getInternalKeyBinding(internalKeyBindingId);
        // UnDERify
        final Certificate certificate;
        try {
            certificate = CertTools.getCertfromByteArray(derEncodedCertificate);
        } catch (CertificateException e) {
            throw new CertificateImportException(e);
        }
        // Verify that this is an accepted type of certificate to import for the current implementation
        assertCertificateIsOkToImport(certificate, internalKeyBinding);
        final int cryptoTokenId = internalKeyBinding.getCryptoTokenId();
        final String currentKeyPairAlias = internalKeyBinding.getKeyPairAlias();
        boolean updated = false;
        try {
            String certificateId = CertTools.getFingerprintAsString(certificate);
            final PublicKey currentPublicKey = cryptoTokenManagementSession.getPublicKey(authenticationToken, cryptoTokenId, currentKeyPairAlias);
            if (currentPublicKey != null && KeyTools.createSubjectKeyId(currentPublicKey).equals(KeyTools.createSubjectKeyId(certificate.getPublicKey()))) {
                // If current key matches current public key -> import + update certificateId
                if (isCertificateAlreadyInDatabase(certificateId)) {
                    log.info("Certificate with fingerprint " + certificateId + " was already present in the database. Only InternalKeyBinding reference will be updated.");
                } else {
                    storeCertificate(authenticationToken, internalKeyBinding, certificate);
                }
                internalKeyBinding.setCertificateId(certificateId);
                updated = true;
            } else {
                final String nextKeyPairAlias = internalKeyBinding.getNextKeyPairAlias();
                if (nextKeyPairAlias == null) {
                    final PublicKey nextPublicKey = cryptoTokenManagementSession.getPublicKey(authenticationToken, cryptoTokenId, nextKeyPairAlias);
                    if (nextPublicKey != null && KeyTools.createSubjectKeyId(nextPublicKey).equals(KeyTools.createSubjectKeyId(certificate.getPublicKey()))) {
                        // If current key matches next public key -> import and update nextKey + certificateId
                        if (isCertificateAlreadyInDatabase(certificateId)) {
                            log.info("Certificate with fingerprint " + certificateId + " was already present in the database. Only InternalKeyBinding reference will be updated.");
                        } else {
                            storeCertificate(authenticationToken, internalKeyBinding, certificate);
                        }
                        internalKeyBinding.updateCertificateIdAndCurrentKeyAlias(certificateId);
                        updated = true;
                    }
                }
            }
        } catch (CryptoTokenOfflineException e) {
            throw new CertificateImportException(e);
        }
        if (updated) {
            try {
                internalKeyBindingDataSession.mergeInternalKeyBinding(internalKeyBinding);
            } catch (InternalKeyBindingNameInUseException e) {
                // This would be very strange if it happened, since we use the same name and id as for the existing one
                throw new CertificateImportException(e);
            }
        } else {
            throw new CertificateImportException("No keys matching the certificate were found.");
        }
        //throw new UnsupportedOperationException("Not yet fully implemented!");
    }
    
    /** Asserts that it is not a CA certificate and that the implementation finds it acceptable.  */
    private void assertCertificateIsOkToImport(Certificate certificate, InternalKeyBinding internalKeyBinding) throws CertificateImportException {
        // Do some general sanity checks that this is not a CA certificate
        if (CertTools.isCA(certificate)) {
            throw new CertificateImportException("Import of CA certificates is not allowed using this operation.");
        }
        // Check that this is an accepted type of certificate from the one who knows (the implementation)
        internalKeyBinding.assertCertificateCompatability(certificate);
    }
    
    /** @return true if a certificate with the specified certificateId (fingerprint) already exists in the database */
    private boolean isCertificateAlreadyInDatabase(String certificateId) {
        return certificateStoreSession.findCertificateByFingerprint(certificateId) != null;
    }
    
    /** Imports the certificate to the database */
    private void storeCertificate(AuthenticationToken authenticationToken, InternalKeyBinding internalKeyBinding, Certificate certificate)
            throws AuthorizationDeniedException, CertificateImportException {
        // Set some values for things we cannot know
        final int certificateProfileId = 0;
        final String username = "IMPORTED_InternalKeyBinding_" + internalKeyBinding.getId();
        // Find caFingerprint through ca(Admin?)Session
        final List<Integer> availableCaIds = caSession.getAvailableCAs();
        final String issuerDn = CertTools.getIssuerDN(certificate);
        String caFingerprint = null;
        for (final Integer caId : availableCaIds) {
            try {
                final Certificate caCert = caSession.getCAInfo(authenticationToken, caId).getCertificateChain().iterator().next();
                final String subjectDn = CertTools.getSubjectDN(caCert);
                if (subjectDn.equals(issuerDn)) {
                    caFingerprint = CertTools.getFingerprintAsString(caCert);
                    break;
                }
            } catch (CADoesntExistsException e) {
                log.debug("CA with caId " + caId + "disappeared during this operation.");
            }
        }
        if (caFingerprint == null) {
            throw new CertificateImportException("No CA certificate for " + issuerDn + " was found on the system.");
        }
        try {
            certificateStoreSession.storeCertificate(authenticationToken, certificate, username, caFingerprint,
                    CertificateConstants.CERT_ACTIVE, CertificateConstants.CERTTYPE_ENDENTITY, certificateProfileId, null, System.currentTimeMillis());
        } catch (CreateException e) {
            // Just pass on the message, in case this is a remote invocation where the underlying class is not available.
            if (log.isDebugEnabled()) {
                log.debug("", e);
            }
            throw new CertificateImportException(e.getMessage());
        }
    }
}

