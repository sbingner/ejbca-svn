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

/** A service configuration interface that manages an SCEPRACAService
*/
public interface ISCEPRACAService extends IRACAService
{    
  
  public String getCAName();
  public void setCAName(String cAName);
  
  public String getEndEntityProfileName();
  public void setEndEntityProfileName(String endEntityProfileName);
  
  public String getCertificateProfileName();
  public void setCertificateProfileName(String certificateProfileName);
}
