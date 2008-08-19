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
 
package org.ejbca.nodecontrol;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet used to start services by calling the ServiceSession.load() at startup<br>
 * This is initialized on strtup as configured in web.xml
 *
 * @version $Id$
 */
public class StartServicesServlet extends HttpServlet {

    
	private static final long serialVersionUID = 1L;

	/**
     * Method used to remove all active timers and stop system services.
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	public void destroy() {
		super.destroy();
	}


    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // To work around a bug in MacOSX
        
        /*
         From: Scott Kovatch [mailto:email@hidden]
		Sent: 08 November 2005 17:39
		To: azack
		Cc: email@hidden; email@hidden
		Subject: Re: Java thread termination - with bootstrap_register() error
		
		Looking at the errors, I think this is a known OS problem. If you create a ServerSocket before loading the AWT in headless mode, the application will die.
		
		The workaround is to start up the AWT first by creating a java.awt.Dimension(), or Toolkit.getDefaultToolkit(), then start listening for connections. I don't know enough about Tomcat to know if that's even feasible, but that's the best we have right now.
		
		-- Scott K.
         */
        java.awt.Toolkit.getDefaultToolkit();
    } // init

    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException {
    } //doPost

    public void doGet(HttpServletRequest req,  HttpServletResponse res) throws java.io.IOException, ServletException {
    } // doGet

    
}
