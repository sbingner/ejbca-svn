package org.ejbca.nodecontrol;

import javax.servlet.http.HttpServletResponse;

/** 
 * A class containing some helpful functions used in more than one servlet, avoiding code duplication.
 * 
 * @author tomasg
 * @version $Id: ServletUtils.java,v 1.2 2006/09/20 15:44:56 anatom Exp $
 */
public class ServletUtils {

    /** Helper methods that removes no-cache headers from a response. No-cache headers 
     * makes IE refuse to save a file that is sent (for exmaple a certificate). 
     * No-cache headers are also autmatically added by Tomcat by default, so we better
     * make sure they are set to a harmless value.
     * 
     * @param res HttpServletResponse parameter as taken from the doGet, doPost methods in a Servlet.
     */
    public static void removeCacheHeaders(HttpServletResponse res) {
        if (res.containsHeader("Pragma")) {
            res.setHeader("Pragma","null");
        }
        if (res.containsHeader("Cache-Control")) {
            res.setHeader("Cache-Control","null");
        }
    }

    /** Helper methods that adds no-cache headers to a response. 
     * 
     * @param res HttpServletResponse parameter as taken from the doGet, doPost methods in a Servlet.
     */
    public static void addCacheHeaders(HttpServletResponse res) {
        if (!res.containsHeader("Pragma")) {
            res.setHeader("Pragma","no-cache");
        }
        if (!res.containsHeader("Cache-Control")) {
            res.setHeader("Cache-Control","no-cache");
        }
    }
}
