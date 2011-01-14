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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.util.encoders.Hex;
import org.ejbca.core.protocol.ws.client.gen.RevokeStatus;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.util.CertTools;
import org.ejbca.util.cert.SubjectDirAttrExtension;
import org.ejbca.util.keystore.KeyTools;

/**
 * Helper functions for displaying certificate information
 * 
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */
public class CertHelper
{
	static String toString(X509Certificate x509Cert, UserDataVOWS user)
	{
		String strResult = "";

//		strResult += x509Cert.toString();
		strResult += "\nUser Name:" + user.getUsername();
		strResult += "\nCertificate Version:" + x509Cert.getType() + " v."
				+ x509Cert.getVersion();
		strResult += "\nCertificate Serial Number:"
				+ CertTools.getSerialNumberAsString(x509Cert); // x509Cert.getSerialNumber().toString(16).toUpperCase());
		strResult += "\nIssuer DN:" + x509Cert.getIssuerDN();
		strResult += "\nValid From:" + x509Cert.getNotBefore();
		strResult += "\nValid To:" + x509Cert.getNotAfter();
		strResult += "\nSubject DN:" + x509Cert.getSubjectDN();

		strResult += "\nSubject Alternative Name: ";
		try
		{
			Collection<List<?>> listSubjectAlternativeNames = x509Cert
					.getSubjectAlternativeNames();
			strResult += ((null == listSubjectAlternativeNames) ? " None"
					: listSubjectAlternativeNames.toString());
		}
		catch (Exception e)
		{
		}

		strResult += "\nSubject Directory Attributes: ";
		String strSubjectDirAttr = "";
		try
		{
			strSubjectDirAttr = SubjectDirAttrExtension
					.getSubjectDirectoryAttributes(x509Cert);
		}
		catch (Exception e)
		{
			strSubjectDirAttr = e.getMessage();
		}
		strResult += ((null == strSubjectDirAttr) ? " None" : strSubjectDirAttr);

		PublicKey publicKey = x509Cert.getPublicKey();
		strResult += "\nPublic Key:" + publicKey.getAlgorithm() + " ("
				+ KeyTools.getKeyLength(x509Cert.getPublicKey())
				+ " bits)\n\t" + CertHelper.getPublicKeyModulus(x509Cert);
		strResult += "\nBasic Constraints:"
				+ CertHelper.getBasicConstraints(x509Cert);
		strResult += "\nKey Usage:" + CertHelper.getKeyUsage(x509Cert);

		strResult += "\nExtended Key Usage: ";
		List<String> listExtKeyUsage = null;
		try
		{
			listExtKeyUsage = x509Cert.getExtendedKeyUsage();
		}
		catch (CertificateParsingException e)
		{
			e.printStackTrace();
		}
		if (null == listExtKeyUsage)
		{
			strResult += "\tNone";
		}
		else
		{
			// Based on code in CertificateView.java
			String[] strExtKeyUsage = new String[listExtKeyUsage.size()];
			Map<String, String> map = CertHelper.getExtendedKeyUsageMap();
			for (int i = 0; i < listExtKeyUsage.size(); i++)
			{
				String item = listExtKeyUsage.get(i);
				strExtKeyUsage[i] = (String) map.get(item);
				strResult += "\n\t" + i + ": " + strExtKeyUsage[i] + " ("
						+ item + ")";
			}
		}

		strResult += "\nQualified Certificate Statement: ";
		try
		{
			boolean bQCS = CertHelper.hasQcStatement(x509Cert);
			strResult += "\t" + bQCS;
		}
		catch (IOException e)
		{
			strResult += "\tUnknown";
		}

		strResult += "\nSignature Algorithm: " + x509Cert.getSigAlgName();

		strResult += "\nFingerprint SHA-1:";
		try
		{
			byte[] res = CertTools.generateSHA1Fingerprint(x509Cert
					.getEncoded());
			String ret = new String(Hex.encode(res));
			strResult += "\n\t" + ret.toUpperCase();
		}
		catch (CertificateEncodingException cee)
		{
		}

		strResult += "\nFingerprint M5:";
		try
		{
			byte[] res = CertTools
					.generateMD5Fingerprint(x509Cert.getEncoded());
			String ret = new String(Hex.encode(res));
			strResult += "\n\t" + ret.toUpperCase();
		}
		catch (CertificateEncodingException cee)
		{
		}

		return strResult;
	}

