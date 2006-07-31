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

import org.bouncycastle.util.encoders.Base64;
import org.ejbca.core.model.UpgradeableDataHashMap;



/**
 * Class used to send a SCEP submessage between RA server and CA.
 * @author philip
 *
 */

public class SCEPSubMessage extends UpgradeableDataHashMap implements ISubMessage {
	
	static final int CLASS_TYPE = 1;
	
	public static final float LATEST_VERSION = (float) 1.0;
		
	private static final String SCEPDATA = "SCEPDATA";
	
	//private static final Log log = LogFactory.getLog(SCEPSubMessage.class);
	
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Default constructor that should be used.
	 * @param sceprequest
	 */
	public SCEPSubMessage(byte[] sceprequest){
		data.put(SCEPDATA, new String(Base64.encode(sceprequest)));
		data.put(CLASSTYPE, new Integer(CLASS_TYPE));
		data.put(VERSION, new Float(LATEST_VERSION));
	}

	/**
	 * Constructor used when laoded from a persisted state
	 * @param sceprequest
	 */	
	public SCEPSubMessage(){}
	
	
	public byte[] getScepData(){
		return Base64.decode(((String) data.get(SCEPDATA)).getBytes());
	}

	public float getLatestVersion() {
		return LATEST_VERSION;
	}

	public void upgrade() {
		if(LATEST_VERSION != getVersion()){
			
			
			data.put(VERSION, new Float(LATEST_VERSION));
		}
		
	}
	
    



}
