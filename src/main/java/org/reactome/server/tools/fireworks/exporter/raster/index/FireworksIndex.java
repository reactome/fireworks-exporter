package org.reactome.server.tools.fireworks.exporter.raster.index;

import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;

import java.util.*;

public class FireworksIndex {

	private final Map<Long, Node> index = new TreeMap<>();
	private final FireworksDecorator decorator;
	private final FireworksAnalysis analysis;
	private final List<Node> nodes = new LinkedList<>();
	private final List<Edge> edges = new LinkedList<>();

	public FireworksIndex(FireworksGraph layout, FireworkArgs args, AnalysisStoredResult result) {
		index(layout);
		this.decorator = new FireworksDecorator(this, layout, args);
		this.analysis = new FireworksAnalysis(this, layout, args, result);
	}

	private void index(FireworksGraph layout) {
		layout.getNodes().forEach(fireworksNode -> {
			final Node node = new Node(fireworksNode);
			index.put(fireworksNode.getDbId(), node);
			nodes.add(node);
		});
		layout.getEdges().forEach(fireworksEdge -> {
			final Node from = index.get(fireworksEdge.getFrom());
			final Node to = index.get(fireworksEdge.getTo());
			final Edge edge = from.addChild(to);
			edges.add(edge);
		});
	}

	Node getNode(Long id) {
		return index.get(id);
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	Set<Long> keySet() {
		return index.keySet();
	}

	public FireworksDecorator getDecorator() {
		return decorator;
	}

	public FireworksAnalysis getAnalysis() {
		return analysis;
	}

}
