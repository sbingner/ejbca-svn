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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JCheckBox;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.ejbca.util.passgen.AllPrintableCharPasswordGenerator;
import org.sio2.common.Common;


/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */
public class EndEntityDefinitionPage extends SiO2WizardPage
{
	private JLabel jLabelPassword;
	private JLabel jLabelPasswordConfirm;
	private JLabel jLabelUsername;
	private JPasswordField jPasswordField;
	private JPasswordField jPasswordFieldConfirm;
	private JTextField jTextFieldUsername;
	private JCheckBox jCheckBoxAutoGenPW;
	private JCheckBox jCheckBoxSendNotification;

	private JPanel jPanel1;
	private SiO2MessagePanel messagePanel;

	public EndEntityDefinitionPage()
	{
		super("", "");

		initialize();
	}

	/**
	 * @param title
	 * @param description
	 */
	public EndEntityDefinitionPage(String title, String description)
	{
		super(title, description);

		initialize();
	}

	private JLabel getJLabelPassword()
	{
		if (jLabelPassword == null)
		{
			jLabelPassword = new JLabel();
			jLabelPassword.setText("Password:");
			jLabelPassword.setLabelFor(getJPasswordField());
			jLabelPassword.setDisplayedMnemonic(KeyEvent.VK_P);
			jLabelPassword.setDisplayedMnemonicIndex(0);
		}
		return jLabelPassword;
	}

	private JLabel getJLabelPasswordConfirm()
	{
		if (jLabelPasswordConfirm == null)
		{
			jLabelPasswordConfirm = new JLabel();
			jLabelPasswordConfirm.setText("Confirm Password:");
			jLabelPasswordConfirm.setLabelFor(getJPasswordFieldConfirm());
			jLabelPasswordConfirm.setDisplayedMnemonic(KeyEvent.VK_C);
			jLabelPasswordConfirm.setDisplayedMnemonicIndex(0);
		}
		return jLabelPasswordConfirm;
	}

	/**
	 * This method initializes jLabelUsername
	 * 
	 * @return javax.swing.JLabel
	 */
	private JLabel getJLabelUsername()
	{
		if (jLabelUsername == null)
		{
			jLabelUsername = new JLabel();
			// jLabelUsername.setPreferredSize(new java.awt.Dimension(70, 15));
			// jLabelUsername.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelUsername.setText("User Name:");
			// jLabelUsername.setSize(70, 15);
			jLabelUsername.setLabelFor(getJTextFieldUsername());
			jLabelUsername.setDisplayedMnemonic(KeyEvent.VK_U);
			jLabelUsername.setDisplayedMnemonicIndex(0);
		}
		return jLabelUsername;
	}

	private JPanel getJPanel1()
	{
		if (jPanel1 == null)
		{
			jPanel1 = new JPanel();
		}
		return jPanel1;
	}

	private JPasswordField getJPasswordFieldConfirm()
	{
		if (jPasswordFieldConfirm == null)
		{
			jPasswordFieldConfirm = new JPasswordField();
		}
		return jPasswordFieldConfirm;
	}

	private JPasswordField getJPasswordField()
	{
		if (jPasswordField == null)
		{
			jPasswordField = new JPasswordField();
			jPasswordField.setName("Password");
		}
		return jPasswordField;
	}

	/**
	 * This method initializes jTextFieldUsername
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldUsername()
	{
		if (jTextFieldUsername == null)
		{
			jTextFieldUsername = new JTextField();
			jTextFieldUsername.setName("UserName");
			// jTextFieldUsername.setSize(6, 22);
		}
		return jTextFieldUsername;
	}

	/**
	 * This method initializes messagePanel
	 * 
	 * @return SiO2MessagePanel
	 */
	private SiO2MessagePanel getMessagePanel()
	{
		if (messagePanel == null)
		{
			messagePanel = new SiO2MessagePanel();
			messagePanel.setVisible(false);
		}
		return messagePanel;
	}

	protected boolean validSettings()
	{
		// TODO What type of validation should be performed on the user name
		// besides making sure that it is non-empty?
		if (0 == jTextFieldUsername.getText().length())
		{
			messagePanel.setMessageAndShow("The user name cannot be empty.");

			// Note that the messagePanel stays visible until the next or prev button is pressed.

			// jTextFieldUsername.requestFocus();
			messagePanel.requestFocusInWindow();
			// messagePanel.requestFocus();

			return false;
		}

		// TODO What other type of validation needs to be performed on the
		// password?
		// Do we enforce any strength requirements on the pw here?
		// Is a 0-length password allowable in some profiles?
		if (!Common.equalPasswordFields(jPasswordField, jPasswordFieldConfirm))
		{
			messagePanel
					.setMessagesAndShow(
							"The password fields cannot be empty and must match.",
							"Either a) one or both of the password fields was left empty, or b) the string entered for \"Password\" does not match that entered in the \"Confirm Password\" field.");

			// jPasswordField.requestFocus();
			messagePanel.requestFocusInWindow();
			// messagePanel.requestFocus();

			return false;
		}

		if (jCheckBoxAutoGenPW.isSelected()
				&& !jCheckBoxSendNotification.isSelected())
		{
			messagePanel
					.setMessageAndShow("If the \"Auto-generate Password\" option is selected, then the \"Send Email Notification\" setting must also be selected.");

			// jPasswordField.requestFocus();
			messagePanel.requestFocusInWindow();
			// messagePanel.requestFocus();

			return false;
		}

		// TODO Should we check that the password isn't empty?
		// Or is that allowable?
	
		return true;
	}

