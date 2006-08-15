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

import java.security.cert.X509Certificate;

/**
 * Ext RA card renewal sub message used when a users certificates on a PrimeCard smart card should be renewed.
 * 
 * Parameters inherited from the base class ExtRARequset is ignored.
 * 
 * @author tomas
 * @version $Id: ExtRACardRenewalRequest.java,v 1.1 2006-08-15 10:44:19 anatom Exp $
 */
public class ExtRACardRenewalRequest extends ExtRARequest {

	public static final float LATEST_VERSION = (float) 1.0;
	
	static final int CLASS_TYPE = 11;
	
	// Public Constants.
	
	// Field constants	
	private static final String AUTHCERT           = "AUTHCERT";
	private static final String SIGNCERT           = "SIGNCERT";
	private static final String AUTHPKCS10         = "AUTHPKCS10";
    private static final String SIGNPKCS10         = "SIGNPKCS10";
    

	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor revoking a specific certificate.
	 */
	public ExtRACardRenewalRequest(long requestId, X509Certificate authcert, X509Certificate signcert, String authreq, String signreq){    
		data.put(REQUESTID, new Long(requestId));
		data.put(CLASSTYPE, new Integer(CLASS_TYPE));
		data.put(VERSION, new Float(LATEST_VERSION));
        
		data.put(AUTHCERT, authcert);
		data.put(SIGNCERT, signcert);
        data.put(AUTHPKCS10, authreq);
		data.put(SIGNPKCS10, signreq);
	}

	/**
	 * Constructor used when laoded from a persisted state
	 */	
	public ExtRACardRenewalRequest(){}
	

	public float getLatestVersion() {
		return LATEST_VERSION;
	}
	
	
	/**
	 * Returns the auth certificate
	 */
	public String getAuthCert(){
		return (String) data.get(AUTHCERT);
	}

	/**
	 * Returns the sign certificate
	 */
	public String getSignCert(){
		return (String) data.get(SIGNCERT);
	}
	
    /**
     * Returns the auth pkcs10 request
     */
    public String getAuthPkcs10(){
        return (String) data.get(AUTHPKCS10);
    }
    /**
     * Returns the sign pkcs10 request
     */
    public String getSignPkcs10(){
        return (String) data.get(SIGNPKCS10);
    }
	
	
	public void upgrade() {
        if(Float.compare(LATEST_VERSION, getVersion()) != 0) {            
			data.put(VERSION, new Float(LATEST_VERSION));
		}		
	}



}
