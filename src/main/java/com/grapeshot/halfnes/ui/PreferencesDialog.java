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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;

import com.grapeshot.halfnes.PrefsSingleton;

/**
 *
 * @author Andrew
 */
public class PreferencesDialog extends JDialog {
	private JButton jButtonCancel;
	private JButton jButtonOK;
	private JCheckBox jCheckBoxNTSC;
	private JCheckBox jCheckBoxNTView;
	private JCheckBox jCheckBoxShowScope;
	private JCheckBox jCheckBoxSleep;
	private JCheckBox jCheckBoxSmoothVideo;
	private JCheckBox jCheckMaintainAspect;
	private JCheckBox jCheckSoundEnable;
	private JCheckBox jCheckSoundFiltering;
	private JLabel jLabel1;
	private JLabel jLabel14;
	private JLabel jLabel15;
	private JLabel jLabel17;
	private JLabel jLabel18;
	private JLabel jLabel19;
	private JLabel jLabel2;
	private JLabel jLabel20;
	private JLabel jLabel3;
	private JComboBox<String> jRegionBox;
	private JComboBox<String> jSampleRateBox;
	private JSpinner jSpinnerScale;
	private JSlider volumeSlider;

	private boolean okClicked = false;


	public PreferencesDialog(Frame parent)
	{
		super(parent, true);
		setTitle("HalfNES Preferences");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setName("OptionsDialog");
		setResizable(false);
		if (parent != null) {
			setLocation(parent.getLocation());
		}

		initComponents();
		loadPrefs();
	}

