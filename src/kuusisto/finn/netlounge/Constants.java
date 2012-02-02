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

package kuusisto.finn.netlounge;

public class Constants {
	
	public static final String MSG_CONNECT = "connect";
	public static final String MSG_KEEPALIVE = "keepalive";
	public static final String MSG_COMMAND = "command";
	public static final String MSG_CONFIRM = "confirm";
	public static final String MSG_STATE = "state";
	public static final String MSG_AUDIO = "audio";
	public static final String MSG_INLINE_SEP = ",";
	public static final String MSG_LINE_SEP = "\n";
	
	public static final int PCMD_UP = 0;
	public static final int PCMD_UP_RIGHT = 1;
	public static final int PCMD_RIGHT = 2;
	public static final int PCMD_DOWN_RIGHT = 3;
	public static final int PCMD_DOWN = 4;
	public static final int PCMD_DOWN_LEFT = 5;
	public static final int PCMD_LEFT = 6;
	public static final int PCMD_UP_LEFT = 7;
	
	public static final int SAMPLE_RATE = 8000;

}
