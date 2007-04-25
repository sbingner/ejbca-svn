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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.ejbca.core.ejb.ServiceLocator;
import org.ejbca.core.model.ca.SignRequestException;
import org.ejbca.core.model.ca.SignRequestSignatureException;
import org.ejbca.core.model.ra.NotFoundException;
import org.ejbca.core.protocol.FailInfo;
import org.ejbca.core.protocol.IRequestMessage;
import org.ejbca.core.protocol.IResponseMessage;
import org.ejbca.core.protocol.ResponseStatus;
import org.ejbca.core.protocol.ScepRequestMessage;
import org.ejbca.core.protocol.ScepResponseMessage;
import org.ejbca.extra.db.ExtRAPKCS10Request;
import org.ejbca.extra.db.ExtRAPKCS10Response;
import org.ejbca.extra.db.HibernateUtil;
import org.ejbca.extra.db.Message;
import org.ejbca.extra.db.MessageHome;
import org.ejbca.extra.db.SubMessages;
import org.ejbca.extra.util.RAKeyStore;
import org.ejbca.ui.web.RequestHelper;
import org.ejbca.util.Base64;
import org.ejbca.util.CertTools;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;



/**
 * Servlet implementing the RA interface of the Simple Certificate Enrollment Protocol (SCEP)
 * It have three funtions:
 *   * Return the CA certificate
 *   * Save certificate requests to ra database and respond pending
 *   * Send back SCEP Success or Failed upon certificate poll request if request have
 *   been processed by CA, othervise respond with pending
 * 
 * 
 * @version $Id: ScepRAServlet.java,v 1.7 2007-04-25 12:47:08 anatom Exp $
 */
