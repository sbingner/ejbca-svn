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
 *  Copyright (c) PrimeKey Solutions AB.                                 *
 *                                                                       *
 *************************************************************************/
 
package org.sio2.ejbca;

import java.util.Comparator;

import org.ejbca.core.protocol.ws.client.gen.NameAndId;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */
public class NameAndIdComparator implements Comparator<NameAndId>
{
	@Override
	public int compare(NameAndId o1, NameAndId o2)
	{
		return o1.getId() - o2.getId();
	}
}
