package pkit.cert;

import java.rmi.*;
import javax.rmi.*;

import java.io.*;
import java.util.*;
import java.math.BigInteger;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.X509CRL;
import java.security.*;

import se.anatom.ejbca.ca.auth.UserAuthData;
import se.anatom.ejbca.ca.crl.RevokedCertInfo;
import se.anatom.ejbca.SecConst;
import se.anatom.ejbca.util.CertTools;
import se.anatom.ejbca.util.KeyTools;
import se.anatom.ejbca.util.Hex;
import se.anatom.ejbca.ca.exception.AuthStatusException;
import se.anatom.ejbca.ca.exception.AuthLoginException;
import se.anatom.ejbca.ca.exception.SignRequestException;
import se.anatom.ejbca.ca.exception.SignRequestSignatureException;

import org.bouncycastle.jce.*;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.*;

/**
 * Creates X509 certificates using RSA keys.
 *
 */
public class RSASigner {

    private PrivateKey privateKey;
    private X509Certificate rootCert;
    private X509Certificate caCert;
    X509Name caSubjectName;
    private Long validity;
    private Long crlperiod;
    private boolean usebc, bccritical;
    private boolean useku, kucritical;
    private boolean useski, skicritical;
    private boolean useaki, akicritical;
    private boolean usecrln, crlncritical;
    private boolean usesan, sancritical;
    private boolean usecrldist, crldistcritical;
    String crldisturi;
    private boolean emailindn;
    private boolean finishUser;
    private SecureRandom random;

    
    	    //----------------------------------------

		//Location of CA keystore;
		String keyStorePath;
		
		
		//Password for server keystore, comment out to prompt for pwd.;
		String storePass;
		
		//Alias in keystore for CA private key;
		String privateKeyAlias;
		
		//Password for CA private key, only used for JKS-keystore. Leave as null for PKCS12-keystore, comment out to prompt;
		String privateKeyPassString;
		
		//Validity in days from days date for created certificate;
		//Long validity;
		
		//Use BasicConstraints?;
		boolean basicConstraints;
		
		//BasicConstraints critical? (RFC2459 says YES);
		boolean basicConstraintsCritical;
		
		//Use KeyUsage?;
		boolean keyUsage;
		
		//KeyUsage critical? (RFC2459 says YES);
		boolean keyUsageCritical;
		
		//Use SubjectKeyIdentifier?;
		boolean subjectKeyIdentifier;
		
		//SubjectKeyIdentifier critical? (RFC2459 says NO);
		boolean subjectKeyIdentifierCritical;
		
		//Use AuthorityKeyIdentifier?;
		boolean authorityKeyIdentifier;
		
		//AuthorityKeyIdentifier critical? (RFC2459 says NO);
		boolean authorityKeyIdentifierCritical;
		
		//Use SubjectAlternativeName?;
		boolean subjectAlternativeName;
		
		//SubjectAlternativeName critical? (RFC2459 says NO);
		boolean subjectAlternativeNameCritical;
		
		//Use CRLDistributionPoint?;
		boolean cRLDistributionPoint;
		
		//CRLDistributionPoint critical? (RFC2459 says NO);
		boolean cRLDistributionPointCritical;
		
		//URI of CRLDistributionPoint?;
		String cRLDistURI;
		
		//Use old style altName with email in DN? (RFC2459 says NO);
		boolean emailInDN;
		
		//Use CRLNumber?;
		boolean cRLNumber;
		
		//CRLNumber critical? (RFC2459 says NO);
		boolean cRLNumberCritical;
		
		//Period in hours between CRLs beeing issued;
		Long cRLPeriod;
		
	    
	    //-------------------------------------------
    

    /** A vector of publishers home interfaces where certs and CRLs are stored */
    private Vector publishers = null;


