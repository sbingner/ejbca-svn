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
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.ejbca.core.model.ra.UserDataConstants;
import org.ejbca.core.protocol.ws.client.gen.Certificate;
import org.ejbca.core.protocol.ws.client.gen.NameAndId;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.client.gen.UserMatch;
import org.sio2.common.Common;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */

public class QueryResultsPage extends SiO2WizardPage
{
	private SiO2MessagePanel messagePanel;
	private JPanel jPanel1;
	private JScrollPane jScrollPane;
	private JMenuItem jMenuItemRevoke;
	private JSeparator jSeparator1;
	private JMenuItem jMenuItemViewHistory;
	private JMenuItem jMenuItemViewCertificates;
	private JMenuItem jMenuItemEditEndEntity;
	private JMenuItem jMenuItemViewEndEntity;
	private JPopupMenu jPopupMenu;
	private JLabel jLabel1;
	private JTable jTableResults;

	private List<UserDataVOWS> results = null;

	/**
	 * @param title
	 * @param description
	 */
	public QueryResultsPage()
	{
		super("", "");
		// Auto-generated constructor stub

		initialize();
	}

	private void initialize()
	{
		this.setTitle("Query Results");
		this.setDescription("Table of query results");

		setLayout(new GridBagLayout());

		// Make sure that messagePanel is created before the other items because
		// an exception
		// may be thrown in initializing them (eg, if the connection to the
		// server fails).
		getMessagePanel();

		this.add(getJScrollPane(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 10, 10, 10), 0, 0));

