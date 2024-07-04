/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.grapeshot.halfnes;

import java.io.IOException;

import javax.swing.UIManager;

import com.grapeshot.halfnes.ui.MainForm;


public class halfNES {

	private halfNES()
	{
	}

	public static void main(String[] args) throws IOException
	{
		JInputHelper.setupJInput();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			;
		}
		new MainForm().start();
	}
}
