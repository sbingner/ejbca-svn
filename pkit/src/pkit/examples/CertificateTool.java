package pkit.examples;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.util.*;

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.*;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.jce.*;
import org.bouncycastle.jce.interfaces.*;

import pkit.cert.*;
import pkit.store.PkitKeyTool;

import se.anatom.ejbca.util.CertTools;
import se.anatom.ejbca.util.FileTools;
import se.anatom.ejbca.util.Base64;


/**
 *  Demonstrates the use of pkit.
 *
 *@author     justin
 *@created    June 8, 2003
 */
class CertificateTool {
	
	/**
	 *  The main program for the CertificateTool class
	 *
	 *@param  args           The command line arguments
	 *@exception  Exception  if anything goes wrong
	 */
	public static void main(String[] args) throws Exception {
		if (args[0].equals("-selfcert")) {
			if (args.length != 8) {
				System.out.println("Usage: CertificateTool  -clientCert DN keysize validity-days filename <storepassword isCA yes/no> <keystoreType PKCS12/JKS>");
				System.exit(1);
			}
			
			String dn = args[1];
			int keysize = Integer.parseInt(args[2]);
			int validity = Integer.parseInt(args[3]);
			String filename = args[4];
			String storepwd = args[5];
			setKeyToolProps(args[7]);
			
			boolean isCA=false;
			if (args[6].equals("yes")) isCA=true;
			
			PkitKeyTool.createKeystoreWithSelfCert(dn,"MyKey",keysize,validity,filename,storepwd.toCharArray(),isCA);
			
		} else if (args[0].equals("-certreq")) {
			if (args.length != 5) {
				System.out.println("Usage: CertificateTool -signCert keystoreFile password fileName ");
				System.exit(1);
			}
			String keystoreFileName = args[1];
			String password = args[2];
			String fileName = args[3];
			setKeyToolProps(args[4]);
			String alias = "MyKey";

			PkitKeyTool.generateCSR(keystoreFileName, password.toCharArray(), fileName,alias);
			System.out.println("Certificate signing request written to file: " + fileName );
		} else if (args[0].equals("-signRequest")) {
			if (args.length != 4) {
				System.out.println("Usage: CertificateTool -signRequest propertiesFile pkcs10File certFileName");
				System.exit(1);
			}
			String propFileName = args[1];
			String pkcs10FileName = args[2];
			String fileName = args[3];
			
			Properties props = new Properties();
			props.load(new FileInputStream(propFileName));
			RSASigner signer = new RSASigner(props);
			byte[] csrBytes = FileTools.readFiletoBuffer(pkcs10FileName);
			byte[] pkcs10DERBytes = PkitKeyTool.pkcs10PEMtoDER(csrBytes);
			PKCS10CertificationRequest request = new PKCS10CertificationRequest(pkcs10DERBytes);
			CertificationRequestInfo reqInfo  = request.getCertificationRequestInfo();
			X509Name subject = reqInfo.getSubject();
			java.security.cert.Certificate certificate = signer.createCertificate(subject, pkcs10DERBytes, 3);
			byte[] encCertBytes = certificate.getEncoded();
			ByteArrayInputStream bais = new ByteArrayInputStream(encCertBytes);
			FileOutputStream fos = new FileOutputStream(fileName);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			int i;
			while ((i = bais.read()) != -1) {
				bos.write(i);
			}
			bos.close();
			bais.close();
			System.out.println("Your cert was written to " + fileName);
		} else if (args[0].equals("-import")) {
			if (args.length != 6) {
				System.out.println ("Usage: CertificateTool -import keystoreFile password certificateFile  CACertFileName");
				System.exit(1);
			}
			
			String keystoreFile = args[1];
			String storePassword = args[2];
			String certificateFile = args[3];
			String cAcertificateFile = args[4];
			setKeyToolProps(args[5]);
			
			System.out.println("Receiving cert reply:");
			System.out.println("Cert reply file: " + certificateFile);
			System.out.println("Storing KeyStore in: " + keystoreFile);
			System.out.println("Protected with storepassword: " + storePassword);
			System.out.println("CA Certificate File: " + cAcertificateFile);
			
			PkitKeyTool.acceptRequestReply(certificateFile, cAcertificateFile, keystoreFile, storePassword.toCharArray(), "MyKey");
		} else if (args[0].equals("-export")) {
			if (args.length != 6) {
				System.out.println ("Usage: CertificateTool -export keystoreFile password alias toFile");
				System.exit(1);
			}
			String keystoreFile = args[1];
			String password = args[2];
			String alias = args[3];
			String fileName = args[4];
			setKeyToolProps(args[5]);
			
			X509Certificate cert = PkitKeyTool.getCertificate(keystoreFile, password.toCharArray(), alias);
			byte[] certBytes = cert.getEncoded();
			ByteArrayInputStream bais = new ByteArrayInputStream(certBytes);
			FileOutputStream fos = new FileOutputStream(fileName);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			int i;
			while ((i = bais.read()) != -1) {
				bos.write(i);
			}
			bos.close();
			bais.close();
			System.out.println("Your cert was written to " + fileName);
		} else if (args[0].equals("-gencrl")) {
			if (args.length < 2) {
				System.out.println ("\t CertificateTool -gencrl propertiesFile CRLfile [certFile1 certFile2 certFile3 ... certFilen]");
				System.exit(1);
			}
			
			String propFileName = args[1];
			String cRLFile = args[2];
			
			Properties props = new Properties();
			props.load(new FileInputStream(propFileName));
			RSASigner signer = new RSASigner(props);
			

			
			Vector certs = null;
			//if the number of args is less than 3 then an empty CRL is created
			if (args.length>=3) {
				certs = new Vector();
				//the third arg will be the FIRST cert to be revoked
				for (int i = 3;i<args.length;i++) {
					X509Certificate cert = CertTools.getCertfromByteArray(FileTools.readFiletoBuffer(args[i]));
					certs.add(cert);
				}
			}
			
			X509CRL cRL = signer.createCRL(365,certs,1);
			byte[] dEREncodedCRLBytes = cRL.getEncoded();		
			ByteArrayInputStream bais = new ByteArrayInputStream(dEREncodedCRLBytes);
			FileOutputStream fos = new FileOutputStream(cRLFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			int i;
			while ((i = bais.read()) != -1) {
				bos.write(i);
			}
			bos.close();
			bais.close();
			
			System.out.println("Your CRL was written to " + cRLFile);
		} else {//if none of the first args matched recognised options
			System.out.println("Usage: CertificateTool  -clientCert DN keysize validity-days filename <storepassword isCA yes/no> <keystoreType PKCS12/JKS>");
			System.out.println("\t CertificateTool  -clientCert DN keysize validity-days filename <storepassword isCA yes/no> <keystoreType PKCS12/JKS>");
			System.out.println("\t CertificateTool -signCert keystoreFile password fileName ");
			System.out.println("\t CertificateTool -signRequest propertiesFile pkcs10File certFileName");
			System.out.println ("\t CertificateTool -import keystoreFile password certificateFile  CACertFileName");
			System.out.println ("\t CertificateTool -export keystoreFile password alias toFile");
			System.out.println ("\t CertificateTool -gencrl CRLfile certFile1 [certFile2 certFile3 ... certFilen]");
		}
	}
	
	private static void setKeyToolProps (String type) {
		if (type.equals("PKCS12")) {
			PkitKeyTool.init();
		} else {
			Properties props = new Properties();
			props.setProperty("pkit.keystore.type","JKS");
			props.setProperty("pkit.keystore.provider","SUN");
			PkitKeyTool.init(props);
		}
		System.out.println("Keystore type "+System.getProperty("pkit.keystore.type")+" Keystore provider " + System.getProperty("pkit.keystore.provider"));
	}

}



