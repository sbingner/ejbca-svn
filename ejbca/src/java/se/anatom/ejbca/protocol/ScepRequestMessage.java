package se.anatom.ejbca.protocol;

import java.io.*;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.NoSuchProviderException;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.*;

import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cms.asn1.*;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.parsers.SignedDataParser;
import org.bouncycastle.cms.parsers.EnvelopedDataParser;

import se.anatom.ejbca.util.Base64;

/** Class to handle SCEP request messages sent to the CA.
 *
* @version  $Id: ScepRequestMessage.java,v 1.1 2002-10-17 15:22:57 anatom Exp $
 */
public class ScepRequestMessage implements RequestMessage, Serializable {

    static private Category cat = Category.getInstance( ScepRequestMessage.class.getName() );

    private static String id_Verisign = "2.16.840.1.113733";
    private static String id_pki = id_Verisign + ".1";
    private static String id_attributes = id_pki + ".9";
    private static String id_messageType = id_attributes + ".2";
    private static String id_pkiStatus = id_attributes + ".3";
    private static String id_failInfo = id_attributes + ".4";
    private static String id_senderNonce = id_attributes + ".5";
    private static String id_recipientNonce = id_attributes + ".6";
    private static String id_transId = id_attributes + ".7";
    private static String id_extensionReq = id_attributes + ".8";

    /** Raw form of the PKCS10 message
     */
    private byte[] msg;

    /** Signed data, the whole enchilada to to speak...
     */
    private transient SignedData sd = null;
    /** Enveloped data, carrying the 'beef' of the request
     */
    private transient EnvelopedData envData = null;

    /** Certificate used for decryption, verification
     */
    private X509Certificate cert;
    /** Private key used for decryption.
     */
    private PrivateKey privateKey;
    /** The contained pkcs10 request message
     */
    private transient PKCS10CertificationRequest pkcs10 = null;

    /** The messageType attribute specify the type of operation performed by the
     * transaction. This attribute is required in all PKI messages. Currently, the following message types are defined:
     * PKCSReq (19)  -- Permits use of PKCS#10 certificate request
     * CertRep (3)   -- Response to certificate or CRL request
     * GetCertInitial (20)  -- Certificate polling in manual enrollment
     * GetCert (21)  -- Retrieve a certificate
     * GetCRL  (22)  -- Retrieve a CRL
     */
    private int messageType = 0;
    /** SenderNonce in a request is used as recipientNonce when the server sends back a reply to the client
    */
    private String sendeNonce = null;
    /** Type of error
     */
    int error = 0;

    /** Constucts a new SCEP/PKCS7 message handler object.
     * @param msg The DER encoded PKCS7 request.
     * @throws IOException if the request can not be parsed.
     */
    public ScepRequestMessage(byte[] msg) throws IOException {
        cat.debug(">ScepRequestMessage");
        this.msg = msg;
        init();
        cat.debug("<ScepRequestMessage");
    }

