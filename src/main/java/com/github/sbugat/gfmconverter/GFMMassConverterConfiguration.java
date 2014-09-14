package com.github.sbugat.gfmconverter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class GFMMassConverterConfiguration {

	private static final String GFM_GITHUB_USER = "gfm.github.user";
	private static final String GFM_GITHUB_REPOSITORY = "gfm.github.repository";
	private static final String GFM_AUTHENTICATION_TOKEN = "gfm.authentication.token";
	private static final String GFM_FILE_ENCODING_PROPERTY = "gfm.file.encoding";
	private static final String GFM_FILE_ENCODING_PROPERTY_DEFAULT_VALUE = StandardCharsets.UTF_8.name();

	private static final String GFM_MASS_CONVERTER_PROPERTIES_FILE = "gfm-mass-converter.properties";

	private final String gFMGitHubUser;

	private final String gFMGitHubRepository;

	private final String gFMAuthenticationToken;

	private final String gFMFileEncoding;

	public GFMMassConverterConfiguration() throws IOException {

		//Configuration loading
		final Path propertyFile = Paths.get( GFM_MASS_CONVERTER_PROPERTIES_FILE );
		if( ! Files.exists( propertyFile ) ){

			throw new FileNotFoundException( GFM_MASS_CONVERTER_PROPERTIES_FILE );
		}

		//Load the configuration file and extract properties
		final Properties properties = new Properties();
		try( final Reader propertyFileReader = Files.newBufferedReader( propertyFile, StandardCharsets.UTF_8 ) ) {
			properties.load( propertyFileReader );
		}

		gFMGitHubUser = properties.getProperty( GFM_GITHUB_USER );
		gFMGitHubRepository = properties.getProperty( GFM_GITHUB_REPOSITORY );

		gFMAuthenticationToken = properties.getProperty( GFM_AUTHENTICATION_TOKEN );
		gFMFileEncoding = getOptionnalProperty( properties, GFM_FILE_ENCODING_PROPERTY, GFM_FILE_ENCODING_PROPERTY_DEFAULT_VALUE );
	}

	private static String getOptionnalProperty( final Properties properties, final String propertyName, final String propertyDefaultValue ) {

		final String propertyValue = properties.getProperty( propertyName, propertyDefaultValue );

		if( null == propertyValue || propertyValue.isEmpty() ) {
			return propertyDefaultValue;
		}

		return propertyValue;
	}

	public String getGFMGitHubUser() {
		return gFMGitHubUser;
	}

	public String getGFMGitHubRepository() {
		return gFMGitHubRepository;
	}

	public String getGFMAuthenticationToken() {
		return gFMAuthenticationToken;
	}

	public String getGFMFileEncoding() {
		return gFMFileEncoding;
	}
}