	private void initialize()
	{
		this.setTitle("End Entity Credentials");
		this.setDescription("End Entity User Name and Password");

		setLayout(new GridBagLayout());

		int labelConstraint = GridBagConstraints.EAST;

		this.add(getJLabelUsername(), new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, labelConstraint, GridBagConstraints.NONE, new Insets(10,
						10, 10, 10), 0, 0));
		this.add(getJTextFieldUsername(),
				new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10,
								10), 0, 0));
		this.add(getJLabelPassword(), new GridBagConstraints(0, 1, 1, 1, 0.0,
				0.0, labelConstraint, GridBagConstraints.NONE, new Insets(10,
						10, 10, 10), 0, 0));
		this.add(getJPasswordField(), new GridBagConstraints(1, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJLabelPasswordConfirm(), new GridBagConstraints(0, 2, 1, 1,
				0.0, 0.0, labelConstraint, GridBagConstraints.NONE, new Insets(
						10, 10, 10, 10), 0, 0));
		this.add(getJPasswordFieldConfirm(),
				new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10,
								10), 0, 0));
		this.add(getJCheckBoxAutoGenPW(), new GridBagConstraints(0, 3, 2, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJCheckBoxSendNotification(), new GridBagConstraints(0, 4,
				2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));

		this.add(getMessagePanel(), new GridBagConstraints(0, 5, 2, 1, 0.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(getJPanel1(), new GridBagConstraints(0, 5, 1, 1, 0.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));

		jTextFieldUsername.requestFocusInWindow();

		if (Common.debug)
		{
			System.err
					.println("*** Remember to turn off/remove debugging/testing initializers. *** ");
			jTextFieldUsername.setText("TestEndUserName2");
			jPasswordField.setText("pw1");
			jPasswordFieldConfirm.setText("pw1");
		}
	}

	// When using QE values, we need to check if auto-generate password is set
	public void rendering(List<WizardPage> path, WizardSettings settings)
	{
		super.rendering(path, settings);
		
		// Don't call autoGeneratePW is selection is false, because we don't want to clear settings each time this page appears.
		if (jCheckBoxAutoGenPW.isSelected())
		{
			// If the checkbox is selected, this will be called whenever next OR prev is pressed to get to this page.
			autoGeneratePW(true);
		}
	}

	private JCheckBox getJCheckBoxAutoGenPW()
	{
		if (jCheckBoxAutoGenPW == null)
		{
			jCheckBoxAutoGenPW = new JCheckBox();
			jCheckBoxAutoGenPW.setText("Auto-generate Password");
			jCheckBoxAutoGenPW.setName("AutoGeneratePassword");
			jCheckBoxAutoGenPW.setMnemonic(java.awt.event.KeyEvent.VK_A);
			jCheckBoxAutoGenPW.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jCheckBoxAutoGenPWActionPerformed(evt);
				}
			});
		}
		return jCheckBoxAutoGenPW;
	}

	private JCheckBox getJCheckBoxSendNotification()
	{
		if (jCheckBoxSendNotification == null)
		{
			jCheckBoxSendNotification = new JCheckBox();
			jCheckBoxSendNotification.setText("Send Email Notification");
			jCheckBoxSendNotification.setName("SendEmailNotification");
			jCheckBoxSendNotification.setMnemonic(java.awt.event.KeyEvent.VK_E);
			jCheckBoxSendNotification.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jCheckBoxSendNotificationActionPerformed(evt);
				}
			});
		}
		return jCheckBoxSendNotification;
	}

	private void jCheckBoxAutoGenPWActionPerformed(ActionEvent evt)
	{
		if (jCheckBoxAutoGenPW.isSelected())
		{
			messagePanel
					.setMessageAndShow(
							"If the \"Auto-generate Password\" option is selected, make sure that the \"Send Email Notification\" option is also selected so that the end user can be emailed the password after the keystore is created.\n\n"
									+ "Select the \"Send Email Notification\" checkbox to see more information.",
							false);

			autoGeneratePW(true);
		}
		else
		{
			messagePanel.setVisible(false);

			autoGeneratePW(false);
		}
	}

	private void autoGeneratePW(boolean bEnable)
	{
		jPasswordField.setEnabled(!bEnable);
		jPasswordFieldConfirm.setEnabled(!bEnable);

		if (bEnable)
		{
			// As done in EjbcaWS.java:
			AllPrintableCharPasswordGenerator gen = new AllPrintableCharPasswordGenerator();
			String password = gen.getNewPassword(15, 20);
			jPasswordField.setText(password);
			jPasswordFieldConfirm.setText(password);

			jCheckBoxSendNotification.setSelected(true);
		}
		else
		{
			jPasswordField.setText("");
			jPasswordFieldConfirm.setText("");
		}
	}

	private void jCheckBoxSendNotificationActionPerformed(ActionEvent evt)
	{
		if (jCheckBoxSendNotification.isSelected())
		{
			messagePanel
					.setMessageAndShow(
							"If the \"Send Email Notification\" option is selected, then email can be configured to send the user name and password to an appropriate recipient.\n\n"
									+ "If the end entity profile does not have the \"Send Notification\" checkbox selected, an exception (UserDoesntFullfillEndEntityProfile_Exception) will occur.\n\n"
									+ "If the user does not receive the email notification, verify with the application server's administrator that "
									+ "the appserver is correctly configured to send email and that the end entity profile is configured to send email to the end user.\n\n"
									+ "Also, the end entity profile's \"Notification Events\" list should include (at minimum) the \"STATUSNEW\" selection."
									,
							false);
			messagePanel.scrollToTop();
		}
		else
		{
			messagePanel.setVisible(false);
		}
	}

}