    private void init() throws IOException {
        cat.debug(">init");
        // Parse and verify the entegrity of the PKIOperation message PKCS#7

        /* If this would have been done using the newer CMS it would have made me so much happier... */
        DERConstructedSequence seq =(DERConstructedSequence)(new DERInputStream(new ByteArrayInputStream(msg)).readObject());
        ContentInfo ci = new ContentInfo(seq);
        String ctoid = ci.getContentType().getId();
        if (ctoid.equals(CMSObjectIdentifiers.signedData.getId())) {
            // This is SignedData so it is a pkcsCertReqSigned,
            //  pkcsGetCertInitialSigned, pkcsGetCertSigned, pkcsGetCRLSigned
            // (could also be pkcsRepSigned or certOnly, but we don't receive them on the server side

            // Try to find out what kind of message this is
            sd = new SignedData((DERConstructedSequence)ci.getContent());
            Enumeration sis = sd.getSignerInfos().getObjects();
            if (sis.hasMoreElements()) {
                SignerInfo si = new SignerInfo((ASN1Sequence)sis.nextElement());
                Enumeration attr = si.getAuthenticatedAttributes().getObjects();
                //Vector attr = si.getSignedAttrs().getAttributes();
                while (attr.hasMoreElements()) {
                    Attribute a = new Attribute((ASN1Sequence)attr.nextElement());
                    //Attribute a = (Attribute)iter.next();
                    cat.debug("Found attribute: "+a.getAttrType().getId());
                    if (a.getAttrType().getId().equals(id_messageType)) {
                        Enumeration values = a.getAttrValues().getObjects();
                        DERPrintableString str = DERPrintableString.getInstance(values.nextElement());
                        messageType = Integer.parseInt(str.getString());
                        cat.debug("Messagetype = "+messageType);
                        break; // we can olny handle one message type per message :-)
                    }
                }
            }
            // If this is a PKCSReq
            if (messageType == 19) {
                // Extract the contents, which is an encrypted PKCS10
                ci = sd.getEncapContentInfo();
                ctoid = ci.getContentType().getId();
                if (ctoid.equals(CMSObjectIdentifiers.data.getId())) {
                    DEROctetString content = (DEROctetString)ci.getContent();
                    cat.debug("envelopedData is "+content.getOctets().length+" bytes.");
                    DERConstructedSequence seq1 =(DERConstructedSequence)(new DERInputStream(new ByteArrayInputStream(content.getOctets())).readObject());
                    ci = new ContentInfo(seq1);
                    ctoid = ci.getContentType().getId();
                    if (ctoid.equals(CMSObjectIdentifiers.envelopedData.getId())) {
                        envData = new EnvelopedData((DERConstructedSequence)ci.getContent());
                    } else {
                        cat.error("EncapsulatedContentInfo does not contain PKCS7 envelopedData: "+ctoid);
                        error = 2;
                    }
                } else {
                    cat.error("EncapsulatedContentInfo is not of type 'data': "+ctoid);
                    error = 3;
                }

            } else {
                cat.error("This is not a certification request!");
                error = 4;
            }
        } else {
            cat.error("PKCSReq does not contain 'signedData': "+ctoid);
            error = 1;
        }
        cat.debug("<init");
    } // init

    private void decrypt() throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, CMSException,
    NoSuchProviderException, BadPaddingException, InvalidAlgorithmParameterException, GeneralSecurityException, IOException {
        cat.debug(">decrypt");
        // Now we are getting somewhere (pheew),
        // Now we just have to get the damn key...to decrypt the PKCS10
        if (envData == null) {
            cat.error("No enveloped data to decrypt!");
            return;
        }
        Enumeration ris = envData.getRecipientInfos().getObjects();
        if (ris.hasMoreElements()) {
            RecipientInfo ri = RecipientInfo.getInstance(ris.nextElement());
            DEREncodable info = ri.getInfo();
            if (info instanceof KeyTransRecipientInfo) {
                KeyTransRecipientInfo kti = (KeyTransRecipientInfo)info;
                String id = kti.getKeyEncryptionAlgorithm().getObjectId().getId();
                if(id.equals(PKCSObjectIdentifiers.rsaEncryption.getId())) {
                    cat.debug("Found key encrypted with RSA inside message.");
                    Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding", "BC");
                    cipher.init(Cipher.DECRYPT_MODE, privateKey);
                    byte[] cekBytes = cipher.doFinal(kti.getEncryptedKey().getOctets());
                    AlgorithmIdentifier aid = envData.getEncryptedContentInfo().getContentEncryptionAlgorithm();
                    String alg = aid.getObjectId().getId();
                    AlgorithmParameterSpec iv = getIv(alg, aid.getParameters());
                    SecretKey cek = getContentEncryptionKey(cekBytes, alg);
                    cat.debug("Extracted secret key.");
                    byte[] enc = envData.getEncryptedContentInfo().getEncryptedContent().getOctets();
                    cipher = getCipher(alg);
                    if(iv == null) {
                        cat.debug("IV is null.");
                        cipher.init(Cipher.DECRYPT_MODE, cek);
                    } else {
                        cat.debug("IV is NOT null.");
                        cipher.init(Cipher.DECRYPT_MODE, cek, iv);
                    }
                    byte[] pkcs10Bytes = unpad(cipher.doFinal(enc));
                    FileOutputStream fos = new FileOutputStream("C:\\pkcs10.txt");
                    fos.write(Base64.encode(pkcs10Bytes));
                    fos.close();
                    DERObject derobj = new DERInputStream(new ByteArrayInputStream(pkcs10Bytes)).readObject();
                    DERConstructedSequence seq = (DERConstructedSequence)derobj;
                    pkcs10 = new PKCS10CertificationRequest(seq);
                    cat.debug("Succesfulyy extracted PKCS10.");
                } else {
                    cat.error("Key not encrypted with RSA!");
                    error = 4;
                }
            } else {
                cat.error("RecipientInfo is not KeyTransRecipientInfo!");
                error = 5;
            }
        }
        cat.debug("<decrypt");
    } // decrypt

