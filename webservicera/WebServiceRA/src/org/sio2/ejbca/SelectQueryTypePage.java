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
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.ejbca.core.protocol.ws.client.gen.NameAndId;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.client.gen.UserMatch;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
*/

public class SelectQueryTypePage extends SiO2WizardPage
{
	private JLabel jLabelQueryBy;
	private JComboBox jComboBoxQueryBy;
	private JComboBox jComboBoxValues;
	private JTextField jTextField;
	private JComboBox jComboBoxMatchType;
	private JLabel jLabelBeginsWith;
	private JLabel jLabelEquals;

	private SiO2MessagePanel messagePanel;
	private JPanel jPanel1;

	/**
	 * @param title
	 * @param description
	 */
	public SelectQueryTypePage()
	{
		super("", "");
		// Auto-generated constructor stub

		initialize();
	}

	private void initialize()
	{
		this.setTitle("Select Query Type");
		this.setDescription("Select the type of query to perform");

		setLayout(new GridBagLayout());

		// Make sure that messagePanel is created before the other items because
		// an exception
		// may be thrown in initializing them (eg, if the connection to the
		// server fails).
		getMessagePanel();

		this.add(getJLabelEndEntityProfile(), new GridBagConstraints(0, 0, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJComboBoxQueryBy(), new GridBagConstraints(1, 0, 1, 1, 1.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJLabelEquals(), new GridBagConstraints(1, 1, 1, 1, 1.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJLabelBeginsWith(), new GridBagConstraints(1, 1, 1, 1, 1.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJComboBoxMatchType(), new GridBagConstraints(1, 1, 1, 1,
				1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJTextField(), new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 10, 10), 0, 0));
		this.add(getJComboBoxValues(), new GridBagConstraints(1, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));

		this.add(getMessagePanel(), new GridBagConstraints(0, 9, 2, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(getJPanel1(), new GridBagConstraints(0, 9, 2, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// TODO What should the default selection be?
		jComboBoxQueryBy.getModel().setSelectedItem(strTypes[0]);
	}

	// TODO What should order of the list be?
	// Which items should not be included?
	// Which items should have clearer or different description?

	// Advanced Query; the value used here should be significantly
	// greater than any of the others (in case new types are added
	// in the future).
	final static int MATCH_WITH_ADVANCED = 10000;

	final static int[] types =
		{
				UserMatch.MATCH_WITH_USERNAME,
				UserMatch.MATCH_WITH_CA,
				UserMatch.MATCH_WITH_UID,
				UserMatch.MATCH_WITH_COMMONNAME,
				UserMatch.MATCH_WITH_DNSERIALNUMBER,
				UserMatch.MATCH_WITH_GIVENNAME,
				UserMatch.MATCH_WITH_INITIALS,
				UserMatch.MATCH_WITH_SURNAME,
				UserMatch.MATCH_WITH_TITLE,
				UserMatch.MATCH_WITH_DN,
				UserMatch.MATCH_WITH_ORGANIZATIONUNIT,
				UserMatch.MATCH_WITH_ORGANIZATION,
				UserMatch.MATCH_WITH_LOCALE,
				UserMatch.MATCH_WITH_STATE,
				UserMatch.MATCH_WITH_DOMAINCOMPONENT,
				UserMatch.MATCH_WITH_COUNTRY,
				UserMatch.MATCH_WITH_EMAIL,
				UserMatch.MATCH_WITH_STATUS,
				UserMatch.MATCH_WITH_ENDENTITYPROFILE,
				UserMatch.MATCH_WITH_CERTIFICATEPROFILE,
				// UserMatch.MATCH_WITH_TOKEN,

				MATCH_WITH_ADVANCED
		};

	final static String[] strTypes =
		{
				"User Name",
				"Certificate Authority (CA)",
				"Unique ID (UID)",
				"Common Name (CN)",
				"Serial Number in DN",
				"First Name", // Given Name
				"Initials", // "First name abbreviation" in web page interface
				"Family Name", // Surname
				"Title",
				"Distinguished Name (DN)",
				"Organization Unit (OU)",
				"Organization (O)",
				"City or Locale (L)", // Not in web page interface?
				"State or Province (ST)",
				"Domain Component (DC)",
				"Country (C)",
				"Email Address",
				"Status",
				"End Entity Profile",
				"Certificate Profile",
				// "Token", // Not in web page interface

				"Advanced Query"
		};

	static int findTypeIndex(String strType)
	{
		int indexType = -1;
		for (int i = 0; i < strTypes.length; i++)
		{
			if (strTypes[i].equals(strType))
			{
				indexType = i;
				break;
			}
		}
		return indexType;
	}

	static int getUserMatchWithType(int index)
	{
		return types[index];
	}

	final static String[] strMatchType =
		{
				"Equals", "Begins With", "Contains"
		};
	
	final static int[] matchTypes =
		{
			UserMatch.MATCH_TYPE_EQUALS,
			UserMatch.MATCH_TYPE_BEGINSWITH,
			UserMatch.MATCH_TYPE_CONTAINS
		};

	static int getMatchType(String strType)
	{
		for (int i = 0; i < strMatchType.length; i++)
		{
			if (strMatchType[i].equals(strType))
			{
				return matchTypes[i];
			}
		}

		return -1;
	}

	final static String [] strStatus =
	{
		"New",
		"Failed",
		"Initialized",
		"In Process",
		"Generated",
		"Revoked",
		"Historical"
	};

	final static int [] iStatus =
		{
			UserDataVOWS.STATUS_NEW,
			UserDataVOWS.STATUS_FAILED,
			UserDataVOWS.STATUS_INITIALIZED,
			UserDataVOWS.STATUS_INPROCESS,
			UserDataVOWS.STATUS_GENERATED,
			UserDataVOWS.STATUS_REVOKED,
			UserDataVOWS.STATUS_HISTORICAL
		};

	static int getStatusValue(String str)
	{
		for (int i = 0; i < strStatus.length; i++)
		{
			if (strStatus[i].equals(str))
			{
				return iStatus[i];
			}
		}

		return -1;
	}

	static String getStatusString(int status)
	{
		for (int i = 0; i < iStatus.length; i++)
		{
			if (status == iStatus[i])
			{
				return strStatus[i];
			}
		}

		return null;
	}


	/**
	 * This method initializes jComboBoxQueryBy
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxQueryBy()
	{
		if (jComboBoxQueryBy == null)
		{
			jComboBoxQueryBy = new JComboBox();
			// Call to setName is required for all components which need to be
			// saved as a WizardSetting
			jComboBoxQueryBy.setName("QueryByType");

			// Initialize combobox with list of available profiles.
			try
			{
				// TODO How are date queries performed?

				assert (strTypes.length == types.length);

				jComboBoxQueryBy.setModel(new DefaultComboBoxModel(strTypes)
				{

					@Override
					public void setSelectedItem(Object anItem)
					{
						super.setSelectedItem(anItem);

						int index = findTypeIndex((String) anItem);
						assert (-1 != index);
						int typeMatchWith = getUserMatchWithType(index);

						switch (typeMatchWith)
						{
							case UserMatch.MATCH_WITH_CA:
							case UserMatch.MATCH_WITH_CERTIFICATEPROFILE:
							case UserMatch.MATCH_WITH_ENDENTITYPROFILE:
							case UserMatch.MATCH_WITH_STATUS:
								jLabelBeginsWith.setVisible(false);
								jLabelEquals.setVisible(true);
								jComboBoxMatchType.setVisible(false);
								jComboBoxValues.setVisible(true);
								jTextField.setVisible(false);
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
								jLabelBeginsWith.setVisible(false);
								jLabelEquals.setVisible(false);
								jComboBoxMatchType.setVisible(true);
								jComboBoxValues.setVisible(false);
								jTextField.setVisible(true);
								break;

							case MATCH_WITH_ADVANCED:
							default:
								jLabelBeginsWith.setVisible(false);
								jLabelEquals.setVisible(false);
								jComboBoxMatchType.setVisible(false);
								jComboBoxValues.setVisible(false);
								jTextField.setVisible(false);
								break;
						}

						ComboBoxModel model = null;
						switch (typeMatchWith)
						{
							case UserMatch.MATCH_WITH_CA:
								try
								{
									final WebServiceConnection ws = new WebServiceConnection();

									List<NameAndId> names = ws
											.getAvailableCAs();

									model = new DefaultComboBoxModel(names
											.toArray());
									jComboBoxValues
											.setRenderer(new NameAndIdCellRenderer());
								}
								catch (Exception exc)
								{
									exc.printStackTrace();

									messagePanel.setMessagesAndShow(exc);
								}
								break;
							case UserMatch.MATCH_WITH_CERTIFICATEPROFILE:
								try
								{
									final WebServiceConnection ws = new WebServiceConnection();

									Set<NameAndId> names = new TreeSet<NameAndId>(
											new NameAndIdComparator());

									// Since there is no direct web service api
									// for getting a list of available
									// certificate profiles,
									// we get all available end entity profiles
									// and then query each for its list.
									List<NameAndId> namesEndEntityProfiles = ws
											.getEndEntityProfiles();
									for (NameAndId nameAndId : namesEndEntityProfiles)
									{
										List<NameAndId> namesCertificateProfiles = ws
												.getAvailableCertificateProfiles(nameAndId
														.getId());
										names.addAll(namesCertificateProfiles);
									}

									model = new DefaultComboBoxModel(names
											.toArray());
									jComboBoxValues
											.setRenderer(new NameAndIdCellRenderer());
								}
								catch (Exception exc)
								{
									exc.printStackTrace();

									messagePanel.setMessagesAndShow(exc);
								}
								break;
							case UserMatch.MATCH_WITH_ENDENTITYPROFILE:
								try
								{
									final WebServiceConnection ws = new WebServiceConnection();

									List<NameAndId> names = ws
											.getEndEntityProfiles();

									model = new DefaultComboBoxModel(names
											.toArray());
									jComboBoxValues
											.setRenderer(new NameAndIdCellRenderer());
								}
								catch (Exception exc)
								{
									exc.printStackTrace();

									messagePanel.setMessagesAndShow(exc);
								}
								break;
							case UserMatch.MATCH_WITH_STATUS:
								// TODO How do we do an "All" status query?
								model = new DefaultComboBoxModel(new String[]
									{
											"New",
											"Failed",
											"Initialized",
											"In Process",
											"Generated",
											"Revoked",
											"Historical"
									});
								break;
						}
						if (null != model)
						{
							jComboBoxValues.setModel(model);
						}
					}
				});
			}
			catch (Exception exc)
			{
				exc.printStackTrace();

				// The real exception is caught earlier and then rethrown so we
				// can display its info here.
				messagePanel.setMessagesAndShow(exc);
			}

		}
		return jComboBoxQueryBy;
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
		if (jLabelQueryBy == null)
		{
			jLabelQueryBy = new JLabel();
			jLabelQueryBy.setText("Query By:");
			jLabelQueryBy.setVisible(false);
		}
		return jLabelQueryBy;
	}

	private JLabel getJLabelEquals()
	{
		if (jLabelEquals == null)
		{
			jLabelEquals = new JLabel();
			jLabelEquals.setText("Equals");
//			jLabelEquals.setName("EqualsLabel");
			jLabelEquals.setVisible(false);
		}
		return jLabelEquals;
	}

	private JLabel getJLabelBeginsWith()
	{
		if (jLabelBeginsWith == null)
		{
			jLabelBeginsWith = new JLabel();
			jLabelBeginsWith.setText("Begins With");
			jLabelBeginsWith.setVisible(false);
		}
		return jLabelBeginsWith;
	}

	private JComboBox getJComboBoxMatchType()
	{
		if (jComboBoxMatchType == null)
		{
			ComboBoxModel jComboBoxMatchTypeModel = new DefaultComboBoxModel(
					strMatchType);
			jComboBoxMatchType = new JComboBox();
			jComboBoxMatchType.setModel(jComboBoxMatchTypeModel);
			jComboBoxMatchType.setVisible(false);
			jComboBoxMatchType.setName("MatchType");
		}
		return jComboBoxMatchType;
	}

	private JTextField getJTextField()
	{
		if (jTextField == null)
		{
			jTextField = new JTextField();
			jTextField.setText("");
			jTextField.setName("TextValue");
		}
		return jTextField;
	}

	private JComboBox getJComboBoxValues()
	{
		if (jComboBoxValues == null)
		{
			ComboBoxModel jComboBoxValuesModel = new DefaultComboBoxModel(
					new String[]
						{
								"Item One", "Item Two"
						});
			jComboBoxValues = new JComboBox();
			jComboBoxValues.setModel(jComboBoxValuesModel);
			jComboBoxValues.setVisible(false);
			jComboBoxValues.setName("ComboValue");
		}
		return jComboBoxValues;
	}
}
