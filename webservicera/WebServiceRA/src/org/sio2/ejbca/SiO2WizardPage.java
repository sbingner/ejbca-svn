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

import java.awt.Container;
import java.util.List;
import java.util.Map;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
*/
public class SiO2WizardPage extends org.ciscavate.cjwizard.WizardPage
{
	// Define here because versions of these in superclass do not have setters
	// outside of constructor
	private String _title;
	private String _description;

	/**
	 * Gets the short 1-2 word description of this WizardPage
	 * 
	 * @return The WizardPage title
	 */
	public String getTitle()
	{
		return _title;
	}

	public void setTitle(String title)
	{
		this._title = title;
	}

	/**
	 * Gets a longer description of this WizardPage.
	 * 
	 * @return The WizardPage description.
	 */
	public String getDescription()
	{
		return _description;
	}

	public void setDescription(String description)
	{
		this._description = description;
	}

	/**
	 * @param title
	 * @param description
	 */
	public SiO2WizardPage(String title, String description)
	{
		super(title, description);
	}

	public SiO2WizardPage()
	{
		super("", "");

		// Remember to set the title and description in derived classes.
	}

	// Use this method to validate before moving to the next page.
	// If not valid, don't change page.
	protected boolean validSettings()
	{
		return true;
	}

	// Use this method to perform any actions that are needed after validation
	// but before moving to the next page.
	// In particular, these actions may need to know the values in
	// WizardSettings.
	// If it doesn't return true valid, don't change page.
	protected boolean postValidationActions(WizardSettings settings)
	{
		return true;
	}
	
	public void onPageChanged(WizardSettings settings, List<WizardPage> path, Map<String, Object> wizardStateSettings)
	{
//		System.out.println(path);
	}

	protected boolean onFinish(WizardSettings settings)
	{
		return true;
	}

	protected void setCancelEnabled(boolean enabled)
	{
		Container container = getParent();
		while (null != container)
		{
			if (container instanceof SiO2WizardContainer)
			{
				SiO2WizardContainer controller = (SiO2WizardContainer) container;
				controller.setCancelEnabled(enabled);
				break;
			}
			container = container.getParent();
		}
	}

	// Pressing "Prev" from the last page should automatically disable the "Finish" button, so we do it here.
	// Enable the "Finish" button in the overwritten version of this method when in the final page.
	public void rendering(List<WizardPage> path, WizardSettings settings)
	{
		super.rendering(path, settings);

		setFinishEnabled(false);
	}

}
