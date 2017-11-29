package org.reactome.server.tools.fireworks.exporter.raster.index;

import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksNode;
import org.reactome.server.tools.fireworks.exporter.profiles.FireworksColorProfile;
import org.reactome.server.tools.fireworks.exporter.raster.layers.FireworksCanvas;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

public class Node extends FireworksElement {

	private static final double MIN_NODE_SIZE = 0.025;
	private static final int NODE_FACTOR = 18;
	private static final Stroke SELECTION_STROKE = new BasicStroke(0.3f);
	private static final Stroke FLAG_STROKE = new BasicStroke(0.7f);
	private FireworksNode node;

	private Set<Edge> from;
	private Set<Edge> to;

	Node(FireworksNode node) {
		this.node = node;
	}

	Edge addChild(Node child) {
		if (this == child) return null;
		final Edge edge = new Edge(this, child);
		if (to == null) to = new HashSet<>();
		to.add(edge);
		if (child.from == null)
			child.from = new HashSet<>();
		child.from.add(edge);
		return edge;
	}

	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		// Fire parents selection
		if (from != null)
			from.forEach(edge -> edge.setSelected(selected));
	}

	@Override
	public void setFlag(boolean flag) {
		super.setFlag(flag);
		if (from != null)
			from.forEach(edge -> edge.setFlag(flag));
	}

	public FireworksNode getNode() {
		return node;
	}

	public void render(FireworksCanvas canvas, FireworksColorProfile profile) {
		final double diameter = (node.getRatio() + MIN_NODE_SIZE) * NODE_FACTOR;
		final double x = node.getX() - diameter * 0.5;
		final double y = node.getY() - diameter * 0.5;
		final Shape ellipse = new Ellipse2D.Double(x, y, diameter, diameter);

		draw(canvas, profile, ellipse);
		selection(canvas, profile, ellipse);
		flag(canvas, profile, ellipse);
		text(canvas);
	}

	private void draw(FireworksCanvas canvas, FireworksColorProfile profile, Shape ellipse) {
		canvas.getNodes().add(ellipse, profile.getNode().getInitial());
	}

	private void selection(FireworksCanvas canvas, FireworksColorProfile profile, Shape ellipse) {
		if (isSelected())
			canvas.getNodeSelection().add(ellipse, profile.getNode().getSelection(), SELECTION_STROKE);
	}

	private void flag(FireworksCanvas canvas, FireworksColorProfile profile, Shape ellipse) {
		if (isFlag())
			canvas.getNodeFlags().add(ellipse, profile.getNode().getFlag(), FLAG_STROKE);
	}

	private void text(FireworksCanvas canvas) {
		if (from == null)
			canvas.getText().add(node.getName(), new Point2D.Double(node.getX(), node.getY()));
	}
}
