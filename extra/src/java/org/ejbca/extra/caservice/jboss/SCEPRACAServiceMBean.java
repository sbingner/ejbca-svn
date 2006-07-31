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
 
package org.ejbca.extra.caservice.jboss;

import org.ejbca.extra.caservice.ISCEPRACAService;

/** A Jboss service that authomatically creates CRLs when neccesary.
*/
public interface SCEPRACAServiceMBean extends org.jboss.system.ServiceMBean, ISCEPRACAService 
{    

}
