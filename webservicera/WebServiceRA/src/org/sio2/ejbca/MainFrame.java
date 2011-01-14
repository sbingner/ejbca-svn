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
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.sio2.common.Common;

import apple.dts.samplecode.osxadapter.OSXAdapter;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */
public class MainFrame extends JFrame
{
	private static ApplicationProperties properties = new ApplicationProperties();

	private static final long serialVersionUID = 1L;

	private JLabel imageLogo;
	private JButton jButtonCreateEndEntity = null;
	private JButton jButtonQueryEndEntities = null;
	private JButton jButtonQuickEnrollment;
	private JPanel jContentPane = null;
	private JMenu jMenu1;
	private JMenuBar jMenuBar;

	private JMenu jMenuHelp;

	private JMenuItem jMenuItemAbout;

	private JMenuItem jMenuItemOptions;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// Parse command line arguments.
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-UseBoldFontInMessagePanel"))
			{
				SiO2MessagePanel.bUseBoldFont = true;
			}
		}

		MainFrame thisClass = null;

		thisClass = new MainFrame();
		thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		thisClass.setVisible(true);

		thisClass.doMacStuff();
	}

	static ApplicationProperties getProperties()
	{
		return properties;
	}

	/**
	 * @throws HeadlessException
	 */
	public MainFrame() throws HeadlessException
	{
		super();

		initialize();
	}

	/**
	 * @param gc
	 */
	public MainFrame(GraphicsConfiguration gc)
	{
		super(gc);

		initialize();
	}

	/**
	 * @param title
	 * @throws HeadlessException
	 */
	public MainFrame(String title) throws HeadlessException
	{
		super(title);

		initialize();
	}

	/**
	 * @param title
	 * @param gc
	 */
	public MainFrame(String title, GraphicsConfiguration gc)
	{
		super(title, gc);

		initialize();
	}

	private void doMacStuff()
	{
		// For the Mac:
		// There's probably a Mac specific way of doing this, but the
		// following doesn't requiring importing any Apple jar files.
		if (Common.isMacOSX())
		{
			/*******
			 * If this code were only ever to be compiled on Mac OS X,
			 * the following code would suffice.
			 * To avoid "com.apple cannot be resolved to a type" error messages,
			 * we avoid this code that dynamically performs equivalent (based on
			 * OSXAdapter.java from Apple).
			
			// When building this code in Eclipse, the following may
			// result in an error (Access restriction: Class is not
			// accessible due to restriction on required library).
			// To remove this error, go into
			// (Project) Properties -> Java Compiler -> Errors/Warnings
			// Deprecated and restricted API
			// Change
			// "Forbidden reference (access rules) from "Error" to "Warning"
			com.apple.eawt.Application application = com.apple.eawt.Application
					.getApplication();
			application.setEnabledPreferencesMenu(true);

			application
					.addApplicationListener(new com.apple.eawt.ApplicationAdapter()
					{
						@Override
						public void handleAbout(
								com.apple.eawt.ApplicationEvent arg0)
						{
							System.out.println(arg0);

							doAboutBox();

							// Set the following so default about box on Mac
							// doesn't appear.
							arg0.setHandled(true);
						}

						public void handlePreferences(
								com.apple.eawt.ApplicationEvent arg0)
						{
							System.out.println(arg0);

							doOptionsDlg();
						}

						@Override
						// Need this handler because Command-Q in system
						// menu added on Mac
						// for quitting application doesn't invoke
						// normal WindowListener events
						// (though closing the main window directly
						// does).
						public void handleQuit(
								com.apple.eawt.ApplicationEvent arg0)
						{
							System.out.println("handleQuit");

							doWindowClosing();
						}
					});
					*/
			
				registerForMacOSXEvents();
		}
	}

	// Adapted from OSXAdapter/MyApp.java
	// Generic registration with the Mac OS X application menu
	//
	// Checks the platform, then attempts to register with the Apple EAWT
	//
	// See OSXAdapter.java to see how this is done without directly referencing
	// any Apple APIs
	public void registerForMacOSXEvents()
	{
		if (Common.isMacOSX())
		{
			try
			{
				// Generate and register the OSXAdapter, passing it a hash of
				// all the methods we wish to

				// use as delegates for various
				// com.apple.eawt.ApplicationListener methods

				OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod(
						"doWindowClosing", (Class[]) null));

				OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod(
						"handleAbout", (Class[]) null));

				OSXAdapter.setPreferencesHandler(this, getClass()
						.getDeclaredMethod("doOptionsDlg", (Class[]) null));

				/*
				OSXAdapter.setFileHandler(this, getClass().getDeclaredMethod(
						"loadImageFile", new Class[]
						{
							String.class
						}));
				*/
			}
			catch (Exception e)
			{
				System.err.println("Error while loading the OSXAdapter:");
				e.printStackTrace();
			}
		}
	}

	// This method needs to be public for reflection lookup (when running on Mac OS) to succeed.
	// The options dialog should be shown the first time the app is run.
	public void doOptionsDlg()
	{
		OptionsPanel panel = new OptionsPanel();
		panel.load();
		while (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this, panel, "Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
		{
			// Call below to avoid SSL session caching also provides test that key store files actually exist and that passwords are correct.
			
			panel.store();
			
			// The following call ensures that no problems with SSL session occurring.
			// See the definition of this method for more information.
			WebServiceConnection ws = new WebServiceConnection(getProperties());
			try
			{
				ws.setNewDefaultSSLSocketFactory();
			}
			catch (Exception e)
			{
//				e.printStackTrace();

				SiO2MessagePanel messagePanel = new SiO2MessagePanel();
				messagePanel.setMessagesAndShow(e);
				messagePanel.setPreferredSize(new Dimension(640, 400));
				JOptionPane.showMessageDialog(this, messagePanel, "Settings Error", JOptionPane.ERROR_MESSAGE);
				
				// Go back into panel to give user a chance to fix the problem.
				continue;
			}
			
			break;
		}
	}

	// This method needs to be public for reflection lookup (when running on Mac OS) to succeed.
	public void handleAbout()
	{
		JOptionPane.showMessageDialog(this, new AboutBox(), "", JOptionPane.PLAIN_MESSAGE);
	}

	// This method needs to be public for reflection lookup (when running on Mac OS) to succeed.
	public boolean doWindowClosing()
	{
		getProperties().store();

		System.exit(0);
		
		return true;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize()
	{
		this.setContentPane(getJContentPane());
		// this.setSize(this.getPreferredSize());
		this.setSize(350, 320);
		this.setMinimumSize(this.getSize());

		// TODO What should the title be?
		this.setTitle("RA Administrator");

		if (Common.isMacOSX())
		{
			// The equivalent of the following can be achieved on the
			// command line by:
			// java -Dapple.laf.useScreenMenuBar=true -jar thisJar.jar
			System.setProperty("apple.laf.useScreenMenuBar", "true");
		}

		setJMenuBar(getJMenuBar1());

		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent evt)
			{
				doWindowClosing();
			}
		});

		// This will be called the first time the app is run because the load() call will fail (the RAAdmin.properties file will not exist yet).
		// Also check that the appserver connection values are all non-empty; if any is missing, then bring up the options dialog.
		ApplicationProperties props = getProperties();
		if ((!props.load())  || (!props.isValid()))
		{
			doOptionsDlg();
		}
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane()
	{
		if (jContentPane == null)
		{
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.setBackground(new java.awt.Color(255, 255, 255));
			jContentPane.add(getImageLogo(), new GridBagConstraints(0, 0, 1, 1,
					0.0, 0.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			jContentPane.add(getJButtonQuickEnrollment(),
					new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0,
							GridBagConstraints.CENTER, GridBagConstraints.NONE,
							new Insets(20, 10, 10, 10), 0, 0));
			jContentPane.add(getJButtonCreateEndEntity(),
					new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0,
							GridBagConstraints.CENTER, GridBagConstraints.NONE,
							new Insets(10, 10, 10, 10), 0, 0));
			jContentPane.add(getJButtonQueryEndEntities(),
					new GridBagConstraints(0, 3, 1, 1, 0.0, 1.0,
							GridBagConstraints.CENTER, GridBagConstraints.NONE,
							new Insets(10, 10, 20, 10), 0, 0));
		}
		return jContentPane;
	}

	private JLabel getImageLogo()
	{
		if (imageLogo == null)
		{
			imageLogo = new JLabel(new ImageIcon(getClass().getResource(
					"images/2436_logo-PrimeKey-100x334.png")));
		}
		return imageLogo;
	}

	/**
	 * This method initializes jButtonCreateEndEntity
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonCreateEndEntity()
	{
		if (jButtonCreateEndEntity == null)
		{
			jButtonCreateEndEntity = new JButton();

			jButtonCreateEndEntity.setText("Create End Entity");
			// jButtonCreateEndEntity.setPreferredSize(new
			// java.awt.Dimension(150, 22));
			jButtonCreateEndEntity.setMnemonic(java.awt.event.KeyEvent.VK_C);
			jButtonCreateEndEntity
					.addActionListener(new java.awt.event.ActionListener()
					{
						// Auto-generated Event stub actionPerformed()
						public void actionPerformed(java.awt.event.ActionEvent e)
						{
							// create the dialog, and show it:
							CreateEndEntityWizard createEndEntityWizard = new CreateEndEntityWizard();
							createEndEntityWizard.setVisible(true);
						}
					});
		}
		return jButtonCreateEndEntity;
	}

	/**
	 * This method initializes jButtonQueryEndEntities
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonQueryEndEntities()
	{
		if (jButtonQueryEndEntities == null)
		{
			jButtonQueryEndEntities = new JButton();

			jButtonQueryEndEntities.setText("Query End Entities");
			// jButtonQueryEndEntities.setPreferredSize(new
			// java.awt.Dimension(119, 22));
			jButtonQueryEndEntities.setMnemonic(java.awt.event.KeyEvent.VK_Q);
			jButtonQueryEndEntities
					.addActionListener(new java.awt.event.ActionListener()
					{
						public void actionPerformed(java.awt.event.ActionEvent e)
						{
							QueryEndEntitiesWizard wizard = new QueryEndEntitiesWizard();
							wizard.setVisible(true);
						}
					});
		}
		return jButtonQueryEndEntities;
	}

	private JButton getJButtonQuickEnrollment()
	{
		if (jButtonQuickEnrollment == null)
		{
			jButtonQuickEnrollment = new JButton();
			jButtonQuickEnrollment.setText("Quick Enrollment");
			jButtonQuickEnrollment.setMnemonic(java.awt.event.KeyEvent.VK_K);
			jButtonQuickEnrollment.setName("jButtonQuickEnrollment");
			jButtonQuickEnrollment.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// TODO If no default QE is set, put up message box telling
					// user,
					// and then redirect to dialog or wizard to create a QE.

					// create the dialog, and show it:
					QuickEnrollmentWizard qeWizard = new QuickEnrollmentWizard();
					qeWizard.setVisible(true);
				}
			});
		}
		return jButtonQuickEnrollment;
	}

	private JMenu getJMenu1()
	{
		if (jMenu1 == null)
		{
			jMenu1 = new JMenu();
			jMenu1.setText("Options");
			jMenu1.setMnemonic(java.awt.event.KeyEvent.VK_O);
			jMenu1.add(getJMenuItemOptions());
		}
		return jMenu1;
	}

	private JMenuBar getJMenuBar1()
	{
		jMenuBar = new JMenuBar();
		if (!Common.isMacOSX())
		{
			jMenuBar.add(getJMenu1());
		}
		jMenuBar.add(getJMenuHelp());

		return jMenuBar;
	}

	private JMenu getJMenuHelp()
	{
		if (jMenuHelp == null)
		{
			jMenuHelp = new JMenu();
			jMenuHelp.setText("Help");
			jMenuHelp.setMnemonic(java.awt.event.KeyEvent.VK_H);
			if (!Common.isMacOSX())
			{
				jMenuHelp.add(getJMenuItemAbout());
			}
		}
		return jMenuHelp;
	}

	private JMenuItem getJMenuItemAbout()
	{
		if (jMenuItemAbout == null)
		{
			jMenuItemAbout = new JMenuItem();
			jMenuItemAbout.setText("About...");
			jMenuItemAbout.setMnemonic(java.awt.event.KeyEvent.VK_A);
			jMenuItemAbout.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jMenuItemAboutActionPerformed(evt);
				}
			});
		}
		return jMenuItemAbout;
	}

	private JMenuItem getJMenuItemOptions()
	{
		if (jMenuItemOptions == null)
		{
			jMenuItemOptions = new JMenuItem();
			jMenuItemOptions.setText("Options...");
			jMenuItemOptions.setMnemonic(java.awt.event.KeyEvent.VK_O);
			jMenuItemOptions.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jMenuItemOptionsActionPerformed(evt);
				}
			});
		}
		return jMenuItemOptions;
	}

	private void jMenuItemAboutActionPerformed(ActionEvent evt)
	{
		handleAbout();
	}

	private void jMenuItemOptionsActionPerformed(ActionEvent evt)
	{
		doOptionsDlg();
	}
}
