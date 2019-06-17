package org.reactome.server.tools.fireworks.exporter.raster.index;

import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;

import java.util.*;

public class FireworksIndex {

	private final Map<Long, Node> index = new TreeMap<>();
	private final FireworksDecorator decorator;
	private final FireworksAnalysis analysis;
	private final FireworkArgs args;
	private final List<Node> nodes = new LinkedList<>();
	private final List<Edge> edges = new LinkedList<>();

	public FireworksIndex(FireworksGraph graph, FireworkArgs args, AnalysisStoredResult result) {
		index(graph);
		this.decorator = new FireworksDecorator(this, graph, args);
		this.analysis = new FireworksAnalysis(this, graph, args, result);
		this.args = args;
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

	public FireworkArgs getArgs() {
		return args;
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
