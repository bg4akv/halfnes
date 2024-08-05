/**
 *
 * @author Andrew Hoffman
 */
package com.grapeshot.halfnes;

import java.io.File;

import javafx.application.Platform;

import com.grapeshot.halfnes.cheats.ActionReplay;
import com.grapeshot.halfnes.mappers.BadMapperException;
import com.grapeshot.halfnes.mappers.Mapper;
import com.grapeshot.halfnes.ppu.PPU;
import com.grapeshot.halfnes.ui.ControllerKeyListener;
import com.grapeshot.halfnes.ui.FrameLimiter;
import com.grapeshot.halfnes.ui.MainForm;
import com.grapeshot.halfnes.util.ThreadLoop;


public class NES {
	public static final String VERSION = "001";
	public static final String URL = "";

	private final MainForm mainForm;
	private Mapper mapper;
	private APU apu;
	private CPU cpu;
	private CPUAddrSpace cpuram;
	private PPU ppu;
	private ControllerKeyListener controller1, controller2;
	private boolean runEmulation = false;
	private boolean needSleep = true;
	//private boolean shutdown = false;
	public long frameStartTime, frameCount, frameDoneTime;
	private boolean frameLimiterOn = true;
	private String curRomPath;
	private final FrameLimiter limiter = new FrameLimiter(this, 16639267);
	// Pro Action Replay device
	private ActionReplay actionReplay;

	private final ThreadLoop loop;


	public NES(final MainForm mainForm)
	{
		this.mainForm = mainForm;
		loop = new ThreadLoop(() -> {
			if (runEmulation) {
				frameStartTime = System.nanoTime();
				actionReplay.applyPatches();
				runFrame();
				if (frameLimiterOn && needSleep) {
					limiter.sleep();
				}
				frameDoneTime = System.nanoTime() - frameStartTime;
			} else {
				limiter.sleepFixed();
				if (ppu != null && frameCount > 1) {
					//mainForm.drawImage();
				}
			}
		}, 0);
	}

	public CPUAddrSpace getCPURAM()
	{
		return cpuram;
	}

	public CPU getCPU()
	{
		return cpu;
	}

