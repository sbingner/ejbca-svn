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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */
public class OptionsPanel extends JPanel
{
	private JLabel jLabelWebServiceURL;
	private JTextField jTextFieldWebServiceURL;
	private JTextField jTextFieldKeyStorePassword;
	private JButton jButtonBrowseKeyStore;
	private JButton jButtonBrowseTrustedKeyStore;
	private JTextField jTextFieldTrustedKeyStorePW;
	private JTextField jTextFieldTrustedKeyStore;
	private JTextField jTextFieldKeyStore;
	private JLabel jLabelTrustedKeyStorePW;
	private JLabel jLabelTrustedKeyStore;
	private JLabel jLabelKeyStorePW;
	private JLabel jLabelKeyStore;
	private JLabel jLabelVerify;
	private JButton jButtonVerify;

	/**
	 * 
	 */
	public OptionsPanel()
	{
		super();

		initGUI();
	}

	private void initGUI()
	{
		try
		{
			{
				GridBagLayout thisLayout = new GridBagLayout();
				this.setLayout(thisLayout);
				{
					jLabelWebServiceURL = new JLabel();
					this.add(jLabelWebServiceURL, new GridBagConstraints(0, 0,
							1, 1, 0.0, 0.0, GridBagConstraints.EAST,
							GridBagConstraints.NONE,
							new Insets(10, 10, 10, 10), 0, 0));
					jLabelWebServiceURL.setText("Web Service URL:");
					jLabelWebServiceURL.setDisplayedMnemonic(KeyEvent.VK_W);
				}
				{
					jTextFieldWebServiceURL = new JTextField();
					jLabelWebServiceURL.setLabelFor(jTextFieldWebServiceURL);
					this.add(jTextFieldWebServiceURL, new GridBagConstraints(1,
							0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, new Insets(10, 10,
									10, 10), 0, 0));
					jTextFieldWebServiceURL
							.setText("https://your.ejbca.server:8443/ejbca/ejbcaws/ejbcaws?wsdl");
				}
				{
					jLabelKeyStore = new JLabel();
					this.add(jLabelKeyStore, new GridBagConstraints(0, 1, 1, 1,
							0.0, 0.0, GridBagConstraints.EAST,
							GridBagConstraints.NONE,
							new Insets(10, 10, 10, 10), 0, 0));
					jLabelKeyStore.setText("Key Store:");
					jLabelKeyStore.setDisplayedMnemonic(KeyEvent.VK_K);
				}
				{
					jTextFieldKeyStore = new JTextField();
					jLabelKeyStore.setLabelFor(jTextFieldKeyStore);
					this.add(jTextFieldKeyStore, new GridBagConstraints(1, 1,
							1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, new Insets(10, 10,
									10, 10), 0, 0));
				}
				jButtonBrowseKeyStore = new JButton();
				jButtonBrowseKeyStore.setText("Browse...");
				jButtonBrowseKeyStore.setMnemonic(KeyEvent.VK_B);
				this.add(jButtonBrowseKeyStore, new GridBagConstraints(2, 1, 1,
						1, 0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10,
								10), 0, 0));
				jButtonBrowseKeyStore.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
					{
						jButtonBrowseKeyStoreActionPerformed(evt);
					}
				});

