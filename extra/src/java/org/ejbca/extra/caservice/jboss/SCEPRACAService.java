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
 
package org.ejbca.extra.caservice.jboss;

import org.ejbca.extra.caservice.RACAServiceThread;
import org.jboss.system.ServiceMBeanSupport;


/** 
 * An MBean service managing an RACAService.
*/
public class SCEPRACAService extends ServiceMBeanSupport implements SCEPRACAServiceMBean
{ 

  private String polltime; 
  private String rACAServiceName;
  private String sessionFactoryContext;
  private String rACAProcessClassPath;
  private String cAName;
  private String endEntityProfileName;
  private String certificateProfileName;
  
  private RACAServiceThread racathread;

  
  public String getPolltime()
  {
     return polltime;
  }

  public void setPolltime(String polltime)
  {
  	  this.polltime = polltime;
  }

  public String getName()
  {
    return rACAServiceName;      
  }

  public void startService() throws Exception
  {
    this.racathread = new RACAServiceThread(this);
  	this.racathread.start();	 
  }
  public void stopService()
  {
  	 this.racathread.stopThread();   	
  }



public String getRACAProcessClassPath() {
	return rACAProcessClassPath;
}

public void setRACAProcessClassPath(String classpath) {
	this.rACAProcessClassPath=classpath;	
}

public String getRACAServiceName() {
	return this.rACAServiceName;
}

public void setRACAServiceName(String name) {
	this.rACAServiceName = name;
}

public String getSessionFactoryContext() {
	return sessionFactoryContext;
}

public void setSessionFactoryContext(String context) {
	this.sessionFactoryContext = context;
	
}

public String getCAName() {
	return cAName;
}

public void setCAName(String cAName) {
	this.cAName = cAName;	
}

public String getEndEntityProfileName() {
	return endEntityProfileName;
}

public void setEndEntityProfileName(String endEntityProfileName) {
	this.endEntityProfileName = endEntityProfileName;
}

public String getCertificateProfileName() {
	return certificateProfileName;
}

public void setCertificateProfileName(String certificateProfileName) {
	this.certificateProfileName = certificateProfileName;
}


  
   
}