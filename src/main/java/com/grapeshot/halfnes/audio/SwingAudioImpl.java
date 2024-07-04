/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.grapeshot.halfnes.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import com.grapeshot.halfnes.PrefsSingleton;
import com.grapeshot.halfnes.mappers.Mapper;

/**
 *
 * @author Andrew
 */
public class SwingAudioImpl implements AudioOutInterface {

	private boolean soundEnable;
	private SourceDataLine sdl;
	private byte[] audiobuf;
	private int bufptr = 0;
	private float outputvol;

	public SwingAudioImpl(final int samplerate, Mapper.TVType tvtype) throws Exception
	{
		soundEnable = PrefsSingleton.getInstance().getBoolean("soundEnable", true);
		outputvol = (float) (PrefsSingleton.getInstance().getInt("outputvol", 13107) / 16384.);
		double fps;
		switch (tvtype) {
			case NTSC:
			default:
				fps = 60.;
				break;
			case PAL:
			case DENDY:
				fps = 50.;
				break;
		}

		if (soundEnable) {
			final int samplesperframe = (int) Math.ceil((samplerate * 2) / fps);
			audiobuf = new byte[samplesperframe * 2];
			try {
				AudioFormat af = new AudioFormat(
						samplerate,
						16,//bit
						2,//channel
						true,//signed
						false //little endian
				//(works everywhere, afaict, but macs need 44100 sample rate)
				);
				sdl = AudioSystem.getSourceDataLine(af);
				sdl.open(af, samplesperframe * 4 * 2 /*ch*/ * 2 /*bytes/sample*/);
				//create 4 frame audio buffer
				sdl.start();
			} catch (Exception a) {
				soundEnable = false;
				throw new Exception(a.getMessage());
			}
		}
	}

	@Override
	public final void flushFrame(final boolean waitIfBufferFull)
	{
		bufptr = 0;

		if (!soundEnable) {
			return;
		}

//		if (sdl.available() == sdl.getBufferSize()) {
//			System.err.println("Audio is underrun");
//		}
		if (sdl.available() < bufptr) {
//		System.err.println("Audio is blocking");
			if (waitIfBufferFull) {
				//write to audio buffer and don't worry if it blocks
				sdl.write(audiobuf, 0, bufptr);
			}
			//else don't bother to write if the buffer is full
		} else {
			sdl.write(audiobuf, 0, bufptr);
		}
	}

	@Override
	public final void outputSample(int sample)
	{
		if (!soundEnable) {
			return;
		}

		sample *= outputvol;
		if (sample < -32768) {
			sample = -32768;
			//System.err.println("clip");
		}
		if (sample > 32767) {
			sample = 32767;
			//System.err.println("clop");
		}
		//left ch
		int lch = sample;
		audiobuf[bufptr] = (byte) (lch & 0xff);
		audiobuf[bufptr + 1] = (byte) ((lch >> 8) & 0xff);
		//right ch
		int rch = sample;
		audiobuf[bufptr + 2] = (byte) (rch & 0xff);
		audiobuf[bufptr + 3] = (byte) ((rch >> 8) & 0xff);
		bufptr += 4;
	}

	@Override
	public void pause()
	{
		if (soundEnable) {
			sdl.flush();
			sdl.stop();
		}
	}

	@Override
	public void resume()
	{
		if (soundEnable) {
			sdl.start();
		}
	}

	@Override
	public final void destroy()
	{
		if (soundEnable) {
			sdl.stop();
			sdl.close();
		}
	}

	@Override
	public final boolean bufferHasLessThan(final int samples)
	{
		//returns true if the audio buffer has less than the specified amt of samples remaining in it
		return (sdl == null)? false
				: ((sdl.getBufferSize() - sdl.available()) <= samples);
	}
}
