/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
 
package se.anatom.ejbca.log;

import java.util.Date;


/**
 * For docs, see LogEntryDataBean
 *
 * @version $Id: LogEntryDataLocal.java,v 1.6 2004-04-16 07:38:57 anatom Exp $
 **/
public interface LogEntryDataLocal extends javax.ejb.EJBLocalObject {
    // Public methods

    public Integer getId();

    public int getAdminType();
    
    public String getAdminData();
    
    public int getCaId();
    
    public int getModule();
    
    public String getUsername();

    public String getCertificateSNR();
  
    public int getEvent();
       
    public String getComment();
      
    public Date getTimeAsDate();

}

