package org.reactome.server.tools.fireworks.exporter.raster.index;

import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;
import org.reactome.server.tools.fireworks.exporter.api.FireworkArgs;
import org.reactome.server.tools.fireworks.exporter.profiles.FireworksColorProfile;

import java.util.*;

public class FireworksIndex {

	private final Map<Long, Node> index = new TreeMap<>();
	private final FireworksGraph layout;
	private final FireworksColorProfile profile;
	private final FireworkArgs args;
	private Collection<Long> selected = new HashSet<>();
	private List<Node> nodes = new LinkedList<>();
	private List<Edge> edges = new LinkedList<>();

	public FireworksIndex(FireworksGraph layout, FireworksColorProfile profile, FireworkArgs args) {
		this.layout = layout;
		this.profile = profile;
		this.args = args;
		selected = getSelected(args.getSelected());
		index();
	}

	private Collection<Long> getSelected(Collection<String> origin) {
		if (origin == null) return Collections.emptyList();
		final Set<Long> sel = new HashSet<>();
		final Map<String, Long> stIdMap = new TreeMap<>();
		layout.getNodes().forEach(node -> stIdMap.put(node.getStId(), node.getDbId()));
		args.getSelected().forEach(word -> {
			try {
				final Long dbId = Long.valueOf(word);
				sel.add(dbId);
			} catch (NumberFormatException ignored) {
			}
			if (stIdMap.containsKey(word))
				sel.add(stIdMap.get(word));
		});
		return sel;
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
		selected.forEach(id -> index.get(id).setSelected(true));
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public List<Edge> getEdges() {
		return edges;
	}
}
