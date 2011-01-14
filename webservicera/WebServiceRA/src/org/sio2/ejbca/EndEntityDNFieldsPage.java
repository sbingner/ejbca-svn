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
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sio2.common.Common;



/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */
public class EndEntityDNFieldsPage extends SiO2WizardPage
{
	private JLabel jLabelSerialNumber;
	private JLabel jLabelCommonName;
	private JTextField jTextFieldSerialNumber;
	private JTextField jTextFieldCommonName;
	private JPanel jPanel1;
	private SiO2MessagePanel messagePanel;
	private JLabel jLabelLastName;
	private JLabel jLabelFirstName;
	private JTextField jTextFieldLastName;
	private JTextField jTextFieldFirstName;
	private JLabel jLabelOU;
	private JTextField jTextFieldOU;
	private JLabel jLabelEmail;
	private JTextField jTextFieldEmail;

	public EndEntityDNFieldsPage()
	{
		super("", "");

		initialize();
	}

	/**
	 * @param title
	 * @param description
	 */
	public EndEntityDNFieldsPage(String title, String description)
	{
		super(title, description);

		initialize();
	}

	private void initialize()
	{
		this.setSize(300, 224);
		this.setMinimumSize(new Dimension(300, 200));
		this.setPreferredSize(new java.awt.Dimension(366, 242));

		this.setTitle("Subject DN Fields");
		this.setDescription("Subject DN (Distinguished Name) Fields");

		setLayout(new GridBagLayout());

		// TODO Do we want labels to be right aligned as in some Mac apps or
		// left aligned as in most everything else?
		// Or change this depending on whether or not the app is run on a Mac
		// OS?
		int labelConstraint = GridBagConstraints.EAST;

		this.add(getJLabelUsername(), new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, labelConstraint, GridBagConstraints.NONE, new Insets(10,
						10, 10, 10), 0, 0));
		this.add(getJTextFieldCommonName(),
				new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10,
								10), 0, 0));
		this.add(getJLabelEmail(), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJTextFieldEmail(), new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJLabelOU(), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJTextFieldOU(), new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJLabelFirstName(), new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJTextFieldFirstName(), new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJLabelLastName(), new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJTextFieldLastName(), new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJLabelSerialNumber(), new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJTextFieldSerialNumber(), new GridBagConstraints(1, 5, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getMessagePanel(), new GridBagConstraints(0, 6, 2, 1, 0.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(getJPanel1(), new GridBagConstraints(0, 6, 1, 1, 0.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));

		jTextFieldCommonName.requestFocusInWindow();
		
		if (Common.debug)
		{
			System.err.println("*** Remember to turn remove debugging/testing initializers. *** ");
			jTextFieldCommonName.setText("TestCN");
			jTextFieldOU.setText("OU");
			jTextFieldFirstName.setText("fN");
			jTextFieldLastName.setText("lN");
			jTextFieldSerialNumber.setText("11111111");
			
			jTextFieldEmail.setText("danh@sio2corp.com");
		}

	}
	
	protected boolean validSettings()
	{
		// TODO What type of validation should be performed on the common name
		// besides making sure that it is non-empty?
		// One reason this must not be empty is that it is used as the alias when the keystore is created.
		if (0 == jTextFieldCommonName.getText().length())
		{
			messagePanel.setMessageAndShow("The Common Name cannot be empty.");

			// jTextFieldCommonName.requestFocus();
			messagePanel.requestFocusInWindow();
//			messagePanel.requestFocus();

			return false;
		}

		// TODO Display this information somewhere?
		// If a field is included here but not included in the end entity profile, an exception such as
		// UserDoesntFullfillEndEntityProfile_Exception: Wrong number of SN fields in Subject DN.
		// may occur.
		// If a field is marked as required in the profile but not assigned here, an exception such as 
		// org.ejbca.core.protocol.ws.client.gen.UserDoesntFullfillEndEntityProfile_Exception: Subject DN field 'ORGANIZATIONUNIT' must exist.
		// may occur.
		
		return true;
	}


	/**
	 * This method initializes jLabelCommonName
	 * 
	 * @return javax.swing.JLabel
	 */
	private JLabel getJLabelUsername()
	{
		if (jLabelCommonName == null)
		{
			jLabelCommonName = new JLabel();
			jLabelCommonName.setText("Common Name (CN):");
			jLabelCommonName.setLabelFor(getJTextFieldCommonName());
			jLabelCommonName.setDisplayedMnemonic(KeyEvent.VK_C);
			jLabelCommonName.setDisplayedMnemonicIndex(0);
		}
		return jLabelCommonName;
	}

	/**
	 * This method initializes jTextFieldCommonName
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldCommonName()
	{
		if (jTextFieldCommonName == null)
		{
			jTextFieldCommonName = new JTextField();
			jTextFieldCommonName.setName("CommonName");
//			jTextFieldCommonName.setSize(6, 22);
		}
		return jTextFieldCommonName;
	}

	private JTextField getJTextFieldEmail() {
		if(jTextFieldEmail == null) {
			jTextFieldEmail = new JTextField();
			jTextFieldEmail.setText("youremail@youremaildomain.com");
			jTextFieldEmail.setName("Email");
		}
		return jTextFieldEmail;
	}
	
	private JLabel getJLabelEmail() {
		if(jLabelEmail == null) {
			jLabelEmail = new JLabel();
			jLabelEmail.setText("Email:");
			jLabelEmail.setDisplayedMnemonic(KeyEvent.VK_E);
			jLabelEmail.setDisplayedMnemonicIndex(0);
			jLabelEmail.setLabelFor(getJTextFieldEmail());
		}
		return jLabelEmail;
	}
	
	private JTextField getJTextFieldOU() {
		if(jTextFieldOU == null) {
			jTextFieldOU = new JTextField();
			jTextFieldOU.setName("OrganizationUnit");
		}
		return jTextFieldOU;
	}
	
	private JLabel getJLabelOU() {
		if(jLabelOU == null) {
			jLabelOU = new JLabel();
			jLabelOU.setText("Organization Unit (OU):");
			jLabelOU.setDisplayedMnemonic(KeyEvent.VK_O);
			jLabelOU.setDisplayedMnemonicIndex(0);
			jLabelOU.setLabelFor(getJTextFieldOU());
		}
		return jLabelOU;
	}
	
	private JTextField getJTextFieldFirstName() {
		if(jTextFieldFirstName == null) {
			jTextFieldFirstName = new JTextField();
			jTextFieldFirstName.setName("FirstName");
		}
		return jTextFieldFirstName;
	}
	
	private JLabel getJLabelFirstName() {
		if(jLabelFirstName == null) {
			jLabelFirstName = new JLabel();
			jLabelFirstName.setText("First Name:");
			jLabelFirstName.setDisplayedMnemonic(KeyEvent.VK_F);
			jLabelFirstName.setDisplayedMnemonicIndex(0);
			jLabelFirstName.setLabelFor(getJTextFieldFirstName());
		}
		return jLabelFirstName;
	}
	
	private JTextField getJTextFieldLastName() {
		if(jTextFieldLastName == null) {
			jTextFieldLastName = new JTextField();
			jTextFieldLastName.setName("LastName");
		}
		return jTextFieldLastName;
	}
	
	private JLabel getJLabelLastName() {
		if(jLabelLastName == null) {
			jLabelLastName = new JLabel();
			jLabelLastName.setText("Last Name:");
			jLabelLastName.setDisplayedMnemonic(KeyEvent.VK_L);
			jLabelLastName.setDisplayedMnemonicIndex(0);
			jLabelLastName.setLabelFor(getJTextFieldLastName());
		}
		return jLabelLastName;
	}

	private JLabel getJLabelSerialNumber()
	{
		if (jLabelSerialNumber == null)
		{
			jLabelSerialNumber = new JLabel();
			jLabelSerialNumber.setText("Serial Number:");
			jLabelSerialNumber.setLabelFor(getJTextFieldSerialNumber());
			jLabelSerialNumber.setDisplayedMnemonic(KeyEvent.VK_S);
			jLabelSerialNumber.setDisplayedMnemonicIndex(0);
		}
		return jLabelSerialNumber;
	}

	private JTextField getJTextFieldSerialNumber()
	{
		if (jTextFieldSerialNumber == null)
		{
			jTextFieldSerialNumber = new JTextField();
			jTextFieldSerialNumber.setName("SerialNumber");
		}
		return jTextFieldSerialNumber;
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

}
