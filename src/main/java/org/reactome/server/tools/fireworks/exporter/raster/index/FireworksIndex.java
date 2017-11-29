package org.reactome.server.tools.fireworks.exporter.raster.index;

import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;
import org.reactome.server.tools.fireworks.exporter.api.FireworkArgs;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.fireworks.exporter.profiles.FireworksColorProfile;

import java.util.*;

public class FireworksIndex {

	private final Map<Long, Node> index = new TreeMap<>();
	private final FireworksGraph layout;
	private final FireworksColorProfile profile;
	private final FireworkArgs args;
	private final FireworksDecorator decorator;
	private final FireworksAnalysis analysis;
	private List<Node> nodes = new LinkedList<>();
	private List<Edge> edges = new LinkedList<>();

	public FireworksIndex(FireworksGraph layout, FireworksColorProfile profile, FireworkArgs args) throws AnalysisServerError, AnalysisException {
		this.layout = layout;
		this.profile = profile;
		this.args = args;
		index();
		this.decorator = new FireworksDecorator(this, layout, args);
		this.analysis = new FireworksAnalysis(this, layout, args);
	}

	private void index() {
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

	public Node getNode(Long id) {
		return index.get(id);
	}
	public List<Node> getNodes() {
		return nodes;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public Set<Long> keySet() {
		return index.keySet();
	}

	public FireworksDecorator getDecorator() {
		return decorator;
	}

	public FireworksAnalysis getAnalysis() {
		return analysis;
	}

}
