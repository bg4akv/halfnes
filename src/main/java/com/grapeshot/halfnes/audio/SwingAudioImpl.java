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
	private SourceDataLine sourceDataLine;
	private byte[] audioBuf;
	private int idx = 0;
	private float outputVol;

	public SwingAudioImpl(final int sampleRate, Mapper.TVType tvtype) throws Exception
	{
		soundEnable = PrefsSingleton.getInstance().getBoolean("soundEnable", true);
		outputVol = (float) (PrefsSingleton.getInstance().getInt("outputvol", 13107) / 16384.);

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

		if (!soundEnable) {
			return;
		}

		int samplesPerFrame = (int) Math.ceil((sampleRate * 2) / fps);
		audioBuf = new byte[samplesPerFrame * 2];

		try {
			AudioFormat af = new AudioFormat(
					sampleRate,
					16,//bit
					2,//channel
					true,//signed
					false);//little endian
			//(works everywhere, afaict, but macs need 44100 sample rate)

			sourceDataLine = AudioSystem.getSourceDataLine(af);
			sourceDataLine.open(af, samplesPerFrame * 4 * 2 /*ch*/ * 2 /*bytes/sample*/);
			//create 4 frame audio buffer
			sourceDataLine.start();
		} catch (Exception e) {
			soundEnable = false;
			throw e;
		}
	}

	@Override
	public final void flushFrame(final boolean waitIfBufferFull)
	{
		if (!soundEnable) {
			return;
		}

		if (idx <= sourceDataLine.available()
			|| (idx > sourceDataLine.available() && waitIfBufferFull)) {
			sourceDataLine.write(audioBuf, 0, idx);
		}

		idx = 0;
	}

	@Override
	public final void outputSample(int sample)
	{
		if (!soundEnable) {
			return;
		}

		sample *= outputVol;
		if (sample < -32768) {
			sample = -32768;
		}
		if (sample > 32767) {
			sample = 32767;
		}

		//left channel
		audioBuf[idx] = (byte) (sample & 0xff);
		audioBuf[idx + 1] = (byte) ((sample >> 8) & 0xff);

		//right channel
		audioBuf[idx + 2] = audioBuf[idx];
		audioBuf[idx + 3] = audioBuf[idx + 1];

		idx += 4;
		if (idx >= audioBuf.length) {
			idx = 0;
		}
	}

	@Override
	public void pause()
	{
		if (soundEnable) {
			sourceDataLine.flush();
			sourceDataLine.stop();
		}
	}

	@Override
	public void resume()
	{
		if (soundEnable) {
			sourceDataLine.start();
		}
	}

	@Override
	public final void destroy()
	{
		if (soundEnable) {
			sourceDataLine.stop();
			sourceDataLine.close();
		}
	}

	@Override
	public final boolean bufferHasLessThan(final int samples)
	{
		//returns true if the audio buffer has less than the specified amt of samples remaining in it
		return (sourceDataLine == null)? false
				: ((sourceDataLine.getBufferSize() - sourceDataLine.available()) <= samples);
	}
}
