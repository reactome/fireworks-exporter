package org.reactome.server.tools.fireworks.exporter.profiles;

import java.awt.*;

public class CommonColorProfile {

	private Color initial;
	private Color highlight;
	private Color selection;
	private Color flag;
	private Color fadeout;
	public void setHighlight(String color) {
		this.highlight = ColorFactory.parseColor(color);
	}
	public void setInitial(String color) {
		this.initial = ColorFactory.parseColor(color);
	}
	public void setSelection(String color) {
		this.selection = ColorFactory.parseColor(color);
	}
	public void setFlag(String color) {
		this.flag = ColorFactory.parseColor(color);
	}
	public void setFadeout(String color) {
		this.fadeout = ColorFactory.parseColor(color);
	}

	public Color getFadeout() {
		return fadeout;
	}

	public Color getFlag() {
		return flag;
	}

	public Color getHighlight() {
		return highlight;
	}

	public Color getInitial() {
		return initial;
	}

	public Color getSelection() {
		return selection;
	}

}
