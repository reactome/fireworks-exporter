package org.reactome.server.tools.fireworks.exporter.raster.index;

import org.reactome.server.tools.fireworks.exporter.profiles.FireworksColorProfile;
import org.reactome.server.tools.fireworks.exporter.raster.layers.FireworksCanvas;

import java.awt.*;
import java.awt.geom.Path2D;

public class Edge extends FireworksElement {

	private static final Stroke DEFAULT_STROKE = new BasicStroke(0.2f);
	private static final Stroke SELECTION_STROKE = new BasicStroke(0.4f);
	private static final Stroke FLAG_STROKE = new BasicStroke(0.6f);

	private final Node from;
	private final Node to;

	public Edge(Node from, Node to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		from.setSelected(selected);
	}

	@Override
	public void setFlag(boolean flag) {
		super.setFlag(flag);
		from.setFlag(true);
	}

	public Node getFrom() {
		return from;
	}

	public Node getTo() {
		return to;
	}

	public void render(FireworksCanvas canvas, FireworksColorProfile profile) {
		final Path2D path = new Path2D.Double();
		path.moveTo(from.getNode().getX(), from.getNode().getY());

		double dX = to.getNode().getX() - from.getNode().getX();
		double dY = to.getNode().getY() - from.getNode().getY();
		double angle = Math.atan2(dY, dX) - (Math.PI / 6);
		double r = Math.sqrt(dX * dX + dY * dY) * 3 / 5.0;
		double x = from.getNode().getX() + r * Math.cos(angle);
		double y = from.getNode().getY() + r * Math.sin(angle);

		path.quadTo(x, y, to.getNode().getX(), to.getNode().getY());

		draw(canvas, profile, path);
		selection(canvas, profile, path);
		flag(canvas, profile, path);

	}

	private void draw(FireworksCanvas canvas, FireworksColorProfile profile, Path2D path) {
		final Color initial = profile.getEdge().getInitial();
		canvas.getEdges().add(path, initial, DEFAULT_STROKE);
	}

	private void selection(FireworksCanvas canvas, FireworksColorProfile profile, Path2D path) {
		if (isSelected())
			canvas.getEdgeSelection().add(path, profile.getEdge().getSelection(), SELECTION_STROKE);
	}

	private void flag(FireworksCanvas canvas, FireworksColorProfile profile, Path2D path) {
		if (isFlag())
			canvas.getEdgeFlags().add(path, profile.getEdge().getFlag(), FLAG_STROKE);
	}
}
