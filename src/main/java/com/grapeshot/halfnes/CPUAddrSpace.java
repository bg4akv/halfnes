/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.grapeshot.halfnes;

import java.util.HashMap;
import java.util.Map;

import com.grapeshot.halfnes.cheats.Patch;
import com.grapeshot.halfnes.device.APU;
import com.grapeshot.halfnes.device.ExpansionOrIO;
import com.grapeshot.halfnes.device.NESDev;
import com.grapeshot.halfnes.device.PPU;
import com.grapeshot.halfnes.device.RAM;
import com.grapeshot.halfnes.device.SRAM;
import com.grapeshot.halfnes.mappers.Mapper;


/**
 *
 * @author Andrew Hoffman
 *
 *
 */
public class CPUAddrSpace {
	//private final int[] wram = new int[2048];
	private final RAM ram;
	private final SRAM sram;
	private final ExpansionOrIO expansionOrIO;

	public Mapper mapper;
	public APU apu;
	private PPU ppu; //need these to call their write handlers from here.
	private Map<Integer, Patch> patches = new HashMap<>();



	public CPUAddrSpace(final Mapper mappy)
	{
		mapper = mappy;
		// init memory
		//Arrays.fill(wram, 0xff);
		ram = new RAM();
		sram = new SRAM();
		expansionOrIO = new ExpansionOrIO();
	}

	public final int read(final int addr)
	{
		if (!patches.isEmpty()) {
			int retval = _read(addr);
			Patch p = patches.get(addr);
			if (p != null && p.getAddress() == addr && p.matchesData(retval)) {
				return p.getData();
			}
			return retval;
		} else {
			return _read(addr);
		}
	}

	private final int _read(final int addr)
	{
		if (addr > 0x4018) {
			return mapper.read(addr);
		} else if (addr <= 0x1fff) {
			return ram.read(addr);
		} else if (addr <= 0x3fff) {
			// 8 byte ppu regs; mirrored lots
			return ppu.read(addr & 7);
		} else if (0x4000 <= addr && addr <= 0x4018) {
			return apu.read(addr - 0x4000);
		} else {
			return addr >> 8; //open bus
		}
	}

	public final void write(final int addr, final int data)
	{
		//if((data & 0xff) != data){
		//	System.err.println("DANGER WILL ROBINSON");
		//}
		if (addr > 0x4018) {
			mapper.write(addr, data);
		} else if (addr <= 0x1fff) {
			ram.write(addr, data);
		} else if (addr <= 0x3fff) {
			// 8 byte ppu regs; mirrored lots
			ppu.write(addr & 7, data);
		} else if (0x4000 <= addr && addr <= 0x4018) {
			apu.write(addr - 0x4000, data);
		}
	}


	private NESDev selectDevice(int addr)
	{
		//bit 15-13
		switch ((addr >> 13) & 0x07) {
		case 0: //CPU RAM
			return ram;
		case 1: //PPU
			return ppu;
		case 2: //Expansion/I/O
			return expansionOrIO.selectDevice(addr);
		case 3: //SRAM
			return sram;
		case 4: //PRG ROM bank 0
			return null;
		case 5: //PRG ROM bank 1
			return null;
		case 6: //PRG ROM bank 2
			return null;
		case 7: //PRG ROM bank 3
			return null;
		default:
			return null;
		}
	}


	public void setAPU(APU apu)
	{
		this.apu = apu;
	}

	public void setPPU(PPU ppu)
	{
		this.ppu = ppu;
	}

	public void setPatches(HashMap<Integer, Patch> p)
	{
		this.patches = p;
	}
}
