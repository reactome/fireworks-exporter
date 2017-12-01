package org.reactome.server.tools.fireworks.exporter.raster.renderers;

import org.reactome.server.tools.fireworks.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;
import org.reactome.server.tools.fireworks.exporter.common.profiles.FireworksColorProfile;
import org.reactome.server.tools.fireworks.exporter.raster.index.FireworksIndex;
import org.reactome.server.tools.fireworks.exporter.raster.layers.FireworksCanvas;

public class FireworksRenderer {

	private final FireworksIndex index;
	private final FireworksCanvas canvas;
	private final FireworksColorProfile profile;
	private final LogoRenderer logoRenderer = new LogoRenderer();

	/**
	 * Creates a FireworksRenderer that renders index nodes and edges into
	 * canvas using profile. Later on, you can use {@link
	 * FireworksRenderer#setCol(int, String)} to create GIFs.
	 */
	public FireworksRenderer(FireworksCanvas canvas, FireworksColorProfile profile, FireworksIndex index) {
		this.canvas = canvas;
		this.profile = profile;
		this.index = index;
		layout();
	}

	private void layout() {
		final NodeRenderer nodeRenderer = new NodeRenderer(profile, index, canvas);
		index.getNodes().forEach(nodeRenderer::render);
		final EdgeRenderer edgeRenderer = new EdgeRenderer(profile, index, canvas);
		index.getEdges().forEach(edgeRenderer::render);
		if (index.getAnalysis().getResult() != null) {
			if (index.getAnalysis().getType() == AnalysisType.EXPRESSION)
				index.getAnalysis().addLegend(canvas, profile);
		}
		logoRenderer.addLogo(canvas);
	}

	public void setCol(int col, String title) {
		logoRenderer.infoText(canvas, index.getAnalysis(), title, col);
		index.getAnalysis().setCol(canvas, profile, col);
	}

	public void writeTitle(String title) {
		logoRenderer.writeTitle(canvas, title);
	}
}
