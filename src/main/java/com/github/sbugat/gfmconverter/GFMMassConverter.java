package com.github.sbugat.gfmconverter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.MarkdownService;
import org.eclipse.egit.github.core.service.RepositoryService;


/**
 * Main class of GitHub Flafored Markdown to html converter
 *
 * @author Sylvain Bugat
 *
 */
public class GFMMassConverter {

	/**File filter to select only directories or markdown files*/
	private static DirectoryStream.Filter<Path> DIRECTORY_AND_MARKDOWN_FILTER = new DirectoryStream.Filter<Path>() {
		public boolean accept( final Path path ) throws IOException {
			return ( Files.isDirectory( path ) || Files.isRegularFile( path ) && path.getFileName().toString().endsWith( ".md" ) );
		}
	};

	private final GFMMassConverterConfiguration gFMMassConverterConfiguration;

	private final GitHubClient gitHubClient;

	private final Repository gitHubProjectRepository;

	private final MarkdownService markdownService;

	public GFMMassConverter() throws IOException {

		gFMMassConverterConfiguration = new GFMMassConverterConfiguration();

		//GitHub client with optional authentication token
		gitHubClient = new GitHubClient();
		final String authenticationToken = gFMMassConverterConfiguration.getGFMAuthenticationToken();
		if( null != authenticationToken && ! authenticationToken.isEmpty() ) {
			gitHubClient.setOAuth2Token( authenticationToken );
		}

		final String gitHubUser = gFMMassConverterConfiguration.getGFMGitHubUser();
		final String gitHubProjet = gFMMassConverterConfiguration.getGFMGitHubRepository();

		//Get the optional repository configured
		if( null != gitHubUser && ! gitHubUser.isEmpty() && null != gitHubProjet && ! gitHubProjet.isEmpty() ) {

			final RepositoryService repositoryService = new RepositoryService( gitHubClient );
			gitHubProjectRepository = repositoryService.getRepository( gitHubUser, gitHubProjet );
		}
		else {
			gitHubProjectRepository = null;
		}

		markdownService = new MarkdownService( gitHubClient );
	}

	/**
	 * Open and convert a markdown file
	 *
	 * @param path file to convert
	 * @throws IOException
	 */
	private void convertFile( final Path path ) throws IOException {

		if( ! Files.exists( path ) ) {
			return;
		}

		final byte[] rawFileContent = Files.readAllBytes( path );
		final String fileContent = new String( rawFileContent, gFMMassConverterConfiguration.getGFMFileEncoding() );

		final Path htmlPath = Paths.get( path.toString().replaceFirst( ".md$", ".html" ) );

		if( null != gitHubProjectRepository ) {
			try( final InputStream markdownStream = markdownService.getRepositoryStream( gitHubProjectRepository, fileContent ) ) {

				Files.copy( markdownStream, htmlPath, StandardCopyOption.REPLACE_EXISTING );
			}
		}
		else {
			try( final InputStream markdownStream = markdownService.getStream( fileContent, MarkdownService.MODE_MARKDOWN ) ) {

				Files.copy( markdownStream, htmlPath, StandardCopyOption.REPLACE_EXISTING );
			}
		}
	}

	/**
	 * Convert a single file or a directory
	 *
	 * @param entryPointPath File or directory
	 * @throws IOException
	 */
	private void convertDirectory( final Path entryPointPath ) throws IOException {

		if( ! Files.exists( entryPointPath ) ) {
			return;
		}

		if( Files.isRegularFile( entryPointPath ) ) {

			convertFile( entryPointPath );
		}
		else if( Files.isDirectory( entryPointPath ) ) {

			for( final Path path : Files.newDirectoryStream( entryPointPath, DIRECTORY_AND_MARKDOWN_FILTER ) ) {

				if( Files.isDirectory( path ) ) {
					convertDirectory( path );
				}
				else if( Files.isRegularFile( path ) ) {
					convertFile( path );
				}
			}
		}
	}

	/**
	 * Mass converter launcher
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main( final String args[] ) throws IOException{

		final GFMMassConverter gFMMassConverter = new GFMMassConverter();
		gFMMassConverter.convertDirectory( Paths.get( "." ) );
	}
}
