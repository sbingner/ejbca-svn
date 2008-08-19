package org.ejbca.nodecontrol;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.ejbca.nodecontrol.util.CertTools;
import org.ejbca.nodecontrol.util.FileTools;
import org.ejbca.nodecontrol.util.NodecontrolConfiguration;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;


@Name("authenticator")
public class Authenticator
{
    @Logger Log log;
	
    @In
	private FacesMessages facesMessages;

    @In Identity identity;
    
    private String cn = null;
   
    HashMap<String, String> authorizedKeys = null;
    
    // This method is configured in pages.xml as an action called for all pages:
    // 	<page view-id="/*" login-required="true" action="#{authenticator.checkLogin}"/>
    public void checkLogin() throws CertificateEncodingException, NoSuchAlgorithmException {
    	log.debug(">checkLogin");
    	// if already logged on, simply continue
    	if (identity.isLoggedIn()) {
    		log.debug("Already logged in as: "+identity.getUsername());
        	log.debug("<checkLogin");
    		return;
    	}

    	// try SSO auto login
    	HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    	log.info("Login request from host #0", request.getRemoteAddr());
        if (request.getAttribute("javax.servlet.request.X509Certificate") != null) {
            X509Certificate cert = ((X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate"))[0];
            log.debug("Certificate: "+cert);
            String name = cert.getSubjectDN().getName();
        	log.debug("Got a certificate for user with name #0", name);
            String fp = CertTools.getFingerprintAsString(cert);
            log.debug("Verifying certificate with sha1 fp: #0", fp);
            if (authorizedKeys == null) {
            	initAuthorizedKeys();
            }
            boolean ok = false;
            try {
            	String role = authorizedKeys.get(fp);
            	if (role != null) {
            		log.info("Found a matching fingerprint for user certificate with fp #0. User #1 has role #2.", fp, name, role);
                	ok = true;
            	} else {
            		log.info("Did not find a matching fingerprint for user certificate with fp #0. User #1.", fp, name);
            	}
            } catch (Exception e) {
            	log.error("Failure verifying certificate: ", e);
            }
            if (!ok) {
            	facesMessages.add("Invalid certificate for '"+name+"'");
            	throw new NotLoggedInException();
            } else {
            	cn = CertTools.getPartFromDN(name, "CN");
            	if(cn != null) {
            		identity.setUsername(cn);
            		identity.login(); // Don't know another, more direct way to login, so store  userData in field and check it in the authenticate method
            	}
            }
        } else {
        	log.error("Could not get a certificate for user from ip #0", request.getRemoteAddr());
        }
    	log.debug("<checkLogin");
    }
    
    public boolean authenticate() {
    	log.debug(">authenticate");
        if (cn == null) {
        	log.error("Not logged in with a valid certificate");
        	facesMessages.add("Not logged in with a valid certificate.");
        	log.debug("<authenticate");
            return false;
        }

        // previously a sso token has been validated - log in automatically
        cn = null;
        facesMessages.clear();
    	log.debug("<authenticate");
        return true;
    }
    
    //
    // Private helpers
    //
    private void initAuthorizedKeys() {
        File dir = new File(NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.AUTHORIZED_ADMINCERTS_DIR));
        try {
            if (dir == null || dir.isDirectory() == false) {
                log.error(dir.getCanonicalPath()+ " is not a directory.");
                throw new IllegalArgumentException(dir.getCanonicalPath()+ " is not a directory.");                
            }
            File files[] = dir.listFiles();
            if (files == null || files.length == 0) {
                log.error("There are no files in directory "+dir.getCanonicalPath());                
            }
        	authorizedKeys = new HashMap<String, String>();
            for ( int i=0; i<files.length; i++ ) {
                final String fileName = files[i].getCanonicalPath();
                if (fileName.endsWith(".pem")) {
                    log.info("Loading certificate file #0", fileName);
                    // Read the file, don't stop completely if one file has errors in it
                    try {
                        byte[] bytes = FileTools.getBytesFromPEM(FileTools.readFiletoBuffer(fileName),
                                "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----");
                        X509Certificate cert = CertTools.getCertfromByteArray(bytes);
                        String key = CertTools.getFingerprintAsString(cert);
                        log.info("Adding certificate with sha1 fp: #0", key);
                        authorizedKeys.put(key, "admin");
                    } catch (CertificateException e) {
                        log.error("Error reading certificate "+fileName+".", e);
                    } catch (IOException e) {
                        log.error("Error reading file "+fileName+".", e);
                    } catch (NoSuchAlgorithmException e) {
                        log.error("Error getting certificate fingerprint for "+fileName+".", e);
    				}                	
                } else {
                	log.info("#0 is not a .pem file so we will no load it.", fileName);
                }
            }
        } catch (IOException e) {
        	String errMsg = "Error reading authorized certificates";
            log.error(errMsg, e);
            throw new IllegalArgumentException(errMsg);
        }

    }
}
