package org.reactome.server.tools.fireworks.exporter.raster.layers;

import org.reactome.server.tools.fireworks.exporter.raster.properties.FontProperties;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class TextLayer extends CommonLayer {

	private static final double TEXT_DISTANCE = 15;

	private final List<RenderableText> objects = new LinkedList<>();

	@Override
	public void render(Graphics2D graphics) {
		objects.forEach(text -> renderText(graphics, text));
	}

	private void renderText(Graphics2D graphics, RenderableText text) {
		graphics.setPaint(text.color);
		final List<String> lines = getLines(text.text);
		// baseline of first line
		final int baseline = FontProperties.DEFAULT_FONT.getSize() * (lines.size() - 1);
		float y = (float) (text.nodePosition.y - TEXT_DISTANCE - baseline);
		for (String line : lines) {
			final int lineWidth = graphics.getFontMetrics().charsWidth(line.toCharArray(), 0, line.length());
			float x = (float) (text.nodePosition.getX() - 0.5 * lineWidth);
			graphics.drawString(line, x, y);
			y += 1.25 * FontProperties.DEFAULT_FONT.getSize();
		}
	}

	private List<String> getLines(String text) {
		if (text.length() <= 15)
			return Collections.singletonList(text);
		else {
			final List<String> words = Arrays.asList(text.split("\\s+"));
			final int midpoint = words.size() / 2;
			return Arrays.asList(
					String.join(" ", words.subList(0, midpoint)),
					String.join(" ", words.subList(midpoint, words.size())));
		}
	}

	@Override
	public void clear() {
		super.clear();
		objects.clear();
	}

	public void add(String text, Point2D.Double center, Color color) {
		objects.add(new RenderableText(text, center, color));
	}

	private class RenderableText {

		private final String text;
		private final Point2D.Double nodePosition;
		private final Color color;

		RenderableText(String text, Point2D.Double nodePosition, Color color) {
			this.text = text;
			this.nodePosition = nodePosition;
			this.color = color;
		}
	}

}
