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
package org.ejbca.extra.caservice;

import java.io.IOException;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ejbca.core.EjbcaException;
import org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionLocal;
import org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionLocalHome;
import org.ejbca.core.ejb.ca.sign.ISignSessionLocal;
import org.ejbca.core.ejb.ca.sign.ISignSessionLocalHome;
import org.ejbca.core.ejb.ca.store.ICertificateStoreSessionLocal;
import org.ejbca.core.ejb.ca.store.ICertificateStoreSessionLocalHome;
import org.ejbca.core.ejb.hardtoken.IHardTokenSessionLocal;
import org.ejbca.core.ejb.hardtoken.IHardTokenSessionLocalHome;
import org.ejbca.core.ejb.keyrecovery.IKeyRecoverySessionLocal;
import org.ejbca.core.ejb.keyrecovery.IKeyRecoverySessionLocalHome;
import org.ejbca.core.ejb.ra.raadmin.IRaAdminSessionLocal;
import org.ejbca.core.ejb.ra.raadmin.IRaAdminSessionLocalHome;
import org.ejbca.core.model.SecConst;
import org.ejbca.core.model.authorization.AuthorizationDeniedException;
import org.ejbca.core.model.ca.AuthLoginException;
import org.ejbca.core.model.ca.AuthStatusException;
import org.ejbca.core.model.ca.IllegalKeyException;
import org.ejbca.core.model.ca.SignRequestException;
import org.ejbca.core.model.ca.SignRequestSignatureException;
import org.ejbca.core.model.ca.caadmin.CADoesntExistsException;
import org.ejbca.core.model.ca.caadmin.CAInfo;
import org.ejbca.core.model.ca.catoken.CATokenConstants;
import org.ejbca.core.model.ca.crl.RevokedCertInfo;
import org.ejbca.core.model.ca.store.CertificateInfo;
import org.ejbca.core.model.hardtoken.profiles.EIDProfile;
import org.ejbca.core.model.hardtoken.profiles.HardTokenProfile;
import org.ejbca.core.model.hardtoken.profiles.SwedishEIDProfile;
import org.ejbca.core.model.keyrecovery.KeyRecoveryData;
import org.ejbca.core.model.log.Admin;
import org.ejbca.core.model.ra.ExtendedInformation;
import org.ejbca.core.model.ra.NotFoundException;
import org.ejbca.core.model.ra.UserDataConstants;
import org.ejbca.core.model.ra.UserDataVO;
import org.ejbca.core.protocol.IResponseMessage;
import org.ejbca.core.protocol.PKCS10RequestMessage;
import org.ejbca.extra.db.ExtRACardRenewalRequest;
import org.ejbca.extra.db.ExtRACardRenewalResponse;
import org.ejbca.extra.db.ExtRAEditUserRequest;
import org.ejbca.extra.db.ExtRAKeyRecoveryRequest;
import org.ejbca.extra.db.ExtRAPKCS10Request;
import org.ejbca.extra.db.ExtRAPKCS10Response;
import org.ejbca.extra.db.ExtRAPKCS12Request;
import org.ejbca.extra.db.ExtRAPKCS12Response;
import org.ejbca.extra.db.ExtRARequest;
import org.ejbca.extra.db.ExtRAResponse;
import org.ejbca.extra.db.ExtRARevocationRequest;
import org.ejbca.extra.db.ISubMessage;
import org.ejbca.extra.db.Message;
import org.ejbca.extra.db.MessageHome;
import org.ejbca.extra.db.SubMessages;
import org.ejbca.extra.util.RAKeyStore;
import org.ejbca.ui.web.RequestHelper;
import org.ejbca.util.CertTools;
import org.ejbca.util.KeyTools;
import org.ejbca.util.query.Query;

/**
 * @version $Id: ExtRACAProcess.java,v 1.17 2007-01-06 15:54:11 anatom Exp $
 */
public class ExtRACAProcess extends RACAProcess {

	private static Logger log = Logger.getLogger(ExtRACAProcess.class);
	
	private MessageHome msgHome = new MessageHome(MessageHome.MESSAGETYPE_EXTRA);
	
	private IExtRACAService conf = null;

	
	private RAKeyStore serviceKeyStore = null;

	private boolean usekeyrecovery = false;
	
	private Admin internalUser = new Admin(Admin.TYPE_INTERNALUSER);
	
	public void init(IRACAService configuration) throws ConfigurationException{
		super.init(configuration);
		
		if(!(configuration instanceof IExtRACAService)){
			throw new ConfigurationException("Wrong RACAService Interface configured, must be IExtRACAService.");
		}
				
		conf = (IExtRACAService) configuration;     
		
		try {
			serviceKeyStore = new RAKeyStore(conf.getKeyStorePath(), conf.getKeyStorePassword());
		} catch (Exception e) {
			if(conf.getEncryptionRequired() || conf.getSignatureRequired()){
			  log.error("Error reading ExtRACAService keystore" ,e);
			}else{
			  log.info("ExtRACAService KeyStore couldn't be configured, but isn't required");	
			}
		}
		
		try {
			usekeyrecovery  = getRAAdminSession().loadGlobalConfiguration(internalUser).getEnableKeyRecovery();
		}  catch (Exception e) {
			log.error("Error instansiating Session Beans: ",e);
			throw new ConfigurationException("Error instansiating Session Beans: ",e);
		}
	}

