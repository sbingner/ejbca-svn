package pkit.cert;

import java.math.BigInteger;
import java.security.cert.Certificate;
import java.util.Date;

/**
 *  For docs, see CertificateDataBean in EJBCA
 *
 *@author     justinw
 *@created    December 2003
 */
public class CertificateData {

	// Constants for Status of certificate
	/**
	 *  Certificate doesn't belong to anyone
	 */
	public static int CERT_UNASSIGNED = 0;
	/**
	 *  Assigned, but not yet active
	 */
	public static int CERT_INACTIVE = 10;
	/**
	 *  Certificate is active and assigned
	 */
	public static int CERT_ACTIVE = 20;
	/**
	 *  Certificate is temporarily blocked (reversible)
	 */
	public static int CERT_TEMP_REVOKED = 30;
	/**
	 *  Certificate is permanently blocked (terminated)
	 */
	public static int CERT_REVOKED = 40;
	/**
	 *  Certificate is expired
	 */
	public static int CERT_EXPIRED = 50;
	/**
	 *  Certificate is expired and kept for archive purpose
	 */
	public static int CERT_ARCHIVED = 60;

	// Certificate types used to create certificates
	/**
	 *  Certificate used for encryption.
	 */
	public final static int CERT_TYPE_ENCRYPTION = 0x1;
	/**
	 *  Certificate used for digital signatures.
	 */
	public final static int CERT_TYPE_SIGNATURE = 0x2;
	/**
	 *  Certificate used for both encryption and signatures.
	 */
	public final static int CERT_TYPE_ENCSIGN = 0x3;

	// Constants used to contruct KeyUsage
	/**
	 *@see    se.anatom.ejbca.ca.sign.ISignSession
	 */
	public final static int digitalSignature = (1 << 7);
	public final static int nonRepudiation = (1 << 6);
	public final static int keyEncipherment = (1 << 5);
	public final static int dataEncipherment = (1 << 4);
	public final static int keyAgreement = (1 << 3);
	public final static int keyCertSign = (1 << 2);
	public final static int cRLSign = (1 << 1);
	public final static int encipherOnly = (1 << 0);
	public final static int decipherOnly = (1 << 15);

}

