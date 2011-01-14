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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.ejbca.core.model.AlgorithmConstants;
import org.ejbca.core.protocol.ws.client.gen.CertificateResponse;
import org.ejbca.core.protocol.ws.client.gen.NameAndId;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.client.gen.UserMatch;
import org.ejbca.core.protocol.ws.common.CertificateHelper;
import org.ejbca.util.Base64;
import org.ejbca.util.CertTools;
import org.ejbca.util.keystore.KeyTools;
import org.sio2.common.Common;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
*/
public class EndEntitySaveAsPage extends SiO2WizardPage
{
	private JLabel jLabelFileName;
	private JCheckBox jCheckBoxGenerateCertificate;
	private JComboBox jComboBoxKeySpec;
	private JLabel jLabelKeySpec;
	private JButton jButtonBrowse;
	private JTextField jTextFieldFileName;
	private JButton jButtonQESaveAs;
	private JTextPane jTextPaneQE;

	private JPanel jPanel1;
	private SiO2MessagePanel messagePanel;

	public EndEntitySaveAsPage()
	{
		super("", "");

		initialize();
	}

	/**
	 * @param title
	 * @param description
	 */
	public EndEntitySaveAsPage(String title, String description)
	{
		super(title, description);

		initialize();
	}

	private JLabel getJLabelFileName()
	{
		if (jLabelFileName == null)
		{
			jLabelFileName = new JLabel();
			jLabelFileName.setText("File Name:");
			jLabelFileName.setLabelFor(getJTextFieldFileName());
			jLabelFileName.setDisplayedMnemonic(KeyEvent.VK_N);
			jLabelFileName.setDisplayedMnemonicIndex(0);
		}
		return jLabelFileName;
	}

	private JPanel getJPanel1()
	{
		if (jPanel1 == null)
		{
			jPanel1 = new JPanel();
		}
		return jPanel1;
	}