	static String getPublicKeyModulus(X509Certificate certificate)
	{
		String mod = null;
		if (certificate.getPublicKey() instanceof RSAPublicKey)
		{
			mod = ""
					+ ((RSAPublicKey) certificate.getPublicKey()).getModulus()
							.toString(16);
			mod = mod.toUpperCase();
			mod = StringUtils.abbreviate(mod, 50);
		}
		else if (certificate.getPublicKey() instanceof DSAPublicKey)
		{
			mod = ""
					+ ((DSAPublicKey) certificate.getPublicKey()).getY()
							.toString(16);
			mod = mod.toUpperCase();
			mod = StringUtils.abbreviate(mod, 50);
		}
		else if (certificate.getPublicKey() instanceof ECPublicKey)
		{
			mod = ""
					+ ((ECPublicKey) certificate.getPublicKey()).getW()
							.getAffineX().toString(16);
			mod = mod
					+ ((ECPublicKey) certificate.getPublicKey()).getW()
							.getAffineY().toString(16);
			mod = mod.toUpperCase();
			mod = StringUtils.abbreviate(mod, 50);
		}
		return mod;
	}

	static String getBasicConstraints(X509Certificate x509cert)
	{
		String retval = "None";
		int bc = x509cert.getBasicConstraints();
		if (bc == Integer.MAX_VALUE)
		{
			retval = "No limit to the allowed length of the certification path.";
		}
		else if (bc == -1)
		{
			retval = "End Entity";
		}
		else
		{
			retval = "Maximum number of CA certificates that may follow this certificate in a certification path: "
					+ x509cert.getBasicConstraints();
		}
		return retval;
	}

	// See documentation for X509Certificate.getKeyUsage for more information.
	static String getKeyUsage(X509Certificate cert)
	{
		String retval = "";

		String[] strKeyUsage =
		{
				"Digital Signature",
				"Non-repudiation",
				"Key Encipherment",
				"Data Encipherment",
				"Key Agreement",
				"Key Certificate Sign",
				"CRL Sign",
				"Encipher Only",
				"Decipher Only"
		};

		boolean[] usages = cert.getKeyUsage();
		for (int i = 0; i < strKeyUsage.length; i++)
		{
			if (usages[i])
			{
				if (0 != retval.length())
				{
					retval += ", ";
				}
				retval += strKeyUsage[i];
			}
		}

		if (0 == retval.length())
		{
			retval = "None";
		}
		return retval;
	}

	// There doesn't seem to be any way to get these strings from the server,
	// so the current workaround is to just hard wire this information from
	// "conf/extendedkeyusage.properties".
	// This code may need to be updated if/when that file is modified.
	// Note that items indicated as null or deprecated in the properties file
	// are not included here.
	// TODO Change this so that the information is read in from a config file.
	// This will require an EJBCA properties configuration file to be copied and
	// distributed with this application.
	private static Map<String, String> mapExtendedKeyUsage = null;

