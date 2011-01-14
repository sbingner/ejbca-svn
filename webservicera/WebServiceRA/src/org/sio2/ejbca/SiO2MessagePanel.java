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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * @author Daniel Horn, SiO2 Corp.
 * 
 * @version $Id$
*/
public class SiO2MessagePanel extends JPanel implements ClipboardOwner
{
	static boolean bUseBoldFont = false;

	private JScrollPane jScrollPane1;
	private JButton jButtonCopy;
	private JButton jButtonLessInformation;
	private JButton jButtonMoreInfo;
	private JTextPane jTextPane1;

	private String strShortMessage = "";
	private String strLongMessage = "";

	public SiO2MessagePanel()
	{
		super();

		initGUI();
	}

	private void setShortMessage(String strShortMessage)
	{
		this.strShortMessage = strShortMessage;
	}

	private void setLongMessage(String strLongMessage)
	{
		this.strLongMessage = strLongMessage;

		boolean bVisible = (null != strLongMessage)
				&& (0 != strLongMessage.length());

		jButtonLessInformation.setVisible(false);
		jButtonMoreInfo.setVisible(bVisible);
	}

	private void setVisibleMessage(String message)
	{
		jTextPane1.setText(message);
	}

	public void setMessageAndShow(String message, boolean bException)
	{
		setForeground(bException);
		
		setShortMessage(message);
		setLongMessage("");
		setVisibleMessage(message);

		this.setVisible(true);
		this.requestFocusInWindow();
	}

	private void setForeground(boolean bException)
	{
		if (bException)
		{
			jTextPane1.setForeground(java.awt.Color.RED);
		}
		else
		{
			jTextPane1.setForeground(java.awt.Color.BLACK);
		}
	}


	public void setMessageAndShow(String message)
	{
		this.setMessageAndShow(message, true);
	}

	public void setMessagesAndShow(String messageShort, String messageLong)
	{
		setForeground(true);

		setShortMessage(messageShort);
		setLongMessage(messageLong);
		setVisibleMessage(messageShort);

		this.setVisible(true);
		this.requestFocusInWindow();
	}

	public void setMessagesAndShow(Exception exc)
	{
		setForeground(true);

		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		exc.printStackTrace(printWriter);

		String messageShort = exc.getMessage();
		if ((null == messageShort) || (0 == messageShort.length()))
		{
			messageShort = exc.toString();
		}
		setShortMessage(messageShort);
		setLongMessage(stringWriter.toString());
		setVisibleMessage(messageShort);

		this.setVisible(true);
		this.requestFocusInWindow();
	}

	private void initGUI()
	{
		try
		{
			{
				GridBagLayout thisLayout = new GridBagLayout();
				thisLayout.columnWidths = new int[]
				{
						7, 7
				};
				this.setLayout(thisLayout);
				this.addAncestorListener(new AncestorListener()
				{
					public void ancestorRemoved(AncestorEvent evt)
					{
						// Simple way to determine that page on which this panel
						// rests has changed.
						// If page is no longer visible, we want the panel to be
						// hidden when/if the page is returned to.
						setVisible(false);
					}

					public void ancestorAdded(AncestorEvent evt)
					{
					}

					public void ancestorMoved(AncestorEvent evt)
					{
					}
				});
				this.addFocusListener(new FocusAdapter()
				{
					public void focusLost(FocusEvent evt)
					{
						thisFocusLost(evt);
					}
				});

				jScrollPane1 = new JScrollPane();
				this.add(jScrollPane1, new GridBagConstraints(0, 0, 2, 1, 1.0,
						1.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0,
						0));

				jTextPane1 = new JTextPane();
				jScrollPane1.setViewportView(jTextPane1);
				jTextPane1.setText("jTextPane1");
				jTextPane1.setEditable(false);
				jTextPane1.setBackground(this.getBackground());

				if (bUseBoldFont)
				{
					Font font = jTextPane1.getFont();
					Font newFont = font.deriveFont(font.getStyle() | Font.BOLD,
							font.getSize() + 2);
					jTextPane1.setFont(newFont);
				}

				jButtonMoreInfo = new JButton();
				this.add(jButtonMoreInfo, new GridBagConstraints(0, 1, 1, 1,
						0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0,
						0));
				jButtonMoreInfo.setText("More Information >");
				jButtonMoreInfo.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
					{
						jButtonMoreInfoActionPerformed(evt);
					}
				});
				jButtonLessInformation = new JButton();
				this.add(jButtonLessInformation, new GridBagConstraints(0, 1,
						1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.VERTICAL,
						new Insets(10, 10, 10, 10), 0, 0));
				jButtonLessInformation.setText("< Less Information");
				jButtonLessInformation.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
					{
						jButtonLessInformationActionPerformed(evt);
					}
				});

				this.add(getJButtonCopy(), new GridBagConstraints(1, 1, 1, 1,
						0.0, 0.0, GridBagConstraints.EAST,
						GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0,
						0));

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private JButton getJButtonCopy()
	{
		if (jButtonCopy == null)
		{
			jButtonCopy = new JButton();
			jButtonCopy.setText("Copy to Clipboard");
			jButtonCopy.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					jButtonCopyActionPerformed(evt);
				}
			});
		}
		return jButtonCopy;
	}

	private void jButtonCopyActionPerformed(ActionEvent evt)
	{
		StringSelection stringSelection = new StringSelection(jTextPane1
				.getText());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, this);
	}

	private void jButtonLessInformationActionPerformed(ActionEvent evt)
	{
		jTextPane1.setText(strShortMessage);
		jButtonLessInformation.setVisible(false);
		jButtonMoreInfo.setVisible(true);
	}

	private void jButtonMoreInfoActionPerformed(ActionEvent evt)
	{
		jTextPane1.setText(strLongMessage);

		jButtonLessInformation.setVisible(true);
		jButtonMoreInfo.setVisible(false);

		scrollToTop();
	}
	
	public void scrollToTop()
	{
		// Set top line visible.
		// Without invokeLater, text is scrolled to bottom.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				jTextPane1.scrollRectToVisible(new Rectangle(0, 0, 10, 10));
			}
		});
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents)
	{
		// Required for ClipboardOwner interface.
	}

	private boolean bHidePanelWhenFocusLost = false;

	public void setHidePanelWhenFocusLost(boolean bHidePanelWhenFocusLost)
	{
		this.bHidePanelWhenFocusLost = bHidePanelWhenFocusLost;
	}

	private void thisFocusLost(FocusEvent evt)
	{
		/*
		 * If you want the behavior that this panel becomes invisible whenever
		 * it loses focus, set the following property to true.
		 */
		if (bHidePanelWhenFocusLost)
		{
			if (!this.isAncestorOf(evt.getOppositeComponent()))
			{
				this.setVisible(false);
			}
			else
			{
				// Get focus back so that next time focusLost event occurs, this
				// method will be called again.
				this.requestFocusInWindow();
			}
		}
	}
}
