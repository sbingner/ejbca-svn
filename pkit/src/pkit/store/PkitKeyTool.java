package pkit.store;

import java.security.Principal;

import java.io.*;
import java.security.spec.*;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.security.interfaces.RSAPublicKey;
import java.security.SignatureException;
import java.security.InvalidKeyException;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.security.cert.CertificateFactory;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.pkcs.*;
import org.bouncycastle.jce.interfaces.*;
import org.bouncycastle.jce.provider.*;
import org.bouncycastle.asn1.pkcs.*;
import org.bouncycastle.jce.PKCS10CertificationRequest;

import org.apache.log4j.*;

import se.anatom.ejbca.util.Base64;
import se.anatom.ejbca.util.Hex;
import se.anatom.ejbca.util.CertTools;
import se.anatom.ejbca.util.KeyTools;
import se.anatom.ejbca.util.FileTools;
import java.security.UnrecoverableKeyException;

/**
 *  Useful methods for handling keyStore tasks
 *
 *@author     justin
 *@created    June 13, 2003
 */
public class PkitKeyTool {

	/**
	 *  Most commonly used header for a CSR in PEM format
	 */
	public static String PEM_CSR_HEADER_TYPE1 = "-----BEGIN CERTIFICATE REQUEST-----";
	/**
	 *  Most commonly used footer for a CSR in PEM format
	 */
	public static String PEM_CSR_FOOTER_TYPE1 = "-----END CERTIFICATE REQUEST-----";

	/**
	 *  Another header for a CSR in PEM format
	 */
	public static String PEM_CSR_HEADER_TYPE2 = "-----BEGIN NEW CERTIFICATE REQUEST-----";
	/**
	 *  Another footer for a CSR in PEM format
	 */
	public static String PEM_CSR_FOOTER_TYPE2 = "-----END NEW CERTIFICATE REQUEST-----";


	/**
	 *  Constructor for the PkitKeyTool object
	 */
	protected PkitKeyTool() { }


	/**
	 *  Initialises the default keystore type (PKCS12) and provider (BC)
	 */
	public static void init() {
		System.setProperty("pkit.keystore.type", "PKCS12");
		System.setProperty("pkit.keystore.provider", "BC");
	}


	/**
	 *  Initialises the default keystore type and provider as defined in the provided properties.
	 *
	 *@param  properties  Description of the Parameter
	 */
	public static void init(Properties properties) {
		System.setProperties(properties);
	}


	/**
	 *  Accepts a certicate signed in reponse to a certificate signing request. For
	 *  now just checks that the issuer DN of the cert to be signed is the same as
	 *  the subject DN of the signers public cert by comparison of the X509Name.
	 *  See the todo.
	 *
	 *@param  certificateFile  The file containing the DER encoded certificate
	 *      signed by the CA
	 *@param  keyStoreFile     The keyStore file that you would like to import the
	 *      response to
	 *@param  storePassword    The password to the keyStore
	 *@param  alias            The alias given to the certificate entry for which
	 *      you are receiving a signed cert
	 *@param  trustedCACert    Description of the Parameter
	 *@exception  Exception    If the cert was issued by a CA with a different DN
	 *      than the one that signed the trustedCACert
	 *@todo                    use authority key identifier to verify the cert
	 *      signer also signed the cAPublicCert and maybe get the CA's public certs
	 *      from the cA root cert from the trust store
	 */
	public static void acceptRequestReply(String certificateFile, String trustedCACert, String keyStoreFile, char[] storePassword, String alias) throws Exception {
		X509Certificate cert = CertTools.getCertfromByteArray(FileTools.readFiletoBuffer(certificateFile));
		X509Certificate caCert = CertTools.getCertfromByteArray(FileTools.readFiletoBuffer(trustedCACert));
		X509Certificate rootcert = null;

		KeyStore store = getKeyStore(keyStoreFile, storePassword);
		Certificate[] certchain = store.getCertificateChain(alias);

		//check if the signed certs issuer matches the trusted ca cert subject
		System.out.println("Loaded certificate chain with length " + certchain.length + " with alias '" + alias + "'.");
		String issuerDN = cert.getIssuerDN().toString();
		String caCertDN = caCert.getSubjectDN().toString();
		X509Name issuerX509 = CertTools.stringToBcX509Name(issuerDN);
		X509Name caX509 = CertTools.stringToBcX509Name(caCertDN);
		System.out.println("Received cert issuer DN: " + issuerDN + " trusted CA cert subject DN: '" + caCertDN + "'.");
		if (!issuerX509.equals(caX509)) {
			throw new Exception("Trusted ca public cert DN cert does not match the issuers DN on the signed cert ");
		}

		PrivateKey privKey = (PrivateKey) store.getKey(alias, storePassword);

		// check if the private and public keys match
		Signature sign = Signature.getInstance("SHA1WithRSA");
		sign.initSign(privKey);
		sign.update("foooooooooooooooo".getBytes());
		byte[] signature = sign.sign();
		sign.initVerify(cert.getPublicKey());
		sign.update("foooooooooooooooo".getBytes());
		if (sign.verify(signature) == false) {
			throw new Exception("Public key in received certificate does not match private key.");
		}

		// Create new keyStore
		KeyStore ks = createKeyStore("privateKey", storePassword, privKey, cert, caCert);
		FileOutputStream os = new FileOutputStream(keyStoreFile);
		ks.store(os, storePassword);
		System.out.println("Keystore '" + keyStoreFile + "' generated successfully.");
	}


