package org.reactome.server.tools.fireworks.exporter.raster;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.property.HorizontalAlignment;
import org.apache.batik.anim.dom.SVG12DOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;
import org.reactome.server.tools.fireworks.exporter.common.profiles.FireworksColorProfile;
import org.reactome.server.tools.fireworks.exporter.common.profiles.ProfilesFactory;
import org.reactome.server.tools.fireworks.exporter.raster.awt.PdfGraphics2D;
import org.reactome.server.tools.fireworks.exporter.raster.gif.AnimatedGifEncoder;
import org.reactome.server.tools.fireworks.exporter.raster.index.FireworksIndex;
import org.reactome.server.tools.fireworks.exporter.raster.layers.FireworksCanvas;
import org.reactome.server.tools.fireworks.exporter.raster.properties.FontProperties;
import org.reactome.server.tools.fireworks.exporter.raster.renderers.EdgeRenderer;
import org.reactome.server.tools.fireworks.exporter.raster.renderers.LogoRenderer;
import org.reactome.server.tools.fireworks.exporter.raster.renderers.NodeRenderer;
import org.reactome.server.tools.fireworks.exporter.raster.svg.GradientHandler;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FireworksRenderer {

	private static final DOMImplementation SVG_IMPL = SVG12DOMImplementation.getDOMImplementation();

	private static final int MARGIN = 15;
	private static final Set<String> TRANSPARENT_FORMATS = new HashSet<>(Collections.singletonList("png"));
	private static final Set<String> NO_TRANSPARENT_FORMATS = new HashSet<>(Arrays.asList("jpg", "jpeg", "gif"));
	private final FireworkArgs args;
	private final FireworksCanvas canvas = new FireworksCanvas();
	private final FireworksIndex index;
	private final String title;
	private final FireworksColorProfile profile;
	private final LogoRenderer logoRenderer = new LogoRenderer();
	private final AnalysisStoredResult result;

	public FireworksRenderer(FireworksGraph graph, FireworkArgs args, AnalysisStoredResult result) {
		this.args = args;
		this.result = result;
		this.profile = ProfilesFactory.getProfile(args.getProfile());
		this.index = new FireworksIndex(graph, args, result);
		title = args.getWriteTitle() != null && args.getWriteTitle()
				? args.getSpeciesName().replace("_", " ") : null;
		layout();
	}

	private void layout() {
		final NodeRenderer nodeRenderer = new NodeRenderer(profile, index, canvas);
		index.getNodes().forEach(nodeRenderer::render);
		final EdgeRenderer edgeRenderer = new EdgeRenderer(profile, index, canvas);
		index.getEdges().forEach(edgeRenderer::render);
		if (result != null)
			index.getAnalysis().addLegend(canvas, profile);
		logoRenderer.addLogo(canvas);
	}

	private void setCol(int col, String title) {
		logoRenderer.infoText(canvas, index.getAnalysis(), title, col);
		index.getAnalysis().setCol(canvas, profile, col);
	}

	private void writeTitle(String title) {
		logoRenderer.writeTitle(canvas, title);
	}

	/**
	 * Creates a {@link BufferedImage} using the specification in args.
	 */
	public BufferedImage render() {
		writeInfoText();
		final BufferedImage image = createImage();
		final Graphics2D graphics = createGraphics(image);
		canvas.render(graphics);
		return image;
	}

	public void render(Document document) {
		try {
			final byte[] image = createPdfImage();
			final Document readDoc = new Document(new PdfDocument(new PdfReader(new ByteArrayInputStream(image))));
			final PdfFormXObject object = readDoc.getPdfDocument().getFirstPage().copyAsFormXObject(document.getPdfDocument());
			final float wi = document.getPdfDocument().getLastPage().getPageSize().getWidth() - document.getLeftMargin() - document.getRightMargin();
			final float he = 0.5f * document.getPdfDocument().getLastPage().getPageSize().getHeight() - document.getTopMargin() - document.getBottomMargin();
			document.add(new com.itextpdf.layout.element.Image(object).scaleToFit(wi, he).setHorizontalAlignment(HorizontalAlignment.CENTER));
			document.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Using the current canvas, creates a PdfDocument with the image and
	 * returns its byte representation.
	 */
	private byte[] createPdfImage() {
		final Rectangle2D bounds = canvas.getBounds();
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final Document document = new Document(new PdfDocument(new PdfWriter(os)));
		document.setMargins(0, 0, 0, 0);
		final PdfPage page = document.getPdfDocument().addNewPage(new PageSize((float) bounds.getWidth() + 6, (float) bounds.getHeight() + 6));
		final PdfCanvas pdfCanvas = new PdfCanvas(page);
		final PdfGraphics2D graphics = new PdfGraphics2D(pdfCanvas, 0, 0, (float) bounds.getWidth() + 6, (float) bounds.getHeight() + 6);
		graphics.translate(3 - bounds.getX(), 3 - bounds.getY());
		graphics.setFont(FontProperties.DEFAULT_FONT);
		graphics.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		this.canvas.render(graphics);
		pdfCanvas.release();
		document.close();
		return os.toByteArray();
	}

	private void writeInfoText() {
		if (result != null) {
			if (args.getColumn() != null)
				setCol(args.getColumn(), title);
			else setCol(0, title);
		} else writeTitle(title);
	}

	/**
	 * Creates an animated GIF into outputStream.
	 *
	 * @param outputStream where to stream the GIF
	 */
	public void renderToGif(OutputStream outputStream) {
		if (index.getAnalysis().getType() != AnalysisType.EXPRESSION)
			throw new IllegalArgumentException("Animated GIF only supported for EXPRESSION analysis");
		final AnimatedGifEncoder encoder = new AnimatedGifEncoder();
		encoder.setDelay(1000);
		encoder.setRepeat(0);
		encoder.start(outputStream);
		for (int i = 0; i < result.getExpressionSummary().getColumnNames().size(); i++) {
			setCol(i, title);
			final BufferedImage image = createImage();
			final Graphics2D graphics = createGraphics(image);
			canvas.render(graphics);
			encoder.addFrame(image);
		}
		encoder.finish();
	}

	private BufferedImage createImage() {
		final double factor = args.getFactor();
		final Rectangle2D bounds = canvas.getBounds();
		final int width = (int) (factor * (2 * MARGIN + bounds.getWidth()) + 0.5);
		final int height = (int) (factor * (2 * MARGIN + bounds.getHeight()) + 0.5);
		final String ext = args.getFormat();
		if (TRANSPARENT_FORMATS.contains(ext))
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		else if (NO_TRANSPARENT_FORMATS.contains(ext))
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		else
			throw new IllegalArgumentException("Unsupported file extension " + ext);
	}

	private Graphics2D createGraphics(BufferedImage image) {
		final String ext = args.getFormat();
		final Graphics2D graphics = image.createGraphics();
		if (NO_TRANSPARENT_FORMATS.contains(ext)) {
			Color bgColor = args.getBackground() == null
					? Color.WHITE
					: args.getBackground();
			graphics.setBackground(bgColor);
			graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
		}
		// This transformation allows elements to use their own dimensions,
		// isn't it nice?
		final Rectangle2D bounds = canvas.getBounds();
		final double factor = args.getFactor();
		final double offsetX = factor * (MARGIN - bounds.getMinX());
		final double offsetY = factor * (MARGIN - bounds.getMinY());
		graphics.translate(offsetX, offsetY);
		graphics.scale(factor, factor);

		graphics.setFont(FontProperties.DEFAULT_FONT);
		graphics.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		return graphics;
	}

	public SVGDocument renderToSvg() {
		final SVGDocument document = (SVGDocument) SVG_IMPL.createDocument(SVGConstants.SVG_NAMESPACE_URI, "svg", null);
		final SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
		ctx.setExtensionHandler(new GradientHandler());
		final SVGGraphics2D graphics2D = new SVGGraphics2D(ctx, true);
		graphics2D.setFont(FontProperties.DEFAULT_FONT);
		canvas.render(graphics2D);
		// Do not know how to extract SVG doc from SVGGraphics2D, so I take the
		// root and append to my document as root
		document.removeChild(document.getRootElement());
		document.appendChild(graphics2D.getRoot());

		final Rectangle2D bounds = canvas.getBounds();
		int width = (int) ((2 * MARGIN + bounds.getWidth()) + 0.5);
		int height = (int) ((2 * MARGIN + bounds.getHeight()) + 0.5);
		int minX = (int) ((MARGIN - bounds.getMinX()) + 0.5);
		int minY = (int) ((MARGIN - bounds.getMinY()) + 0.5);

		final String viewBox = String.format("%d %d %d %d", -minX, -minY, width, height);
		document.getRootElement().setAttribute(SVGConstants.SVG_VIEW_BOX_ATTRIBUTE, viewBox);
		return document;
	}
}
