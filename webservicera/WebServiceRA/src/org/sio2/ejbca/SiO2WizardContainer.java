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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.ciscavate.cjwizard.PageFactory;
import org.ciscavate.cjwizard.WizardContainer;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.ciscavate.cjwizard.pagetemplates.PageTemplate;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
*/
public class SiO2WizardContainer extends WizardContainer implements
		AncestorListener
{
	public static final String NAVIGATION_BUTTON_PRESSED = "NavigationButtonPressed";

	private JButton buttonCancel = null;
	private JButton buttonNext = null;
	private JButton buttonPrev = null;

	public SiO2WizardContainer(PageFactory factory)
	{
		super(factory);

		initialize();
	}

	public SiO2WizardContainer(PageFactory factory, PageTemplate template,
			WizardSettings settings)
	{
		super(factory, template, settings);

		initialize();
	}

	private void initialize()
	{
		addAncestorListener(this);
	}

	// Use this to store settings that shouldn't change based on the current page viewed (as happens with WizardSettings).
	private Map<String, Object> wizardStateSettings = new HashMap<String, Object>();

	Map<String, Object> getWizardStateSettings()
	{
		return wizardStateSettings;
	}

	// We keep track of which button was pressed to get to the current screen of
	// the wizard in case we need to distinguish between navigation
	// with Prev and Next.
	enum ButtonPressed
	{
		BUTTON_UNKNOWN, BUTTON_NEXT, BUTTON_PREV
	};

	/**
	 * Provide a place to avoid moving to the next page is validation fails.
	 */
	// TODO Do we also want to validate on prev?
	// Possibly not, as they may need to go back to a previous page to fix
	// current problem?
	// TODO Do we also want to validate on finish?
	// Probably, because we want to validate before actually finishing.
	public void next()
	{
		WizardPage lastPage = currentPage();

		if (SiO2WizardPage.class.isInstance(lastPage))
		{
			SiO2WizardPage page = (SiO2WizardPage) lastPage;
			if (!page.validSettings())
			{
				return;
			}

			WizardSettings settings = getSettings();
			page.updateSettings(settings);

			if (!page.postValidationActions(settings))
			{
				return;
			}
		}

		super.next();
	}

	public void finish()
	{
		WizardPage lastPage = currentPage();

		if (SiO2WizardPage.class.isInstance(lastPage))
		{
			SiO2WizardPage page = (SiO2WizardPage) lastPage;
			if (!page.validSettings())
			{
				return;
			}

			// In base class, settings for last page aren't saved, so the
			// following takes care of that.
			WizardSettings settings = getSettings();
			page.updateSettings(settings);

			// Do actual finish work in last page so that any error messages
			// will appear on the page and the user has a chance to fix them.
			if (!page.onFinish(settings))
			{
				return;
			}
		}

		super.finish();
	}

	/*
	 * This is a clunky way of finding the cancel button. However, as the base
	 * class makes it private information, this seems to be the easiest way to
	 * find it without requiring a change to the source code of the base class.
	 */
	JButton getCancelButton()
	{
		if (null == buttonCancel)
		{
			String buttonLabel = "Cancel";
			buttonCancel = getButton(buttonLabel);
		}

		return buttonCancel;
	}

	JButton getNextButton()
	{
		if (null == buttonNext)
		{
			String buttonLabel = "Next >";
			buttonNext = getButton(buttonLabel);
		}

		return buttonNext;
	}

	JButton getPrevButton()
	{
		if (null == buttonPrev)
		{
			String buttonLabel = "< Prev";
			buttonPrev = getButton(buttonLabel);
		}

		return buttonPrev;
	}

	private JButton getButton(String buttonLabel)
	{
		int numComponents = getComponentCount();
		for (int i = 0; i < numComponents; i++)
		{
			Component comp = getComponent(i);

			if (comp instanceof JPanel)
			{
				JPanel panel = (JPanel) comp;
				int num = panel.getComponentCount();

				for (int j = 0; j < num; j++)
				{
					Component comp1 = panel.getComponent(j);

					if (comp1 instanceof JButton)
					{
						JButton button = (JButton) comp1;
						String text = button.getText();
						if (text.equals(buttonLabel))
						{
							return button;
						}
					}
				}
			}
		}

		// In practice, we should never reach here.
		assert (false);

		return null;
	}

	public void setCancelEnabled(boolean enabled)
	{
		getCancelButton().setEnabled(enabled);
	}

	@Override
	public void ancestorAdded(AncestorEvent event)
	{
		// Use this method to add following functionality because we are assured
		// that the buttons exist by this point.

		JButton nextButton = getNextButton();
		nextButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
//				System.out.println("*** Next pressed");

				wizardStateSettings.put(NAVIGATION_BUTTON_PRESSED,
						ButtonPressed.BUTTON_NEXT);
			}
		});

		JButton prevButton = getPrevButton();
		prevButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
//				System.out.println("*** Prev pressed");

				wizardStateSettings.put(NAVIGATION_BUTTON_PRESSED,
						ButtonPressed.BUTTON_PREV);
			}
		});
	}

	@Override
	public void ancestorMoved(AncestorEvent event)
	{
	}

	@Override
	public void ancestorRemoved(AncestorEvent event)
	{
	}

}
