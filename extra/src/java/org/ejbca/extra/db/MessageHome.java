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
package org.ejbca.extra.db;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;


/**
 * Home object for domain model class Message.
 * @see org.ejbca.extra.db.Message
 * @author Hibernate Tools
 * @version $Id: MessageHome.java,v 1.2 2007-05-15 12:57:59 anatom Exp $
 */
public class MessageHome {

	public static final Integer MESSAGETYPE_EXTRA = Integer.valueOf(1);
	public static final Integer MESSAGETYPE_SCEPRA = Integer.valueOf(2);

	
    private static final Log log = LogFactory.getLog(MessageHome.class);

    private final Integer type;

    
    public MessageHome(Integer type){
    	this.type = type;
    }
     

    
    /**
     * Creates a message and store it to database with status waiting.
     * If a message already exists, it will be overwritten.
     * 
     * @param Messageid, the unique message id.
     * @param message, the actual message string.
     * @return String, the uniqueId that is the primare key in the database
     */
    
    public String create(String messageid, SubMessages submessages){
    	log.debug(">create : Message, messageid : " + messageid);
    	Message msg = new Message(messageid,type);
        String ret = msg.getUniqueId();
    	Transaction transaction = null;
        try {
        	Session session = HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE);   
        	if(HibernateUtil.isManageTransactions()){
        	  transaction = session.beginTransaction();
        	}
           	Message data = (Message) session.get(Message.class, msg.getUniqueId());
           	
           	
           	
        	if(data != null){
        		data.update(submessages,Message.STATUS_WAITING);
        		data.setCreatetime(new Date().getTime());
        	}else{
        		data =  new Message(messageid,type);
        		data.setSubMessages(submessages);
        		session.persist(data);   
        	}		
        	HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE).flush();
        	if(HibernateUtil.isManageTransactions()){
        		transaction.commit();
          	}
 
