/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */

/*
 * OptionsDialog.java
 *
 * Created on Jan 16, 2011, 4:21:43 PM
 */
package com.grapeshot.halfnes.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javafx.util.Pair;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

import com.grapeshot.halfnes.PrefsSingleton;

/**
 *
 * @author Andrew
 */
public class ControlsDialog extends JDialog {
	private JButton button1;
	private JButton button2;
	private JButton buttonCancel;
	private JButton buttonOK;
	private JTextField textField1A;
	private JTextField textField1B;
	private JTextField textField1Down;
	private JTextField textField1Left;
	private JTextField textField1Right;
	private JTextField textField1Select;
	private JTextField textField1Start;
	private JTextField textField1Up;
	private JTextField textField2A;
	private JTextField textField2B;
	private JTextField textField2Down;
	private JTextField textField2Left;
	private JTextField textField2Right;
	private JTextField textField2Select;
	private JTextField textField2Start;
	private JTextField textField2Up;
	private JLabel label1;
	private JLabel label10;
	private JLabel label11;
	private JLabel label12;
	private JLabel label13;
	private JLabel label16;
	private JLabel label3;
	private JLabel label4;
	private JLabel label5;
	private JLabel label6;
	private JLabel label7;
	private JLabel label8;
	private JLabel label9;
	private JLabel labelCtrl1;
	private JLabel labelCtrl2;

	private final Preferences prefs = PrefsSingleton.getInstance();
	private boolean okClicked = false;

	private final Map<String, Integer> map = new HashMap<String, Integer>();


