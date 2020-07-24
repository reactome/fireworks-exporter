package org.reactome.server.tools.fireworks.exporter.common.profiles;

import java.awt.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * From String to Color, from Color to String. Parse colors in hex RGB (#FF0000)
 * and rgba(255,255,0, 0.5). Also contain color interpolation methods.
 */
public class ColorFactory {
	private final static Pattern RGBA = Pattern.compile("^rgba\\(\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])\\s*,\\s*((0.[0-9]+)|[01]|1.0*)\\s*\\)$");
	private static final float INV_255 = 0.003921569f; // 1 / 255

	// speed up with a color cache
	// of course, this shouldn't be necessary if the Profiles already had the
	// colors parsed
	private static final Map<String, Color> cache = new HashMap<>();

	public static Color parseColor(String color) {
		if (color == null || color.trim().isEmpty()) return null;
		return cache.computeIfAbsent(color, ColorFactory::strToColor);
	}

	private static Color strToColor(String color) {
		return color.startsWith("#")
				? hexToColor(color)
				: rgbaToColor(color);
	}

	public static Color hexToColor(String input) {
		int r = Integer.valueOf(input.substring(1, 3), 16);
		int g = Integer.valueOf(input.substring(3, 5), 16);
		int b = Integer.valueOf(input.substring(5, 7), 16);

		return new Color(r, g, b);
	}

	private static Color rgbaToColor(String input) {
		final Matcher m = RGBA.matcher(input);
		if (m.matches()) {
			return new Color(Integer.parseInt(m.group(1)),
					Integer.parseInt(m.group(2)),
					Integer.parseInt(m.group(3)),
					(int) (Float.parseFloat(m.group(4)) * 255f));
		}
		return null;
	}

	public static Color interpolate(GradientColorProfile gradient, double scale) {
		if (gradient.getStop() == null)
			return interpolate(gradient.getMin(), gradient.getMax(), scale);
		else if (scale < 0.5)
			return interpolate(gradient.getMin(), gradient.getStop(), scale * 2);
		else
			return interpolate(gradient.getStop(), gradient.getMax(), (scale - 0.5) * 2);
	}

	public static Color interpolate(Color a, Color b, double t) {
		if (t <= 0.0) return a;
		if (t >= 1.0) return b;
		float scale = (float) t;
		return new Color(
				(int) (a.getRed() + (b.getRed() - a.getRed()) * scale),
				(int) (a.getGreen() + (b.getGreen() - a.getGreen()) * scale),
				(int) (a.getBlue() + (b.getBlue() - a.getBlue()) * scale),
				(int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * scale));
	}


	public static String hex(Color color) {
		return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
	}

	@SuppressWarnings("unused")
	public static String rgba(Color color) {
		final float alpha = color.getAlpha() * INV_255;
		String a;
		if (alpha > 0.99) a = "1";
		else a = String.format("%.2f", alpha);
		return String.format(Locale.UK, "rgba(%d,%d,%d,%s)", color.getRed(),
				color.getGreen(), color.getBlue(), a);
	}

}
