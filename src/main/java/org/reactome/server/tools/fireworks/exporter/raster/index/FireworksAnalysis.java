package org.reactome.server.tools.fireworks.exporter.raster.index;

import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.analysis.core.model.PathwayNodeData;
import org.reactome.server.analysis.core.model.resource.MainResource;
import org.reactome.server.analysis.core.model.resource.ResourceFactory;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.PathwayNodeSummary;
import org.reactome.server.analysis.core.result.model.PathwaySummary;
import org.reactome.server.analysis.core.result.model.SpeciesFilteredResult;
import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;
import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksNode;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;
import org.reactome.server.tools.fireworks.exporter.common.profiles.FireworksColorProfile;
import org.reactome.server.tools.fireworks.exporter.common.profiles.GradientColorProfile;
import org.reactome.server.tools.fireworks.exporter.raster.layers.FireworksCanvas;
import org.reactome.server.tools.fireworks.exporter.raster.layers.RegulationBar;
import org.reactome.server.tools.fireworks.exporter.raster.properties.FontProperties;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
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
	private static final int ARROW_SIZE = 5;

	private static final DecimalFormat EXPRESSION_FORMAT = new DecimalFormat("#.##E0", DecimalFormatSymbols.getInstance(Locale.UK));
	private static final DecimalFormat ENRICHMENT_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.UK));


	private AnalysisType type;
	private List<PathwaySummary> pathwaySummary;
	private AnalysisStoredResult result;
	private FireworksIndex index;
	private FireworksGraph layout;
	private FireworkArgs args;
	private Rectangle2D.Double colorBar;
	private MainResource resource;
	private RegulationBar regulationBars;

	FireworksAnalysis(FireworksIndex index, FireworksGraph layout, FireworkArgs args, AnalysisStoredResult result) {
		this.index = index;
		this.layout = layout;
		this.args = args;
		this.result = result;
		this.type = result == null ? null : AnalysisType.getType(result.getSummary().getType());
		analyse();
	}

	private void analyse() {
		if (result == null) return;
		final List<String> pathways = layout.getNodes().stream()
				.map(FireworksNode::getStId)
				.distinct()
				.collect(Collectors.toList());
		final String resource = args.getResource() == null
				? result.getResourceSummary().size() == 2
				? result.getResourceSummary().get(1).getResource()
				: result.getResourceSummary().get(0).getResource()
				: args.getResource();
		this.resource = ResourceFactory.getMainResource(resource);

//		this.pathwaySummary = AnalysisClient.getPathwaysSummary(pathways, args.getToken(), resource);
		this.pathwaySummary = result.filterByPathways(pathways, resource);
		if (type == AnalysisType.EXPRESSION) {
			expression();
		} else if (type == AnalysisType.OVERREPRESENTATION
				|| type == AnalysisType.SPECIES_COMPARISON) {
			enrichment();
		} else if (type == AnalysisType.GSA_REGULATION
				|| type == AnalysisType.GSA_STATISTICS
				|| type == AnalysisType.GSVA) {
			gsa();
		}
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

	private void gsa() {
		expression();
	}

	public void addLegend(FireworksCanvas canvas, FireworksColorProfile profile) {
		final Rectangle2D bounds = canvas.getBounds();
		addBackground(canvas, bounds);
		addGradient(canvas, bounds, profile);
		addLabels(canvas);
	}

	private void addGradient(FireworksCanvas canvas, Rectangle2D bounds, FireworksColorProfile profile) {
		GradientColorProfile gradient = (index.getAnalysis().getType() == AnalysisType.EXPRESSION
									  || index.getAnalysis().getType() == AnalysisType.GSA_REGULATION
									  || index.getAnalysis().getType() == AnalysisType.GSA_STATISTICS
									  || index.getAnalysis().getType() == AnalysisType.GSVA)
				? profile.getNode().getExpression()
				: profile.getNode().getEnrichment();

		double colorBarHeight = LEGEND_HEIGHT - 2 * (BG_PADDING + TEXT_PADDING + FontProperties.DEFAULT_FONT.getSize());
		colorBar = new Rectangle2D.Double(
				bounds.getMaxX() + LEGEND_TO_DIAGRAM_SPACE + BG_PADDING,
				bounds.getCenterY() - 0.5 * colorBarHeight,
				LEGEND_WIDTH - 2 * BG_PADDING,
				colorBarHeight);

		if (index.getAnalysis().getType() == AnalysisType.GSA_REGULATION
				|| index.getAnalysis().getType() == AnalysisType.GSA_STATISTICS
				|| index.getAnalysis().getType() == AnalysisType.GSVA) {

			regulationBars = new RegulationBar(gradient, colorBar.getX(), colorBar.getY(), colorBar.getWidth(), colorBar.getHeight());
			regulationBars.getShapes().forEach((k,v) -> {
				canvas.getLegendBar().add(v, regulationBars.getColorMap().get(k));
			});
			canvas.getLegendBarLabels().add(regulationBars.getSymbols());

		} else {
			final Paint paint;
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
		if (args.getCoverage()) {
			topText = "0%";
			bottomText = "100%";
		} else if (index.getAnalysis().getType() == AnalysisType.EXPRESSION) {
			String resource = args.getResource() == null ? "TOTAL" : args.getResource();
			SpeciesFilteredResult sfr = result.filterBySpecies(layout.getSpeciesId(), resource);
			topText = EXPRESSION_FORMAT.format(sfr.getExpressionSummary().getMax());
			bottomText = EXPRESSION_FORMAT.format(sfr.getExpressionSummary().getMin());
		} else if (index.getAnalysis().getType() == AnalysisType.GSVA
					|| index.getAnalysis().getType() == AnalysisType.GSA_REGULATION
		    		|| index.getAnalysis().getType() == AnalysisType.GSA_STATISTICS) {
			topText = "Up-regulated";
			bottomText = "Down-regulated";
		} else {
			topText = ENRICHMENT_FORMAT.format(0);
			bottomText = ENRICHMENT_FORMAT.format(P_VALUE_THRESHOLD);
		}
		canvas.getLegendLabels().add(topText, Color.BLACK, top, FontProperties.DEFAULT_FONT);
		canvas.getLegendLabels().add(bottomText, Color.BLACK, bottom, FontProperties.DEFAULT_FONT);
	}


	public AnalysisStoredResult getResult() {
		return result;
	}

	public AnalysisType getType() {
		return type;
	}

	public void setCol(FireworksCanvas canvas, FireworksColorProfile profile, int col) {
		ticks(canvas, profile, col);
	}

	private void ticks(FireworksCanvas canvas, FireworksColorProfile profile, int col) {
		if (index.getDecorator().getSelected() == null || index.getDecorator().getSelected().isEmpty()) return;

		canvas.getTickArrows().clear();
		canvas.getTicks().clear();

		for (Long id : index.getDecorator().getSelected()) {
			final Node node = index.getNode(id);
			final double val;
			if (type == AnalysisType.EXPRESSION
				|| type == AnalysisType.GSVA
				|| type == AnalysisType.GSA_STATISTICS
				|| type == AnalysisType.GSA_REGULATION) {

				if (node.getExp() == null) continue;
				final double value = node.getExp().get(col);
				val = 1 - (value - result.getExpressionSummary().getMin()) /
						(result.getExpressionSummary().getMax() - result.getExpressionSummary().getMin());
			} else {
				if (node.getpValue() == null) continue;
				double value = node.getpValue();
				val = value / P_VALUE_THRESHOLD;
			}

			if (index.getAnalysis().getType() == AnalysisType.GSA_REGULATION) {
				if (node.getExp() == null) continue;
				if (node.getpValue() > P_VALUE_THRESHOLD) continue;

				// draw tick
				Shape box = regulationBars.getShapes().get(node.getExp().get(col).intValue());
				final Shape line = new Line2D.Double(colorBar.getX(), box.getBounds2D().getCenterY(), colorBar.getMaxX(), box.getBounds2D().getCenterY());
				// Notice the -1. It puts the arrow over the line
				final Shape arrow = arrow(colorBar.getMaxX() - 1, box.getBounds2D().getCenterY());
				canvas.getTicks().add(line, profile.getNode().getSelection(), TICK_STROKE);
				canvas.getTickArrows().add(arrow, profile.getNode().getSelection());

			} else {
				if (node.getExp() == null) continue;
				if (node.getpValue() > P_VALUE_THRESHOLD) continue;
				final double y = colorBar.getY() + val * colorBar.getHeight();
				final Shape line = new Line2D.Double(colorBar.getX(), y, colorBar.getMaxX(), y);
				canvas.getTicks().add(line, profile.getNode().getSelection(), TICK_STROKE);
				// Notice the -1. It puts the arrow over the line
				final Shape arrow = arrow(colorBar.getMaxX() - 1, y);
				canvas.getTickArrows().add(arrow, profile.getNode().getSelection());
			}
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

	public MainResource getResource() {
		return resource;
	}

	public Double getCoverage(Node node) {
		final PathwayNodeSummary pathway = index.getAnalysis().getResult().getPathway(node.getFireworksNode().getStId());
		if (pathway == null) return null;
		final PathwayNodeData data = pathway.getData();
		final MainResource mr = index.getAnalysis().getResource();
		double p;
		if (mr == null) {
			p = data.getEntitiesFound() / (double) data.getEntitiesCount();
		} else {
			p = data.getEntitiesFound(mr) / (double) data.getEntitiesCount(mr);
		}
		return p;
	}
}
