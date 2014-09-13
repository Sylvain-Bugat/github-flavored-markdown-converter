package com.github.sbugat.gfmconverter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.MarkdownService;


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
		final String fileContent = new String( rawFileContent, StandardCharsets.UTF_8 );

		final GitHubClient gitHubClient = new GitHubClient();
		final MarkdownService markdownService = new MarkdownService( gitHubClient );
		final String html = markdownService.getHtml( fileContent, MarkdownService.MODE_GFM );

		final Path htmlPath = Paths.get( path.toString().replaceFirst( ".md$", ".html" ) );

		try( final BufferedWriter BufferedWriter = Files.newBufferedWriter( htmlPath, StandardCharsets.ISO_8859_1 ) ) {

			BufferedWriter.write( html );
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