    public RSASigner() {
        try {
            // Install BouncyCastle provider
	    
            Provider BCJce = new org.bouncycastle.jce.provider.BouncyCastleProvider();
            
	    int result = Security.addProvider(BCJce);
	    System.out.println("loaded provided at position " + result);
	    //----------------------------------------

		//Location of CA keystore;
		keyStorePath = "keystore.p12";
		//keyStorePath = "./etc/server.p12";
		
		
		//Password for server keystore, comment out to prompt for pwd.;
		//storePass = "foo123";
		storePass = "password";
		
		//Alias in keystore for CA private key;
		privateKeyAlias = "privateKey";
		
		//Password for CA private key, only used for JKS-keystore. Leave as the string "null" for PKCS12-keystore, comment out to prompt;
		privateKeyPassString = "foo";
		
		//Validity in days from days date for created certificate;
		validity = new Long(730);
		
		//Use BasicConstraints?;
		basicConstraints = true;
		
		//BasicConstraints critical? (RFC2459 says YES);
		basicConstraintsCritical = true;
		
		//Use KeyUsage?;
		keyUsage = true;
		
		//KeyUsage critical? (RFC2459 says YES);
		keyUsageCritical = true;
		
		//Use SubjectKeyIdentifier?;
		subjectKeyIdentifier = true;
		
		//SubjectKeyIdentifier critical? (RFC2459 says NO);
		subjectKeyIdentifierCritical = false;
		
		//Use AuthorityKeyIdentifier?;
		authorityKeyIdentifier = true;
		
		//AuthorityKeyIdentifier critical? (RFC2459 says NO);
		authorityKeyIdentifierCritical = false;
		
		//Use SubjectAlternativeName?;
		subjectAlternativeName = true;
		
		//SubjectAlternativeName critical? (RFC2459 says NO);
		subjectAlternativeNameCritical = false;
		
		//Use CRLDistributionPoint?;
		cRLDistributionPoint = false;
		
		//CRLDistributionPoint critical? (RFC2459 says NO);
		cRLDistributionPointCritical = false;
		
		//URI of CRLDistributionPoint?;
		String cRLDistURI = "http://127.0.0.1:8080/webdist/certdist?cmd=crl";
		
		//Use old style altName with email in DN? (RFC2459 says NO);
		emailInDN = false;
		
		//Use CRLNumber?;
		cRLNumber = true;
		
		//CRLNumber critical? (RFC2459 says NO);
		cRLNumberCritical = false;
		
		//Period in hours between CRLs beeing issued;
		cRLPeriod = new Long(24);

	    
	    //-------------------------------------------
            // Get env variables and read in nessecary data
            KeyStore keyStore=KeyStore.getInstance("PKCS12", "BC");
	    
	    
	    
            InputStream is = new FileInputStream(keyStorePath);
	   System.out.println("keystore:" + keyStorePath);
            char[] keyStorePass = getPassword(storePass);
            keyStore.load(is, keyStorePass);
            System.out.println("privateKeyAlias: " + privateKeyAlias);
            char[] privateKeyPass = getPassword(privateKeyPassString);
           if ((new String(privateKeyPass)).equals("null"))
                privateKeyPass = null;
	    System.out.println("about to get private key from keystore");
            privateKey = (PrivateKey) keyStore.getKey(privateKeyAlias, privateKeyPass);
            if (privateKey == null) {
                System.out.println("Cannot load key with alias '"+privateKeyAlias+"' from keystore '"+keyStorePath+"'");
                throw new Exception("Cannot load key with alias '"+privateKeyAlias+"' from keystore '"+keyStorePath+"'");
            }
            Certificate[] certchain = KeyTools.getCertChain(keyStore, privateKeyAlias);
            if (certchain.length < 1) {
                System.out.println("Cannot load certificate chain with alias '"+privateKeyAlias+"' from keystore '"+keyStorePath+"'");
                throw new Exception("Cannot load certificate chain with alias '"+privateKeyAlias+"' from keystore '"+keyStorePath+"'");
            }
            // We only support a ca hierarchy with depth 2.
            caCert = (X509Certificate)certchain[0];
            System.out.println("cacertIssuer: " + caCert.getIssuerDN().toString());
            System.out.println("cacertSubject: " + caCert.getSubjectDN().toString());

            // We must keep the same order in the DN in the issuer field in created certificates as there
            // is in the subject field of the CA-certificate.
            caSubjectName = new X509Name(caCert.getSubjectDN().toString());
            //caSubjectName = CertTools.stringToBcX509Name(caCert.getSubjectDN().toString());

            // root cert is last cert in chain
            rootCert = (X509Certificate)certchain[certchain.length-1];
            System.out.println("rootcertIssuer: " + rootCert.getIssuerDN().toString());
            System.out.println("rootcertSubject: " + rootCert.getSubjectDN().toString());
            // is root cert selfsigned?
            if (!CertTools.isSelfSigned(rootCert))
                throw new Exception("Root certificate is not self signed!");

            // The validity in days is specified in environment

            // Should extensions be used? Critical or not?
            if ((usebc = basicConstraints) == true)
                bccritical = basicConstraintsCritical;
            if ((useku = keyUsage) == true)
                kucritical = keyUsageCritical;
            if ((useski = subjectKeyIdentifier) == true);
                skicritical = subjectKeyIdentifierCritical;
            if ((useaki = authorityKeyIdentifier) == true);
                akicritical = authorityKeyIdentifierCritical;
            if ((usecrln = cRLNumber) == true);
                crlncritical = cRLNumberCritical;
            if ((usesan = subjectAlternativeName) == true);
                sancritical = subjectAlternativeNameCritical;
            if ((usecrldist = cRLDistributionPoint)== true) {
                crldistcritical = cRLDistributionPointCritical;
                crldisturi = cRLDistURI;
            }
            // Use old style email address in DN? (really deprecated but old habits die hard...)
            emailindn = emailInDN;
            // The period between CRL issue
            crlperiod = cRLPeriod;
            // Should we set user to finished state after generating certificate? Probably means onyl one cert can be issued
            // without resetting users state in user DB
            //finishUser = finishUser;

            // Init random number generator for random serialnumbers
            random = SecureRandom.getInstance("SHA1PRNG");
            // Using this seed we should get a different seed every time.
            // We are not concerned about the security of the random bits, only that they are different every time.
            // Extracting 64 bit random numbers out of this should give us 2^32 (4 294 967 296) serialnumbers before
            // collisions (which are seriously BAD), well anyhow sufficien for pretty large scale installations.
            // Design criteria: 1. No counter to keep track on. 2. Multiple thereads can generate numbers at once, in
            // a clustered environment etc.
            long seed = (new Date().getTime()) + this.hashCode();
            random.setSeed(seed);
            /* Another possibility is to use SecureRandom's default seeding which is designed to be secure:
            * <p>The seed is produced by counting the number of times the VM
            * manages to loop in a given period. This number roughly
            * reflects the machine load at that point in time.
            * The samples are translated using a permutation (s-box)
            * and then XORed together. This process is non linear and
            * should prevent the samples from "averaging out". The s-box
            * was designed to have even statistical distribution; it's specific
            * values are not crucial for the security of the seed.
            * We also create a number of sleeper threads which add entropy
            * to the system by keeping the scheduler busy.
            * Twenty such samples should give us roughly 160 bits of randomness.
            * <P> These values are gathered in the background by a daemon thread
            * thus allowing the system to continue performing it's different
            * activites, which in turn add entropy to the random seed.
            * <p> The class also gathers miscellaneous system information, some
            * machine dependent, some not. This information is then hashed together
            * with the 20 seed bytes. */
        } catch( Exception e ) {
           e.printStackTrace();
        }
    }

