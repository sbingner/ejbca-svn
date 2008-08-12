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
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;

import org.ejbca.cvc.exception.ConstructionException;


/**
 * Represents a CVC-request having an outer signature
 * 
 * @author Keijo Kurkinen, Swedish National Police Board
 * @version $Id$
 *
 */
public class CVCAuthenticatedRequest
      extends AbstractSequence {


   private static CVCTagEnum[] allowedFields = new CVCTagEnum[] {
      CVCTagEnum.CV_CERTIFICATE, 
      CVCTagEnum.CA_REFERENCE,
      CVCTagEnum.SIGNATURE
   };

   @Override
   CVCTagEnum[] getAllowedFields() {
      return allowedFields;
   }

   /**
    * Default constructor
    */
   CVCAuthenticatedRequest() {
      super(CVCTagEnum.REQ_AUTHENTICATION);
   }

   /**
    * Creates an instance
    * @param cvcert
    * @param caReference
    * @param signatureData
    */
   public CVCAuthenticatedRequest(CVCertificate cvcert, CAReferenceField caReference, byte[] signatureData) 
   throws ConstructionException {
      this();
      
      addSubfield(cvcert);
      addSubfield(caReference);
      addSubfield(new ByteField(CVCTagEnum.SIGNATURE, signatureData));
   }

   /**
    * Returns the embedded request (as an instance of CVCertificate)
    * @return
    */
   public CVCertificate getRequest() throws NoSuchFieldException {
      return (CVCertificate)getSubfield(CVCTagEnum.CV_CERTIFICATE);
   }

   /**
    * Returns CA_REFERENCE
    * @return
    */
   public CAReferenceField getAuthorityReference() throws NoSuchFieldException {
      return (CAReferenceField)getSubfield(CVCTagEnum.CA_REFERENCE);
   }

   /**
    * Returns the signature
    * @return
    */
   public byte[] getSignature() throws NoSuchFieldException {
      return ((ByteField)getSubfield(CVCTagEnum.SIGNATURE)).getData();
   }


   /**
    * Verifies the signature
    * @param pubKey
    * @throws CertificateException
    * @throws NoSuchAlgorithmException
    * @throws InvalidKeyException
    * @throws NoSuchProviderException
    * @throws SignatureException
    */
   public void verify(PublicKey pubKey) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
      try {
         // Must find the hash algorithm
         String algorithm = "";
         if( pubKey instanceof CVCPublicKey ){
            // If pubKey is an instance of CVCPublicKey then the algorithm can be extracted
        	// using its OID
            CVCPublicKey cvcKey = (CVCPublicKey)pubKey;
            algorithm = AlgorithmUtil.getAlgorithmName(cvcKey.getObjectIdentifier());
         }
         else {
            // Otherwise we assume that the inner signature is calculated using the same 
            // hash algorithm as the outer one!
            CVCPublicKey cvcKey = getRequest().getCertificateBody().getPublicKey();
            algorithm = AlgorithmUtil.getAlgorithmName(cvcKey.getObjectIdentifier());
         }
         Signature sign = Signature.getInstance(algorithm);
         
         // Now verify the signature
         TBSData tbs = TBSData.getInstance(getRequest());
         sign.initVerify(pubKey);
         sign.update(tbs.getEncoded());
         if( !sign.verify(getSignature()) ){
            throw new SignatureException("Signature verification failed!");
         }
      }
      catch( NoSuchFieldException e ){
         throw new CertificateException("CV-Certificate is corrupt", e);
      }
      catch( IOException e ){
         throw new CertificateException("CV-Certificate is corrupt", e);
      }
   }

   /**
    * Little helper
    */
   public String toString() {
      return getAsText("", true);
   }

}
