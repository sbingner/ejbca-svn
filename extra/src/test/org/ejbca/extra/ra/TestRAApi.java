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

import java.math.BigInteger;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import junit.framework.TestCase;

import org.ejbca.extra.db.Constants;
import org.ejbca.extra.db.ExtRAPKCS10Response;
import org.ejbca.extra.db.ExtRAPKCS12Response;
import org.ejbca.extra.db.ExtRAResponse;
import org.ejbca.extra.db.ExtRARevocationRequest;
import org.ejbca.extra.db.Message;
import org.ejbca.extra.db.SubMessages;
import org.ejbca.extra.db.TestExtRAMessages;
import org.ejbca.extra.db.TestMessageHome;
import org.ejbca.util.CertTools;


/**
 * Junit test used to test the ExtRA Api in a similar enviroment as used in production. Will connect to a RA message database and
 * sent messages that should be pulled and processed by the CA.
 * 
 * The following requirements should be set in order to run the tests.
 * 
 * TODO
 * 
 * 
 * @author philip
 * $Id: TestRAApi.java,v 1.3 2006-08-09 07:33:07 anatom Exp $
 */

public class TestRAApi extends TestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
		CertTools.installBCProvider();			
	}
	
	private static X509Certificate firstCertificate = null;
	private static X509Certificate secondCertificate = null;
	
	public void test01GenerateSimplePKCS10Request() throws Exception {
		
		SubMessages smgs = new SubMessages(null,null,null);
		smgs.addSubMessage(TestExtRAMessages.genExtRAPKCS10Request(100,"SimplePKCS10Test1", Constants.pkcs10_1));
		smgs.addSubMessage(TestExtRAMessages.genExtRAPKCS10Request(101,"SimplePKCS10Test1", Constants.pkcs10_2));
		
		TestMessageHome.msghome.create("SimplePKCS10Test1", smgs);
		
        Message msg = waitForUser("SimplePKCS10Test1");
		
		assertNotNull(msg);
		
		SubMessages submessagesresp = msg.getSubMessages(null,null,null);
		
		assertTrue(submessagesresp.getSubMessages().size() == 2);
		
		Iterator iter =  submessagesresp.getSubMessages().iterator();
		ExtRAPKCS10Response resp = (ExtRAPKCS10Response) iter.next();
		assertTrue(resp.getRequestId() == 100);
		assertTrue(resp.isSuccessful() == true);		
		assertTrue(resp.getCertificate().getSubjectDN().toString().equals("CN=PKCS10REQ"));
		firstCertificate = resp.getCertificate();
		
		
	    resp = (ExtRAPKCS10Response) iter.next();
		assertTrue(resp.getRequestId() == 101);
		assertTrue(resp.isSuccessful() == true);		
		assertTrue(resp.getCertificate().getSubjectDN().toString().equals("CN=PKCS10REQ"));
		secondCertificate = resp.getCertificate();
	
	}
	
	public void test02GenerateSimplePKCS12Request() throws Exception {
		
		SubMessages smgs = new SubMessages(null,null,null);
		smgs.addSubMessage(TestExtRAMessages.genExtRAPKCS12Request(200,"SimplePKCS12Test1", false));
		
		TestMessageHome.msghome.create("SimplePKCS12Test1", smgs);
		
        Message msg = waitForUser("SimplePKCS12Test1");
		
		assertNotNull(msg);
		
		SubMessages submessagesresp = msg.getSubMessages(null,null,null);
		
		assertTrue(submessagesresp.getSubMessages().size() == 1);
		
		ExtRAPKCS12Response resp = (ExtRAPKCS12Response) submessagesresp.getSubMessages().iterator().next();
		assertTrue(resp.getRequestId() == 200);
		assertTrue(resp.isSuccessful() == true);
		assertNotNull(resp.getKeyStore("foo123"));
		KeyStore ks = resp.getKeyStore("foo123");
		String alias = (String) ks.aliases().nextElement();
		
		assertTrue(((X509Certificate) resp.getKeyStore("foo123").getCertificate(alias)).getSubjectDN().toString().equals("CN=PKCS12REQ"));
			
		
		
	}
	
	public void test03GenerateSimpleKeyRecoveryRequest() throws Exception {
		
		// First generate keystore
		SubMessages smgs = new SubMessages(null,null,null);
		smgs.addSubMessage(TestExtRAMessages.genExtRAPKCS12Request(300,"SimpleKeyRecTest", true));
		
		TestMessageHome.msghome.create("SimpleKeyRecTest", smgs);
		
        Message msg = waitForUser("SimpleKeyRecTest");
		
		assertNotNull(msg);
		
		SubMessages submessagesresp = msg.getSubMessages(null,null,null);
		
		assertTrue(submessagesresp.getSubMessages().size() == 1);
		
		ExtRAPKCS12Response resp = (ExtRAPKCS12Response) submessagesresp.getSubMessages().iterator().next();
		assertTrue(resp.getRequestId() == 300);
		assertTrue(resp.isSuccessful() == true);
		assertNotNull(resp.getKeyStore("foo123"));
		//KeyStore ks = resp.getKeyStore("foo123");		
		
		X509Certificate orgCert = (X509Certificate) resp.getKeyStore("foo123").getCertificate("PKCS12REQ");
		
		assertTrue(orgCert.getSubjectDN().toString().equals("CN=PKCS12REQ"));
		
		// Generate Key Reokevry request with original cert.
		
		smgs = new SubMessages(null,null,null);
		smgs.addSubMessage(TestExtRAMessages.genExtRAKeyRecoveryRequest(301,"SimpleKeyRecTest",true,orgCert));
		
		TestMessageHome.msghome.create("SimpleKeyRecTest", smgs);
		
        msg = waitForUser("SimpleKeyRecTest");
		
		assertNotNull(msg);
		
		submessagesresp = msg.getSubMessages(null,null,null);
		
		assertTrue(submessagesresp.getSubMessages().size() == 1);
		
		resp = (ExtRAPKCS12Response) submessagesresp.getSubMessages().iterator().next();
		assertTrue(resp.getRequestId() == 301);
		assertTrue(resp.isSuccessful() == true);
		
		X509Certificate keyRecCert = (X509Certificate) resp.getKeyStore("foo123").getCertificate("PKCS12REQ");
        assertTrue(keyRecCert.getSerialNumber().equals(orgCert.getSerialNumber()));
        
        // Generate Key Recovery Request with new cert
        
		smgs = new SubMessages(null,null,null);
		smgs.addSubMessage(TestExtRAMessages.genExtRAKeyRecoveryRequest(302,"SimpleKeyRecTest",false,orgCert));
		
		TestMessageHome.msghome.create("SimpleKeyRecTest", smgs);
		
        msg = waitForUser("SimpleKeyRecTest");
		
		assertNotNull(msg);
		
		submessagesresp = msg.getSubMessages(null,null,null);
		
		assertTrue(submessagesresp.getSubMessages().size() == 1);
		
		resp = (ExtRAPKCS12Response) submessagesresp.getSubMessages().iterator().next();
		assertTrue(resp.getRequestId() == 302);
		assertTrue(resp.isSuccessful() == true);
		
		keyRecCert = (X509Certificate) resp.getKeyStore("foo123").getCertificate("KEYRECREQ");
        assertFalse(keyRecCert.getSerialNumber().equals(orgCert.getSerialNumber()));
        

		
	}
	
	public void test04GenerateSimpleRevokationRequest() throws Exception {
		
		// revoke first certificate
		SubMessages smgs = new SubMessages(null,null,null);
		smgs.addSubMessage(new ExtRARevocationRequest(10, CertTools.getIssuerDN(firstCertificate), firstCertificate.getSerialNumber(), ExtRARevocationRequest.REVOKATION_REASON_UNSPECIFIED));
		
		TestMessageHome.msghome.create("SimpleRevocationTest", smgs);
		
        Message msg = waitForUser("SimpleRevocationTest");
		
		assertNotNull(msg);
		
		SubMessages submessagesresp = msg.getSubMessages(null,null,null);
		
		assertTrue("Number of submessages " + submessagesresp.getSubMessages().size(), submessagesresp.getSubMessages().size() == 1);
		
		ExtRAResponse resp = (ExtRAResponse) submessagesresp.getSubMessages().iterator().next();
		assertTrue("Wrong Request ID" + resp.getRequestId(), resp.getRequestId() == 10);
		assertTrue(resp.isSuccessful() == true);
	
		// revoke second certificate	
		SubMessages smgs2 = new SubMessages(null,null,null);
		smgs2.addSubMessage(new ExtRARevocationRequest(6, CertTools.getIssuerDN(secondCertificate), secondCertificate.getSerialNumber(), ExtRARevocationRequest.REVOKATION_REASON_UNSPECIFIED));
		
		TestMessageHome.msghome.create("SimpleRevocationTest", smgs2);
		
        Message msg2 = waitForUser("SimpleRevocationTest");
		
		assertNotNull(msg2);
		
		SubMessages submessagesresp2 = msg2.getSubMessages(null,null,null);
		
		assertTrue("Number of submessages " + submessagesresp2.getSubMessages().size() ,  submessagesresp2.getSubMessages().size() == 1);
		
		ExtRAResponse resp2 = (ExtRAResponse) submessagesresp2.getSubMessages().iterator().next();
		assertTrue(resp2.getRequestId() == 6);
		assertTrue(resp2.isSuccessful() == true); 
		
		// try to revoke nonexisting certificate	
		SubMessages smgs3 = new SubMessages(null,null,null);
		smgs3.addSubMessage(new ExtRARevocationRequest(7, CertTools.getIssuerDN(secondCertificate), new BigInteger("1234"), ExtRARevocationRequest.REVOKATION_REASON_UNSPECIFIED));
		
		TestMessageHome.msghome.create("SimpleRevocationTest", smgs3);
		
        Message msg3 = waitForUser("SimpleRevocationTest");
		
		assertNotNull(msg3);
		
		SubMessages submessagesresp3 = msg3.getSubMessages(null,null,null);
		
		assertTrue(submessagesresp3.getSubMessages().size() == 1);
		
		ExtRAResponse resp3 = (ExtRAResponse) submessagesresp3.getSubMessages().iterator().next();
		assertTrue(resp3.getRequestId() == 7);
		assertTrue(resp3.isSuccessful() == false); 
        
		// try to revoke a users all certificates
		SubMessages smgs4 = new SubMessages(null,null,null);
		smgs4.addSubMessage(new ExtRARevocationRequest(8, CertTools.getIssuerDN(secondCertificate), secondCertificate.getSerialNumber(), ExtRARevocationRequest.REVOKATION_REASON_UNSPECIFIED, false, true));
		
		TestMessageHome.msghome.create("SimpleRevocationTest", smgs4);
		
        Message msg4 = waitForUser("SimpleRevocationTest");
		
		assertNotNull(msg4);
		
		SubMessages submessagesresp4 = msg4.getSubMessages(null,null,null);
		
		assertTrue(submessagesresp4.getSubMessages().size() == 1);
		
		ExtRAResponse resp4 = (ExtRAResponse) submessagesresp4.getSubMessages().iterator().next();
		assertTrue(resp4.getRequestId() == 8);
		assertTrue(resp4.isSuccessful() == true);
		
		// try to revoke a users all certificates by giving the username
		SubMessages smgs5 = new SubMessages(null,null,null);
		smgs5.addSubMessage(new ExtRARevocationRequest(9, "SimplePKCS10Test1", ExtRARevocationRequest.REVOKATION_REASON_UNSPECIFIED, false));
		
		TestMessageHome.msghome.create("SimpleRevocationTest", smgs5);
		
        Message msg5 = waitForUser("SimpleRevocationTest");
		
		assertNotNull(msg5);
		
		SubMessages submessagesresp5 = msg5.getSubMessages(null,null,null);
		
		assertTrue(submessagesresp5.getSubMessages().size() == 1);
		
		ExtRAResponse resp5 = (ExtRAResponse) submessagesresp5.getSubMessages().iterator().next();
		assertTrue(resp5.getRequestId() == 9);
		assertTrue(resp5.isSuccessful() == true);
		
		// Try some error cases
        // First a message with null as parameters
		SubMessages smgs6 = new SubMessages(null,null,null);
		smgs6.addSubMessage(new ExtRARevocationRequest(10, null, ExtRARevocationRequest.REVOKATION_REASON_UNSPECIFIED, false));		
		TestMessageHome.msghome.create("SimpleRevocationTest", smgs6);
        Message msg6 = waitForUser("SimpleRevocationTest");
		assertNotNull(msg6);
		SubMessages submessagesresp6 = msg6.getSubMessages(null,null,null);
		assertTrue(submessagesresp6.getSubMessages().size() == 1);
		ExtRAResponse resp6 = (ExtRAResponse) submessagesresp6.getSubMessages().iterator().next();
		assertTrue(resp6.getRequestId() == 10);
		assertTrue(resp6.isSuccessful() == false);
        assertEquals(resp6.getFailInfo(), "Either username or issuer/serno is required");
	}
	
	public void test05GenerateSimpleEditUserRequest() throws Exception {
		
		// revoke first certificate
		SubMessages smgs = new SubMessages(null,null,null);
		smgs.addSubMessage(TestExtRAMessages.genExtRAEditUserRequest(11,"SimpleEditUserTest"));
		
		TestMessageHome.msghome.create("SimpleEditUserTest", smgs);
		
        Message msg = waitForUser("SimpleEditUserTest");
		
		assertNotNull(msg);
		
		SubMessages submessagesresp = msg.getSubMessages(null,null,null);
		
		assertTrue("Number of submessages " + submessagesresp.getSubMessages().size(), submessagesresp.getSubMessages().size() == 1);
		
		ExtRAResponse resp = (ExtRAResponse) submessagesresp.getSubMessages().iterator().next();
		assertTrue("Wrong Request ID" + resp.getRequestId(), resp.getRequestId() == 11);
		assertTrue(resp.isSuccessful() == true);
	


	}	
	
	public void test06GenerateComplexRequest() throws Exception {
		

		SubMessages smgs = new SubMessages(null,null,null);
		smgs.addSubMessage(TestExtRAMessages.genExtRAPKCS10Request(1,"ComplexReq", Constants.pkcs10_1));
		smgs.addSubMessage(TestExtRAMessages.genExtRAPKCS12Request(2,"ComplexReq", false));
		smgs.addSubMessage(TestExtRAMessages.genExtRAPKCS12Request(3,"ComplexReq", false));
		
		TestMessageHome.msghome.create("COMPLEXREQ_1", smgs);
		
        Message msg = waitForUser("COMPLEXREQ_1");
		
		assertNotNull(msg);
		
		SubMessages submessagesresp = msg.getSubMessages(null,null,null);
		
		assertTrue(submessagesresp.getSubMessages().size() == 3);
		
		
		Iterator iter = submessagesresp.getSubMessages().iterator();
		ExtRAPKCS10Response resp1 = (ExtRAPKCS10Response) iter.next();
		ExtRAPKCS12Response resp2 = (ExtRAPKCS12Response) iter.next();
		ExtRAPKCS12Response resp3 = (ExtRAPKCS12Response) iter.next();
		assertTrue(resp1.getRequestId() == 1);
		assertTrue(resp1.isSuccessful() == true);
		assertTrue(resp2.getRequestId() == 2);
		assertTrue(resp2.isSuccessful() == true);
		assertTrue(resp3.getRequestId() == 3);
		assertTrue(resp3.isSuccessful() == true);
	}
	
	public void test06GenerateLotsOfRequest() throws Exception {
		
		int numberOfRequests = 10;
		
		
		
		for(int i=0; i< numberOfRequests; i++){
		  SubMessages smgs = new SubMessages(null,null,null);
		  smgs.addSubMessage(TestExtRAMessages.genExtRAPKCS10Request(1,"LotsOfReq" + i, Constants.pkcs10_1));		
		  TestMessageHome.msghome.create("LotsOfReq" + i, smgs);
		}
		  
		
		Message[] resps = new Message[numberOfRequests];
		for(int i=0; i < numberOfRequests; i++){
			resps[i] = waitForUser("LotsOfReq"+i);
			assertNotNull(resps[i]);
			SubMessages submessagesresp = resps[i].getSubMessages(null,null,null);
			ExtRAPKCS10Response resp = (ExtRAPKCS10Response) submessagesresp.getSubMessages().iterator().next();
			assertTrue(resp.isSuccessful() == true);
		}								

		
	} 
	
	
	
	
	
	private Message waitForUser(String user) throws InterruptedException{
		int waittime = 30; // Wait a maximum if 15 seconds
		
		boolean processed = false;
		Message msg = null;
		do{			
			msg = TestMessageHome.msghome.findByMessageId(user);
			assertNotNull(msg);
			
			if(msg.getStatus().equals(Message.STATUS_PROCESSED)){
				processed = true;
				break;
			}	
			Thread.sleep(1000);
		}while( waittime-- >= 0);
		
		if(!processed){
			msg = null;
		}
		
		return msg;
	}

}