    /**
     * Implements ISignSession::getCertificateChain
     */
    public Certificate[] getCertificateChain() throws RemoteException {
        System.out.println(">getCertificateChain()");
        // TODO: should support more than 2 levels of CAs
        Certificate[] chain;
        if (CertTools.isSelfSigned(caCert)) {
            chain = new Certificate[1];
        } else {
            chain = new Certificate[2];
            chain[1] = rootCert;
        }
        chain[0] = caCert;
        System.out.println("<getCertificateChain()");
        return chain;
    } // getRootCertificate

    /**
     * Implements ISignSession::createCertificate
     */
    public Certificate createCertificate(String dn, PublicKey pk) throws Exception{
        System.out.println(">createCertificate(pk)");
        // Standard key usages for end users are: digitalSignature | keyEncipherment or nonRepudiation
        // Default key usage is digitalSignature | keyEncipherment

        // Create an array for KeyUsage acoording to X509Certificate.getKeyUsage()
        boolean[] keyusage = new boolean[9];
        Arrays.fill(keyusage, false);
        // digitalSignature
        keyusage[0] = true;
        // keyEncipherment
        keyusage[2] = true;
        System.out.println("<createCertificate(pk)");
        return createCertificate(dn, pk, keyusage);
    } // createCertificate

    /**
     * Implements ISignSession::createCertificate
     */
    public Certificate createCertificate(String dn, PublicKey pk, boolean[] keyusage) throws Exception {
        return createCertificate(dn, pk, sunKeyUsageToBC(keyusage));
    }