	/**
	 *  Creates a keyStore with self signed certificate
	 *
	 *@param  dn             The DN for the certificate
	 *@param  keysize        The size in bits of the key
	 *@param  validity       The period in days for which the certificate will be
	 *      valid
	 *@param  filename       The filename to be used for the keyStore
	 *@param  storepwd       the password to be used for the keyStore
	 *@param  isCA           whether or no this cert will be used to certifiy other
	 *      certs
	 *@param  alias          Description of the Parameter
	 *@exception  Exception
	 */
	public static void createKeystoreWithSelfCert(String dn, String alias, int keysize, int validity, String filename, char[] storepwd, boolean isCA) throws Exception {
		System.out.println("DN: " + dn);
		System.out.println("Keysize: " + keysize);
		System.out.println("Validity (days): " + validity);
		System.out.println("Storing in: " + filename);
		System.out.println("Protected with storepassword: " + storepwd);
		System.out.println("Generating keys, please wait...");
		KeyPair rsaKeys = KeyTools.genKeys(keysize);
		X509Certificate cert = CertTools.genSelfCert(dn, (long) validity, rsaKeys.getPrivate(), rsaKeys.getPublic(), isCA);
		KeyStore ks = createKeyStore(alias, storepwd, rsaKeys.getPrivate(), cert, (X509Certificate) null);
		FileOutputStream os = new FileOutputStream(filename);
		System.out.println("Storing keyStore '" + filename + "'.");
		ks.store(os, storepwd);
		System.out.println("Keystore " + filename + " generated successfully.");
	}


	/**
	 *  Exports a certificate to a file
	 *
	 *@param  keyStoreFile   The keystore that contains the certiifcate
	 *@param  password       The password for the keystore (assumes password for key is same as keystore)
	 *@param  alias          The alias associated with the certificate
	 *@param  certFileName   The name of the file to which the cert will be written
	 *@exception  Exception  if an IO error occurs of if there is some trouble accessing the cert
	 */
	public static void exportCertificate(String keyStoreFile, char[] password, String alias, String certFileName) throws Exception {
		X509Certificate cert = getCertificate(keyStoreFile, password, alias);
		byte[] certBytes = cert.getEncoded();
		ByteArrayInputStream bais = new ByteArrayInputStream(certBytes);
		FileOutputStream fos = new FileOutputStream(certFileName);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		int i;
		while ((i = bais.read()) != -1) {
			bos.write(i);
		}
		bos.close();
		bais.close();
	}


	/**
	 *  Gets the DER encoded bytes from from a PEM encoded certificate signing
	 *  request CSR
	 *
	 *@param  csr            The certificate siging request in PEM format
	 *@return                DER encoded bytes
	 */
	public static byte[] pkcs10PEMtoDER(byte[] csr) {
		byte[] buffer;
		try {
			buffer = FileTools.getBytesFromPEM(csr, PEM_CSR_HEADER_TYPE1, PEM_CSR_FOOTER_TYPE1);
		} catch (IOException e) {
			try {
				buffer = FileTools.getBytesFromPEM(csr, PEM_CSR_HEADER_TYPE2, PEM_CSR_FOOTER_TYPE2);
			} catch (IOException ioe) {
				buffer = Base64.decode(csr);
			}
		}
		return buffer;
	}

