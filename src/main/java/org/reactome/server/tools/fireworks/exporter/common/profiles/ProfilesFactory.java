package org.reactome.server.tools.fireworks.exporter.common.profiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.data.fireworks.profile.FireworksProfile;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ProfilesFactory {

	private static final Map<String, FireworksColorProfile> PROFILE_MAP = new TreeMap<>();
	private static final List<String> PROFILE_NAMES = Arrays.asList(
			"profile_01.json",
			"profile_02.json",
			"profile_03.json",
			"profile_04.json");
	private static FireworksColorProfile DEFAULT_PROFILE;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	/*
	 * Load all the profiles in memory. Profiles are resources. No Exception
	 * should be thrown.
	 */
	static {
		PROFILE_NAMES.forEach(name -> {
			final URL url = ProfilesFactory.class.getResource(name);
			try {
				final String content = IOUtils.toString(url, Charset.defaultCharset());
				final FireworksColorProfile profile = MAPPER.readValue(content, FireworksColorProfile.class);
				PROFILE_MAP.put(profile.getName().toLowerCase(), profile);
				if (DEFAULT_PROFILE == null) DEFAULT_PROFILE = profile;
			} catch (IOException e) {
				// Should never happen, they are resources
				e.printStackTrace();
			}
		});
	}

	/**
	 * Gets the {@link FireworksProfile} with name equals to profile, ignoring
	 * case.
	 *
	 * @param profile name of the profile, cas insensitive.
	 *
	 * @return the profile with profile name, or a default one.
	 */
	public static FireworksColorProfile getProfile(String profile) {
		if (profile == null || !PROFILE_MAP.containsKey(profile.toLowerCase()))
			return DEFAULT_PROFILE;
		return PROFILE_MAP.get(profile.toLowerCase());
	}
}