    /**
     * Implements ISignSession::createCertificate
     */
    public Certificate createCertificate(String dn, PublicKey pk, int keyusage)  throws Exception {
        System.out.println(">createCertificate(pk, ku)");
                if ( false) {
                    // If this is a CA, only allow CA-type keyUsage
                    keyusage = X509KeyUsage.keyCertSign + X509KeyUsage.cRLSign;
                }
		System.out.println("dn"+dn);
		System.out.println("caSubjectName"+caSubjectName);
		System.out.println("validity"+validity);
		System.out.println("keyusage"+pk);
		System.out.println("keyusage"+keyusage);
		
		
                X509Certificate cert = makeBCCertificate(dn, caSubjectName, validity.longValue(), pk, keyusage);
                // Verify before returning
                cert.verify(caCert.getPublicKey());
		return cert;

    } // createCertificate

    /**
     * Implements ISignSession::createCertificate
     */
    public Certificate createCertificate(String dn, int certType, PublicKey pk) throws Exception {
        System.out.println(">createCertificate(pk, certType)");
        // Create an array for KeyUsage acoording to X509Certificate.getKeyUsage()
        boolean[] keyusage = new boolean[9];
        Arrays.fill(keyusage, false);
        switch (certType) {
            case CertificateData.CERT_TYPE_ENCRYPTION:
                // keyEncipherment
                keyusage[2] = true;
                // dataEncipherment
                keyusage[3] = true;
                break;
            case CertificateData.CERT_TYPE_SIGNATURE:
                // digitalSignature
                keyusage[0] = true;
                // non-repudiation
                keyusage[1] = true;
                break;
            default:
                // digitalSignature
                keyusage[0] = true;
                // keyEncipherment
                keyusage[2] = true;
                break;
        }

        Certificate ret = createCertificate(dn, pk, keyusage);
        System.out.println("<createCertificate(pk, certType)");
        return ret;
    } // createCertificate

    /**
     * Implements ISignSession::createCertificate
     */
    public Certificate createCertificate(String dn, Certificate incert) throws Exception{
        System.out.println(">createCertificate(cert)");
        X509Certificate cert = (X509Certificate)incert;
        try {
            cert.verify(cert.getPublicKey());
        } catch (Exception e) {
            System.out.println("POPO verification failed for "+ dn);
            throw new Exception("Verification of signature (popo) on certificate failed.");
        }
        // TODO: extract more extensions than just KeyUsage
        Certificate ret = createCertificate(dn, cert.getPublicKey(), cert.getKeyUsage());
        System.out.println("<createCertificate(cert)");
        return ret;
    } // createCertificate

    /**
     * Implements ISignSession::createCertificate
     */
    public Certificate createCertificate(String dn, byte[] pkcs10req) throws Exception {
        return createCertificate( dn, pkcs10req, -1 );
    }

    /**
     * Implements ISignSession::createCertificate
     */
    public Certificate createCertificate(String dn, byte[] pkcs10req, int keyUsage) throws Exception {
        System.out.println(">createCertificate(pkcs10)");
        Certificate ret = null;
        try {
            DERObject derobj = new DERInputStream(new ByteArrayInputStream(pkcs10req)).readObject();
            DERConstructedSequence seq = (DERConstructedSequence)derobj;
            PKCS10CertificationRequest pkcs10 = new PKCS10CertificationRequest(seq);
            if (pkcs10.verify() == false) {
                System.out.println("POPO verification failed for "+dn);
                throw new Exception("Verification of signature (popo) on PKCS10 request failed.");
            }
            // TODO: extract more information or attributes
            if ( keyUsage < 0 )
                ret = createCertificate(dn, pkcs10.getPublicKey());
            else
                ret = createCertificate(dn, pkcs10.getPublicKey(), keyUsage);
        } catch (IOException e) {
            System.out.println("Error reading PKCS10-request.");
            throw new SignRequestException("Error reading PKCS10-request.");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error in PKCS10-request, no such algorithm.");
            throw new SignRequestException("Error in PKCS10-request, no such algorithm.");
        } catch (NoSuchProviderException e) {
            System.out.println("Internal error processing PKCS10-request.");
            throw new SignRequestException("Internal error processing PKCS10-request.");
        } catch (InvalidKeyException e) {
            System.out.println("Error in PKCS10-request, invlid key.");
            throw new SignRequestException("Error in PKCS10-request, invalid key.");
        } catch (SignatureException e) {
            System.out.println("Error in PKCS10-signature.");
            throw new SignRequestSignatureException("Error in PKCS10-signature.");
        }
        System.out.println("<createCertificate(pkcs10)");
        return ret;
    }

