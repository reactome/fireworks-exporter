package org.reactome.server.tools.fireworks.exporter.raster.index;

import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;
import org.reactome.server.tools.fireworks.exporter.api.FireworkArgs;

import java.util.*;

public class FireworksDecorator {

	FireworksDecorator(FireworksIndex index, FireworksGraph layout, FireworkArgs args) {
		Collection<Long> selected = getSelected(layout, args.getSelected());
		Collection<Long> flags = getFlags(layout, args.getFlags());
		selected.forEach(id -> index.getNode(id).setSelected(true));
		// TODO: why im receiving ids that are not in fireworks
		flags.retainAll(index.keySet());
		flags.forEach(id -> index.getNode(id).setFlag(true));

	}

	private Collection<Long> getSelected(FireworksGraph layout, Collection<String> origin) {
		if (origin == null) return Collections.emptyList();
		final Set<Long> sel = new HashSet<>();
		final Map<String, Long> stIdMap = new TreeMap<>();
		layout.getNodes().forEach(node -> stIdMap.put(node.getStId(), node.getDbId()));
		origin.forEach(word -> {
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

	private Collection<Long> getFlags(FireworksGraph layout, Collection<String> flags) {
		if (flags == null) return Collections.emptyList();
		final Set<Long> pathwaysHit = new TreeSet<>();
		flags.forEach(s -> {
			final Set<Long> responses = ContentServiceClient.getFlagged(s, layout.getSpeciesId());
			if (responses == null) return;
			pathwaysHit.addAll(responses);
		});
		return pathwaysHit;

	}

}
