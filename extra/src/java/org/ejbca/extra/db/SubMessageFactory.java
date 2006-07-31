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

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class used to create a ISubMessage depending of it's classtype, 
 * All valid submessages should be registred in the creatInstance method.
 * 
 * 
 * @author philip
 * $Id: SubMessageFactory.java,v 1.1 2006-07-31 13:13:07 herrvendil Exp $
 */

class SubMessageFactory {
	
	private static final Log log = LogFactory.getLog(SubMessageFactory.class);
	
	static ISubMessage createInstance(HashMap data){
		ISubMessage retval = null;
		int classType = ((Integer) data.get(ISubMessage.CLASSTYPE)).intValue();
		switch(classType){
		case ExtRAPKCS10Request.CLASS_TYPE:
			retval =  new ExtRAPKCS10Request();
			log.debug("Class of type ExtRAPKCS10Request created");
			break;
		case ExtRAPKCS12Request.CLASS_TYPE:
			retval =  new ExtRAPKCS12Request();
			log.debug("Class of type ExtRAPKCS12Request created");
			break;
		case ExtRAKeyRecoveryRequest.CLASS_TYPE:
			retval =  new ExtRAKeyRecoveryRequest();
			log.debug("Class of type ExtRAKeyRecoveryRequest created");
			break;
		case ExtRARevocationRequest.CLASS_TYPE:
			retval =  new ExtRARevocationRequest();
			log.debug("Class of type ExtRARevocationRequest created");
			break;			
		case ExtRAPKCS10Response.CLASS_TYPE:
			retval =  new ExtRAPKCS10Response();
			log.debug("Class of type ExtRAPKCS10Response created");
			break;
		case ExtRAPKCS12Response.CLASS_TYPE:
			retval =  new ExtRAPKCS12Response();
			log.debug("Class of type ExtRAPKCS12Response created");
			break;
		case ExtRAEditUserRequest.CLASS_TYPE:
			retval =  new ExtRAEditUserRequest();
			log.debug("Class of type ExtRAEditUserRequest created");
			break;				
		case ExtRAResponse.CLASS_TYPE:
			retval =  new ExtRAResponse();
			log.debug("Class of type ExtRAResponse created");
			break;			
		default:
			log.error("Error Class of type : " + classType + " not registred");
		}
		
		
		return retval;
	}
	
}
