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

import java.util.Properties;

import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.ejbca.core.ejb.approval.IApprovalSessionLocal;
import org.ejbca.core.ejb.approval.IApprovalSessionLocalHome;
import org.ejbca.core.ejb.ra.IUserAdminSessionLocal;
import org.ejbca.core.ejb.ra.IUserAdminSessionLocalHome;
import org.ejbca.core.model.approval.ApprovalDataVO;
import org.ejbca.core.model.approval.ApprovalException;
import org.ejbca.core.model.approval.WaitingForApprovalException;
import org.ejbca.core.model.approval.approvalrequests.AddEndEntityApprovalRequest;
import org.ejbca.core.model.approval.approvalrequests.EditEndEntityApprovalRequest;
import org.ejbca.core.model.authorization.AuthorizationDeniedException;
import org.ejbca.core.model.log.Admin;
import org.ejbca.core.model.ra.UserDataConstants;
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
 * $Id: RACAProcess.java,v 1.6 2007-03-29 15:13:11 anatom Exp $
 */

public abstract class RACAProcess {

	private static Logger log = Logger.getLogger(RACAProcess.class);
	
	private IUserAdminSessionLocal usersession = null;
	private IApprovalSessionLocal approvalsession = null;
	private Properties properties = null;
	
	
	/**
	 * Method performin initialization, should be called directly after creation
	 * @throws ConfigurationException 
	 */
	public void init(IRACAService configuration) throws ConfigurationException {
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
	 * Help method used to store userdata in userdatabase with given status, that is
	 * waiting for user to be reviewed. This methid handles approval as well.
	 * 
	 * @throws UserDoesntFullfillEndEntityProfile 
	 * @throws AuthorizationDeniedException 
	 * @throws FinderException 
	 * @throws DuplicateKeyException 
	 * @throws WaitingForApprovalException 
	 * @throws ApprovalException
	 * @throws Exception 
	 * 
	 */
	protected void storeUserData(Admin admin, UserDataVO userdata, boolean clearpwd, int status) throws AuthorizationDeniedException, UserDoesntFullfillEndEntityProfile, DuplicateKeyException, FinderException, ApprovalException, WaitingForApprovalException, Exception {
		log.debug(">storeUserData() username : " + userdata.getUsername());

        // First we will look to see if there is an existing approval request pending for this user
		// If there is an existing reject, we will fail
		EditEndEntityApprovalRequest ear = new EditEndEntityApprovalRequest(userdata, clearpwd, userdata, admin,null,1,userdata.getCAId(),userdata.getEndEntityProfileId());
        AddEndEntityApprovalRequest aar = new AddEndEntityApprovalRequest(userdata,clearpwd,admin,null,1,userdata.getCAId(),userdata.getEndEntityProfileId());
        ApprovalDataVO vo = getApprovalSession().findNonExpiredApprovalRequest(admin, ear.generateApprovalId());
        if (vo == null) {
            vo = getApprovalSession().findNonExpiredApprovalRequest(admin, aar.generateApprovalId());        	
        }
        if (vo != null) {
        	log.debug("Found an exiting approval request with status: :"+vo.getStatus());
        }
        if ( (vo != null) && ((vo.getStatus() == ApprovalDataVO.STATUS_REJECTED) || (vo.getStatus() == ApprovalDataVO.STATUS_EXECUTIONDENIED)) ) {
        	// Our request have been rejected so we fail
        	throw new Exception("Approval request was rejected.");
        }
		// If there is an request waiting for approval we don't have to go on and try to add the user
        if ( (vo != null) && (vo.getStatus() == ApprovalDataVO.STATUS_WAITINGFORAPPROVAL) ) {
        	// Our request have been rejected so we fail
        	throw new ApprovalException("There is already an existing approval request pending.");
        }
        
		// Check if user exists
		UserDataVO oldData = getUserAdminSession().findUser(admin, userdata.getUsername());
		if (oldData != null) {
			log.debug("User '"+userdata.getUsername()+"' already exist, edit user.");
			if ( (oldData.getStatus() == UserDataConstants.STATUS_INPROCESS) || (oldData.getStatus() == UserDataConstants.STATUS_NEW) ) {
				log.info("User '"+userdata.getUsername()+"' have status NEW or INPROCESS, we will NOT edit it");
			} else {
				userdata.setStatus(status);
				getUserAdminSession().changeUser(admin,userdata,clearpwd);			  
			}
		} else {
			log.debug("User '"+userdata.getUsername()+"' does not exist, add user.");
			getUserAdminSession().addUser(admin,userdata,clearpwd);
			getUserAdminSession().setUserStatus(admin,userdata.getUsername(), status);
		}
		log.debug("<storeUserData()");
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
						IUserAdminSessionLocalHome.JNDI_NAME), IUserAdminSessionLocalHome.class)).create();   
			}
		}catch(Exception e)	{
			log.error("Error instancing User Admin Session Bean",e);
			throw new EJBException(e);
		}
		return usersession;
	}
	private IApprovalSessionLocal getApprovalSession() {
		try{
			if(approvalsession == null){
				Context context = new InitialContext();
				approvalsession = ((IApprovalSessionLocalHome) javax.rmi.PortableRemoteObject.narrow(context.lookup(
						IApprovalSessionLocalHome.JNDI_NAME), IApprovalSessionLocalHome.class)).create();   
			}
		}catch(Exception e)	{
			log.error("Error instancing Approval Session Bean",e);
			throw new EJBException(e);
		}
		return approvalsession;
	}
	
}
