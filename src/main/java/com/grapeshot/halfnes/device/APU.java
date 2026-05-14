/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.grapeshot.halfnes.device;

import java.util.ArrayList;
import java.util.List;

import com.grapeshot.halfnes.CPU;
import com.grapeshot.halfnes.CPUAddrSpace;
import com.grapeshot.halfnes.NES;
import com.grapeshot.halfnes.PrefsSingleton;
import com.grapeshot.halfnes.utils;
import com.grapeshot.halfnes.audio.AudioOutInterface;
import com.grapeshot.halfnes.audio.ExpansionSoundChip;
import com.grapeshot.halfnes.audio.NoiseTimer;
import com.grapeshot.halfnes.audio.SquareTimer;
import com.grapeshot.halfnes.audio.SwingAudioImpl;
import com.grapeshot.halfnes.audio.Timer;
import com.grapeshot.halfnes.audio.TriangleTimer;
import com.grapeshot.halfnes.mappers.Mapper;
import com.grapeshot.halfnes.ui.OscilloScope;

public class APU extends NESDev {
	private static final int REG_SQ1_VOL = 0x00; //W: Pulse 1 Duty cycle and volume, R: Open bus
	private static final int REG_SQ1_SWEEP = 0x01; //W: Pulse 1 Sweep control register, R: Open bus
	private static final int REG_SQ1_LO = 0x02; //W: Pulse 1 Low byte of period, R: Open bus
	private static final int REG_SQ1_HI = 0x03; //W: Pulse 1 High byte of period and length counter value, R: Open bus

	private static final int REG_SQ2_VOL = 0x04; //W: Pulse 2 Duty cycle and volume , R: Open bus
	private static final int REG_SQ2_SWEEP = 0x05; //W: Pulse 2 Sweep control register, R: Open bus
	private static final int REG_SQ2_LO = 0x06; //W: Pulse 2 Low byte of period, R: Open bus
	private static final int REG_SQ2_HI = 0x07; //W: Pulse 2 High byte of period and length counter value, R: Open bus

	private static final int REG_TRI_LINEAR = 0x08; //W: Triangle Linear counter , R: Open bus
	private static final int REG_TRI_UNUSED = 0x09; //W: Triangle Unused, R: Open bus
	private static final int REG_TRI_LO = 0x0a; //W: Triangle Low byte of period, R: Open bus
	private static final int REG_TRI_HI = 0x0b; //W: Triangle High byte of period and length counter value, R: Open bus

	private static final int REG_NOISE_VOL = 0x0c; //W: Noise Volume, R: Open bus
	private static final int REG_NOISE_UNUSED = 0x0d; //W: Noise Unused, R: Open bus
	private static final int REG_NOISE_LO = 0x0e; //W: Noise Period and waveform shape, R: Open bus
	private static final int REG_NOISE_HI = 0x0f; //W: Noise Length counter value , R: Open bus

	private static final int REG_DMC_FREQ = 0x10; //W: DMC IRQ flag, loop flag and frequency, R: Open bus
	private static final int REG_DMC_RAW = 0x11; //W: DMC 7-bit DAC, R: Open bus
	private static final int REG_DMC_START = 0x12; //W: DMC Start address = $C000 + $40*$xx, R: Open bus
	private static final int REG_DMC_LEN = 0x13; //W: DMC Sample length = $10*$xx + 1 bytes (128*$xx + 8 samples), R: Open bus

	private static final int REG_OAM_DMA = 0x14; //W: OAM DMA: Copy 256 bytes from $xx00-$xxFF into OAM via OAMDATA ($2004), R: Open bus

	private static final int REG_SND_CHN = 0x15; //W: Sound channels enable, R: Sound channel and IRQ status

	private static final int REG_JOY1 = 0x16; //W: Joystick strobe, R: Joystick 1 data
	private static final int REG_JOY2 = 0x17; //W: Frame counter control, R: Joystick 2 data

	private final static int[] TRI_VOL_LOOKUP;
	private final static int[] SQU_VOL_LOOKUP;
	private final static int[][] DUTY_LOOKUP = {
		{0, 1, 0, 0, 0, 0, 0, 0},
		{0, 1, 1, 0, 0, 0, 0, 0},
		{0, 1, 1, 1, 1, 0, 0, 0},
		{1, 0, 0, 1, 1, 1, 1, 1}
	};

