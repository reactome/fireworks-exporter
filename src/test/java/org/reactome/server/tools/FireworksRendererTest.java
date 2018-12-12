package org.reactome.server.tools;


import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.tools.fireworks.exporter.FireworksExporter;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.fail;

/**
 * Unit test for simple FireworksRenderer.
 */
public class FireworksRendererTest {
	private static final String TOKEN_OVER_1 = "MjAxODExMDEwNzI3NDNfOA%253D%253D"; // uniprot (GBM Uniprot)
	private static final String TOKEN_OVER_2 = "MjAxODExMDEwNzMyMDdfOQ%253D%253D"; // Gene NCBI (12 tumors)
	private static final String TOKEN_EXPRESSION_1 = "MjAxODExMDEwNzMyMjJfMTA%253D";  // microarray (probeset)
	private static final String TOKEN_EXPRESSION_2 = "MjAxODEwMzAxMDIzMDBfNQ%253D%253D";  // HPA (GeneName)
	private static final String TOKEN_SPECIES = "MjAxODExMDEwNzMzMTRfMTE%253D"; // canis

	private static final String ANALYSIS_PATH = "src/test/resources/org/reactome/server/tools/fireworks/exporter/analysis";
	private static final String FIREWORK_PATH = "src/test/resources/org/reactome/server/tools/fireworks/exporter/layouts";
	private static final File IMAGE_FOLDER = new File("test-image");
	private static final boolean save = true;

	private static final TokenUtils TOKEN_UTILS = new TokenUtils(ANALYSIS_PATH);
	private static final FireworksExporter exporter = new FireworksExporter(FIREWORK_PATH, ANALYSIS_PATH);


	@BeforeClass
	public static void beforeClass() {
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
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setQuality(8);
		args.setFlags(Collections.singletonList("R-HSA-450294"));
		args.setWriteTitle(true);
		render(args, null);
	}

	@Test
	public void testEnrichment() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setQuality(8);
		args.setSelected(Arrays.asList("R-HSA-196783"));
		args.setToken(TOKEN_OVER_1);
		render(args, null);
	}

	@Test
	public void testExpression() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setQuality(8);
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

	@Test
	public void testToSvg() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "svg");
		try {
			final FileOutputStream os = new FileOutputStream(new File(IMAGE_FOLDER, "Homo_sapiens.svg"));
			exporter.render(args, os);
		} catch (AnalysisServerError | TranscoderException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testToSvgWithAnalysis() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "svg");
		args.setToken(TOKEN_OVER_1);
		args.setSelected(Arrays.asList("R-HSA-169911", "R-HSA-3560792"));
		try {
			final FileOutputStream os = new FileOutputStream(new File(IMAGE_FOLDER, "Homo_sapiens_expression.svg"));
			exporter.render(args, os);
		} catch (AnalysisServerError | TranscoderException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testPdf() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "pdf");
		final AnalysisStoredResult result = new TokenUtils(ANALYSIS_PATH).getFromToken(TOKEN_EXPRESSION_2);
		try {
			final OutputStream os = save
					? new NullOutputStream()
					: new FileOutputStream(new File(IMAGE_FOLDER, "Homo_sapies.pdf"));
			exporter.render(args, result, os);
		} catch (AnalysisServerError | TranscoderException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
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
