package org.reactome.server.tools.fireworks.exporter.raster.index;

import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;

import java.util.*;

public class FireworksDecorator {

	private final Collection<Long> selected;
	private final Map<String, Long> stIdMap;

	FireworksDecorator(FireworksIndex index, FireworksGraph graph, FireworkArgs args) {
		stIdMap = new TreeMap<>();
		graph.getNodes().forEach(node -> stIdMap.put(node.getStId(), node.getDbId()));
		selected = getIds(args.getSelected());
		Collection<Long> flags = getIds(args.getFlags());
		selected.forEach(id -> index.getNode(id).setSelected(true));
		// TODO: why im receiving ids that are not in fireworks
		flags.retainAll(index.keySet());
		flags.forEach(id -> index.getNode(id).setFlag(true));

	}

	private Collection<Long> getIds(Collection<String> input) {
		if (input == null) return Collections.emptyList();
		final Set<Long> sel = new HashSet<>();
		input.forEach(word -> {
			try {
				final Long dbId = Long.valueOf(word);
				sel.add(dbId);
			} catch (NumberFormatException ignored) {
			}
			final Long id = stIdMap.get(word);
			if (id != null) sel.add(id);
		});
		return sel;
	}

	public Collection<Long> getSelected() {
		return selected;
	}
}
