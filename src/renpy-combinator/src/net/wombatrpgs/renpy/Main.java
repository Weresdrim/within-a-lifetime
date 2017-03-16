/**
 *  Main.java
 *  Created on Jul 8, 2016 8:08:41 PM for project renpy-combinator
 *  Author: psy_wombats
 *  Contact: psy_wombats@wombatrpgs.net
 */
package net.wombatrpgs.renpy;

/**
 * Entry point. Converts a script in esoteric psy markdown format into ren'py .rpy style formatting.
 * 
 * At the moment, doesn't have all the bells and whistles of say, the NScript combinator or the
 * HTML combinator, but so far that hasn't been necessary for any ren'py projects (sigh). That means
 * that this will only convert text lines and ren'py literals. Once there's demand for blt-style
 * entrances and exits (automatically adding character appearance and positioning code), that will
 * be implemented.
 * 
 * Assumes that init.rpy is written by hand.
 * 
 * Basic ren'py junk is here: https://www.renpy.org/doc/html/language_basics.html
 */
public class Main {
	
	private static String DEFAULT_INIT_FILENAME = "init.rpy";
	private static String DEFAULT_IN_DIRNAME = ".";
	private static String DEFAULT_OUT_FILENAME = "script.rpy";

	/**
	 * Entry point. Usage:
	 * 		args <dir> <out>
	 * 
	 * where <dir> is the top level directory of the folder to recursively build from, and <out> is
	 * the output file. Assumes there's a file called init.rpy located in the input directory at the
	 * top level, or else some file called initfile.
	 * 
	 * If no arguments supplied, assumes the current directory is the sourcedir and local directory
	 * called 'game.rpy' is the outfile.
	 * 
	 * @param	args			Command line args: <dir> <out> [initfile]
	 */
	public static void main(String[] args) {
		if (args.length > 3) {
			System.out.println("Usage: renpy-combinator <dir> <our> [initfile]");
			return;
		}
		
		String dir = (args.length >= 1) ? args[0] : DEFAULT_IN_DIRNAME;
		String out = (args.length >= 2) ? args[1] : DEFAULT_OUT_FILENAME;
		String initFilename = (args.length >= 3) ? args[2] : DEFAULT_INIT_FILENAME;
		
		System.out.println("Generating a script file from " + dir + " to " + out + "...");
		CombinatorJob job = new CombinatorJob(dir, out, initFilename);
		job.execute();
		System.out.println("Complete.");
	}

}
