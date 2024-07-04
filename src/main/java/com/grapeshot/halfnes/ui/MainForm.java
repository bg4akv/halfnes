/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.grapeshot.halfnes.ui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import com.grapeshot.halfnes.FileUtils;
import com.grapeshot.halfnes.NES;
import com.grapeshot.halfnes.PrefsSingleton;
import com.grapeshot.halfnes.cheats.ActionReplay;
import com.grapeshot.halfnes.cheats.ActionReplayGui;
import com.grapeshot.halfnes.video.NTSCRenderer;
import com.grapeshot.halfnes.video.RGBRenderer;
import com.grapeshot.halfnes.video.Renderer;


public class MainForm extends JFrame {
	private static final String CMD_QUIT = "Quit";
	private static final String CMD_RESET = "Reset";
	private static final String CMD_HARD_RESET = "Hard Reset";
	private static final String CMD_PAUSE = "Pause";
	private static final String CMD_RESUME = "Resume";
	private static final String CMD_PREF = "Preferences";
	private static final String CMD_FAST_FORWARD = "Fast Forward";
	private static final String CMD_ABOUT = "About";
	private static final String CMD_ROM_INFO = "ROM Info";
	private static final String CMD_OPEN_ROM = "Open ROM";
	private static final String CMD_TOGGLE_FULL_SCREEN = "Toggle Fullscreen";
	private static final String CMD_FRAME_ADVANCE = "Frame Advance";
	private static final String CMD_ESCAPE = "Escape";
	private static final String CMD_CONTROLLER_SETTINGS = "Controller Settings";
	private static final String CMD_CHEAT_CODES = "Cheat Codes";


	private final NES nes;
	private final ControllerKeyListener padController1, padController2;
	private ActionListener actionListener;

	private Canvas canvas;

	private int screenScaleFactor;
	private final long[] frameTimes = new long[60];
	private int frameTimeIdx = 0;
	private boolean smoothScale, inFullScreen = false;
	private GraphicsDevice graphicsDevice;
	private int NES_HEIGHT, NES_WIDTH;
	private Renderer renderer;


	public MainForm()
	{
		init();
		nes = new NES(this);
		padController1 = new ControllerKeyListener(0);
		padController2 = new ControllerKeyListener(1);
		nes.setControllers(padController1, padController2);

		addKeyListener(padController1);
		addKeyListener(padController2);
	}

	private synchronized void setRenderOptions()
	{
		if (canvas != null) {
			remove(canvas);
		}

		screenScaleFactor = PrefsSingleton.getInstance().getInt("screenScaling", 2);
		smoothScale = PrefsSingleton.getInstance().getBoolean("smoothScaling", false);

		if (PrefsSingleton.getInstance().getBoolean("TVEmulation", false)) {
			renderer = new NTSCRenderer();
			NES_WIDTH = 302;
		} else {
			renderer = new RGBRenderer();
			NES_WIDTH = 256;
		}

		if (PrefsSingleton.getInstance().getInt("region", 0) > 1) {
			NES_HEIGHT = 240;
			renderer.setClip(0);
		} else {
			NES_HEIGHT = 224;
			renderer.setClip(8);
		}

		// Create canvas for painting
		canvas = new Canvas();
		canvas.setSize(NES_WIDTH * screenScaleFactor, NES_HEIGHT * screenScaleFactor);
		canvas.setEnabled(false); //otherwise it steals input events.
		// Add canvas to game window
		add(canvas);
		pack();
		canvas.createBufferStrategy(2);
	}

	public void start()
	{
		nes.start();
	}

	private void stop()
	{
		nes.stop();
	}