	public void processWaitingMessage() {
		
		Collection cACertChain = null;
		try {
			cACertChain = getCACertChain(internalUser, conf.getRAIssuer(), true);
		} catch (ConfigurationException e) {
			if(conf.getEncryptionRequired() || conf.getSignatureRequired()){
				log.error("RAIssuer is misconfigured: ", e);
				return;
			}else{
				log.info("RAIssuer is misconfigured, but isn't required");	
			}
		}				
		
		Message msg = null;
	   do{	
	     msg = msgHome.getNextWaitingMessage();
	     if(msg != null){
	    	  String errormessage = null;
	    	  SubMessages submgs = null;
	    	  try {
	    		log.info("Started processing message with messageId: " + msg.getMessageid()); 

	    		if(serviceKeyStore != null){
				  submgs = msg.getSubMessages(
						  (PrivateKey) serviceKeyStore.getKeyStore().getKey(serviceKeyStore.getAlias(), conf.getKeyStorePassword().toCharArray()),
						  cACertChain,null);
	    		}else{
	              submgs =  msg.getSubMessages(null,null,null);
	    		}
				if(conf.getSignatureRequired() && !submgs.isSigned()){
					errormessage = "Error: Message from : " + msg.getMessageid() + " wasn't signed which is a requrement";
					log.error(errormessage);
					
				}
				if(conf.getEncryptionRequired() && !submgs.isEncrypted()){
					errormessage = "Error: Message from : " + msg.getMessageid() + " wasn't encrypted which is a requrement";
					log.error(errormessage);
					
				}
				} catch (Exception e) {
					   errormessage = "Error processing waiting message with Messageid : " + msg.getMessageid() + " : "+ e.getMessage();
		               log.error("Error processing waiting message with Messageid : " + msg.getMessageid(),e);
				}
				
				if(submgs != null){
					SubMessages respSubMsg;
					try {
						respSubMsg = generateResponseSubMessage(submgs.getSignerCert());
						Iterator iter = submgs.getSubMessages().iterator();
						while(iter.hasNext()){
							respSubMsg.addSubMessage(processSubMessage(getAdmin(submgs), (ISubMessage) iter.next(), errormessage));
						}
						
						msg.setStatus(Message.STATUS_PROCESSED);
						msg.setSubMessages(respSubMsg);
						msgHome.update(msg);
					} catch (Exception e) {
						 log.error("Error generating response message with Messageid : " + msg.getMessageid(),e);
					}
					
				}

	     }	     
	   }while(msg != null);

	}
	
	
	/**
	 * Method generating a reponse to a given sub request
	 * @param admin the administrator (signer for signed requests, internal user othervise) performing the action
	 * @param submessage the request submessage
	 * @return a response, contains failinfo if anything went wrong
	 */
	private ISubMessage processSubMessage(Admin admin, ISubMessage submessage, String errormessage) {
		if(submessage instanceof ExtRAPKCS10Request){
			if(errormessage == null){
				return processExtRAPKCS10Request(admin, (ExtRAPKCS10Request) submessage);
			}else{
				return new ExtRAPKCS10Response(((ExtRARequest) submessage).getRequestId(), false, errormessage, null, null);
			}
		}
		if(submessage instanceof ExtRAPKCS12Request){
			if(errormessage == null){
				return processExtRAPKCS12Request(admin, (ExtRAPKCS12Request) submessage);
			}else{
				return new ExtRAPKCS12Response(((ExtRARequest) submessage).getRequestId(),false, errormessage,null, null);
			}
		}
		if(submessage instanceof ExtRAEditUserRequest){
			if(errormessage == null){
				return processExtRAEditUserRequest(admin, (ExtRAEditUserRequest) submessage);
			}else{
				return new ExtRAResponse(((ExtRARequest) submessage).getRequestId(),false, errormessage);
			}
		}
		if(submessage instanceof ExtRAKeyRecoveryRequest){
			if(errormessage == null){
				return processExtRAKeyRecoveryRequest(admin, (ExtRAKeyRecoveryRequest) submessage);
			}else{
				return new ExtRAPKCS12Response(((ExtRARequest) submessage).getRequestId(),false, errormessage,null, null);
			}
		}
		if(submessage instanceof ExtRARevocationRequest){
			if(errormessage == null){
				return processExtRARevocationRequest(admin, (ExtRARevocationRequest) submessage);
			}else{
				return new ExtRAResponse(((ExtRARequest) submessage).getRequestId(),false, errormessage);
			}
		}
		if(submessage instanceof ExtRACardRenewalRequest){
			if(errormessage == null){
				return processExtRACardRenewalRequest(admin, (ExtRACardRenewalRequest) submessage);
			}else{
				return new ExtRACardRenewalResponse(((ExtRARequest) submessage).getRequestId(),false, errormessage, null, null);
			}
		}
		log.error("Received an illegal submessage request :" + submessage.getClass().getName());
		return null; // Should never happen.
	}





