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

import java.util.HashMap;

/**
 * Utility for mapping a String of type "SHA1WITHRSA" to our own type OIDFIeld.
 * 
 * @author Keijo Kurkinen, Swedish National Police Board
 * @version $Id$
 */
public class AlgorithmUtil {

   private static HashMap<String, OIDField> algorithmMap = new HashMap<String, OIDField>();
   
   static {
      algorithmMap.put("SHA1WITHRSA",          CVCObjectIdentifiers.id_TA_RSA_v1_5_SHA_1);
      algorithmMap.put("SHA256WITHRSA",        CVCObjectIdentifiers.id_TA_RSA_v1_5_SHA_256);
      algorithmMap.put("SHA1WITHRSAANDMGF1",   CVCObjectIdentifiers.id_TA_RSA_PSS_SHA_1);
      algorithmMap.put("SHA256WITHRSAANDMGF1", CVCObjectIdentifiers.id_TA_RSA_PSS_SHA_256);
      
      algorithmMap.put("SHA1WITHECDSA",        CVCObjectIdentifiers.id_TA_ECDSA_SHA_1);
      algorithmMap.put("SHA224WITHECDSA",      CVCObjectIdentifiers.id_TA_ECDSA_SHA_224);
      algorithmMap.put("SHA256WITHECDSA",      CVCObjectIdentifiers.id_TA_ECDSA_SHA_256);
   }

   /**
    * Returns the OIDField associated with 'algorithmName'
    * @param algorithmName
    * @return
    */
   public static OIDField getOIDField(String algorithmName) {
      OIDField oid = algorithmMap.get(algorithmName.toUpperCase());
      if( oid==null ) {
         throw new IllegalArgumentException("Unsupported algorithmName: " + algorithmName);
      }
      return oid;
   }
 
   
   /**
    * Returns algorithmName for a given OID
    * @param oid
    * @return
    */
   public static String getAlgorithmName(OIDField oid){
      for( String key : algorithmMap.keySet() ){
         OIDField oidfield = algorithmMap.get(key);
         if( oidfield.getValue().equals(oid.getValue()) ){
            return key;
         }
      }
      throw new IllegalArgumentException("Unknown OIDField: " + oid.getValue());
   }

}