	private final static int[] DMC_RATES_NTSC =  new int[] {428, 380, 340, 320, 286, 254, 226, 214, 190, 160, 142, 128, 106, 84, 72, 54};
	private final static int[] DMC_RATES_DENDY = new int[] {428, 380, 340, 320, 286, 254, 226, 214, 190, 160, 142, 128, 106, 84, 72, 54};
	private final static int[] DMC_RATES_PAL =   new int[] {398, 354, 316, 298, 276, 236, 210, 198, 176, 148, 132, 118,  98, 78, 66, 50};


	private int sampleRate;
	private double cyclesPerSample;
	public final NES nes;
	private CPU cpu;
	private CPUAddrSpace cpuram;


	public int sprdma_count;
	private int apuCycle = 0;
	private int remainder = 0;
	private int[] noiseperiod;
	// different for PAL
	private long accum = 0;
	private final List<ExpansionSoundChip> expansionSoundChips = new ArrayList<>();
	private boolean soundFiltering;

	private int framectrreload;
	private int framectrdiv = 7456;
	private int dckiller = -6392; //removes icky power on thump
	private int lpaccum = 0;


	private int cyclesPerFrame;
	private AudioOutInterface audioOutput;


	private static class LengthCounter {
		private final static int[] VALUES = {
			10, 254, 20, 2, 40, 4, 80, 6, 160, 8, 60, 10, 14, 12, 26, 14,
			12, 16, 24, 18, 48, 20, 96, 22, 192, 24, 72, 26, 16, 28, 32, 30
		};

		private boolean enabled = false;
		private boolean halted = true;
		private int value = 0;
		private final Runnable runnable;


		public LengthCounter(Runnable runnable)
		{
			this.runnable = runnable;
		}

		public void setEnabled(boolean enabled)
		{
			this.enabled = enabled;
			if (!enabled) {
				halted = true;
				value = 0;
			}
		}


		public void setHalted(boolean halted)
		{
			//if (enabled) {
				this.halted = halted;
			//}
		}

		public boolean isHalted()
		{
			return halted;
		}

		public void selectValue(int index)
		{
			if (enabled && index >= 0 && index < VALUES.length) {
				value = VALUES[index];
			}
		}

		public int getValue()
		{
			return value;
		}

		public void countDown()
		{
			if (halted || value == 0) {
				return;
			}

			if (--value == 0
				&& runnable != null) {
				runnable.run();
			}
		}
	}

	private static class Status {
		private boolean enabled = false;
		private boolean occurred = false;

		public void setEnabled(boolean enabled)
		{
			this.enabled = enabled;
		}

		public boolean isEnabled()
		{
			return enabled;
		}

		public void setOccurred(boolean occurred)
		{
			this.occurred = occurred;
		}

		public boolean isOccurred()
		{
			return occurred;
		}
	}

	private static enum SequMode {
		SEQU_4_STEP, SEQU_5_STEP;
	}

	private static class FrameCounter {
		public SequMode sequMode;
		public int value = 0;
		public final Status interruptStatus = new Status();
	}

	private final FrameCounter frameCounter = new FrameCounter();

	private static class DMCSample {
		public int byteLength = 1;
		public int byteLeft = 0;
		public int startAddr = 0xc000;
		public int addr = 0xc000;

		public boolean silence = true;

		public void restart()
		{
			addr = startAddr;
			byteLeft = byteLength;
			silence = false;
		}
	}

	private static class DMC {
		public int[] rates;

		public int rate = 0x36;
		public boolean loop = false;
		public int outputLevel = 0;

		public final DMCSample sample = new DMCSample();

		public int pos = 0;
		public int shiftRegister = 0;
		public int buffer = 0;

		public int bitsLeft = Byte.SIZE;



		public boolean bufferEmpty = true;

		public final Status interruptStatus = new Status();

	}

	private final DMC dmc = new DMC();


