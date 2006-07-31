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
import java.util.Properties;

import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.ejbca.core.ejb.ra.IUserAdminSessionLocal;
import org.ejbca.core.ejb.ra.IUserAdminSessionLocalHome;
import org.ejbca.core.model.authorization.AuthorizationDeniedException;
import org.ejbca.core.model.log.Admin;
import org.ejbca.core.model.ra.UserDataVO;
import org.ejbca.core.model.ra.raadmin.UserDoesntFullfillEndEntityProfile;
import org.ejbca.extra.db.Message;
import org.ejbca.extra.db.MessageHome;
import org.ejbca.util.query.Query;


/**
 * An abstract base class in charge of retreiving and processing waiting
 * requests from the rasrc database.
 * 
 * Main methods are processWaitingMessage() and processReviewedRequests()
 * that should be implemented by inheriting classes.
 * 
 * Contains help methods for storing users in database for approvals.
 * 
 * @author Philip Vendil
 * $Id: RACAProcess.java,v 1.1 2006-07-31 13:13:09 herrvendil Exp $
 */

public abstract class RACAProcess {

	private static Logger log = Logger.getLogger(RACAProcess.class);
	
	protected IUserAdminSessionLocal usersession = null;
	
	protected Properties properties = null;
	
	
	/**
	 * Method performin initialization, should be called directly after creation
	 * @throws IOException 
	 */
	public void init(IRACAService configuration) throws ConfigurationException{
	}
	
	
	/**
	 * Method that should return the right type of messagehome class.
	 */
	protected abstract MessageHome getMessageHome();
	
	/**
	 * Method that should return query used in the this ra service to retrive approved users.
	 */
	protected abstract Query getQuery();
	
	
	
	/**
	 * Method that should be implemented by all RACAServices.
	 * Will perform the task of checking with rasrv if there exists 
	 * any waiting messages and add them to userdatabase database as 
	 * initialized.
	 *
	 */
	public abstract void processWaitingMessage();

	/**
	 * Method that should be implemented by all RACAServices.
	 * Should check the userdatabase if there exists any
	 * users that have been approved or rejected and send back
	 * and apropriate answer to the rasrv.
	 *
	 */
	public abstract void processReviewedRequests();
	
	/**
	 * Retreives the oldest message on ra server with status waiting 
	 * or null if no message is currently waiting
	 * 
	 */
	protected Message retrieveNextWaitingMessage(){	
		log.debug("retrieveNextWaitingMessage() ");
		return getMessageHome().getNextWaitingMessage();		
	}
	
	/**
	 * Help method used to store userdata in userdatabase with given status, tha is
	 * waiting for user to be reviewed.
	 * @throws UserDoesntFullfillEndEntityProfile 
	 * @throws AuthorizationDeniedException 
	 * @throws FinderException 
	 * @throws DuplicateKeyException 
	 * 
	 */
	protected void storeUserData(Admin admin, UserDataVO userdata, boolean clearpwd, int status) throws AuthorizationDeniedException, UserDoesntFullfillEndEntityProfile, DuplicateKeyException, FinderException{
		log.debug(">storeUserData() username : " + userdata.getUsername());
		
		// Check if user exists
			if(getUserAdminSession().findUser(admin, userdata.getUsername()) != null){
				log.debug("User " + userdata.getUsername() + " exists, update the userdata." );
				userdata.setStatus(status);
				getUserAdminSession().changeUser(admin,userdata,clearpwd);
			}else{
				log.debug(" New User " + userdata.getUsername() + ", adding userdata." );
				getUserAdminSession().addUser(admin,userdata,clearpwd);
				getUserAdminSession().setUserStatus(admin,userdata.getUsername(), status);
			}
	
		log.debug("<storeUserData()");
	}
	
	/**
	 * Help method that returns the next user that have been reviewed.
	 * Direclty after it have been retrieved, the users status is set to INPROCESS
	 * to avoid problems with concurrent processes.
	 * 
	 * @return the UserDataVO of the next user to generate answer for or null if no 
	 * reweived user exists.
	 */
	protected UserDataVO getNextReviewedUser(){
		//log.debug("getNextReviewedUser() caid : " + cAId + " endentityprofileid : " + endEntityProfileId);
		//return userdatahome.getNextProcessedUser(cAId, endEntityProfileId);
		return null;
	}

	protected void storeMessageInRA(Message msg){
		log.debug(">storeMessageInRA() MessageId : " + msg.getMessageid());
		getMessageHome().update(msg);
		log.debug("<storeMessageInRA() MessageId : " + msg.getMessageid());		
	}
	
	protected IUserAdminSessionLocal getUserAdminSession() {
		try{
			if(usersession == null){
				Context context = new InitialContext();
				usersession = ((IUserAdminSessionLocalHome) javax.rmi.PortableRemoteObject.narrow(context.lookup(
				"UserAdminSessionLocal"), IUserAdminSessionLocalHome.class)).create();   
			}
		}catch(Exception e)	{
			log.error("Error instancing User Admin Session Bean",e);
			throw new EJBException(e);
		}
		return usersession;
	}
	
}