	private ISubMessage processExtRAPKCS10Request(Admin admin, ExtRAPKCS10Request submessage) {
		log.debug("Processing ExtRAPKCS10Request");
		ExtRAPKCS10Response retval = null;
		try{
          UserDataVO userdata = generateUserDataVO(admin, submessage);
		  userdata.setPassword("foo123");
	      storeUserData(admin, userdata,false,UserDataConstants.STATUS_INPROCESS );
	      
	      // Create a PKCS10
	      PKCS10RequestMessage pkcs10 =RequestHelper.genPKCS10RequestMessageFromPEM(submessage.getPKCS10().getBytes());
	      
	      X509Certificate cert = (X509Certificate) getSignSession().createCertificate(admin,submessage.getUsername(),"foo123", pkcs10.getRequestPublicKey());
	      byte[] pkcs7 = getSignSession().createPKCS7(admin, cert, true);
	      retval = new ExtRAPKCS10Response(submessage.getRequestId(),true,null,cert,pkcs7);
	      
		}catch(Exception e){
			log.error("Error processing ExtRAPKCS10Requset : ", e);
			retval = new ExtRAPKCS10Response(submessage.getRequestId(),false,e.getMessage(),null,null);
		}
		
		return retval;
	}
	


	private ISubMessage processExtRAPKCS12Request(Admin admin, ExtRAPKCS12Request submessage) {
		log.debug("Processing ExtRAPKCS12Request");
		ExtRAPKCS12Response retval = null;
        UserDataVO userdata = null;
		try{
            userdata = generateUserDataVO(admin, submessage);
            userdata.setPassword("foo123");
	      storeUserData(admin, userdata, false, UserDataConstants.STATUS_INPROCESS);
	      
	      // Generate keys
	      KeyPair keys = generateKeys(submessage);
	      // Generate Certificate
	      X509Certificate cert = (X509Certificate) getSignSession().createCertificate(admin,submessage.getUsername(),"foo123", keys.getPublic());
	      
	      // Generate Keystore
	        // Fetch CA Cert Chain.	        
	      Certificate[] chain = (Certificate[]) getCACertChain(admin, submessage.getCAName(), false).toArray(new Certificate[0]);
	      String alias = CertTools.getPartFromDN(CertTools.getSubjectDN(cert), "CN");
	      if (alias == null){
	    	  alias = submessage.getUsername();
	      }	      	      
	      KeyStore pkcs12 = KeyTools.createP12(alias, keys.getPrivate(), cert, chain);
	      
	      // Store Keys if requested
	        if (usekeyrecovery && submessage.getStoreKeys()) {
	            // Save generated keys to database.
	            getKeyRecoverySession().addKeyRecoveryData(admin, cert, submessage.getUsername(), keys);
	        }

	      retval = new ExtRAPKCS12Response(submessage.getRequestId(),true,null,pkcs12,submessage.getPassword());
          storeUserData(admin, userdata, false, UserDataConstants.STATUS_GENERATED);
		}catch(Exception e){
			log.error("Error processing ExtRAPKCS12Requset : ", e);
            if (userdata != null) {
                try {
                    storeUserData(admin, userdata, false, UserDataConstants.STATUS_FAILED);                    
                } catch (Exception ignore) {/*ignore*/}
            }
			retval = new ExtRAPKCS12Response(submessage.getRequestId(),false,e.getMessage(),null,null);
		}
		
		return retval;
	}
	
	private ISubMessage processExtRAEditUserRequest(Admin admin, ExtRAEditUserRequest submessage) {
		log.debug("Processing ExtRAEditUserRequest");
		ExtRAResponse retval = null;
        UserDataVO userdata = null;
		try{
            userdata = generateUserDataVO(admin, submessage);
            userdata.setPassword(submessage.getPassword());			   
			userdata.setType(submessage.getType());
			userdata.setTokenType(getTokenTypeId(admin, submessage.getTokenName()));
			userdata.setHardTokenIssuerId(getHardTokenIssuerId(admin, submessage.getHardTokenIssuerName()));
            
	        storeUserData(admin, userdata, false, submessage.getStatus());
	        retval = new ExtRAResponse(submessage.getRequestId(),true,null);

		}catch(Exception e){
			log.error("Error processing ExtRAEditUserRequest : ", e);
            if (userdata != null) {
                try {
                    storeUserData(admin, userdata, false, UserDataConstants.STATUS_FAILED);                    
                } catch (Exception ignore) {/*ignore*/}
            }
			retval = new ExtRAResponse(submessage.getRequestId(),false,e.getMessage());
		}
		
		return retval;
	}

	private ISubMessage processExtRAKeyRecoveryRequest(Admin admin, ExtRAKeyRecoveryRequest submessage) {
		log.debug("Processing ExtRAKeyRecoveryRequest");
		ExtRAPKCS12Response retval = null;
		try{
			
			UserDataVO userdata = null;
			
			if(submessage.getReUseCertificate()){
				userdata = getUserAdminSession().findUser(admin,submessage.getUsername());
			}else{
			  userdata = generateUserDataVO(admin, submessage);
			  userdata.setPassword("foo123");
			}
			
			// Get KeyPair
			getKeyRecoverySession().unmarkUser(admin,submessage.getUsername());
			X509Certificate orgcert = (X509Certificate) getCertStoreSession().findCertificateByIssuerAndSerno(admin,CertTools.stringToBCDNString(submessage.getIssuerDN()), submessage.getCertificateSN());
			if(orgcert == null){
				throw new EjbcaException("Error in Key Recovery Request, couldn't find specified certificate");
			}
			if(!getKeyRecoverySession().markAsRecoverable(admin,orgcert,userdata.getEndEntityProfileId())){
				throw new EjbcaException("Error in Key Recovery Request, no keys saved for specified request");
			}
			KeyRecoveryData keyData = getKeyRecoverySession().keyRecovery(admin, submessage.getUsername(), userdata.getEndEntityProfileId());
			if(keyData == null){
				throw new EjbcaException("Error in Key Recovery Request, no keys saved for specified request");
			}			
			KeyPair savedKeys = keyData.getKeyPair();
			
			X509Certificate cert = null;	
			if(submessage.getReUseCertificate()){	
				cert= orgcert;
				
			}else{
				storeUserData(admin, userdata,false, UserDataConstants.STATUS_INPROCESS);
				
				// Generate Certificate
				cert = (X509Certificate) getSignSession().createCertificate(admin,submessage.getUsername(),"foo123", savedKeys.getPublic());			  
			}			
			
			// Generate Keystore
			// Fetch CA Cert Chain.	        
			int caid = CertTools.stringToBCDNString(cert.getIssuerDN().toString()).hashCode(); 
			Certificate[] chain = (Certificate[]) getCAAdminSession().getCAInfo(admin, caid).getCertificateChain().toArray(new Certificate[0]);
			String alias = CertTools.getPartFromDN(CertTools.getSubjectDN(cert), "CN");
			if (alias == null){
				alias = submessage.getUsername();
			}	      	      
			KeyStore pkcs12 = KeyTools.createP12(alias, savedKeys.getPrivate(), cert, chain);
			
			retval = new ExtRAPKCS12Response(submessage.getRequestId(),true,null,pkcs12,submessage.getPassword());
			
		}catch(Exception e){
			log.error("Error processing ExtRAKeyRecoveryRequset : ", e);
			retval = new ExtRAPKCS12Response(submessage.getRequestId(),false,e.getMessage(),null,null);
		}
		
		return retval;
	}
	
