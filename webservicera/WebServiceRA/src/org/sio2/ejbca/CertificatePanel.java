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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.ejbca.core.protocol.ws.client.gen.Certificate;
import org.ejbca.core.protocol.ws.client.gen.RevokeStatus;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.util.CertTools;
import org.sio2.common.Common;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */
public class CertificatePanel extends JPanel
{
	private JLabel jLabel1;
	private JTextArea jTextAreaCertificate = new JTextArea();
	private JScrollPane jScrollPane1;
	private JComboBox jComboBoxRevocationReason;
	private JLabel jLabel2;
	private JButton jButtonNext;
	private JButton jButtonPrev;
	private JButton jButtonRevoke;
	private JCheckBox jCheckBoxRaw;

	private UserDataVOWS user = null;
	private java.util.List<Certificate> certs = null;
	private int currIndex = 0;

	/**
	 * 
	 */
	public CertificatePanel()
	{
		initGUI();
	}

	/**
	 * @param layout
	 */
	public CertificatePanel(LayoutManager layout)
	{
		super(layout);

		initGUI();
	}

	/**
	 * @param isDoubleBuffered
	 */
	public CertificatePanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);

		initGUI();
	}

	/**
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public CertificatePanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);

		initGUI();
	}

	private void initGUI()
	{
		try
		{
			{
				GridBagLayout thisLayout = new GridBagLayout();
				this.setLayout(thisLayout);
				this.setPreferredSize(new java.awt.Dimension(640, 600));
				{
					jLabel1 = new JLabel();
					this.add(jLabel1, new GridBagConstraints(0, 0, 3, 1, 0.0,
							0.0, GridBagConstraints.WEST,
							GridBagConstraints.NONE,
							new Insets(10, 10, 10, 10), 0, 0));
					jLabel1.setText("Certificate Information:");
				}
				{
					jScrollPane1 = new JScrollPane();
					this.add(jScrollPane1, new GridBagConstraints(0, 1, 3, 1,
							1.0, 1.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH,
							new Insets(10, 10, 10, 10), 0, 0));
					{
						jScrollPane1.setViewportView(jTextAreaCertificate);
						jTextAreaCertificate.setEnabled(true);
						jTextAreaCertificate.setEditable(false);
					}
				}
				{
					jCheckBoxRaw = new JCheckBox();
					this.add(jCheckBoxRaw, new GridBagConstraints(0, 2, 3, 1,
							0.0, 0.0, GridBagConstraints.WEST,
							GridBagConstraints.NONE,
							new Insets(10, 10, 10, 10), 0, 0));
					jCheckBoxRaw.setText("View Raw Certificate Data");
					jCheckBoxRaw.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent evt)
						{
							jCheckBoxRawActionPerformed(evt);
						}
					});
				}
				this.add(getJButtonRevoke(), new GridBagConstraints(0, 3, 1, 1,
						0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0,
						0));
				this.add(getJButtonPrev(), new GridBagConstraints(0, 4, 1, 1,
						1.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0,
						0));
				this.add(getJButtonNext(), new GridBagConstraints(2, 4, 1, 1,
						1.0, 0.0, GridBagConstraints.EAST,
						GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0,
						0));
				this.add(getJLabel2(), new GridBagConstraints(1, 3, 1, 1, 0.0,
						0.0, GridBagConstraints.CENTER,
						GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0,
						0));
				this.add(getJComboBoxRevocationReason(),
						new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
								GridBagConstraints.WEST,
								GridBagConstraints.NONE, new Insets(10, 10, 10,
										10), 0, 0));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private JButton getJButtonRevoke()
	{
		if (jButtonRevoke == null)
		{
			jButtonRevoke = new JButton();
			jButtonRevoke.setText("Revoke");
			jButtonRevoke.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jButtonRevokeActionPerformed(evt);
				}
			});
		}
		return jButtonRevoke;
	}

	private JButton getJButtonPrev()
	{
		if (jButtonPrev == null)
		{
			jButtonPrev = new JButton();
			jButtonPrev.setText("Previous");
			jButtonPrev.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jButtonPrevActionPerformed(evt);
				}
			});
		}
		return jButtonPrev;
	}

	private JButton getJButtonNext()
	{
		if (jButtonNext == null)
		{
			jButtonNext = new JButton();
			jButtonNext.setText("Next");
			jButtonNext.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jButtonNextActionPerformed(evt);
				}
			});
		}
		return jButtonNext;
	}

	void setCerts(java.util.List<Certificate> certs, UserDataVOWS user)
	{
		this.user = user;

		this.certs = certs;
		currIndex = 0;

		if (1 == certs.size())
		{
			jButtonNext.setVisible(false);
			jButtonPrev.setVisible(false);
		}

		setCertInfo();
	}

	private void setCertInfo()
	{
		boolean bRaw = jCheckBoxRaw.isSelected();

		jLabel1.setText("Certificate Information (Number " + (currIndex + 1)
				+ " of " + certs.size() + "):");

		Certificate cert = certs.get(currIndex);

		X509Certificate x509Cert;
		try
		{
			x509Cert = (X509Certificate) CertTools.getCertfromByteArray(cert
					.getRawCertificateData());
		}
		catch (CertificateException e1)
		{
			e1.printStackTrace();

			jTextAreaCertificate
					.setText("*** Information currently not available ***");
			return;
		}

		if (bRaw)
		{
			jTextAreaCertificate.setText(x509Cert.toString());
			Common.scrollTextAreaToTop(jTextAreaCertificate);
		}
		else
		{
			StringBuilder strResult = new StringBuilder();

			strResult.append(CertHelper.toString(x509Cert, user));

			// TODO Reuse ws to make more efficient.
			WebServiceConnection ws = new WebServiceConnection();

			RevokeStatus revokeStatus;
			try
			{
				revokeStatus = ws.checkRevokationStatus(x509Cert.getIssuerDN()
						.getName(), x509Cert.getSerialNumber().toString(16));
				int reason = revokeStatus.getReason();
				strResult
						.append("\nRevoked: "
								+ ((RevokeStatus.NOT_REVOKED == reason) ? "No"
										: ("Yes\n\tDate: "
												+ revokeStatus
														.getRevocationDate()
												+ "\n\tReason: " + CertHelper
												.getHumanReadableRevocationReason(reason))));
			}
			catch (Exception e)
			{
				e.printStackTrace();

				strResult
						.append("\n\tRevoked: Information currently not available"
								+ "\n\t\tReason: Currently not available");
			}
			jTextAreaCertificate.setText(strResult.toString());
		}
	}

	private void jButtonRevokeActionPerformed(ActionEvent evt)
	{
		int reason = getRevocationReason();
		Certificate cert = certs.get(currIndex);
		try
		{
			X509Certificate x509Cert = (X509Certificate) CertTools
					.getCertfromByteArray(cert.getRawCertificateData());

			WebServiceConnection ws = new WebServiceConnection();
			ws.revokeCert(x509Cert.getIssuerDN().toString(), CertTools
					.getSerialNumberAsString(x509Cert), reason);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setCertInfo();
	}

	private void jButtonPrevActionPerformed(ActionEvent evt)
	{
		int numItems = certs.size();

		if (0 == numItems)
		{
			return;
		}

		if (currIndex > 0)
		{
			currIndex--;
		}
		else
		{
			currIndex = numItems - 1;
		}

		setCertInfo();
	}

	private void jButtonNextActionPerformed(ActionEvent evt)
	{
		int numItems = certs.size();

		if (0 == numItems)
		{
			return;
		}

		if (currIndex < (numItems - 1))
		{
			currIndex++;
		}
		else
		{
			currIndex = 0;
		}

		setCertInfo();
	}

	private void jCheckBoxRawActionPerformed(ActionEvent evt)
	{
		setCertInfo();
	}

	private JLabel getJLabel2()
	{
		if (jLabel2 == null)
		{
			jLabel2 = new JLabel();
			jLabel2.setText("Reason:");
		}
		return jLabel2;
	}

	private JComboBox getJComboBoxRevocationReason()
	{
		if (jComboBoxRevocationReason == null)
		{
			ComboBoxModel jComboBoxRevocationReasonModel = new DefaultComboBoxModel(
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
			jComboBoxRevocationReason.setModel(jComboBoxRevocationReasonModel);
		}
		return jComboBoxRevocationReason;
	}

	// The order of these items must match the order of the items in the combo
	// box.
	private static int[] revocationReasons =
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
