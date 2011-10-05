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

package org.ejbca.core.model.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authentication.tokens.UsernamePrincipal;
import org.cesecore.certificates.certificate.CertificateInfo;
import org.cesecore.certificates.certificate.CertificateStoreSessionRemote;
import org.cesecore.certificates.certificate.InternalCertificateStoreSessionRemote;
import org.cesecore.jndi.JndiHelper;
import org.cesecore.keys.util.KeyTools;
import org.cesecore.mock.authentication.tokens.TestAlwaysAllowLocalAuthenticationToken;
import org.cesecore.util.CertTools;
import org.cesecore.util.CryptoProviderTools;
import org.ejbca.core.ejb.ca.CaTestCase;
import org.ejbca.core.ejb.ca.sign.SignSessionRemote;
import org.ejbca.core.ejb.ra.UserAdminSessionRemote;
import org.ejbca.core.ejb.services.ServiceDataSessionRemote;
import org.ejbca.core.ejb.services.ServiceSessionRemote;
import org.ejbca.core.model.SecConst;
import org.ejbca.core.model.services.actions.NoAction;
import org.ejbca.core.model.services.intervals.PeriodicalInterval;
import org.ejbca.core.model.services.workers.CertificateExpirationNotifierWorker;
import org.ejbca.core.model.services.workers.EmailSendingWorkerConstants;
import org.ejbca.util.InterfaceCache;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the certificate expiration notifications.
 * 
 * @version $Id$
 */
public class CertificateExpireTest extends CaTestCase {

    private static final Logger log = Logger.getLogger(CertificateExpireTest.class);
    private static final AuthenticationToken admin = new TestAlwaysAllowLocalAuthenticationToken(new UsernamePrincipal("CertificateExpireTest"));
    private static final String CA_NAME = "CertExpNotifCA";
    private int caid = getTestCAId(CA_NAME);

    private static String username;
    private static String pwd;

    private static final String CERTIFICATE_EXPIRATION_SERVICE = "CertificateExpirationService";

    private InternalCertificateStoreSessionRemote internalCertificateStoreSession = JndiHelper.getRemoteSession(InternalCertificateStoreSessionRemote.class);
    private CertificateStoreSessionRemote certificateStoreSession = InterfaceCache.getCertificateStoreSession();
    private ServiceSessionRemote serviceSession = InterfaceCache.getServiceSession();
    private SignSessionRemote signSession = InterfaceCache.getSignSession();
    private UserAdminSessionRemote userAdminSession = InterfaceCache.getUserAdminSession();
    private ServiceDataSessionRemote serviceDataSession = InterfaceCache.getServiceDataSessionRemote();

 
    private List<Certificate> certificatesToRemove;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        createTestCA(CA_NAME);
        certificatesToRemove = new ArrayList<Certificate>();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        
        for(Certificate certificate : certificatesToRemove) {
            internalCertificateStoreSession.removeCertificate(certificate);
        }
        for(Certificate certificate : internalCertificateStoreSession.findCertificatesByIssuer("CN="+CA_NAME)) {
            internalCertificateStoreSession.removeCertificate(certificate);
        }
        
