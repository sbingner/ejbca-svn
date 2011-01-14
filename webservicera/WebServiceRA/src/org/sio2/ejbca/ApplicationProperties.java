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
 *  Copyright (c) PrimeKey Solutions AB.                                 *
 *                                                                       *
 *************************************************************************/
 
package org.sio2.ejbca;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Daniel Horn, SiO2 Corp.
 *
 * @version $Id$
*/
public class ApplicationProperties extends Properties
{
	private String urlWebService = ""; 	// eg, "https://ejbca.course:8443/ejbca/ejbcaws/ejbcaws?wsdl"

	// TODO These are stored as plain text.  They should be encrypted or obfuscated in some fashion.
	private String strKeyStore = ""; 	// "/Users/danielhorn/Downloads/test1.jks";
	private String strKeyStorePassword = "";	// "foo123"
	private String strTrustedKeyStore = ""; 	// "/Users/danielhorn/Downloads/test1.jks";
	private String strTrustedKeyStorePassword = "";	// "foo123"
	
	private String strCurrentQEProfile = "";	// QE Filename currently selected.
	
	/**
	 * 
	 */
	public ApplicationProperties()
	{
	}

	/**
	 * @param defaults
	 */
	public ApplicationProperties(Properties defaults)
	{
		super(defaults);
	}

	// TODO What should the name be?
	private static final String strFileName = "RAAdmin.properties";

	private String getPropertiesFileName()
	{
		return "." + System.getProperty("file.separator") + strFileName;
	}

	boolean load()
	{
		boolean rc = false;
		try
		{
			FileInputStream in = new FileInputStream(getPropertiesFileName());
			super.load(in);
			in.close();
			
			setUrlWebService(getProperty("EJBCA_WebServiceUrl", ""));
			setKeyStoreStr(getProperty("EJBCA_KeyStore", ""));
			strKeyStorePassword = getProperty("EJBCA_KeyStorePassword", "");
			strTrustedKeyStore = getProperty("EJBCA_TrustedKeyStore", "");
			strTrustedKeyStorePassword = getProperty("EJBCA_TrustedKeyStorePassword", "");
	
			setCurrentQEProfile(getProperty("DefaultQEProfile", ""));
			
			rc = true;
		}
		catch (FileNotFoundException ex)
		{
			// Probably should be created first time app is run, so exception here is not a problem as long as reasonable defaults are set.
//			ex.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return rc;
	}

	boolean store()
	{
		boolean rc = false;

		setProperty("EJBCA_WebServiceUrl", getUrlWebService());
		setProperty("EJBCA_KeyStore", getKeyStoreStr());
		setProperty("EJBCA_KeyStorePassword", strKeyStorePassword);
		setProperty("EJBCA_TrustedKeyStore", strTrustedKeyStore);
		setProperty("EJBCA_TrustedKeyStorePassword", strTrustedKeyStorePassword);

		setProperty("DefaultQEProfile", strCurrentQEProfile);

		try
		{
			FileOutputStream out = new FileOutputStream(getPropertiesFileName());
			super.store(out, "RA Administrator Properties");
			out.close();
			
			rc = true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return rc;
	}
	
	// The validity check here is just to see if the settings are non-empty.
	boolean isValid()
	{
		if ((0 == urlWebService.length())
				|| (0 == strKeyStore.length())
				|| (0 == strKeyStorePassword.length())
				|| (0 == strTrustedKeyStore.length())
				|| (0 == strTrustedKeyStorePassword.length())
				)
		{
			return false;
		}
		
		return true;
	}

	public String getKeyStoreStr()
	{
		return strKeyStore;
	}

	public void setKeyStoreStr(String strKeyStore)
	{
		this.strKeyStore = strKeyStore;
		setProperty("EJBCA_KeyStore", strKeyStore);
	}

	public String getKeyStorePassword()
	{
		return strKeyStorePassword;
	}

	public void setKeyStorePassword(String strKeyStorePassword)
	{
		this.strKeyStorePassword = strKeyStorePassword;
		setProperty("EJBCA_KeyStorePassword", strKeyStorePassword);
	}

	public String getTrustedKeyStoreStr()
	{
		return strTrustedKeyStore;
	}

	public void setTrustedKeyStoreStr(String strTrustedKeyStore)
	{
		this.strTrustedKeyStore = strTrustedKeyStore;
		setProperty("EJBCA_TrustedKeyStore", strTrustedKeyStore);
	}

	public String getTrustedKeyStorePassword()
	{
		return strTrustedKeyStorePassword;
	}

	public void setTrustedKeyStorePassword(String strTrustedKeyStorePassword)
	{
		this.strTrustedKeyStorePassword = strTrustedKeyStorePassword;
		setProperty("EJBCA_TrustedKeyStorePassword", strTrustedKeyStorePassword);
	}

	public String getUrlWebService()
	{
		return urlWebService;
	}

	public void setUrlWebService(String urlWebService)
	{
		this.urlWebService = urlWebService;
		setProperty("EJBCA_WebServiceUrl", urlWebService);
	}

	public void setCurrentQEProfile(String strCurrentQEProfile)
	{
		this.strCurrentQEProfile = strCurrentQEProfile;
		setProperty("DefaultQEProfile", strCurrentQEProfile);
	}

	public String getCurrentQEProfile()
	{
		return strCurrentQEProfile;
	}
	
	// For debugging:
	public String toString()
	{
		String result = "UrlWebService: " + getProperty("EJBCA_WebServiceUrl", "")
			+ "\nKeyStore: " + getProperty("EJBCA_KeyStore", "")
			+ "\nKeyStorePassword: " + getProperty("EJBCA_KeyStorePassword", "")
			+ "\nTrustedKeyStore: " + getProperty("EJBCA_TrustedKeyStore", "")
			+ "\nTrustedKeyStorePassword: " + getProperty("EJBCA_TrustedKeyStorePassword", "")
			+ "\nCurrentQEProfile: " + getProperty("DefaultQEProfile", "");
		
		return result;
	}

}
