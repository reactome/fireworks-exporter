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
	private List<RenderableText> objects = new LinkedList<>();

	@Override
	public void render(Graphics2D graphics) {
		objects.forEach(text -> {
			graphics.setPaint(text.color);
			final List<String> lines = getLines(text.text);
			final float height = lines.size() * FontProperties.DEFAULT_FONT.getSize();
			float y = (float) (text.nodePosition.y - height - TEXT_DISTANCE);
			for (String line : lines) {
				final int lineWidth = graphics.getFontMetrics().charsWidth(line.toCharArray(), 0, line.length());
				float x = (float) (text.nodePosition.getX() - 0.5 * lineWidth);
				graphics.drawString(line, x, y);
				y += 1.75 * FontProperties.DEFAULT_FONT.getSize();
			}
		});
	}

	private List<String> getLines(String text) {
		// If less than 15 characters, return a line
		// else, split into 2 similar lines
		if (text.length() <= 15) {
			return Collections.singletonList(text);
		} else {
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

	public void add(String text, Color color, Point2D.Double center) {
		objects.add(new RenderableText(text, color, center));
	}

	private class RenderableText {

		private final String text;
		private final Color color;
		private final Point2D.Double nodePosition;

		RenderableText(String text, Color color, Point2D.Double nodePosition) {
			this.text = text;
			this.color = color;
			this.nodePosition = nodePosition;
		}
	}

}
