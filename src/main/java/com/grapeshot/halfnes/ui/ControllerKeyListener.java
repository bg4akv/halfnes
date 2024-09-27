/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.grapeshot.halfnes.ui;

import static com.grapeshot.halfnes.utils.BIT0;
import static com.grapeshot.halfnes.utils.BIT1;
import static com.grapeshot.halfnes.utils.BIT2;
import static com.grapeshot.halfnes.utils.BIT3;
import static com.grapeshot.halfnes.utils.BIT4;
import static com.grapeshot.halfnes.utils.BIT5;
import static com.grapeshot.halfnes.utils.BIT6;
import static com.grapeshot.halfnes.utils.BIT7;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javafx.scene.Scene;
import javafx.util.Pair;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

import com.grapeshot.halfnes.PrefsSingleton;
import com.grapeshot.halfnes.util.ThreadLoop;

/**
 *
 * @author Andrew, Zlika This class uses the JInput Java game controller API
 * (cf. http://java.net/projects/jinput).
 */
public class ControllerKeyListener implements KeyListener {
	public static final String KEY_CTRL1_KEYUP = "controller1.keyUp";
	public static final String KEY_CTRL1_KEYDN = "controller1.keyDown";
	public static final String KEY_CTRL1_KEYLF = "controller1.keyLeft";
	public static final String KEY_CTRL1_KEYRT = "controller1.keyRight";
	public static final String KEY_CTRL1_KEYA = "controller1.keyA";
	public static final String KEY_CTRL1_KEYB = "controller1.keyB";
	public static final String KEY_CTRL1_KEYSL = "controller1.keySelect";
	public static final String KEY_CTRL1_KEYST = "controller1.keyStart";

	public static final String KEY_CTRL2_KEYUP = "controller2.keyUp";
	public static final String KEY_CTRL2_KEYDN = "controller2.keyDown";
	public static final String KEY_CTRL2_KEYLF = "controller2.keyLeft";
	public static final String KEY_CTRL2_KEYRT = "controller2.keyRight";
	public static final String KEY_CTRL2_KEYA = "controller2.keyA";
	public static final String KEY_CTRL2_KEYB = "controller2.keyB";
	public static final String KEY_CTRL2_KEYSL = "controller2.keySelect";
	public static final String KEY_CTRL2_KEYST = "controller2.keyStart";


	private final Preferences prefs = PrefsSingleton.getInstance();
	private Controller gameController;
	private Component[] buttons;
	private int latchByte = 0, controllerByte = 0, prevByte = 0, outByte = 0, gamepadByte = 0;
	private final Map<Integer, Integer> map = new HashMap<>();
	private final int controllernum;
	private final ThreadLoop loop;

	public static final Map<String, Pair<Integer, Integer>> keyMap = new HashMap<String, Pair<Integer, Integer>>() {{
		put(KEY_CTRL1_KEYA, new Pair<Integer, Integer>(KeyEvent.VK_X, BIT0));
		put(KEY_CTRL1_KEYB, new Pair<Integer, Integer>(KeyEvent.VK_Z, BIT1));
		put(KEY_CTRL1_KEYSL, new Pair<Integer, Integer>(KeyEvent.VK_SHIFT, BIT2));
		put(KEY_CTRL1_KEYST, new Pair<Integer, Integer>(KeyEvent.VK_ENTER, BIT3));
		put(KEY_CTRL1_KEYUP, new Pair<Integer, Integer>(KeyEvent.VK_UP, BIT4));
		put(KEY_CTRL1_KEYDN, new Pair<Integer, Integer>(KeyEvent.VK_DOWN, BIT5));
		put(KEY_CTRL1_KEYLF, new Pair<Integer, Integer>(KeyEvent.VK_LEFT, BIT6));
		put(KEY_CTRL1_KEYRT, new Pair<Integer, Integer>(KeyEvent.VK_RIGHT, BIT7));

		put(KEY_CTRL2_KEYA, new Pair<Integer, Integer>(KeyEvent.VK_G, BIT0));
		put(KEY_CTRL2_KEYB, new Pair<Integer, Integer>(KeyEvent.VK_F, BIT1));
		put(KEY_CTRL2_KEYSL, new Pair<Integer, Integer>(KeyEvent.VK_R, BIT2));
		put(KEY_CTRL2_KEYST, new Pair<Integer, Integer>(KeyEvent.VK_T, BIT3));
		put(KEY_CTRL2_KEYUP, new Pair<Integer, Integer>(KeyEvent.VK_W, BIT4));
		put(KEY_CTRL2_KEYDN, new Pair<Integer, Integer>(KeyEvent.VK_S, BIT5));
		put(KEY_CTRL2_KEYLF, new Pair<Integer, Integer>(KeyEvent.VK_A, BIT6));
		put(KEY_CTRL2_KEYRT, new Pair<Integer, Integer>(KeyEvent.VK_D, BIT7));
	}};

