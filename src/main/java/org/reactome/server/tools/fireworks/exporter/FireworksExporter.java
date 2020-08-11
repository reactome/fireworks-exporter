package org.reactome.server.tools.fireworks.exporter;

import com.itextpdf.layout.Document;
import org.apache.batik.transcoder.TranscoderException;
import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.exception.ResourceGoneException;
import org.reactome.server.analysis.core.result.exception.ResourceNotFoundException;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;
import org.reactome.server.tools.fireworks.exporter.common.ResourcesFactory;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;
import org.reactome.server.tools.fireworks.exporter.raster.FireworksOutput;
import org.reactome.server.tools.fireworks.exporter.raster.FireworksRenderer;
import org.w3c.dom.svg.SVGDocument;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

public class FireworksExporter {


	private final String fireworkPath;
	private final TokenUtils tokenUtils;

	public FireworksExporter(String fireworkPath, String analysisPath) {
		this.fireworkPath = fireworkPath;
		this.tokenUtils = new TokenUtils(analysisPath);
	}

	/**
	 * If result is null, then it tries to load analysis using the token.
	 */
	public BufferedImage renderRaster(FireworkArgs args, AnalysisStoredResult result) throws AnalysisServerError {
		final FireworksGraph layout = ResourcesFactory.getGraph(fireworkPath, args.getSpeciesName());
		return new FireworksRenderer(layout, args, getResult(args.getToken(), result)).render();
	}

	public void renderGif(FireworkArgs args, AnalysisStoredResult result, OutputStream os) throws AnalysisServerError {
		final FireworksGraph layout = ResourcesFactory.getGraph(fireworkPath, args.getSpeciesName());
		new FireworksRenderer(layout, args, getResult(args.getToken(), result)).renderToGif(os);
	}

	public SVGDocument renderSvg(FireworkArgs args, AnalysisStoredResult result) throws AnalysisServerError {
		final FireworksGraph layout = ResourcesFactory.getGraph(fireworkPath, args.getSpeciesName());
		return new FireworksRenderer(layout, args, getResult(args.getToken(), result)).renderToSvg();
	}

	public Document renderPdf(FireworkArgs args) throws IOException, AnalysisServerError {
		return renderPdf(args, null);
	}

	public Document renderPdf(FireworkArgs args, AnalysisStoredResult result) throws AnalysisServerError, IOException {
		final FireworksGraph layout = ResourcesFactory.getGraph(fireworkPath, args.getSpeciesName());
		return new FireworksRenderer(layout, args, getResult(args.getToken(), result)).renderToPdf();
	}

	public void render(FireworkArgs args, AnalysisStoredResult result, OutputStream os) throws AnalysisServerError, TranscoderException, IOException {
		final FireworksGraph layout = ResourcesFactory.getGraph(fireworkPath, args.getSpeciesName());
		final FireworksRenderer renderer = new FireworksRenderer(layout, args, getResult(args.getToken(), result));
		final AnalysisType type = renderer.getResult() == null ? null : AnalysisType.valueOf(renderer.getResult().getSummary().getType());
		if (args.getFormat().equalsIgnoreCase("gif")
				&& args.getColumn() == null
				&& (type == AnalysisType.EXPRESSION || type == AnalysisType.GSVA || type == AnalysisType.GSA_STATISTICS || type == AnalysisType.GSA_REGULATION))
			renderer.renderToGif(os);
		else if (args.getFormat().equalsIgnoreCase("svg"))
			FireworksOutput.save(renderer.renderToSvg(), os);
		else if (args.getFormat().equalsIgnoreCase("pdf"))
			FireworksOutput.save(renderer.renderToPdf(), os);
		else FireworksOutput.save(renderer.render(), args.getFormat(), os);
	}

	private AnalysisStoredResult getResult(String token, AnalysisStoredResult result) throws AnalysisServerError {
		if (result != null) return result;
		if (token == null) return null;
		try {
			return tokenUtils.getFromToken(token);
		} catch (ResourceGoneException e) {
			throw new AnalysisServerError("Token has expired: " + token);
		} catch (ResourceNotFoundException e) {
			throw new AnalysisServerError("Token not valid: " + token);
		}
	}

	public void render(FireworkArgs args, OutputStream os) throws IOException, AnalysisServerError, TranscoderException {
		render(args, null, os);
	}
}