		this.add(getJLabel1(), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(
						10, 10, 10, 10), 0, 0));

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

	/**
	 * This is the last page in the wizard, so we will enable the finish button
	 * and disable the "Next >" button just before the page is displayed:
	 */
	public void rendering(List<WizardPage> path, WizardSettings settings)
	{
		super.rendering(path, settings);

		setFinishEnabled(true);
		setNextEnabled(false);
	}

	private WizardSettings wizardSettings = null;

	// If the state settings are not actually needed, this functionality would
	// be better moved into the "rendering" method.
	public void onPageChanged(WizardSettings settings, List<WizardPage> path,
			Map<String, Object> wizardStateSettings)
	{
		wizardSettings = settings;

		// Only perform the query if the page appeared as a result of a "Next"
		// action, not a "Prev" action.
		if (wizardStateSettings
				.get(SiO2WizardContainer.NAVIGATION_BUTTON_PRESSED) != SiO2WizardContainer.ButtonPressed.BUTTON_PREV)
		{
			this.messagePanel.setVisible(false);

			doQuery(wizardSettings);
		}
	}

	private String getItem(String strColumnName, UserDataVOWS user)
	{
		int iVal = SelectQueryTypePage.findTypeIndex(strColumnName);

		assert (-1 != iVal);

		switch (SelectQueryTypePage.getUserMatchWithType(iVal))
		{
			case UserMatch.MATCH_WITH_USERNAME:
				return user.getUsername();
			case UserMatch.MATCH_WITH_CA:
				return user.getCaName();
			case UserMatch.MATCH_WITH_CERTIFICATEPROFILE:
				return user.getCertificateProfileName();
			case UserMatch.MATCH_WITH_ENDENTITYPROFILE:
				return user.getEndEntityProfileName();
			case UserMatch.MATCH_WITH_STATUS:
				return UserDataConstants.getStatusText(user.getStatus());
			case UserMatch.MATCH_WITH_EMAIL:
				return user.getEmail();
			case UserMatch.MATCH_WITH_DN:
				return user.getSubjectDN();

			case UserMatch.MATCH_WITH_COMMONNAME:
			case UserMatch.MATCH_WITH_COUNTRY:
			case UserMatch.MATCH_WITH_DNSERIALNUMBER:
			case UserMatch.MATCH_WITH_DOMAINCOMPONENT:
			case UserMatch.MATCH_WITH_GIVENNAME:
			case UserMatch.MATCH_WITH_INITIALS:
			case UserMatch.MATCH_WITH_LOCALE:
			case UserMatch.MATCH_WITH_ORGANIZATION:
			case UserMatch.MATCH_WITH_ORGANIZATIONUNIT:
			case UserMatch.MATCH_WITH_STATE:
			case UserMatch.MATCH_WITH_SURNAME:
			case UserMatch.MATCH_WITH_TITLE:
				// case UserMatch.MATCH_WITH_TOKEN:
			case UserMatch.MATCH_WITH_UID:

			case SelectQueryTypePage.MATCH_WITH_ADVANCED: // Should never get
				// here
			default:
				return "*** Not available ***";
		}
	}

	private void doQuery(WizardSettings settings)
	{
		results = null;

		// *** Perform query and list results.
		if (Common.debug)
		{
			for (String key : settings.keySet())
			{
				System.out
						.println("\t" + key + ": [" + settings.get(key) + "]");
			}
		}

		String strType = (String) settings.get("QueryByType");
		int indexType = SelectQueryTypePage.findTypeIndex(strType);
		assert (-1 != indexType);
		int matchWithType = SelectQueryTypePage.getUserMatchWithType(indexType);
		if (SelectQueryTypePage.MATCH_WITH_ADVANCED == matchWithType)
		{
			return;
		}

		// Always list user name, CA, CN, and status.
		// First item after check box should be type the query was performed on.
		// For the following logic to work, make sure that the column names used
		// always match those
		// listed on the previous wizard page.
		Vector<String> vColumns = new Vector<String>();
		vColumns.add("User Name");
		vColumns.add("Certificate Authority (CA)");
		vColumns.add("Distinguished Name (DN)");
		vColumns.add("Status");
		int index = vColumns.indexOf(strType);
		if (-1 != index)
		{
			vColumns.remove(index);
		}
		vColumns.insertElementAt(strType, 0);

		DefaultTableModel tableModel = new DefaultTableModel(vColumns, 0);

		UserMatch usermatch = new UserMatch();

		usermatch.setMatchwith(matchWithType);

		switch (matchWithType)
		{
			case UserMatch.MATCH_WITH_CA:
			case UserMatch.MATCH_WITH_CERTIFICATEPROFILE:
			case UserMatch.MATCH_WITH_ENDENTITYPROFILE:
			{
				usermatch.setMatchtype(UserMatch.MATCH_TYPE_EQUALS);

				Object value = settings.get("ComboValue");
				String strText = null;
				if (value instanceof String)
				{
					strText = (String) value;
				}
				else if (value instanceof NameAndId)
				{
					strText = ((NameAndId) value).getName();
				}
				else
				{
					assert (false);
				}

				usermatch.setMatchvalue(strText);
			}
				break;

			case UserMatch.MATCH_WITH_STATUS:
			{
				usermatch.setMatchtype(UserMatch.MATCH_TYPE_EQUALS);

				Object value = settings.get("ComboValue");
				String strText = (String) value;
				int iValue = SelectQueryTypePage.getStatusValue(strText);

				usermatch.setMatchvalue(Integer.toString(iValue));
			}
				break;

			case UserMatch.MATCH_WITH_COMMONNAME:
			case UserMatch.MATCH_WITH_COUNTRY:
			case UserMatch.MATCH_WITH_DN:
			case UserMatch.MATCH_WITH_DNSERIALNUMBER:
			case UserMatch.MATCH_WITH_DOMAINCOMPONENT:
			case UserMatch.MATCH_WITH_EMAIL:
			case UserMatch.MATCH_WITH_GIVENNAME:
			case UserMatch.MATCH_WITH_INITIALS:
			case UserMatch.MATCH_WITH_LOCALE:
			case UserMatch.MATCH_WITH_ORGANIZATION:
			case UserMatch.MATCH_WITH_ORGANIZATIONUNIT:
			case UserMatch.MATCH_WITH_STATE:
			case UserMatch.MATCH_WITH_SURNAME:
			case UserMatch.MATCH_WITH_TITLE:
				// case UserMatch.MATCH_WITH_TOKEN:
			case UserMatch.MATCH_WITH_UID:
			case UserMatch.MATCH_WITH_USERNAME:
			{
				String strMatchType = (String) settings.get("MatchType");
				int iType = SelectQueryTypePage.getMatchType(strMatchType);
				usermatch.setMatchtype(iType);

				String strText = (String) settings.get("TextValue");
				usermatch.setMatchvalue(strText);
			}
				break;

			case SelectQueryTypePage.MATCH_WITH_ADVANCED: // Should never get
				// here
			default:
				break;

		}
		try
		{
			final WebServiceConnection ws = new WebServiceConnection();

			results = ws.findUser(usermatch);
			if ((null == results) || (0 == results.size()))
			{
				messagePanel.setMessageAndShow("*** No results found. ***");
			}
			else
			{
				final int numResults = results.size();
				if (100 == numResults)
				{
					messagePanel
							.setMessageAndShow("*** The first 100 results are listed. ***");
				}
				for (UserDataVOWS user : results)
				{
					Vector<Object> rowData = new Vector<Object>();
					for (int j = 0; j < vColumns.size(); j++)
					{
						rowData.add(getItem(vColumns.elementAt(j), user));
					}

					tableModel.insertRow(tableModel.getRowCount(), rowData);

					if (Common.debug)
					{
						System.out
								.println("\n****\n\t" + user.getUsername()
										+ "\n\t" + user.getPassword() + "\n\t"
										+ user.getCaName() + "\n\t"
										+ user.getCertificateProfileName()
										+ "\n\t" + user.getEmail() + "\n\t"
										+ user.getEndEntityProfileName()
										+ "\n\t"
										+ user.getHardTokenIssuerName()
										+ "\n\t" + user.getStartTime() + "\n\t"
										+ user.getEndTime() + "\n\t"
										+ user.getStatus() + "\n\t"
										+ user.getSubjectAltName() + "\n\t"
										+ user.getSubjectDN() + "\n\t"
										+ user.getTokenType() + "\n\t"
										+ user.getType());
					}
				}
				if (Common.debug)
				{
					System.out.println("Number results found = " + numResults);
					System.out.println("Table model size = "
							+ tableModel.getRowCount());
				}
				tableModel.setRowCount(numResults);
			}

			jTableResults.setModel(tableModel);
			jTableResults.invalidate();
		}
		catch (Exception exc)
		{
			exc.printStackTrace();

			messagePanel.setMessagesAndShow(exc);
		}
	}

	private JScrollPane getJScrollPane()
	{
		if (jScrollPane == null)
		{
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTableResults());
		}
		return jScrollPane;
	}

	/**
	 * Columns displayed should include: User Name, CA, Common Name, Status and
	 * first column (after selection checkbox) should always be the query type.
	 * 
	 * @return
	 */
	private JTable getJTableResults()
	{
		if (jTableResults == null)
		{
			jTableResults = new JTable();
			jTableResults.setAutoCreateRowSorter(true);
			setComponentPopupMenu(jTableResults, getJPopupMenu());
		}
		return jTableResults;
	}

	private JLabel getJLabel1()
	{
		if (jLabel1 == null)
		{
			jLabel1 = new JLabel();
			jLabel1
					.setText("Select (or multi-select) items in the results table above and then right click for a menu of actions that may be performed.");
		}
		return jLabel1;
	}

	private JPopupMenu getJPopupMenu()
	{
		if (jPopupMenu == null)
		{
			jPopupMenu = new JPopupMenu();
			jPopupMenu.add(getJMenuItemRevoke());
			jPopupMenu.add(getJSeparator1());
			jPopupMenu.add(getJMenuItemViewEndEntity());
			jPopupMenu.add(getJMenuItemEditEndEntity());
			jPopupMenu.add(getJMenuItemViewCertificates());
			jPopupMenu.add(getJMenuItemViewHistory());
		}
		return jPopupMenu;
	}

	/**
	 * Auto-generated method for setting the popup menu for a component
	 */
	private void setComponentPopupMenu(final java.awt.Component parent,
			final javax.swing.JPopupMenu menu)
	{
		parent.addMouseListener(new java.awt.event.MouseAdapter()
		{
			public void mousePressed(java.awt.event.MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showContextMenu(parent, menu, e);
				}
			}

			public void mouseReleased(java.awt.event.MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showContextMenu(parent, menu, e);
				}
			}
		});
	}

	private void showContextMenu(final java.awt.Component parent,
			final javax.swing.JPopupMenu menu, java.awt.event.MouseEvent e)
	{
		if (null == results)
		{
			return;
		}

		int numRowsSelected = jTableResults.getSelectedRowCount();

		if (numRowsSelected < 1)
		{
			return;
		}

		boolean bShowSingleItemActions = (1 == numRowsSelected);
		jMenuItemViewEndEntity.setVisible(bShowSingleItemActions);
		jMenuItemEditEndEntity.setVisible(bShowSingleItemActions);
		jMenuItemViewCertificates.setVisible(bShowSingleItemActions);
		jMenuItemViewHistory.setVisible(bShowSingleItemActions);
		jSeparator1.setVisible(bShowSingleItemActions);

		menu.show(parent, e.getX(), e.getY());
	}

	private JMenuItem getJMenuItemRevoke()
	{
		if (jMenuItemRevoke == null)
		{
			jMenuItemRevoke = new JMenuItem();
			jMenuItemRevoke.setText("Revoke...");
			jMenuItemRevoke.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jMenuItemRevokeActionPerformed(evt);
				}
			});
		}
		return jMenuItemRevoke;
	}

	private JMenuItem getJMenuItemViewEndEntity()
	{
		if (jMenuItemViewEndEntity == null)
		{
			jMenuItemViewEndEntity = new JMenuItem();
			jMenuItemViewEndEntity.setText("View End Entity");
			jMenuItemViewEndEntity.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jMenuItemViewEndEntityActionPerformed(evt);
				}
			});
		}
		return jMenuItemViewEndEntity;
	}

	private JMenuItem getJMenuItemEditEndEntity()
	{
		if (jMenuItemEditEndEntity == null)
		{
			jMenuItemEditEndEntity = new JMenuItem();
			jMenuItemEditEndEntity.setText("Edit End Entity");
			jMenuItemEditEndEntity.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jMenuItemEditEndEntityActionPerformed(evt);
				}
			});
		}
		return jMenuItemEditEndEntity;
	}

	private JMenuItem getJMenuItemViewCertificates()
	{
		if (jMenuItemViewCertificates == null)
		{
			jMenuItemViewCertificates = new JMenuItem();
			jMenuItemViewCertificates.setText("View Certificates");
			jMenuItemViewCertificates.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jMenuItemViewCertificatesActionPerformed(evt);
				}
			});
		}
		return jMenuItemViewCertificates;
	}

	private JMenuItem getJMenuItemViewHistory()
	{
		if (jMenuItemViewHistory == null)
		{
			jMenuItemViewHistory = new JMenuItem();
			jMenuItemViewHistory.setText("View History");
			jMenuItemViewHistory.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jMenuItemViewHistoryActionPerformed(evt);
				}
			});
		}
		return jMenuItemViewHistory;
	}

	private JSeparator getJSeparator1()
	{
		if (jSeparator1 == null)
		{
			jSeparator1 = new JSeparator();
		}
		return jSeparator1;
	}

	private void jMenuItemRevokeActionPerformed(ActionEvent evt)
	{
		if (0 == jTableResults.getSelectedRowCount())
		{
			JOptionPane.showMessageDialog(this, "No items selected.");
			return;
		}

		RevocationPanel panel = new RevocationPanel();

		int[] selRows = jTableResults.getSelectedRows();
		DefaultTableModel model = (DefaultTableModel) jTableResults.getModel();
		// Find the column for "User Name"
		int numCols = model.getColumnCount();
		int colUserName = -1;
		for (int i = 0; i < numCols; i++)
		{
			String name = model.getColumnName(i);
			if (name.equals("User Name"))
			{
				colUserName = i;
				break;
			}
		}
		assert (-1 != colUserName);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < selRows.length; i++)
		{
			sb.append(model.getValueAt(selRows[i], colUserName));
			sb.append("\n");
		}

		panel.init(selRows.length, sb.toString());

		if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this, panel,
				"Revoke Certificates", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE))
		{
			boolean bDeleteUser = panel.isDeleteUser();
			int reason = panel.getRevocationReason();

			final WebServiceConnection ws = new WebServiceConnection();

			for (int i = 0; i < selRows.length; i++)
			{
				String username = (String) model.getValueAt(selRows[i],
						colUserName);
				try
				{
					ws.revokeUser(username, reason, bDeleteUser);
				}
				catch (Exception e)
				{
					e.printStackTrace();

					JOptionPane.showMessageDialog(this, "Exception ["
							+ e.getClass() + "]\n\twith message ["
							+ e.getMessage()
							+ "]\n\tthrown while revoking user " + username);
					// TODO Stop revoking if an error?
					// Or continue?
					// Currently, revoking stops after an error; probably not
					// what most users want?
					break;
				}
			}

			// Refresh results screen after performing action
			doQuery(wizardSettings);
		}
	}

	private void jMenuItemViewEndEntityActionPerformed(ActionEvent evt)
	{
		if (null == results)
		{
			return;
		}
		if (1 != jTableResults.getSelectedRowCount())
		{
			return;
		}

		int rowIndex = jTableResults.getSelectedRow();
		assert (-1 != rowIndex);

		UserDataVOWS user = results.get(rowIndex);

		String strResult = "";
		strResult += "User Name: " + user.getUsername();
		strResult += "\nEnd Entity Profile: " + user.getEndEntityProfileName();
		strResult += "\nIs Clear Password: " + user.isClearPwd();
		strResult += "\nSubject DN: " + user.getSubjectDN();

		String strEmail = user.getEmail();
		strResult += "\nEmail: " + (null == strEmail ? "None" : strEmail);

		strResult += "\nCertificate Profile: "
				+ user.getCertificateProfileName();

		// TODO Support for this information not available via EJBCA web
		// services?
		strResult += "\nRevocation reason to set after certificate issuance: ????";

		strResult += "\nCA: " + user.getCaName();
		strResult += "\nToken: " + user.getTokenType();

		// TODO Support for this information not available via EJBCA web
		// services?
		strResult += "\nCreated: ????";

		// TODO Support for this information not available via EJBCA web
		// services?
		strResult += "\nModified: ????";

		strResult += "\nStatus: "
				+ SelectQueryTypePage.getStatusString(user.getStatus());
		
		/*
		List<ExtendedInformationWS> listExtInfo = user.getExtendedInformation();
		for (ExtendedInformationWS info: listExtInfo)
		{
			System.out.println("Name: [" + info.getName() + "]: [" + info.getValue() + "]");
		}
		*/

		InfoWindow infoWindow = new InfoWindow("End Entity: "
				+ user.getUsername());
		infoWindow.getJTextPane1().setText(strResult);
		infoWindow.getJTextPane1().setEditable(false);
		infoWindow.setSize(new java.awt.Dimension(800, 600));
		infoWindow.setVisible(true);
	}

	private void jMenuItemEditEndEntityActionPerformed(ActionEvent evt)
	{
		JOptionPane.showMessageDialog(this, "Not yet implemented.");
	}

	private void jMenuItemViewHistoryActionPerformed(ActionEvent evt)
	{
		JOptionPane.showMessageDialog(this, "Not yet implemented.");
	}

	// Based on code in CertificateView.java
	private void jMenuItemViewCertificatesActionPerformed(ActionEvent evt)
	{
		if (null == results)
		{
			return;
		}
		if (1 != jTableResults.getSelectedRowCount())
		{
			return;
		}

		int rowIndex = jTableResults.getSelectedRow();
		assert (-1 != rowIndex);

		UserDataVOWS user = results.get(rowIndex);

		try
		{
			final WebServiceConnection ws = new WebServiceConnection();

			java.util.List<Certificate> certs = ws.findCerts(
					user.getUsername(), false);

			if ((null == certs) || (0 == certs.size()))
			{
				if (Common.debug)
				{
					System.out.println("Num certs = 0");
				}
				// messagePanel.setMessageAndShow("*** No results found. ***");
			}
			else
			{
				final int numResults = certs.size();
				if (Common.debug)
				{
					System.out.println("Num certs = " + numResults);
				}

				CertificatePanel panel = new CertificatePanel();
				panel.setCerts(certs, user);

				// TODO Should this be modal (as currently) or would users ever
				// want to look at these screens side by side?
				JOptionPane.showMessageDialog(this, panel, "Certificates",
						JOptionPane.PLAIN_MESSAGE);
			}
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
		}

	}

}
