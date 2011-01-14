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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.ciscavate.cjwizard.WizardSettings;
import org.ejbca.core.protocol.ws.client.gen.NameAndId;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */
public class QuickEnrollmentInfo
{
	private NameAndId EndEntityProfile = null;
	private NameAndId CertificateProfile = null;
	private NameAndId CertificateAuthority = null;

	private String OrganizationUnit = null;

	private boolean bAutoGeneratePassword = false;
	private boolean bSendEmailNotification = false;

	private String KeySpec = null;

	private String description = "";

	private boolean bGenerateCertificate = false;

	// Before using this, make sure that an actual path is assigned.
	private File file = new File("");

	public QuickEnrollmentInfo()
	{
	}

	/**
	 * 
	 */
	public QuickEnrollmentInfo(WizardSettings settings)
	{
		setEndEntityProfile((NameAndId) settings.get("EndEntityProfile"));
		setCertificateProfile((NameAndId) settings.get("CertificateProfile"));
		setCertificateAuthority((NameAndId) settings
				.get("CertificateAuthority"));

		setOrganizationUnit((String) settings.get("OrganizationUnit"));

		setAutoGeneratePassword((Boolean) settings.get("AutoGeneratePassword"));
		setSendEmailNotification((Boolean) settings
				.get("SendEmailNotification"));

		setKeySpec((String) settings.get("KeySpec"));

		Boolean bGenerateCertificate = (Boolean) settings.get("GenerateCertificate");
		if (null == bGenerateCertificate)
		{
			bGenerateCertificate = false;
		}
		setGenerateCertificate(bGenerateCertificate);

		// Set the default name in this case to the name of the end entity
		// profile
		setDescription(getEndEntityProfile().getName());
	}

	private static String getNameAndIdString(Object obj)
	{
		if (obj instanceof NameAndId)
		{
			NameAndId name = (NameAndId) obj;

			return NameAndIdCellRenderer.getNameAndIdString(name);
		}

		return "(Null)";
	}

	public String toString()
	{
		return getDescription();
	}

