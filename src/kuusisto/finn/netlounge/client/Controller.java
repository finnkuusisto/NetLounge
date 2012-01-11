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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputListener;

public class Controller implements KeyListener, MouseInputListener,
									FocusListener {
	
	//key events
	public static final int K_UP = 0;
	public static final int K_DOWN = 1;
	public static final int K_RIGHT = 2;
	public static final int K_LEFT = 3;
	public static final int K_SPACE = 4;
	public static final int K_ESCAPE = 5;
	public static final int K_ENTER = 6;
	public static final int K_W = 7;
	public static final int K_A = 8;
	public static final int K_S = 9;
	public static final int K_D = 10;
	private static final int NUM_KEYS = 11;
	
	//mouse events
	private static boolean mouseDown = false;
	
	private static boolean[] keys = new boolean[Controller.NUM_KEYS];
	private static Controller controller = new Controller();
	
	public static void init() { }
	
	public static Controller getListener() {
		return Controller.controller;
	}
	
	public static boolean isKeyDown(int key) {
		return (key >= Controller.K_UP && key <= Controller.K_D) ?
				Controller.keys[key] : false;
	}
	
	public static boolean isMouseDown() {
		return Controller.mouseDown;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			Controller.keys[Controller.K_UP] = true;
			break;
		case KeyEvent.VK_DOWN:
			Controller.keys[Controller.K_DOWN] = true;
			break;
		case KeyEvent.VK_RIGHT:
			Controller.keys[Controller.K_RIGHT] = true;
			break;
		case KeyEvent.VK_LEFT:
			Controller.keys[Controller.K_LEFT] = true;
			break;
		case KeyEvent.VK_W:
			Controller.keys[Controller.K_W] = true;
			break;
		case KeyEvent.VK_A:
			Controller.keys[Controller.K_A] = true;
			break;
		case KeyEvent.VK_S:
			Controller.keys[Controller.K_S] = true;
			break;
		case KeyEvent.VK_D:
			Controller.keys[Controller.K_D] = true;
			break;
		case KeyEvent.VK_SPACE:
			Controller.keys[Controller.K_SPACE] = true;
			break;
		case KeyEvent.VK_ENTER:
			Controller.keys[Controller.K_ENTER] = true;
			break;
		case KeyEvent.VK_ESCAPE:
			Controller.keys[Controller.K_ESCAPE] = true;
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			Controller.keys[Controller.K_UP] = false;
			break;
		case KeyEvent.VK_DOWN:
			Controller.keys[Controller.K_DOWN] = false;
			break;
		case KeyEvent.VK_RIGHT:
			Controller.keys[Controller.K_RIGHT] = false;
			break;
		case KeyEvent.VK_LEFT:
			Controller.keys[Controller.K_LEFT] = false;
			break;
		case KeyEvent.VK_W:
			Controller.keys[Controller.K_W] = false;
			break;
		case KeyEvent.VK_A:
			Controller.keys[Controller.K_A] = false;
			break;
		case KeyEvent.VK_S:
			Controller.keys[Controller.K_S] = false;
			break;
		case KeyEvent.VK_D:
			Controller.keys[Controller.K_D] = false;
			break;
		case KeyEvent.VK_SPACE:
			Controller.keys[Controller.K_SPACE] = false;
			break;
		case KeyEvent.VK_ENTER:
			Controller.keys[Controller.K_ENTER] = false;
			break;
		case KeyEvent.VK_ESCAPE:
			Controller.keys[Controller.K_ESCAPE] = false;
			break;
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) { /*unused*/ }

	@Override
	public void mouseClicked(MouseEvent e) { /*unused*/ }

	@Override
	public void mouseEntered(MouseEvent e) { /*unused*/ }

	@Override
	public void mouseExited(MouseEvent e) { /*unused*/ }

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			Controller.mouseDown = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			Controller.mouseDown = false;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) { /*unused*/ }

	@Override
	public void mouseMoved(MouseEvent e) { /*unused*/ }

	@Override
	public void focusGained(FocusEvent e) { /*unused*/ }

	@Override
	public void focusLost(FocusEvent e) {
		Controller.mouseDown = false;
		for (int i = 0; i < Controller.keys.length; i++) {
			Controller.keys[i] = false;
		}
	}

	
	
}