	public ControllerKeyListener(final Scene scene, final int controllernum)
	{
		this(controllernum);
		scene.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> pressKey(e.getCode().impl_getCode()));
		scene.addEventHandler(javafx.scene.input.KeyEvent.KEY_RELEASED, e -> releaseKey(e.getCode().impl_getCode()));
	}

	public ControllerKeyListener(final int controllernum)
	{
		if ((controllernum != 0) && (controllernum != 1)) {
			throw new IllegalArgumentException("controllerNum must be 0 or 1");
		}

		this.controllernum = controllernum;
		updateButtons();

		loop = new ThreadLoop(() -> {
			if (gameController == null) {
				return;
			}

			final double threshold = 0.25;
			Event event = new Event();
			gameController.poll();
			EventQueue queue = gameController.getEventQueue();

			while (queue.getNextEvent(event)) {
				Component component = event.getComponent();
				if (component.getIdentifier() == Component.Identifier.Axis.X) {
					if (event.getValue() > threshold) {
						gamepadByte |= BIT7;//left on, right off
						gamepadByte &= ~BIT6;
					} else if (event.getValue() < -threshold) {
						gamepadByte |= BIT6;
						gamepadByte &= ~BIT7;
					} else {
						gamepadByte &= ~(BIT7 | BIT6);
					}
				} else if (component.getIdentifier() == Component.Identifier.Axis.Y) {
					if (event.getValue() > threshold) {
						gamepadByte |= BIT5;//up on, down off
						gamepadByte &= ~BIT4;
					} else if (event.getValue() < -threshold) {
						gamepadByte |= BIT4;//down on, up off
						gamepadByte &= ~BIT5;
					} else {
						gamepadByte &= ~(BIT4 | BIT5);
					}
				} else if (component == buttons[0]) {
					if (isPressed(event)) {
						gamepadByte |= BIT0;
					} else {
						gamepadByte &= ~BIT0;
					}
				} else if (component == buttons[1]) {
					if (isPressed(event)) {
						gamepadByte |= BIT1;
					} else {
						gamepadByte &= ~BIT1;
					}
				} else if (component == buttons[2]) {
					if (isPressed(event)) {
						gamepadByte |= BIT2;
					} else {
						gamepadByte &= ~BIT2;
					}
				} else if (component == buttons[3]) {
					if (isPressed(event)) {
						gamepadByte |= BIT3;
					} else {
						gamepadByte &= ~BIT3;
					}
				}
			}
		}, 5);
	}

	@Override
	public void keyPressed(final KeyEvent keyEvent)
	{
		pressKey(keyEvent.getKeyCode());
	}

	private void pressKey(int keyCode)
	{
		//enable the byte of whatever is found
		prevByte = controllerByte;
		if (!map.containsKey(keyCode)) {
			return;
		}
		//enable the corresponding bit to the key
		controllerByte |= map.get(keyCode);
		//special case: if up and down are pressed at once, use whichever was pressed previously
		if ((controllerByte & (BIT4 | BIT5)) == (BIT4 | BIT5)) {
			controllerByte &= ~(BIT4 | BIT5);
			controllerByte |= (prevByte & ~(BIT4 | BIT5));
		}
		//same for left and right
		if ((controllerByte & (BIT6 | BIT7)) == (BIT6 | BIT7)) {
			controllerByte &= ~(BIT6 | BIT7);
			controllerByte |= (prevByte & ~(BIT6 | BIT7));
		}
	}

	@Override
	public void keyReleased(final KeyEvent keyEvent)
	{
		releaseKey(keyEvent.getKeyCode());
	}

	private void releaseKey(int keyCode)
	{
		prevByte = controllerByte;
		if (!map.containsKey(keyCode)) {
			return;
		}
		controllerByte &= ~map.get(keyCode);
	}

	public int getByte()
	{
		return outByte;
	}

	public int peekOutput()
	{
		return latchByte;
	}

	@Override
	public void keyTyped(final KeyEvent keyEvent)
	{
		;
	}

	public void strobe()
	{
		//shifts a byte out
		outByte = latchByte & 1;
		latchByte = ((latchByte >> 1) | 0x100);
	}

	public void output(final boolean state)
	{
		latchByte = gamepadByte | controllerByte;
	}

	public void start()
	{
		loop.start();
	}

	private boolean isPressed(Event event)
	{
		Component component = event.getComponent();
		if (component.isAnalog()) {
			if (Math.abs(event.getValue()) > 0.2f) {
				return true;
			} else {
				return false;
			}
		} else if (event.getValue() == 0) {
			return false;
		} else {
			return true;
		}
	}

	public void stop()
	{
		loop.stop();
	}


	private List<Controller> getGameControllers()
	{
		List<Controller> gameControllers = new ArrayList<>();
		// Get a list of the controllers JInput knows about and can interact
		// with
		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
		// Check the useable controllers (gamepads or joysticks with at least 2
		// axis and 2 buttons)
		for (Controller controller : controllers) {
			if ((controller.getType() == Controller.Type.GAMEPAD)
				|| (controller.getType() == Controller.Type.STICK)) {

				int nbOfAxis = 0;
				// Get this controllers components (buttons and axis)
				Component[] components = controller.getComponents();
				// Check the availability of X/Y axis and at least 2 buttons
				// (for A and B, because select and start can use the keyboard)
				for (Component component : components) {
					if ((component.getIdentifier() == Component.Identifier.Axis.X)
						|| (component.getIdentifier() == Component.Identifier.Axis.Y)) {
						nbOfAxis++;
					}
				}
				if ((nbOfAxis >= 2) && (getButtons(controller).length >= 2)) {
					// Valid game controller
					gameControllers.add(controller);
				}
			}
		}

		return gameControllers;
	}

	/**
	 * Return the available buttons on this controller (by priority order).
	 */
	private Component[] getButtons(Controller controller)
	{
		if (controller == null) {
			return null;
		}
		List<Component> buttons = new ArrayList<>();
		// Get this controllers components (buttons and axis)
		Component[] components = controller.getComponents();
		for (Component component : components) {
			if (component.getIdentifier() instanceof Component.Identifier.Button) {
				buttons.add(component);
			}
		}
		return buttons.toArray(new Component[0]);
	}

	public final void updateButtons()
	{
		map.clear();
		for (Entry<String, Pair<Integer, Integer>> entry : keyMap.entrySet()) {
			Pair<Integer, Integer> value = entry.getValue();
			map.put(prefs.getInt(entry.getKey(), value.getKey()), value.getValue());
		}

		List<Controller> controllers = getGameControllers();
		if (controllernum < controllers.size()) {
			gameController = controllers.get(controllernum);
			PrefsSingleton.getInstance().put("controller" + controllernum, gameController.getName());
			System.err.println(controllernum + 1 + ". " + gameController.getName());
			this.buttons = getButtons(controllers.get(controllernum));
		} else {
			prefs.put("controller" + controllernum, "");
			this.gameController = null;
			this.buttons = null;
		}
	}
}
