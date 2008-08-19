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

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

/** This is a singleton. Used to configure common-configuration with our sources.
 * Use like this:
 * String value = EjbcaConfiguration.instance().getString("my.conf.property.key");
 * See in-line comments below for the sources added to the configuration.
 * 
 * @author tomas
 * @version $Id: ExtraConfiguration.java,v 1.2 2008/02/12 14:45:55 anatom Exp $
 */
@Name("configuration")
public class NodecontrolConfiguration {
	//private static Logger log = Logger.getLogger(NodecontrolConfiguration.class);   
	@Logger 
	private static 	Log log;

	private static CompositeConfiguration config = null;
	
	/** This is a singleton so it's not allowed to create an instance explicitly */ 
	private NodecontrolConfiguration() {}

	public static final String CONFIGALLOWEXTERNAL = "allow.external-dynamic.configuration";

	public static final String JBOSSPANEL_OPEN = "nodecontrol.jbosspanel.open";
	public static final String ROTATIONPANEL_OPEN = "nodecontrol.rotationpanel.open";
	public static final String LOGPANEL_OPEN = "nodecontrol.logpanel.open";
	
	public static final String RUNNINGGREP = "nodecontrol.running.grepfor";
	public static final String HEALTHCHECKURL = "nodecontrol.healthcheckurl";
	public static final String START_CMD = "nodecontrol.startcmd";
	public static final String STOP_CMD = "nodecontrol.stopcmd";
	public static final String AUTHORIZED_ADMINCERTS_DIR = "nodecontrol.authorized.admin.cert.dir";
	
	public static final String ROTATION_FILE = "nodecontrol.rotation.control.file";
	public static final String ROTATION_KEY = "nodecontrol.rotation.control.key";

	public static final String LOG_DIR = "nodecontrol.log.directory";
	public static final String LOG_PIPE_CMD = "nodecontrol.log.pipecmd";
	public static final String LOG_PIPE_USERINFO = "nodecontrol.log.pipeuserinfo";
	public static final String LOG_PIPE_USERINFOFORMAT = "nodecontrol.log.pipeuserinfoformat";
	public static final String LOG_DAYS_TO_SHOW = "nodecontrol.log.daystolist";
	public static final String LOG_MAX_LINES = "nodecontrol.log.maxlines";	
	
	public static Configuration instance() {
		if (config == null) {
	        try {
	        	// Default values build into war file, this is last prio used if no of the other sources override this
	        	URL url = NodecontrolConfiguration.class.getResource("/nodecontrol.properties");
	        	PropertiesConfiguration pc = new PropertiesConfiguration(url);
	        	String allowexternal = pc.getString(CONFIGALLOWEXTERNAL, "false");

	        	config = new CompositeConfiguration();
	        	
	        	// Only add these config sources if we allow external configuration
	        	if (StringUtils.equals(allowexternal, "true")) {
		        	// Override with system properties, this is prio 1 if it exists (java -Dscep.test=foo)
		        	config.addConfiguration(new SystemConfiguration());
		        	log.info("Added system properties to configuration source (java -Dfoo.prop=bar).");
		        	
		        	// Override with file in "application server home directory"/conf, this is prio 2
		        	File f1 = new File("conf/nodecontrol.properties");
		        	pc = new PropertiesConfiguration(f1);
		        	pc.setReloadingStrategy(new FileChangedReloadingStrategy());
		        	config.addConfiguration(pc);
		        	log.info("Added file to configuration source: "+f1.getAbsolutePath());
		        	
		        	// Override with file in "/etc/ejbca/conf/extra, this is prio 3
		        	File f2 = new File("/etc/ejbca/conf/nodecontrol/nodecontrol.properties");
		        	pc = new PropertiesConfiguration(f2);
		        	pc.setReloadingStrategy(new FileChangedReloadingStrategy());
		        	config.addConfiguration(pc);
		        	log.info("Added file to configuration source: "+f2.getAbsolutePath());	        		
	        	}
	        	
	        	// Default values build into war file, this is last prio used if no of the other sources override this
	        	url = NodecontrolConfiguration.class.getResource("/nodecontrol.properties");
	        	pc = new PropertiesConfiguration(url);
	        	config.addConfiguration(pc);
	        	log.info("Added url to configuration source: "+url);
	        	
	            log.info("Allow external re-configuration: "+allowexternal);
	            log.info("Using health check URL: "+config.getString(HEALTHCHECKURL));
	            log.info("Using start cmd: "+config.getString(START_CMD));
	            log.info("Using stop cmd: "+config.getString(STOP_CMD));
	            log.info("Directory with authorized certificates: "+config.getString(AUTHORIZED_ADMINCERTS_DIR));
	            log.info("Rotation control file: "+config.getString(ROTATION_FILE));
	            log.info("Rotation control key: "+config.getString(ROTATION_KEY));
	        } catch (ConfigurationException e) {
	        	log.error("Error intializing Nodecontrol Configuration: ", e);
	        }
		} 
		return config;
	}
}
