/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.grapeshot.halfnes;

import java.util.prefs.Preferences;


/**
 *
 * @author Andrew
 */
public class PrefsSingleton {
	private static final Preferences instance = Preferences.userNodeForPackage(NES.class);

	protected PrefsSingleton()
	{
		// Exists only to defeat instantiation.
	}

	public synchronized static Preferences getInstance()
	{
		return instance;
	}
}
