package org.reactome.server.tools.fireworks.exporter.raster.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

public class ContentServiceClient {

	private static final String QUERY = "data/pathways/low/diagram/identifier/%s/allForms?speciesId=%s";
	private static String HOST = "http://localhost";
	private static String SERVICE = "/ContentService/";
	private static final ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings("unused")
	public static void setHost(String host) {
		ContentServiceClient.HOST = host;
	}

	@SuppressWarnings("unused")
	public static void setService(String service) {
		ContentServiceClient.SERVICE = service;
	}

	/**
	 * Get a list of dbIds of pathways that should be flagged. This method calls
	 * ContentService from reactome:
	 * <p>
	 * <pre>
	 * {host}{service}data/pathways/low/diagram/identifier/{term}/allForms?speciesId={speciesId}
	 * </pre>
	 * where <em>host</em> is <strong>http://localhost</strong> and
	 * <em>service</em>, <strong>/ContentService/</strong> by default.
	 *
	 * @param term      term to look for
	 * @param speciesId species where to look for
	 *
	 * @return a list of dbIds that should be flagged, null if service is
	 * unavailable
	 */
	static Set<Long> getFlagged(String term, Long speciesId) {
		try {
			final URL url = new URL(HOST + SERVICE + String.format(QUERY, term, speciesId));

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Response-Type", "application/json");
			switch (connection.getResponseCode()) {
				case 200:
					final String json = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
					final ArrayNode nodes = mapper.readValue(json, ArrayNode.class);
					final Set<Long> ids = new HashSet<>();
					nodes.forEach(jsonNode -> ids.add(jsonNode.get("dbId").longValue()));
					return ids;
				default:
					final String error = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
					// TODO: throw proper error
					// This is throwing a DeseralizationException
//					throw new AnalysisException(getObject(AnalysisError.class, error));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


}
