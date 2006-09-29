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
package org.ejbca.extra.ra;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ejbca.core.model.authorization.AuthorizationDeniedException;
import org.ejbca.core.model.ca.AuthLoginException;
import org.ejbca.core.model.ca.AuthStatusException;
import org.ejbca.core.model.ca.SignRequestException;
import org.ejbca.core.model.ca.caadmin.CADoesntExistsException;
import org.ejbca.core.model.ra.NotFoundException;
import org.ejbca.core.protocol.IRequestMessage;
import org.ejbca.core.protocol.ResponseStatus;
import org.ejbca.core.protocol.ScepRequestMessage;
import org.ejbca.core.protocol.ScepResponseMessage;
import org.ejbca.extra.db.HibernateUtil;
import org.ejbca.extra.db.Message;
import org.ejbca.extra.db.MessageHome;
import org.ejbca.extra.db.SCEPSubMessage;
import org.ejbca.extra.db.SubMessages;
import org.ejbca.extra.util.RAKeyStore;
import org.ejbca.ui.web.RequestHelper;
import org.ejbca.util.Base64;
import org.ejbca.util.CertTools;
import org.hibernate.SessionFactory;



/**
 * Servlet implementing the RA interface of the Simple Certificate Enrollment Protocol (SCEP)
 * It have three funtions:
 *   * Return the CA certificate
 *   * Save certificate requests to ra database and respond pending
 *   * Send back SCEP Success or Failed upon certificate poll request if request have
 *   been processed by CA, othervise respond with pending
 * 
 * 
 * @version $Id: ScepRAServlet.java,v 1.3 2006-09-29 13:44:54 anatom Exp $
 */
