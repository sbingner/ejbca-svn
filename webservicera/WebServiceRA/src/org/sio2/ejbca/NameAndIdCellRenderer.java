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

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.ejbca.core.protocol.ws.client.gen.NameAndId;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */
public class NameAndIdCellRenderer extends BasicComboBoxRenderer
{

	/**
	 * 
	 */
	public NameAndIdCellRenderer()
	{
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus)
	{
		super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);

		// If value isn't of expected type, just behave like the base class
		// version.
		if (value instanceof NameAndId)
		{
			NameAndId name = (NameAndId) value;
			if (null != name)
			{
				setText(getNameAndIdString(name));
			}
		}

		return this;
	}

	static String getNameAndIdString(NameAndId name)
	{
		return name.getName() + " (Id: " + name.getId() + ")";
	}
}