    public PublicKey getRequestPublicKey() {
        cat.debug(">getRequestPublicKey()");
        PublicKey ret = null;
        try {
            if (envData == null) {
                init();
                decrypt();
            }
            ret = pkcs10.getPublicKey();
        } catch (IOException e) {
            cat.error("PKCS7 not inited!");
            return null;
        } catch (GeneralSecurityException e) {
            cat.error("Error in PKCS7:", e);
            return null;
        } catch (CMSException e) {
            cat.error("Error in PKCS7:", e);
            return null;
        }
        cat.debug("<getRequestPublicKey()");
        return ret;
    }

    public boolean verify() {
        cat.debug(">verify()");
        boolean ret = false;
        try {
            if (pkcs10 == null) {
                init();
                decrypt();
            }
            ret = pkcs10.verify();
        } catch (IOException e) {
            cat.error("PKCS7 not inited!");
            return false;
        } catch (GeneralSecurityException e) {
            cat.error("Error in PKCS7:", e);
            return false;
        } catch (CMSException e) {
            cat.error("Error in PKCS7:", e);
            return false;
        }
        cat.debug("<verify()");
        return ret;
    }

    public boolean requireKeyInfo() {
        return true;
    }
    public void setKeyInfo(X509Certificate cert, PrivateKey key) {
        this.cert = cert;
        this.privateKey = key;
    }

    //
    // Private helper methods
    //
    private static Cipher getCipher(String _alg)
        throws CMSException, GeneralSecurityException {

        if(_alg.equals(PKCSObjectIdentifiers.des_EDE3_CBC.getId())) {
            return Cipher.getInstance("DESEDE/CBC/NoPadding", "BC");
        }
        else if(_alg.equals(PKCSObjectIdentifiers.RC2_CBC.getId())) {
            return Cipher.getInstance("RC2/CBC/NoPadding", "BC");
        }
        else {
            throw new CMSException("Invalid cipher algorithm");
        }
    }

    private static SecretKey getContentEncryptionKey(byte[] _keyBytes, String _alg)
        throws CMSException {

        if(_alg.equals(PKCSObjectIdentifiers.des_EDE3_CBC.getId())) {
            return new SecretKeySpec(_keyBytes, "DESEDE");
        }
        else if(_alg.equals(PKCSObjectIdentifiers.RC2_CBC.getId())) {
            return new SecretKeySpec(_keyBytes, "RC2");
        }
        else {
            throw new CMSException("Invalid content encryption key algorithm");
        }
    }

    // RFC 2630 6.3
    private static byte[] unpad(byte[] _dec) {
        byte _pad    = _dec[_dec.length - 1];
        int  _padInt = 0x000000FF & _pad;

        if((_padInt < 1) || (_padInt > 8)) {
            return _dec;
        }

        boolean _padded = true;
        for(int i = 1; i <= _padInt; i++) {
            byte _p = _dec[_dec.length - i];
            if(_p != _pad) {
                _padded = false;
                break;
            }
        }

        if(_padded) {
            byte[] _buf = new byte[_dec.length - _padInt];
            System.arraycopy(_dec, 0, _buf, 0, _buf.length);
            return _buf;
        }

        return _dec;
    }

    private static AlgorithmParameterSpec getIv(String _alg, DEREncodable _tmp)
        throws CMSException
    {
        // get 3des parameter spec

        if(_alg.equals(PKCSObjectIdentifiers.des_EDE3_CBC.getId())) {
            ASN1OctetString _iv = (ASN1OctetString)_tmp;
            return new IvParameterSpec( _iv.getOctets() );
        }
        // get rc2 parameter spec
        else if(_alg.equals(PKCSObjectIdentifiers.RC2_CBC.getId())) {
            // retrieve key parameter version and spec bytes
            //TODO:
        }
        else
        {
            throw new CMSException("Invalid cipher algorithm");
        }
        return null;
    }
} // ScepRequestMessage