    /**
     * Implements ISignSession::createCRL
     */
    public X509CRL createCRL(Vector certs)  throws Exception {
        System.out.println(">createCRL()");
        X509CRL crl = null;
        return crl;
    } // createCRL

    private char[] getPassword(String password) throws Exception {
       if ( password == null ) {
            System.out.println(" password: " + password);
            BufferedReader in
            = new BufferedReader(new InputStreamReader(System.in));
            return (in.readLine()).toCharArray();
        } else
            return password.toCharArray();
    }

    private int sunKeyUsageToBC(boolean[] sku) {

        int bcku = 0;
        if (sku[0] == true)
            bcku = bcku | X509KeyUsage.digitalSignature;
        if (sku[1] == true)
            bcku = bcku | X509KeyUsage.nonRepudiation;
        if (sku[2] == true)
            bcku = bcku | X509KeyUsage.keyEncipherment;
        if (sku[3] == true)
            bcku = bcku | X509KeyUsage.dataEncipherment;
        if (sku[4] == true)
            bcku = bcku | X509KeyUsage.keyAgreement;
        if (sku[5] == true)
            bcku = bcku | X509KeyUsage.keyCertSign;
        if (sku[6] == true)
            bcku = bcku | X509KeyUsage.cRLSign;
        if (sku[7] == true)
            bcku = bcku | X509KeyUsage.encipherOnly;
        if (sku[8] == true)
            bcku = bcku | X509KeyUsage.decipherOnly;
        return bcku;
    }

    private X509Certificate makeBCCertificate(String dn, X509Name caname, 
    long validity, PublicKey publicKey, int keyusage)     
    throws Exception {
	 //start initialising the cert---------------------   
        final String sigAlg="SHA1WithRSA";

        Date firstDate = new Date();
        // Set back startdate ten minutes to avoid some problems with wrongly set clocks.
        firstDate.setTime(firstDate.getTime() - 10*60*1000);
        Date lastDate = new Date();
        // validity in days = validity*24*60*60*1000 milliseconds
        lastDate.setTime(lastDate.getTime() + (validity * 24 * 60 * 60 * 1000));

        X509V3CertificateGenerator certgen = new X509V3CertificateGenerator();
        // Serialnumber is random bits, where random generator is initialized with Date.getTime() when this
        // bean is created.
        byte[] serno = new byte[8];
        random.nextBytes(serno);
        certgen.setSerialNumber((new java.math.BigInteger(serno)).abs());
        certgen.setNotBefore(firstDate);
        certgen.setNotAfter(lastDate);
        certgen.setSignatureAlgorithm(sigAlg);
        // Make DNs
	certgen.setSubjectDN(CertTools.stringToBcX509Name(dn));
        certgen.setIssuerDN(caname);
        certgen.setPublicKey(publicKey);
	//end initialising the cert ------------------------

        // Basic constranits, all subcerts are NOT CAs
        if (usebc == true) {
            boolean isCA = false;
            /*
	    if ( ((subject.getType() & SecConst.USER_CA) == SecConst.USER_CA) || ((subject.getType() & SecConst.USER_ROOTCA) == SecConst.USER_ROOTCA) )
             isCA=true;
	    */
            BasicConstraints bc = new BasicConstraints(isCA);
            certgen.addExtension(X509Extensions.BasicConstraints.getId(), bccritical, bc);
        }
        // Key usage
        if (useku == true) {
            X509KeyUsage ku = new X509KeyUsage(keyusage);
            certgen.addExtension(X509Extensions.KeyUsage.getId(), kucritical, ku);
        }
        // Subject key identifier
        if (useski == true) {
            SubjectPublicKeyInfo spki = new SubjectPublicKeyInfo((DERConstructedSequence)new DERInputStream(
                new ByteArrayInputStream(publicKey.getEncoded())).readObject());
            SubjectKeyIdentifier ski = new SubjectKeyIdentifier(spki);
            certgen.addExtension(X509Extensions.SubjectKeyIdentifier.getId(), skicritical, ski);
        }
        // Authority key identifier
        if (useaki == true) {
            SubjectPublicKeyInfo apki = new SubjectPublicKeyInfo((DERConstructedSequence)new DERInputStream(
                new ByteArrayInputStream(caCert.getPublicKey().getEncoded())).readObject());
            AuthorityKeyIdentifier aki = new AuthorityKeyIdentifier(apki);
            certgen.addExtension(X509Extensions.AuthorityKeyIdentifier.getId(), akicritical, aki);
        }

        /* not sure how to handle alternative names at this stage (justin)
	Subject Alternative name
        if ((usesan == true) && (subject.getEmail() != null)) {
            GeneralName gn = new GeneralName(new DERIA5String(subject.getEmail()),1);
	    
            DERConstructedSequence seq = new DERConstructedSequence();
            seq.addObject(gn);
            GeneralNames san = new GeneralNames(seq);
            certgen.addExtension(X509Extensions.SubjectAlternativeName.getId(), sancritical, san);
        }*/

        // CRL Distribution point URI
        if (usecrldist == true) {
            GeneralName gn = new GeneralName(new DERIA5String(crldisturi),6);
            DERConstructedSequence seq = new DERConstructedSequence();
            seq.addObject(gn);
            GeneralNames gns = new GeneralNames(seq);
            DistributionPointName dpn = new DistributionPointName(0, gns);
            DistributionPoint distp = new DistributionPoint(dpn, null, null);
            DERConstructedSequence ext = new DERConstructedSequence();
            ext.addObject(distp);
            certgen.addExtension(X509Extensions.CRLDistributionPoints.getId(), crldistcritical, ext);
        }

        X509Certificate cert = certgen.generateX509Certificate(privateKey);

        System.out.println("<makeBCCertificate()");
        return (X509Certificate)cert;

    } // makeBCCertificate

