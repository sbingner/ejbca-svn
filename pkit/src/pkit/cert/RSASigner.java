package pkit.cert;

import java.rmi.*;
import javax.rmi.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Properties;
import java.util.Arrays;
import java.util.Vector;
import java.util.Iterator;
import java.util.Date;

import java.math.BigInteger;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.X509CRL;
import java.security.cert.CertificateException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.InvalidKeyException;

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

import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.jce.X509V2CRLGenerator;
import org.bouncycastle.jce.X509V3CertificateGenerator;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CRLNumber;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERInputStream;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERInputStream;
import org.bouncycastle.asn1.ASN1Sequence;

import java.util.Properties;

/**
 *  Creates X509 certificates using RSA keys. <p>
 *
 *  NOTE: This class only caters for signing by two or less level CA certs ie a
 *  root CA cert or sub CA cert signed by a root cert </p>
 *
 *@author     Justin Wood & Tomas Gustavson
 *@created    26 February 2003
 *@todo       Add support for CA cert levels >2 and add option to use PKCS10
 *      attributes to construct certs
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

	/**
	 *  The default name used for the keystore file containing the private key
	 */
	public static String DEFAULT_KEYSTORE_NAME = "keystore";
	/**
	 *  The default alias of the private key
	 */
	public static String DEFAULT_PRIVATE_KEY_ALIAS = "MyKey";

	Properties defaultProperties;
	
	//the type of keysstore to use eg PKCS12 or JKS
	String keyStoreType;
	
	//the provider to use for the keystore
	String keyStoreProvider;
	
	
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


	/**
	 *  Constructor for the RSASigner object sets all fields to the values define
	 *  in the properties file or leaves them at the default if not defined in the
	 *  passed properties
	 *
	 *@param  properties  RSASigner properties to over ride the default properties.
	 */
	public RSASigner(Properties properties) {
		initProperties(properties);
		initClass();
	}


	/**
	 *  Constructor for the RSASigner object sets all fields to their most common
	 *  usage using the passed keystore parameters to retreive the private key,
	 *
	 *@param  keyStorePath     file path the keystore containing the CA's keys
	 *@param  storePass        the password to this keystore
	 *@param  privateKeyAlias  the alias of the key to used for signing
	 *@param  privateKeyPass   The keypass used for the signing key ignored for
	 *      keystores not supporting alias specific passes
	 */
	public RSASigner(String keyStorePath, String storePass, String privateKeyAlias, String privateKeyPass) {
		initDefaults();
		this.keyStorePath = keyStorePath;
		this.storePass = storePass;
		this.privateKeyAlias = privateKeyAlias;
		this.privateKeyPassString = privateKeyPass;
		initClass();
	}


	/**
	 *  Constructor for the RSASigner object sets all fields to their most common
	 *  usage. Initialises the the signer to prompt for passwords via the
	 *  commandline. The name of the keystore and private key alias are drawn from
	 *  the propertie file.
	 */
	public RSASigner() {
		initDefaults();
		initClass();
	}


	/**
	 *  Retrieves the certificate chain for the signer with the root cert and
	 *  position 0 the cert it signs at position 1 and so on
	 *
	 *@return    the certificate chain, never null
	 *@todo      Add support for certificate levels greater than 2.
	 */
	public Certificate[] getCertificateChain() {
		System.out.println(">getCertificateChain()");
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
	}

	// getRootCertificate


	/**
	 *  Returns a certificate created for the passed name and public key with a
	 *  default key usage i.e. digital signture and key encipherment.
	 *
	 *@param  x509Name       The <code>X509Name</code> of the certificate to be
	 *      signed
	 *@param  pk             The public key of the
	 *@return                A signed certicate
	 *@exception  Exception  Description of the Exception
	 */
	public Certificate createCertificate(X509Name x509Name, PublicKey pk) throws SignerException {
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
		return createCertificate(x509Name, pk, keyusage);
	}

	// createCertificate


	/**
	 *  Creates a certificate for the passed name and public key with specified key
	 *  usage using a Sun style boolean array. CAs are only allowed to have
	 *  certificateSign and CRLSign set.
	 *
	 *@param  x509Name       The <code>X509Name</code> of the certificate to be
	 *      signed
	 *@param  pk             The public key of the certificate to be signed.
	 *@param  keyusage       A key usage array in Sun's format where bits are set
	 *      according to id-ce-keyUsage OBJECT IDENTIFIER ::= { id-ce 15 } KeyUsage
	 *      ::= BIT STRING { digitalSignature (0), nonRepudiation (1),
	 *      keyEncipherment (2), dataEncipherment (3), keyAgreement (4),
	 *      keyCertSign (5), cRLSign (6), encipherOnly (7), decipherOnly (8) }
	 *@return                A signed certificate or null
	 *@exception  Exception  Description of the Exception
	 */
	public Certificate createCertificate(X509Name x509Name, PublicKey pk, boolean[] keyusage) throws SignerException {
		return createCertificate(x509Name, pk, sunKeyUsageToBC(keyusage));
	}


	/**
	 *  Returns a certificate created for the passed name and public key with the
	 *  given key usage.
	 *
	 *@param  x509Name       The <code>X509Name</code> of the certificate to be
	 *      signed
	 *@param  pk             The public key of the certificate to be signed.
	 *@param  keyusage       nteger with bit mask describing desired keys usage.
	 *      Bit mask is packed in in integer using constants from CertificateData.
	 *      ex. int keyusage = CertificateData.digitalSignature |
	 *      CertificateData.nonRepudiation; gives digitalSignature and
	 *      nonRepudiation. ex. int keyusage = CertificateData.keyCertSign |
	 *      CertificateData.cRLSign; gives keyCertSign and cRLSign
	 *@return                A signed certificate or null
	 *@exception  Exception  Description of the Exception
	 */
	public Certificate createCertificate(X509Name x509Name, PublicKey pk, int keyusage) throws SignerException {
		System.out.println(">createCertificate(pk, ku)");
		if (false) {
			// If this is a CA, only allow CA-type keyUsage
			keyusage = X509KeyUsage.keyCertSign + X509KeyUsage.cRLSign;
		}
		System.out.println("x509Name" + x509Name);
		System.out.println("caSubjectName" + caSubjectName);
		System.out.println("validity" + validity);
		System.out.println("keyusage" + pk);
		System.out.println("keyusage" + keyusage);

		X509Certificate cert = makeBCCertificate(x509Name, caSubjectName, validity.longValue(), pk, keyusage);
		// Verify before returning
		try {
			cert.verify(caCert.getPublicKey());
		} catch (CertificateException e) {
			throw new SignerException("Error verifying created cert." + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			throw new SignerException("Error verifying created cert." + e.getMessage());
		} catch (NoSuchProviderException e) {
			throw new SignerException("Error verifying created cert." + e.getMessage());
		} catch (InvalidKeyException e) {
			throw new SignerException("Error verifying created cert." + e.getMessage());
		} catch (SignatureException e) {
			throw new SignerException("Error verifying created cert." + e.getMessage());
		}
		
		return cert;
	}
	// createCertificate


	/**
	 *  Returns a certificate created for the passed name and public key with the
	 *  given cert type
	 *
	 *@param  x509Name       The <code>X509Name</code> of the certificate to be
	 *      signed
	 *@param  certType       integer type of certificate taken from
	 *      CertificateData.CERT_TYPE_XXX. the type
	 *      CertificateData.CERT_TYPE_ENCRYPTION gives keyUsage keyEncipherment,
	 *      dataEncipherment. the type CertificateData.CERT_TYPE_SIGNATURE gives
	 *      keyUsage digitalSignature, non-repudiation. all other CERT_TYPES gives
	 *      the default keyUsage digitalSignature, keyEncipherment
	 *@param  pk             The public key of the certificate to be signed.
	 *@return                A signed certificate or null
	 *@exception  Exception  Description of the Exception
	 */
	public Certificate createCertificate(X509Name x509Name, int certType, PublicKey pk) throws SignerException {
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

		Certificate ret = createCertificate(x509Name, pk, keyusage);
		System.out.println("<createCertificate(pk, certType)");
		return ret;
	}
	// createCertificate


	/**
	 *  Creates a cert using the public key from a self signed cert and teh given
	 *  name
	 *
	 *@param  x509Name       The <code>X509Name</code> of the certificate to be
	 *      signed
	 *@param  incert         An existing certificate
	 *@return                A signed certificate or null
	 *@exception  Exception  Description of the Exception
	 *@todo                  extract more extensions than just KeyUsage
	 */
	public Certificate createCertificate(X509Name x509Name, Certificate incert) throws SignerException {
		System.out.println(">createCertificate(cert)");
		X509Certificate cert = (X509Certificate) incert;
		try {
			cert.verify(cert.getPublicKey());
		} catch (Exception e) {
			System.out.println("POPO verification failed for " + x509Name);
			throw new SignerException("Verification of signature (popo) on certificate failed.");
		}
		Certificate ret = createCertificate(x509Name, cert.getPublicKey(), cert.getKeyUsage());
		System.out.println("<createCertificate(cert)");
		return ret;
	}
	// createCertificate


	/**
	 *  Creates a cert using the given name and pkcs10 request
	 *
	 *@param  x509Name       The <code>X509Name</code> of the certificate to be
	 *      signed
	 *@param  pkcs10req      a pkcs10 request as DER encoded bytes
	 *@return                A signed certificate or null
	 *@exception  Exception  Description of the Exception
	 */
	public Certificate createCertificate(X509Name x509Name, byte[] pkcs10req) throws SignerException {
		return createCertificate(x509Name, pkcs10req, -1);
	}


	/**
	 *  Implements ISignSession::createCertificate
	 *
	 *@param  x509Name       The <code>X509Name</code> of the certificate to be
	 *      signed
	 *@param  pkcs10req      a pkcs10 request as DER encoded bytes
	 *@param  keyUsage       Description of the Parameter
	 *@return                A signed certificate or null
	 *@exception  Exception  Description of the Exception
	 */
	public Certificate createCertificate(X509Name x509Name, byte[] pkcs10req, int keyUsage) throws SignerException {
		System.out.println(">createCertificate(pkcs10)");
		Certificate ret = null;
		try {
			DERObject derobj = new DERInputStream(new ByteArrayInputStream(pkcs10req)).readObject();
			ASN1Sequence seq = (ASN1Sequence) derobj;
			PKCS10CertificationRequest pkcs10 = new PKCS10CertificationRequest(seq);
			if (pkcs10.verify() == false) {
				System.out.println("POPO verification failed for " + x509Name);
				throw new SignerException("Verification of signature (popo) on PKCS10 request failed.");
			}
			// TODO: extract more information or attributes
			if (keyUsage < 0) {
				ret = createCertificate(x509Name, pkcs10.getPublicKey());
			} else {
				ret = createCertificate(x509Name, pkcs10.getPublicKey(), keyUsage);
			}
		} catch (IOException e) {
			throw new SignerException("Error reading PKCS10-request.");
		} catch (NoSuchAlgorithmException e) {
			throw new SignerException("Error in PKCS10-request, no such algorithm.");
		} catch (NoSuchProviderException e) {
			throw new SignerException("Internal error processing PKCS10-request.");
		} catch (InvalidKeyException e) {
			throw new SignerException("Error in PKCS10-request, invalid key.");
		} catch (SignatureException e) {
			throw new SignerException("Error in PKCS10-signature.");
		}
		System.out.println("<createCertificate(pkcs10)");
		return ret;
	}


	/**
	 *  Implements ISignSession::createCRL
	 *
	 *@param  certs          Description of the Parameter
	 *@return                Description of the Return Value
	 *@exception  Exception  Description of the Exception
	 */
	public X509CRL createCRL(long crlperiod, Vector certs, int crlnumber) throws SignerException{
		System.out.println(">createCRL()");
		return makeBCCRL(crlperiod,certs,crlnumber) ;
	}


	/**
	 *  Gets the password from the user.
	 *
	 *@param  password       A password string
	 *@param  prompt         The text shown to the user to input a password
	 *@return                The password as a char array
	 *@exception  Exception  Description of the Exception
	 *@todo                  replace with a callback inplementaion JAAS or similar
	 */
	private char[] getPassword(String password, String prompt) throws Exception {
		if (password == null) {
			System.out.println(prompt);
			BufferedReader in
					 = new BufferedReader(new InputStreamReader(System.in));
			return (in.readLine()).toCharArray();
		} else {
			return password.toCharArray();
		}
	}


	/**
	 *  Converts a Sun key usage boolean array to a Bouncy Castle key usage integer
	 *
	 *@param  sku  Sun key usage style boolean array.
	 *@return      An bouncy castle style key usage integer
	 */
	private int sunKeyUsageToBC(boolean[] sku) {

		int bcku = 0;
		if (sku[0] == true) {
			bcku = bcku | X509KeyUsage.digitalSignature;
		}
		if (sku[1] == true) {
			bcku = bcku | X509KeyUsage.nonRepudiation;
		}
		if (sku[2] == true) {
			bcku = bcku | X509KeyUsage.keyEncipherment;
		}
		if (sku[3] == true) {
			bcku = bcku | X509KeyUsage.dataEncipherment;
		}
		if (sku[4] == true) {
			bcku = bcku | X509KeyUsage.keyAgreement;
		}
		if (sku[5] == true) {
			bcku = bcku | X509KeyUsage.keyCertSign;
		}
		if (sku[6] == true) {
			bcku = bcku | X509KeyUsage.cRLSign;
		}
		if (sku[7] == true) {
			bcku = bcku | X509KeyUsage.encipherOnly;
		}
		if (sku[8] == true) {
			bcku = bcku | X509KeyUsage.decipherOnly;
		}
		return bcku;
	}


	/**
	 *  Creates the certificate
	 *
	 *@param  x509Name       The <code>X509Name</code> of the certificate to be
	 *      signed
	 *@param  caname         x509Name for the CA
	 *@param  validity       number of days for which the cert is valid
	 *@param  publicKey      the public key of the certificate to be signed
	 *@param  keyusage       integer with bit mask describing desired keys usage.
	 *      Bit mask is packed in in integer using contants from CertificateData.
	 *      ex. int keyusage = CertificateData.digitalSignature |
	 *      CertificateData.nonRepudiation; gives digitalSignature and
	 *      nonRepudiation. ex. int keyusage = CertificateData.keyCertSign |
	 *      CertificateData.cRLSign; gives keyCertSign and cRLSign
	 *@return                A signed certificate or null
	 *@exception  Exception  Description of the Exception
	 */
	private X509Certificate makeBCCertificate(X509Name x509Name, X509Name caname,
			long validity, PublicKey publicKey, int keyusage) throws SignerException  {
		X509Certificate cert = null;
		//start initialising the cert---------------------
		final String sigAlg = "SHA1WithRSA";
		try {
			Date firstDate = new Date();
			// Set back startdate ten minutes to avoid some problems with wrongly set clocks.
			firstDate.setTime(firstDate.getTime() - 10 * 60 * 1000);
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
			certgen.setSubjectDN(x509Name);
			certgen.setIssuerDN(caname);
			certgen.setPublicKey(publicKey);
			//end initialising the cert ------------------------
	
			// Basic constranits, all subcerts are NOT CAs
			if (usebc == true) {
				boolean isCA = false;
				/*
				 *  if ( ((subject.getType() & SecConst.USER_CA) == SecConst.USER_CA) || ((subject.getType() & SecConst.USER_ROOTCA) == SecConst.USER_ROOTCA) )
				 *  isCA=true;
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
				SubjectPublicKeyInfo spki = new SubjectPublicKeyInfo(
					(DERSequence) new DERInputStream(new ByteArrayInputStream(publicKey.getEncoded())).readObject());
				SubjectKeyIdentifier ski = new SubjectKeyIdentifier(spki);
				certgen.addExtension(X509Extensions.SubjectKeyIdentifier.getId(), skicritical, ski);
			}
			// Authority key identifier
			if (useaki == true) {
				SubjectPublicKeyInfo apki = new SubjectPublicKeyInfo(
					(DERSequence) new DERInputStream(new ByteArrayInputStream(caCert.getPublicKey().getEncoded())).readObject()
				);
				AuthorityKeyIdentifier aki = new AuthorityKeyIdentifier(apki);
				certgen.addExtension(X509Extensions.AuthorityKeyIdentifier.getId(), akicritical, aki);
			}
	
			/*
			 *  not sure how to handle alternative names at this stage (justin)
			 *  Subject Alternative name
			 *  if ((usesan == true) && (subject.getEmail() != null)) {
			 *  GeneralName gn = new GeneralName(new DERIA5String(subject.getEmail()),1);
			 *  DERConstructedSequence seq = new DERConstructedSequence();
			 *  seq.addObject(gn);
			 *  GeneralNames san = new GeneralNames(seq);
			 *  certgen.addExtension(X509Extensions.SubjectAlternativeName.getId(), sancritical, san);
			 *  }
			 */
			// CRL Distribution point URI
			if (usecrldist) {
			    //GeneralName name = new GeneralName(new DERIA5String(cRLDistributionPoint), 6);
			    GeneralName name = new GeneralName(new DERIA5String(cRLDistURI),6);
			    GeneralNames names = new GeneralNames(new DERSequence(name));
			    DistributionPointName distPointName = new DistributionPointName(0, names);
			    DistributionPoint distPoint = new DistributionPoint(distPointName, null, null);
			    certgen.addExtension(
				X509Extensions.CRLDistributionPoints.getId(),
				crldistcritical,
				new DERSequence(distPoint));
			}
			cert = certgen.generateX509Certificate(privateKey);
			
		} catch (SecurityException e) {
			throw new SignerException("There was a problem creating the cert " + e.getMessage());
		} catch (SignatureException e) {
			throw new SignerException("There was a problem creating the cert" + e.getMessage());
		} catch (InvalidKeyException e) {
			throw new SignerException("There was a problem creating the cert" + e.getMessage());
		} catch (IOException e) {
			throw new SignerException("There was a problem reading the public key from the cert. " + e.getMessage());
		}

		System.out.println("<makeBCCertificate()");
		return (X509Certificate) cert;
	}

	
	// makeBCCertificate

	/**
	 *  Requests for a CRL to be created with the passed (revoked) certificates.
	 *
	 *@param  crlperiod      period in days
	 *@param  certs          collection of RevokedCertInfo object.
	 *@param  crlnumber      The CRL number
	 *@return                A signed CRL or null
	 *@exception  Exception  Description of the Exception
	 */
	private X509CRL makeBCCRL(long crlperiod, Vector certs, int crlnumber) throws SignerException {
		System.out.println(">makeBCCRL()");
		System.out.println("Issued by: " + caCert.getIssuerDN());
		X509Name caname =new X509Name(caCert.getIssuerDN().toString()); 
		
		X509CRL crl =null;
		
		final String sigAlg = "SHA1WithRSA";
		try {
			Date thisUpdate = new Date();
			Date nextUpdate = new Date();
			// crlperiod is hours = crlperiod*60*60*1000 milliseconds
			nextUpdate.setTime(nextUpdate.getTime() + (crlperiod * 60 * 60 * 1000));
	
			X509V2CRLGenerator crlgen = new X509V2CRLGenerator();
			crlgen.setThisUpdate(thisUpdate);
			crlgen.setNextUpdate(nextUpdate);
			crlgen.setSignatureAlgorithm(sigAlg);
			// Make DNs
			System.out.println("Issuer=" + caname);
			crlgen.setIssuerDN(caname);
			if (certs != null) {
				System.out.println("Number of revoked certificates: " + certs.size());
				Iterator it = certs.iterator();
				while (it.hasNext()) {
					RevokedCertInfo certinfo = (RevokedCertInfo) it.next();
					crlgen.addCRLEntry(certinfo.getUserCertificate(), certinfo.getRevocationDate(), certinfo.getReason());
				}
			}
	
			// Authority key identifier
			if (useaki == true) {
				SubjectPublicKeyInfo apki = new SubjectPublicKeyInfo((DERSequence) new DERInputStream(
					new ByteArrayInputStream(caCert.getPublicKey().getEncoded())).readObject());
				AuthorityKeyIdentifier aki = new AuthorityKeyIdentifier(apki);
				crlgen.addExtension(X509Extensions.AuthorityKeyIdentifier.getId(), akicritical, aki);
			}
			// CRLNumber extension
			if (usecrln == true) {
				CRLNumber crlnum = new CRLNumber(BigInteger.valueOf(crlnumber));
				crlgen.addExtension(X509Extensions.CRLNumber.getId(), crlncritical, crlnum);
			}
			crl = crlgen.generateX509CRL(privateKey); 
		} catch (SecurityException e) {
			throw new SignerException("There was a problem generating the CRL" + e.getMessage());
		} catch (SignatureException e) {
			throw new SignerException("There was a problem generating the CRL" + e.getMessage());
		} catch (InvalidKeyException e) {
			throw new SignerException("There was a problem generating the CRL" + e.getMessage());
		} catch (IOException e) {
			throw new SignerException("There was a problem getting the public cert from the signers certficate " + e.getMessage());
		}

		System.out.println("<makeBCCRL()");
		return (X509CRL) crl;
	}
	// makeBCCRL


	/**
	 *  Initialises the class variables according to the defaults or properties
	 */
	private void initClass() {
		try {
			// Install BouncyCastle provider

			Provider BCJce = new org.bouncycastle.jce.provider.BouncyCastleProvider();

			int result = Security.addProvider(BCJce);
			System.out.println("loaded provider at position " + result);

			// Get env variables and read in nessecary data
			KeyStore keyStore = KeyStore.getInstance(keyStoreType,keyStoreProvider);

			InputStream is = new FileInputStream(keyStorePath);
			System.out.println("keystore:" + keyStorePath);
			char[] keyStorePass = getPassword(storePass, "Please enter your keystore password: ");
			keyStore.load(is, keyStorePass);
			System.out.println("privateKeyAlias: " + privateKeyAlias);
			char[] privateKeyPass = getPassword(privateKeyPassString, "Please enter your private key password (may be blank depending on your keystore implementation)");
			if ((new String(privateKeyPass)).equals("null")) {
				privateKeyPass = null;
			}
			System.out.println("about to get private key from keystore");
			privateKey = (PrivateKey) keyStore.getKey(privateKeyAlias, privateKeyPass);
			if (privateKey == null) {
				System.out.println("Cannot load key with alias '" + privateKeyAlias + "' from keystore '" + keyStorePath + "'");
				throw new Exception("Cannot load key with alias '" + privateKeyAlias + "' from keystore '" + keyStorePath + "'");
			}
			Certificate[] certchain = KeyTools.getCertChain(keyStore, privateKeyAlias);
			if (certchain.length < 1) {
				System.out.println("Cannot load certificate chain with alias '" + privateKeyAlias + "' from keystore '" + keyStorePath + "'");
				throw new Exception("Cannot load certificate chain with alias '" + privateKeyAlias + "' from keystore '" + keyStorePath + "'");
			}
			// We only support a ca hierarchy with depth 2.
			caCert = (X509Certificate) certchain[0];
			System.out.println("cacertIssuer: " + caCert.getIssuerDN().toString());
			System.out.println("cacertSubject: " + caCert.getSubjectDN().toString());

			// We must keep the same order in the DN in the issuer field in created certificates as there
			// is in the subject field of the CA-certificate.
			caSubjectName = new X509Name(caCert.getSubjectDN().toString());
			//caSubjectName = CertTools.stringToBcX509Name(caCert.getSubjectDN().toString());

			// root cert is last cert in chain
			rootCert = (X509Certificate) certchain[certchain.length - 1];
			System.out.println("rootcertIssuer: " + rootCert.getIssuerDN().toString());
			System.out.println("rootcertSubject: " + rootCert.getSubjectDN().toString());
			// is root cert selfsigned?
			if (!CertTools.isSelfSigned(rootCert)) {
				throw new Exception("Root certificate is not self signed!");
			}

			// The validity in days is specified in environment

			// Should extensions be used? Critical or not?
			if ((usebc = basicConstraints) == true) {
				bccritical = basicConstraintsCritical;
			}
			if ((useku = keyUsage) == true) {
				kucritical = keyUsageCritical;
			}
			if ((useski = subjectKeyIdentifier) == true) {
				;
			}
			skicritical = subjectKeyIdentifierCritical;
			if ((useaki = authorityKeyIdentifier) == true) {
				;
			}
			akicritical = authorityKeyIdentifierCritical;
			if ((usecrln = cRLNumber) == true) {
				;
			}
			crlncritical = cRLNumberCritical;
			if ((usesan = subjectAlternativeName) == true) {
				;
			}
			sancritical = subjectAlternativeNameCritical;
			if ((usecrldist = cRLDistributionPoint) == true) {
				crldistcritical = cRLDistributionPointCritical;
				crldisturi = cRLDistURI;
			}
			// Use old style email address in DN? (really deprecated but old habits die hard...)
			emailindn = emailInDN;
			// The period between CRL issue
			crlperiod = cRLPeriod;

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
			/*
			 *  Another possibility is to use SecureRandom's default seeding which is designed to be secure:
			 *  <p>The seed is produced by counting the number of times the VM
			 *  manages to loop in a given period. This number roughly
			 *  reflects the machine load at that point in time.
			 *  The samples are translated using a permutation (s-box)
			 *  and then XORed together. This process is non linear and
			 *  should prevent the samples from "averaging out". The s-box
			 *  was designed to have even statistical distribution; it's specific
			 *  values are not crucial for the security of the seed.
			 *  We also create a number of sleeper threads which add entropy
			 *  to the system by keeping the scheduler busy.
			 *  Twenty such samples should give us roughly 160 bits of randomness.
			 *  <P> These values are gathered in the background by a daemon thread
			 *  thus allowing the system to continue performing it's different
			 *  activites, which in turn add entropy to the random seed.
			 *  <p> The class also gathers miscellaneous system information, some
			 *  machine dependent, some not. This information is then hashed together
			 *  with the 20 seed bytes.
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 *  Initialises the class variables according to the provided props
	 *
	 *@param  props  Properties to set the class variables to.
	 */
	private void initProperties(Properties props) {
		initDefaults();
		//now run through and set all the properties
		keyStoreType=props.getProperty("keyStoreType");
		keyStoreProvider=props.getProperty("keyStoreProvider");
		keyStorePath = props.getProperty("keyStorePath");
		System.out.println(keyStorePath);
		//NO DEFAULT if this is null the user will be prompted for it later
		storePass = props.getProperty("storePass");
		privateKeyAlias = props.getProperty("privateKeyAlias");
		//NO DEFAULT if this is null the user will be prompted later
		privateKeyPassString = props.getProperty("privateKeyPassString");
		validity = new Long(props.getProperty("validity"));
		basicConstraints = new Boolean(props.getProperty("validity")).booleanValue();
		basicConstraintsCritical = new Boolean(props.getProperty("validity")).booleanValue();
		keyUsage = new Boolean(props.getProperty("validity")).booleanValue();
		keyUsageCritical = new Boolean(props.getProperty("validity")).booleanValue();
		subjectKeyIdentifier = new Boolean(props.getProperty("validity")).booleanValue();
		subjectKeyIdentifierCritical = new Boolean(props.getProperty("validity")).booleanValue();
		authorityKeyIdentifier = new Boolean(props.getProperty("validity")).booleanValue();
		authorityKeyIdentifierCritical = new Boolean(props.getProperty("validity")).booleanValue();
		subjectAlternativeName = new Boolean(props.getProperty("validity")).booleanValue();
		subjectAlternativeNameCritical = new Boolean(props.getProperty("validity")).booleanValue();
		cRLDistributionPoint = new Boolean(props.getProperty("validity")).booleanValue();
		cRLDistributionPointCritical = new Boolean(props.getProperty("validity")).booleanValue();
		String cRLDistURI = props.getProperty("cRLDistURI");
		emailInDN = new Boolean(props.getProperty("validity")).booleanValue();
		cRLNumber = new Boolean(props.getProperty("validity")).booleanValue();
		cRLNumberCritical = new Boolean(props.getProperty("validity")).booleanValue();
		cRLPeriod = Long.valueOf(props.getProperty("validity"));
	}


	/**
	 *  Sets all the class variables to default values and assigns default
	 *  properties
	 */
	private void initDefaults() {
		defaultProperties = new Properties();
		//the passwords are not assigned here by default they stay null
		
		keyStoreType="PKCS12";
		defaultProperties.setProperty("keyStoreType",keyStoreType);
		keyStoreProvider="BC";
		defaultProperties.setProperty("keyStoreProvider",keyStoreProvider);
		//file path to the keystore
		keyStorePath = DEFAULT_KEYSTORE_NAME;
		defaultProperties.setProperty("keyStorePath", keyStorePath);
		//the alias given to the private key in the keystore
		privateKeyAlias = DEFAULT_PRIVATE_KEY_ALIAS;
		defaultProperties.setProperty("privateKeyAlias", privateKeyAlias);
		//Validity in days from days date for created certificate;
		validity = new Long(730);
		defaultProperties.setProperty("validity", validity.toString());
		//Use BasicConstraints?;
		basicConstraints = true;
		defaultProperties.setProperty("basicConstraints", Boolean.toString(basicConstraints));
		//BasicConstraints critical? (RFC2459 says YES);
		basicConstraintsCritical = true;
		defaultProperties.setProperty("basicConstraintsCritical", Boolean.toString(basicConstraintsCritical));
		//Use KeyUsage?;
		keyUsage = true;
		defaultProperties.setProperty("keyUsage", Boolean.toString(keyUsage));
		//KeyUsage critical? (RFC2459 says YES);
		keyUsageCritical = true;
		defaultProperties.setProperty("keyUsageCritical", Boolean.toString(keyUsageCritical));
		//Use SubjectKeyIdentifier?;
		subjectKeyIdentifier = true;
		defaultProperties.setProperty("subjectKeyIdentifier", Boolean.toString(subjectKeyIdentifier));
		//SubjectKeyIdentifier critical? (RFC2459 says NO);
		subjectKeyIdentifierCritical = false;
		defaultProperties.setProperty("subjectKeyIdentifierCritical", Boolean.toString(subjectKeyIdentifierCritical));
		//Use AuthorityKeyIdentifier?;
		authorityKeyIdentifier = true;
		defaultProperties.setProperty("authorityKeyIdentifier", Boolean.toString(authorityKeyIdentifier));
		//AuthorityKeyIdentifier critical? (RFC2459 says NO);
		authorityKeyIdentifierCritical = false;
		defaultProperties.setProperty("authorityKeyIdentifierCritical", Boolean.toString(authorityKeyIdentifierCritical));
		//Use SubjectAlternativeName?;
		subjectAlternativeName = true;
		defaultProperties.setProperty("subjectAlternativeName", Boolean.toString(subjectAlternativeName));
		//SubjectAlternativeName critical? (RFC2459 says NO);
		subjectAlternativeNameCritical = false;
		defaultProperties.setProperty("subjectAlternativeNameCritical", Boolean.toString(subjectAlternativeNameCritical));
		//Use CRLDistributionPoint?;
		cRLDistributionPoint = false;
		defaultProperties.setProperty("cRLDistributionPoint", Boolean.toString(cRLDistributionPoint));
		//CRLDistributionPoint critical? (RFC2459 says NO);
		cRLDistributionPointCritical = false;
		defaultProperties.setProperty("cRLDistributionPointCritical", Boolean.toString(cRLDistributionPointCritical));
		//URI of CRLDistributionPoint?;
		String cRLDistURI = "http://127.0.0.1:8080/webdist/certdist?cmd=crl";
		defaultProperties.setProperty("cRLDistURI", cRLDistURI);
		//Use old style altName with email in DN? (RFC2459 says NO);
		emailInDN = false;
		defaultProperties.setProperty("emailInDN", Boolean.toString(emailInDN));
		//Use CRLNumber?;
		cRLNumber = true;
		defaultProperties.setProperty("cRLNumber", Boolean.toString(cRLNumber));
		//CRLNumber critical? (RFC2459 says NO);
		cRLNumberCritical = false;
		defaultProperties.setProperty("cRLNumberCritical", Boolean.toString(cRLNumberCritical));
		//Period in hours between CRLs beeing issued;
		cRLPeriod = new Long(24);
		defaultProperties.setProperty("cRLPeriod", cRLPeriod.toString());
	}
}//RSASigner

