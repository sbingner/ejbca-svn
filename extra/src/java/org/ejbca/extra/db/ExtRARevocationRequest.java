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

import java.math.BigInteger;

/**
 * Ext RA Revocation Reguest sub message used when the CA should recover a generated certificate.
 * 
 * Parameters inhereited from the base class ExtRARequset is ignored.
 * 
 * @author philip
 * $Id: ExtRARevocationRequest.java,v 1.1 2006-07-31 13:13:06 herrvendil Exp $
 */
public class ExtRARevocationRequest extends ExtRARequest {

	public static final float LATEST_VERSION = (float) 2.0;
	
	static final int CLASS_TYPE = 8;
	
	// Public Constants.
	/**
	 * Constant specifying type of revocation
	 */
    public static final int REVOKATION_REASON_UNSPECIFIED          = 0;
    public static final int REVOKATION_REASON_KEYCOMPROMISE        = 1;
    public static final int REVOKATION_REASON_CACOMPROMISE         = 2;
    public static final int REVOKATION_REASON_AFFILIATIONCHANGED   = 3;
    public static final int REVOKATION_REASON_SUPERSEDED           = 4;
    public static final int REVOKATION_REASON_CESSATIONOFOPERATION = 5;
    public static final int REVOKATION_REASON_CERTIFICATEHOLD      = 6;
    public static final int REVOKATION_REASON_REMOVEFROMCRL        = 8;
    public static final int REVOKATION_REASON_PRIVILEGESWITHDRAWN  = 9;
    public static final int REVOKATION_REASON_AACOMPROMISE         = 10;
	
	// Field constants	
	private static final String REVOKATIONREASON      = "REVOCATIONREASON";
	private static final String ISSUERDN              = "ISSUERDN";
	private static final String CERTIFICATESN         = "CERTIFICATESN";
	private static final String REVOKEALL             = "REVOKEALL";
	private static final String REVOKEUSER            = "REVOKEUSER";

	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor that should be used.
	 */
	public ExtRARevocationRequest(long requestId, String issuerdn, BigInteger certificatesn, int revocationReason){    
		data.put(REQUESTID, new Long(requestId));
		data.put(CLASSTYPE, new Integer(CLASS_TYPE));
		data.put(VERSION, new Float(LATEST_VERSION));
		data.put(ISSUERDN, issuerdn);
		data.put(CERTIFICATESN, certificatesn);
		data.put(REVOKATIONREASON, new Integer(revocationReason));
		data.put(REVOKEALL, new Boolean(false));
		data.put(REVOKEUSER, new Boolean(false));
	}
	/**
	 * Constructor that should be used.
	 */
	public ExtRARevocationRequest(long requestId, String issuerdn, BigInteger certificatesn, int revocationReason, boolean revokeuser, boolean revokeall){    
		data.put(REQUESTID, new Long(requestId));
		data.put(CLASSTYPE, new Integer(CLASS_TYPE));
		data.put(VERSION, new Float(LATEST_VERSION));
		data.put(ISSUERDN, issuerdn);
		data.put(CERTIFICATESN, certificatesn);
		data.put(REVOKATIONREASON, new Integer(revocationReason));
		data.put(REVOKEALL, new Boolean(revokeall));
		data.put(REVOKEUSER, new Boolean(revokeuser));
	}
	

	/**
	 * Constructor used when laoded from a persisted state
	 */	
	public ExtRARevocationRequest(){}
	

	public float getLatestVersion() {
		return LATEST_VERSION;
	}
	
	
	/**
	 * Returns the issuer DN of the certificate which keystore should be recreated
	 */
	public String getIssuerDN(){
		return (String) data.get(ISSUERDN);
	}

	/**
	 * Returns the Certificate Serialnumber of the certificate which keystore should be recreated
	 */
	public BigInteger getCertificateSN(){
		return (BigInteger) data.get(CERTIFICATESN);
	}
	
	/**
	 * Returns the revokation reason. One of the REVOKATION_REASON_ constants.
	 */
	public int getRevocationReason(){
		return ((Integer) data.get(REVOKATIONREASON)).intValue();
	}
	
	/**
	 * Returns the true is the all the users certificate should be revoked, false if only the given one.
	 */
	public boolean getRevokeAll(){
		return ((Boolean) data.get(REVOKEALL)).booleanValue();
	}

	/**
	 * Returns the true is the user should be revoked, false if only the certificates.
	 */
	public boolean getRevokeUser(){
		return ((Boolean) data.get(REVOKEUSER)).booleanValue();
	}
	
	
	public void upgrade() {
		if(LATEST_VERSION != getVersion()){	
			data.put(REVOKEALL, new Boolean(false));
			data.put(REVOKEUSER, new Boolean(false));
			data.put(VERSION, new Float(LATEST_VERSION));
		}
		
	}



}