	private JTextField getJTextFieldFileName()
	{
		if (jTextFieldFileName == null)
		{
			jTextFieldFileName = new JTextField();
			jTextFieldFileName.setName("FileName");
		}
		return jTextFieldFileName;
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

	// TODO Move settings into base class?
	private WizardSettings settings = null;

	public void rendering(List<WizardPage> path, WizardSettings settings)
	{
		super.rendering(path, settings);
		setFinishEnabled(false);
		setCancelEnabled(true); // It may have been disabled when we reached the
		// finish page, so we have to re-enable it in
		// case we go to the previous page from the
		// final page.

		this.settings = settings;
	}

	protected boolean validSettings()
	{
		// TODO What type of validation should be performed on the file name
		// besides making sure that it is non-empty?
		// eg, if it already exists, is it an error or do we ask the user if
		// he/she wants to overwrite?
		if (jCheckBoxGenerateCertificate.isSelected())
		{
			String strFileName = jTextFieldFileName.getText();
			if (0 == strFileName.length())
			{
				messagePanel.setMessageAndShow("No file name was selected.");

				return false;
			}

			// Check that only of the expected file extensions is present.
			if (!(strFileName.endsWith(".p12") || strFileName.endsWith(".jks")))
			{
				messagePanel
						.setMessageAndShow("The file name selected must be of type \".p12\" or \".jks\".");

				return false;
			}
		}
		return true;
	}

	protected boolean postValidationActions(WizardSettings settings)
	{
		return generateCertificate(settings);
	}

	private boolean generateCertificate(WizardSettings settings)
	{
		// Generate certificate.
		UserDataVOWS user1 = new UserDataVOWS();
		user1.setUsername((String) settings.get("UserName"));

		// TODO Can a common name have spaces?
		String strDN = "CN=" + (String) settings.get("CommonName");

		String strFirstName = (String) settings.get("FirstName");
		if ((null != strFirstName) && (0 != strFirstName.length()))
		{
			strDN += ",givenName=" + strFirstName;
		}

		String strLastName = (String) settings.get("LastName");
		if ((null != strLastName) && (0 != strLastName.length()))
		{
			strDN += ",surName=" + strLastName;
		}

		String strOU = (String) settings.get("OrganizationUnit");
		if ((null != strOU) && (0 != strOU.length()))
		{
			strDN += ",OU=" + strOU;
		}

		String strSerialNumber = (String) settings.get("SerialNumber");
		if ((null != strSerialNumber) && (0 != strSerialNumber.length()))
		{
			strDN += ",serialNumber=" + strSerialNumber;
		}

		user1.setSubjectDN(strDN);

		String strCA = ((NameAndId) settings.get("CertificateAuthority"))
				.getName();
		user1.setCaName(strCA);

		user1.setEmail((String) settings.get("Email"));
		// TODO Do we want to expose these as options in the UI?
		user1.setSubjectAltName(null);

		String strEndEntityProfile = ((NameAndId) settings
				.get("EndEntityProfile")).getName();
		user1.setEndEntityProfileName(strEndEntityProfile);

		String strCertificateProfile = ((NameAndId) settings
				.get("CertificateProfile")).getName();
		user1.setCertificateProfileName(strCertificateProfile);

		user1.setPassword((String) settings.get("Password"));

		String strFileName = (String) settings.get("FileName");
		if (strFileName.endsWith(".p12"))
		{
			user1.setTokenType(UserDataVOWS.TOKEN_TYPE_P12);
		}
		else if (strFileName.endsWith(".jks"))
		{
			user1.setTokenType(UserDataVOWS.TOKEN_TYPE_JKS);
		}
		else if (!jCheckBoxGenerateCertificate.isSelected())
		{
			// strFileName could be empty string in this case, so just set token type to something.
			// TODO Does this choice matter here?
			// Or only that it is not null?
			user1.setTokenType(UserDataVOWS.TOKEN_TYPE_P12);
		}
		else
		{
			// Should never reach this line unless we add a new type and
			// forget to update this block of code.
			assert (false);
		}

		user1.setSendNotification((Boolean) settings
				.get("SendEmailNotification")); // defaults to false if not set

		// TODO Make this an option or choose most likely setting?
		// clearPwd indicates if the password should be stored in clear text,
		// required when creating server generated keystores.
		user1.setClearPwd(false);

		// TODO Which of the following, if any, do we want the user to
		// be able to customize?
		/*
		 * user1.setStartTime(startTime); // defaults to null
		 * user1.setEndTime(endTime); // defaults to null
		 * user1.setKeyRecoverable(keyrecoverable); // defaults to false
		 * user1.setStatus(status); // defaults to 0 which is not in the list of
		 * statuses; what does this mean? Probably doesn't matter when we are
		 * creating a new user, so status is set to new.
		 * 
		 * user1.setHardTokenIssuerName(hardTokenIssuerName); // defaults to
		 * null, meaning no hard token issuer used.
		 */

		WebServiceConnection ws = new WebServiceConnection();

		try
		{
			System.out.println("Saving " + user1.getTokenType());

			{
				String strKeySpec = (String) jComboBoxKeySpec.getSelectedItem();

				// If checkbox is not selected, add user without generating
				// certificate
				if (!jCheckBoxGenerateCertificate.isSelected())
				{
					// Use findUser first to see if the user already exists
					// to avoid overwriting existing user.
					UserMatch userMatch = new UserMatch(
							UserMatch.MATCH_WITH_USERNAME,
							UserMatch.MATCH_TYPE_EQUALS, user1.getUsername());
					java.util.List<UserDataVOWS> listUser = ws
							.findUser(userMatch);
					if ((null != listUser) && (0 < listUser.size()))
					{
						if (JOptionPane.YES_OPTION != JOptionPane
								.showConfirmDialog(
										this,
										"An end entity with user name\n"
												+ user1.getUsername()
												+ "\nalready exists.\n\nDo you wish to overwrite its settings and reset its status to \"New\"?",
										"User Name Already Exists",
										JOptionPane.YES_NO_CANCEL_OPTION,
										JOptionPane.WARNING_MESSAGE))
						{
							return false;
						}
					}
					user1.setStatus(UserDataVOWS.STATUS_NEW);
					ws.editUser(user1);

					return true;
				}

				// TODO Should key algorithm be an option?
				// For now, assume RSA and SHA1WithRSA.
				KeyPair keys = KeyTools.genKeys(strKeySpec,
						AlgorithmConstants.KEYALGORITHM_RSA);

				PKCS10CertificationRequest pkcs10 = new PKCS10CertificationRequest(
				// TODO What should argument be used here? Is SubjectDN the
						// right thing?
						"SHA1WithRSA", CertTools.stringToBcX509Name(user1
								.getSubjectDN()), keys.getPublic(), null, keys
								.getPrivate());

				CertificateResponse certenv = ws.certificateRequest(user1,
						new String(Base64.encode(pkcs10.getEncoded())),
						CertificateHelper.CERT_REQ_TYPE_PKCS10, null,
						CertificateHelper.RESPONSETYPE_CERTIFICATE);

				X509Certificate cert = certenv.getCertificate();
				java.security.KeyStore jks = java.security.KeyStore
						.getInstance(user1.getTokenType().equals("JKS") ? "JKS"
								: "pkcs12");
				jks.load(null, user1.getPassword().toCharArray());

				java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory
						.getInstance("X.509");
				java.security.cert.Certificate cert1 = cf
						.generateCertificate(new ByteArrayInputStream(cert
								.getEncoded()));

				java.security.cert.Certificate[] certs = new java.security.cert.Certificate[1];
				certs[0] = cert1;

				// Following logic used in EjbcaWS.java, the alias is the common
				// name, if present, and otherwise, is the username.
				String alias = CertTools.getPartFromDN(user1.getSubjectDN(),
						"CN");
				if (alias == null)
				{
					alias = user1.getUsername();
				}

				FileOutputStream out = new FileOutputStream(strFileName);

				// storing keystore
				java.security.PrivateKey ff = keys.getPrivate();

				jks.setKeyEntry(alias, ff, user1.getPassword().toCharArray(),
						certs);
				jks.store(out, user1.getPassword().toCharArray());
				out.close();
			}

			if (Common.debug)
			{
				System.out.println("File " + strFileName + " created.");
			}
		}
		catch (Exception e)
		{
			if (Common.debug)
			{
				e.printStackTrace();
			}

			messagePanel.setMessagesAndShow(e);
			return false;
		}

		return true;
	}

	private void initialize()
	{
		this.setTitle("Add End Entity");
		this
				.setDescription("If you also wish to generate the end entity's certificate now, select the file type and file name for the certificate.");

		setLayout(new GridBagLayout());

		getMessagePanel();

		this.add(getJCheckBoxGenerateCertificate(), new GridBagConstraints(0,
				0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJLabelKeySpec(), new GridBagConstraints(0, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJLabelFileName(), new GridBagConstraints(0, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJTextFieldFileName(),
				new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10,
								10), 0, 0));
		this.add(getJButtonBrowse(), new GridBagConstraints(2, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJTextPaneQE(), new GridBagConstraints(0, 5, 2, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJButtonQESaveAs(), new GridBagConstraints(2, 5, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));

		this.add(getMessagePanel(), new GridBagConstraints(0, 6, 3, 1, 0.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(getJPanel1(), new GridBagConstraints(0, 6, 1, 1, 0.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(getJComboBoxKeySpec(), new GridBagConstraints(1, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 10, 10), 0, 0));

		jCheckBoxGenerateCertificateActionPerformed(null);
	}

	private JButton getJButtonBrowse()
	{
		if (jButtonBrowse == null)
		{
			jButtonBrowse = new JButton();
			jButtonBrowse.setText("Browse...");
			jButtonBrowse.setMnemonic(java.awt.event.KeyEvent.VK_B);
			jButtonBrowse.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jButtonBrowseActionPerformed(evt);
				}
			});
		}
		return jButtonBrowse;
	}

	private JTextPane getJTextPaneQE()
	{
		if (jTextPaneQE == null)
		{
			jTextPaneQE = new JTextPane();
			jTextPaneQE.setBackground(this.getBackground());
			jTextPaneQE
					.setText("Press the \"QE Save As\" button to use the current settings for \"Quick Enrollment\".");
			jTextPaneQE.setEditable(false);
		}
		return jTextPaneQE;
	}

	private JButton getJButtonQESaveAs()
	{
		if (jButtonQESaveAs == null)
		{
			jButtonQESaveAs = new JButton();
			jButtonQESaveAs.setText("QE Save As...");
			jButtonQESaveAs.setMnemonic(java.awt.event.KeyEvent.VK_Q);
			jButtonQESaveAs.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jButtonQESaveAsActionPerformed(evt);
				}
			});
		}
		return jButtonQESaveAs;
	}

	private static String strCurrentDir = "";

	private void jButtonBrowseActionPerformed(ActionEvent evt)
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
		// Add the p12 file filter second to make it the default.
		FileFilter p12FileFilter = new FileNameExtensionFilter("P12 File",
				"p12");
		fileChooser.addChoosableFileFilter(p12FileFilter);

		// We use showDialog below instead of showSaveDialog because, on the
		// Mac, the latter does not highlight file names accepted by the
		// FileFilter.
		// We use setDialogTitle because showDialog does not set the title.
		fileChooser.setDialogTitle("Save As...");
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);