				{
					jLabelKeyStorePW = new JLabel();
					this.add(jLabelKeyStorePW, new GridBagConstraints(0, 2, 1,
							1, 0.0, 0.0, GridBagConstraints.EAST,
							GridBagConstraints.NONE,
							new Insets(10, 10, 10, 10), 0, 0));
					jLabelKeyStorePW.setText("Key Store Password:");
					jLabelKeyStorePW.setDisplayedMnemonic(KeyEvent.VK_P);
				}
				{
					jTextFieldKeyStorePassword = new JTextField();
					jLabelKeyStorePW.setLabelFor(jTextFieldKeyStorePassword);
					this.add(jTextFieldKeyStorePassword,
							new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER,
									GridBagConstraints.HORIZONTAL, new Insets(
											10, 10, 10, 10), 0, 0));
				}
				{
					jLabelTrustedKeyStore = new JLabel();
					this.add(jLabelTrustedKeyStore, new GridBagConstraints(0,
							3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
							GridBagConstraints.NONE,
							new Insets(10, 10, 10, 10), 0, 0));
					jLabelTrustedKeyStore.setText("Trusted Key Store:");
					jLabelTrustedKeyStore.setDisplayedMnemonic(KeyEvent.VK_T);
				}
				{
					jTextFieldTrustedKeyStore = new JTextField();
					jLabelTrustedKeyStore
							.setLabelFor(jTextFieldTrustedKeyStore);
					this.add(jTextFieldTrustedKeyStore, new GridBagConstraints(
							1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, new Insets(10, 10,
									10, 10), 0, 0));
				}
				{
					jButtonBrowseTrustedKeyStore = new JButton();
					jButtonBrowseTrustedKeyStore.setMnemonic(KeyEvent.VK_B);
					this.add(jButtonBrowseTrustedKeyStore,
							new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER,
									GridBagConstraints.NONE, new Insets(0, 0,
											0, 0), 0, 0));
					jButtonBrowseTrustedKeyStore.setText("Browse...");
					jButtonBrowseTrustedKeyStore
							.addActionListener(new ActionListener()
							{
								public void actionPerformed(ActionEvent evt)
								{
									jButtonBrowseTrustedKeyStoreActionPerformed(evt);
								}
							});
				}
				{
					jLabelTrustedKeyStorePW = new JLabel();
					this.add(jLabelTrustedKeyStorePW, new GridBagConstraints(0,
							4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
							GridBagConstraints.NONE,
							new Insets(10, 10, 10, 10), 0, 0));
					jLabelTrustedKeyStorePW
							.setText("Trusted Key Store Password:");
					jLabelTrustedKeyStorePW.setDisplayedMnemonic(KeyEvent.VK_A);
				}
				{
					jTextFieldTrustedKeyStorePW = new JTextField();
					jLabelTrustedKeyStorePW
							.setLabelFor(jTextFieldTrustedKeyStorePW);
					this.add(jTextFieldTrustedKeyStorePW,
							new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER,
									GridBagConstraints.HORIZONTAL, new Insets(
											10, 10, 10, 10), 0, 0));
				}
				jButtonVerify = new JButton();
				jButtonVerify.setText("Verify");
				this.add(jButtonVerify, new GridBagConstraints(2, 5, 1, 1, 0.0,
						0.0, GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10,
								10), 0, 0));
				jButtonVerify.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
					{
						jButtonVerifyActionPerformed(evt);
					}
				});
				this.add(getJLabelVersion(), new GridBagConstraints(0, 5, 1, 1,
						0.0, 0.0, GridBagConstraints.EAST,
						GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0,
						0));

			}
			{
				jLabelVerify = new JLabel();
				this.add(jLabelVerify, new GridBagConstraints(0, 5, 2, 1, 0.0,
						0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
						new Insets(10, 10, 10, 10), 0, 0));
				jLabelVerify
						.setText("Verify settings provide access to web service:\n");
				jLabelVerify.setLabelFor(jButtonVerify);
				jLabelVerify.setDisplayedMnemonic(KeyEvent.VK_V);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	void load()
	{
		ApplicationProperties props = MainFrame.getProperties();

		String str = props.getUrlWebService();
		if (0 == str.length())
		{
			str = "https://your.ejbca.server:8443/ejbca/ejbcaws/ejbcaws?wsdl";
		}
		jTextFieldWebServiceURL.setText(str);

		jTextFieldKeyStore.setText(props.getKeyStoreStr());
		jTextFieldKeyStorePassword.setText(props.getKeyStorePassword());
		jTextFieldTrustedKeyStore.setText(props.getTrustedKeyStoreStr());
		jTextFieldTrustedKeyStorePW.setText(props.getTrustedKeyStorePassword());
	}

	void store()
	{
		ApplicationProperties props = MainFrame.getProperties();

		props.setUrlWebService(jTextFieldWebServiceURL.getText());
		props.setKeyStoreStr(jTextFieldKeyStore.getText());
		props.setKeyStorePassword(jTextFieldKeyStorePassword.getText());
		props.setTrustedKeyStoreStr(jTextFieldTrustedKeyStore.getText());
		props.setTrustedKeyStorePassword(jTextFieldTrustedKeyStorePW.getText());

		props.store();
	}

	private void jButtonBrowseKeyStoreActionPerformed(ActionEvent evt)
	{
		doBrowse(jTextFieldKeyStore);
	}

	private void jButtonBrowseTrustedKeyStoreActionPerformed(ActionEvent evt)
	{
		doBrowse(jTextFieldTrustedKeyStore);
	}

	private static String strCurrentDir = "";
	private JLabel jLabelVersion;

	private void doBrowse(JTextField jTextFieldFileName)
	{
		JFileChooser fileChooser = new JFileChooser(strCurrentDir);
		String strFileName = jTextFieldFileName.getText();
		if (0 != strFileName.length())
		{
			fileChooser.setSelectedFile(new File(strFileName));
		}

		fileChooser.setAcceptAllFileFilterUsed(false);
		FileFilter jksFileFilter = new FileNameExtensionFilter("Keystore",
				"jks");
		fileChooser.addChoosableFileFilter(jksFileFilter);

		// We use showDialog below instead of showSaveDialog because, on the
		// Mac, the latter does not highlight file names accepted by the
		// FileFilter.
		// We use setDialogTitle because showDialog does not set the title.
		fileChooser.setDialogTitle("Select...");
		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

		while (JFileChooser.APPROVE_OPTION == fileChooser.showDialog(this,
				"Select"))
		{
			File fileSel = fileChooser.getSelectedFile();
			if (null == fileSel)
			{
				JOptionPane.showMessageDialog(null, "No file name selected",
						"Select...", JOptionPane.ERROR_MESSAGE);
				continue;
			}

			// Make sure that appropriate extension is part of the filename.
			// On some platforms (eg, Windows) this shouldn't be necessary, but
			// on the Mac, the FileFilter does not prevent the selection of file
			// names that are not accepted by the filter.
			if (!fileSel.getAbsolutePath().endsWith(".jks"))
			{
				JOptionPane
						.showMessageDialog(
								null,
								"Incorrect file extension; the file must be a \".jks\" file.",
								"Select...", JOptionPane.ERROR_MESSAGE);
				continue;
			}

			File parentDir = fileSel.getParentFile();
			if (null == parentDir)
			{
				JOptionPane.showMessageDialog(null, "No directory selected.",
						"Select...", JOptionPane.ERROR_MESSAGE);
				continue;
			}

			if (!fileSel.exists())
			{
				JOptionPane.showMessageDialog(null,
						"The selected file does not exist.", // or the user does
						// not have
						// permission to
						// read the file
						"Select...", JOptionPane.ERROR_MESSAGE);
				continue;
			}

			strCurrentDir = parentDir.getAbsolutePath();

			jTextFieldFileName.setText(fileSel.getAbsolutePath());
			break;
		}
	}

	private void jButtonVerifyActionPerformed(ActionEvent evt)
	{
		// Create properties to be used with WebServiceConnection
		ApplicationProperties props = new ApplicationProperties();
		props.setUrlWebService(jTextFieldWebServiceURL.getText());
		props.setKeyStoreStr(jTextFieldKeyStore.getText());
		props.setKeyStorePassword(jTextFieldKeyStorePassword.getText());
		props.setTrustedKeyStoreStr(jTextFieldTrustedKeyStore.getText());
		props.setTrustedKeyStorePassword(jTextFieldTrustedKeyStorePW.getText());

		WebServiceConnection ws = new WebServiceConnection(props);

		// First try web service call that doesn't require any
		// authentication.
		if (!ws.test())
		{
			jLabelVersion.setVisible(false);
			JOptionPane
					.showMessageDialog(
							this,
							"The web service url is incorrect or its server cannot be accessed.",
							"Verification Failure", JOptionPane.ERROR_MESSAGE);
			return;
		}
		jLabelVersion.setText("Version: " + ws.getEjbcaVersion());
		jLabelVersion.setVisible(true);

		// If we reach this point, then the url is correct, so next we
		// verify
		// that authentication with the keystores works by making a call
		// that
		// doesn't require any arguments.
		try
		{
			// List<NameAndId> list =
			ws.getAvailableCAs();

			// If none found, an empty list is returned, never null.
			// We assume success if no exception thrown.
		}
		catch (Exception e)
		{
			/*
			JOptionPane
					.showMessageDialog(
							this,
							"Web service authentication failure:  one or more of the keystore settings is invalid or one of the keystores does not provide the appropriate privileges.",
							"Verification Failure", JOptionPane.ERROR_MESSAGE);
			*/
			
			// The following should be more useful information than the above generic message; eg, filename doesn't exist,
			// password is invalid, privileges are incorrect, etc.
			SiO2MessagePanel messagePanel = new SiO2MessagePanel();
			messagePanel.setMessagesAndShow(e);
			messagePanel.setPreferredSize(new Dimension(640, 400));
			JOptionPane.showMessageDialog(this, messagePanel,
					"Verification Error", JOptionPane.ERROR_MESSAGE);

			return;
		}

		// Success
		JOptionPane
				.showMessageDialog(
						this,
						"The web service was successfully accessed using authentication from the keystore.",
						"Verification Success", JOptionPane.INFORMATION_MESSAGE);
	}

	private JLabel getJLabelVersion()
	{
		if (jLabelVersion == null)
		{
			jLabelVersion = new JLabel();
			jLabelVersion.setText("Ejbca version");
			jLabelVersion.setVisible(false);
		}
		return jLabelVersion;
	}
}
