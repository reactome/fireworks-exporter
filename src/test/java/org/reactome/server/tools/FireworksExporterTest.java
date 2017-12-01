package org.reactome.server.tools;


import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reactome.server.tools.fireworks.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.fireworks.exporter.common.analysis.model.AnalysisResult;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;
import org.reactome.server.tools.fireworks.exporter.raster.FireworksExporter;
import org.reactome.server.tools.fireworks.exporter.raster.index.ContentServiceClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.fail;

/**
 * Unit test for simple FireworksExporter.
 */
public class FireworksExporterTest {
	private static final String FIREWORK_PATH = "src/test/resources/org/reactome/server/tools/fireworks/exporter/layouts";
	private static final File IMAGE_FOLDER = new File("test-image");

	private static final boolean save = false;

	@BeforeClass
	public static void beforeClass() {
		ContentServiceClient.setHost("https://reactomedev.oicr.on.ca");
		AnalysisClient.setServer("https://reactomedev.oicr.on.ca");
		// For local testing
		// AnalysisClient.setServer("http://localhost:8080");
		// AnalysisClient.setService("");
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
		render(args);
	}

	@Test
	public void testFlags() {
		final FireworkArgs args = new FireworkArgs("Canis_familiaris", "png");
		args.setFlags(Collections.singletonList("CTP"));
		args.setFactor(2.);
		args.setWriteTitle(true);
		render(args);
	}

	@Test
	public void testEnrichment() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setFactor(2.);
		try {
			args.setToken(createEnrichmentToken());
		} catch (IOException | AnalysisException | AnalysisServerError e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		render(args);
	}

	@Test
	public void testExpression() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
//		args.setFactor(2.);
		args.setSelected(Arrays.asList("R-HSA-169911", "R-HSA-3560792"));
		args.setProfile("Calcium Salts");
		args.setColumn(1);
		try {
			args.setToken(createExpressionToken());
		} catch ( IOException | AnalysisServerError | AnalysisException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		render(args);
	}

	@Test
	public void testTitle() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "gif");
		args.setFactor(2.);
		args.setWriteTitle(true);
		render(args);
	}

	@Test
	public void testAnimatedGif() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "gif");
		args.setSelected(Arrays.asList("R-HSA-169911", "R-HSA-3560792"));
//		args.setFlags(Arrays.asList("CTP"));
		args.setProfile("Copper plus");
		args.setWriteTitle(true);
//		args.setColumn(1);
		try {
			args.setToken(createExpressionToken());
			final FireworksExporter exporter = new FireworksExporter(args, FIREWORK_PATH);
			final File file = new File(IMAGE_FOLDER, args.getSpeciesName() + "." + args.getFormat());
			final FileOutputStream outputStream = new FileOutputStream(file);
			exporter.renderToGif(outputStream);
		} catch ( IOException | AnalysisServerError | AnalysisException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void render(FireworkArgs args){
		try {
			final FireworksExporter exporter = new FireworksExporter(args, FIREWORK_PATH);
			final BufferedImage image = exporter.render();
			if (save) saveToDisk(args, image);
		} catch (IOException | AnalysisServerError | AnalysisException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void saveToDisk(FireworkArgs args, BufferedImage image) throws IOException {
		final File file = new File(IMAGE_FOLDER, args.getSpeciesName() + "." + args.getFormat());
		ImageIO.write(image, args.getFormat(), file);
	}

	private String createEnrichmentToken() throws IOException, AnalysisException, AnalysisServerError {
		final URL resource = getClass().getResource("enrichment.txt");
		final String text = IOUtils.toString(resource, Charset.defaultCharset());
		final AnalysisResult result = AnalysisClient.performAnalysis(text);
		return result.getSummary().getToken();
	}

	private String createExpressionToken() throws IOException, AnalysisException, AnalysisServerError {
		final URL resource = getClass().getResource("expression.txt");
		final String text = IOUtils.toString(resource, Charset.defaultCharset());
		final AnalysisResult result = AnalysisClient.performAnalysis(text);
		return result.getSummary().getToken();
	}

}
