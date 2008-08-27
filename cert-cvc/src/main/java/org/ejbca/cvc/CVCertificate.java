/*************************************************************************
 *                                                                       *
 *  CERT-CVC: EAC 1.11 Card Verifiable Certificate Library               * 
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.ejbca.cvc;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.InvalidKeySpecException;

import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.ejbca.cvc.exception.ConstructionException;
import org.ejbca.cvc.util.BCECUtil;

/**
 * 
 * Represents a Card Verifiable Certificate according to the specification for EAC 1.11.
 * 
 * @author Keijo Kurkinen, Swedish National Police Board
 * @version $Id: CVCertificate.java 5999 2008-08-12 15:41:09Z keijox $
 */
public class CVCertificate extends AbstractSequence implements Signable {

   private static CVCTagEnum[] allowedFields = new CVCTagEnum[] {
      CVCTagEnum.CERTIFICATE_BODY, 
      CVCTagEnum.SIGNATURE 
   };

   @Override
   CVCTagEnum[] getAllowedFields() {
      return allowedFields;
   }


   /**
    * Default constructor
    */
   CVCertificate(){
      super(CVCTagEnum.CV_CERTIFICATE);
   }

   /**
    * Creates an instance from a CVCRequestBody
    * @param body
    * @throws IllegalArgumentException if the argument is null
    */
   CVCertificate(CVCertificateBody body) throws ConstructionException {
      this();
      
      if( body==null ){
         throw new IllegalArgumentException("body is null");
      }
      addSubfield(body);
   }

   /**
    * Adds signature data
    * @param signatureData
    * @throws ConstructionException
    */
   public void setSignature(byte[] signatureData) throws ConstructionException {
      addSubfield(new ByteField(CVCTagEnum.SIGNATURE, signatureData));
   }

   /**
    * Returns the embedded CertificateBody
    * @return
    */
   public CVCertificateBody getCertificateBody() throws NoSuchFieldException {
      return (CVCertificateBody)getSubfield(CVCTagEnum.CERTIFICATE_BODY);
   }

   /**
    * Returns the signature
    * @return
    */
   public byte[] getSignature() throws NoSuchFieldException {
      return ((ByteField)getSubfield(CVCTagEnum.SIGNATURE)).getData();
   }

   /**
    * Returns the data To Be Signed
    */
   public byte[] getTBS() throws ConstructionException {
      try {
         return getCertificateBody().getDEREncoded();
      }
      catch( IOException e ){
         throw new ConstructionException(e);
      }
      catch( NoSuchFieldException e ){
         throw new ConstructionException(e);
      }
   }


   /**
    * Returns the certificate in text format
    */
   public String toString() {
      return getAsText("");
   }

   /**
    * Verifies the signature
    */
   public void verify(PublicKey key, String provider) throws CertificateException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
      try {
         // Lookup the OID, the hash-algorithm can be found through it
         OIDField oid = getCertificateBody().getPublicKey().getObjectIdentifier();
         Signature sign = Signature.getInstance(AlgorithmUtil.getAlgorithmName(oid), provider);
         
         // Verify the signature
         PublicKey vKey = key;
         
         // If the key is an EC key, we must convert our CVC EC key to a BC EC key in order for the verification to work.
         if (key instanceof PublicKeyEC) {
        	 PublicKeyEC eckey = (PublicKeyEC)key;
        	 ECParameterSpec spec = eckey.getParams();
        	 ECPoint point = eckey.getW();
        	 org.bouncycastle.jce.spec.ECParameterSpec bcspec = BCECUtil.convertSpec(spec, false);
        	 org.bouncycastle.math.ec.ECPoint bcpoint = BCECUtil.convertPoint(spec, point, false);
        	 ECPublicKeySpec pkspec = new ECPublicKeySpec(bcpoint, bcspec);
        	 KeyFactory f = KeyFactory.getInstance("ECDSA", "BC");
        	 try {
				vKey = f.generatePublic(pkspec);
			} catch (InvalidKeySpecException e) {
				throw new InvalidKeyException(e);
			}
         }
         
         sign.initVerify(vKey);
         sign.update(getTBS());
         if( !sign.verify(getSignature()) ){
            throw new SignatureException("Signature verification failed!");
         }
      }
      catch( NoSuchFieldException e ){
         throw new CertificateException("CV-Certificate is corrupt", e);
      }
      catch( ConstructionException e ){
         throw new CertificateException("CV-Certificate is corrupt", e);
      }
   }

}
