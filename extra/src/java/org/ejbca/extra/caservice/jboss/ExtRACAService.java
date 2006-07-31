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
public class ExtRACAService extends ServiceMBeanSupport implements ExtRACAServiceMBean
{ 

  private String  polltime; 
  private String  rACAServiceName;
  private String  sessionFactoryContext;
  private String  rACAProcessClassPath;
  private boolean encryptionRequired;
  private boolean signatureRequired;
  private String  keyStorePath;
  private String keyStorePassword;
  private String rAIssuer;
  
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

public boolean getEncryptionRequired() {
	return encryptionRequired;
}

public void setEncryptionRequired(boolean required) {
    encryptionRequired = required;	
}

public boolean getSignatureRequired() {
    return signatureRequired;
}

public void setSignatureRequired(boolean required) {
	signatureRequired = required;
}

public String getKeyStorePath() {
	return keyStorePath;
}

public void setKeyStorePath(String keyStorePath) {
   this.keyStorePath = keyStorePath;	
}

public String getKeyStorePassword() {
	return keyStorePassword;
}

public void setKeyStorePassword(String keyStorePassword) {
	this.keyStorePassword = keyStorePassword;	
}

public String getRAIssuer() {
	return rAIssuer;
}

public void setRAIssuer(String rAIssuer) {
	this.rAIssuer = rAIssuer;	
}


  
   
}