	private void init()
	{
		actionListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				String cmd = e.getActionCommand();
				// placeholder for more robust handler
				if (cmd.equals(CMD_QUIT)) {
					close();
				} else if (cmd.equals(CMD_RESET)) {
					nes.reset();
				} else if (cmd.equals(CMD_HARD_RESET)) {
					try {
						nes.reloadROM();
					} catch (Exception exp) {
						showMessageDialog(exp.getMessage());
					}
				} else if (cmd.equals(CMD_PAUSE)) {
					nes.pause();
				} else if (cmd.equals(CMD_RESUME)) {
					nes.resume();
				} else if (cmd.equals(CMD_PREF)) {
					showOptions();
				} else if (cmd.equals(CMD_FAST_FORWARD)) {
					nes.toggleFrameLimiter();
				} else if (cmd.equals(CMD_ABOUT)) {
					showMessageDialog("HalfNES " + NES.VERSION
							+ "\n"
							+ "Get the latest version and report any bugs at " + NES.URL + " \n"
							+ "\n"
							+ "This program is free software licensed under the GPL version 3, and comes with \n"
							+ "NO WARRANTY of any kind. (but if something's broken, please report it). \n"
							+ "See the license.txt file for details.");
				} else if (cmd.equals(CMD_ROM_INFO)) {
					String info = nes.getrominfo();
					if (info != null) {
						showMessageDialog(info);
					}
				} else if (cmd.equals(CMD_OPEN_ROM)) {
					loadROM();
				} else if (cmd.equals(CMD_TOGGLE_FULL_SCREEN)) {
					toggleFullScreen();
				} else if (cmd.equals(CMD_FRAME_ADVANCE)) {
					nes.frameAdvance();
				} else if (cmd.equals(CMD_ESCAPE)) {
					if (inFullScreen) {
						toggleFullScreen();
					} else {
						close();
					}
				} else if (cmd.equals(CMD_CONTROLLER_SETTINGS)) {
					showControlsDialog();
				} else if (cmd.equals(CMD_CHEAT_CODES)) {
					showActionReplayDialog();
				}
			}
		};

		//construct window
		setTitle("HalfNES " + NES.VERSION);
		setResizable(false);
		buildMenus(actionListener);
		setRenderOptions();
		setLocation(PrefsSingleton.getInstance().getInt("windowX", 0), PrefsSingleton.getInstance().getInt("windowY", 0));

		registerKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CMD_ESCAPE);
		registerKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), CMD_TOGGLE_FULL_SCREEN);
		registerKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK), CMD_QUIT);

		addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e)
			{
				;
			}

			@Override
			public void windowIconified(WindowEvent e)
			{
				;
			}

			@Override
			public void windowDeiconified(WindowEvent e)
			{
				;
			}

			@Override
			public void windowDeactivated(WindowEvent e)
			{
				;
			}

			@Override
			public void windowClosing(WindowEvent e)
			{
				close();
			}

			@Override
			public void windowClosed(WindowEvent e)
			{
				;
			}

			@Override
			public void windowActivated(WindowEvent e)
			{
				;
			}
		});
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		setVisible(true);
		// Create BackBuffer

		//now add the drag and drop handler.
		setTransferHandler(new TransferHandler() {
			@Override
			public boolean canImport(final TransferHandler.TransferSupport support)
			{
				return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
			}

			@Override
			public boolean importData(final TransferHandler.TransferSupport support)
			{
				if (!canImport(support)) {
					return false;
				}

				Transferable t = support.getTransferable();
				try {
					//holy typecasting batman (this interface predates generics)
					File toload = (File) ((List) t.getTransferData(DataFlavor.javaFileListFlavor)).get(0);
					loadROM(toload.getCanonicalPath());
				} catch (Exception e) {
					return false;
				}

				return true;
			}
		});
	}

	private void registerKeyboardAction(final KeyStroke keyStroke, String cmd)
	{
		if (keyStroke == null
			|| cmd == null || cmd.isEmpty()) {
			return;
		}

		JRootPane rootPane = getRootPane();
		InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = rootPane.getActionMap();

		inputMap.put(keyStroke, keyStroke);
		actionMap.put(keyStroke, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				actionListener.actionPerformed(new ActionEvent(e.getSource(), e.getID(), cmd));
			}
		});
	}

	private void buildMenus(ActionListener actionListener)
	{
		if (actionListener == null) {
			return;
		}

		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu("File");
		menuBar.add(menu);

		JMenuItem item;
		menu.add(item = new JMenuItem(CMD_OPEN_ROM));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.addSeparator();

		menu.add(item = new JMenuItem(CMD_PREF));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.addSeparator();

		menu.add(item = new JMenuItem(CMD_TOGGLE_FULL_SCREEN));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
		item.addActionListener(actionListener);

		menu.add(item = new JMenuItem(CMD_QUIT));
		item.addActionListener(actionListener);
		menuBar.add(menu);

		menu = new JMenu("NES");
		menuBar.add(menu);

		menu.add(item = new JMenuItem(CMD_RESET));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.add(item = new JMenuItem(CMD_HARD_RESET));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.add(item = new JMenuItem(CMD_PAUSE));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));

		menu.add(item = new JMenuItem(CMD_RESUME));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));

		menu.add(item = new JMenuItem(CMD_FAST_FORWARD));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.add(item = new JMenuItem(CMD_FRAME_ADVANCE));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.addSeparator();

		menu.add(item = new JMenuItem(CMD_CONTROLLER_SETTINGS));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.add(item = new JMenuItem(CMD_CHEAT_CODES));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.addSeparator();

		menu.add(item = new JMenuItem(CMD_ROM_INFO));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu = new JMenu("Help");
		menuBar.add(menu);

		menu.add(item = new JMenuItem(CMD_ABOUT));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

		setJMenuBar(menuBar);
	}

	public void loadROM()
	{
		FileDialog fileDialog = new FileDialog(this);
		fileDialog.setMode(FileDialog.LOAD);
		fileDialog.setTitle("Select a ROM to load");
		//should open last folder used, and if that doesn't exist, the folder it's running in
		final String path = PrefsSingleton.getInstance().get("filePath", System.getProperty("user.dir", ""));
		final File startDirectory = new File(path);
		if (startDirectory.isDirectory()) {
			fileDialog.setDirectory(path);
		}
		//and if the last path used doesn't exist don't set the directory at all
		//and hopefully the jFileChooser will open somewhere usable
		//on Windows it does - on Mac probably not.
		fileDialog.setFilenameFilter(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name != null && !name.isEmpty()
					&& (name.endsWith(".nes")
						|| name.endsWith(".fds")
						|| name.endsWith(".nsf")
						|| name.endsWith(".zip"));
			}
		});
		boolean wasInFullScreen = false;
		if (inFullScreen) {
			wasInFullScreen = true;
			//load dialog won't show if we are in full screen, so this fixes for now.
			toggleFullScreen();
		}
		fileDialog.setVisible(true);
		if (fileDialog.getFile() != null) {
			PrefsSingleton.getInstance().put("filePath", fileDialog.getDirectory());
			loadROM(fileDialog.getDirectory() + fileDialog.getFile());
		}
		if (wasInFullScreen) {
			toggleFullScreen();
		}
	}

	private void loadROM(String path)
	{
		if (path == null || path.isEmpty()) {
			return;
		}

		if (path.toLowerCase().endsWith(".zip")) {
			try {
				loadRomFromZip(path);
			} catch (Exception e) {
				showMessageDialog("Could not load file:\nFile does not exist or is not a valid NES game.\n" + e.getMessage());
			}
		} else {
			try {
				nes.loadROM(path);
			} catch (Exception e) {
				showMessageDialog(e.getMessage());
			}
		}
	}

	private void loadRomFromZip(String zipName) throws Exception
	{
		final String romName = selectRomInZip(listRomsInZip(zipName));
		if (romName != null) {
			final File extractedFile = extractRomFromZip(zipName, romName);
			if (extractedFile != null) {
				extractedFile.deleteOnExit();
				nes.loadROM(extractedFile.getCanonicalPath());
			}
		}
	}

	private List<String> listRomsInZip(String zipName) throws IOException
	{
		final ZipFile zipFile = new ZipFile(zipName);
		final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
		final List<String> romNames = new ArrayList<>();

		while (zipEntries.hasMoreElements()) {
			final ZipEntry entry = zipEntries.nextElement();
			if (!entry.isDirectory()
				&& (entry.getName().endsWith(".nes")
					|| entry.getName().endsWith(".fds")
					|| entry.getName().endsWith(".nsf"))) {
				romNames.add(entry.getName());
			}
		}

		zipFile.close();
		if (romNames.isEmpty()) {
			throw new IOException("No NES games found in ZIP file.");
		}

		return romNames;
	}

	private String selectRomInZip(List<String> romNames)
	{
		if (romNames == null || romNames.isEmpty()) {
			return null;
		}

		if (romNames.size() == 1) {
			return romNames.get(0);
		}

		return (String) JOptionPane.showInputDialog(this,
				"Select ROM to load", "Select ROM to load",
				JOptionPane.PLAIN_MESSAGE, null,
				romNames.toArray(), romNames.get(0));
	}

	private File extractRomFromZip(String zipName, String romName) throws IOException
	{
		final ZipInputStream zipStream = new ZipInputStream(new FileInputStream(zipName));
		ZipEntry entry;
		do {
			entry = zipStream.getNextEntry();
		} while ((entry != null) && (!entry.getName().equals(romName)));
		if (entry == null) {
			zipStream.close();
			throw new IOException("Cannot find file " + romName + " inside archive " + zipName);
		}
		//name temp. extracted file after parent zip and file inside

		//note: here's the bug, when it saves the temp file if it's in a folder
		//in the zip it's trying to put it in the same folder outside the zip
		final File outputFile = new File(new File(zipName).getParent()
				+ File.separator + FileUtils.stripExtension(new File(zipName).getName())
				+ " - " + romName);
		if (outputFile.exists()) {
			showMessageDialog("Cannot extract file. File " + outputFile.getCanonicalPath() + " already exists.");
			zipStream.close();
			return null;
		}
		final byte[] buf = new byte[4096];
		final FileOutputStream fos = new FileOutputStream(outputFile);
		int numBytes;
		while ((numBytes = zipStream.read(buf, 0, buf.length)) != -1) {
			fos.write(buf, 0, numBytes);
		}
		zipStream.close();
		fos.close();
		return outputFile;
	}

	public synchronized void toggleFullScreen()
	{
		if (inFullScreen) { // disable
			dispose();
			graphicsDevice.setFullScreenWindow(null);
			canvas.setSize(NES_HEIGHT * screenScaleFactor, NES_WIDTH * screenScaleFactor);
			setUndecorated(false);
			setVisible(true);
			inFullScreen = false;
			buildMenus(actionListener);
			// nes.resume();
		} else { // enable
			setJMenuBar(null);
			graphicsDevice = getGraphicsConfiguration().getDevice();
			if (!graphicsDevice.isFullScreenSupported()) {
				//then fullscreen will give a window the size of the screen instead
				showMessageDialog("Fullscreen is not supported by your OS or version of Java.");
				return;
			}
			dispose();
			setUndecorated(true);

			graphicsDevice.setFullScreenWindow(this);
			setVisible(true);

			DisplayMode dm = graphicsDevice.getDisplayMode();
			canvas.setSize(dm.getWidth(), dm.getHeight());

			inFullScreen = true;
		}
	}


	private int frameSkip = 0;


	//Frame is now a 256x240 array with NES color numbers from 0-3F
	//plus the state of the 3 color emphasis bits in bits 7,8,9
	public final synchronized void setFrame(final int[] nextFrame, final int[] bgColors, boolean dotcrawl)
	{
		//todo: stop running video filters while paused!
		//also move video filters into a worker thread because they
		//don't really depend on emulation state at all. Yes this is going to
		//cause more lag but it will hopefully get back up to playable speed with NTSC filter

		frameTimes[frameTimeIdx++] = nes.getFrameTime();
		frameTimeIdx %= frameTimes.length;

		if (frameTimeIdx == 0) {
			long averageFrameTime = 0;
			for (long time : frameTimes) {
				averageFrameTime += time;
			}
			averageFrameTime /= frameTimes.length;

			double fps = 1E9 / averageFrameTime;
			setTitle(String.format("HalfNES %s - %s, %2.2f fps"
					+ ((frameSkip > 0) ? " frameskip " + frameSkip : ""),
					NES.VERSION,
					nes.getCurrentRomName(),
					fps));
		}

		if (nes.frameCount % (frameSkip + 1) == 0) {
			BufferedImage frameImage = renderer.render(nextFrame, bgColors, dotcrawl);
			drawFrameImage(frameImage);
		}
	}

	private void drawFrameImage(BufferedImage frameImage)
	{
		if (frameImage == null) {
			return;
		}

		BufferStrategy bufferStrategy = canvas.getBufferStrategy();
		Graphics2D graphics = (Graphics2D) bufferStrategy.getDrawGraphics();

		if (smoothScale) {
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}

		if (inFullScreen) {
			graphics.setColor(Color.BLACK);
			DisplayMode dm = graphicsDevice.getDisplayMode();
			int scrnheight = dm.getHeight();
			int scrnwidth = dm.getWidth();
			graphics.fillRect(0, 0, scrnwidth, scrnheight);
			if (PrefsSingleton.getInstance().getBoolean("maintainAspect", true)) {
				double scaleFactor = getMinScale(scrnwidth, scrnheight);
				int height = (int) (NES_HEIGHT * scaleFactor);
				int width = (int) (256 * scaleFactor * 1.1666667);
				graphics.drawImage(frameImage, ((scrnwidth / 2) - (width / 2)),
						((scrnheight / 2) - (height / 2)),
						width,
						height,
						null);
			} else {
				graphics.drawImage(frameImage, 0, 0, scrnwidth, scrnheight, null);
			}
			graphics.setColor(Color.DARK_GRAY);
			graphics.drawString(getTitle(), 16, 16);

		} else {
			graphics.drawImage(frameImage, 0, 0, NES_WIDTH * screenScaleFactor, NES_HEIGHT * screenScaleFactor, null);
		}

		graphics.dispose();
		bufferStrategy.show();
	}

	private void showOptions()
	{
		final PreferencesDialog dialog = new PreferencesDialog(this);
		dialog.setVisible(true);
		if (dialog.okClicked()) {
			setRenderOptions();
			nes.setParameters();
		}
	}

	private void showControlsDialog()
	{
		final ControlsDialog dialog = new ControlsDialog(this);
		dialog.setVisible(true);
		if (dialog.okClicked()) {
			padController1.setButtons();
			padController2.setButtons();
		}
	}

	private void showActionReplayDialog()
	{
		nes.pause();
		final ActionReplay actionReplay = nes.getActionReplay();
		if (actionReplay != null) {
			final ActionReplayGui dialog = new ActionReplayGui(this, false, actionReplay);
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(this, "You have to load a game first.", "No ROM", JOptionPane.ERROR_MESSAGE);
		}
		nes.resume();
	}

	private void saveWindowLocation()
	{
		PrefsSingleton.getInstance().putInt("windowX", getX());
		PrefsSingleton.getInstance().putInt("windowY", getY());
	}

	private double getMinScale(final int width, final int height)
	{
		return Math.min(height / (double) NES_HEIGHT, width / (double) NES_WIDTH);
	}

	private void showMessageDialog(final String message)
	{
		JOptionPane.showMessageDialog(this, message);
	}

	private void close()
	{
		stop();
		saveWindowLocation();
		dispose();
	}
}