	static Map<String, String> getExtendedKeyUsageMap()
	{
		if (null == mapExtendedKeyUsage)
		{
			mapExtendedKeyUsage = new HashMap<String, String>();

			String[] oid =
			{
					"2.5.29.37.0",
					"1.3.6.1.5.5.7.3.1",
					"1.3.6.1.5.5.7.3.2",
					"1.3.6.1.5.5.7.3.3",
					"1.3.6.1.5.5.7.3.4",
					// # IPSECENDSYSTEM is deprecated
					// extendedkeyusage.oid.5 = 1.3.6.1.5.5.7.3.5
					// # IPSECTUNNEL is deprecated
					// extendedkeyusage.oid.6 = 1.3.6.1.5.5.7.3.6
					// # IPSECUSER is deprecated
					// extendedkeyusage.oid.7 = 1.3.6.1.5.5.7.3.7
					"1.3.6.1.5.5.7.3.8",
					// # MS smart card logon
					"1.3.6.1.4.1.311.20.2.2",
					"1.3.6.1.5.5.7.3.9",
					// # Microsoft Encrypted File System Certificates
					"1.3.6.1.4.1.311.10.3.4",
					// # Microsoft Encrypted File System Recovery Certificates
					"1.3.6.1.4.1.311.10.3.4.1",
					"1.3.6.1.5.5.7.3.17",
					"1.3.6.1.5.5.7.3.15",
					"1.3.6.1.5.5.7.3.16",
					// # Microsoft Signer of documents
					"1.3.6.1.4.1.311.10.3.12",
					// # Intel AMT (out of band) network management
					"2.16.840.1.113741.1.2.3",
					// # ETSI TS 102 231 TSL Signer (id-tsl-kp-tslSigning)
					"0.4.0.2231.3.0"
			};

			String[] oidDescription =
			{
					"Any Extended Key Usage",
					"Server Authentication",
					"Client Authentication",
					"Code Signing",
					"Email Protection",
					// # IPSECENDSYSTEM is deprecated
					// extendedkeyusage.name.5 = null
					// # IPSECTUNNEL is deprecated
					// extendedkeyusage.name.6 = null
					// # IPSECUSER is deprecated
					// extendedkeyusage.name.7 = null
					"Time Stamping",
					"MS Smart Card Logon",
					"OCSP Signer",
					"MS Encrypted File System (EFS)",
					"MS EFS Recovery",
					"Internet Key Exchange for IPsec",
					"SCVP Server",
					"SCVP Client",
					"MS Document Signing",
					"Intel AMT Management",
					"TSL Signing"
			};

			assert (oid.length == oidDescription.length);

			for (int i = 0; i < oid.length; i++)
			{
				mapExtendedKeyUsage.put(oid[i], oidDescription[i]);
			}
		}

		return mapExtendedKeyUsage;
	}

	// Adapted from QCStatementExtension
	static boolean hasQcStatement(X509Certificate cert) throws IOException
	{
		boolean ret = false;
		String oid = X509Extensions.QCStatements.getId();
		byte[] bytes = cert.getExtensionValue(oid);
		if (bytes == null)
		{
			return false;
		}
		ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(
				bytes));
		ASN1OctetString octs = (ASN1OctetString) aIn.readObject();
		aIn = new ASN1InputStream(new ByteArrayInputStream(octs.getOctets()));
		DERObject obj = aIn.readObject();

		if (obj != null)
		{
			ret = true;
		}

		return ret;
	}

	static String getHumanReadableRevocationReason(int reason)
	{
		switch (reason)
		{
			case RevokeStatus.NOT_REVOKED:
				return "The certificate is not revoked";
			case RevokeStatus.REVOKATION_REASON_UNSPECIFIED:
				return "Unspecified";
			case RevokeStatus.REVOKATION_REASON_KEYCOMPROMISE:
				return "Key compromise";
			case RevokeStatus.REVOKATION_REASON_CACOMPROMISE:
				return "CA compromise";
			case RevokeStatus.REVOKATION_REASON_AFFILIATIONCHANGED:
				return "Affiliation changed";
			case RevokeStatus.REVOKATION_REASON_SUPERSEDED:
				return "Superseded";
			case RevokeStatus.REVOKATION_REASON_CESSATIONOFOPERATION:
				return "Cessation of operation";
			case RevokeStatus.REVOKATION_REASON_CERTIFICATEHOLD:
				return "Certificate hold";
			case RevokeStatus.REVOKATION_REASON_REMOVEFROMCRL:
				return "Remove from CRL";
			case RevokeStatus.REVOKATION_REASON_PRIVILEGESWITHDRAWN:
				return "Privileges withdrawn";
			case RevokeStatus.REVOKATION_REASON_AACOMPROMISE:
				return "AA compromise";
			default:
				return "Unknown";
		}
	}

}
