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
package org.ejbca.extra.db;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.util.encoders.Base64;
import org.ejbca.util.CertTools;

/**
 * Class used as response to a ExtRA PKCS 10Sub Message response. If request was succesful then will the resposne
 * contain the generated certificate.
 * @author philip
 * $Id: ExtRAPKCS10Response.java,v 1.1 2006-07-31 13:13:07 herrvendil Exp $
 */

public class ExtRAPKCS10Response extends ExtRAResponse {

	private static final Log log = LogFactory.getLog(ExtRAPKCS10Response.class);
	
	public static final float LATEST_VERSION = (float) 1.0;
	
	static final int CLASS_TYPE = 5; // Must be uniqu to all submessage classes
		// Field constants
	private static final String CERTIFICATE           = "CERTIFICATE";		
	
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Default constructor that should be used.
	 *  
	 */
	public ExtRAPKCS10Response(long requestId, boolean success, String failinfo, X509Certificate certificate){
        super(requestId, success, failinfo);
        try {
    		data.put(CLASSTYPE, new Integer(CLASS_TYPE));
    		data.put(VERSION, new Float(LATEST_VERSION));
    		if(certificate != null){
			  String certstring = new String(Base64.encode(certificate.getEncoded()));
			  data.put(CERTIFICATE, certstring);
    		}  
		} catch (CertificateEncodingException e) {
			log.error("Certificate encoding failed" , e);
		}
	}

	/**
	 * Constructor used when laoded from a persisted state
	 * 
	 */	
	public ExtRAPKCS10Response(){}
	
	/**
	 * Returns the generated certifcate.
	 */
	public X509Certificate getCertificate(){
		CertificateFactory cf = CertTools.getCertificateFactory();
	    X509Certificate cert = null;
		try {
			cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(Base64.decode(((String) data.get(CERTIFICATE)).getBytes())));
		} catch (CertificateException e) {
			log.error("Error decoding certificate ", e);
		}
	    return cert;
	}
	
	public void upgrade() {
		
		
	}

	public float getLatestVersion() {
		return LATEST_VERSION;
	}

}
