package org.reactome.server.tools.fireworks.exporter.raster.layers;

import org.reactome.server.tools.fireworks.exporter.common.profiles.ColorFactory;
import org.reactome.server.tools.fireworks.exporter.common.profiles.GradientColorProfile;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegulationSheet {

	private GradientColorProfile gradient;
	private Map<Integer, Color> colorMap;
	private Color text;

	public RegulationSheet(GradientColorProfile gradient) {
		this.gradient = gradient;
		init();
	}

	public Color getText() {
		return text;
	}

	public GradientColorProfile getGradient() {
		return gradient;
	}

	public void setText(String color) {
		this.text = ColorFactory.parseColor(color);
	}

	public Map<Integer, Color> getColorMap() {
		return colorMap;
	}

	private void init() {
		colorMap = new LinkedHashMap<>(5);
		colorMap.put(2, ColorFactory.interpolate(gradient, 0));
		colorMap.put(1, ColorFactory.interpolate(gradient, 0.25));
		colorMap.put(0, ColorFactory.interpolate(gradient, 0.5));
		colorMap.put(-1, ColorFactory.interpolate(gradient, 0.75));
		colorMap.put(-2, ColorFactory.interpolate(gradient, 1));
	}
}