	private ISubMessage processExtRARevocationRequest(Admin admin, ExtRARevocationRequest submessage) {
		log.debug("Processing ExtRARevocationRequest");
		ExtRAResponse retval = null;
		try {			 
			// If this is a message that dod contain an explicit username, use it
			String username = submessage.getUsername();
			String issuerDN = submessage.getIssuerDN();
			BigInteger serno = submessage.getCertificateSN();
			if (StringUtils.isEmpty(issuerDN) && StringUtils.isEmpty(username)) {
				retval = new ExtRAResponse(submessage.getRequestId(),false,"Either username or issuer/serno is required");
			} else {
				if (StringUtils.isEmpty(username)) {
					username = getCertStoreSession().findUsernameByCertSerno(admin, serno, CertTools.stringToBCDNString(issuerDN));
				} 
				if (username != null) {
					if ( (submessage.getRevokeAll() || submessage.getRevokeUser()) ) {
						// Revoke all users certificates by revoking the whole user
						UserDataVO vo = getUserAdminSession().findUser(admin,username);
						if (vo != null) {
							getUserAdminSession().revokeUser(admin,username, submessage.getRevocationReason());
							if (!submessage.getRevokeUser()) {
								// If we were not to revoke the user itself, but only the certificates, we should set back status
								getUserAdminSession().setUserStatus(admin, username, vo.getStatus());
							}					
						} else {
							retval = new ExtRAResponse(submessage.getRequestId(),false,"User not found from username: username="+username);							
						}
					} else {
						// Revoke only this certificate
						getUserAdminSession().revokeCert(admin, serno, CertTools.stringToBCDNString(issuerDN), username, submessage.getRevocationReason());				
					}					
				} else {
					retval = new ExtRAResponse(submessage.getRequestId(),false,"User not found from issuer/serno: issuer='"+issuerDN+"', serno="+serno);					
				}
				// If we didn't create any other return value, it was a success
				if (retval == null) {
					retval = new ExtRAResponse(submessage.getRequestId(),true,null);					
				}
			}
		} catch (AuthorizationDeniedException e) {
			log.error("Error processing ExtRARevocationRequest : ", e);
			retval = new ExtRAResponse(submessage.getRequestId(),false, "AuthorizationDeniedException: " + e.getMessage());
		} catch (FinderException e) {
			log.error("Error processing ExtRARevocationRequest : ", e);
			retval = new ExtRAResponse(submessage.getRequestId(),false, "ExtRARevocationRequest: " + e.getMessage());
		}catch(Exception e){
			log.error("Error processing ExtRARevocationRequest : ", e);
			retval = new ExtRAResponse(submessage.getRequestId(),false,e.getMessage());
		} 
		
		return retval;
	}
	
