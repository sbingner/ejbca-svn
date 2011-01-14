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
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 * 
*/
public class AboutBox extends JPanel
{
	private JLabel jLabelImage;
	private JLabel jLabelVersion;
	private JLabel jLabelAllRights;
	private JLabel jLabelDevelopedBy;
	private JLabel jLabelJavaVersion;
	private JLabel jLabelOSVersion;
	private JLabel jLabelCopyright;
	private JLabel jLabelAppName;

	public AboutBox()
	{
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
					jLabelImage = new JLabel();
					this.add(jLabelImage, new GridBagConstraints(0, 0, 1, 1,
							0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.NONE, new Insets(0, 0, 10, 0),
							0, 0));
					jLabelImage
							.setIcon(new ImageIcon(
									getClass()
											.getClassLoader()
											.getResource(
													"org/sio2/ejbca/images/2436_logo-PrimeKey-100x334.png")));
				}
				{
					jLabelAppName = new JLabel();
					this.add(jLabelAppName, new GridBagConstraints(0, 1, 1, 1,
							0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.NONE, new Insets(10, 0, 10, 0),
							0, 0));
					jLabelAppName.setText("RA Administrator");
					jLabelAppName.setFont(new java.awt.Font("Dialog", 1, 12));
				}
				{
					jLabelVersion = new JLabel();
					this.add(jLabelVersion, new GridBagConstraints(0, 2, 1, 1,
							0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.NONE, new Insets(0, 0, 10, 0),
							0, 0));
					jLabelVersion.setText("Version 0.9.0.1");
				}
				{
					jLabelCopyright = new JLabel();
					this.add(jLabelCopyright, new GridBagConstraints(0, 5, 1,
							1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,
							0));
					jLabelCopyright
							.setText("Copyright © 2010 PrimeKey Solutions AB ");
				}
				{
					jLabelAllRights = new JLabel();
					this.add(jLabelAllRights, new GridBagConstraints(0, 6, 1,
							1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.NONE, new Insets(0, 0, 10, 0),
							0, 0));
					jLabelAllRights.setText("All rights reserved.");
				}
				Properties props = System.getProperties();
				{
					jLabelJavaVersion = new JLabel();
					this.add(jLabelJavaVersion, new GridBagConstraints(0, 3, 1,
							1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.NONE, new Insets(0, 0, 10, 0),
							0, 0));
					jLabelJavaVersion.setText("Java Version "
							+ props.getProperty("java.runtime.version"));
				}
				{
					jLabelOSVersion = new JLabel();
					this.add(jLabelOSVersion, new GridBagConstraints(0, 4, 1,
							1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.NONE, new Insets(0, 0, 10, 0),
							0, 0));
					jLabelOSVersion.setText(props.getProperty("os.name") + " "
							+ props.getProperty("os.version") + " ("
							+ props.getProperty("os.arch") + ")");
				}
				{
					jLabelDevelopedBy = new JLabel();
					this.add(jLabelDevelopedBy, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 0, 20, 0), 0, 0));
					jLabelDevelopedBy.setText("Developed for PrimeKey by D. Horn (SiO2corp.com)");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
