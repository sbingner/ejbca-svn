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

package org.ejbca.ui.web.protocol;

import java.math.BigInteger;
import java.security.cert.Certificate;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.ejbca.core.ejb.ca.caadmin.CAAdminSessionLocal;
import org.ejbca.core.ejb.ca.store.CertificateStatus;
import org.ejbca.core.ejb.ca.store.CertificateStoreSessionLocal;
import org.ejbca.core.model.ca.caadmin.CADoesntExistsException;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.ExtendedCAServiceNotActiveException;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.ExtendedCAServiceRequestException;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.IllegalExtendedCAServiceRequestException;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.OCSPCAServiceRequest;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.OCSPCAServiceResponse;
import org.ejbca.core.model.log.Admin;
import org.ejbca.core.protocol.ocsp.CertificateCache;
import org.ejbca.core.protocol.ocsp.CertificateCacheInternal;

/** 
 * Servlet implementing server side of the Online Certificate Status Protocol (OCSP)
 * For a detailed description of OCSP refer to RFC2560.
 *
 * @author Thomas Meckel (Ophios GmbH), Tomas Gustavsson, Lars Silven
 * @version  $Id$
 */
public class OCSPServlet extends OCSPServletBase {

    @EJB
    private CAAdminSessionLocal caAdminSessionLocal;

    private ISignSessionLocal m_signsession = null;
    
    public OCSPServlet() {
        super(new LocalOCSPData());
    }

    protected OCSPCAServiceResponse extendedService(Admin adm, int caid, OCSPCAServiceRequest request) throws CADoesntExistsException, ExtendedCAServiceRequestException, IllegalExtendedCAServiceRequestException, ExtendedCAServiceNotActiveException {
        return (OCSPCAServiceResponse)caAdminSessionLocal.extendedService(adm, caid, request);
    }
    static private class LocalOCSPData extends OCSPData {
        @EJB
        private CertificateStoreSessionLocal certificateStoreSessionLocal;
    
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPData#findCertificateByIssuerAndSerno(org.ejbca.core.model.log.Admin, java.lang.String, java.math.BigInteger)
         */
        protected Certificate findCertificateByIssuerAndSerno(Admin adm, String issuer, BigInteger serno) {
            return certificateStoreSessionLocal.findCertificateByIssuerAndSerno(adm, issuer, serno);
        }
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPData#getStatus(java.lang.String, java.math.BigInteger)
         */
        public CertificateStatus getStatus(String name, BigInteger serialNumber) {
            return certificateStoreSessionLocal.getStatus(name, serialNumber);
        }
    }

    protected CertificateCache createCertificateCache() {
		return CertificateCacheInternal.getInstance();
	}

    /* (non-Javadoc)
     * @see org.ejbca.ui.web.protocol.OCSPServletBase#loadPrivateKeys(org.ejbca.core.model.log.Admin, java.lang.String)
     */
    protected void loadPrivateKeys(Admin adm, String password) {
        // not used by this servlet
    }
} // OCSPServlet
