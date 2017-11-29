package org.reactome.server.tools.fireworks.exporter.common.profiles;

public class FireworksColorProfile {

	private NodeColorProfile node;
	private NodeColorProfile edge;
	private ThumbnailColorProfile thumbnail;
	private String name;

	public String getName() {
		return name;
	}

	public NodeColorProfile getEdge() {
		return edge;
	}

	public NodeColorProfile getNode() {
		return node;
	}

	public ThumbnailColorProfile getThumbnail() {
		return thumbnail;
	}

}
