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

 
package org.sio2.common;

import java.awt.Rectangle;

import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Level;

/**
 * @author Daniel Horn, SiO2 Corp.
 *
 * @version $Id$
 */
public class Common
{
	public static boolean debug = false;
//	public static boolean debug = true;
	
	private static boolean bIsMacOSX = false;
	private static boolean bIsWindows = false;
	
	private static String strFileSeparator = (String)System.getProperty("file.separator");
	
	static
	{
		bIsMacOSX = System.getProperty("os.name").startsWith("Mac OS X");
//		assert(bIsMacOSX == (null != System.getProperty("mrj.version")));	// Another way to check to see if one is running on the Mac; documentation suggests this will become deprecated in the future.
		
		bIsWindows = System.getProperty("os.name").startsWith("Windows ");
		
		if (debug)
		{
			System.err.println("*** Turn off debug messages. ***");

			// Set logging level. Level info is set by default; for debugging, we want a higher level of information.
			Log log = LogFactory.getLog(Common.class);
//			System.out.println("log is " + log.getClass());

			if (log instanceof Log4JLogger)
			{
				Log4JLogger log1 = (Log4JLogger)log;
				// debug, trace, and info levels are all enabled after the following.
				((org.apache.log4j.Logger)log1.getLogger()).setLevel(Level.DEBUG);
				System.out.println("log1 is debug enabled: " + log1.isDebugEnabled());
				System.out.println("log is trace enabled: " + log1.isInfoEnabled());
				System.out.println("log is info enabled: " + log1.isInfoEnabled());
			}
		}
	}

	/**
	 * 
	 */
	public Common()
	{
	}

	public static boolean isMacOSX()
	{
		return bIsMacOSX;
	}
	
	public static boolean isWindows()
	{
		return bIsWindows;
	}
	
	public static String getFileSeparator()
	{
		return strFileSeparator;
	}
	
	// Returns true only if the two password fields are both non-empty and have the same value.
	public static boolean equalPasswordFields(JPasswordField pf1, JPasswordField pf2)
	{
		char[] pw1 = null;
		char[] pw2 = null;
		try
		{
			pw1 = pf1.getPassword();
			pw2 = pf2.getPassword();

			if ((null == pw1) || (null == pw2) || (pw1.length != pw2.length) || (0 == pw1.length))
			{
				return false;
			}
		}
		finally
		{
			if (null != pw1)
			{
				for (int i = pw1.length - 1; i >= 0; i--)
				{
					pw1[i] = 0;
				}
				pw1 = null;
			}

			if (null != pw2)
			{
				for (int i = pw2.length - 1; i >= 0; i--)
				{
					pw2[i] = 0;
				}
				pw2 = null;
			}
		}
		
		return true;
	}
	
	public static void scrollTextPaneToTop(final JTextPane jTextPane)
	{
		// Set top line visible.
		// Without invokeLater, text is scrolled to bottom.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				jTextPane.scrollRectToVisible(new Rectangle(0, 0, 10, 10));
			}
		});
	}

	public static void scrollTextAreaToTop(final JTextArea jTextArea)
	{
		// Set top line visible.
		// Without invokeLater, text is scrolled to bottom.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				jTextArea.scrollRectToVisible(new Rectangle(0, 0, 10, 10));
			}
		});
	}

}
