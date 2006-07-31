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

import java.util.Calendar;
import java.util.Date;

import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.ejbca.extra.db.HibernateUtil;
import org.hibernate.SessionFactory;




/** 
 * A thread managing the an RACAService
*/
public class RACAServiceThread extends Thread 
{ 
  private static Logger log = Logger.getLogger(RACAServiceThread.class);

    /** Constants */
  public static final String POLLTIME_DAILY      = "DAILY";	
  public static final String POLLTIME_HOURLY     = "HOURLY";
  public static final String POLLTIME_30MIN      = "30MIN";
  public static final String POLLTIME_15MIN      = "15MIN";
  public static final String POLLTIME_1MIN       = "1MIN";
  public static final String POLLTIME_30SEC      = "30SEC";
  public static final String POLLTIME_15SEC      = "15SEC";
  public static final String POLLTIME_10SEC      = "10SEC";
  public static final String POLLTIME_5SEC       = "5SEC";
  public static final String POLLTIME_1SEC       = "1SEC";
  
  /** Constants used internally */
  public static final int CHECK_DAILY      = 1;	
  public static final int CHECK_HOURLY     = 2;
  public static final int CHECK_30MIN      = 3;
  public static final int CHECK_15MIN      = 4;
  public static final int CHECK_1MIN       = 5;
  public static final int CHECK_30SEC      = 6;
  public static final int CHECK_15SEC      = 7;
  public static final int CHECK_10SEC      = 8;
  public static final int CHECK_5SEC       = 9;
  public static final int CHECK_1SEC       = 10;
  
  String serviceName = null;
  
  private RACAProcess rACAProcess = null;
  
  private boolean run = false;
  private int check = 0; 
  
   public RACAServiceThread(IRACAService configuration){
   	    super();
   	       	
   	    this.serviceName = configuration.getRACAServiceName();
   	    setPollTime(configuration.getPolltime());
   	    

   	    try{
   	        Context context = new InitialContext();
   	        
   	        HibernateUtil.setSessionFactory(HibernateUtil.SESSIONFACTORY_RAMESSAGE, (SessionFactory) context.lookup(configuration.getSessionFactoryContext()),false);
   	        

   	    }catch(Exception e){
   	        throw new EJBException(e);
   	    }  
   	       	    
        try {
        	rACAProcess = (RACAProcess) RACAServiceThread.class.getClassLoader().loadClass(configuration.getRACAProcessClassPath()).newInstance();
        	rACAProcess.init( configuration);
        } catch (ConfigurationException ce){
			log.error("Invalid configuration for RACAProcess " + serviceName + ", Message :" + ce.getMessage() + ",! Service will not start.");
			throw new EJBException(ce);        	
		} catch (Exception e) {
			log.error("Invalid RACAProcess classpath set for " + serviceName + "! Service will not start.");
			throw new EJBException(e);
		}

   	    
   }


public void run() 
   {	   
       run=true;
       
       if(rACAProcess == null){
    	 run = false; // Do not run if missconfigured.  
       }
    	   
       while(run){
           try{       	  	       	   
               sleep(getTimeToNextPoll());       	    
               try{      
                   if(run){                	   
                	 rACAProcess.processWaitingMessage();
                	 rACAProcess.processReviewedRequests();
                   }  
               }catch(Exception e){
                   log.error("Exception in RA CA Service",e);
               }       	    
           }catch( InterruptedException e){}
       }        
   }

  public void stopThread()
  {
  	this.run = false;
    this.check = 0;  	
   // HibernateUtil.closeSession(HibernateUtil.SESSIONFACTORY_RAMESSAGE);
  
  }
  
  private void setPollTime(String polltime) {
	  check = CHECK_1MIN;
	  if(polltime.equals(POLLTIME_DAILY)) check = CHECK_DAILY;
	  if(polltime.equals(POLLTIME_HOURLY))check = CHECK_HOURLY;
	  if(polltime.equals(POLLTIME_30MIN)) check = CHECK_30MIN;
	  if(polltime.equals(POLLTIME_15MIN)) check = CHECK_15MIN;
	  if(polltime.equals(POLLTIME_1MIN))  check = CHECK_1MIN;
	  if(polltime.equals(POLLTIME_30SEC)) check = CHECK_30SEC;
	  if(polltime.equals(POLLTIME_15SEC)) check = CHECK_15SEC;
	  if(polltime.equals(POLLTIME_10SEC)) check = CHECK_10SEC;
	  if(polltime.equals(POLLTIME_5SEC))  check = CHECK_5SEC;
	  if(polltime.equals(POLLTIME_1SEC))  check = CHECK_1SEC;		
	}
  
  
  /**
   * Method calculating the time in milliseconds to the next Poll  
   */  
  private long getTimeToNextPoll(){     
	  
      long nexttime = 0;
      Calendar nextcalendar = Calendar.getInstance();

      switch(check){
           case CHECK_DAILY :
               nextcalendar.add(Calendar.DATE,1);
               nextcalendar.set(Calendar.HOUR_OF_DAY,0);
               nextcalendar.set(Calendar.MINUTE, 0);
               nextcalendar.set(Calendar.SECOND, 0);
               nexttime = nextcalendar.getTimeInMillis();               
               break;
           case CHECK_HOURLY :
               nextcalendar.add(Calendar.HOUR_OF_DAY,1);               
               nextcalendar.set(Calendar.MINUTE, 0);
               nextcalendar.set(Calendar.SECOND, 0);
               nexttime = nextcalendar.getTimeInMillis();
               break;
           case CHECK_30MIN :               
               nextcalendar.add(Calendar.MINUTE,(30 - (nextcalendar.get(Calendar.MINUTE) % 30)));               
               nextcalendar.set(Calendar.SECOND, 0);
               nexttime = nextcalendar.getTimeInMillis();
               break;
           case CHECK_15MIN :
               nextcalendar.add(Calendar.MINUTE,(15 - (nextcalendar.get(Calendar.MINUTE) % 15)));               
               nextcalendar.set(Calendar.SECOND, 0);
               nexttime = nextcalendar.getTimeInMillis(); 
               break;
           case CHECK_1MIN :
               nextcalendar.add(Calendar.MINUTE,1);               
               nextcalendar.set(Calendar.SECOND, 0);
               nexttime = nextcalendar.getTimeInMillis(); 
               break;
           case CHECK_30SEC :
               nexttime = (new Date()).getTime() + 30000; 
               break;
           case CHECK_15SEC :
               nexttime = (new Date()).getTime() + 15000;
               break;
           case CHECK_10SEC :
               nexttime = (new Date()).getTime() + 10000; 
               break;
           case CHECK_5SEC :
               nexttime = (new Date()).getTime() + 5000; 
               break;
           case CHECK_1SEC :
               nexttime = (new Date()).getTime() + 1000;
               break;               
           default : 
               log.error("Invalid Polltime set for " + serviceName + "! Using 1 minute.");
               nextcalendar.add(Calendar.MINUTE,1);               
               nextcalendar.set(Calendar.SECOND, 0);
               nexttime = nextcalendar.getTimeInMillis(); 
               break;
               
      }            

      return nexttime - (new Date()).getTime();
  }

 
    
}