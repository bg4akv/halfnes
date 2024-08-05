package com.grapeshot.halfnes.ppu;

import java.util.BitSet;

public class OAM {

	private static class Sprite {
		public byte yPos;
		public byte tileIndex;
		public byte attributes;
		public byte xPos;

		public BitSet bitSet;
	}

}
