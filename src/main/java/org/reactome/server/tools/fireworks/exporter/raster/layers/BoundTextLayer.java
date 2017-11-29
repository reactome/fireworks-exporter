package org.reactome.server.tools.fireworks.exporter.raster.layers;

import org.reactome.server.tools.fireworks.exporter.raster.renderers.TextRenderer;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

public class BoundTextLayer extends CommonLayer {

	private List<RenderableText> objects = new LinkedList<>();

	public void add(String text, Color color, Rectangle2D limits, Font font) {
		objects.add(new RenderableText(text, limits, color, font));
		addShape(new Rectangle2D.Double(limits.getX(), limits.getY(), limits.getWidth(), limits.getHeight()));
	}

	@Override
	public void render(Graphics2D graphics) {
		objects.forEach(text -> {
			graphics.setFont(text.font);
			graphics.setPaint(text.color);
			TextRenderer.drawText(graphics, text.text, text.limits);
		});

	}

	@Override
	public void clear() {
		super.clear();
		objects.clear();
	}

	private class RenderableText {

		private final String text;
		private final Rectangle2D limits;
		private final Color color;
		private final Font font;

		RenderableText(String text, Rectangle2D limits, Color color, Font font) {
			this.text = text;
			this.limits = limits;
			this.color = color;
			this.font = font;
		}

	}
}
