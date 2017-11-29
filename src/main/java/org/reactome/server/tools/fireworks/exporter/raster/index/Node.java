package org.reactome.server.tools.fireworks.exporter.raster.index;

import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Encapsulates a Node and adds data for rendering.
 */
public class Node extends FireworksElement {

	private final FireworksNode fireworksNode;

	private Set<Edge> parents;
	private List<Double> exp;

	Node(FireworksNode fireworksNode) {
		this.fireworksNode = fireworksNode;
	}

	Edge addChild(Node child) {
		if (this == child) return null;
		final Edge edge = new Edge(this, child);
//		if (to == null) to = new HashSet<>();
//		to.add(edge);
		if (child.parents == null)
			child.parents = new HashSet<>();
		child.parents.add(edge);
		return edge;
	}

	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		// Fire parents selection
		if (parents != null)
			parents.forEach(edge -> edge.setSelected(selected));
	}

	@Override
	public void setFlag(boolean flag) {
		super.setFlag(flag);
		if (parents != null)
			parents.forEach(edge -> edge.setFlag(flag));
	}

	public FireworksNode getFireworksNode() {
		return fireworksNode;
	}

	public void setpValue(Double pValue) {
		super.setpValue(pValue);
		if (parents != null)
			parents.forEach(edge -> edge.setpValue(pValue));
	}

	public List<Double> getExp() {
		return exp;
	}

	void setExp(List<Double> exp) {
		this.exp = exp;
	}

	public boolean isTopLevel() {
		return parents != null;
	}
}