            log.debug("create successful");                         
        }
        catch (RuntimeException re) {
            log.error("create failed", re);
        	if(HibernateUtil.isManageTransactions() && transaction != null){
        		transaction.rollback();
        	}           
            throw re;
        }finally{
        	//HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE).close();
        }
    	log.debug("<create : Message, messageid : " + messageid);
        return ret;
    }
   
    /**
     * Method that updates the message data to the database. 
     * 
     * @param msg the message class.
     */
    public void update(Message msg){
      log.debug(">update : Message, Messageid : " + msg.getMessageid()); 
      Transaction transaction = null;
      try {    	  
    	  msg.setModifytime(new Date().getTime());
      	  Session session = HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE);   
          if(HibernateUtil.isManageTransactions()){
      	    transaction = session.beginTransaction();
      	  }
    	  try{
            session.update(msg);
            HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE).flush();
        	if(HibernateUtil.isManageTransactions()){
        		transaction.commit();
          	}   	  
            log.debug("update successful");
    	  }catch(HibernateException e){
    		  log.error("update failed", e);
          	if(HibernateUtil.isManageTransactions()){
          		transaction.rollback();
          	}
    		
    		  throw e;
    	  }
      }
      catch (RuntimeException re) {
          log.error("update failed", re);
          if(HibernateUtil.isManageTransactions() && transaction != null){
    		transaction.rollback();
    	  }       
          throw re;
      }finally{
      	//HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE).close();
      }        
            
      log.debug("<update : Message, Messageid : " + msg.getMessageid());
    }
    
    /**
     * Method that removes a message from the database.
     * 
     * @param Messageid, the unique message id.
     */
    public void remove(String Messageid){
        log.debug(">remove : Message, Messageid : " + Messageid);
        Message msg = new Message(Messageid,type);
        Transaction transaction = null;
        try {
           	Session session = HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE);   
            if(HibernateUtil.isManageTransactions()){
          	    transaction = session.beginTransaction();
          	  }
        	try{
        	  Message instance = (Message) HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE).get("org.ejbca.extra.db.Message", msg.getUniqueId());
        	  HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE).delete(instance);
        	  HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE).flush();
            	if(HibernateUtil.isManageTransactions()){
              		transaction.commit();
              	}
        	  log.debug("delete successful");
        	}catch(HibernateException e){
        		log.error("delete failed", e);
              	if(HibernateUtil.isManageTransactions()){
              		transaction.rollback();
              	}
            	throw e;
            }  
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            if(HibernateUtil.isManageTransactions() && transaction != null){
        		transaction.rollback();
        	} 
            throw re;
        }finally{
        //	HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE).close();
        }
              
        log.debug("<remove : Message, Messageid : " + Messageid);
      }
        
    /**
     * Method that finds the Message for the unique messageId.
     * 
     * This method does not alter the state or lock the message 
     * in any way.
     * 
     * @param Messageid, the unique message id.
     * @return the Message or null if user doesn't exist in database.
     * 
     * @deprecated Use findByMessageId instead
     */
    public Message findByUser(String Messageid) {
       return findByMessageId(Messageid);
    }
    
    /**
     * Method that finds the Message for the unique messageid.
     * 
     * This method does not alter the state or lock the message 
     * in any way.
     * 
     * @param messageID the unique message id.
     * @return the Message or null if message doesn't exist in database.
     */
    public Message findByMessageId(String Messageid) {
        log.debug(">findByMessageId Message with Messageid: " + Messageid);
        Message msg = new Message(Messageid,type);
        try {
        	HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE).flush();
            Message instance = (Message) HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE)
                    .get(Message.class, msg.getUniqueId());
            if (instance==null) {
                log.debug("get successful, no instance found");
            }
            else {
            	HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE).refresh(instance);
                log.debug("get successful, instance found");
            }
            log.debug("<findByMessageId Message with Messageid: " + Messageid);
            return instance;
        }
        catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }finally{
        //	HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE).close();
        }
    }
    
    /**
     * Method that finds the oldest created Message with status waiting.
     * 
     * This method will for concurrency reasons be update the message
     * with status STATUS_INPROCESS in one transaction
     * to avoid confilicts.
     * 
     * @return the Message or null if no message is waiting.
     * @deprecated use getNextWaitingMessage instead.
     */
    public Message getNextWaitingUser() {
      return getNextWaitingMessage();
    }
    
    
    /**
     * Method that finds the oldest created Message with status waiting.
     * 
     * This method will for concurrency reasons be update the message
     * with status STATUS_INPROCESS in one transaction
     * to avoid confilicts.
     * 
     * @return the Message or null if no message is waiting.
     */
    public Message getNextWaitingMessage() {
        log.debug(">getNextWaitingMessage()");
        Transaction transaction = null;
        try {
        	Session session = HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE);
            if(HibernateUtil.isManageTransactions()){
          	    transaction = session.beginTransaction();
          	  }
            try{        	
            	
            	List messages = HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE).createCriteria(Message.class)
            	.add( Restrictions.eq("status", Message.STATUS_WAITING) )
            	.addOrder( Order.asc("createtime"))
            	.setFetchSize(1)
            	//.setLockMode(LockMode.UPGRADE)
            	.list();           	       
            	
            	Iterator iter = messages.iterator();
            	if(iter.hasNext()){
            		Message msg = (Message) iter.next();
            		session.refresh(msg);
            		msg.setModifytime(new Date().getTime());
            		msg.setStatus(Message.STATUS_INPROCESS);
            		session.update(msg);
            		session.flush();
                	if(HibernateUtil.isManageTransactions()){
                  		transaction.commit();
                  	}
            		log.debug("<getNextWaitingMessage() : Message " + msg.getMessageid() +" found");
            		return msg;
            	}     
            }catch(HibernateException e){
            	log.error("get failed", e);
              	if(HibernateUtil.isManageTransactions()){
              		transaction.rollback();
              	}
            	throw e;
            }
            
            log.debug("<getNextWaitingMessage() : No Message found");
        	return null;
        }
        catch (RuntimeException re) {        	
        	log.error("get failed", re);
            if(HibernateUtil.isManageTransactions() && transaction != null){
        		transaction.rollback();
        	} 
        	throw re;
        }finally{
        	//HibernateUtil.currentSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE).close();
        }
    }
 

}