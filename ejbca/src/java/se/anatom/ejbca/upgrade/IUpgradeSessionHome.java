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
 
package se.anatom.ejbca.upgrade;

import java.rmi.RemoteException;
import javax.ejb.CreateException;

/**
 * @version $Id: IUpgradeSessionHome.java,v 1.2 2004-04-16 07:39:02 anatom Exp $
 */
public interface IUpgradeSessionHome extends javax.ejb.EJBHome {
    /**
     * Default create method. Maps to ejbCreate in implementation.
     *
     * @return IUpgradeSessionRemote interface
     *
     * @throws CreateException
     * @throws RemoteException
     */
    IUpgradeSessionRemote create() throws CreateException, RemoteException;
}
