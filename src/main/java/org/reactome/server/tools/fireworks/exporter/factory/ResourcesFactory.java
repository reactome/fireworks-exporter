package org.reactome.server.tools.fireworks.exporter.factory;

import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.data.FireworksFactory;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ResourcesFactory {
	private static final String NOT_FOUND = "Specie not found: %s";
	private static final String ERROR_PARSING = "Error reading file %s";


	/**
	 * Get the {@link FireworksGraph} of the speciesName. Json file should be in
	 * layoutPath.
	 *
	 * @param layoutPath  path containing the json file
	 * @param speciesName name of the specie
	 *
	 * @return the {@link FireworksGraph}
	 *
	 * @throws IllegalArgumentException if species is not found or file is
	 *                                  corrupt
	 */
	public static FireworksGraph getGraph(String layoutPath, String speciesName) {
		final File file = new File(layoutPath, speciesName + ".json");
		if (!file.exists())
			throw new IllegalArgumentException(String.format(NOT_FOUND, speciesName));
		try {
			final String content = IOUtils.toString(file.toURI(), Charset.defaultCharset());
			return FireworksFactory.getGraph(content);
		} catch (DeserializationException | IOException e) {
			throw new IllegalArgumentException(String.format(ERROR_PARSING, file.getName()), e);
		}
	}


}
