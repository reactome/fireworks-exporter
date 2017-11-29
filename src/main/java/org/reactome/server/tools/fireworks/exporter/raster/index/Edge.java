package org.reactome.server.tools.fireworks.exporter.raster.index;

import org.reactome.server.tools.fireworks.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.fireworks.exporter.common.profiles.ColorFactory;
import org.reactome.server.tools.fireworks.exporter.common.profiles.FireworksColorProfile;
import org.reactome.server.tools.fireworks.exporter.raster.layers.FireworksCanvas;

import java.awt.*;
import java.awt.geom.Path2D;

import static org.reactome.server.tools.fireworks.exporter.raster.index.Node.P_VALUE_THRESHOLD;

public class Edge extends FireworksElement {

	private static final Stroke DEFAULT_STROKE = new BasicStroke(0.2f);
	private static final Stroke SELECTION_STROKE = new BasicStroke(0.4f);
	private static final Stroke FLAG_STROKE = new BasicStroke(0.6f);

	private final Node from;
	private final Node to;

	Edge(Node from, Node to) {
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

	public Node getTo() {
		return to;
	}

	public void render(FireworksCanvas canvas, FireworksColorProfile profile, FireworksIndex index) {
		final Path2D path = new Path2D.Double();
		path.moveTo(from.getNode().getX(), from.getNode().getY());

		double dX = to.getNode().getX() - from.getNode().getX();
		double dY = to.getNode().getY() - from.getNode().getY();
		double angle = Math.atan2(dY, dX) - (Math.PI / 6);
		double r = Math.sqrt(dX * dX + dY * dY) * 3 / 5.0;
		double x = from.getNode().getX() + r * Math.cos(angle);
		double y = from.getNode().getY() + r * Math.sin(angle);

		path.quadTo(x, y, to.getNode().getX(), to.getNode().getY());

		draw(canvas, profile, path, index);
		selection(canvas, profile, path);
		flag(canvas, profile, path);

	}

	private void draw(FireworksCanvas canvas, FireworksColorProfile profile, Path2D path, FireworksIndex index) {
		final Color color = getEdgeColor(profile, index);
		canvas.getEdges().add(path, color, DEFAULT_STROKE);
	}

	private Color getEdgeColor(FireworksColorProfile profile, FireworksIndex index) {
		if (index.getAnalysis().getResult() == null)
			return profile.getEdge().getInitial();
		if (index.getAnalysis().getType() == AnalysisType.EXPRESSION) {
			if (to.getExp() != null) {
				if (getpValue() <= P_VALUE_THRESHOLD) {
					final double min = index.getAnalysis().getResult().getExpression().getMin();
					final double max = index.getAnalysis().getResult().getExpression().getMax();
					final double val = 1 - (to.getExp().get(0) - min) / (max - min);
					return ColorFactory.interpolate(profile.getEdge().getExpression(), val);
				} else return profile.getEdge().getHit();
			}
		} else if (index.getAnalysis().getType() == AnalysisType.OVERREPRESENTATION
				|| index.getAnalysis().getType() == AnalysisType.SPECIES_COMPARISON) {
			if (getpValue() != null && getpValue() <= P_VALUE_THRESHOLD) {
				final double val = getpValue() / P_VALUE_THRESHOLD;
				return ColorFactory.interpolate(profile.getEdge().getEnrichment(), val);
			}
		}
		return profile.getEdge().getFadeout();
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
