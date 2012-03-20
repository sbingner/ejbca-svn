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

package org.ejbca.ui.web.protocol;

import javax.ejb.EJB;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.cesecore.certificates.ocsp.OcspResponseGeneratorSessionLocal;
import org.cesecore.certificates.ocsp.standalone.StandaloneOcspResponseGeneratorSessionLocal;
import org.ejbca.config.GlobalConfiguration;
import org.ejbca.ui.web.pub.cluster.ValidationAuthorityHealthCheck;

/** 
 * Servlet implementing server side of the Online Certificate Status Protocol (OCSP)
 * For a detailed description of OCSP refer to RFC2560.
 *
 * @author Lars Silven PrimeKey
 * @version  $Id$
 */
public class OCSPServletStandAlone extends BaseOcspServlet {

    private static final long serialVersionUID = -7093480682721604160L;

    /** Special logger only used to log version number. ejbca.version.log can be directed to a special logger, or have a special log level 
     * in the log4j configuration. 
     */
	private static final Logger m_versionLog = Logger.getLogger("org.ejbca.version.log");

	 private static final Logger log = Logger.getLogger(OCSPServletStandAlone.class);
	    
	    @EJB
	    private StandaloneOcspResponseGeneratorSessionLocal standaloneOcspResponseGeneratorSession;
	

    /* (non-Javadoc)
     * @see org.ejbca.ui.web.protocol.OCSPServletBase#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig config) throws ServletException {

        // Log with warn priority so it will be visible in strict production configurations  
	    m_versionLog.warn("Init, "+GlobalConfiguration.EJBCA_VERSION+" OCSP startup");
    }
    
    /**
     * Method used to log OCSP service shutdown.
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	public void destroy() {
		super.destroy();
        // Log with warn priority so it will be visible in strict production configurations  
	    m_versionLog.warn("Destroy, "+GlobalConfiguration.EJBCA_VERSION+" OCSP shutdown");
	}


    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected OcspResponseGeneratorSessionLocal getOcspResponseGenerator() {

        return standaloneOcspResponseGeneratorSession;
    }
}
