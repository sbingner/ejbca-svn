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

	
	public static final int SESSIONFACTORY_RAMESSAGE = 0;
		
	private static final int NUMBEROFTYPES = 1;
    private static SessionFactory[] sessionFactories = new SessionFactory[NUMBEROFTYPES];
    private static Session[] sessions = new Session[NUMBEROFTYPES];
    
    
    private static boolean manageTransactions = true;

    /**
     * Method that sets the sessionfactory to use.
     * 
     * @param type Should be one of the SESSIONFACTORY_ constants
     * @param factory the sessionfactory
     * @param manageTransactions, set to true if application should manage it's own transactions.
     */
    public static void setSessionFactory(int type, SessionFactory factory, boolean manageTransactions) {
        HibernateUtil.sessionFactories[type] = factory;
        HibernateUtil.manageTransactions = manageTransactions;
    }

    /**
     * Method that retreives the current session
     * 
     * @param type Should be one of the SESSIONFACTORY_ constants
     */    
    public static Session currentSession(int type) {
    	
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
     * Method that retreives the closes session
     * 
     * @param type Should be one of the SESSIONFACTORY_ constants
     */
    public static void closeSession(int type) {
        if (sessions[type] != null){
        	sessions[type].flush();
        	sessions[type].close();
        }
        
        sessions[type]=null;
    }
    
    /**
     * 
     * @return  true if application should manage it's own transactions.
     */
	public static boolean isManageTransactions() {
		return manageTransactions;
	}
    

}
