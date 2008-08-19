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
 
package org.ejbca.nodecontrol.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.jboss.seam.util.Hex;


/**
 * Tools to handle common certificate operations.
 *
 * @version $Id: CertTools.java,v 1.54 2008/03/14 16:55:36 anatom Exp $
 */
public class CertTools {

    /**
     * Gets a specified part of a DN. Specifically the first occurrence it the DN contains several
     * instances of a part (i.e. cn=x, cn=y returns x).
     *
     * @param dn String containing DN, The DN string has the format "C=SE, O=xx, OU=yy, CN=zz".
     * @param dnpart String specifying which part of the DN to get, should be "CN" or "OU" etc.
     *
     * @return String containing dnpart or null if dnpart is not present
     */
    public static String getPartFromDN(String dn, String dnpart) {
        String part = null;
        if ((dn != null) && (dnpart != null)) {
            String o;
            dnpart += "="; // we search for 'CN=' etc.
            X509NameTokenizer xt = new X509NameTokenizer(dn);
            while (xt.hasMoreTokens()) {
                o = xt.nextToken();
                //log.debug("checking: "+o.substring(0,dnpart.length()));
                if ((o.length() > dnpart.length()) &&
                        o.substring(0, dnpart.length()).equalsIgnoreCase(dnpart)) {
                    part = o.substring(dnpart.length());

                    break;
                }
            }
        }
        return part;
    } //getPartFromDN

    /**
     * Generate SHA1 fingerprint of certificate in string representation.
     *
     * @param cert X509Certificate.
     *
     * @return String containing hex format of SHA1 fingerprint, or null if input is null.
     * @throws NoSuchAlgorithmException 
     * @throws CertificateEncodingException 
     */
    public static String getFingerprintAsString(X509Certificate cert) throws CertificateEncodingException, NoSuchAlgorithmException {
    	if (cert == null) return null;
    	byte[] res = generateSHA1Fingerprint(cert.getEncoded());
    	return new String(Hex.encodeHex(res));
    }

    /**
     * Generate a SHA1 fingerprint from a byte array containing a X.509 certificate
     *
     * @param ba Byte array containing DER encoded X509Certificate.
     *
     * @return Byte array containing SHA1 hash of DER encoded certificate.
     * @throws NoSuchAlgorithmException 
     */
    public static byte[] generateSHA1Fingerprint(byte[] ba) throws NoSuchAlgorithmException {
    	MessageDigest md = MessageDigest.getInstance("SHA1");
    	return md.digest(ba);
    } // generateSHA1Fingerprint

    /**
     * Creates X509Certificate from byte[].
     *
     * @param cert byte array containing certificate in DER-format
     *
     * @return X509Certificate
     *
     * @throws CertificateException if the byte array does not contain a proper certificate.
     * @throws IOException if the byte array cannot be read.
     */
    public static X509Certificate getCertfromByteArray(byte[] cert) throws CertificateException {
    	CertificateFactory cf = CertificateFactory.getInstance("X.509");
    	X509Certificate x509cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert));
    	return x509cert;
    } // getCertfromByteArray

} // CertTools
