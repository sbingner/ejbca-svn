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

import java.awt.Dimension;
import java.util.List;

import javax.swing.JDialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ciscavate.cjwizard.PageFactory;
import org.ciscavate.cjwizard.StackWizardSettings;
import org.ciscavate.cjwizard.WizardListener;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.ciscavate.cjwizard.pagetemplates.TitledPageTemplate;

/**
 * This class uses a JDialog to hold the wizard. Based on WizardTest found in
 * org.ciscavate.cjwizard
 * 
 * 
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */
public class QueryEndEntitiesWizard extends JDialog
{
	/**
	 * Commons logging log instance
	 */
	private static Log log = LogFactory.getLog(QueryEndEntitiesWizard.class);

	public QueryEndEntitiesWizard()
	{
		// first, build the wizard. The TestFactory defines the
		// wizard content and behavior.
		final SiO2WizardContainer wc = new SiO2WizardContainer(
				new SiO2TestFactory(), new TitledPageTemplate(),
				new StackWizardSettings());
		
		// add a wizard listener to update the dialog titles and notify the
		// surrounding application of the state of the wizard:
		wc.addWizardListener(new WizardListener()
		{
			@Override
			public void onCanceled(List<WizardPage> path,
					WizardSettings settings)
			{
				log.debug("settings: " + wc.getSettings());
				QueryEndEntitiesWizard.this.dispose();
			}

			@Override
			public void onFinished(List<WizardPage> path,
					WizardSettings settings)
			{
				log.debug("settings: " + wc.getSettings());

				/*
				 * WizardSettings settings1 = settings; // wc.getSettings();
				 * System.out.println("onFinished: settings [" +
				 * settings1.toString() + "]"); Set<String> keysSettings =
				 * settings1.keySet(); for (String key : keysSettings) { Object
				 * value = settings1.get(key); System.out.println("\tKey: [" +
				 * key + "]\tValue:[" + value + "] (" + value.getClass() + ")");
				 * }
				 */

				QueryEndEntitiesWizard.this.dispose();
			}

			// If you don't actually need the wizardStateSettings, just use rendering which gets the rest of the information at about the same time.
			@Override
			public void onPageChanged(WizardPage newPage, List<WizardPage> path)
			{
				WizardSettings settings = wc.getSettings();
				
				log.debug("settings: " + settings);

				// Set the dialog title to match the description of the new
				// page:
				QueryEndEntitiesWizard.this.setTitle(newPage.getTitle());
				
				if (newPage instanceof SiO2WizardPage)
				{
					SiO2WizardPage page = (SiO2WizardPage)newPage;
					page.onPageChanged(settings, path, wc.getWizardStateSettings());
				}
			}
		});

		// Set up the standard bookkeeping stuff for a dialog, and
		// add the wizard to the JDialog:
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().add(wc);

		wc.setPreferredSize(new Dimension(800, 600));

		this.pack();
	}

	/**
	 * Implementation of PageFactory to generate the wizard pages needed for the
	 * wizard.
	 */
	private class SiO2TestFactory implements PageFactory
	{

		// To keep things simple, we'll just create an array of wizard pages:
		private final WizardPage[] pages =
		{
				new SelectQueryTypePage(),

				new QueryResultsPage()
		};

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ciscavate.cjwizard.PageFactory#createPage(java.util.List,
		 * org.ciscavate.cjwizard.WizardSettings)
		 */
		@Override
		public WizardPage createPage(List<WizardPage> path,
				WizardSettings settings)
		{
			log.debug("creating page " + path.size());

			// Get the next page to display. The path is the list of all wizard
			// pages that the user has proceeded through from the start of the
			// wizard, so we can easily see which step the user is on by taking
			// the length of the path. This makes it trivial to return the next
			// WizardPage:
			WizardPage page = pages[path.size()];

			// if we wanted to, we could use the WizardSettings object like a
			// Map<String, Object> to change the flow of the wizard pages.
			// In fact, we can do arbitrarily complex computation to determine
			// the next wizard page.

			log.debug("Returning page: " + page);
			return page;
		}

	}
}
