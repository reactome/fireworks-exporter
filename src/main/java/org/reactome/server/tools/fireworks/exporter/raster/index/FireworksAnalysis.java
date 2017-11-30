package org.reactome.server.tools.fireworks.exporter.raster.index;

import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;
import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksNode;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.fireworks.exporter.common.ResourcesFactory;
import org.reactome.server.tools.fireworks.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.fireworks.exporter.common.analysis.model.AnalysisResult;
import org.reactome.server.tools.fireworks.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.fireworks.exporter.common.analysis.model.PathwaySummary;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;
import org.reactome.server.tools.fireworks.exporter.common.profiles.FireworksColorProfile;
import org.reactome.server.tools.fireworks.exporter.common.profiles.GradientColorProfile;
import org.reactome.server.tools.fireworks.exporter.raster.layers.FireworksCanvas;
import org.reactome.server.tools.fireworks.exporter.raster.properties.FontProperties;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class FireworksAnalysis {

	public static final double P_VALUE_THRESHOLD = 0.05;
	private static final Color BACKGROUND_BORDER = new Color(175, 175, 175);
	private static final Color BACKGROUND_FILL = new Color(220, 220, 220);
	private static final Stroke BACKGROUND_STROKE = new BasicStroke(0.5f);
	private static final Stroke TICK_STROKE = new BasicStroke(0.5f);
	private static final double LEGEND_WIDTH = 40;
	private static final double LEGEND_HEIGHT = 250;
	private static final double LEGEND_TO_DIAGRAM_SPACE = 15;
	private static final double TEXT_PADDING = 2;
	private static final double BG_PADDING = 5;
	private static final double RELATIVE_LOGO_WIDTH = 0.1;
	private static final double MIN_LOGO_WIDTH = 50;
	private static final int ARROW_SIZE = 5;

	private static final DecimalFormat EXPRESSION_FORMAT = new DecimalFormat("#.##E0", DecimalFormatSymbols.getInstance(Locale.UK));
	private static final DecimalFormat ENRICHMENT_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.UK));


	private AnalysisType type;
	private PathwaySummary[] pathwaySummary;
	private AnalysisResult result;
	private FireworksIndex index;
	private FireworksGraph layout;
	private FireworkArgs args;
	private double logo_width;
	private double logo_height;
	private Rectangle2D.Double infoBox;
	private Rectangle2D.Double colorBar;

	FireworksAnalysis(FireworksIndex index, FireworksGraph layout, FireworkArgs args) throws AnalysisServerError, AnalysisException {
		if (args.getToken() == null) return;
		this.index = index;
		this.layout = layout;
		this.args = args;
		this.result = AnalysisClient.getAnalysisResult(args.getToken());
		this.type = AnalysisType.getType(result.getSummary().getType());
		analyse();
	}

	private void analyse() throws AnalysisException, AnalysisServerError {
		final Collection<String> pathways = layout.getNodes().stream()
				.map(FireworksNode::getStId)
				.collect(Collectors.toSet());
		final String resource = args.getResource() == null
				? result.getResourceSummary().size() == 2
				? result.getResourceSummary().get(1).getResource()
				: result.getResourceSummary().get(0).getResource()
				: args.getResource();
		this.pathwaySummary = AnalysisClient.getPathwaysSummary(pathways, args.getToken(), resource);
		if (type == AnalysisType.EXPRESSION) {
			expression();
		} else if (type == AnalysisType.OVERREPRESENTATION
				|| type == AnalysisType.SPECIES_COMPARISON)
			enrichment();
	}

	private void enrichment() {
		for (PathwaySummary summary : pathwaySummary) {
			final Node node = index.getNode(summary.getDbId());
			if (node == null) continue;
			node.setpValue(summary.getEntities().getpValue());
		}
	}

	private void expression() {
		for (PathwaySummary summary : pathwaySummary) {
			final Node node = index.getNode(summary.getDbId());
			if (node == null) continue;
			node.setExp(summary.getEntities().getExp());
			node.setpValue(summary.getEntities().getpValue());
		}
	}

	public void addLegend(FireworksCanvas canvas, FireworksColorProfile profile) {
		final Rectangle2D bounds = canvas.getBounds();
		addBackground(canvas, bounds);
		addGradient(canvas, bounds, profile);
		addLabels(canvas);
	}

	private void addGradient(FireworksCanvas canvas, Rectangle2D bounds, FireworksColorProfile profile) {
		GradientColorProfile gradient = index.getAnalysis().getType() == AnalysisType.EXPRESSION
				? profile.getNode().getExpression()
				: profile.getNode().getEnrichment();

		final Paint paint;
		double colorBarHeight = LEGEND_HEIGHT - 2 * (BG_PADDING + TEXT_PADDING + FontProperties.DEFAULT_FONT.getSize());
		colorBar = new Rectangle2D.Double(
				bounds.getMaxX() + LEGEND_TO_DIAGRAM_SPACE + BG_PADDING,
				bounds.getCenterY() - 0.5 * colorBarHeight,
				LEGEND_WIDTH - 2 * BG_PADDING,
				colorBarHeight);
		if (gradient.getStop() == null)
			paint = new GradientPaint(
					(float) colorBar.getX(), (float) colorBar.getMaxY(), gradient.getMax(),
					(float) colorBar.getX(), (float) colorBar.getY(), gradient.getMin());
		else {
			paint = new LinearGradientPaint(
					(float) colorBar.getX(), (float) colorBar.getMaxY(),
					(float) colorBar.getX(), (float) colorBar.getY(),
					new float[]{0, 0.5f, 1},
					new Color[]{gradient.getMax(),
							gradient.getStop(),
							gradient.getMin()});
		}
		canvas.getLegendBar().add(colorBar, paint);
	}

	private void addBackground(FireworksCanvas canvas, Rectangle2D bounds) {
		final RoundRectangle2D rectangle = new RoundRectangle2D.Double(
				bounds.getMaxX() + LEGEND_TO_DIAGRAM_SPACE,
				bounds.getCenterY() - LEGEND_HEIGHT * 0.5,
				LEGEND_WIDTH, LEGEND_HEIGHT, 20, 20);
		canvas.getLegendBackground().add(BACKGROUND_FILL, BACKGROUND_BORDER, BACKGROUND_STROKE, rectangle);
	}

	private void addLabels(FireworksCanvas canvas) {
		// We create a box to get the label centered
		float textX = (float) (colorBar.getX() - BG_PADDING);
		float textWidth = (float) (colorBar.getWidth() + 2 * BG_PADDING);
		final double textHeight = FontProperties.DEFAULT_FONT.getSize();
		final Rectangle2D top = new Rectangle2D.Double(textX,
				colorBar.getY() - textHeight - TEXT_PADDING,
				textWidth, textHeight);
		final Rectangle2D bottom = new Rectangle2D.Double(textX,
				(float) colorBar.getMaxY() + TEXT_PADDING,
				textWidth, textHeight);

		final String topText;
		final String bottomText;
		if (index.getAnalysis().getType() == AnalysisType.EXPRESSION) {
			topText = EXPRESSION_FORMAT.format(index.getAnalysis().getResult().getExpression().getMax());
			bottomText = EXPRESSION_FORMAT.format(index.getAnalysis().getResult().getExpression().getMin());
		} else {
			topText = ENRICHMENT_FORMAT.format(0);
			bottomText = ENRICHMENT_FORMAT.format(P_VALUE_THRESHOLD);
		}
		canvas.getLegendLabels().add(topText, Color.BLACK, top, FontProperties.DEFAULT_FONT);
		canvas.getLegendLabels().add(bottomText, Color.BLACK, bottom, FontProperties.DEFAULT_FONT);
	}

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
					bounds.getMaxY() + LEGEND_TO_DIAGRAM_SPACE,
					logo_width, logo_height);
			canvas.getLogoLayer().add(logo, limits);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public AnalysisResult getResult() {
		return result;
	}

	public AnalysisType getType() {
		return type;
	}

	public void setCol(FireworksCanvas canvas, FireworksColorProfile profile, int col) {
		infoText(canvas, col);
		ticks(canvas, profile, col);
	}

	private void ticks(FireworksCanvas canvas, FireworksColorProfile profile, int col) {
		if (index.getDecorator().getSelected() == null
				|| index.getDecorator().getSelected().isEmpty()) return;
		canvas.getTickArrows().clear();
		canvas.getTicks().clear();
		for (Long id : index.getDecorator().getSelected()) {
			final Node node = index.getNode(id);
			final double val;
			if (type == AnalysisType.EXPRESSION) {
				if (node.getExp() == null)
					continue;
				final double value = node.getExp().get(col);
				val = 1 - (value - result.getExpression().getMin()) /
						(result.getExpression().getMax() - result.getExpression().getMin());
			} else {
				if (node.getpValue() == null)
					continue;
				double value = node.getpValue();
				val = value / P_VALUE_THRESHOLD;
			}
			final double y = colorBar.getY() + val * colorBar.getHeight();
			final Shape line = new Line2D.Double(colorBar.getX(), y, colorBar.getMaxX(), y);
			canvas.getTicks().add(line, profile.getNode().getSelection(), TICK_STROKE);
			// Notice the -1. It puts the arrow over the line
			final Shape arrow = arrow(colorBar.getMaxX() - 1, y);
			canvas.getTickArrows().add(arrow, profile.getNode().getSelection());
		}
	}

	private Shape arrow(double x, double y) {
		final Path2D arrow = new Path2D.Double();
		arrow.moveTo(x, y);
		arrow.lineTo(x + ARROW_SIZE, y + ARROW_SIZE);
		arrow.lineTo(x + ARROW_SIZE, y - ARROW_SIZE);
		arrow.closePath();
		return arrow;
	}

	private void infoText(FireworksCanvas canvas, int col) {
		if (infoBox == null) {
			final Rectangle2D bounds = canvas.getBounds();
			infoBox = new Rectangle2D.Double(0, bounds.getMaxY() - logo_height,
					bounds.getWidth() - logo_width, logo_height);
		}
		final String text;
		if (type == AnalysisType.EXPRESSION) {
			text = String.format("[%s] %d/%d %s",
					result.getSummary().getSampleName(),
					col + 1,
					result.getExpression().getColumnNames().size(),
					result.getExpression().getColumnNames().get(col));
		} else
			text = result.getSummary().getSampleName();

		canvas.getInfoText().clear();
		canvas.getInfoText().add(text, Color.BLACK, infoBox, FontProperties.DEFAULT_FONT);
	}
}
