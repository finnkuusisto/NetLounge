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

package kuusisto.finn.netlounge.server;

import java.net.InetAddress;

public class Person {

	private InetAddress address;
	private int port;
	private String name;
	private int id;
	private double x;
	private double y;
	
	public Person() { }
	
	public Person(InetAddress address, int port, String name, int id) {
		this.address = address;
		this.port = port;
		this.name = name;
		this.id = id;
	}
	
	public void setAddress(InetAddress address) {
		this.address = address;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public InetAddress getAddress() {
		return this.address;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getID() {
		return this.id;
	}
	
	public double getX() {
		return this.x;
	}
	
	public double getY() {
		return this.y;
	}
	
}
