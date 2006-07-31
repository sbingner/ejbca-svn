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

import java.security.cert.X509Certificate;

import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.FinderException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionLocal;
import org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionLocalHome;
import org.ejbca.core.ejb.ca.sign.ISignSessionLocal;
import org.ejbca.core.ejb.ca.sign.ISignSessionLocalHome;
import org.ejbca.core.ejb.ca.store.ICertificateStoreSessionLocal;
import org.ejbca.core.ejb.ca.store.ICertificateStoreSessionLocalHome;
import org.ejbca.core.ejb.ra.raadmin.IRaAdminSessionLocal;
import org.ejbca.core.ejb.ra.raadmin.IRaAdminSessionLocalHome;
import org.ejbca.core.model.SecConst;
import org.ejbca.core.model.authorization.AuthorizationDeniedException;
import org.ejbca.core.model.ca.SignRequestSignatureException;
import org.ejbca.core.model.ca.caadmin.CAInfo;
import org.ejbca.core.model.log.Admin;
import org.ejbca.core.model.ra.SCEPRAExtendedInformation;
import org.ejbca.core.model.ra.UserDataConstants;
import org.ejbca.core.model.ra.UserDataVO;
import org.ejbca.core.model.ra.raadmin.UserDoesntFullfillEndEntityProfile;
import org.ejbca.core.protocol.ScepRequestMessage;
import org.ejbca.core.protocol.ScepResponseMessage;
import org.ejbca.extra.db.Message;
import org.ejbca.extra.db.MessageHome;
import org.ejbca.extra.db.SCEPSubMessage;
import org.ejbca.extra.db.SubMessages;
import org.ejbca.util.CertTools;
import org.ejbca.util.query.Query;


/**
 * Class in charge of retreiving and processing waiting SCEP Request
 * requests from the rasrc database.
 * 
 * 
 * @author Philip Vendil
 * $Id: SCEPRACAProcess.java,v 1.1 2006-07-31 13:13:09 herrvendil Exp $
 */
public class SCEPRACAProcess extends RACAProcess {
	
	private static Logger log = Logger.getLogger(RACAProcess.class);

	private MessageHome msgHome = new MessageHome(MessageHome.MESSAGETYPE_SCEPRA);
	
	private ISignSessionLocal signsession = null;
	
	private Admin admin = new Admin(Admin.TYPE_INTERNALUSER);
	
	private int cAId = 0;
	private int endEntityProfileId = 0;
	private int certificateProfileId = 0;
	
	protected MessageHome getMessageHome() {
		return msgHome;
	}
	
	public void init( IRACAService configuration) throws ConfigurationException{
		super.init( configuration);
		
		
		
		if(!(configuration instanceof ISCEPRACAService)){
			throw new ConfigurationException("Wrong RACAService Interface configured, must be ISCEPRACAService.");
		}
		
		ISCEPRACAService scepconf = (ISCEPRACAService) configuration;
		
		try{
		Context context = new InitialContext();
		
		ICAAdminSessionLocal caadminsession = ((ICAAdminSessionLocalHome) javax.rmi.PortableRemoteObject.narrow(context.lookup(
		"CAAdminSessionLocal"), ICAAdminSessionLocalHome.class)).create();   
		CAInfo cainfo = caadminsession.getCAInfo(admin, scepconf.getCAName());
		if(cainfo == null){
			log.error("Misconfigured CA Name in RAService");
			throw new ConfigurationException("Misconfigured CA Name in RAService");
		}
		cAId = cainfo.getCAId();
		
		IRaAdminSessionLocal raadminsession = ((IRaAdminSessionLocalHome) javax.rmi.PortableRemoteObject.narrow(context.lookup(
		"RaAdminSessionLocal"), IRaAdminSessionLocalHome.class)).create();    	           	           	        
		endEntityProfileId = raadminsession.getEndEntityProfileId(admin, scepconf.getEndEntityProfileName());
		if(endEntityProfileId == 0){
			log.error("Misconfigured End Entity Profile in RAService");
			throw new ConfigurationException("Misconfigured End Entity Profile in RAService");
		}
		
		ICertificateStoreSessionLocal certificatestoresession = ((ICertificateStoreSessionLocalHome) javax.rmi.PortableRemoteObject.narrow(context.lookup(
		"CertificateStoreSessionLocal"), ICertificateStoreSessionLocalHome.class)).create();    	           	           	        
		certificateProfileId = certificatestoresession.getCertificateProfileId(admin, scepconf.getCertificateProfileName());
		if(certificateProfileId == 0){
			log.error("Misconfigured Certificate Profile in RAService");
			throw new ConfigurationException("Misconfigured Certificate Profile in RAService");
		}
		}catch(NamingException ne){
			log.error("Error instancing  Session Beans",ne);
			throw new ConfigurationException("Error instancing  Session Beans",ne);			
		} catch (ClassCastException e) {
			log.error("Error instancing  Session Beans",e);
			throw new ConfigurationException("Error instancing  Session Beans",e);
		} catch (CreateException e) {
			log.error("Error instancing  Session Beans",e);
			throw new ConfigurationException("Error instancing  Session Beans",e);
		}
	}

