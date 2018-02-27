package org.reactome.server.tools.fireworks.exporter;

import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.exception.ResourceGoneException;
import org.reactome.server.analysis.core.result.exception.ResourceNotFoundException;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;
import org.reactome.server.tools.fireworks.exporter.common.ResourcesFactory;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;
import org.reactome.server.tools.fireworks.exporter.raster.FireworksRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

public class FireworksExporter {

	private static final Logger logger = LoggerFactory.getLogger("infoLogger");

	private final String fireworkPath;
	private final TokenUtils tokenUtils;

	public FireworksExporter(String fireworkPath, String analysisPath) {
		this.fireworkPath = fireworkPath;
		this.tokenUtils = new TokenUtils(analysisPath);
		loadFonts();
	}

	private void loadFonts() {
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("fonts/arial.ttf")));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("fonts/arialbd.ttf")));
		} catch (FontFormatException | IOException e) {
			// resources shouldn't throw exceptions
			logger.error("Couldn't load font", e);
		}
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
}
