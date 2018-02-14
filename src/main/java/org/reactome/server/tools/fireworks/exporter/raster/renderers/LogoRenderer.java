package org.reactome.server.tools.fireworks.exporter.raster.renderers;

import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.fireworks.exporter.common.ResourcesFactory;
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
		String text = getText(analysis, title, col);
		canvas.getInfoText().clear();
		canvas.getInfoText().add(text, Color.BLACK, infoBox, FontProperties.DEFAULT_FONT);
	}

	private String getText(FireworksAnalysis analysis, String title, int col) {
			/*
		null                -> Homo sapiens
		OVERREPRESENTATION  -> Homo sapiens (Gene names from liver)
		SPECIES_COMPARISON  -> Homo sapiens (Canis familiaris)
		EXPRESSION          -> Homo sapiens (Probeset) 1/5 10h_control
		 */
		final StringBuilder text = new StringBuilder();
		if (title != null) text.append(title);
		if (analysis.getType() == null) return text.toString();
		if (analysis.getType() == AnalysisType.OVERREPRESENTATION)
			text.append(" (").append(analysis.getResult().getSummary().getSampleName()).append(")");
		else if (analysis.getType() == AnalysisType.SPECIES_COMPARISON)
			text.append(" (").append(analysis.getResult().getSummary().getSpecies()).append(")");
		else if (analysis.getType() == AnalysisType.EXPRESSION)
			text.append(" (").append(analysis.getResult().getSummary().getSampleName()).append(")")
					.append(" ").append(col + 1).append("/")
					.append(analysis.getResult().getExpressionSummary().getColumnNames().size())
			.append(" ").append(analysis.getResult().getExpressionSummary().getColumnNames().get(col));
		return text.toString();
	}

	public void writeTitle(FireworksCanvas canvas, String title) {
		if (title == null) return;
		if (infoBox == null) createInfoBox(canvas);
		canvas.getInfoText().add(title, Color.BLACK, infoBox, FontProperties.DEFAULT_FONT);
	}

}
