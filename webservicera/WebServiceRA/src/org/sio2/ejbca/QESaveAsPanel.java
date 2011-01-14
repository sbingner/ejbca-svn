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
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
 */
public class QESaveAsPanel extends JPanel
{
	private JScrollPane jScrollPane1;
	private JButton jButtonBrowse;
	private JTextField jTextFieldFileName;
	private JLabel jLabel2;
	private JTextField jTextFieldDescription;
	private JLabel jLabel1;
	private JTextPane jTextPaneInformation;

	/**
	 * 
	 */
	public QESaveAsPanel()
	{
		super();

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
					jScrollPane1 = new JScrollPane();
					this.add(jScrollPane1, new GridBagConstraints(0, 0, 3, 1,
							1.0, 1.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH,
							new Insets(10, 10, 10, 10), 0, 0));
					{
						jTextPaneInformation = new JTextPane();
						jTextPaneInformation.setEditable(false);
						jScrollPane1.setViewportView(jTextPaneInformation);
					}
				}
				{
					jLabel1 = new JLabel();
					this.add(jLabel1, new GridBagConstraints(0, 1, 1, 1, 0.0,
							0.0, GridBagConstraints.CENTER,
							GridBagConstraints.NONE,
							new Insets(10, 10, 10, 10), 0, 0));
					jLabel1.setText("Description:");
				}
				{
					jTextFieldDescription = new JTextField();
					this.add(jTextFieldDescription, new GridBagConstraints(1,
							1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, new Insets(10, 10,
									10, 10), 0, 0));
				}
				{
					jLabel2 = new JLabel();
					this.add(jLabel2, new GridBagConstraints(0, 2, 1, 1, 0.0,
							0.0, GridBagConstraints.CENTER,
							GridBagConstraints.NONE,
							new Insets(10, 10, 10, 10), 0, 0));
					jLabel2.setText("File Name:");
				}
				{
					jTextFieldFileName = new JTextField();
					this.add(jTextFieldFileName, new GridBagConstraints(1, 2,
							1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, new Insets(10, 10,
									10, 10), 0, 0));
				}
				{
					jButtonBrowse = new JButton();
					this.add(jButtonBrowse, new GridBagConstraints(2, 2, 1, 1,
							0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.NONE,
							new Insets(10, 10, 10, 10), 0, 0));
					jButtonBrowse.setText("Browse...");
					jButtonBrowse.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent evt)
						{
							jButtonBrowseActionPerformed(evt);
						}
					});
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	void setInformation(String str, String strDescription)
	{
		jTextPaneInformation.setText(str);
		jTextFieldDescription.setText(strDescription);
	}

	boolean isDescriptionValid()
	{
		return jTextFieldDescription.getText().length() > 0;
	}

	String getFilename()
	{
		return jTextFieldFileName.getText();
	}

	String getDescription()
	{
		return jTextFieldDescription.getText();
	}

	static FileFilter qeFileFilter = new FileFilter()
	{
		@Override
		public boolean accept(File f)
		{
			return f.getName().endsWith(".QE");
		}

		@Override
		public String getDescription()
		{
			return "Quick Enrollment File (*.QE)";
		}
		
	};
	

	private static String strCurrentDir = "";

	private void jButtonBrowseActionPerformed(ActionEvent evt)
	{
		JFileChooser fileChooser = new JFileChooser(strCurrentDir);
		String strFileName = jTextFieldFileName.getText();
		if (0 != strFileName.length())
		{
			fileChooser.setSelectedFile(new File(strFileName));
		}

		fileChooser.setFileFilter(qeFileFilter);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);

		// Use showDialog instead of showSaveDialog because the latter doesn't hilight
		// files accepted by the FileFilter on Mac OS X.
		// TODO On Mac OS X, there is still the problem that non-highlighted files 
		// (ie, those not accepted by the file filter) may still be selected.
		// Also, the caption to the dialog is missing in this case.
		if (JFileChooser.APPROVE_OPTION != fileChooser.showDialog(this, "Save"))
		{
			return;
		}

		File fileSel = fileChooser.getSelectedFile();
		if (null == fileSel)
		{
			JOptionPane.showMessageDialog(null, "No file name selected",
					"Save As...", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Make sure that appropriate extension is part of the filename.
		final String strExtension = ".QE";
		if (!fileSel.getAbsolutePath().endsWith(strExtension))
		{
			fileSel = new File(fileSel.getAbsolutePath() + strExtension);
		}

		/* This check is performed immediately before actual save.
		if (fileSel.exists())
		{
			if (JOptionPane.YES_OPTION != JOptionPane
					.showConfirmDialog(
							this,
							"File with name\n"
									+ fileSel.toString()
									+ "\nalready exists.\n\nSave using this name anyway?",
							"File Already Exists",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE))
			{
				return;
			}
		}
		*/

		File parentDir = fileSel.getParentFile();
		if (null == parentDir)
		{
			JOptionPane.showMessageDialog(null, "No directory selected.",
					"Save As...", JOptionPane.ERROR_MESSAGE);
			return;
		}

		strCurrentDir = parentDir.getAbsolutePath();

		jTextFieldFileName.setText(fileSel.getAbsolutePath());
	}

}