	private static class LinearCounter {
		public boolean controlFlag = true;
		public int value = 0;
		public int load = 0;
	}

	private static class Envelope {
		public boolean constVolumeFlag = true;
		public int constVolume = 15;
		public int counter = 0;
		public int pos = 0;
		public boolean startFlag = false;

		public int getVolume()
		{
			return constVolumeFlag? constVolume : counter;
		}
	}

	private static class Sweep {
		public boolean enable = false;
		public boolean negate = false;
		public boolean silence = false;
		public boolean reload = false;
		public int period = 15;
		public int shift = 0;
		public int pos = 0;
	}

	private static class Channel {
		public LengthCounter lengthCounter;
		public final Timer timer;
		public final Envelope envelope = new Envelope();

		public int volume;


		public Channel(Timer timer)
		{
			this.timer = timer;
			lengthCounter = new LengthCounter(this::refreshVolume);
		}


		public void refreshEnvelope()
		{
			if (envelope.startFlag) {
				envelope.startFlag = false;
				envelope.pos = envelope.constVolume + 1;
				envelope.counter = 15;
			} else {
				envelope.pos--;
			}

			if (envelope.pos <= 0) {
				envelope.pos = envelope.constVolume + 1;
				if (envelope.counter > 0) {
					--envelope.counter;
				} else if (lengthCounter.isHalted() && envelope.counter <= 0) {
					envelope.counter = 15;
				}
			}
		}

		public void refreshVolume()
		{
			volume = (lengthCounter.getValue() <= 0)? 0 : envelope.getVolume();
		}

//		public void refreshLengthCounter()
//		{
//			if (lengthCounter.halted
//				|| lengthCounter.value <= 0) {
//				return;
//			}
//
//			if (--lengthCounter.value == 0) {
//				refreshVolume();
//			}
//		}
		public void refreshLengthCounter()
		{
			lengthCounter.countDown();
		}
	}

	private static class Pulse extends Channel {
		private final int index;
		public final Sweep sweep = new Sweep();

		public Pulse(int index)
		{
			super(new SquareTimer(8, 2));
			this.index = index;
		}

		@Override
		public void refreshVolume()
		{
			volume = (lengthCounter.getValue() <= 0 || sweep.silence)?
				0 : envelope.getVolume();
		}

		private void refreshSweep()
		{
			sweep.silence = false;
			if (sweep.reload) {
				sweep.reload = false;
				sweep.pos = sweep.period;
			}
			sweep.pos++;

			final int rawPeriod = (timer.getPeriod() >> 1);
			int shiftedPeriod = (rawPeriod >> sweep.shift);
			if (sweep.negate) {
				//invert bits of period
				//add 1 on second channel only
				shiftedPeriod = -shiftedPeriod + index;
			}

			shiftedPeriod += rawPeriod;
			if ((rawPeriod < 8) || shiftedPeriod > 0x7ff) {
				// silence channel
				sweep.silence = true;
			} else if (sweep.enable
					&& sweep.shift != 0
					&& lengthCounter.getValue() > 0
					&& sweep.pos > sweep.period) {
				sweep.pos = 0;
				timer.setPeriod(shiftedPeriod << 1);
			}
		}
	}

	private static class Triangle extends Channel {
		public final LinearCounter linearCounter = new LinearCounter();

		public Triangle()
		{
			super(new TriangleTimer());
		}

		public void refreshLinearCounter()
		{
			if (linearCounter.controlFlag) {
				linearCounter.value = linearCounter.load;
			} else if (linearCounter.value > 0) {
				linearCounter.value--;
			}

			if (!lengthCounter.isHalted()) {
				linearCounter.controlFlag = false;
			}
		}

	}

	private static class Noise extends Channel {

		public Noise()
		{
			super(new NoiseTimer());
		}
	}

	private final Pulse[] pulses = new Pulse[] {new Pulse(0), new Pulse(1)};
	private final Triangle triangle = new Triangle();
	private final Noise noise = new Noise();

	private final Channel[] channels = new Channel[] {pulses[0], pulses[1], triangle, noise};


