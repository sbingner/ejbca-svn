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

public interface IExtRACAService  extends IRACAService
{

   /**
    * Indicates if recieved messages must be encrypted 
	*/
	public boolean getEncryptionRequired();
	public void setEncryptionRequired(boolean required);
	
	
	/**
	 * Indicates if recieved message must be signed
	 */
	public boolean getSignatureRequired();
	public void setSignatureRequired(boolean required);
	
	
	/**
	 * Path to key store used to sign and encrypt messages
	 */
	public String getKeyStorePath();
	public void setKeyStorePath(String keyStorePath);
	
	/**
	 * Password to unlock key store used to sign and encrypt messages
	 */
	public String getKeyStorePassword();
	public void setKeyStorePassword(String keyStorePassword);
	
	/**
	 * CA Name of the CA issuing RA Certificates
	 */
	public String getRAIssuer();
	public void setRAIssuer(String rAIssuer);
	
}
