/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.grapeshot.halfnes.mappers;

import com.grapeshot.halfnes.*;

public class AnromMapper extends Mapper {

	@Override
	public void loadrom() throws BadMapperException {
		super.loadrom();
		for (int i = 0; i < 32; ++i) {
			prg_map[i] = (1024 * i) & (prgsize - 1);
		}
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * i) & (chrsize - 1);
		}
	}

	@Override
	public final void cartWrite(final int addr, final int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}
		//remap all 32k of PRG to 32 x bank #
		for (int i = 0; i < 32; ++i) {
			prg_map[i] = (1024 * (i + (32 * (data & 15)))) & (prgsize - 1);
		}
		setmirroring(((data & (utils.BIT4)) != 0) ? MirrorType.SS_MIRROR1 : MirrorType.SS_MIRROR0);

	}
}