	static {
		SQU_VOL_LOOKUP = new int[31];
		for (int i = 0; i < SQU_VOL_LOOKUP.length; i++) {
			SQU_VOL_LOOKUP[i] = (int) ((95.52 / (8128.0 / i + 100)) * 49151);
		}

		TRI_VOL_LOOKUP = new int[203];
		for (int i = 0; i < TRI_VOL_LOOKUP.length; i++) {
			TRI_VOL_LOOKUP[i] = (int) ((163.67 / (24329.0 / i + 100)) * 49151);
		}
	}

	public APU(final NES nes, final CPU cpu, final CPUAddrSpace cpuram)
	{
		this.sampleRate = 1; //just in case we can't init audio
		//then init the audio stream
		this.nes = nes;
		this.cpu = cpu;
		this.cpuram = cpuram;
		setParameters();
	}

	public final synchronized void setParameters()
	{
		Mapper.TVType tvtype = cpuram.mapper.getTVType();
		soundFiltering = PrefsSingleton.getInstance().getBoolean("soundFiltering", true);
		sampleRate = PrefsSingleton.getInstance().getInt("sampleRate", 44100);

		if (audioOutput != null) {
			audioOutput.destroy();
		}

		try {
			audioOutput = new SwingAudioImpl(sampleRate, tvtype);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (PrefsSingleton.getInstance().getBoolean("showScope", false)) {
			audioOutput = new OscilloScope(audioOutput);
		}

		//pick the appropriate pitches and lengths for NTSC or PAL
		switch (tvtype) {
		case NTSC:
		default:
			this.dmc.rates = DMC_RATES_NTSC;
			this.noiseperiod = new int[] {4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068};
			this.framectrreload = 7456;
			cyclesPerSample = 1789773.0 / sampleRate;
			cyclesPerFrame = 29781;
			break;

		case DENDY:
			this.dmc.rates = DMC_RATES_DENDY;
			this.noiseperiod = new int[] {4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068};
			this.framectrreload = 7456;
			cyclesPerSample = 1773448.0 / sampleRate;
			cyclesPerFrame = 35469;
			break;
		case PAL:
			cyclesPerSample = 1662607.0 / sampleRate;
			this.dmc.rates = DMC_RATES_PAL;
			this.noiseperiod = new int[] {4, 8, 14, 30, 60, 88, 118, 148, 188, 236, 354, 472, 708, 944, 1890, 3778};
			this.framectrreload = 8312;
			cyclesPerFrame = 33252;
			break;
		}
	}

	public boolean bufferHasLessThan(int samples)
	{
		return (audioOutput != null)?
				audioOutput.bufferHasLessThan(samples)
				: false;
	}

	@Override
	public final int read(final int addr)
	{
		updateto(cpu.clocks);

		switch (addr & ADDR_MASK()) {
		case REG_SND_CHN:
			//returns channel status
			//for future ref: NEED to put those ternary operators in parentheses!
			//otherwise order of operations does the wrong thing.
			int data =
				((pulses[0].lengthCounter.getValue() > 0)? utils.BIT0 : 0)
				| ((pulses[1].lengthCounter.getValue() > 0)? utils.BIT1 : 0)
				| ((triangle.lengthCounter.getValue() > 0)? utils.BIT2 : 0)
				| ((noise.lengthCounter.getValue() > 0)? utils.BIT3 : 0)
				| ((dmc.sample.byteLeft > 0)? utils.BIT4 : 0)
				| (frameCounter.interruptStatus.isOccurred()? utils.BIT6 : 0)
				| (dmc.interruptStatus.isOccurred()? utils.BIT7 : 0);


			if (frameCounter.interruptStatus.isOccurred()) {
				//System.err.println("Frame interrupt ack at " + cpu.cycles);
				frameCounter.interruptStatus.setOccurred(false);
				cpu.interrupt--;
			}

			return data;

		case REG_JOY1:
			nes.getcontroller1().strobe();
			return nes.getcontroller1().getByte() | 0x40;

		case REG_JOY2:
			nes.getcontroller2().strobe();
			return nes.getcontroller2().getByte() | 0x40;

		default:
			return 0x40; //open bus
		}
	}

	public void addExpansionSoundChip(ExpansionSoundChip chip)
	{
		expansionSoundChips.add(chip);
	}

	public void destroy()
	{
		if (audioOutput != null) {
			audioOutput.destroy();
		}
	}

	public void pause()
	{
		if (audioOutput != null) {
			audioOutput.pause();
		}
	}

	public void resume()
	{
		if (audioOutput != null) {
			audioOutput.resume();
		}
	}

	@Override
	public final void write(final int reg, final int data)
	{
		//This is how values written to any of the APU's memory
		//mapped registers change the state of the system.
		updateto(cpu.clocks - 1);

		switch (reg & ADDR_MASK()) {
		case REG_SQ1_VOL:
			pulses[0].lengthCounter.setHalted((data & utils.BIT5) != 0);
			pulses[0].timer.setDuty(DUTY_LOOKUP[data >> 6]);
			pulses[0].envelope.constVolumeFlag = (data & utils.BIT4) != 0;
			pulses[0].envelope.constVolume = data & 0x0f;
			break;

		case REG_SQ1_SWEEP:
			pulses[0].sweep.enable = (data & utils.BIT7) != 0;
			pulses[0].sweep.period = (data >> 4) & 0x07;
			pulses[0].sweep.negate = (data & (utils.BIT3)) != 0;
			pulses[0].sweep.shift = data & 0x07;
			pulses[0].sweep.reload = true;
			break;

		case REG_SQ1_LO:
			pulses[0].timer.setPeriod((pulses[0].timer.getPeriod() & 0xfe00) + (data << 1));
			break;

		case REG_SQ1_HI:
			//if (pulses[0].isEnabled()) {
				pulses[0].lengthCounter.selectValue((data >> 3) & 0x1f);
			//}
			pulses[0].timer.setPeriod((pulses[0].timer.getPeriod() & 0x1ff) + ((data & 7) << 9));
			pulses[0].timer.reset();
			pulses[0].envelope.startFlag = true;
			break;

		case REG_SQ2_VOL: //length counter 2 halt
			pulses[1].lengthCounter.setHalted((data & utils.BIT5) != 0);
			// pulse 2 duty cycle
			pulses[1].timer.setDuty(DUTY_LOOKUP[data >> 6]);
			// and envelope
			pulses[1].envelope.constVolumeFlag = (data & (utils.BIT4)) != 0;
			pulses[1].envelope.constVolume = data & 0x0f;
			break;

		case REG_SQ2_SWEEP: //pulse 2 sweep setup
			pulses[1].sweep.enable = (data & utils.BIT7) != 0;
			pulses[1].sweep.period = (data >> 4) & 7;
			pulses[1].sweep.negate = (data & utils.BIT3) != 0;
			pulses[1].sweep.shift = data & 7;
			pulses[1].sweep.reload = true;
			break;

		case REG_SQ2_LO:
			// pulse 2 timer low bit
			pulses[1].timer.setPeriod((pulses[1].timer.getPeriod() & 0xfe00) + (data << 1));
			break;

		case REG_SQ2_HI:
			//if (pulses[1].isEnabled()) {
				pulses[1].lengthCounter.selectValue((data >> 3) & 0x1f);
			//}
			pulses[1].timer.setPeriod((pulses[1].timer.getPeriod() & 0x1ff) + ((data & 7) << 9));
			// sequencer restarted
			pulses[1].timer.reset();
			//envelope also restarted
			pulses[1].envelope.startFlag = true;
			break;

		case REG_TRI_LINEAR:
			triangle.lengthCounter.setHalted((data & utils.BIT7) != 0);
			triangle.linearCounter.load = data & 0x7f;
			break;

		case REG_TRI_UNUSED:
			break;

		case REG_TRI_LO:
			// triangle low bits of timer
			triangle.timer.setPeriod(((triangle.timer.getPeriod() * 1) & 0xff00) + data);
			break;

		case REG_TRI_HI:
			// triangle length counter load
			// and high bits of timer
			//if (triangle.isEnabled()) {
				triangle.lengthCounter.selectValue((data >> 3) & 0x1f);
			//}
			triangle.timer.setPeriod(((triangle.timer.getPeriod() * 1) & 0xff) + ((data & 7) << 8));
			triangle.linearCounter.controlFlag = true;
			break;

		case REG_NOISE_VOL:
			//noise halt and envelope
			noise.lengthCounter.setHalted((data & utils.BIT5) != 0);
			noise.envelope.constVolumeFlag = (data & utils.BIT4) != 0;
			noise.envelope.constVolume = data & 0x0f;
			break;

		case REG_NOISE_UNUSED:
			break;

		case REG_NOISE_LO:
			noise.timer.setDuty(((data & utils.BIT7) != 0)? 6 : 1);
			noise.timer.setPeriod(noiseperiod[data & 0x0f]);
			break;

		case REG_NOISE_HI:
			//noise length counter load, envelope restart
			//if (noise.isEnabled()) {
				noise.lengthCounter.selectValue((data >> 3) & 0x1f);
			//}
			noise.envelope.startFlag = true;
			break;

		case REG_DMC_FREQ:
			dmc.rate = dmc.rates[data & 0x0f];
			dmc.loop = (data & utils.BIT6) != 0;
			dmc.interruptStatus.setEnabled((data & utils.BIT7) != 0);
			if (!dmc.interruptStatus.isEnabled()
				&& dmc.interruptStatus.isOccurred()) {
				cpu.interrupt--;
				dmc.interruptStatus.setOccurred(false);
			}
			break;

		case REG_DMC_RAW:
			dmc.outputLevel = data & 0x7f;
			break;

		case REG_DMC_START:
			dmc.sample.startAddr = (data << 6) + 0xc000;
			break;

		case REG_DMC_LEN:
			dmc.sample.byteLength = (data << 4) + 1;
			break;

		case REG_OAM_DMA:
			//sprite dma
			for (int i = 0; i < 256; i++) {
				cpuram.write(0x2004, cpuram.read((data << 8) + i));
			}
			//account for time stolen from cpu
			sprdma_count = 2;
			break;

		case REG_SND_CHN:
			//status register
			// counter enable(silence channel when bit is off)
			for (int i = 0; i < channels.length; i++) {
				//channels[i].lengthCounter.enabled = (data & (1 << i)) != 0;
				channels[i].lengthCounter.setEnabled((data & (1 << i)) != 0);

				//THIS was the channels not cutting off bug! If you toggle a channel's
				//status on and off very quickly then the length counter should
				//IMMEDIATELY be forced to zero.
//				if (!channels[i].isEnabled()) {
//					channels[i].lengthCounter.halted = true;
//					channels[i].lengthCounter.value = 0;
//				}
			}

			if ((data & utils.BIT4) != 0) {
				if (dmc.sample.byteLeft == 0) {
					dmc.sample.restart();
				}
			} else {
				dmc.sample.byteLeft = 0;
				dmc.sample.silence = true;
			}

			if (dmc.interruptStatus.isOccurred()) {
				cpu.interrupt--;
				dmc.interruptStatus.setOccurred(false);
			}
			break;

		case REG_JOY1:
			// latch controller 1 + 2
			nes.getcontroller1().output((data & utils.BIT0) != 0);
			nes.getcontroller2().output((data & utils.BIT0) != 0);
			break;

		case REG_JOY2:
			//ctrmode = ((data & utils.BIT7) != 0)? 5 : 4;
			frameCounter.sequMode = ((data & utils.BIT7) != 0)?
				SequMode.SEQU_5_STEP
				: SequMode.SEQU_4_STEP;

			//apuintflag = (data & utils.BIT6) != 0;
			frameCounter.interruptStatus.setEnabled((data & utils.BIT6) == 0);
			//set is no interrupt, clear is an interrupt

			frameCounter.value = 0;

			framectrdiv = framectrreload + 8; //Why +8?
			if (!frameCounter.interruptStatus.isEnabled()
				&& frameCounter.interruptStatus.isOccurred()) {
				frameCounter.interruptStatus.setOccurred(false);
				cpu.interrupt--;
			}

			if (frameCounter.sequMode == SequMode.SEQU_4_STEP) {
				//everything frame counter runs is clocked no matter what
				refreshEnvelopes();
				triangle.refreshLinearCounter();
				//refreshLengthCounters();
				refreshSweeps();
			} else {
				refreshLengthCounters();
			}
			break;

		default:
			break;
		}
	}

	public final void updateto(final int cpucycle)
	{
		//still have to run this even if sound is disabled, some games rely on DMC IRQ etc.
		if (soundFiltering) {
			//linear sampling code
			//should really be a FIR filter + decimator instead
			//but I don't have the DSP experience to design something like that
			//that would be fast enough to work / not require calculating every sample
			//this works well enough at eliminating aliasing anyway.
			while (apuCycle < cpucycle) {
				++remainder;
				clockDMC();
				if (--framectrdiv <= 0) {
					framectrdiv = framectrreload;
					clockFrameCounter();
				}
				pulses[0].timer.clock();
				pulses[1].timer.clock();
				if (triangle.lengthCounter.getValue() > 0
					&& triangle.linearCounter.value > 0) {
					triangle.timer.clock();
				}
				noise.timer.clock();
				if (!expansionSoundChips.isEmpty()) {
					for (ExpansionSoundChip c : expansionSoundChips) {
						c.clock(1);
					}
				}
				accum += getOutputLevel();

				if ((apuCycle % cyclesPerSample) < 1) {
					//not quite right - there's a non-integer # cycles per sample.
					audioOutput.outputSample(lowpass_filter(highpass_filter((int) (accum / remainder))));
					remainder = 0;
					accum = 0;
				}
				++apuCycle;
			}
		} else {
			//point sampling code
			while (apuCycle < cpucycle) {
				++remainder;
				clockDMC();
				if (--framectrdiv <= 0) {
					framectrdiv = framectrreload;
					clockFrameCounter();
				}
				if ((apuCycle % cyclesPerSample) < 1) {
					//not quite right - there's a non-integer # cycles per sample.
					pulses[0].timer.clock(remainder);
					pulses[1].timer.clock(remainder);
					if (triangle.lengthCounter.getValue() > 0
						&& triangle.linearCounter.value > 0) {
						triangle.timer.clock(remainder);
					}
					noise.timer.clock(remainder);
					int mixvol = getOutputLevel();
					if (!expansionSoundChips.isEmpty()) {
						for (ExpansionSoundChip c : expansionSoundChips) {
							c.clock(remainder);
						}
					}
					remainder = 0;
					audioOutput.outputSample(lowpass_filter(highpass_filter(mixvol)));
				}
				++apuCycle;
			}
		}
	}

	private int getOutputLevel()
	{
		int vol = SQU_VOL_LOOKUP[
				pulses[0].volume * pulses[0].timer.getVal()
				+ pulses[1].volume * pulses[1].timer.getVal()
			]
			+ TRI_VOL_LOOKUP[
				3 * triangle.timer.getVal()
				+ 2 * noise.volume * noise.timer.getVal()
				+ dmc.outputLevel
			];

		if (!expansionSoundChips.isEmpty()) {
			vol *= 0.8;
			for (ExpansionSoundChip c : expansionSoundChips) {
				vol += c.getval();
			}
		}

		return vol; //as usual, lack of unsigned types causes unending pain.
	}

	private int highpass_filter(int sample)
	{
		//for killing the dc in the signal
		sample -= dckiller;
		dckiller += sample >> 8;//the actual high pass part
		dckiller += (sample > 0 ? 1 : -1);//guarantees the signal decays to exactly zero
		return sample;
	}

	private int lowpass_filter(int sample)
	{
		return lpaccum += 0.5 * (sample - lpaccum); //y = y + a * (x - y)
	}

	public final void finishframe()
	{
		updateto(cyclesPerFrame);
		apuCycle = 0;
		audioOutput.flushFrame(nes.isFrameLimiterOn());
	}

	private void clockFrameCounter()
	{
		//System.err.println("frame ctr clock " + framectr + ' ' + cpu.cycles);
		//should be ~4x a frame, 240 Hz
		//but the problem is this isn't exactly related to the video signal,
		//it's a completely separate timer, so the phase can shift in relation to the
		//video signal. also in the current implementation APU interrupts can only be fired when
		//an APU register is written/read from, or @ end of frame. So both of those need work

		if (frameCounter.sequMode == SequMode.SEQU_4_STEP
			|| (frameCounter.sequMode == SequMode.SEQU_5_STEP && frameCounter.value != 3)) {
			refreshEnvelopes();
			triangle.refreshLinearCounter();
		}

		if ((frameCounter.sequMode == SequMode.SEQU_4_STEP && (frameCounter.value == 1 || frameCounter.value == 3))
				|| (frameCounter.sequMode == SequMode.SEQU_5_STEP && (frameCounter.value == 1 || frameCounter.value == 4))) {
			refreshLengthCounters();
			refreshSweeps();
		}

		if ((frameCounter.sequMode == SequMode.SEQU_4_STEP)
			&& (frameCounter.value == 3)
			&& frameCounter.interruptStatus.isEnabled()
			&& !frameCounter.interruptStatus.isOccurred()) {

			frameCounter.interruptStatus.setOccurred(true);
			cpu.interrupt++;
		}

		frameCounter.value++;
		frameCounter.value %= (frameCounter.sequMode == SequMode.SEQU_4_STEP)? 4 : 5;

		refreshVolumes();
	}






	private void clockDMC()
	{
		if (dmc.bufferEmpty && dmc.sample.byteLeft > 0) {
			dmcfillbuffer();
		}

		dmc.pos = (dmc.pos + 1) % dmc.rate;
		if (dmc.pos != 0) {
			return;
		}

		if (dmc.bitsLeft <= 0) {
			dmc.bitsLeft = Byte.SIZE;
			if (dmc.bufferEmpty) {
				dmc.sample.silence = true;
			} else {
				dmc.sample.silence = false;
				dmc.shiftRegister = dmc.buffer;
				dmc.bufferEmpty = true;
			}
		}

		if (!dmc.sample.silence) {
			dmc.outputLevel += ((dmc.shiftRegister & utils.BIT0) != 0)? 2 : -2;

			//DMC output register doesn't wrap around
			if (dmc.outputLevel > 0x7f) {
				dmc.outputLevel = 0x7f;
			}
			if (dmc.outputLevel < 0) {
				dmc.outputLevel = 0;
			}

			dmc.shiftRegister >>= 1;
			--dmc.bitsLeft;
		}
	}

	private void dmcfillbuffer()
	{
		if (dmc.sample.byteLeft <= 0) {
			dmc.sample.silence = true;
			return;
		}

		dmc.buffer = cpuram.read(dmc.sample.addr++);
		dmc.bufferEmpty = false;
		cpu.stealcycles(4);

		//DPCM Does steal cpu cycles - this should actually vary between 1-4
		//can't do this properly without a cycle accurate cpu/ppu
		if (dmc.sample.addr > 0xffff) {
			dmc.sample.addr = 0x8000;
		}
		//dmc.samplesLeft--;

		if (--dmc.sample.byteLeft != 0) {
			return;
		}

		if (dmc.loop) {
			dmc.sample.restart();
		} else if (dmc.interruptStatus.isEnabled()
			&& !dmc.interruptStatus.isOccurred()) {
			//this is supposed to fire after we've just READ the
			//last byte, not when coming back AFTER reading the last byte
			//and finding that there are no more bytes left to read.
			//that meant all dmc timing was too long.
			cpu.interrupt++;
			dmc.interruptStatus.setOccurred(true);
			//System.err.println("dmc irq fire");
		}
	}


	private void refreshLengthCounters()
	{
		for (Channel channel : channels) {
			channel.refreshLengthCounter();
		}
	}


	private void refreshEnvelopes()
	{
		for (Channel channel : channels) {
			channel.refreshEnvelope();
		}
	}

	private void refreshSweeps()
	{
		for (Pulse pulse :pulses) {
			pulse.refreshSweep();
		}
	}

	private void refreshVolumes()
	{
		for (Channel channel : channels) {
			channel.refreshVolume();
		}
	}

	@Override
	public int ADDR_MASK()
	{
		//return 0x0017; //bit4,2-0
		return 0x001f;
	}
}
