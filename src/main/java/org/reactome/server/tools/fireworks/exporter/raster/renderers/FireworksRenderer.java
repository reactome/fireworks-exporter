package org.reactome.server.tools.fireworks.exporter.raster.renderers;

import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;
import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksNode;
import org.reactome.server.tools.fireworks.exporter.api.FireworkArgs;
import org.reactome.server.tools.fireworks.exporter.profiles.FireworksColorProfile;
import org.reactome.server.tools.fireworks.exporter.raster.index.FireworksIndex;
import org.reactome.server.tools.fireworks.exporter.raster.layers.FireworksCanvas;

import java.util.Map;
import java.util.TreeMap;

public class FireworksRenderer {

	private final FireworksIndex index;
	private final FireworksGraph layout;
	private final FireworksCanvas canvas;
	private final FireworksColorProfile profile;
	private final FireworkArgs args;
	private final Map<Long, FireworksNode> nodeIndex = new TreeMap<>();

	public FireworksRenderer(FireworksGraph layout, FireworksCanvas canvas, FireworksColorProfile profile, FireworkArgs args, FireworksIndex index) {
		this.layout = layout;
		this.canvas = canvas;
		this.profile = profile;
		this.args = args;
		this.index = index;
		layout.getNodes().forEach(node -> nodeIndex.put(node.getDbId(), node));
	}

	public void layout() {
		index.getNodes().forEach(node -> node.render(canvas, profile));
		index.getEdges().forEach(edge -> edge.render(canvas, profile));
	}

}