	public void processWaitingMessage() {
		log.debug(">processWaitingMessage()");
		Message msg = null;
		do{
			msg = retrieveNextWaitingMessage();
			if(msg != null){
				
				
				ScepRequestMessage scepmessage = verifyAndDecryptMessage(msg);
				if(scepmessage != null){
					
					try {
						extractUserDataAndAdd(msg.getMessageid(), new String(Base64.encode(((SCEPSubMessage) msg.getSubMessages(null,null,null).getSubMessages().iterator().next()).getScepData())), scepmessage.getSignerCert());
					} catch (DuplicateKeyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (AuthorizationDeniedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UserDoesntFullfillEndEntityProfile e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FinderException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					sendSCEPFailedMessage(msg.getMessageid(), new String(Base64.encode(((SCEPSubMessage) msg.getSubMessages(null,null,null).getSubMessages().iterator().next()).getScepData())));
				}
			}
		}while (msg != null);
		
		log.debug("<processWaitingMessage()");
	}

	public void processReviewedRequests() {
		log.debug(">processReviewedRequests()");
		UserDataVO userdata = null;
		do{
		  userdata = getNextReviewedUser();		  
		  if(userdata !=null){
			  log.debug("  Reviewed user found " + userdata.getUsername() + " with status " +userdata.getStatus());
			  if(userdata.getStatus() == UserDataConstants.STATUS_NEW){
				  genCertAndSendToRA(userdata);
			  }
			  if(userdata.getStatus() == UserDataConstants.STATUS_FAILED){
				  sendSCEPFailedMessage(userdata);
			  }
		  }
		}while(userdata != null);
		
		log.debug("<processReviewedRequests()");
	}
	
	/**
	 * Method that decrypts and verifies the SCEP request, returns null
	 * if message was faulty.
	 * 
	 * @param user, the unique username
	 * @param msg, the actual message
	 * @return a SCEPRequestMessage or null if message couldn't be verified
	 */
	private ScepRequestMessage verifyAndDecryptMessage(Message msg){
		log.debug(">verifyAndDecryptMessage()");
		ScepRequestMessage scepreq = null;
		try{
		    scepreq = new ScepRequestMessage(((SCEPSubMessage) msg.getSubMessages(null,null,null).getSubMessages().iterator().next()).getScepData(),false);
			scepreq = (ScepRequestMessage) getSignSession().decryptAndVerifyRequest(admin, scepreq);
		} catch (SignRequestSignatureException e) {
			// Message couldn't be verified
			scepreq = null;
		} catch (Exception e) {
			log.error("Error decrypting and verifying scep message ",e);			
		} 
		
		log.debug("<verifyAndDecryptMessage()");
		return scepreq;
	}
	
	/**
	 * Method that should exctract the userdata from the SCEP message
	 * and add the user to the database as 'initialized'.
	 * 
	 * @param msg should be a decrypted and verified SCEP Message
	 * @throws FinderException 
	 * @throws UserDoesntFullfillEndEntityProfile 
	 * @throws AuthorizationDeniedException 
	 * @throws DuplicateKeyException 
	 */
	private void extractUserDataAndAdd(String user, String msg, X509Certificate signercert) throws DuplicateKeyException, AuthorizationDeniedException, UserDoesntFullfillEndEntityProfile, FinderException{
		log.debug(">extractUserDataAndAdd()");		
		String dn = signercert.getSubjectDN().toString();
		String subjectaltname = null;
		try {
			subjectaltname = CertTools.getSubjectAlternativeName(signercert);
		} catch (Exception e) {
			log.error("Error extracting subject altname from signing certificate .", e);
		}
		SCEPRAExtendedInformation scepextendedinfo = new SCEPRAExtendedInformation(msg, user);
		UserDataVO userdata = new UserDataVO(user,dn,cAId,subjectaltname,null,UserDataConstants.STATUS_INITIALIZED,SecConst.USER_ENDUSER,endEntityProfileId,certificateProfileId,null,null,SecConst.TOKEN_SOFT_BROWSERGEN,0,scepextendedinfo);
		userdata.setPassword("foo123");
		storeUserData(admin, userdata,true, UserDataConstants.STATUS_INITIALIZED);
		log.debug("<extractUserDataAndAdd()");		
	}
	
	/**
	 * Method that creates a SCEP failed message when a user
	 * have been rejected.
	 * 
	 * @param userdata
	 */
	private void sendSCEPFailedMessage(UserDataVO userdata){
		log.debug(">sendSCEPFailedMessage() userdata : " + userdata.getUsername());
		SCEPRAExtendedInformation scepinfo = (SCEPRAExtendedInformation) userdata.getExtendedinformation();
		sendSCEPFailedMessage(scepinfo.getUser(),scepinfo.getSCEPRequest());
		log.debug("<sendSCEPFailedMessage()");			
	}

	/**
	 * Method that creates a SCEP failed message when the
	 * request coulnd't be decrypted or verified.
	 * 
	 * @param user the unique username used by rasrv.
	 * @param scepmessage 
	 */
	private void sendSCEPFailedMessage(String user, String scepmessage){
		log.debug(">sendSCEPFailedMessage() user : " + user );
		try {
			ScepRequestMessage scepmsg = new ScepRequestMessage(Base64.decode(scepmessage.getBytes()),false);
			ScepResponseMessage res = (ScepResponseMessage) getSignSession().createRequestFailedResponse(admin, scepmsg, Class.forName("se.anatom.ejbca.protocol.ScepResponseMessage"));
			Message msg = msgHome.findByMessageId(user);
			SubMessages submsgs = new SubMessages();
			submsgs.addSubMessage(new SCEPSubMessage(res.getResponseMessage()));
			msg.setSubMessages(submsgs);
			msg.setStatus(Message.STATUS_PROCESSED);
			msgHome.update(msg);
		} catch (Exception e) {
			log.error("Error when creating or storing a SCEP Failed Message", e);
		}
		
		log.debug("<sendSCEPFailedMessage()");			
	}
	
	/**
	 * Method called after a user have been approved for certificate.
	 * 
	 * It will issue the certificate, create a SCEP Successful (or failure) response
	 * and store it to rasrv database.
	 */
	private void genCertAndSendToRA(UserDataVO userdata){
		log.debug(">genCertAndSendToRA()");		
		try {
			String user = ((SCEPRAExtendedInformation) userdata.getExtendedinformation()).getUser();
			ScepRequestMessage scepmsg = new ScepRequestMessage(Base64.decode(((SCEPRAExtendedInformation) userdata.getExtendedinformation()).getSCEPRequest().getBytes()),false);
			ScepResponseMessage res = (ScepResponseMessage) getSignSession().createCertificate(admin, scepmsg, -1, Class.forName("se.anatom.ejbca.protocol.ScepResponseMessage"));
			Message msg = msgHome.findByMessageId(user);
			SubMessages submsgs = new SubMessages();
			submsgs.addSubMessage(new SCEPSubMessage(res.getResponseMessage()));
			msg.setSubMessages(submsgs);		
			msg.setStatus(Message.STATUS_PROCESSED);
			msgHome.update(msg);
		} catch (Exception e) {
			log.error("Error generating approved scep request", e);
		} 		
		log.debug("<genCertAndSendToRA()");			
	}
	
	private ISignSessionLocal getSignSession(){
		if(signsession == null){
			try {
   	            Context context = new InitialContext();
				signsession = ((ISignSessionLocalHome) javax.rmi.PortableRemoteObject.narrow(context.lookup(
					"SignSessionLocal"), ISignSessionLocalHome.class)).create();
			} catch (Exception e) {
				log.error("Error creating SignSession", e);				
			}  
		}
		
		return signsession;
	}

	protected Query getQuery() {
		// TODO Auto-generated method stub
		return null;
	}

}
