package cl.uchile.dcc.data.stats;

import static cl.uchile.dcc.dynamics.utils.Constants.TICKS;
import static cl.uchile.dcc.dynamics.utils.Constants.TRIPLE_REGEX;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import cl.uchile.dcc.dynamics.utils.MemStats;

public class DatasetStats {

	public static void main(String[] args) throws IOException {
		Option inlO = new Option("l", "left input file");
		inlO.setArgs(1);
		inlO.setRequired(true);

		Option ingzO = new Option("igz", "input file is GZipped");
		ingzO.setArgs(0);

		Option outO = new Option("o", "output file");
		outO.setArgs(1);
		outO.setRequired(true);

		Option outgzO = new Option("ogz", "output file should be GZipped");
		outgzO.setArgs(0);

		Option helpO = new Option("h", "print help");

		Options options = new Options();
		options.addOption(inlO);
		options.addOption(ingzO);
		options.addOption(outO);
		options.addOption(outgzO);
		options.addOption(helpO);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("***ERROR: " + e.getClass() + ": " + e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("parameters:", options);
			return;
		}

		// print help options and return
		if (cmd.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("parameters:", options);
			return;
		}

		// open the inputs
		String inl = cmd.getOptionValue(inlO.getOpt());
		boolean gzIn = cmd.hasOption(ingzO.getOpt());

		// open the output
		String out = cmd.getOptionValue(outO.getOpt());
		boolean gzOut = cmd.hasOption(outgzO.getOpt());

		// call the method that does the hard work
		diffGraph(inl, gzIn, out, gzOut);
	}

	private static void diffGraph(String in, boolean gzIn, String out, boolean gzOut) throws IOException {

		InputStream is = new FileInputStream(in);
		if (gzIn)
			is = new GZIPInputStream(is);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "utf-8"));
		System.err.println("Reading from " + in);
		OutputStream os = new FileOutputStream(out);
		if (gzOut)
			os = new GZIPOutputStream(os);
		PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os), "utf-8"));
		System.err.println("Writing to " + out + "\n");
		String triple = bufferedReader.readLine();
		long tripleCount = 0L;
		Set<String> entities = new HashSet<>();
		Set<String> predicates = new HashSet<>();
		Set<String> blanks = new HashSet<>();
		long literalCount = 0L;
		while (triple != null) {
			Pattern pattern = Pattern.compile(TRIPLE_REGEX);
			String src = null;
			String pred = null;
			String obj = null;
			Matcher matcher = pattern.matcher(triple);
			if (matcher.matches()) {
				src = matcher.group(1);
				pred = matcher.group(2);
				obj = matcher.group(3).trim();
			} else {
				System.err.println(triple);
			}
			if (isBlank(src)) {
				blanks.add(src);
			} else {
				entities.add(src);
			}
			predicates.add(pred);
			entities.add(pred);
			if (isBlank(obj)) {
				blanks.add(obj);
			} else if (isLiteral(obj)) {
				literalCount++;
			} else {
				entities.add(obj);
			}
			triple = bufferedReader.readLine();
			tripleCount++;
			if (tripleCount % TICKS == 0L) {
				System.err.println("Read" + tripleCount + " triples");
				System.err.println(MemStats.getMemStats() + "\n");
			}
		}
		System.err.print(
				"Iteracion " + tripleCount + " entities = " + entities.size() + " predicates = " + predicates.size());
		System.err.println(" literals = " + literalCount + " blanks = " + blanks.size());
		bufferedReader.close();
		printWriter.close();
		System.err.println("Read" + tripleCount + " triples");
	}

	private static boolean isLiteral(String paramString) {
		return (!isUri(paramString) && !isBlank(paramString));
	}

	private static boolean isBlank(String paramString) {
		return paramString.substring(0, 1).equals("_");
	}

	private static boolean isUri(String paramString) {
		return paramString.substring(0, 1).equals("<");
	}
}