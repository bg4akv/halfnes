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

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
	private Canvas canvas;
	private BufferStrategy bufferStrategy;
	private final NES nes;
	private static final long serialVersionUID = 6411494245530679723L;
	private final ActionListener actionListener;
	private int screenScaleFactor;
	private final long[] frameTimes = new long[60];
	private int frameTimeIdx = 0;
	private boolean smoothScale, inFullScreen = false;
	private GraphicsDevice graphicsDevice;
	private int NES_HEIGHT, NES_WIDTH;
	private Renderer renderer;
	private final ControllerImpl padController1, padController2;


	public MainForm()
	{
		init();
		nes = new NES(this);
		screenScaleFactor = PrefsSingleton.getInstance().getInt("screenScaling", 2);
		padController1 = new ControllerImpl(this, 0);
		padController2 = new ControllerImpl(this, 1);
		nes.setControllers(padController1, padController2);
		padController1.startEventQueue();
		padController2.startEventQueue();

		actionListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				String cmd = e.getActionCommand();
				// placeholder for more robust handler
				if (cmd.equals("Quit")) {
					close();
				} else if (cmd.equals("Reset")) {
					nes.reset();
				} else if (cmd.equals("Hard Reset")) {
					nes.reloadROM();
				} else if (cmd.equals("Pause")) {
					nes.pause();
				} else if (cmd.equals("Resume")) {
					nes.resume();
				} else if (cmd.equals("Preferences")) {
					showOptions();
				} else if (cmd.equals("Fast Forward")) {
					nes.toggleFrameLimiter();
				} else if (cmd.equals("About")) {
					messageBox("HalfNES " + NES.VERSION
							+ "\n"
							+ "Get the latest version and report any bugs at " + NES.URL + " \n"
							+ "\n"
							+ "This program is free software licensed under the GPL version 3, and comes with \n"
							+ "NO WARRANTY of any kind. (but if something's broken, please report it). \n"
							+ "See the license.txt file for details.");
				} else if (cmd.equals("ROM Info")) {
					String info = nes.getrominfo();
					if (info != null) {
						messageBox(info);
					}
				} else if (cmd.equals("Open ROM")) {
					loadROM();
				} else if (cmd.equals("Toggle Fullscreen")) {
					toggleFullScreen();
				} else if (cmd.equals("Frame Advance")) {
					nes.frameAdvance();
				} else if (cmd.equals("Escape")) {
					if (inFullScreen) {
						toggleFullScreen();
					} else {
						close();
					}
				} else if (cmd.equals("Controller Settings")) {
					showControlsDialog();
				} else if (cmd.equals("Cheat Codes")) {
					showActionReplayDialog();
				}
			}
		};
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
		bufferStrategy = canvas.getBufferStrategy();
	}

	public void start(String[] args)
	{
		if (args == null || args.length < 1 || args[0] == null) {
			nes.start();
		} else {
			nes.run(args[0]);
		}
	}

	private synchronized void init()
	{
		//construct window
		setTitle("HalfNES " + NES.VERSION);
		setResizable(false);
		buildMenus();
		setRenderOptions();

		getRootPane().registerKeyboardAction(actionListener, "Escape",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(actionListener, "Toggle Fullscreen",
				KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(actionListener, "Quit",
				KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
		setLocation(PrefsSingleton.getInstance().getInt("windowX", 0),
				PrefsSingleton.getInstance().getInt("windowY", 0));

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

	public void buildMenus()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu("File");
		menuBar.add(menu);

		JMenuItem item;
		menu.add(item = new JMenuItem("Open ROM"));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.addSeparator();

		menu.add(item = new JMenuItem("Preferences"));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.addSeparator();

		menu.add(item = new JMenuItem("Toggle Fullscreen"));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
		item.addActionListener(actionListener);

		menu.add(item = new JMenuItem("Quit"));
		item.addActionListener(actionListener);
		menuBar.add(menu);

		menu = new JMenu("NES");
		menuBar.add(menu);

		menu.add(item = new JMenuItem("Reset"));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.add(item = new JMenuItem("Hard Reset"));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.add(item = new JMenuItem("Pause"));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));

		menu.add(item = new JMenuItem("Resume"));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));

		menu.add(item = new JMenuItem("Fast Forward"));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.add(item = new JMenuItem("Frame Advance"));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.addSeparator();

		menu.add(item = new JMenuItem("Controller Settings"));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.add(item = new JMenuItem("Cheat Codes"));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu.addSeparator();

		menu.add(item = new JMenuItem("ROM Info"));
		item.addActionListener(actionListener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menu = new JMenu("Help");
		menuBar.add(menu);

		menu.add(item = new JMenuItem("About"));
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
		if (path.endsWith(".zip") || path.endsWith(".ZIP")) {
			try {
				loadRomFromZip(path);
			} catch (IOException ex) {
				messageBox("Could not load file:\nFile does not exist or is not a valid NES game.\n" + ex.getMessage());
			}
		} else {
			nes.loadROM(path);
		}
	}

	private void loadRomFromZip(String zipName) throws IOException
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
			if (!entry.isDirectory() && (entry.getName().endsWith(".nes")
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
		if (romNames.size() > 1) {
			return (String) JOptionPane.showInputDialog(this,
					"Select ROM to load", "Select ROM to load",
					JOptionPane.PLAIN_MESSAGE, null,
					romNames.toArray(), romNames.get(0));
		} else if (romNames.size() == 1) {
			return romNames.get(0);
		}
		return null;
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
			messageBox("Cannot extract file. File " + outputFile.getCanonicalPath() + " already exists.");
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
			buildMenus();
			// nes.resume();
		} else { // enable
			setJMenuBar(null);
			graphicsDevice = getGraphicsConfiguration().getDevice();
			if (!graphicsDevice.isFullScreenSupported()) {
				//then fullscreen will give a window the size of the screen instead
				messageBox("Fullscreen is not supported by your OS or version of Java.");
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

	public void messageBox(final String message)
	{
		JOptionPane.showMessageDialog(this, message);
	}

	private BufferedImage frame;
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
			long averageFrames = 0;
			for (long l : frameTimes) {
				averageFrames += l;
			}

			averageFrames /= frameTimes.length;
			double fps = 1E9 / averageFrames;
			setTitle(String.format("HalfNES %s - %s, %2.2f fps"
					+ ((frameSkip > 0) ? " frameskip " + frameSkip : ""),
					NES.VERSION,
					nes.getCurrentRomName(),
					fps));
		}

		if (nes.frameCount % (frameSkip + 1) == 0) {
			frame = renderer.render(nextFrame, bgColors, dotcrawl);
			drawImage();
		}
	}

	public final synchronized void drawImage()
	{
		final Graphics2D graphics = (Graphics2D) bufferStrategy.getDrawGraphics();
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
				graphics.drawImage(frame, ((scrnwidth / 2) - (width / 2)),
						((scrnheight / 2) - (height / 2)),
						width,
						height,
						null);
			} else {
				graphics.drawImage(frame, 0, 0,
						scrnwidth,
						scrnheight,
						null);
			}
			graphics.setColor(Color.DARK_GRAY);
			graphics.drawString(getTitle(), 16, 16);

		} else {
			graphics.drawImage(frame, 0, 0, NES_WIDTH * screenScaleFactor, NES_HEIGHT * screenScaleFactor, null);
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

	private void close()
	{
		dispose();
		saveWindowLocation();
		padController1.stopEventQueue();
		padController2.stopEventQueue();
		nes.stop();
	}
}
