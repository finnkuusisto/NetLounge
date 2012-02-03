/*
 * Copyright (C) 2012 by Finn Kuusisto
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package kuusisto.finn.netlounge.client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

//To anyone: you probably don't want to use this mixer
//this is just a proof of concept
public class CrappyClientMixer {

	private Map<Integer,Queue<Byte>> buffers;
	private Map<Integer,Long> timestamps;
	
	public CrappyClientMixer() {
		this.buffers = new HashMap<Integer,Queue<Byte>>();
		this.timestamps = new HashMap<Integer,Long>();
	}
	
	public synchronized void writeBytes(int id, long timestamp, byte[] data,
			int offset,	int length) {
		//seen this one yet?
		if (!this.buffers.containsKey(id)) {
			this.buffers.put(id, new LinkedList<Byte>());
			this.timestamps.put(id, 0L);
		}
		//new or old data
		if (timestamp <= this.timestamps.get(id)) {
			return; //old
		}
		//otherwise add it
		Queue<Byte> buf = this.buffers.get(id);
		for (int i = offset; i < offset + length; i++) {
			buf.add(data[i]);
		}
		//update timestamp
		this.timestamps.put(id, timestamp);
	}
	
	public synchronized int readBytes(byte[] data) {
		int numRead = 0;
		for (int i = 0; i < data.length; i++) {
			int next = this.nextByte();
			if (next != -1000) {
				data[i] = (byte)next;
				numRead++;
			}
		}
		return numRead;
	}
	
	public synchronized int nextByte() {
		//assumes 8-bit, linear PCM
		int numLines = 0;
		int val = 0;
		for (Queue<Byte> buf : this.buffers.values()) {
			if (buf.size() > 0) {
				val += buf.poll();
				numLines++;
			}
		}
		//average or -1000 if no data
		return (numLines == 0) ? -1000 : (val / numLines);
	}
	
}
