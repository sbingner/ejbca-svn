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
 
package org.ejbca.core.model.ra;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.ejbca.core.model.InternalResources;
import org.ejbca.core.model.UpgradeableDataHashMap;
import org.ejbca.util.Base64;


/**
 * The model representation of Exended Information about a user. It's used for non-searchable data about a user, 
 * like a image, in an effort to minimize the need for database alterations
 *
 * @author  Philip Vendil
 * @version $Id: ExtendedInformation.java,v 1.8 2007-01-05 05:32:53 herrvendil Exp $
 */
public class ExtendedInformation extends UpgradeableDataHashMap implements java.io.Serializable, Cloneable {
    private static final Logger log = Logger.getLogger(ExtendedInformation.class);
    /** Internal localization of logs and errors */
    private static final InternalResources intres = InternalResources.getInstance();

    /**
     * Determines if a de-serialized file is compatible with this class.
     *
     * Maintainers must change this value if and only if the new version
     * of this class is not compatible with old versions. See Sun docs
     * for <a href=http://java.sun.com/products/jdk/1.1/docs/guide
     * /serialization/spec/version.doc.html> details. </a>
     *
     */
    private static final long serialVersionUID = 3981761824188420319L;
    
    public static final float LATEST_VERSION = 1;    

    public static final int TYPE_BASIC = 0;
    public static final int TYPE_SCEPRA = 1;
    
    public static final String TYPE = "type";

    // protected fields.
    protected static final String SUBJECTDIRATTRIBUTES = "subjectdirattributes";

    protected static final String XKMSREVOCATIONCODEIDENTIFIER = "revocationcodeidentifier";
    
    
    // Public constants

    // Wait for fields to use with this class. 
    
    // Public methods.
    /** Creates a new instance of EndEntity Profile */
    public ExtendedInformation() {
    	setType(TYPE_BASIC);
    	data.put(SUBJECTDIRATTRIBUTES, "");
    }

    public String getSubjectDirectoryAttributes(){ 
    	String ret = (String) data.get(SUBJECTDIRATTRIBUTES);
    	if (ret == null) {
    		ret = "";
    	}
    	return ret;
    }
    public void setSubjectDirectoryAttributes(String subjdirattr) {
      if(subjdirattr==null)
        data.put(SUBJECTDIRATTRIBUTES,"");
      else
        data.put(SUBJECTDIRATTRIBUTES,subjdirattr);
    }
    
    /**
     * Returns the revocation code identifier primarily used
     * in the XKMS protocol to let the end user revoke his certificate.
     * 
     * The method is autoupgradable
     * 
     * @returns The code or null if no revocationcode have been set.
     */
    public String getRevocationCodeIdentifier(){ 
    	String retval = (String) data.get(XKMSREVOCATIONCODEIDENTIFIER);
    	
    	if(retval != null){
    		retval = new String(Base64.decode(retval.getBytes()));
    	}
    	
    	return retval;     	
    }
    public void setRevocationCodeIdentifier(String revocationCodeIdentifier) {
    	String value = revocationCodeIdentifier;
    	
    	if(value != null){
    		value = new String(Base64.encode(value.getBytes()));
    	}
    	    	
    	data.put(XKMSREVOCATIONCODEIDENTIFIER,value);

    }
    
    public Object clone() throws CloneNotSupportedException {
      ExtendedInformation clone = new ExtendedInformation();
      HashMap clonedata = (HashMap) clone.saveData();

      Iterator i = (data.keySet()).iterator();
      while(i.hasNext()){
        Object key = i.next();
        clonedata.put(key, data.get(key));
      }

      clone.loadData(clonedata);
      return clone;
    }

    /** Implemtation of UpgradableDataHashMap function getLatestVersion */
    public float getLatestVersion(){
       return LATEST_VERSION;
    }

    /** Implemtation of UpgradableDataHashMap function upgrade. */

    public void upgrade(){
    	if(Float.compare(LATEST_VERSION, getVersion()) != 0) {
    		// New version of the class, upgrade
			String msg = intres.getLocalizedMessage("ra.extendedinfoupgrade", Float.valueOf(getVersion()));
            log.info(msg);
    		
            if(data.get(SUBJECTDIRATTRIBUTES) == null){
                data.put(SUBJECTDIRATTRIBUTES, "");   
            }            

    		data.put(VERSION, new Float(LATEST_VERSION));
    	}
    }
    
    /**
     * Method that returns the classpath to the this or inheriting classes.
     * @return String containing the classpath.
     */
    public int getType(){
    	return ((Integer) data.get(TYPE)).intValue();
    }
    
    /**
     * Method used to specify which kind of object that should be created during
     * deserialization process.
     * 
     * Inheriting class should call 'setClassPath(this) in it's constructor.
     * 
     * @param object
     */
    protected void setType(int type){
       data.put(TYPE,new Integer(type));	
    }

}
