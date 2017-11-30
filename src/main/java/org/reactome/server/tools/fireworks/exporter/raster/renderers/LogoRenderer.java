package org.reactome.server.tools.fireworks.exporter.raster.renderers;

import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.fireworks.exporter.common.ResourcesFactory;
import org.reactome.server.tools.fireworks.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.fireworks.exporter.raster.index.FireworksAnalysis;
import org.reactome.server.tools.fireworks.exporter.raster.layers.FireworksCanvas;
import org.reactome.server.tools.fireworks.exporter.raster.properties.FontProperties;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class LogoRenderer {
	private static final double RELATIVE_LOGO_WIDTH = 0.1;
	private static final double MIN_LOGO_WIDTH = 50;
	private static final double PADDING = 15;

	private double logo_width;
	private double logo_height;
	private Rectangle2D.Double infoBox;

	public void addLogo(FireworksCanvas canvas) {
		try {
			final Rectangle2D bounds = canvas.getBounds();
			final BufferedImage logo = ResourcesFactory.getLogo();
			logo_width = bounds.getWidth() * RELATIVE_LOGO_WIDTH;
			if (logo_width > logo.getWidth()) logo_width = logo.getWidth();
			if (logo_width < MIN_LOGO_WIDTH) logo_width = MIN_LOGO_WIDTH;
			logo_height = logo_width / logo.getWidth() * logo.getHeight();

			final NodeProperties limits = NodePropertiesFactory.get(
					bounds.getMaxX() - logo_width,
					bounds.getMaxY() + PADDING,
					logo_width, logo_height);
			canvas.getLogoLayer().add(logo, limits);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createInfoBox(FireworksCanvas canvas) {
		final Rectangle2D bounds = canvas.getBounds();
		infoBox = new Rectangle2D.Double(0, bounds.getMaxY() - logo_height,
				bounds.getWidth() - logo_width, logo_height);
	}


	public void infoText(FireworksCanvas canvas, FireworksAnalysis analysis, String title, int col) {
		if (infoBox == null) createInfoBox(canvas);
		String text = "";
		if (title != null)
			text = title + " ";
		if (analysis.getType() == AnalysisType.EXPRESSION) {
			text += String.format("[%s] %d/%d %s",
					analysis.getResult().getSummary().getSampleName(),
					col + 1,
					analysis.getResult().getExpression().getColumnNames().size(),
					analysis.getResult().getExpression().getColumnNames().get(col));
		} else
			text += analysis.getResult().getSummary().getSampleName();
		canvas.getInfoText().clear();
		canvas.getInfoText().add(text, Color.BLACK, infoBox, FontProperties.DEFAULT_FONT);
	}

	public void writeTitle(FireworksCanvas canvas, String title) {
		if (title == null) return;
		if (infoBox == null) createInfoBox(canvas);
		canvas.getInfoText().add(title, Color.BLACK, infoBox, FontProperties.DEFAULT_FONT);
	}

}
