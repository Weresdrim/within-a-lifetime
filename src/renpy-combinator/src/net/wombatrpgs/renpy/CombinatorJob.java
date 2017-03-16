/**
 *  CombinatorJob.java
 *  Created on Jul 8, 2016 8:13:09 PM for project renpy-combinator
 *  Author: psy_wombats
 *  Contact: psy_wombats@wombatrpgs.net
 */
package net.wombatrpgs.renpy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Excecutable class for recombining renpy files.
 */
public class CombinatorJob {
	
	protected static String DISCLAIMER_MESSAGE = "# WARNING! This file is generated! Do not edit!";
	
	protected static String EXTENSION_CONVERT = "txt";
	protected static String EXTENSION_LITERAL = "rpy";
	
	protected String inDirectoryFilename;
	protected String outFilename;
	protected String initFilename;
	
	/**
	 * Creates (but does not run) a new job.
	 * @param	inDirectoryFilename	The name of the directory to recursively read from
	 * @param	outFilename			The name of the file to write to
	 * @param	initFilename		The name of the file with initialization code (should be first)
	 */
	public CombinatorJob(String inDirectoryFilename, String outFilename, String initFilename) {
		this.inDirectoryFilename = inDirectoryFilename;
		this.outFilename = outFilename;
		this.initFilename = initFilename;
	}
	
	/**
	 * Runs the combinator job and generates the output file!
	 */
	public void execute() {
		try {
			File outFile = new File(outFilename);
			outFile.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
			
			writer.write(DISCLAIMER_MESSAGE + "\n\n");
			
			File initFile = new File(initFilename);
			BufferedReader initReader = new BufferedReader(new FileReader(initFile));
			cat(initReader, writer);
			
			for (File file : recursivelyListFiles(new File(inDirectoryFilename))) {
				// don't double-write init if it happens to be in the dir
				if (!file.getAbsolutePath().equals(initFile.getAbsolutePath())) {
					BufferedReader reader = new BufferedReader(new FileReader(file));
					int extensionIndex = file.getName().lastIndexOf(".");
					String extension = "";
					if (extensionIndex > 0) {
						extension = file.getName().substring(extensionIndex + 1);
					}
					cat(reader, writer, FilenameU);
				}
			}
			
			writer.close();
			
		} catch (IOException exception) {
			exception.printStackTrace();
			System.err.println("IO exception in job body");
		}
	}
	
	/**
	 * Recursively adds all files in the given directory to a list and returns it.
	 * @param	directory			The directory to read from
	 * @return						A list of all non-directory files in that directory and children
	 */
	protected List<File> recursivelyListFiles(File directory) {
		ArrayList<File> results = new ArrayList<>();
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				results.addAll(recursivelyListFiles(file));
			} else {
				results.add(file);
			}
		}
		return results;
	}
	
	/**
	 * Writes the contents of reader into writer. Does not open the streams or close them either.
	 * @throws	IOException			If there's an error reading/writing or extension is unknown
	 * @param	reader				The reader to read from
	 * @param	writer				The writer to write to
	 * @param	extension			The file extension of the read file, determines convert mode
	 */
	protected void cat(BufferedReader reader, BufferedWriter writer, String extension) throws IOException {
		while (reader.ready()) {
			String line = reader.readLine();
			if (extension.equals(EXTENSION_CONVERT)) {
				String convertedLine = convertLine(line);
				writer.write(convertedLine);
			} else if (extension.equals(EXTENSION_LITERAL)) {
				writer.write(line);
			} else {
				throw new IOException("Unknown extension " + extension);
			}
		}
		writer.write("\n");
	}
	
	/**
	 * Converts a single line from psy format to rpy format. Not tokenized at the moment for
	 * simplicity's sake, just a straight if chain. Supports only literals, dialog lines, and
	 * monologue lines.
	 * @param	sourceLine			The source line to convert, in psy format
	 * @return						That line in rpy format
	 */
	protected String convertLine(String sourceLine) {
		String converted = sourceLine;
		
		if (converted.trim().length() == 0) {
			// whitespace lol
		} else if (!converted.startsWith("label")) {
			// label literal, should be the /only/ thing not indented
			if (!converted.endsWith(":")) {
				converted += ":";
			}
		} else {
			String speaker = speakerForLine(sourceLine);
			if (speaker == null) {
				if (Character.isLowerCase(sourceLine.trim().charAt(0))) {
					// line began with a lowercase character, no doubt it's a rpy literal
					// please don't do anything mega ungrammatical like this with the monologue
					// (no need for more processing)
				} else {
					// monologue line
					converted = "\"" + converted + "\"";
				}
			} else {
				// spoken line
				String lowerSpeaker = speaker.toLowerCase();
				converted = converted.substring(converted.indexOf(":") + 1, converted.length());
				converted = lowerSpeaker + " " + converted;
			}
		}
		
		converted += "\n";
		return converted;
	}
	
	/** 
	 * Checks if the given line begins with a lower case character.
	 * @param	line				The line to check
	 * @return						True if begins with lower, false otherwise
	 */
	protected boolean beginsWithUpper(String line) {
		if (line.trim().length() < 1) {
			return false;
		}
		if (!Character.isAlphabetic(line.trim().charAt(0))) {
			return false;
		}
		return Character.isUpperCase(line.trim().charAt(0));
	}
	
	/**
	 * Checks if the given psy line has a speaker, and if so, extracts their name.
	 * @param	line				The line to check
	 * @return						The all-caps name of the speaker, or null if none
	 */
	protected String speakerForLine(String line) {
		if (!beginsWithUpper(line)) {
			return null;
		}
		int colonIndex = line.indexOf(':');
		if (colonIndex < 0) {
			return null;
		}
		int startIndex;
		for (startIndex = colonIndex - 1; startIndex >= 0; startIndex -= 1) {
			if (line.charAt(startIndex) == ' ') {
				// we went past the first character
				startIndex += 1;
				break;
			} else if (!Character.isUpperCase(line.charAt(startIndex))) {
				// non-upper detected before colon
				return null;
			}
		}
		String speaker = line.substring(startIndex, colonIndex);
		if (speaker.equals("???")) {
			// hack for voices
			return "UNKNOWN";
		} else {
			return speaker;
		}
	}
}
