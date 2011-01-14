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
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */
public class QuickEnrollmentTypePage extends SiO2WizardPage
{
	private JLabel jLabelEndEntityProfile;

	private SiO2MessagePanel messagePanel;
	private JPanel jPanel1;
	private JLabel jLabel1;
	private JButton jButtonBrowse;
	private JTextField jTextFieldDescription;

	/**
	 * @param title
	 * @param description
	 */
	public QuickEnrollmentTypePage()
	{
		super("", "");

		initialize();
	}

	private void initialize()
	{
		this.setTitle("Quick Enrollment Profile");
		this
				.setDescription("The type of certificate to create is determined by the selection of quick enrollment profile.");

		setLayout(new GridBagLayout());

		// Make sure that messagePanel is created before the other items because
		// an exception
		// may be thrown in initializing them (eg, if the connection to the
		// server fails).
		getMessagePanel();

		this.add(getJLabelEndEntityProfile(), new GridBagConstraints(0, 0, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJTextFieldDescription(),
				new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10,
								10), 0, 0));
		this.add(getJButtonBrowse(), new GridBagConstraints(0, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJLabel1(), new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 10, 10), 0, 0));

		this.add(getMessagePanel(), new GridBagConstraints(0, 9, 2, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(getJPanel1(), new GridBagConstraints(0, 9, 2, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

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

	private JPanel getJPanel1()
	{
		if (jPanel1 == null)
		{
			jPanel1 = new JPanel();
		}
		return jPanel1;
	}

	private JLabel getJLabelEndEntityProfile()
	{
		if (jLabelEndEntityProfile == null)
		{
			jLabelEndEntityProfile = new JLabel();
			jLabelEndEntityProfile.setText("Description:");
		}
		return jLabelEndEntityProfile;
	}

	private JTextField getJTextFieldDescription()
	{
		if (jTextFieldDescription == null)
		{
			jTextFieldDescription = new JTextField();
			jTextFieldDescription.setName("QEDescription");
		}
		return jTextFieldDescription;
	}

	private JButton getJButtonBrowse()
	{
		if (jButtonBrowse == null)
		{
			jButtonBrowse = new JButton();
			jButtonBrowse.setText("Browse...");
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

	private JLabel getJLabel1()
	{
		if (jLabel1 == null)
		{
			jLabel1 = new JLabel();
			jLabel1
					.setText("Press \"Browse\" to select a different Quick Enrollment (QE) profile.");
		}
		return jLabel1;
	}

	void assignQE()
	{
		ApplicationProperties props = MainFrame.getProperties();
		String str = props.getProperty("DefaultQEProfile", "");

		// TODO Check that this works even if str is an invalid filename (eg, the empty string).
		assignQE(new File(str));
	}
	
	private boolean assignQE(File file)
	{
		jTextFieldDescription.setText("");

		QuickEnrollmentInfo info = QuickEnrollmentInfo.loadFromFile(file);
		if (null != info)
		{
			jTextFieldDescription.setText(info.getDescription());
			info.putToWizardSettings(settings);

			MainFrame.getProperties().put("DefaultQEProfile", info.getFile().getAbsolutePath());

			return true;
		}
		
		return false;
	}

	// We need a reference to the settings to store the QuickEnrollmentInfo
	private WizardSettings settings = null;
	
	public void rendering(List<WizardPage> path, WizardSettings settings)
	{
		super.rendering(path, settings);

		this.settings = settings;

		assignQE();
	}

	private static String strCurrentDir = "";

	private void jButtonBrowseActionPerformed(ActionEvent evt)
	{
		QuickEnrollmentInfo info = (QuickEnrollmentInfo)settings.get("QuickEnrollmentInfo");

		JFileChooser fileChooser = new JFileChooser(strCurrentDir);

		if (null != info)
		{
			fileChooser.setSelectedFile(info.getFile());
		}

		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(QESaveAsPanel.qeFileFilter);

		if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
		{
			return;
		}

		File fileSel = fileChooser.getSelectedFile();
		if (null == fileSel)
		{
			JOptionPane.showMessageDialog(null, "No file name selected",
					"Select QE...", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Make sure that appropriate extension is part of the filename.
		final String strExtension = ".QE";
		if (!fileSel.getAbsolutePath().endsWith(strExtension))
		{
			JOptionPane.showMessageDialog(null, "The file name must end in \".QE\"",
					"Select QE...", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (!fileSel.exists())
		{
			JOptionPane.showMessageDialog(null, "The file does not exist.",
					"Select QE...", JOptionPane.ERROR_MESSAGE);
			return;
		}

		File parentDir = fileSel.getParentFile();
		if (null == parentDir)
		{
			JOptionPane.showMessageDialog(null, "No directory selected.",
					"Select QE...", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (!assignQE(fileSel))
		{
			JOptionPane.showMessageDialog(null, "Error loading QE information.  Invalid file?",
					"Select QE...", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Success if we reach here.
		// The current selection will be set as the default QE.
	}

}