		while (JFileChooser.APPROVE_OPTION == fileChooser.showDialog(this,
				"Save"))
		{
			File fileSel = fileChooser.getSelectedFile();
			if (null == fileSel)
			{
				JOptionPane.showMessageDialog(null, "No file name selected",
						"Save As...", JOptionPane.ERROR_MESSAGE);
				continue;
			}

			// Make sure that appropriate extension is part of the filename.
			// On some platforms (eg, Windows) this shouldn't be necessary, but
			// on the Mac, the FileFilter does not prevent the selection of file
			// names that are not accepted by the filter.
			if (!(fileSel.getAbsolutePath().endsWith(".p12") || fileSel
					.getAbsolutePath().endsWith(".jks")))
			{
				JOptionPane
						.showMessageDialog(
								null,
								"Incorrect file extension; the file must be a \".p12\" or \".jks\" file.",
								"Save As...", JOptionPane.ERROR_MESSAGE);
				continue;
			}

			File parentDir = fileSel.getParentFile();
			if (null == parentDir)
			{
				JOptionPane.showMessageDialog(null, "No directory selected.",
						"Save As...", JOptionPane.ERROR_MESSAGE);
				continue;
			}

			if (fileSel.exists())
			{
				if (JOptionPane.YES_OPTION != JOptionPane
						.showConfirmDialog(
								this,
								"File with name\n"
										+ fileSel.toString()
										+ "\nalready exists.\n\nSave using this name anyway?",
								"File Already Exists",
								JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.WARNING_MESSAGE))
				{
					return;
				}
			}

			strCurrentDir = parentDir.getAbsolutePath();

			jTextFieldFileName.setText(fileSel.getAbsolutePath());
			break;
		}
	}

	private void jButtonQESaveAsActionPerformed(ActionEvent evt)
	{
		// TODO Any other things that need to go into the QE?

		QuickEnrollmentInfo info = new QuickEnrollmentInfo(settings);

		// Any possible changes to the QE on this page have to be saved now.
		String strKeySpec = (String) jComboBoxKeySpec.getSelectedItem();
		info.setKeySpec(strKeySpec);
		info.setGenerateCertificate(jCheckBoxGenerateCertificate.isSelected());

		QESaveAsPanel panel = new QESaveAsPanel();
		// Use the end entity profile name as the default description.
		panel.setInformation(info.toVerboseString(), info.getDescription());

		while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this,
				panel, "Save Quick Enrollment Settings",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
		{
			// Check that description is not empty.
			if (!panel.isDescriptionValid())
			{
				JOptionPane.showMessageDialog(this,
						"No description was provided.", "Error",
						JOptionPane.ERROR_MESSAGE);
				continue;
			}

			String strFilename = panel.getFilename();

			// Check that filename field is not empty.
			if (0 == strFilename.length())
			{
				JOptionPane.showMessageDialog(this,
						"No file name was provided.", "Error",
						JOptionPane.ERROR_MESSAGE);
				continue;
			}

			File file = new File(strFilename);

			// If the file already exists, the user must verify that it is ok to
			// overwrite it.
			if (file.exists())
			{
				if (JOptionPane.YES_OPTION != JOptionPane
						.showConfirmDialog(
								this,
								"File with name\n"
										+ file.toString()
										+ "\nalready exists.\n\nSave using this name anyway?",
								"File Already Exists",
								JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.WARNING_MESSAGE))
				{
					continue;
				}
			}

			// TODO Handle case when invalid filename provided.

			info.setDescription(panel.getDescription());
			info.setFile(file);
			info.createFile();

			break;
		}
	}

	private JLabel getJLabelKeySpec()
	{
		if (jLabelKeySpec == null)
		{
			jLabelKeySpec = new JLabel();
			jLabelKeySpec.setText("Key Specification (Bits):");
			jLabelKeySpec.setDisplayedMnemonic(KeyEvent.VK_K);
			jLabelKeySpec.setDisplayedMnemonicIndex(0);
			jLabelKeySpec.setLabelFor(getJComboBoxKeySpec());
		}
		return jLabelKeySpec;
	}

	private JComboBox getJComboBoxKeySpec()
	{
		if (jComboBoxKeySpec == null)
		{
			jComboBoxKeySpec = new JComboBox();
			jComboBoxKeySpec.setName("KeySpec");
			ComboBoxModel jComboBoxKeySpecModel = new DefaultComboBoxModel(
					new String[]
					{
							"1024", "2048", "4096"
					});
			jComboBoxKeySpec.setModel(jComboBoxKeySpecModel);
			jComboBoxKeySpec.setSelectedIndex(1); // Set 2048 as the default.
		}
		return jComboBoxKeySpec;
	}

	private JCheckBox getJCheckBoxGenerateCertificate()
	{
		if (jCheckBoxGenerateCertificate == null)
		{
			jCheckBoxGenerateCertificate = new JCheckBox();
			jCheckBoxGenerateCertificate.setName("GenerateCertificate");
			jCheckBoxGenerateCertificate
					.setText("Generate certificate for end entity");
			jCheckBoxGenerateCertificate.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jCheckBoxGenerateCertificateActionPerformed(evt);
				}
			});
		}
		return jCheckBoxGenerateCertificate;
	}

	private void jCheckBoxGenerateCertificateActionPerformed(ActionEvent evt)
	{
		boolean bEnable = jCheckBoxGenerateCertificate.isSelected();

		jLabelKeySpec.setEnabled(bEnable);
		jComboBoxKeySpec.setEnabled(bEnable);
		jLabelFileName.setEnabled(bEnable);
		jTextFieldFileName.setEnabled(bEnable);
		jButtonBrowse.setEnabled(bEnable);

		if (bEnable)
		{
			messagePanel
					.setMessageAndShow(
							"By default, the end entity is added to the EJBCA database, enabling a user to later create a certificate using EJBCA's public web interface.\n\n"
									+ "Select the checkbox only if you wish to generate the certificate now.  "
									+ "(In this latter case, note that the end entity profile may need to be modified to expose the \"Batch generation (clear text pwd storage)\" option.)",
							false);
			messagePanel.scrollToTop();
		}
		else
		{
			messagePanel.setVisible(false);
		}
	}

}
