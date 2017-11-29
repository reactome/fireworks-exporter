package org.reactome.server.tools.fireworks.exporter.profiles;

import java.awt.*;

public class GradientColorProfile {
	private Color min;
	private Color stop;
	private Color max;


	public void setMin(String color) {
		this.min = ColorFactory.parseColor(color);
	}

	public void setMax(String color) {
		this.max = ColorFactory.parseColor(color);
	}
	public void setStop(String color) {
		this.stop = ColorFactory.parseColor(color);
	}

	public Color getStop() {
		return stop;
	}

	public Color getMax() {
		return max;
	}

	public Color getMin() {
		return min;
	}
}