public class ScepRAServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;


	private static Logger log = Logger.getLogger(ScepRAServlet.class);   
    

	private SecureRandom randomSource;
	private RAKeyStore scepraks;
	private String keystorepwd;
	private String cryptProvider;
	private MessageHome msgHome = new MessageHome(MessageHome.MESSAGETYPE_SCEPRA);

    /**
     * Inits the SCEP servlet
     *
     * @param config servlet configuration
     *
     * @throws ServletException on error during initialization
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try {
            // Install BouncyCastle provider
            CertTools.installBCProvider();
            
            String keystorepath = getInitParameter("keyStorePath");
            keystorepwd = getInitParameter("keyStorePassword");
            cryptProvider = getInitParameter("cryptProvider");

            scepraks = new RAKeyStore(keystorepath, keystorepwd);
            
            String randomAlgorithm = "SHA1PRNG";
            randomSource = SecureRandom.getInstance(randomAlgorithm);
            
            Context context = new InitialContext();
            HibernateUtil.setSessionFactory(HibernateUtil.SESSIONFACTORY_RAMESSAGE, (SessionFactory) context.lookup(getInitParameter("sessionFactoryContext")),false);
            
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Handles HTTP post
     *
     * @param request java standard arg
     * @param response java standard arg
     *
     * @throws IOException input/output error
     * @throws ServletException if the post could not be handled
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        log.debug(">doPost()");
        /* 
         If the remote CA supports it, any of the PKCS#7-encoded SCEP messages
         may be sent via HTTP POST instead of HTTP GET.   This is allowed for
         any SCEP message except GetCACert, GetCACertChain, GetNextCACert,
         or GetCACaps.  In this form of the message, Base 64 encoding is not
         used.
         
         POST /cgi-bin/pkiclient.exe?operation=PKIOperation
         <binary PKCS7 data>
         */
        String operation = "PKIOperation";
        ServletInputStream sin = request.getInputStream();
        // This small code snippet is inspired/copied by apache IO utils to Tomas Gustavsson...
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;
        while (-1 != (n = sin.read(buf))) {
            output.write(buf, 0, n);
        }
        String message = new String(Base64.encode(output.toByteArray()));
        service(operation, message, request.getRemoteAddr(), response);
        log.debug("<doPost()");
    } //doPost

    /**
     * Handles HTTP get
     *
     * @param request java standard arg
     * @param response java standard arg
     *
     * @throws IOException input/output error
     * @throws ServletException if the post could not be handled
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws java.io.IOException, ServletException {
        log.debug(">doGet()");

            log.debug("query string=" + request.getQueryString());

            // These are mandatory in SCEP GET
            /*
             GET /cgi-bin/pkiclient.exe?operation=PKIOperation&message=MIAGCSqGSIb3D
             QEHA6CAMIACAQAxgDCBzAIBADB2MGIxETAPBgNVBAcTCE ......AAAAAA== 
             */
            String operation = request.getParameter("operation");
            String message = request.getParameter("message");

            service(operation, message, request.getRemoteAddr(), response);
            
        log.debug("<doGet()");
    } // doGet

    private void service(String operation, String message, String remoteAddr, HttpServletResponse response) throws IOException {
        try {
            if ((operation == null) || (message == null)) {
                log.error("Got request missing operation and/or message parameters.");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "Parameters 'operation' and 'message' must be supplied!");
                return;
            }
            log.debug("Got request '" + operation + "'");
            log.debug("Message: " + message);
        	log.debug("Operation is : " + operation);
        	
            String alias = scepraks.getAlias();
        	log.debug("SCEP RA Keystore alias : " + alias);
            KeyStore raks = scepraks.getKeyStore();

        	
            if (operation.equals("PKIOperation")) {
                byte[] scepmsg = Base64.decode(message.getBytes());
 
                // Read the message end get the cert, this also checksauthorization
                boolean includeCACert = true;
                if (StringUtils.equals("0", getInitParameter("includeCACert"))) {
                	includeCACert = false;
                }

                byte[] reply = null;                                
                ScepRequestMessage reqmsg = new ScepRequestMessage(scepmsg, includeCACert);
                if(reqmsg.getMessageType() == ScepRequestMessage.SCEP_TYPE_GETCERTINITIAL){
                	log.debug("Got a get certinitial");
                	
                	Message msg = msgHome.findByMessageId(remoteAddr);
                	if(msg != null){
                		if(msg.getStatus() == Message.STATUS_PROCESSED){
                			log.debug("Request is processed");
                			reply = ((SCEPSubMessage) msg.getSubMessages(null,null,null).getSubMessages().iterator().next()).getScepData();
                		}else{
                			log.debug("Request is not yet processed");
                    		ScepRequestMessage orgmsg = new ScepRequestMessage(((SCEPSubMessage) msg.getSubMessages(null,null,null).getSubMessages().iterator().next()).getScepData(), includeCACert);
                    		reply = createPendingResponseMessage(orgmsg, (X509Certificate) raks.getCertificate(alias), (PrivateKey) raks.getKey(alias, keystorepwd.toCharArray()), cryptProvider).getResponseMessage();
                    		log.debug("Responding with pending response of original request.");               			
                		}                		
                	}else{
                		// User doesn't exist
                	}
                	
                	                	                	
                }else{         
                	if(reqmsg.getMessageType() == ScepRequestMessage.SCEP_TYPE_PKCSREQ){  
                		SubMessages submessages = new SubMessages();
                		submessages.addSubMessage(new SCEPSubMessage(scepmsg));
                		msgHome.create(remoteAddr, submessages);
                		reply = createPendingResponseMessage(reqmsg, (X509Certificate) raks.getCertificate(alias), (PrivateKey) raks.getKey(alias, keystorepwd.toCharArray()), cryptProvider).getResponseMessage();
                	}
                }
                
                if (reply == null) {
                    // This is probably a getCert message?
                    response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
                    "Can not handle request");
                    return;
                }
                // Send back Scep response, PKCS#7 which contains the end entity's certificate (or failure)
                RequestHelper.sendBinaryBytes(reply, response, "application/x-pki-message");
            } else if (operation.equals("GetCACert")) {
                log.debug("Got SCEP cert request for CA '" + message + "'");
            
                //X509Certificate racert = (X509Certificate) raks.getCertificateChain(alias)[0];
                
                X509Certificate cacert = (X509Certificate) raks.getCertificateChain(alias)[1];
                log.debug("Sent certificate for CA '" + message + "' to SCEP client.");
                RequestHelper.sendNewX509CaCert(cacert.getEncoded(), response);
                
                /*byte[] pkcs7response = signsession.createPKCS7(administrator, racert, true);                               
                
                if (racert != null ) {
                	log.debug("Found RA with DN '" + racert.getSubjectDN().toString() + "'");
                    // CAs certificate is in the first position in the Collection
                    log.debug("Sent RA and CA certificate for CA '" + message + "' to SCEP client.");
                    
                                        
                    RequestHelper.sendBinaryBytes(pkcs7response, response, "application/x-x509-ca-ra-cert");
                } else {
                    log.error("SCEP cert request for unknown CA '" + message + "'");
                    response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "No CA certificates found.");
                }*/
            } else if (operation.equals("GetCACertChain")) {
            	log.error("Error recieved a GetCACert operation which isn't supported");
            } else if (operation.equals("GetCACaps")) {
            	log.error("Error recieved a GetCACaps operation which isn't supported");
            }	
        } catch (CADoesntExistsException cae) {
            log.error("SCEP cert request for unknown CA, or can't find user.", cae);
            
            // TODO: Send back proper Failure Response
            response.sendError(HttpServletResponse.SC_NOT_FOUND, cae.getMessage());
        } catch (java.lang.ArrayIndexOutOfBoundsException ae) {
            log.error("Empty or invalid request received.", ae);
            
            // TODO: Send back proper Failure Response
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ae.getMessage());
        } catch (AuthorizationDeniedException ae) {
            log.error("Authorization denied.", ae);
            
            // TODO: Send back proper Failure Response
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
        } catch (AuthLoginException ae) {
            log.error("Authorization denied.", ae);
            
            // TODO: Send back proper Failure Response
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
        } catch (AuthStatusException ae) {
            log.error("Wrong client status.", ae);
            
            // TODO: Send back proper Failure Response
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
        } catch (Exception e) {
            log.error("Error in ScepRAServlet:", e);
            
            // TODO: Send back proper Failure Response
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
    
    private ScepResponseMessage createPendingResponseMessage(IRequestMessage req, X509Certificate racert, PrivateKey rakey, String cryptProvider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, IOException, SignRequestException, NotFoundException {
    	ScepResponseMessage ret = null;
    	// Create the response message and set all required fields
    	try {
    		ret = (ScepResponseMessage) Class.forName("org.ejbca.core.protocol.ScepResponseMessage").newInstance();
    	} catch (InstantiationException e) {
    		//TODO : do something with these exceptions
    		log.error("Error creating response message", e);
    		return null;
    	} catch (IllegalAccessException e) {
    		log.error("Error creating response message", e);
    		return null;
    	} catch (ClassNotFoundException e) {
    		log.error("Error creating response message", e);
    		return null;
		}
    	if (ret.requireSignKeyInfo()) {
    		ret.setSignKeyInfo(racert, rakey, cryptProvider);
    	}
    	if (ret.requireEncKeyInfo()) {
    		ret.setEncKeyInfo(racert, rakey, cryptProvider);
    	}
    	if (req.getSenderNonce() != null) {
    		ret.setRecipientNonce(req.getSenderNonce());
    	}
    	if (req.getTransactionId() != null) {
    		ret.setTransactionId(req.getTransactionId());
    	}
    	// Sendernonce is a random number
    	byte[] senderNonce = new byte[16];
    	randomSource.nextBytes(senderNonce);
    	ret.setSenderNonce(new String(Base64.encode(senderNonce)));
    	// If we have a specified request key info, use it in the reply
    	if (req.getRequestKeyInfo() != null) {
    		ret.setRecipientKeyInfo(req.getRequestKeyInfo());
    	}
    	// Which digest algorithm to use to create the response, if applicable
    	ret.setPreferredDigestAlg(req.getPreferredDigestAlg());
    	// Include the CA cert or not in the response, if applicable for the response type
    	ret.setIncludeCACert(req.includeCACert());         
        ret.setStatus(ResponseStatus.PENDING);
        ret.create();
    	return ret;
    }
    

    
    
} // ScepRAServlet
