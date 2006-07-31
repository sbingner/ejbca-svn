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

/** A service base configuration interface for an RACAService.
*/
public interface IRACAService
{    
  public String getPolltime();  
  public void setPolltime(String Polltime); 
  
  public String getRACAServiceName();
  public void setRACAServiceName(String name);
  
  public String getRACAProcessClassPath();
  public void setRACAProcessClassPath(String classpath);  
  
  public String getSessionFactoryContext();
  public void setSessionFactoryContext(String context);
  
}
