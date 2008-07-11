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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Klass som hanterar datum som ett f�lt i CV-certifikatet
 * 
 * @author Keijo Kurkinen, Swedish National Police Board
 * @version $Id$
 *
 */
public class DateField extends AbstractDataField {

   /* L�ngden p� array f�r datum �r alltid sex tecken */
   private static final int  DATE_ARRAY_SIZE = 6;
   
   /* Datumformat f�r att plocka fram det relevanta */
   //private static final DateFormat FORMAT_YEAR_TO_DAY  = new SimpleDateFormat("yyMMdd");
   private static final DateFormat FORMAT_PRINTABLE    = new SimpleDateFormat("yyyy-MM-dd");


   private Date date;

   DateField(CVCTagEnum type){
      super(type);
   }

   /**
    * Konstruktor som tar en Date
    * @param type
    * @param date
    */
   DateField(CVCTagEnum type, Date date){
      this(type);
      
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);

      // St�da bort tidsinformationen
      int year  = cal.get(Calendar.YEAR);
      int month = cal.get(Calendar.MONTH);
      int day   = cal.get(Calendar.DAY_OF_MONTH);
      cal.clear();
      cal.set(year, month, day);
      this.date = cal.getTime();
   }

   /**
    * Konstruktor f�r att avkoda DER-kodat data
    * @param type
    * @param data
    */
   DateField(CVCTagEnum type, byte[] data){
      this(type);
      if( data==null || data.length!=6 ){
         throw new IllegalArgumentException("data argument must have length 6, was " + (data==null?0:data.length));
      }
      int year  = 2000 + data[0]*10 + data[1];
      int month = data[2]*10 + data[3] - 1; // Java month index starts with 0...
      int day   = data[4]*10 + data[5];

      Calendar cal = Calendar.getInstance();
      cal.clear();
      cal.set(year, month, day);
      date = cal.getTime();
   }
   
   /**
    * Returnerar datumet
    * @return
    */
   public Date getDate() {
      return date;
   }

   
   /**
    * Kodar datum s� att varje siffra i '080407' lagras som en egen byte
    * @param date
    * @return
    */
   @Override
   protected byte[] getEncoded() {
      byte[] dateArr = new byte[DATE_ARRAY_SIZE];
      
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      int year  = cal.get(Calendar.YEAR) - 2000; // �ret kodas som 08, 09, 10 ...
      int month = cal.get(Calendar.MONTH) + 1;   // M�nad kodas som 1,2, ... ,12
      int day   = cal.get(Calendar.DAY_OF_MONTH);

      dateArr[0] = (byte)(year / 10);
      dateArr[1] = (byte)(year % 10);
      dateArr[2] = (byte)(month / 10);
      dateArr[3] = (byte)(month % 10);
      dateArr[4] = (byte)(day / 10);
      dateArr[5] = (byte)(day % 10);
      return dateArr;
   }

   @Override
   protected String valueAsText() {
      return FORMAT_PRINTABLE.format(date);
   }
   
}