	private ISubMessage processExtRACardRenewalRequest(Admin admin, ExtRACardRenewalRequest submessage) {
		log.debug("Processing ExtRACardRenewalRequest");
		ExtRAResponse retval = null;
		try {
			X509Certificate authcert = submessage.getAuthCertificate();
			X509Certificate signcert = submessage.getSignCertificate();
			String authReq = submessage.getAuthPkcs10();
			String signReq = submessage.getSignPkcs10();
			if ( (authcert == null) || (signcert == null) || (authReq == null) || (signReq == null) ) {
				retval = new ExtRAResponse(submessage.getRequestId(),false,"An authentication cert, a signature cert, an authentication request and a signature request are required");
			} else {
				BigInteger serno = authcert.getSerialNumber();
				String issuerDN = authcert.getIssuerDN().getName();
                // Verify the certificates with CA cert, and then verify the pcks10 requests
                CertificateInfo authInfo = getCertStoreSession().getCertificateInfo(admin, CertTools.getFingerprintAsString(authcert));
                Certificate authcacert = getCertStoreSession().findCertificateByFingerprint(admin, authInfo.getCAFingerprint());
                CertificateInfo signInfo = getCertStoreSession().getCertificateInfo(admin, CertTools.getFingerprintAsString(signcert));
                Certificate signcacert = getCertStoreSession().findCertificateByFingerprint(admin, signInfo.getCAFingerprint());
                // Verify certificate
                try {
                    authcert.verify(authcacert.getPublicKey());                    
                } catch (Exception e) {
                    log.error("Error verifying authentication certificate: ", e);
                    retval = new ExtRAResponse(submessage.getRequestId(),false,"Error verifying authentication certificate: "+e.getMessage());
                    return retval;
                }
                try {
                    signcert.verify(signcacert.getPublicKey());                    
                } catch (Exception e) {
                    log.error("Error verifying signature certificate: ", e);
                    retval = new ExtRAResponse(submessage.getRequestId(),false,"Error verifying signature certificate: "+e.getMessage());
                    return retval;
                }
                // Verify requests
                byte[] authReqBytes = authReq.getBytes();
                byte[] signReqBytes = signReq.getBytes();
                PKCS10RequestMessage authPkcs10 = RequestHelper.genPKCS10RequestMessageFromPEM(authReqBytes);
                PKCS10RequestMessage signPkcs10 = RequestHelper.genPKCS10RequestMessageFromPEM(signReqBytes);
                String authok = null;
                try {
                    if (!authPkcs10.verify(authcert.getPublicKey())) {
                        authok = "Verify failed for authentication request";
                    }                    
                } catch (Exception e) {
                    authok="Error verifying authentication request: "+e.getMessage();
                    log.error("Error verifying authentication request: ", e);
                }
                if (authok != null) {
                    retval = new ExtRAResponse(submessage.getRequestId(),false,authok);
                    return retval;                                        
                }
                String signok = null;
                try {
                    if (!signPkcs10.verify(signcert.getPublicKey())) {
                        signok = "Verify failed for signature request";
                    }                    
                } catch (Exception e) {
                    signok="Error verifying signaturerequest: "+e.getMessage();
                    log.error("Error verifying signaturerequest: ", e);
                }
                if (signok != null) {
                    retval = new ExtRAResponse(submessage.getRequestId(),false,signok);
                    return retval;                                        
                }
                
                // Now start the actual work, we are ok and verified here
				String username = getCertStoreSession().findUsernameByCertSerno(admin, serno, CertTools.stringToBCDNString(issuerDN));
				if (username != null) {
		            final UserDataVO data = getUserAdminSession().findUser(admin, username);
		            if ( data.getStatus() != UserDataConstants.STATUS_NEW) {
		            	log.error("User status must be new for "+username);
						retval = new ExtRAResponse(submessage.getRequestId(),false,"User status must be new for "+username);
		            } else {
                        log.info("Processing Card Renewal for: issuer='"+issuerDN+"', serno="+serno);
                        int authCertProfile = -1;
                        int signCertProfile = -1;
                        int authCA = -1;
                        int signCA = -1;
                        // Get the profiles and CAs from the message if they exist
		            	if (submessage.getAuthProfile() != -1) {
		            		authCertProfile = submessage.getAuthProfile();
		            	}
		            	if (submessage.getSignProfile() != -1) {
		            		signCertProfile = submessage.getSignProfile();
		            	}
		            	if (submessage.getAuthCA() != -1) {
		            		authCA = submessage.getAuthCA();
		            	}
		            	if (submessage.getSignCA() != -1) {
		            		signCA = submessage.getSignCA();
		            	}
                        HardTokenProfile htp = getHardTokenSession().getHardTokenProfile(admin, data.getTokenType());
                        if ( htp!=null && htp instanceof EIDProfile ) {
                        	EIDProfile hardTokenProfile = (EIDProfile)htp;
                        	if (authCertProfile == -1) {
                        		authCertProfile = hardTokenProfile.getCertificateProfileId(SwedishEIDProfile.CERTUSAGE_AUTHENC);                        		
                        	}
                        	if (signCertProfile == -1) {
                        		signCertProfile = hardTokenProfile.getCertificateProfileId(SwedishEIDProfile.CERTUSAGE_SIGN);
                        	}
                        	if (authCA == -1) {
                        		authCA = hardTokenProfile.getCAId(SwedishEIDProfile.CERTUSAGE_AUTHENC);
                        		if (authCA == EIDProfile.CAID_USEUSERDEFINED) {
                        			authCA = data.getCAId();
                        		}
                        	}
                        	if (signCA == -1) {
                        		signCA = hardTokenProfile.getCAId(SwedishEIDProfile.CERTUSAGE_SIGN);
                        		if (signCA == EIDProfile.CAID_USEUSERDEFINED) {
                        			signCA = data.getCAId();
                        		}                        		
                        	}
                        } else {
                        	if (authCertProfile == -1) {
                        		authCertProfile = data.getCertificateProfileId();
                        	}
                        	if (signCertProfile == -1) {
                        		signCertProfile = data.getCertificateProfileId();
                        	}
                        	if (authCA == -1) {
                        		authCA = data.getCAId();
                        	}
                        	if (signCA == -1) {
                        		signCA = data.getCAId();
                        	}
                        }

		            	// Set certificate profile and CA for auth certificate
		            	getUserAdminSession().changeUser(admin, username,data.getPassword(), data.getDN(), data.getSubjectAltName(),
		            			data.getEmail(), true, data.getEndEntityProfileId(), authCertProfile, data.getType(),
		            			data.getTokenType(), data.getHardTokenIssuerId(), data.getStatus(), authCA);
		            	// We may have changed to a new autogenerated password
			            UserDataVO data1 = getUserAdminSession().findUser(admin, username);
		            	X509Certificate authcertOut=pkcs10CertRequest(admin, getSignSession(), authPkcs10, username, data1.getPassword());

		            	// Set certificate and CA for sign certificate
		            	getUserAdminSession().changeUser(admin, username, data.getPassword(), data.getDN(), data.getSubjectAltName(),
		            			data.getEmail(), true, data.getEndEntityProfileId(), signCertProfile, data.getType(),
		            			data.getTokenType(), data.getHardTokenIssuerId(), UserDataConstants.STATUS_NEW, signCA);
		            	// We may have changed to a new autogenerated password
			            data1 = getUserAdminSession().findUser(admin, username);
		            	X509Certificate signcertOut=pkcs10CertRequest(admin, getSignSession(), signPkcs10, username, data1.getPassword());

		            	// We are generated all right
		            	data.setStatus(UserDataConstants.STATUS_GENERATED);
		            	// set back to original values (except for generated)
		            	getUserAdminSession().changeUser(admin, data, true); 
		            	retval = new ExtRACardRenewalResponse(submessage.getRequestId(), true, null, authcertOut, signcertOut);
		            }
				} else {
                    log.error("User not found from issuer/serno: issuer='"+issuerDN+"', serno="+serno);
					retval = new ExtRAResponse(submessage.getRequestId(),false,"User not found from issuer/serno: issuer='"+issuerDN+"', serno="+serno);					
				}
			} 			
		} catch(Exception e) {
			log.error("Error processing ExtRACardRenewalRequest : ", e);
			retval = new ExtRAResponse(submessage.getRequestId(),false,e.getMessage());
		} 
			
		return retval;
	}
    /**
     * Handles PKCS10 certificate request, these are constructed as: <code> CertificationRequest
     * ::= SEQUENCE { certificationRequestInfo  CertificationRequestInfo, signatureAlgorithm
     * AlgorithmIdentifier{{ SignatureAlgorithms }}, signature                       BIT STRING }
     * CertificationRequestInfo ::= SEQUENCE { version             INTEGER { v1(0) } (v1,...),
     * subject             Name, subjectPKInfo   SubjectPublicKeyInfo{{ PKInfoAlgorithms }},
     * attributes          [0] Attributes{{ CRIAttributes }}} SubjectPublicKeyInfo { ALGORITHM :
     * IOSet} ::= SEQUENCE { algorithm           AlgorithmIdentifier {{IOSet}}, subjectPublicKey
     * BIT STRING }</code> PublicKey's encoded-format has to be RSA X.509.
     *
     * @param signsession signsession to get certificate from
     * @param b64Encoded base64 encoded pkcs10 request message
     * @param username username of requesting user
     * @param password password of requesting user
     * @param resulttype should indicate if a PKCS7 or just the certificate is wanted.
     *
     * @return Base64 encoded byte[] 
     * @throws ClassNotFoundException 
     * @throws SignRequestSignatureException 
     * @throws SignRequestException 
     * @throws CADoesntExistsException 
     * @throws IllegalKeyException 
     * @throws AuthLoginException 
     * @throws AuthStatusException 
     * @throws ObjectNotFoundException 
     * @throws IOException 
     * @throws CertificateException 
     * @throws CertificateEncodingException 
     */
    private X509Certificate pkcs10CertRequest(Admin administrator, ISignSessionLocal signsession, PKCS10RequestMessage req,
        String username, String password) throws NotFoundException, AuthStatusException, AuthLoginException, IllegalKeyException, CADoesntExistsException, SignRequestException, SignRequestSignatureException, ClassNotFoundException, CertificateEncodingException, CertificateException, IOException {
        X509Certificate cert=null;
		req.setUsername(username);
        req.setPassword(password);
        IResponseMessage resp = signsession.createCertificate(administrator,req,Class.forName("org.ejbca.core.protocol.X509ResponseMessage"));
        cert = CertTools.getCertfromByteArray(resp.getResponseMessage());
        return cert;
    } //pkcs10CertReq

	
    private UserDataVO generateUserDataVO(Admin admin, ExtRARequest submessage) throws ClassCastException, EjbcaException, CreateException, NamingException{
        String dirAttributes = submessage.getSubjectDirectoryAttributes();
        ExtendedInformation ext = null;
        if (dirAttributes != null) {
            ext = new ExtendedInformation();
            ext.setSubjectDirectoryAttributes(dirAttributes);
        }
           return  new UserDataVO(submessage.getUsername(),
                   submessage.getSubjectDN(),
                   getCAId(admin,submessage.getCAName()),
                   submessage.getSubjectAltName(),
                   submessage.getEmail(),
                   UserDataConstants.STATUS_INPROCESS,
                   SecConst.USER_ENDUSER,
                   getEndEntityProfileId(admin, submessage.getEndEntityProfileName()),
                   getCertificateProfileId(admin, submessage.getCertificateProfileName()),
                   null,
                   null,
                   SecConst.TOKEN_SOFT_BROWSERGEN,
                   0,
                   ext);
    }

