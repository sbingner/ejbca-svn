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
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import org.ejbca.core.protocol.ws.client.gen.NameAndId;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */
public class SelectEndEntityProfilePage extends SiO2WizardPage
{
	private JLabel jLabelEndEntityProfile;
	private JTextPane jTextPane;
	private JComboBox jComboBoxEndEntityProfiles;

	private JLabel jLabelCertificateProfile;
	private JTextPane jTextPane2;
	private JComboBox jComboBoxCertificateProfiles;

	private JLabel jLabelCA;
	private JTextPane jTextPaneCA;
	private JComboBox jComboBoxCA;

	private SiO2MessagePanel messagePanel;
	private JPanel jPanel1;

	/**
	 * @param title
	 * @param description
	 */
	public SelectEndEntityProfilePage(String title, String description)
	{
		super(title, description);

		initialize();
	}

	private void initialize()
	{
		this.setTitle("Select End Entity Type");
		this.setDescription("Select the type of end entity to create");

		setLayout(new GridBagLayout());

		// Make sure that messagePanel is created before the other items because
		// an exception
		// may be thrown in initializing them (eg, if the connection to the
		// server fails).
		getMessagePanel();

		this.add(getJLabelEndEntityProfile(), new GridBagConstraints(0, 0, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJComboBoxEndEntityProfiles(), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJTextPane(), new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		this.add(getJLabelCertificateProfile(), new GridBagConstraints(0, 3, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJComboBoxCertificateProfiles(), new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJTextPane2(), new GridBagConstraints(0, 5, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		this.add(getJLabelCA(), new GridBagConstraints(0, 6, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJComboBoxCA(), new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJTextPaneCA(), new GridBagConstraints(0, 8, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		this.add(getMessagePanel(), new GridBagConstraints(0, 9, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.add(getJPanel1(), new GridBagConstraints(0, 9, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		// Set selection to first entry; set it to something else first, so that
		// second combo box gets initialized.
		// TODO What would be a better way to accomplish this?

		// An exception is thrown if setting an index greater than the the
		// number of elements in the list (eg, if the
		// connection to the server fails).
		int numEntries = jComboBoxEndEntityProfiles.getItemCount();
		if (numEntries > 1)
		{
			jComboBoxEndEntityProfiles.setSelectedIndex(1);
		}
		if (numEntries > 0)
		{
			jComboBoxEndEntityProfiles.setSelectedIndex(0);
		}
	}

	/**
	 * This method initializes jTextPane
	 * 
	 * @return javax.swing.JTextPane
	 */
	private JTextPane getJTextPane()
	{
		if (jTextPane == null)
		{
			jTextPane = new JTextPane();
			jTextPane.setEditable(false);
			jTextPane.setBackground(this.getBackground());
			jTextPane
					.setText("The type of the end entity is determined by the choice of the end entity profile in the above list.");
		}
		return jTextPane;
	}

	/**
	 * This method initializes jTextPane
	 * 
	 * @return javax.swing.JTextPane
	 */
	private JTextPane getJTextPane2()
	{
		if (jTextPane2 == null)
		{
			jTextPane2 = new JTextPane();
			jTextPane2.setEditable(false);
			jTextPane2.setBackground(this.getBackground());
			jTextPane2
					.setText("The end entity profile selection determines the list of available certificate profiles which may be chosen from the above list.");
		}
		return jTextPane2;
	}

	/**
	 * This method initializes jComboBoxCertificateProfiles
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxCertificateProfiles()
	{
		if (jComboBoxCertificateProfiles == null)
		{
			jComboBoxCertificateProfiles = new JComboBox();
			jComboBoxCertificateProfiles
					.setRenderer(new NameAndIdCellRenderer());
			jComboBoxCertificateProfiles.setName("CertificateProfile");
		}
		return jComboBoxCertificateProfiles;
	}

	/**
	 * This method initializes jComboBoxEndEntityProfiles
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxEndEntityProfiles()
	{
		if (jComboBoxEndEntityProfiles == null)
		{
			jComboBoxEndEntityProfiles = new JComboBox();
			jComboBoxEndEntityProfiles.setRenderer(new NameAndIdCellRenderer());
			// Call to setName is required for all components which need to be
			// saved as a WizardSetting
			jComboBoxEndEntityProfiles.setName("EndEntityProfile");

			// Initialize combobox with list of available profiles.
			try
			{
				final WebServiceConnection ws = new WebServiceConnection();
				
				List<NameAndId> names = ws.getEndEntityProfiles();

				jComboBoxEndEntityProfiles.setModel(new DefaultComboBoxModel(
						names.toArray())
				{
					@Override
					public void setSelectedItem(Object anItem)
					{
						super.setSelectedItem(anItem);

						System.out.println("setSelectedItem called: [" + anItem
								+ "]");

						List<NameAndId> list;
						List<NameAndId> listCAs;
						try
						{
							list = ws
									.getAvailableCertificateProfiles(((NameAndId) anItem)
											.getId());
							jComboBoxCertificateProfiles
									.setModel(new DefaultComboBoxModel(list
											.toArray()));

							// If the end entity profile has "Any CA" as the single choice, then the default CA is returned.
							// (In the admin web page, the default CA is automatically added as a selection in the available CAs list).
							listCAs = ws
									.getAvailableCAsInProfile(((NameAndId) anItem)
											.getId());
							jComboBoxCA.setModel(new DefaultComboBoxModel(
									listCAs.toArray()));
						}
						catch (Exception e)
						{
							e.printStackTrace();

							messagePanel.setMessagesAndShow(e);
						}
					}

				});
			}
			catch (Exception exc)
			{
				// messagePanel is used to display the exception message in some meaningful way to suggest to the
				// user what he needs to change so that things work.
				exc.printStackTrace();

				// The real exception is caught earlier and then rethrown so we
				// can display its info here.
				messagePanel.setMessagesAndShow(exc);
			}
		}
		return jComboBoxEndEntityProfiles;
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
			jLabelEndEntityProfile.setText("End Entity Profile:");
		}
		return jLabelEndEntityProfile;
	}

	private JLabel getJLabelCA()
	{
		if (jLabelCA == null)
		{
			jLabelCA = new JLabel();
			jLabelCA.setText("Certificate Authority:");
		}
		return jLabelCA;
	}

	private JLabel getJLabelCertificateProfile()
	{
		if (jLabelCertificateProfile == null)
		{
			jLabelCertificateProfile = new JLabel();
			jLabelCertificateProfile.setText("Certificate Profile:");
		}
		return jLabelCertificateProfile;
	}

	private JComboBox getJComboBoxCA()
	{
		if (jComboBoxCA == null)
		{
			jComboBoxCA = new JComboBox();
			jComboBoxCA
					.setRenderer(new NameAndIdCellRenderer());
			jComboBoxCA.setName("CertificateAuthority");
		}
		return jComboBoxCA;
	}

	/**
	 * This method initializes jTextPane
	 * 
	 * @return javax.swing.JTextPane
	 */
	private JTextPane getJTextPaneCA()
	{
		if (jTextPaneCA == null)
		{
			jTextPaneCA = new JTextPane();
			jTextPaneCA.setEditable(false);
			jTextPaneCA.setBackground(this.getBackground());
			jTextPaneCA
					.setText("The list of available certificate authorities is determined by the choice of the end entity profile.");
		}
		return jTextPaneCA;
	}

} // @jve:decl-index=0:visual-constraint="10,10"
