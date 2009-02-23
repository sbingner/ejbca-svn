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

import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;



public class HibernateUtil {

	public static final String defaultHibernateResource = "hibernate1.cfg.xml";

	
	public static final int SESSIONFACTORY_RAMESSAGE = 0;
		
	private static final int NUMBEROFTYPES = 1;
    private SessionFactory[] sessionFactories = new SessionFactory[NUMBEROFTYPES];
    private Session[] sessions = new Session[NUMBEROFTYPES];
    
    
    private boolean manageTransactions = true;

    /**
     * Method that sets the session factory to use.
     * 
     * @param type Should be one of the SESSIONFACTORY_ constants
     * @param factory the session factory
     * @param manageTransactions, set to true if application should manage it's own transactions.
     */
    public HibernateUtil(int type, SessionFactory factory, boolean manageTransactions) {
        sessionFactories[type] = factory;
        this.manageTransactions = manageTransactions;
    }

    /**
     * Method that retrieves the current session
     * 
     * @param type Should be one of the SESSIONFACTORY_ constants
     */    
    public Session currentSession(int type) {
    	
        if (sessions[type] == null) {
        	sessions[type] = sessionFactories[type].openSession();
        	sessions[type].setCacheMode(CacheMode.IGNORE);
        }else{
        	if(!sessions[type].isConnected()){
        		sessions[type].close();
        		sessions[type] = sessionFactories[type].openSession(); 
        		sessions[type].setCacheMode(CacheMode.IGNORE);
        	}
        }
        
        
        return sessions[type];
    }

    /**
     * Method that retrieves and closes the session
     * 
     * @param type Should be one of the SESSIONFACTORY_ constants
     */
    public void closeSession(int type) {
        if (sessions[type] != null){
        	sessions[type].flush();
        	sessions[type].close();
        }
        
        sessions[type]=null;
    }

    /**
     * Method that retrieves and closes the session factory
     * 
     * @param type Should be one of the SESSIONFACTORY_ constants
     */
    public void closeSessionFactory(int type) {
        if (sessionFactories[type] != null){
        	sessionFactories[type].close();
        }
        
        sessionFactories[type]=null;
    }

    /**
     * 
     * @return  true if application should manage it's own transactions.
     */
	public boolean isManageTransactions() {
		return manageTransactions;
	}
    

}
