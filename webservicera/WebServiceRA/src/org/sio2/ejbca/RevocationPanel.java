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
import java.awt.LayoutManager;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.ejbca.core.protocol.ws.client.gen.RevokeStatus;



/**
 * @author Daniel Horn, SiO2 Corp.
 *
 * @version $Id$
 */
public class RevocationPanel extends JPanel
{
	private JCheckBox jCheckBoxRevokeAndDelete;
	private JLabel jLabelReason;
	private JScrollPane jScrollPane2;
	private JTextArea jTextAreaItems;
	private JLabel jLabelItemsToRevoke;
	private JComboBox jComboBoxRevocationReason;

	/**
	 * 
	 */
	public RevocationPanel()
	{
		initGUI();
	}

	/**
	 * @param layout
	 */
	public RevocationPanel(LayoutManager layout)
	{
		super(layout);

		initGUI();
	}

	/**
	 * @param isDoubleBuffered
	 */
	public RevocationPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);

		initGUI();
	}

	/**
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public RevocationPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);

		initGUI();
	}
	
	private void initGUI() {
		try {
			{
				GridBagLayout thisLayout = new GridBagLayout();
				this.setLayout(thisLayout);
				{
					jCheckBoxRevokeAndDelete = new JCheckBox();
					this.add(jCheckBoxRevokeAndDelete, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
					jCheckBoxRevokeAndDelete.setText("Delete end entities after revocation");
					jCheckBoxRevokeAndDelete.setSelected(true);
				}
				{
					jLabelReason = new JLabel();
					this.add(jLabelReason, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 2));
					jLabelReason.setText("Revocation reason:");
				}
				{
					ComboBoxModel jComboBoxRevocationReasonModel = 
						new DefaultComboBoxModel(
								new String[] 
								           { 
										"Unspecified", 
										"Key Compromise", 
										"CA Compromise", 
										"Affiliation Changed", 
										"Superseded", 
										"Cessation of Operation", 
										"Certificate Hold", 
										"Remove from CRL", 
										"Privileges Withdrawn", 
										"AA Compromise", 
										});
					jComboBoxRevocationReason = new JComboBox();
					this.add(jComboBoxRevocationReason, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
					jComboBoxRevocationReason.setModel(jComboBoxRevocationReasonModel);
				}
				{
					jLabelItemsToRevoke = new JLabel();
					this.add(jLabelItemsToRevoke, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
					jLabelItemsToRevoke.setText("End Entities to revoke:");
				}
				{
					jScrollPane2 = new JScrollPane();
					this.add(jScrollPane2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
					{
						jTextAreaItems = new JTextArea();
						jScrollPane2.setViewportView(jTextAreaItems);
						jTextAreaItems.setEditable(false);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	void init(int numItems, String strItems)
	{
		jTextAreaItems.setText(strItems);
		
		if (1 == numItems)
		{
			jLabelItemsToRevoke.setText("End Entity to revoke:");
			jCheckBoxRevokeAndDelete.setText("Delete end entity after revocation");
		}
	}
	
	boolean isDeleteUser()
	{
		return jCheckBoxRevokeAndDelete.isSelected();
	}
	
	// The order of these items must match the order of the items in the combo box.
	private static int [] revocationReasons =
	{
		RevokeStatus.REVOKATION_REASON_UNSPECIFIED,
		RevokeStatus.REVOKATION_REASON_KEYCOMPROMISE,
		RevokeStatus.REVOKATION_REASON_CACOMPROMISE,
		RevokeStatus.REVOKATION_REASON_AFFILIATIONCHANGED,
		RevokeStatus.REVOKATION_REASON_SUPERSEDED,
		RevokeStatus.REVOKATION_REASON_CESSATIONOFOPERATION,
		RevokeStatus.REVOKATION_REASON_CERTIFICATEHOLD,
		RevokeStatus.REVOKATION_REASON_REMOVEFROMCRL,
		RevokeStatus.REVOKATION_REASON_PRIVILEGESWITHDRAWN,
		RevokeStatus.REVOKATION_REASON_AACOMPROMISE
	};
	           
	int getRevocationReason()
	{
		int index = jComboBoxRevocationReason.getSelectedIndex();
		
		if (-1 == index)
		{
			return RevokeStatus.NOT_REVOKED;
		}
		
		return revocationReasons[index];
	}
}