	public void run(final String romtoload)
	{
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 1);
		//set thread priority higher than the interface thread
		curRomPath = romtoload;
		//gui.loadROMs(romtoload);
		start();
	}

	public void start()
	{
		if (controller1 != null) {
			controller1.start();
		}

		if (controller2 != null) {
			controller2.start();
		}

		loop.start();
	}

	private synchronized void runFrame()
	{
		//run cpu, ppu for a whole frame
		ppu.runFrame();

		//do end of frame stuff
		needSleep = !apu.bufferHasLessThan(1000);
		//if the audio buffer is completely drained, don't sleep for this frame
		//this is to prevent the emulator from getting stuck sleeping too much
		//on a slow system or when the audio buffer runs dry.

		apu.finishframe();
		cpu.modcycles();

		//if (framecount == 13 * 60) {
		//	cpu.startLog();
		//	System.err.println("log on");
		//}
		//render the frame
		ppu.renderFrame(mainForm);
		if ((frameCount & 0x07ff) == 0) {
			//save sram every 30 seconds or so
			saveSRAM(true);
		}
		++frameCount;
		//System.err.println(framecount);
	}

	public void setControllers(ControllerKeyListener controller1, ControllerKeyListener controller2)
	{
		this.controller1 = controller1;
		this.controller2 = controller2;
	}

	public void toggleFrameLimiter()
	{
		frameLimiterOn = !frameLimiterOn;
	}

	public synchronized void loadROM(final String filename) throws Exception
	{
		loadROM(filename, null);
	}

	private synchronized void loadROM(final String filename, Integer initialPC) throws Exception
	{
		runEmulation = false;
		if (!FileUtils.exists(filename)
			|| (!FileUtils.getExtension(filename).equalsIgnoreCase(".nes")
				&& !FileUtils.getExtension(filename).equalsIgnoreCase(".nsf"))) {
			throw new Exception("Could not load file:\nFile " + filename + "\n"
					+ "does not exist or is not a valid NES game.");
		}

		Mapper mapper;
		try {
			final ROMLoader loader = new ROMLoader(filename);
			loader.parseHeader();
			mapper = Mapper.getCorrectMapper(loader);
			mapper.setLoader(loader);
			mapper.loadrom();
		} catch (BadMapperException e) {
			throw new Exception("Error Loading File: ROM is"
					+ " corrupted or uses an unsupported mapper.\n" + e.getMessage());
		} catch (Exception e) {
			throw new Exception("Error Loading File: ROM is"
					+ " corrupted or uses an unsupported mapper.\n" + e.toString() + e.getMessage());
		}

		if (apu != null) {
			//if rom already running save its sram before closing
			apu.destroy();
			saveSRAM(false);
			//also get rid of mapper etc.
			this.mapper.destroy();
			cpu = null;
			cpuram = null;
			ppu = null;
		}

		this.mapper = mapper;
		//now some annoying getting of all the references where they belong
		cpuram = this.mapper.getCPURAM();
		actionReplay = new ActionReplay(cpuram);
		cpu = this.mapper.cpu;
		ppu = this.mapper.ppu;
		apu = new APU(this, cpu, cpuram);
		cpuram.setAPU(apu);
		cpuram.setPPU(ppu);
		curRomPath = filename;

		frameCount = 0;
		//if savestate exists, load it
		if (this.mapper.hasSRAM()) {
			loadSRAM();
		}

		//and start emulation
		cpu.init(initialPC);
		this.mapper.init();
		setParameters();
		runEmulation = true;
	}

	private void saveSRAM(final boolean async)
	{
		if (mapper != null && mapper.hasSRAM() && mapper.supportsSaves()) {
			if (async) {
				FileUtils.asyncwritetofile(mapper.getPRGRam(), FileUtils.stripExtension(curRomPath) + ".sav");
			} else {
				FileUtils.writetofile(mapper.getPRGRam(), FileUtils.stripExtension(curRomPath) + ".sav");
			}
		}
	}

	private void loadSRAM()
	{
		final String name = FileUtils.stripExtension(curRomPath) + ".sav";
		if (FileUtils.exists(name) && mapper.supportsSaves()) {
			mapper.setPRGRAM(FileUtils.readFromFile(name));
		}

	}

	public void stop()
	{
		//save SRAM and quit
		//should wait for any save sram workers to be done before here
		if (cpu != null && curRomPath != null) {
			runEmulation = false;
			saveSRAM(false);
		}
		//there might be some subtle threading bug with saving?
		//System.Exit is very dirty and does NOT let the delete on exit handler
		//fire so the natives stick around...
		//shutdown = true;
		loop.stop();
		Platform.exit();

		if (controller1 != null) {
			controller1.stop();
		}
		if (controller2 != null) {
			controller2.stop();
		}
	}

	public synchronized void reset()
	{
		if (cpu != null) {
			mapper.reset();
			cpu.reset();
			runEmulation = true;
			apu.pause();
			apu.resume();
		}
		//reset frame counter as well because PPU is reset
		//on Famicom, PPU is not reset when Reset is pressed
		//but some NES games expect it to be and you get garbage.
		frameCount = 0;
	}

	public synchronized void reloadROM() throws Exception
	{
		loadROM(curRomPath);
	}

	public synchronized void pause()
	{
		if (apu != null) {
			apu.pause();
		}
		runEmulation = false;
	}

	public long getFrameTime()
	{
		return frameDoneTime;
	}

	public String getrominfo()
	{
		if (mapper != null) {
			return mapper.getrominfo();
		}
		return null;
	}

	public synchronized void frameAdvance()
	{
		runEmulation = false;
		if (cpu != null) {
			runFrame();
		}
	}

	public synchronized void resume()
	{
		if (apu != null) {
			apu.resume();
		}
		if (cpu != null) {
			runEmulation = true;
		}
	}

	public String getCurrentRomName()
	{
		return new File(curRomPath).getName();
	}

	public boolean isFrameLimiterOn()
	{
		return frameLimiterOn;
	}

	public ControllerKeyListener getcontroller1()
	{
		return controller1;
	}

	public ControllerKeyListener getcontroller2()
	{
		return controller2;
	}

	public synchronized void setParameters()
	{
		if (apu != null) {
			apu.setParameters();
		}
		if (ppu != null) {
			ppu.setParameters();
		}
		if (limiter != null && mapper != null) {
			switch (mapper.getTVType()) {
			case NTSC:
			default:
				limiter.setInterval(16639267);
				break;
			case PAL:
			case DENDY:
				limiter.setInterval(19997200);
			}
		}
	}

	/**
	 * Access to the Pro Action Replay device.
	 */
	public synchronized ActionReplay getActionReplay()
	{
		return actionReplay;
	}
}
