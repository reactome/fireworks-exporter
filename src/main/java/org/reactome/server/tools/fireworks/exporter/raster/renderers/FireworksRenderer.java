package org.reactome.server.tools.fireworks.exporter.raster.renderers;

import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;
import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksNode;
import org.reactome.server.tools.fireworks.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.fireworks.exporter.common.profiles.FireworksColorProfile;
import org.reactome.server.tools.fireworks.exporter.raster.index.FireworksIndex;
import org.reactome.server.tools.fireworks.exporter.raster.layers.FireworksCanvas;

import java.util.Map;
import java.util.TreeMap;

public class FireworksRenderer {

	private final FireworksIndex index;
	private final FireworksCanvas canvas;
	private final FireworksColorProfile profile;
	private final Map<Long, FireworksNode> nodeIndex = new TreeMap<>();

	public FireworksRenderer(FireworksGraph layout, FireworksCanvas canvas, FireworksColorProfile profile, FireworksIndex index) {
		this.canvas = canvas;
		this.profile = profile;
		this.index = index;
		layout.getNodes().forEach(node -> nodeIndex.put(node.getDbId(), node));
	}

	public void layout() {
		index.getNodes().forEach(node -> node.render(canvas, profile, index));
		index.getEdges().forEach(edge -> edge.render(canvas, profile, index));
		if (index.getAnalysis().getResult() != null) {
			if (index.getAnalysis().getType() == AnalysisType.EXPRESSION)
				index.getAnalysis().addLegend(canvas, profile);
		}
		index.getAnalysis().addLogo(canvas);
	}

	public void setCol(int col) {
		index.getAnalysis().setCol(canvas, profile, col);
	}

}
