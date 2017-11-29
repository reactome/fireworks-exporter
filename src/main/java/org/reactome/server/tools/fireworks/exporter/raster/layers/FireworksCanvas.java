package org.reactome.server.tools.fireworks.exporter.raster.layers;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

public class FireworksCanvas {
	private FillLayer nodes = new FillLayer();
	private DrawLayer nodeSelection = new DrawLayer();
	private DrawLayer edges = new DrawLayer();
	private DrawLayer edgeSelection = new DrawLayer();
	private DrawLayer flags = new DrawLayer();
	private TextLayer text = new TextLayer();
	private DrawLayer edgeFlags = new DrawLayer();
	private DrawLayer nodeFlags = new DrawLayer();
	private FillDrawLayer legendBackground = new FillDrawLayer();
	private FillLayer legendBar = new FillLayer();
	private ImageLayer logoLayer = new ImageLayer();
	private BoundTextLayer infoText = new BoundTextLayer();
	private BoundTextLayer legendLabels = new BoundTextLayer();
	private FillLayer tickArrows = new FillLayer();
	private DrawLayer ticks = new DrawLayer();

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
}