    private X509CRL makeBCCRL(X509Name caname, long crlperiod, Vector certs, int crlnumber)
    throws Exception {
        System.out.println(">makeBCCRL()");

        final String sigAlg="SHA1WithRSA";

        Date thisUpdate = new Date();
        Date nextUpdate = new Date();
        // crlperiod is hours = crlperiod*60*60*1000 milliseconds
        nextUpdate.setTime(nextUpdate.getTime() + (crlperiod * 60 * 60 * 1000));

        X509V2CRLGenerator crlgen = new X509V2CRLGenerator();
        crlgen.setThisUpdate(thisUpdate);
        crlgen.setNextUpdate(nextUpdate);
        crlgen.setSignatureAlgorithm(sigAlg);
        // Make DNs
        System.out.println("Issuer="+caname);
        crlgen.setIssuerDN(caname);
        if (certs != null) {
            System.out.println("Number of revoked certificates: "+certs.size());
            Iterator it = certs.iterator();
            while( it.hasNext() ) {
                RevokedCertInfo certinfo = (RevokedCertInfo)it.next();
                crlgen.addCRLEntry(certinfo.getUserCertificate(), certinfo.getRevocationDate(), certinfo.getReason());
            }
        }

        // Authority key identifier
        if (useaki == true) {
            SubjectPublicKeyInfo apki = new SubjectPublicKeyInfo((DERConstructedSequence)new DERInputStream(
                new ByteArrayInputStream(caCert.getPublicKey().getEncoded())).readObject());
            AuthorityKeyIdentifier aki = new AuthorityKeyIdentifier(apki);
            crlgen.addExtension(X509Extensions.AuthorityKeyIdentifier.getId(), akicritical, aki);
        }
        // CRLNumber extension
        if (usecrln == true) {
            CRLNumber crlnum = new CRLNumber(BigInteger.valueOf(crlnumber));
            crlgen.addExtension(X509Extensions.CRLNumber.getId(), crlncritical, crlnum);
        }
        X509CRL crl = crlgen.generateX509CRL(privateKey);

        System.out.println("<makeBCCRL()");
        return (X509CRL)crl;
    } // makeBCCRL

} //RSASigner

