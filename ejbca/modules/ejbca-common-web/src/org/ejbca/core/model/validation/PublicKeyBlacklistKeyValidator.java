/*************************************************************************
 *                                                                       *
 *  EJBCA Community: The OpenSource Certificate Authority                *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package org.ejbca.core.model.validation;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.cesecore.certificates.certificateprofile.CertificateProfile;
import org.cesecore.keys.util.KeyTools;
import org.cesecore.keys.validation.KeyValidatorBase;
import org.cesecore.keys.validation.ValidationException;
import org.cesecore.profiles.Profile;
import org.ejbca.core.model.util.EjbLocalHelper;

/**
 * Public key blacklist key validator using the Bouncy Castle BCRSAPublicKey implementation 
 * (see org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey). 
 * 
 * The key validator is used to filter out weak debian keys or other blacklisted public keys 
 * generated by 'openssl', 'ssh-keygen', or 'openvpn --keygen', see {@link https://wiki.debian.org/SSLkeys#Identifying_Weak_Keys}
 * 
 * @version $Id$
 */
public class PublicKeyBlacklistKeyValidator extends KeyValidatorBase {

    private static final long serialVersionUID = 215729318959311916L;

    /** List separator. */
    private static final String LIST_SEPARATOR = ";";

    /** Class logger. */
    private static final Logger log = Logger.getLogger(PublicKeyBlacklistKeyValidator.class);

    /** The key validator type. */
    private static final String TYPE_IDENTIFIER = "BLACKLIST_KEY_VALIDATOR";

    /** View template in /ca/editvalidators. */
    protected static final String TEMPLATE_FILE = "editBlacklistKeyValidator.xhtml";

    protected static final String KEY_ALGORITHMS = "keyAlgorithms";
    
    /** field used for JUnit testing, avoiding lookups so we can control the cache */
    private boolean useOnlyCache = false;
    
    /**
     * Public constructor needed for deserialization.
     */
    public PublicKeyBlacklistKeyValidator() {
        super();
        init();
    }
    
    /**
     * Creates a new instance.
     */
    public PublicKeyBlacklistKeyValidator(final String name) {
        super(name);
        init();
    }

    /**
     * Initializes uninitialized data fields.
     */
    public void init() {
        super.init();
        if (data.get(KEY_ALGORITHMS) == null) {
            ArrayList<String> algs = new ArrayList<String>();
            algs.add("RSA");
            algs.add("ECDSA");
            setKeyAlgorithms(algs);
        }
    }

    /** Gets a list of key algorithms.
     * @return a list.
     */
    public List<String> getKeyAlgorithms() {
        final String value = (String) data.get(KEY_ALGORITHMS);
        final List<String> result = new ArrayList<String>();
        final String[] tokens = value.trim().split(LIST_SEPARATOR);
        for (int i = 0, j = tokens.length; i < j; i++) {
            result.add(tokens[i]);
        }
        return result;
    }

    /** Sets the key algorithms, RSA, ECDSA, ....
     * 
     * @param algorithms list of key algorithms.
     */
    public void setKeyAlgorithms(List<String> algorithms) {
        final StringBuilder builder = new StringBuilder();
        for (String index : algorithms) {
            if (builder.length() == 0) {
                builder.append(index);
            } else {
                builder.append(LIST_SEPARATOR).append(index);
            }
        }
        data.put(KEY_ALGORITHMS, builder.toString());
    }

    @Override
    public String getTemplateFile() {
        return TEMPLATE_FILE;
    }
    
    @Override
    public float getLatestVersion() {
        return LATEST_VERSION;
    }

    @Override
    public void upgrade() {
        super.upgrade();
        if (log.isTraceEnabled()) {
            log.trace(">upgrade: " + getLatestVersion() + ", " + getVersion());
        }
        if (Float.compare(LATEST_VERSION, getVersion()) != 0) {
            // New version of the class, upgrade.
            log.info(intres.getLocalizedMessage("blacklistkeyvalidator.upgrade", new Float(getVersion())));
            init();
        }
    }

    @Override
    public List<String> validate(final PublicKey publicKey, final CertificateProfile certificateProfile) throws ValidationException {
        List<String> messages = new ArrayList<String>();
        final int keyLength = KeyTools.getKeyLength(publicKey);
        final String keyAlgorithm = publicKey.getAlgorithm(); // AlgorithmTools.getKeyAlgorithm(publicKey);
        if (log.isDebugEnabled()) {
            log.debug("Validating public key with algorithm " + keyAlgorithm + ", length " + keyLength + ", format " + publicKey.getFormat()
                    + ", implementation " + publicKey.getClass().getName() + " against public key blacklist.");
        }
        // Use the entry class to create a correct fingerprint
        final String fingerprint = PublicKeyBlacklistEntry.createFingerprint(publicKey);
        log.info("Matching public key with blacklist fingerprint " + fingerprint + " with public key blacklist.");
        if (!useOnlyCache) {
            // A bit hackish, make a call to blacklist session to ensure that blacklist cache has this entry loaded
            // TODO: if the key is not in the cache (which it hopefully is not) this is a database lookup for each key. Huuge performance hit
            // should better be implemented as a full in memory cache with a state so we know if it's loaded or not, with background updates. See ECA-5951
            // We could simply add a call to load the whole map into cache, when the cache needs updating
            // See BlacklistSessionBean.getBlacklistEntryIdToFingerprintMap (and add cache there)
            new EjbLocalHelper().getBlacklistSession().getBlacklistEntryId(PublicKeyBlacklistEntry.TYPE, fingerprint);
        }
        Integer idValue = PublicKeyBlacklistEntryCache.INSTANCE.getNameToIdMap().get(fingerprint);
        final PublicKeyBlacklistEntry entry = PublicKeyBlacklistEntryCache.INSTANCE.getEntry(idValue);
        boolean keySpecMatched = false;

        if (null != entry) {
            // Filter for key specifications.
            if (getKeyAlgorithms().contains("-1") || getKeyAlgorithms().contains(getKeySpec(publicKey))) {
                keySpecMatched = true;
            }
        }
        if (keySpecMatched) {
            final String message = "Public key with id " + entry.getID() + " and fingerprint " + fingerprint
                    + " found in public key blacklist.";
            messages.add("Invalid: " + message);
        } else {
            log.trace("publicKeyBlacklist passed");
        }

        if (log.isDebugEnabled()) {
            for (String message : messages) {
                log.debug(message);
            }
        }
        return messages;
    }

    private final String getKeySpec(PublicKey publicKey) {
        String keySpec;
        if (publicKey instanceof BCRSAPublicKey) {
            keySpec = publicKey.getAlgorithm().toUpperCase() + Integer.toString(((BCRSAPublicKey) publicKey).getModulus().bitLength());
        } else if (publicKey instanceof BCECPublicKey) {
            keySpec = publicKey.getAlgorithm().toUpperCase();
        } else {
            keySpec = publicKey.getAlgorithm().toUpperCase();
        }
        if (log.isTraceEnabled()) {
            log.trace("Key specification " + keySpec + " determined for public key " + publicKey.getEncoded());
        }
        return keySpec;
    }

    protected void setUseOnlyCache(boolean useOnlyCache) {
        this.useOnlyCache = useOnlyCache;
    }

    @Override
    public String getValidatorTypeIdentifier() {
        return TYPE_IDENTIFIER;
    }

    @Override
    public String getLabel() {
        return intres.getLocalizedMessage("validator.implementation.key.blacklist");
    }

    @Override
    protected Class<? extends Profile> getImplementationClass() {
        return PublicKeyBlacklistKeyValidator.class;
    }
    
}
