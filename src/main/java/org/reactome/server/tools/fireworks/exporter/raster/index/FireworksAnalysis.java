package org.reactome.server.tools.fireworks.exporter.raster.index;

import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;
import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksNode;
import org.reactome.server.tools.fireworks.exporter.api.FireworkArgs;
import org.reactome.server.tools.fireworks.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.fireworks.exporter.common.analysis.model.AnalysisResult;
import org.reactome.server.tools.fireworks.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.fireworks.exporter.common.analysis.model.PathwaySummary;
import org.reactome.server.tools.fireworks.exporter.raster.layers.FireworksCanvas;

import java.util.Collection;
import java.util.stream.Collectors;

public class FireworksAnalysis {
	private AnalysisType type;
	private PathwaySummary[] pathwaySummary;
	private AnalysisResult result;
	private FireworksIndex index;
	private FireworksGraph layout;
	private FireworkArgs args;

	FireworksAnalysis(FireworksIndex index, FireworksGraph layout, FireworkArgs args) throws AnalysisServerError, AnalysisException {
		if (args.getToken() == null) return;
		this.index = index;
		this.layout = layout;
		this.args = args;
		this.result = AnalysisClient.getAnalysisResult(args.getToken());
		this.type = AnalysisType.getType(result.getSummary().getType());
		analyse();
	}

	private void analyse() throws AnalysisException, AnalysisServerError {
		final Collection<String> pathways = layout.getNodes().stream()
				.map(FireworksNode::getStId)
				.collect(Collectors.toSet());
		final String resource = args.getResource() == null
				? result.getResourceSummary().size() == 2
				? result.getResourceSummary().get(1).getResource()
				: result.getResourceSummary().get(0).getResource()
				: args.getResource();
		this.pathwaySummary = AnalysisClient.getPathwaysSummary(pathways, args.getToken(), resource);
		if (type == AnalysisType.EXPRESSION) {
			expression();
		} else if (type == AnalysisType.OVERREPRESENTATION
				|| type == AnalysisType.SPECIES_COMPARISON)
			enrichment();
	}

	private void enrichment() {
		for (PathwaySummary summary : pathwaySummary) {
			final Node node = index.getNode(summary.getDbId());
			if (node == null) continue;
			node.setpValue(summary.getEntities().getpValue());
		}
	}

	private void expression() {
		for (PathwaySummary summary : pathwaySummary) {
			final Node node = index.getNode(summary.getDbId());
			if (node == null) continue;
			node.setExp(summary.getEntities().getExp());
			node.setpValue(summary.getEntities().getpValue());
		}

	}

	public void addLegend(FireworksCanvas canvas) {

	}

	public AnalysisResult getResult() {
		return result;
	}

	public AnalysisType getType() {
		return type;
	}
}
