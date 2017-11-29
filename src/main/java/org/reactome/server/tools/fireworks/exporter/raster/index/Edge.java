package org.reactome.server.tools.fireworks.exporter.raster.index;

/**
 * Contains and edge of the fireworks. It points directly to the from and to
 * Nodes.
 */
public class Edge extends FireworksElement {

	private final Node from;
	private final Node to;

	Edge(Node from, Node to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		from.setSelected(selected);
	}

	@Override
	public void setFlag(boolean flag) {
		super.setFlag(flag);
		from.setFlag(true);
	}

	public Node getTo() {
		return to;
	}

	public Node getFrom() {
		return from;
	}
}
