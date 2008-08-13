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

/**
 * Represents data To Be Signed
 * 
 * @author Keijo Kurkinen, Swedish National Police Board
 * @version $Id$
 */
public class TBSData {

   private byte[] data;

   // Only static methods...
   private TBSData(byte[] data){
      this.data = data;
   }

   /**
    * Constructs an instance using a CVCertificate (used when creating
    * a CVCAuthenticatedRequest)
    * @param body
    * @return
    * @throws IOException
    */
   static TBSData getInstance(CVCertificate cert) throws IOException {
      return getTBS(cert);
   }

   /**
    * Constructs an instance using a CVCertificateBody (used when creating
    * a CVCertificate representing a certificate or a certificate request).
    * @param body
    * @return
    * @throws IOException
    */
   static TBSData getInstance(CVCertificateBody body) throws IOException {
      return getTBS(body);
   }


   // Helper method
   private static TBSData getTBS(AbstractSequence seq) throws IOException {
      return new TBSData(seq.getDEREncoded());
   }

   /**
    * Returns the data to be signed
    * @return
    */
   public byte[] getEncoded() {
      return data;
   }

}
