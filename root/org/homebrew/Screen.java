package org.homebrew;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

public class Screen extends Container {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4761178503523947426L;
	private ArrayList messages;
	private Font font;
	
	public Screen(ArrayList messages) {
		this.messages = messages;
		font = new Font(null, Font.PLAIN, 36);
	}

	public void paint(Graphics g) {
		 g.setFont(font);
        g.setColor(new Color(100, 110, 160));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(new Color(255, 255, 255));
        int top = 60;
        for (int i = 0; i < messages.size(); i++) {
        	String message = (String) messages.get(i);
           int message_width = g.getFontMetrics().stringWidth(message);
           g.drawString(message, 100, top + (i*40));
        }
    }
}
