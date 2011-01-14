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

import java.awt.BorderLayout;

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */
public class InfoWindow extends JFrame
{
	private JScrollPane jScrollPane1;
	private JTextPane jTextPane1;

	/**
	 * @throws HeadlessException
	 */
	public InfoWindow() throws HeadlessException
	{
		initialize();
	}

	/**
	 * @param gc
	 */
	public InfoWindow(GraphicsConfiguration gc)
	{
		super(gc);

		initialize();
	}

	/**
	 * @param title
	 * @throws HeadlessException
	 */
	public InfoWindow(String title) throws HeadlessException
	{
		super(title);

		initialize();
	}

	/**
	 * @param title
	 * @param gc
	 */
	public InfoWindow(String title, GraphicsConfiguration gc)
	{
		super(title, gc);

		initialize();
	}

	private void initialize()
	{
		jScrollPane1 = new JScrollPane();
		jScrollPane1.setViewportView(getJTextPane1());

		getContentPane().add(jScrollPane1, BorderLayout.CENTER);
	}

	JTextPane getJTextPane1()
	{
		if (null == jTextPane1)
		{
			jTextPane1 = new JTextPane();
		}
		
		return jTextPane1;
	}
}
