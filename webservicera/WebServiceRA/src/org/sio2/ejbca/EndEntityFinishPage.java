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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.sio2.common.Common;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */
public class EndEntityFinishPage extends SiO2WizardPage
{
	private JTextPane jLabelFinish;
	private JTextPane jTextPaneVerify;
	private JButton jButtonTest;
	private JPanel jPanel1;
	private SiO2MessagePanel messagePanel;
	private String strFileName;
	private String strPW;

	public EndEntityFinishPage()
	{
		super("", "");

		initialize();
	}

	/**
	 * @param title
	 * @param description
	 */
	public EndEntityFinishPage(String title, String description)
	{
		super(title, description);

		initialize();
	}

	/**
	 * This method initializes jLabelSaveAs
	 * 
	 * @return javax.swing.JTextPane
	 */
	private JTextPane getJLabelFinish()
	{
		if (jLabelFinish == null)
		{
			jLabelFinish = new JTextPane();
			jLabelFinish.setEditable(false);
			jLabelFinish.setBackground(this.getBackground());
			jLabelFinish.setText("Finish Message");
		}
		return jLabelFinish;
	}

	private JPanel getJPanel1()
	{
		if (jPanel1 == null)
		{
			jPanel1 = new JPanel();
		}
		return jPanel1;
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
		return true;
	}

	protected boolean onFinish(WizardSettings settings)
	{
		return true;
	}

	private void initialize()
	{
		this.setTitle("Certificate Saved");
		this.setDescription("The end entity certificate was saved to file.");

		GridBagLayout thisLayout = new GridBagLayout();
		this.setLayout(thisLayout);

		getMessagePanel();

		this.add(getJLabelFinish(), new GridBagConstraints(0, 0, 2, 1, 1.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJButtonTest(), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(
						10, 10, 10, 10), 0, 0));
		this.add(getJTextPaneVerify(), new GridBagConstraints(1, 1, 1, 1, 1.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getMessagePanel(), new GridBagConstraints(0, 3, 3, 1, 0.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(getJPanel1(), new GridBagConstraints(0, 3, 1, 1, 0.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));
	}

	/**
	 * This is the last page in the wizard, so we will enable the finish button
	 * and disable the "Next >" button just before the page is displayed:
	 */
	public void rendering(List<WizardPage> path, WizardSettings settings)
	{
		super.rendering(path, settings);
		setFinishEnabled(true);
		setNextEnabled(false);
		setCancelEnabled(false); // No need to confuse things by providing both
		// this and "Finish"

		strFileName = (String) settings.get("FileName");
		strPW = (String) settings.get("Password");

		jLabelFinish
				.setText("The certificate was created and saved as file:\n "
						+ strFileName);

		Boolean bGenerateCertificate = (Boolean) settings
				.get("GenerateCertificate");
		jButtonTest.setVisible(bGenerateCertificate);
		jTextPaneVerify.setVisible(bGenerateCertificate);
		if (bGenerateCertificate)
		{
			this.setTitle("Certificate Saved");
			this.setDescription("The end entity certificate was saved to file.");
		}
		else
		{
			this.setTitle("End Entity Created");
			this.setDescription("The end entity was created.");

			String strUserName = (String) settings.get("UserName");
			jLabelFinish
					.setText("The end entity with user name " + strUserName + " was added to the EJBCA database.");
		}
	}

	private JButton getJButtonTest()
	{
		if (jButtonTest == null)
		{
			jButtonTest = new JButton();
			jButtonTest.setText("Verify");
			jButtonTest.setMnemonic(java.awt.event.KeyEvent.VK_V);
			jButtonTest.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					try
					{
						// Remember to put quotes around the filename (because
						// Windows allows spaces in filenames).
						String strCmdLine = System.getProperty("java.home")
								+ Common.getFileSeparator()
								+ "bin"
								+ Common.getFileSeparator()
								+ "keytool -v -list -keystore "
								+ (Common.isWindows() ? "\"" : "")
								+ strFileName
								+ (Common.isWindows() ? "\"" : "")
								+ " -storepass "
								+ strPW
								+ (strFileName.endsWith(".p12") ? " -storetype PKCS12"
										: "");
						System.out.println(strCmdLine);

						// strCmdLine = System.getProperty("java.home") +
						// Common.getFileSeparator() + "bin" +
						// Common.getFileSeparator() +
						// "keytool -v -list -keystore " +
						// "/Users/danielhorn/Downloads/ATestA/test1.jks" +
						// " -storepass " + "pw1";

						Process p = Runtime.getRuntime().exec(strCmdLine);
						BufferedReader input = new BufferedReader(
								new InputStreamReader(p.getInputStream()));
						String line, output = "";
						while (null != (line = input.readLine()))
						{
							output += line + "\n";
						}
						input.close();

						messagePanel.setMessageAndShow(output);
					}
					catch (Exception err)
					{
						messagePanel.setMessagesAndShow(err);
					}
				}
			});
		}
		return jButtonTest;
	}

	private JTextPane getJTextPaneVerify()
	{
		if (jTextPaneVerify == null)
		{
			jTextPaneVerify = new JTextPane();
			jTextPaneVerify.setBackground(this.getBackground());
			jTextPaneVerify
					.setText("Press the \"Verify\" button to view information about the certificate file.");
			jTextPaneVerify.setEditable(false);
		}
		return jTextPaneVerify;
	}

}
