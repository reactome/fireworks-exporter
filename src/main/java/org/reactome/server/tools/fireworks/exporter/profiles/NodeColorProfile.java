package org.reactome.server.tools.fireworks.exporter.profiles;

import java.awt.*;

public class NodeColorProfile extends CommonColorProfile {
	private Color hit;
	private GradientColorProfile enrichment;
	private GradientColorProfile expression;

	public Color getHit() {
		return hit;
	}

	public void setHit(String color) {
		this.hit = ColorFactory.parseColor(color);
	}

	public GradientColorProfile getEnrichment() {
		return enrichment;
	}

	public GradientColorProfile getExpression() {
		return expression;
	}
}