	String toVerboseString()
	{
		// Build string containing information about QE settings.
		StringBuilder sb = new StringBuilder();
		sb.append("End Entity Profile: ");
		sb.append(getNameAndIdString(getEndEntityProfile()));
		sb.append("\n\n");
		sb.append("Certificate Profile: ");
		sb.append(getNameAndIdString(getCertificateProfile()));
		sb.append("\n\n");
		sb.append("Certificate Authority: ");
		sb.append(getNameAndIdString(getCertificateAuthority()));
		sb.append("\n\n");
		sb.append("Organization Unit: ");
		sb.append(getOrganizationUnit());
		sb.append("\n\n");
		sb.append("Auto-Generate Password: ");
		sb.append(isAutoGeneratePassword());
		sb.append("\n\n");
		sb.append("Send Email Notification: ");
		sb.append(isSendEmailNotification());
		sb.append("\n\n");
		sb.append("Key Specification (Bits): ");
		sb.append(getKeySpec());
		sb.append("\n\n");
		sb.append("Generate Certificate: ");
		sb.append(isGenerateCertificate() ? "Yes" : "No");
		sb.append("\n\n");

		return sb.toString();
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	public File getFile()
	{
		return file;
	}

	boolean createFile()
	{
		assert (file.getAbsolutePath().length() != 0);

		boolean rc = false;

		Properties propsQE = new Properties();
		propsQE.setProperty("Description", getDescription());
		propsQE.setProperty("EndEntityProfile",
				getNameAndIdString(getEndEntityProfile()));
		propsQE.setProperty("CertificateProfile",
				getNameAndIdString(getCertificateProfile()));
		propsQE.setProperty("CertificateAuthority",
				getNameAndIdString(getCertificateAuthority()));
		propsQE.setProperty("OrganizationUnit", getOrganizationUnit());
		propsQE.setProperty("AutoGeneratePassword", isAutoGeneratePassword()
				.toString());
		propsQE.setProperty("SendEmailNotification", isSendEmailNotification()
				.toString());
		propsQE.setProperty("KeySpec", getKeySpec());
		propsQE.setProperty("GenerateCertificate", isGenerateCertificate()
				.toString());

		try
		{
			propsQE.store(new FileOutputStream(file),
					"Quick Enrollment Properties");

			makeDefaultQE(true);

			rc = true;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		/*
		 * // TODO Save as XML instead? // Quick and dirty save of XML file.
		 * BufferedWriter writer; try { writer = new BufferedWriter(new
		 * FileWriter(file)); writer.write("<?xml version=\"1.0\"?>");
		 * 
		 * writer.write("\n<QuickEnrollment>"); writer.write("\n\t<Description>"
		 * + panel.getDescription() + "</Description>");
		 * writer.write("\n\t<EndEntityProfile>" +
		 * getNameAndIdString(settings.get("EndEntityProfile")) +
		 * "</EndEntityProfile>"); writer.write("\n\t<CertificateProfile>" +
		 * getNameAndIdString(settings.get("CertificateProfile")) +
		 * "</CertificateProfile>"); writer.write("\n\t<CertificateAuthority>" +
		 * getNameAndIdString(settings.get("CertificateAuthority")) +
		 * "</CertificateAuthority>"); writer.write("\n\t<OrganizationUnit>" +
		 * settings.get("OrganizationUnit") + "</OrganizationUnit>");
		 * 
		 * writer.write("\n</QuickEnrollment>");
		 * 
		 * writer.close(); } catch (IOException e) { e.printStackTrace(); }
		 */

		return rc;
	}

	void makeDefaultQE(boolean bChangeOnlyIfNoDefault)
	{
		// Check the property specifying the default QE.
		// If it isn't set, use the QE just created.

		ApplicationProperties props = MainFrame.getProperties();
		String strQE = props.getCurrentQEProfile();
		if ((!bChangeOnlyIfNoDefault) || (null == strQE)
				|| (0 == strQE.length()))
		{
			props.setCurrentQEProfile(file.getAbsolutePath());
		}
	}

	private static int parseId(String str)
	{
		final String strPrefix = "(Id: ";
		int index = str.lastIndexOf(strPrefix);
		if (-1 == index)
		{
			return -1;
		}

		String temp = str.substring(index + strPrefix.length());
		int indexEnd = temp.lastIndexOf(")", index);
		if (-1 == indexEnd)
		{
			return -1;
		}

		String temp2 = temp.substring(0, indexEnd);

		return Integer.parseInt(temp2);
	}

	private static String parseName(String str)
	{
		final String strPrefix = " (Id: ";
		int index = str.lastIndexOf(strPrefix);
		if (-1 == index)
		{
			return str;
		}

		String temp = str.substring(0, index);
		return temp;
	}

	static QuickEnrollmentInfo loadFromFile(File file)
	{
		QuickEnrollmentInfo info = null;

		if (!file.canRead())
		{
			return info;
		}

		Properties props = new Properties();
		try
		{
			props.load(new FileInputStream(file));

			info = new QuickEnrollmentInfo();

			String strEndEntityProfile = props.getProperty("EndEntityProfile",
					"");
			info.EndEntityProfile = new NameAndId(
					parseName(strEndEntityProfile),
					parseId(strEndEntityProfile));

			String strCertificateProfile = props.getProperty(
					"CertificateProfile", "");
			info.CertificateProfile = new NameAndId(
					parseName(strCertificateProfile),
					parseId(strCertificateProfile));

			String strCertificateAuthority = props.getProperty(
					"CertificateAuthority", "");
			info.CertificateAuthority = new NameAndId(
					parseName(strCertificateAuthority),
					parseId(strCertificateAuthority));

			info.description = props.getProperty("Description", "");
			info.OrganizationUnit = props.getProperty("OrganizationUnit", "");

			info.bAutoGeneratePassword = props.getProperty(
					"AutoGeneratePassword", "false").equals("true");
			info.bSendEmailNotification = props.getProperty(
					"SendEmailNotification", "false").equals("true");
			info.KeySpec = props.getProperty("KeySpec", "2048");

			info.bGenerateCertificate = props.getProperty(
					"GenerateCertificate", "false").equals("true");

			info.file = file;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return info;
	}

	public void putToWizardSettings(WizardSettings settings)
	{
		settings.put("QuickEnrollmentInfo", this);
		settings.put("EndEntityProfile", this.getEndEntityProfile());
		settings.put("CertificateProfile", this.getCertificateProfile());
		settings.put("CertificateAuthority", this.getCertificateAuthority());
		settings.put("OrganizationUnit", this.getOrganizationUnit());
		settings.put("AutoGeneratePassword", this.isAutoGeneratePassword());
		settings.put("SendEmailNotification", this.isSendEmailNotification());
		settings.put("KeySpec", this.getKeySpec());
		settings.put("GenerateCertificate", this.isGenerateCertificate());
	}

	public void setEndEntityProfile(NameAndId endEntityProfile)
	{
		EndEntityProfile = endEntityProfile;
	}

	public NameAndId getEndEntityProfile()
	{
		return EndEntityProfile;
	}

	public void setCertificateProfile(NameAndId certificateProfile)
	{
		CertificateProfile = certificateProfile;
	}

	public NameAndId getCertificateProfile()
	{
		return CertificateProfile;
	}

	public void setCertificateAuthority(NameAndId certificateAuthority)
	{
		CertificateAuthority = certificateAuthority;
	}

	public NameAndId getCertificateAuthority()
	{
		return CertificateAuthority;
	}

	public void setOrganizationUnit(String organizationUnit)
	{
		OrganizationUnit = organizationUnit;
	}

	public String getOrganizationUnit()
	{
		return OrganizationUnit;
	}

	public void setAutoGeneratePassword(boolean bAutoGeneratePassword)
	{
		this.bAutoGeneratePassword = bAutoGeneratePassword;
	}

	public Boolean isAutoGeneratePassword()
	{
		return bAutoGeneratePassword;
	}

	public void setSendEmailNotification(boolean bSendEmailNotification)
	{
		this.bSendEmailNotification = bSendEmailNotification;
	}

	public Boolean isSendEmailNotification()
	{
		return bSendEmailNotification;
	}

	public void setKeySpec(String keySpec)
	{
		KeySpec = keySpec;
	}

	public String getKeySpec()
	{
		return KeySpec;
	}

	public void setGenerateCertificate(boolean bGenerateCertificate)
	{
		this.bGenerateCertificate = bGenerateCertificate;
	}

	public Boolean isGenerateCertificate()
	{
		return bGenerateCertificate;
	}
}