	private KeyPair generateKeys(ExtRAPKCS12Request submessage) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		KeyPair retval = null;
		String keyalg = null;
		if(submessage.getKeyAlg() == ExtRAPKCS12Request.KEYALG_RSA){
			keyalg = CATokenConstants.KEYALGORITHM_RSA;
		} else if(submessage.getKeyAlg() == ExtRAPKCS12Request.KEYALG_ECDSA){
				keyalg = CATokenConstants.KEYALGORITHM_ECDSA;
		} else {
			throw new NoSuchAlgorithmException("Wrong Key Algorithm specified.");
		}
		retval = KeyTools.genKeys(submessage.getKeySpec(), keyalg);

		return retval;
	}

	
	/**
	 * Method that generates a response submessage depending on
	 * requried security configuration
	 * @param reqCert the requestors certificate used for encryption.
	 * @return a new instance of a SubMessage
	 * @throws UnrecoverableKeyException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
	 */	
	private SubMessages generateResponseSubMessage(X509Certificate reqCert) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
		
		if(conf.getEncryptionRequired() && conf.getSignatureRequired()){
			return new SubMessages((X509Certificate) serviceKeyStore.getKeyStore().getCertificate(serviceKeyStore.getAlias()),
					               (PrivateKey) serviceKeyStore.getKeyStore().getKey(serviceKeyStore.getAlias(), conf.getKeyStorePassword().toCharArray()),
					               reqCert);					                
		}
		if(conf.getSignatureRequired()){
			return new SubMessages((X509Certificate) serviceKeyStore.getKeyStore().getCertificate(serviceKeyStore.getAlias()),
					               (PrivateKey) serviceKeyStore.getKeyStore().getKey(serviceKeyStore.getAlias(), conf.getKeyStorePassword().toCharArray()),
					               null);					                
		}
		if(conf.getEncryptionRequired()){
			return new SubMessages(null,
					               null,
					               reqCert);					                
		}
		
		return new SubMessages(null,null,null);
	}

	/**
	 * Method returning the CACertChain used to verify the message.
	 * CAChain returned is specified in the service config xml.
	 * 
	 * @param cAName
	 * @param checkRevokation
	 * @return the CACertChain.
	 * @throws ConfigurationException if any of the CAs doesn't exist or is revoked
	 */
	private Collection getCACertChain(Admin admin, String cAName,boolean checkRevokation) throws ConfigurationException{		
		try{
			CAInfo cainfo = getCAAdminSession().getCAInfo(admin, cAName);
			if(cainfo == null){
				log.error("Misconfigured CA Name in RAService");
				throw new ConfigurationException("Misconfigured CA Name in RAService");
			}
			
			if(checkRevokation){
			  if(cainfo.getStatus()==SecConst.CA_REVOKED){
				throw new ConfigurationException("CA " + cainfo.getName() + " Have been revoked");
			  }
			
			  Iterator iter = cainfo.getCertificateChain().iterator();
			  iter.next(); // Throw away the first one.
			  while(iter.hasNext()){
				X509Certificate cacert = (X509Certificate) iter.next();
				CAInfo cainfo2 = getCAAdminSession().getCAInfo(admin,CertTools.stringToBCDNString(cacert.getSubjectDN().toString()).hashCode());
				if(cainfo2.getStatus()==SecConst.CA_REVOKED){
					throw new ConfigurationException("CA " + cainfo2.getName() + " Have been revoked");
				}
			  }
			}  
			return cainfo.getCertificateChain();
		}catch(Exception e){
			if (e instanceof ConfigurationException) {
				throw (ConfigurationException)e;
			}
			log.error("Exception getting CA cert chain: ", e);
			throw new ConfigurationException("Couldn't instantiate CAAdminSessionBean");
		}	
	}
	
	/**
	 * Method used to retrive which administrator to use.
	 * If message is signed then use the signer as admin othervise use
	 * InternalUser
	 * @throws NamingException 
	 * @throws CreateException 
	 * @throws ClassCastException 
	 * @throws SignatureException 
	 * @throws AuthorizationDeniedException 
	 * @throws RemoteException 
	 */
	private Admin getAdmin(SubMessages submessages) throws ClassCastException, CreateException, NamingException, SignatureException,  AuthorizationDeniedException{
		if(submessages.isSigned()){
			
			// Check if Signer Cert is revoked
			X509Certificate signerCert = submessages.getSignerCert();
			
			Admin admin = new Admin(signerCert);
			
			// Check that user have the administrator flag set.
			getUserAdminSession().checkIfCertificateBelongToAdmin(admin, signerCert.getSerialNumber(), signerCert.getIssuerDN().toString());
			
			RevokedCertInfo revokeResult =  getCertStoreSession().isRevoked(new Admin(Admin.TYPE_INTERNALUSER),CertTools.stringToBCDNString(signerCert.getIssuerDN().toString()), signerCert.getSerialNumber());
			if(revokeResult == null || revokeResult.getReason() != RevokedCertInfo.NOT_REVOKED){
				throw new SignatureException("Error Signer certificate doesn't exist or is revoked.");
			}
			
			
			return admin;
		}
		return internalUser;
	}
	
	private int getCertificateProfileId(Admin admin, String certificateProfileName) throws EjbcaException, ClassCastException, CreateException, NamingException{		
		int retval = getCertStoreSession().getCertificateProfileId(admin,certificateProfileName);
		if(retval == 0){
			throw new EjbcaException("Error Certificate profile " + certificateProfileName + " doesn't exists.");
		}
		return retval;
	}

	private int getEndEntityProfileId(Admin admin,String endEntityProfileName) throws EjbcaException, ClassCastException, CreateException, NamingException {
		int retval = getRAAdminSession().getEndEntityProfileId(admin,endEntityProfileName);
		if(retval == 0){
			throw new EjbcaException("Error End Entity profile " + endEntityProfileName + " doesn't exists.");
		}
		return retval;
	}

	private int getCAId(Admin admin, String cAName) throws EjbcaException, ClassCastException, CreateException, NamingException {
		int retval = getCAAdminSession().getCAInfo(admin,cAName).getCAId();
		if(retval == 0){
			throw new EjbcaException("Error CA " + cAName + " doesn't exists.");
		}
		return retval;
	}
	
	private static final String[] AVAILABLESOFTTOKENNAMES = {ExtRAEditUserRequest.SOFTTOKENNAME_USERGENERATED, 
		                                                     ExtRAEditUserRequest.SOFTTOKENNAME_P12, 
		                                                     ExtRAEditUserRequest.SOFTTOKENNAME_JKS, 
		                                                     ExtRAEditUserRequest.SOFTTOKENNAME_PEM };
	private static final int[] AVAILABLESOFTTOKENIDS = {SecConst.TOKEN_SOFT_BROWSERGEN,
		SecConst.TOKEN_SOFT_P12, 
		SecConst.TOKEN_SOFT_JKS, 
		SecConst.TOKEN_SOFT_PEM};	
	
	private int getTokenTypeId(Admin admin, String tokenName) throws EjbcaException, ClassCastException, CreateException, NamingException {
		
		for(int i=0; i< AVAILABLESOFTTOKENNAMES.length ; i++){
			if(tokenName.equalsIgnoreCase(AVAILABLESOFTTOKENNAMES[i])){
				return AVAILABLESOFTTOKENIDS[i];
			}
		}
		
		int retval = getHardTokenSession().getHardTokenProfileId(admin,tokenName);
		if(retval == 0){
			throw new EjbcaException("Error Token with name " + tokenName + " doesn't exists.");
		}
		return retval;
	}
	
	private int getHardTokenIssuerId(Admin admin, String hardTokenIssuerName) throws ClassCastException, CreateException, NamingException {
				
		int retval = getHardTokenSession().getHardTokenIssuerId(admin,hardTokenIssuerName);

		return retval;
	}
	
	private ICAAdminSessionLocal caadminsession = null;
	private ICAAdminSessionLocal getCAAdminSession() throws ClassCastException, CreateException, NamingException{ 		
	    if(caadminsession == null){	  
	    	Context context = new InitialContext();	    	
	    	caadminsession = ((ICAAdminSessionLocalHome) javax.rmi.PortableRemoteObject.narrow(context.lookup(
	    	"CAAdminSessionLocal"), ICAAdminSessionLocalHome.class)).create();   
	    }
	    return caadminsession;
	}
	
	private IRaAdminSessionLocal raadminsession = null;
	private IRaAdminSessionLocal getRAAdminSession() throws ClassCastException, CreateException, NamingException{
		if(raadminsession == null){
		  Context context = new InitialContext();
	      raadminsession = ((IRaAdminSessionLocalHome) javax.rmi.PortableRemoteObject.narrow(context.lookup(
	      "RaAdminSessionLocal"), IRaAdminSessionLocalHome.class)).create();    	           	           	        
		}
		return raadminsession;
	}
	
	private ICertificateStoreSessionLocal certificatestoresession = null;
	private ICertificateStoreSessionLocal getCertStoreSession() throws ClassCastException, CreateException, NamingException{
		if(certificatestoresession == null){
			Context context = new InitialContext();
			certificatestoresession = ((ICertificateStoreSessionLocalHome) javax.rmi.PortableRemoteObject.narrow(context.lookup(
			"CertificateStoreSessionLocal"), ICertificateStoreSessionLocalHome.class)).create();    	           	           	        
		}
		return certificatestoresession;
	}
	
	private ISignSessionLocal signsession = null;
	private ISignSessionLocal getSignSession() throws ClassCastException, CreateException, NamingException{
		if(signsession == null){
			Context context = new InitialContext();
			signsession = ((ISignSessionLocalHome) javax.rmi.PortableRemoteObject.narrow(context.lookup(
			"SignSessionLocal"), ISignSessionLocalHome.class)).create();    	           	           	        
		}
		return signsession;
	}
	
	private IKeyRecoverySessionLocal keyrecoverysession = null;
	private IKeyRecoverySessionLocal getKeyRecoverySession() throws ClassCastException, CreateException, NamingException{
		if(keyrecoverysession == null){
			Context context = new InitialContext();
			keyrecoverysession = ((IKeyRecoverySessionLocalHome) javax.rmi.PortableRemoteObject.narrow(context.lookup(
			"KeyRecoverySessionLocal"), IKeyRecoverySessionLocalHome.class)).create();    	           	           	        
		}
		return keyrecoverysession;
	}
	
	private IHardTokenSessionLocal hardtokensession = null;
	private IHardTokenSessionLocal getHardTokenSession() throws ClassCastException, CreateException, NamingException{
		if(hardtokensession == null){
			Context context = new InitialContext();
			hardtokensession = ((IHardTokenSessionLocalHome) javax.rmi.PortableRemoteObject.narrow(context.lookup(
			"HardTokenSessionLocal"), IHardTokenSessionLocalHome.class)).create();    	           	           	        
		}
		return hardtokensession;
	}
	 
	
	
	public void processReviewedRequests() {
		// Do Nothing
	}
	
	protected MessageHome getMessageHome() {
		return msgHome;
	}

	protected Query getQuery() {
		// Not used
		return null;
	}

}
