package pkit.examples;

import pkit.cert.RSASigner;
import pkit.cert.SignUtils;
import pkit.cert.CertificateData;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 *  A example of how to use the cert package to signe a cert
 *
 *@author     justinw
 *@created    December 2003
 */
class CertificateTool {

	/**
	 *  The main program for the CertificateTool class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) {
		String dn = SignUtils.createDN("My Name", "@Home", "Gumjungle", "Boksburg", "Gauteng", "ZA");
		RSASigner signer = new RSASigner();
		try {
			//makes a key pair
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
			keyGen.initialize(1024, random);
			KeyPair pair = keyGen.generateKeyPair();
			PrivateKey privateKey = pair.getPrivate();
			PublicKey publicKey = pair.getPublic();

			//sign it
			Certificate certificate = signer.createCertificate(dn, CertificateData.CERT_TYPE_SIGNATURE, publicKey);

			//save that to a file
			byte[] encCertBytes = certificate.getEncoded();
			ByteArrayInputStream bais = new ByteArrayInputStream(encCertBytes);

			FileOutputStream fos = new FileOutputStream("myCert.cer");
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			System.out.println(new String(encCertBytes));
			int i;
			while ((i = bais.read()) != -1) {
				bos.write(i);
			}
			bos.close();
			bais.close();

			System.out.println("Your cert was written to a myCert.cer");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