public class ScepRAServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;


	private static Logger log = Logger.getLogger(ScepRAServlet.class);   
    

	private SecureRandom randomSource;
	private RAKeyStore scepraks;
	private String keystorepwd;
	private String cryptProvider;
	private MessageHome msgHome = new MessageHome(MessageHome.MESSAGETYPE_SCEPRA);
	String certificateProfile = "ENDUSER";
	String entityProfile = "EMPTY";
	String defaultCA = "ScepTest";

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
            log.debug("Re-installing BC-provider");
            CertTools.removeBCProvider();
            CertTools.installBCProvider();
            
            String keystorepath = getInitParameter("keyStorePath");
            keystorepwd = getInitParameter("keyStorePassword");
            cryptProvider = getInitParameter("cryptProvider");

            scepraks = new RAKeyStore(keystorepath, keystorepwd);
            
            String randomAlgorithm = "SHA1PRNG";
            randomSource = SecureRandom.getInstance(randomAlgorithm);
            
            SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
            HibernateUtil.setSessionFactory(HibernateUtil.SESSIONFACTORY_RAMESSAGE, sessionFactory,false);

            certificateProfile = ServiceLocator.getInstance().getString("java:comp/env/certificateProfile");            
            entityProfile = ServiceLocator.getInstance().getString("java:comp/env/entityProfile");            
            defaultCA = ServiceLocator.getInstance().getString("java:comp/env/defaultCA");
            log.info("Using certificate profile: "+certificateProfile);
            log.info("Using entity profile: "+entityProfile);
            log.info("Using default CA: "+defaultCA);
            
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
            X509Certificate racert = (X509Certificate) raks.getCertificate(alias);
            PrivateKey rapriv = (PrivateKey) raks.getKey(alias, keystorepwd.toCharArray());

        	
            if (operation.equals("PKIOperation")) {
                byte[] scepmsg = Base64.decode(message.getBytes());
 
                // Read the message end get the cert, this also checksauthorization
                boolean includeCACert = true;
                if (StringUtils.equals("0", getInitParameter("includeCACert"))) {
                	includeCACert = false;
                }

                byte[] reply = null;                                
                ScepRequestMessage reqmsg = new ScepRequestMessage(scepmsg, includeCACert);
                String transId = reqmsg.getTransactionId();
                if(reqmsg.getMessageType() == ScepRequestMessage.SCEP_TYPE_GETCERTINITIAL) {
                	log.info("Received a GetCertInitial message from host: "+remoteAddr);
                	Message msg = msgHome.findByMessageId(transId);
                	if(msg != null) {
                		if(msg.getStatus().equals(Message.STATUS_PROCESSED)) {
                			log.debug("Request is processed with status: "+msg.getStatus());
                			SubMessages submessagesresp = msg.getSubMessages(null,null,null);
                			Iterator iter =  submessagesresp.getSubMessages().iterator();
                			ExtRAPKCS10Response resp = (ExtRAPKCS10Response) iter.next();
                			// create proper ScepResponseMessage
                			IResponseMessage ret = reqmsg.createResponseMessage(org.ejbca.core.protocol.ScepResponseMessage.class, reqmsg, racert, rapriv, rapriv, cryptProvider);
            				X509Certificate respCert = resp.getCertificate();
                			if ( resp.isSuccessful() && (respCert != null) ) {
                				ret.setCertificate(respCert);                					
                			} else {
                				ret.setStatus(ResponseStatus.FAILURE);
                				ret.setFailInfo(FailInfo.BAD_REQUEST);
                				String failText = resp.getFailInfo();
                				ret.setFailText(failText);
                			}
                			ret.create();
                			reply = ret.getResponseMessage();                				
                		} else {
                			log.debug("Request is not yet processed, status: "+msg.getStatus());
                    		reply = createPendingResponseMessage(reqmsg, racert, rapriv, cryptProvider).getResponseMessage();
                    		log.debug("Responding with pending response, still pending.");               			
                		}                		
                	}else{
                		// User doesn't exist
                	}
                } else {         
                	if(reqmsg.getMessageType() == ScepRequestMessage.SCEP_TYPE_PKCSREQ) {  
                    	log.debug("Received a PKCSReq message from host: "+remoteAddr);
                    	// Decrypt the Scep message and extract the pkcs10 request
                        if (reqmsg.requireKeyInfo()) {
                            // scep encrypts message with the RAs certificate
                            reqmsg.setKeyInfo(racert, rapriv, cryptProvider);
                        }
                        // Verify the request
                        if (reqmsg.verify() == false) {
                        	String msg = "POPO verification failed.";
                            log.error(msg);
                            throw new SignRequestSignatureException(msg);
                        }
                        String username = reqmsg.getUsername(); 
                        if (username == null) {
                        	String msg = "No username in request, request DN: "+reqmsg.getRequestDN();
                            log.error(msg);
                            throw new SignRequestException(msg);
                        }
                        log.info("Received a SCEP/PKCS10 request for user: "+username+", from host: "+remoteAddr);
                        PKCS10CertificationRequest p10 = reqmsg.getCertificationRequest();
                        // Try to find the CA name from the issuerDN, if we can't find it (i.e. not defuined in web.xml) we use the default
                        String issuerDN = CertTools.stringToBCDNString(reqmsg.getIssuerDN());
                        String caName = ServiceLocator.getInstance().getString("java:comp/env/"+issuerDN);
                        if (StringUtils.isEmpty(caName)) {
                        	caName = defaultCA;
                        	log.debug("Did not find a CA name from issuerDN: "+issuerDN+", using the default CA '"+caName+"'");
                        } else {
                        	log.debug("Found a CA name '"+caName+"' from issuerDN: "+issuerDN);
                        }
                        String pkcs10 = new String(Base64.encode(p10.getEncoded(), false));
                        
                    	// Create a pkcs10 request
                		ExtRAPKCS10Request req = new ExtRAPKCS10Request(100,username, reqmsg.getRequestDN(), null, null, null, entityProfile, certificateProfile, caName, pkcs10);
                		SubMessages submessages = new SubMessages();
                		submessages.addSubMessage(req);
                		msgHome.create(transId, submessages);
                		reply = createPendingResponseMessage(reqmsg, racert, rapriv, cryptProvider).getResponseMessage();
                	}
                }
                
                if (reply == null) {
                    // This is probably a getCert message?
                    response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Can not handle request");
                    return;
                }
                // Send back Scep response, PKCS#7 which contains the end entity's certificate (or failure)
                RequestHelper.sendBinaryBytes(reply, response, "application/x-pki-message", null);
            } else if (operation.equals("GetCACert")) {
                // The response has the content type tagged as application/x-x509-ca-cert. 
                // The body of the response is a DER encoded binary X.509 certificate. 
                // For example: "Content-Type:application/x-x509-ca-cert\n\n"<BER-encoded X509>
            	// IF we are not an RA, which in case we should return the same thing as GetCACertChain
                log.debug("Got SCEP cert request for CA '" + message + "'");
                Certificate[] chain = raks.getCertificateChain(alias);
                if (chain != null) {
                	if (chain.length > 1) {
                		// We are an RA, so return the same as GetCACertChain
                        getCACertChain(message, remoteAddr, response, alias, raks);
                	} else {
                    	// The CA certificate is no 0
                    	X509Certificate cert = (X509Certificate)chain[0];
                    	if (chain.length > 1) {
                    		cert = (X509Certificate)chain[1];
                    	}
                    	log.debug("Found cert with DN '" + cert.getSubjectDN().toString() + "'");
                        log.info("Sent certificate for CA '" + message + "' to SCEP client with ip " + remoteAddr);
                        RequestHelper.sendNewX509CaCert(cert.getEncoded(), response);                		
                	}
                } else {
                    log.error("No CA certificates found");
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "No CA certificates found.");
                }                
            } else if (operation.equals("GetCACertChain")) {                
                // The response for GetCACertChain is a certificates-only PKCS#7 
                // SignedDatato carry the certificates to the end entity, with a 
                // Content-Type of application/x-x509-ca-ra-cert-chain.
                log.debug("Got SCEP cert chain request for CA '" + message + "'");
                getCACertChain(message, remoteAddr, response, alias, raks);
            } else if (operation.equals("GetCACaps")) {
                // The response for GetCACaps is a <lf> separated list of capabilities

                /*
                 "GetNextCACert"       CA Supports the GetNextCACert message.
                 "POSTPKIOperation"    PKIOPeration messages may be sent via HTTP POST.
                 "SHA-1"               CA Supports the SHA-1 hashing algorithm in 
                                       signatures and fingerprints.  If present, the
                                       client SHOULD use SHA-1.  If absent, the client
                                       MUST use MD5 to maintain backward compatability.
                 "Renewal"             Clients may use current certificate and key to
                                       authenticate an enrollment request for a new
                                       certificate.  
                 */
                log.debug("Got SCEP CACaps request for CA '" + message + "'");
                response.setContentType("text/plain");
                response.getOutputStream().print("POSTPKIOperation\nSHA-1");
            }	
        } catch (java.lang.ArrayIndexOutOfBoundsException ae) {
            log.error("Empty or invalid request received.", ae);            
            // TODO: Send back proper Failure Response
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ae.getMessage());
        } catch (Exception e) {
            log.error("Error in ScepRAServlet:", e);
            // TODO: Send back proper Failure Response
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

	private void getCACertChain(String message, String remoteAddr, HttpServletResponse response, String alias, KeyStore raks) throws KeyStoreException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, CertStoreException, CMSException, IOException, Exception {
		Certificate[] chain = raks.getCertificateChain(alias);
		if (chain != null) {
			X509Certificate cert = (X509Certificate) raks.getCertificateChain(alias)[0];
			log.debug("Found cert with DN '" + cert.getSubjectDN().toString() + "'");
			byte[] pkcs7response = createPKCS7(chain);                               
			log.debug("Sent certificate(s) for CA/RA '" + message + "' to SCEP client with ip "+remoteAddr);
			RequestHelper.sendBinaryBytes(pkcs7response, response, "application/x-x509-ca-ra-cert", null);                		
		} else {
		    log.error("No CA certificates found");
		    response.sendError(HttpServletResponse.SC_NOT_FOUND, "No CA certificates found.");
		}
	}
    
    private ScepResponseMessage createPendingResponseMessage(IRequestMessage req, X509Certificate racert, PrivateKey rakey, String cryptProvider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, IOException, SignRequestException, NotFoundException {
    	ScepResponseMessage ret = null;
    	// Create the response message and set all required fields
    	try {
    		ret = (ScepResponseMessage) Class.forName(org.ejbca.core.protocol.ScepResponseMessage.class.getName()).newInstance();
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
    		log.debug("Signing message with cert: "+racert.getSubjectDN().getName());
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
    
    private byte[] createPKCS7(Certificate[] chain) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, CertStoreException, CMSException, IOException {
    	Collection<Certificate> certList = Arrays.asList(chain);
    	CMSProcessable msg = new CMSProcessableByteArray("EJBCA".getBytes());
    	CertStore certs = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList), "BC");
    	CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
    	gen.addCertificatesAndCRLs(certs);
    	CMSSignedData s = gen.generate(msg, "BC");
    	return s.getEncoded();
    }    

    
    
} // ScepRAServlet
