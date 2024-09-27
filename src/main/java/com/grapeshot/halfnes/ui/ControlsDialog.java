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

import javax.swing.ButtonGroup;
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
	private ButtonGroup buttonGroup1;
	private JButton jButton1;
	private JButton jButton2;
	private JButton jButtonCancel;
	private JButton jButtonOK;
	private JTextField jField1A;
	private JTextField jField1B;
	private JTextField jField1Down;
	private JTextField jField1Left;
	private JTextField jField1Right;
	private JTextField jField1Select;
	private JTextField jField1Start;
	private JTextField jField1Up;
	private JTextField jField2A;
	private JTextField jField2B;
	private JTextField jField2Down;
	private JTextField jField2Left;
	private JTextField jField2Right;
	private JTextField jField2Select;
	private JTextField jField2Start;
	private JTextField jField2Up;
	private JLabel jLabel1;
	private JLabel jLabel10;
	private JLabel jLabel11;
	private JLabel jLabel12;
	private JLabel jLabel13;
	private JLabel jLabel16;
	private JLabel jLabel3;
	private JLabel jLabel4;
	private JLabel jLabel5;
	private JLabel jLabel6;
	private JLabel jLabel7;
	private JLabel jLabel8;
	private JLabel jLabel9;
	private JLabel jLabelCtrl1;
	private JLabel jLabelCtrl2;

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
		jField1Up.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL1_KEYUP)));
		jField1Down.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL1_KEYDN)));
		jField1Left.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL1_KEYLF)));
		jField1Right.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL1_KEYRT)));
		jField1A.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL1_KEYA)));
		jField1B.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL1_KEYB)));
		jField1Select.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL1_KEYSL)));
		jField1Start.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL1_KEYST)));

		jField2Up.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL2_KEYUP)));
		jField2Down.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL2_KEYDN)));
		jField2Left.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL2_KEYLF)));
		jField2Right.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL2_KEYRT)));
		jField2A.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL2_KEYA)));
		jField2B.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL2_KEYB)));
		jField2Select.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL2_KEYSL)));
		jField2Start.setText(KeyEvent.getKeyText(map.get(ControllerKeyListener.KEY_CTRL2_KEYST)));

		//set the controller text if we've detected some
		String ctrl1 = prefs.get("controller0", "");
		String ctrl2 = prefs.get("controller1", "");
		if (!ctrl1.isEmpty()) {
			jLabelCtrl1.setText(ctrl1);
		}
		if (!ctrl2.isEmpty()) {
			jLabelCtrl2.setText(ctrl2);
		}
		jButtonOK.setActionCommand("OK");
		jButtonCancel.setActionCommand("Cancel");
	}


	private void initComponents()
	{
		buttonGroup1 = new ButtonGroup();
		jLabel3 = new JLabel();
		jLabel4 = new JLabel();
		jLabel5 = new JLabel();
		jLabel6 = new JLabel();
		jLabel7 = new JLabel();
		jLabel8 = new JLabel();
		jLabel9 = new JLabel();
		jLabel10 = new JLabel();
		jLabel11 = new JLabel();
		jLabel12 = new JLabel();
		jLabel13 = new JLabel();
		jField1Up = new JTextField();
		jField1Down = new JTextField();
		jField1Right = new JTextField();
		jField1Left = new JTextField();
		jField1Start = new JTextField();
		jField1Select = new JTextField();
		jField1B = new JTextField();
		jField1A = new JTextField();
		jField2Up = new JTextField();
		jField2Down = new JTextField();
		jField2Left = new JTextField();
		jField2Right = new JTextField();
		jField2A = new JTextField();
		jField2B = new JTextField();
		jField2Select = new JTextField();
		jField2Start = new JTextField();
		jButtonCancel = new JButton();
		jButtonOK = new JButton();
		jLabel16 = new JLabel();
		jLabel1 = new JLabel();
		jLabelCtrl1 = new JLabel();
		jButton1 = new JButton();
		jButton2 = new JButton();
		jLabelCtrl2 = new JLabel();


		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setName("ControlsDialog");
		setResizable(false);

		jLabel3.setFont(new Font("Tahoma", 1, 11));
		jLabel3.setText("Keybindings:");

		jLabel4.setText("Controller 1");
		jLabel5.setText("Controller 2");

		jLabel6.setText("Up");
		jLabel7.setText("Down");
		jLabel8.setText("Left");
		jLabel9.setText("Right");
		jLabel10.setText("A");
		jLabel11.setText("B");
		jLabel12.setText("Select");
		jLabel13.setText("Start");

		jField1Up.setMinimumSize(new Dimension(120, 20));
		jField1Up.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL1_KEYUP, jField1Up);
			}
		});

		jField1Down.setMinimumSize(new Dimension(120, 20));
		jField1Down.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL1_KEYDN, jField1Down);
			}
		});

		jField1Right.setMinimumSize(new Dimension(120, 20));
		jField1Right.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL1_KEYRT, jField1Right);
			}
		});

		jField1Left.setMinimumSize(new Dimension(120, 20));
		jField1Left.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL1_KEYLF, jField1Left);
			}
		});

		jField1Start.setMinimumSize(new Dimension(120, 20));
		jField1Start.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL1_KEYST, jField1Start);
			}
		});

		jField1Select.setMinimumSize(new Dimension(120, 20));
		jField1Select.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL1_KEYSL, jField1Select);
			}
		});

		jField1B.setMinimumSize(new Dimension(120, 20));
		jField1B.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL1_KEYB, jField1B);
			}
		});

		jField1A.setMinimumSize(new Dimension(120, 20));
		jField1A.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL1_KEYA, jField1A);
			}
		});

		jField2Up.setMinimumSize(new Dimension(120, 20));
		jField2Up.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL2_KEYUP, jField2Up);
			}
		});

		jField2Down.setMinimumSize(new Dimension(120, 20));
		jField2Down.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL2_KEYDN, jField2Down);
			}
		});

		jField2Left.setMinimumSize(new Dimension(120, 20));
		jField2Left.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL2_KEYLF, jField2Left);
			}
		});

		jField2Right.setMinimumSize(new Dimension(120, 20));
		jField2Right.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL2_KEYRT, jField2Right);
			}
		});

		jField2A.setMinimumSize(new Dimension(120, 20));
		jField2A.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL2_KEYA, jField2A);
			}
		});

		jField2B.setMinimumSize(new Dimension(120, 20));
		jField2B.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL2_KEYB, jField2B);
			}
		});

		jField2Select.setMinimumSize(new Dimension(120, 20));
		jField2Select.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL2_KEYSL, jField2Select);
			}
		});

		jField2Start.setMinimumSize(new Dimension(120, 20));
		jField2Start.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt)
			{
				keyAction(evt, ControllerKeyListener.KEY_CTRL2_KEYST, jField2Start);
			}
		});

		jButtonCancel.setText("Cancel");
		jButtonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				setVisible(false);
			}
		});

		jButtonOK.setText("OK");
		jButtonOK.addActionListener(new ActionListener() {
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

		jLabel16.setText(" Click in text box and type a key to change that binding.");

		jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
		jLabel1.setText("Detected Game Controllers:");

		jLabelCtrl1.setText("No Player 1 controller connected");
		jLabelCtrl1.setEnabled(false);

		jButton1.setText("Set Buttons");
		jButton1.setEnabled(false);
		jButton1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				;
			}
		});

		jButton2.setText("Set Buttons");
		jButton2.setEnabled(false);
		jButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				;
			}
		});

		jLabelCtrl2.setText("No Player 2 controller connected");
		jLabelCtrl2.setEnabled(false);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(jLabel1)
					.addComponent(jLabel16)
					.addGroup(layout.createSequentialGroup()
						.addGap(49, 49, 49)
						.addComponent(jLabel4)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(jLabel5))
					.addComponent(jLabel3)
					.addGroup(layout.createSequentialGroup()
						.addGap(10, 10, 10)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
							.addComponent(jLabel7)
							.addComponent(jLabel8)
							.addComponent(jLabel9)
							.addComponent(jLabel10)
							.addComponent(jLabel11)
							.addComponent(jLabel13)
							.addComponent(jLabel6)
							.addComponent(jLabel12))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(jField1Right, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(jField1Left, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(jField1B, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(jField1A, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(jField1Start, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(jField1Select, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(jField1Up, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(jField1Down, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(jField2Select, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(jField2B, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(jField2Start, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(jField2A, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(jField2Right, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(jField2Up, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(jField2Down, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addComponent(jField2Left, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)))
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addGroup(layout.createSequentialGroup()
							.addComponent(jButtonOK, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(jButtonCancel))
						.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(jLabelCtrl1)
								.addComponent(jLabelCtrl2))
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(jButton2)
								.addComponent(jButton1))))))
		);

		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] {jField1A, jField1B, jField1Down, jField1Left, jField1Right, jField1Select, jField1Start, jField1Up, jField2A, jField2B, jField2Down, jField2Left, jField2Right, jField2Select, jField2Start, jField2Up});

		layout.setVerticalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addComponent(jLabel3)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jLabel16, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
				.addGap(11, 11, 11)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(jLabel4)
					.addComponent(jLabel5))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(jField2Up, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(jLabel6)
									.addComponent(jField1Up, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(jField1Down, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabel7))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(jField1Left, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabel8))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(jField1Right, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabel9)
									.addComponent(jField2Right, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(jField1A, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabel10)
									.addComponent(jField2A, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
							.addGroup(layout.createSequentialGroup()
								.addGap(31, 31, 31)
								.addComponent(jField2Down, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(jField2Left, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jField1B, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(jLabel11)
							.addComponent(jField2B, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jField1Select, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(jLabel12)
							.addComponent(jField2Select, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jField1Start, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(jLabel13)
							.addComponent(jField2Start, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jLabel1)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(jLabelCtrl1)
					.addComponent(jButton2))
				.addGap(12, 12, 12)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(jLabelCtrl2)
					.addComponent(jButton1))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(jButtonOK)
					.addComponent(jButtonCancel))
				.addGap(0, 8, Short.MAX_VALUE))
		);

		layout.linkSize(SwingConstants.VERTICAL, new Component[] {jField1A, jField1B, jField1Down, jField1Left, jField1Right, jField1Select, jField1Start, jField1Up, jField2A, jField2B, jField2Down, jField2Left, jField2Right, jField2Select, jField2Start, jField2Up});

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