        userAdminSession.deleteUser(admin, username);
        log.debug("Removed user: " + username);
        serviceSession.removeService(admin, CERTIFICATE_EXPIRATION_SERVICE);
        log.debug("Removed service:" + CERTIFICATE_EXPIRATION_SERVICE);
        assertNull("ServiceData object with id 4711 was not removed properly.", serviceDataSession.findById(4711));
        removeTestCA(CA_NAME);
        log.debug("Removed test CA");

    }

    @BeforeClass
    public static void  beforeClass() {
        CryptoProviderTools.installBCProvider();
        
    }
    
    /**
     * Add a new user and an expire service. Test that the service expires the
     * users password
     * 
     */
    @Test
    public void testExpireCertificate() throws Exception {

        // Create a new user
        username = genRandomUserName();
        pwd = genRandomPwd();
        userAdminSession.addUser(admin, username, pwd, "C=SE,O=AnaTom,CN=" + username, null, null, false, SecConst.EMPTY_ENDENTITYPROFILE,
                SecConst.CERTPROFILE_FIXED_ENDUSER, SecConst.USER_ENDUSER, SecConst.TOKEN_SOFT_PEM, 0, caid);
        log.debug("created user: " + username);

        KeyPair keys = KeyTools.genKeys("1024", "RSA");
        X509Certificate cert = (X509Certificate) signSession.createCertificate(admin, username, pwd, keys.getPublic());
        certificatesToRemove.add(cert);
 
        assertNotNull("Failed to create certificate", cert);

        String fp = CertTools.getFingerprintAsString(cert);
        X509Certificate ce = (X509Certificate) certificateStoreSession.findCertificateByFingerprint(fp);
        assertNotNull("Cannot find certificate with fp=" + fp, ce);
        CertificateInfo info = certificateStoreSession.getCertificateInfo(fp);
        // log.info("Got certificate info for cert with fp="+fp);
        assertEquals("fingerprint does not match.", fp, info.getFingerprint());
        assertEquals("serialnumber does not match.", cert.getSerialNumber(), info.getSerialNumber());
        assertEquals("issuerdn does not match.", CertTools.getIssuerDN(cert), info.getIssuerDN());
        assertEquals("subjectdn does not match.", CertTools.getSubjectDN(cert), info.getSubjectDN());
        // The cert was just stored above with status INACTIVE
        assertEquals("status does not match.", SecConst.CERT_ACTIVE, info.getStatus());
        long seconds = (cert.getNotAfter().getTime() - new Date().getTime()) / 1000l;
        log.debug("ceritificate OK in store, expires in " + seconds + " seconds");

        // Create a new UserPasswordExpireService
        ServiceConfiguration config = new ServiceConfiguration();
        config.setActive(true);
        config.setDescription("This is a description");
        // No mailsending for this Junit test service
        config.setActionClassPath(NoAction.class.getName());
        config.setActionProperties(null);
        config.setIntervalClassPath(PeriodicalInterval.class.getName());
        Properties intervalprop = new Properties();
        // Run the service every 3:rd second
        intervalprop.setProperty(PeriodicalInterval.PROP_VALUE, "3");
        intervalprop.setProperty(PeriodicalInterval.PROP_UNIT, PeriodicalInterval.UNIT_SECONDS);
        config.setIntervalProperties(intervalprop);
        config.setWorkerClassPath(CertificateExpirationNotifierWorker.class.getName());
        Properties workerprop = new Properties();
        workerprop.setProperty(EmailSendingWorkerConstants.PROP_SENDTOADMINS, "FALSE");
        workerprop.setProperty(EmailSendingWorkerConstants.PROP_SENDTOENDUSERS, "FALSE");
        workerprop.setProperty(BaseWorker.PROP_CAIDSTOCHECK, String.valueOf(caid));
        workerprop.setProperty(BaseWorker.PROP_TIMEBEFOREEXPIRING, String.valueOf(seconds - 10));
        workerprop.setProperty(BaseWorker.PROP_TIMEUNIT, BaseWorker.UNIT_SECONDS);
        config.setWorkerProperties(workerprop);

        if (serviceSession.getService(admin, CERTIFICATE_EXPIRATION_SERVICE) == null) {
            serviceSession.addService(admin, 4711, CERTIFICATE_EXPIRATION_SERVICE, config);
        }
        serviceSession.activateServiceTimer(admin, CERTIFICATE_EXPIRATION_SERVICE);

        // The service will run... the cert should still be active after 5 seconds..
        Thread.sleep(5000);
        info = certificateStoreSession.getCertificateInfo(fp);
        assertEquals("status dotes not match.", SecConst.CERT_ACTIVE, info.getStatus());
  
        // The service will run...We need some tolerance since timers cannot
        // be guaranteed to executed at the exact interval. 
        Thread.sleep(4000);
        int tries = 0;
        while (info.getStatus() != SecConst.CERT_NOTIFIEDABOUTEXPIRATION && tries<5) {
        	Thread.sleep(1000);
        	info = certificateStoreSession.getCertificateInfo(fp);
        	tries++;
        }
        info = certificateStoreSession.getCertificateInfo(fp);
        assertEquals("Status does not match.", SecConst.CERT_NOTIFIEDABOUTEXPIRATION, info.getStatus());
    	log.debug("It took >" + (9+tries) + " seconds before the certificate was expired!");

    }

    public String getRoleName() {
        return this.getClass().getSimpleName(); 
    }

}