	public ControlsDialog(Frame parent)
	{
		super(parent, true);
		if (parent != null) {
			setLocation(parent.getLocation());
		}

		for (Entry<String, Pair<Integer, Integer>> entry : ControllerKeyListener.keyMap.entrySet()) {
			String key = entry.getKey();
			Pair<Integer, Integer> value = entry.getValue();
			map.put(key, prefs.getInt(key, value.getKey()));
		}

		setTitle("HalfNES Controller Settings");
		initComponents();

		//set all of the text boxes
		textField1Up.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL1_KEY_UP)));
		textField1Down.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL1_KEY_DN)));
		textField1Left.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL1_KEY_LF)));
		textField1Right.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL1_KEY_RT)));
		textField1A.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL1_KEY_A)));
		textField1B.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL1_KEY_B)));
		textField1Select.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL1_KEY_SL)));
		textField1Start.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL1_KEY_ST)));

		textField2Up.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL2_KEY_UP)));
		textField2Down.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL2_KEY_DN)));
		textField2Left.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL2_KEY_LF)));
		textField2Right.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL2_KEY_RT)));
		textField2A.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL2_KEY_A)));
		textField2B.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL2_KEY_B)));
		textField2Select.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL2_KEY_SL)));
		textField2Start.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.CTRL2_KEY_ST)));

		//set the controller text if we've detected some
		String ctrl1 = prefs.get("controller0", "");
		String ctrl2 = prefs.get("controller1", "");
		if (!ctrl1.isEmpty()) {
			labelCtrl1.setText(ctrl1);
		}
		if (!ctrl2.isEmpty()) {
			labelCtrl2.setText(ctrl2);
		}
		buttonOK.setActionCommand("OK");
		buttonCancel.setActionCommand("Cancel");
	}


	private void initComponents()
	{
		label3 = new JLabel();
		label4 = new JLabel();
		label5 = new JLabel();
		label6 = new JLabel();
		label7 = new JLabel();
		label8 = new JLabel();
		label9 = new JLabel();
		label10 = new JLabel();
		label11 = new JLabel();
		label12 = new JLabel();
		label13 = new JLabel();
		textField1Up = new JTextField();
		textField1Down = new JTextField();
		textField1Right = new JTextField();
		textField1Left = new JTextField();
		textField1Start = new JTextField();
		textField1Select = new JTextField();
		textField1B = new JTextField();
		textField1A = new JTextField();
		textField2Up = new JTextField();
		textField2Down = new JTextField();
		textField2Left = new JTextField();
		textField2Right = new JTextField();
		textField2A = new JTextField();
		textField2B = new JTextField();
		textField2Select = new JTextField();
		textField2Start = new JTextField();
		buttonCancel = new JButton();
		buttonOK = new JButton();
		label16 = new JLabel();
		label1 = new JLabel();
		labelCtrl1 = new JLabel();
		button1 = new JButton();
		button2 = new JButton();
		labelCtrl2 = new JLabel();


		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setName("ControlsDialog");
		setResizable(false);

		label3.setFont(new Font("Tahoma", 1, 11));
		label3.setText("Keybindings:");

		label4.setText("Controller 1");
		label5.setText("Controller 2");

		label6.setText("Up");
		label7.setText("Down");
		label8.setText("Left");
		label9.setText("Right");
		label10.setText("A");
		label11.setText("B");
		label12.setText("Select");
		label13.setText("Start");

		textField1Up.setMinimumSize(new Dimension(120, 20));
		textField1Up.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL1_KEY_UP, textField1Up);
			}
		});

		textField1Down.setMinimumSize(new Dimension(120, 20));
		textField1Down.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL1_KEY_DN, textField1Down);
			}
		});

		textField1Right.setMinimumSize(new Dimension(120, 20));
		textField1Right.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL1_KEY_RT, textField1Right);
			}
		});

		textField1Left.setMinimumSize(new Dimension(120, 20));
		textField1Left.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL1_KEY_LF, textField1Left);
			}
		});

		textField1Start.setMinimumSize(new Dimension(120, 20));
		textField1Start.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL1_KEY_ST, textField1Start);
			}
		});

		textField1Select.setMinimumSize(new Dimension(120, 20));
		textField1Select.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL1_KEY_SL, textField1Select);
			}
		});

		textField1B.setMinimumSize(new Dimension(120, 20));
		textField1B.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL1_KEY_B, textField1B);
			}
		});

		textField1A.setMinimumSize(new Dimension(120, 20));
		textField1A.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL1_KEY_A, textField1A);
			}
		});

		textField2Up.setMinimumSize(new Dimension(120, 20));
		textField2Up.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL2_KEY_UP, textField2Up);
			}
		});

		textField2Down.setMinimumSize(new Dimension(120, 20));
		textField2Down.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL2_KEY_DN, textField2Down);
			}
		});

		textField2Left.setMinimumSize(new Dimension(120, 20));
		textField2Left.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL2_KEY_LF, textField2Left);
			}
		});

		textField2Right.setMinimumSize(new Dimension(120, 20));
		textField2Right.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL2_KEY_RT, textField2Right);
			}
		});

		textField2A.setMinimumSize(new Dimension(120, 20));
		textField2A.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL2_KEY_A, textField2A);
			}
		});

		textField2B.setMinimumSize(new Dimension(120, 20));
		textField2B.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL2_KEY_B, textField2B);
			}
		});

		textField2Select.setMinimumSize(new Dimension(120, 20));
		textField2Select.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL2_KEY_SL, textField2Select);
			}
		});

		textField2Start.setMinimumSize(new Dimension(120, 20));
		textField2Start.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.CTRL2_KEY_ST, textField2Start);
			}
		});

		buttonCancel.setText("Cancel");
		buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				setVisible(false);
			}
		});

		buttonOK.setText("OK");
		buttonOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				for (Entry<String, Integer> entry : map.entrySet()) {
					prefs.putInt(entry.getKey(), entry.getValue());
				}

				try {
					prefs.flush();
				} catch (Exception ex) {
					;
				}

				okClicked = true;
				setVisible(false);
			}
		});

		label16.setText(" Click in text box and type a key to change that binding.");

		label1.setFont(new Font("Tahoma", 1, 11));
		label1.setText("Detected Game Controllers:");

		labelCtrl1.setText("No Player 1 controller connected");
		labelCtrl1.setEnabled(false);

		button1.setText("Set Buttons");
		button1.setEnabled(false);
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				;
			}
		});

		button2.setText("Set Buttons");
		button2.setEnabled(false);
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				;
			}
		});

		labelCtrl2.setText("No Player 2 controller connected");
		labelCtrl2.setEnabled(false);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(label1)
					.addComponent(label16)
					.addGroup(layout.createSequentialGroup()
						.addGap(49, 49, 49)
						.addComponent(label4)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(label5))
					.addComponent(label3)
					.addGroup(layout.createSequentialGroup()
						.addGap(10, 10, 10)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
							.addComponent(label7)
							.addComponent(label8)
							.addComponent(label9)
							.addComponent(label10)
							.addComponent(label11)
							.addComponent(label13)
							.addComponent(label6)
							.addComponent(label12))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(textField1Right, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(textField1Left, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(textField1B, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(textField1A, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(textField1Start, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(textField1Select, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(textField1Up, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(textField1Down, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(textField2Select, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(textField2B, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(textField2Start, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(textField2A, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(textField2Right, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(textField2Up, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(textField2Down, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(textField2Left, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)))
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addGroup(layout.createSequentialGroup()
							.addComponent(buttonOK, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(buttonCancel))
						.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(labelCtrl1)
								.addComponent(labelCtrl2))
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(button2)
								.addComponent(button1))))))
		);

		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] {textField1A, textField1B, textField1Down, textField1Left, textField1Right, textField1Select, textField1Start, textField1Up, textField2A, textField2B, textField2Down, textField2Left, textField2Right, textField2Select, textField2Start, textField2Up});

		layout.setVerticalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addComponent(label3)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(label16, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
				.addGap(11, 11, 11)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(label4)
					.addComponent(label5))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(textField2Up, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(label6)
									.addComponent(textField1Up, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(textField1Down, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(label7))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(textField1Left, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(label8))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(textField1Right, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(label9)
									.addComponent(textField2Right, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(textField1A, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(label10)
									.addComponent(textField2A, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
							.addGroup(layout.createSequentialGroup()
								.addGap(31, 31, 31)
								.addComponent(textField2Down, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(textField2Left, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(textField1B, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(label11)
							.addComponent(textField2B, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(textField1Select, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(label12)
							.addComponent(textField2Select, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(textField1Start, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(label13)
							.addComponent(textField2Start, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(label1)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(labelCtrl1)
					.addComponent(button2))
				.addGap(12, 12, 12)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(labelCtrl2)
					.addComponent(button1))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(buttonOK)
					.addComponent(buttonCancel))
				.addGap(0, 8, Short.MAX_VALUE))
		);

		layout.linkSize(SwingConstants.VERTICAL, new Component[] {textField1A, textField1B, textField1Down, textField1Left, textField1Right, textField1Select, textField1Start, textField1Up, textField2A, textField2B, textField2Down, textField2Left, textField2Right, textField2Select, textField2Start, textField2Up});

		pack();
	}


	public boolean okClicked()
	{
		return okClicked;
	}

	private void keyAction(KeyEvent event, String key, JTextField textField)
	{
		int keyCode = event.getKeyCode();
		map.put(key, keyCode);
		textField.setText(KeyEvent.getKeyText(keyCode));
	}
}
