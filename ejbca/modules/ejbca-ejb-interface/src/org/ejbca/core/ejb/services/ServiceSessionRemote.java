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
package org.ejbca.core.ejb.services;

import javax.ejb.Remote;

/**
 * Remote interface for ServiceSession.
 */
@Remote
public interface ServiceSessionRemote {
    /**
     * Adds a Service to the database.
     * 
     * @throws ServiceExistsException
     *             if service already exists.
     * @throws EJBException
     *             if a communication or other error occurs.
     */
    public void addService(org.ejbca.core.model.log.Admin admin, java.lang.String name, org.ejbca.core.model.services.ServiceConfiguration serviceConfiguration)
            throws org.ejbca.core.model.services.ServiceExistsException, java.rmi.RemoteException;

    /**
     * Adds a service to the database. Used for importing and exporting profiles
     * from xml-files.
     * 
     * @throws ServiceExistsException
     *             if service already exists.
     * @throws EJBException
     *             if a communication or other error occurs.
     */
    public void addService(org.ejbca.core.model.log.Admin admin, int id, java.lang.String name,
            org.ejbca.core.model.services.ServiceConfiguration serviceConfiguration) throws org.ejbca.core.model.services.ServiceExistsException,
            java.rmi.RemoteException;

    /**
     * Updates service configuration, but does not re-set the timer
     * 
     * @param noLogging
     *            if true no logging (to the database will be done
     * @throws EJBException
     *             if a communication or other error occurs.
     */
    public void changeService(org.ejbca.core.model.log.Admin admin, java.lang.String name,
            org.ejbca.core.model.services.ServiceConfiguration serviceConfiguration, boolean noLogging) throws java.rmi.RemoteException;

    /**
     * Adds a service with the same content as the original.
     * 
     * @throws ServiceExistsException
     *             if service already exists.
     * @throws EJBException
     *             if a communication or other error occurs.
     */
    public void cloneService(org.ejbca.core.model.log.Admin admin, java.lang.String oldname, java.lang.String newname)
            throws org.ejbca.core.model.services.ServiceExistsException, java.rmi.RemoteException;

    /**
     * Removes a service from the database.
     * 
     * @throws EJBException
     *             if a communication or other error occurs.
     */
    public boolean removeService(org.ejbca.core.model.log.Admin admin, java.lang.String name) throws java.rmi.RemoteException;

    /**
     * Renames a service
     * 
     * @throws ServiceExistsException
     *             if service already exists.
     * @throws EJBException
     *             if a communication or other error occurs.
     */
    public void renameService(org.ejbca.core.model.log.Admin admin, java.lang.String oldname, java.lang.String newname)
            throws org.ejbca.core.model.services.ServiceExistsException, java.rmi.RemoteException;

    /**
     * Retrives a Collection of id:s (Integer) to visible authorized services.
     * Currently is the only check if the superadmin can see them all
     * 
     * @return Collection of id:s (Integer)
     */
    public java.util.Collection getAuthorizedVisibleServiceIds(org.ejbca.core.model.log.Admin admin) throws java.rmi.RemoteException;

    /**
     * Method creating a hashmap mapping service id (Integer) to service name
     * (String).
     */
    public java.util.HashMap getServiceIdToNameMap(org.ejbca.core.model.log.Admin admin) throws java.rmi.RemoteException;

    /**
     * Retrives a named service.
     * 
     * @returns the service configuration or null if it doesn't exist.
     */
    public org.ejbca.core.model.services.ServiceConfiguration getService(org.ejbca.core.model.log.Admin admin, java.lang.String name)
            throws java.rmi.RemoteException;

    /**
     * Finds a service configuration by id.
     * 
     * @returns the service configuration or null if it doesn't exist.
     */
    public org.ejbca.core.model.services.ServiceConfiguration getServiceConfiguration(org.ejbca.core.model.log.Admin admin, int id)
            throws java.rmi.RemoteException;

    /**
     * Returns a service id, given it's service name
     * 
     * @return the id or 0 if the service cannot be found.
     */
    public int getServiceId(org.ejbca.core.model.log.Admin admin, java.lang.String name) throws java.rmi.RemoteException;

    /**
     * Returns a Service name given its id.
     * 
     * @return the name or null if id doesnt exists
     * @throws EJBException
     *             if a communication or other error occurs.
     */
    public java.lang.String getServiceName(org.ejbca.core.model.log.Admin admin, int id) throws java.rmi.RemoteException;

    /**
     * Activates the timer for a named service. The service must already be
     * previously added.
     * 
     * @param admin
     *            The administrator performing the action
     * @param name
     *            the name of the service for which to activate the timer
     * @throws EJBException
     *             if a communication or other error occurs.
     */
    public void activateServiceTimer(org.ejbca.core.model.log.Admin admin, java.lang.String name) throws java.rmi.RemoteException;

}