	private void initComponents()
	{
		jLabel1 = new JLabel("Screen scaling: scale up ");
		jCheckSoundEnable = new JCheckBox("Enable Sound");
		jButtonCancel = new JButton("Cancel");
		jButtonOK = new JButton("OK");
		jSampleRateBox = new JComboBox<String>();
		jLabel2 = new JLabel("Sample Rate:");
		jCheckMaintainAspect = new JCheckBox();
		jLabel14 = new JLabel();
		jCheckSoundFiltering = new JCheckBox();
		jLabel15 = new JLabel();
		volumeSlider = new JSlider();
		jLabel17 = new JLabel();
		jLabel18 = new JLabel();
		jCheckBoxNTSC = new JCheckBox();
		jLabel19 = new JLabel();
		jSpinnerScale = new JSpinner();
		jLabel20 = new JLabel();
		jCheckBoxSmoothVideo = new JCheckBox();
		jCheckBoxSleep = new JCheckBox();
		jCheckBoxShowScope = new JCheckBox();
		jCheckBoxNTView = new JCheckBox();
		jLabel3 = new JLabel();
		jRegionBox = new JComboBox<String>();


		jButtonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});

		jButtonOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				storePrefs();
				okClicked = true;
				dispose();
			}
		});

		jSampleRateBox.setModel(new DefaultComboBoxModel<String>(new String[] { "16000", "24000", "44100", "48000","96000" }));


		jCheckMaintainAspect.setText("Maintain Aspect Ratio");

		jLabel14.setText("Sound:");

		jCheckSoundFiltering.setText("Enable Filtering");

		jLabel15.setText("Output Volume:");

		volumeSlider.setMajorTickSpacing(8192);
		volumeSlider.setMaximum(16384);
		volumeSlider.setMinorTickSpacing(4096);
		volumeSlider.setPaintTicks(true);
		volumeSlider.setValue(80);

		jLabel17.setText("0");

		jLabel18.setText("100");

		jCheckBoxNTSC.setText("Use NTSC TV Filter (Experimental)");

		jLabel19.setText("Video options:");

		jSpinnerScale.setModel(new SpinnerNumberModel(2, 1, 6, 1));

		jLabel20.setText("times");

		jCheckBoxSmoothVideo.setText("Use Smooth Scaling");

		jCheckBoxSleep.setText("Sleep Between Frames");

		jCheckBoxShowScope.setText("Show Audio Output");

		jCheckBoxNTView.setText("Show Nametable Viewer");

		jLabel3.setText("Console Region:");

		jRegionBox.setModel(new DefaultComboBoxModel<String>(new String[] { "Auto Detect", "NTSC", "PAL", "Dendy (Hybrid)" }));


		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addGap(0, 0, Short.MAX_VALUE)
						.addComponent(jButtonOK)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(jButtonCancel))
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addGroup(layout.createSequentialGroup()
										.addComponent(jLabel1)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jSpinnerScale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jLabel20, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE))
									.addComponent(jLabel19)
									.addComponent(jCheckBoxNTSC)
									.addComponent(jCheckMaintainAspect)
									.addComponent(jCheckBoxSmoothVideo)
									.addComponent(jCheckBoxSleep)
									.addComponent(jCheckBoxShowScope)
									.addComponent(jCheckBoxNTView))
								.addGap(18, 18, 18)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
									.addComponent(volumeSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabel14)
									.addComponent(jCheckSoundEnable)
									.addComponent(jCheckSoundFiltering)
									.addGroup(layout.createSequentialGroup()
										.addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 73, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jSampleRateBox, GroupLayout.PREFERRED_SIZE, 83, GroupLayout.PREFERRED_SIZE))
									.addComponent(jLabel15)
									.addGroup(layout.createSequentialGroup()
										.addComponent(jLabel17)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(jLabel18))))
							.addGroup(layout.createSequentialGroup()
								.addComponent(jLabel3)
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(jRegionBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addGap(0, 0, Short.MAX_VALUE)))
				.addContainerGap())
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(6, 6, 6)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
						.addComponent(jLabel14)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckSoundEnable)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckSoundFiltering)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jLabel2)
							.addComponent(jSampleRateBox, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
						.addGap(3, 3, 3)
						.addComponent(jLabel15)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(volumeSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(jLabel17)
							.addComponent(jLabel18)))
					.addGroup(layout.createSequentialGroup()
						.addComponent(jLabel19)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jLabel1)
							.addComponent(jSpinnerScale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(jLabel20))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxSmoothVideo)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckMaintainAspect)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxNTSC)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxSleep)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxShowScope)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxNTView)))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(jLabel3)
					.addComponent(jRegionBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(jButtonOK)
					.addComponent(jButtonCancel))
				.addContainerGap())
		);

		pack();
	}


	private void loadPrefs()
	{
		Preferences prefs = PrefsSingleton.getInstance();

		//set all of the text boxes
		//aand the sound enable checkbox.
		jCheckSoundEnable.setSelected(prefs.getBoolean("soundEnable", true));
		jCheckSoundFiltering.setSelected(prefs.getBoolean("soundFiltering", true));
		jCheckMaintainAspect.setSelected(prefs.getBoolean("maintainAspect", true));
		jCheckBoxNTSC.setSelected(prefs.getBoolean("TVEmulation", false));
		jCheckBoxNTView.setSelected(prefs.getBoolean("ntView", false));
		jCheckBoxShowScope.setSelected(prefs.getBoolean("showScope", false));
		jCheckBoxSleep.setSelected(prefs.getBoolean("Sleep", true));
		jButtonOK.setActionCommand("OK");
		jButtonCancel.setActionCommand("Cancel");
		jSpinnerScale.setValue(prefs.getInt("screenScaling", 2));
		jSampleRateBox.setSelectedItem(Integer.toString(prefs.getInt("sampleRate", 44100)));
		jCheckBoxSmoothVideo.setSelected(prefs.getBoolean("smoothScaling", false));
		volumeSlider.setValue(prefs.getInt("outputvol", 13107));
		jRegionBox.setSelectedIndex(prefs.getInt("region", 0));
		//0-> auto, 1-> NTSC, 2-> PAL, 3-> Dendy
	}

	private void storePrefs()
	{
		Preferences prefs = PrefsSingleton.getInstance();

		prefs.putBoolean("soundEnable", jCheckSoundEnable.isSelected());
		prefs.putBoolean("soundFiltering", jCheckSoundFiltering.isSelected());
		prefs.putBoolean("maintainAspect", jCheckMaintainAspect.isSelected());
		prefs.putBoolean("TVEmulation", jCheckBoxNTSC.isSelected());
		prefs.putBoolean("Sleep", jCheckBoxSleep.isSelected());
		prefs.putBoolean("smoothScaling", jCheckBoxSmoothVideo.isSelected());
		prefs.putBoolean("showScope", jCheckBoxShowScope.isSelected());
		prefs.putBoolean("ntView", jCheckBoxNTView.isSelected());
		prefs.putInt("screenScaling", (Integer) (jSpinnerScale.getModel().getValue()));
		prefs.putInt("sampleRate", Integer.parseInt(jSampleRateBox.getSelectedItem().toString()));
		prefs.putInt("outputvol", volumeSlider.getValue());
		prefs.putInt("region", jRegionBox.getSelectedIndex());

		try {
			prefs.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean okClicked()
	{
		return okClicked;
	}

	public static void main(String[] args)
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			;
		}
		PreferencesDialog dlg = new PreferencesDialog(null);
		dlg.setVisible(true);

	}
}
