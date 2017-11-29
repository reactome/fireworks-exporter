package org.reactome.server.tools.fireworks.exporter.raster.renderers;

import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Where the magic of the texts happens. Two methods are provided:
 * <code>drawTextSingleLine()</code> for drawing texts in one line, regardless
 * of the width of the line, and <code>drawText()</code> for drawing the text
 * inside a box, wrapping and shrinking it if necessary.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class TextRenderer {

	private static final List<Character> WORD_SPLIT_CHARS = Arrays.asList(':', '.', '-', ',', ')', '/', '+');
	private static final float SHADOW_OFFSET = 0.5f;
	private static final Color ANALYSIS_SHADOW_COLOR = Color.GRAY;
	private static final Color ANALYSIS_TEXT_COLOR = Color.WHITE;

	/**
	 * Displays text in the assigned space. If the text does not fit in one
	 * line, it is split in several lines. If the font size is very large, it is
	 * lower until the text fits. Each line is centered to the box and the whole
	 * text is vertically centered.
	 *
	 * @param graphics  canvas to write
	 * @param text      text to display
	 * @param limits    box where text should be fit
	 */
	public static void drawText(Graphics2D graphics, String text, Rectangle2D limits) {
		// Fit the text
		Font font = graphics.getFont();
		List<String> lines;
		while ((lines = fit(text, font, graphics, limits.getWidth(), limits.getHeight())) == null)
			font = font.deriveFont(font.getSize() - 1f);

		// Impossible to fit even with font size 1. May happen with thumbnails.
		// Don't draw anything
		if (lines.isEmpty()) return;

		final Font old = graphics.getFont();
		graphics.setFont(font);

		final int lineHeight = graphics.getFontMetrics().getHeight();
		final int textHeight = lines.size() * lineHeight;
		final double centerX = limits.getX() + limits.getWidth() * 0.5;
		double yOffset = limits.getY() + (limits.getHeight() - textHeight) * 0.5;
		// Centering at ascent gives a more natural view (centers at -)
		// https://goo.gl/x1EExY [difference between ascent/descent/height]
		yOffset += graphics.getFontMetrics().getAscent();
		for (int i = 0; i < lines.size(); i++) {
			final String line = lines.get(i);
			final int lineWidth = computeWidth(line, graphics);
			final float left = (float) (centerX - 0.5 * lineWidth);
			final float base = (float) (yOffset + i * lineHeight);
			graphics.drawString(line, left, base);
		}
		graphics.setFont(old);
	}

	/**
	 * @return a list of lines if text can be fit inside maxWidth and maxHeight.
	 * If text does not fit, returns null. If font size is less than 1, then it
	 * returns an empty list to indicate that text is impossible to be shown,
	 * probably because image size is too small.
	 */
	private static List<String> fit(String text, Font font, Graphics2D graphics, double maxWidth, double maxHeight) {
		if (font.getSize() < 1) return Collections.emptyList();
		// Test if text fits in 1 line
		if (computeWidth(text, font, graphics) < maxWidth
				&& computeHeight(1, font, graphics) < maxHeight)
			return Collections.singletonList(text);

		final List<String> lines = new LinkedList<>();
		final String[] words = text.trim().split(" ");
		String line = "";
		String temp;
		for (String word : words) {
			temp = line.isEmpty() ? word : line + " " + word;
			if (computeWidth(temp, font, graphics) < maxWidth)
				line = temp;
			else {
				// Split word in smaller parts and add as much parts as possible
				// to the line
				final List<String> parts = splitWord(word);
				boolean firstPart = true;
				for (String part : parts) {
					// If the part can't fit a line, the text won't fit
					if (computeWidth(part, font, graphics) > maxWidth)
						return null;
					if (line.isEmpty()) temp = part;
					else if (firstPart) temp = line + " " + part;
					else temp = line + part;
					if (computeWidth(temp, font, graphics) < maxWidth)
						line = temp;
					else {
						// Start a new line with part
						lines.add(line);
						line = part;
						if (computeHeight(lines.size(), font, graphics) > maxHeight)
							return null;
					}
					firstPart = false;
				}
			}
		}
		if (!line.isEmpty()) lines.add(line);
		if (computeHeight(lines.size(), font, graphics) > maxHeight)
			return null;
		else return lines;
	}

	private static int computeHeight(int lines, Font font, Graphics2D graphics) {
		return lines * graphics.getFontMetrics(font).getHeight();
	}

	private static int computeWidth(String text, Font font, Graphics2D graphics) {
		return graphics.getFontMetrics(font).charsWidth(text.toCharArray(), 0, text.length());
	}

	private static int computeWidth(String text, Graphics2D graphics) {
		return graphics.getFontMetrics().charsWidth(text.toCharArray(), 0, text.length());
	}

	/**
	 * Will split a word by any character in WORD_SPLIT_CHARS
	 * <pre>:.-,)/+</pre>
	 * The split characters are inserted as the last character of the fragment
	 *
	 * For example <pre>splitWord("p-T402-PAK2(213-524)")</pre> will result in
	 * <pre>{"p-", "T402-", "PAK2(", "213-", "524)"}</pre>
	 */
	private static List<String> splitWord(String word) {
		final List<String> parts = new LinkedList<>();
		int start = 0;
		for (int i = 0; i < word.length(); i++) {
			if (WORD_SPLIT_CHARS.contains(word.charAt(i))) {
				parts.add(word.substring(start, i + 1));
				start = i + 1;
			}
		}
		final String end = word.substring(start);
		if (!end.isEmpty()) parts.add(end);
		return parts;
	}

	/**
	 * Renders the given text in the desired position.
	 *
	 * @param graphics canvas to draw into
	 * @param text     text to draw
	 * @param position top left position of text
	 */
	public static void drawTextSingleLine(Graphics2D graphics, String text, Coordinate position) {
		drawTextSingleLine(graphics, text, position.getX(), position.getY());
	}

	private static void drawTextSingleLine(Graphics2D graphics, String text, double x, double y) {
		final int height = graphics.getFontMetrics().getHeight();
		final int baseY = (int) (y + height);
		graphics.drawString(text, (int) x, baseY);
	}
}