	//----------------------------

	/**
	 *  Creates a keystore, the private key password is is the same as the store password
	 *
	 *@param  alias          the alias used for the key entry
	 *@param  privKey        RSA private key
	 *@param  cert           user certificate
	 *@param  cacert         CA-certificate or null if only one cert in chain, in
	 *      that case use 'cert'.
	 *@param  password       user's password
	 *@return                byte[] containing PKCS12-file in binary format
	 *@exception  Exception  if input parameters are not OK or certificate
	 *      generation fails
	 */
	public static KeyStore createKeyStore(String alias, char[] password, PrivateKey privKey, X509Certificate cert, X509Certificate cacert)
			 throws Exception {
		Certificate[] chain;
		if (cacert == null) {
			chain = null;
		} else {
			chain = new Certificate[1];
			chain[0] = cacert;
		}
		return createKeyStore(alias, password, privKey, cert, chain);
	}


	/**
	 *  Creates a keystore, the private key password is null.
	 *
	 *@param  alias          the alias used for the key entry
	 *@param  privKey        RSA private key
	 *@param  cert           user certificate
	 *@param  cachain        CA-certificate chain or null if only one cert in
	 *      chain, in that case use 'cert'.
	 *@param  password       user's password
	 *@return                byte[] containing PKCS12-file in binary format
	 *@exception  Exception  if input parameters are not OK or certificate
	 *      generation fails
	 */
	public static KeyStore createKeyStore(String alias, char[] password, PrivateKey privKey, X509Certificate cert, Certificate[] cachain)
			 throws Exception {
		System.out.println(">createKeystore: privKey, cert=" + cert.getSubjectDN() + ", cachain.length=" + (cachain == null ? 0 : cachain.length));

		if (cert == null) {
			throw new IllegalArgumentException("Parameter cert cannot be null.");
		}
		int len = 1;
		if (cachain != null) {
			len += cachain.length;
		}
		Certificate[] chain = new Certificate[len];
		// To not get a ClassCastException we need to genereate a real new certificate with BC
		CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
		chain[0] = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert.getEncoded()));
		if (cachain != null) {
			for (int i = 0; i < cachain.length; i++) {
				X509Certificate tmpcert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cachain[i].getEncoded()));
				chain[i + 1] = tmpcert;
			}
		}
		if (chain.length > 1) {
			for (int i = 1; i < chain.length; i++) {
				X509Certificate cacert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(chain[i].getEncoded()));
				// Set attributes on CA-cert
				PKCS12BagAttributeCarrier caBagAttr = (PKCS12BagAttributeCarrier) chain[i];
				String cafriendly = CertTools.getPartFromDN(cacert.getSubjectDN().toString(), "CN");
				caBagAttr.setBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString(cafriendly));
			}
		}
		// Set attributes on user-cert
		PKCS12BagAttributeCarrier certBagAttr = (PKCS12BagAttributeCarrier) chain[0];
		certBagAttr.setBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString(alias));
		// in this case we just set the local key id to that of the public key
		certBagAttr.setBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, createSubjectKeyId(chain[0].getPublicKey()));

		// "Clean" private key, i.e. remove any old attributes
		KeyFactory keyfact = KeyFactory.getInstance(privKey.getAlgorithm(), "BC");
		PrivateKey pk = keyfact.generatePrivate(new PKCS8EncodedKeySpec(privKey.getEncoded()));
		// Set attributes for private key
		PKCS12BagAttributeCarrier keyBagAttr = (PKCS12BagAttributeCarrier) pk;
		// in this case we just set the local key id to that of the public key
		keyBagAttr.setBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString(alias));
		keyBagAttr.setBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, createSubjectKeyId(chain[0].getPublicKey()));

		//set the keystore type and provider
		String type = System.getProperty("pkit.keystore.type");
		//if one of the props has not been set then not even the default properteis have ben assigned
		if (type == null) {
			init();
			type = System.getProperty("pkit.keystore.type");
		}
		String provider = System.getProperty("pkit.keystore.provider");
		System.out.println("Keystore type: " + type + " Keystore provider" + provider);
		// store the key and the certificate chain
		KeyStore store = KeyStore.getInstance(type, provider);
		store.load(null, null);
		store.setKeyEntry(alias, pk, password, chain);
		System.out.println(">createKeystore: privKey, cert=" + cert.getSubjectDN() + ", cachain.length=" + (cachain == null ? 0 : cachain.length));

		return store;
	}

	//-----------------------------

	/**
	 *  Gets the certificate corresponding to the given alias from the keyStore
	 *
	 *@param  keyStoreFilename               The filename of the keyStore
	 *@param  password                       The keyStore password
	 *@param  alias                          The alias for the certifcate
	 *@return                                The DER encoded bytes of a cert
	 *@exception  KeyStoreException
	 *@exception  NoSuchProviderException
	 *@exception  NoSuchAlgorithmException
	 *@exception  IOException
	 *@exception  CertificateException
	 *@exception  UnrecoverableKeyException
	 */
	public static X509Certificate getCertificate(String keyStoreFilename, char[] password, String alias)
			 throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException,
			IOException, CertificateException, UnrecoverableKeyException {
		KeyStore keyStore = getKeyStore(keyStoreFilename, password);
		X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
		return cert;
	}


	/**
	 *  Gets a loaded keystore for the given keystore file and password using the
	 *  property defined type and provider..
	 *
	 *@param  keyStoreFileName               The filename of the keystore
	 *@param  password                       The password to the keystore
	 *@return                                a keystore
	 *@exception  KeyStoreException 
	 *@exception  NoSuchProviderException
	 *@exception  NoSuchAlgorithmException
	 *@exception  IOException
	 *@exception  CertificateException 
	 *@exception  UnrecoverableKeyException
	 */
	public static KeyStore getKeyStore(String keyStoreFileName, char[] password) throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException,
			IOException, CertificateException, UnrecoverableKeyException {
		String type = System.getProperty("pkit.keystore.type");
		//if one of the props has not been set then not even the default properteis have ben assigned
		if (type == null) {
			init();
			type = System.getProperty("pkit.keystore.type");
		}
		String provider = System.getProperty("pkit.keystore.provider");
		KeyStore keyStore = KeyStore.getInstance(type, provider);
		keyStore.load(new FileInputStream(keyStoreFileName), password);
		return keyStore;
	}

	public static void generateCSR(String keystoreFileName, char[] password, String cSRFileName, String alias) throws 
		NoSuchProviderException,NoSuchAlgorithmException,CertificateException,UnrecoverableKeyException,
		FileNotFoundException, IOException, KeyStoreException, SignatureException, InvalidKeyException {
			KeyStore keystore = getKeyStore(keystoreFileName, password);
			PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, password);
			X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);
			PublicKey publicKey = cert.getPublicKey();
			Principal principal = cert.getSubjectDN();
			X509Name subject = new X509Name(principal.getName());
			DERConstructedSet attributes = null;
			PKCS10CertificationRequest pkcs10Req 	 = new PKCS10CertificationRequest("SHA1withRSA", subject,
					publicKey, attributes,
					privateKey);
			byte[] pkcs10DERBytes = pkcs10Req.getEncoded();
			FileOutputStream fos = new FileOutputStream(cSRFileName);
			fos.write("-----BEGIN CERTIFICATE REQUEST-----\n".getBytes());
			fos.write(Base64.encode(pkcs10DERBytes));
			fos.write("\n-----END CERTIFICATE REQUEST-----\n".getBytes());
			fos.close();
	}
			
			

	/**
	 *  Create the subject key identifier.
	 *
	 *@param  pubKey  the public key
	 *@return         SubjectKeyIdentifer asn.1 structure
	 */
	public static SubjectKeyIdentifier createSubjectKeyId(PublicKey pubKey) {
		try {
			ByteArrayInputStream bIn = new ByteArrayInputStream(pubKey.getEncoded());
			SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
					(ASN1Sequence) new DERInputStream(bIn).readObject());
			return new SubjectKeyIdentifier(info);
		} catch (Exception e) {
			throw new RuntimeException("error creating key");
		}
	}
}

