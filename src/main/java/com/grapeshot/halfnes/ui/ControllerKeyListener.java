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
import java.util.prefs.Preferences;

import javafx.scene.Scene;
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
	private Controller gameController;
	private Component[] buttons;
	private int latchByte = 0, controllerByte = 0, prevByte = 0, outByte = 0, gamepadByte = 0;
	private final HashMap<Integer, Integer> m = new HashMap<>(10);
	private final int controllernum;
	private final ThreadLoop loop;


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
		setButtons();

		loop = new ThreadLoop(() ->{
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
		if (!m.containsKey(keyCode)) {
			return;
		}
		//enable the corresponding bit to the key
		controllerByte |= m.get(keyCode);
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
		if (!m.containsKey(keyCode)) {
			return;
		}
		controllerByte &= ~m.get(keyCode);
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
	public void keyTyped(final KeyEvent arg0)
	{
		// TODO Auto-generated method stub
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

	/**
	 * Start in a separate thread the processing of the controller event queue.
	 * Must be called after construction of the class to enable the processing
	 * of the joystick / gamepad events.
	 */
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

	/**
	 * Stop the controller event queue thread. Must be called before closing the
	 * application.
	 */
	public void stop()
	{
		loop.stop();
	}

	/**
	 * This method detects the available joysticks / gamepads on the computer
	 * and return them in a list.
	 *
	 * @return List of available joysticks / gamepads connected to the computer
	 */
	private static Controller[] getAvailablePadControllers()
	{
		List<Controller> gameControllers = new ArrayList<>();
		// Get a list of the controllers JInput knows about and can interact
		// with
		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
		// Check the useable controllers (gamepads or joysticks with at least 2
		// axis and 2 buttons)
		for (Controller controller : controllers) {
			if ((controller.getType() == Controller.Type.GAMEPAD) || (controller.getType() == Controller.Type.STICK)) {
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

		return gameControllers.toArray(new Controller[0]);
	}

	/**
	 * Return the available buttons on this controller (by priority order).
	 */
	private static Component[] getButtons(Controller controller)
	{
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

	public final void setButtons()
	{
		Preferences prefs = PrefsSingleton.getInstance();
		//reset the buttons from prefs
		m.clear();
		switch (controllernum) {
		case 0:
			m.put(prefs.getInt("keyUp1", KeyEvent.VK_UP), BIT4);
			m.put(prefs.getInt("keyDown1", KeyEvent.VK_DOWN), BIT5);
			m.put(prefs.getInt("keyLeft1", KeyEvent.VK_LEFT), BIT6);
			m.put(prefs.getInt("keyRight1", KeyEvent.VK_RIGHT), BIT7);
			m.put(prefs.getInt("keyA1", KeyEvent.VK_X), BIT0);
			m.put(prefs.getInt("keyB1", KeyEvent.VK_Z), BIT1);
			m.put(prefs.getInt("keySelect1", KeyEvent.VK_SHIFT), BIT2);
			m.put(prefs.getInt("keyStart1", KeyEvent.VK_ENTER), BIT3);
			break;
		case 1:
		default:
			m.put(prefs.getInt("keyUp2", KeyEvent.VK_W), BIT4);
			m.put(prefs.getInt("keyDown2", KeyEvent.VK_S), BIT5);
			m.put(prefs.getInt("keyLeft2", KeyEvent.VK_A), BIT6);
			m.put(prefs.getInt("keyRight2", KeyEvent.VK_D), BIT7);
			m.put(prefs.getInt("keyA2", KeyEvent.VK_G), BIT0);
			m.put(prefs.getInt("keyB2", KeyEvent.VK_F), BIT1);
			m.put(prefs.getInt("keySelect2", KeyEvent.VK_R), BIT2);
			m.put(prefs.getInt("keyStart2", KeyEvent.VK_T), BIT3);
			break;

		}

		Controller[] controllers = getAvailablePadControllers();
		if (controllers.length > controllernum) {
			this.gameController = controllers[controllernum];
			PrefsSingleton.getInstance().put("controller" + controllernum, gameController.getName());
			System.err.println(controllernum + 1 + ". " + gameController.getName());
			this.buttons = getButtons(controllers[controllernum]);
		} else {
			PrefsSingleton.getInstance().put("controller" + controllernum, "");
			this.gameController = null;
			this.buttons = null;
		}
	}
}
