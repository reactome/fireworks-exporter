package org.reactome.server.tools;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.tools.fireworks.exporter.FireworksExporter;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;
import org.reactome.server.tools.fireworks.exporter.raster.index.ContentServiceClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.fail;

/**
 * Unit test for simple FireworksRenderer.
 */
public class FireworksRendererTest {
	private static final String TOKEN_OVER_1 = "MjAxODAyMTIxMTI5MzdfMQ==";
	private static final String TOKEN_OVER_2 = "MjAxODAyMTIxMTMwMTRfMg==";
	private static final String TOKEN_EXPRESSION_1 = "MjAxODAyMTIxMTMwNDhfMw==";
	private static final String TOKEN_EXPRESSION_2 = "MjAxODAyMTIxMTMxMTZfNA==";
	private static final String TOKEN_SPECIES = "MjAxODAyMTIxMTMyMzdfNQ==";

	private static final String ANALYSIS_PATH = "src/test/resources/org/reactome/server/tools/fireworks/exporter/analysis";
	private static final String FIREWORK_PATH = "src/test/resources/org/reactome/server/tools/fireworks/exporter/layouts";
	private static final File IMAGE_FOLDER = new File("test-image");
	private static final boolean save = false;

	private static final TokenUtils TOKEN_UTILS = new TokenUtils(ANALYSIS_PATH);
	private static final FireworksExporter exporter = new FireworksExporter(FIREWORK_PATH, ANALYSIS_PATH);


	@BeforeClass
	public static void beforeClass() {
		ContentServiceClient.setHost("https://reactomedev.oicr.on.ca");
		createImageDir();
	}

	private static void createImageDir() {
		if (!IMAGE_FOLDER.exists() && !IMAGE_FOLDER.mkdirs())
			System.err.println("Couldn't create test dir " + IMAGE_FOLDER);
	}

	@AfterClass
	public static void afterClass() {
		if (!save) removeDir(IMAGE_FOLDER);
	}

	private static void removeDir(File dir) {
		if (!dir.exists()) return;
		final File[] files = dir.listFiles();
		if (files == null) return;
		for (File child : files)
			if (!child.delete())
				System.err.println("Couldn't delete file " + child);
		if (!dir.delete())
			System.err.println("Couldn't delete dir " + dir);
	}

	@Test
	public void testSimple() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setSelected(Arrays.asList("R-HSA-169911", "R-HSA-3560792"));
		render(args, null);
	}

	@Test
	public void testFlags() {
		final FireworkArgs args = new FireworkArgs("Canis_familiaris", "png");
		args.setFlags(Collections.singletonList("CTP"));
		args.setFactor(2.);
		args.setWriteTitle(true);
		render(args, null);
	}

	@Test
	public void testEnrichment() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setFactor(2.);
		args.setToken(TOKEN_OVER_1);
		render(args, null);
	}

	@Test
	public void testExpression() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
//		args.setFactor(2.);
		args.setSelected(Arrays.asList("R-HSA-169911", "R-HSA-3560792"));
		args.setProfile("Calcium Salts");
		args.setColumn(1);
		args.setToken(TOKEN_EXPRESSION_1);
		render(args, null);
	}

	@Test
	public void testAnimatedGif() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "gif");
		args.setSelected(Arrays.asList("R-HSA-169911", "R-HSA-3560792"));
//		args.setFlags(Arrays.asList("CTP"));
		args.setProfile("Copper plus");
		args.setWriteTitle(true);
//		args.setColumn(1);
		args.setToken(TOKEN_EXPRESSION_1);
		renderGif(args, null);
	}

	@Test
	public void testUsingResult() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		final AnalysisStoredResult result = TOKEN_UTILS.getFromToken(TOKEN_EXPRESSION_1);
		render(args, result);
	}

	@Test
	public void testTitleNoAnalysis() {
		final FireworkArgs args = new FireworkArgs("Canis_familiaris", "png");
		args.setWriteTitle(true);
		render(args, null);
	}

	@Test
	public void testTitleExpression() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setWriteTitle(true);
		render(args, TOKEN_UTILS.getFromToken(TOKEN_EXPRESSION_1));
	}

	@Test
	public void testTitleOverRepresentation() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setWriteTitle(true);
		render(args, TOKEN_UTILS.getFromToken(TOKEN_OVER_1));
	}

	@Test
	public void testTitleSpecies() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setWriteTitle(true);
		render(args, TOKEN_UTILS.getFromToken(TOKEN_SPECIES));
	}

	private void renderGif(FireworkArgs args, AnalysisStoredResult result) {
		try {
			final File file = new File(IMAGE_FOLDER, getFileName(args));
			final FileOutputStream outputStream = new FileOutputStream(file);
			exporter.renderGif(args, result, outputStream);
		} catch (IOException | AnalysisServerError e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void render(FireworkArgs args, AnalysisStoredResult result) {
		try {
			final BufferedImage image = exporter.renderRaster(args, result);
			if (save) saveToDisk(args, image);
		} catch (IOException | AnalysisServerError e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void saveToDisk(FireworkArgs args, BufferedImage image) throws IOException {
		final File file = new File(IMAGE_FOLDER, getFileName(args));
		ImageIO.write(image, args.getFormat(), file);
	}

	private String getFileName(FireworkArgs args) {
		return args.getSpeciesName() + "." + args.getFormat();
	}


}
