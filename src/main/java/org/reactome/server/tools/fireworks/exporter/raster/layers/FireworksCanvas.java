package org.reactome.server.tools.fireworks.exporter.raster.layers;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

public class FireworksCanvas {
	private final FillLayer nodes = new FillLayer();
	private final DrawLayer nodeSelection = new DrawLayer();
	private final DrawLayer edges = new DrawLayer();
	private final DrawLayer edgeSelection = new DrawLayer();
	private final TextLayer text = new TextLayer();
	private final DrawLayer edgeFlags = new DrawLayer();
	private final DrawLayer nodeFlags = new DrawLayer();
	private final FillDrawLayer legendBackground = new FillDrawLayer();
	private final FillLayer legendBar = new FillLayer();
	private final ImageLayer logoLayer = new ImageLayer();
	private final BoundTextLayer infoText = new BoundTextLayer();
	private final BoundTextLayer legendLabels = new BoundTextLayer();
	private final TextLayer legendBarLabels = new TextLayer();
	private final FillLayer tickArrows = new FillLayer();
	private final DrawLayer ticks = new DrawLayer();

	private final List<Layer> layers = Arrays.asList(
			edgeFlags,
			edgeSelection,
			edges,
			nodeFlags,
			nodeSelection,
			nodes,
			text,

			legendBackground,
			legendBar,
			legendBarLabels,
			legendLabels,
			ticks,
			tickArrows,

			logoLayer,
			infoText
	);

	public void render(Graphics2D graphics) {
		layers.forEach(layer -> layer.render(graphics));
	}

	public Rectangle2D getBounds() {
		Double minX = null;
		Double minY = null;
		Double maxX = null;
		Double maxY = null;
		for (Layer layer : layers) {
			final Rectangle2D bounds = layer.getBounds();
			if (bounds == null) continue;
			if (minX == null) {
				minX = bounds.getMinX();
				minY = bounds.getMinY();
				maxX = bounds.getMaxX();
				maxY = bounds.getMaxY();
			} else {
				if (bounds.getX() < minX) minX = bounds.getX();
				if (bounds.getY() < minY) minY = bounds.getY();
				if (bounds.getMaxX() > maxX) maxX = bounds.getMaxX();
				if (bounds.getMaxY() > maxY) maxY = bounds.getMaxY();
			}
		}
		if (minX == null) return null;
		return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
	}
	public FillLayer getNodes() {
		return nodes;
	}

	public DrawLayer getEdges() {
		return edges;
	}

	public DrawLayer getNodeSelection() {
		return nodeSelection;
	}

	public DrawLayer getEdgeSelection() {
		return edgeSelection;
	}

	public TextLayer getText() {
		return text;
	}

	public DrawLayer getEdgeFlags() {
		return edgeFlags;
	}

	public DrawLayer getNodeFlags() {
		return nodeFlags;
	}

	public FillDrawLayer getLegendBackground() {
		return legendBackground;
	}

	public FillLayer getLegendBar() {
		return legendBar;
	}

	public ImageLayer getLogoLayer() {
		return logoLayer;
	}

	public BoundTextLayer getInfoText() {
		return infoText;
	}

	public BoundTextLayer getLegendLabels() {
		return legendLabels;
	}

	public FillLayer getTickArrows() {
		return tickArrows;
	}

	public DrawLayer getTicks() {
		return ticks;
	}

	public TextLayer getLegendBarLabels() {
		return legendBarLabels;
	}
}
