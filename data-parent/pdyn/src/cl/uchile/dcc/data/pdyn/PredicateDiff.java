package cl.uchile.dcc.data.pdyn;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import cl.uchile.dcc.dynamics.utils.MemStats;

public class PredicateDiff {
	public static void main(String[] args) throws IOException {
		Option inlO = new Option("l", "left input file");
		inlO.setArgs(1);
		inlO.setRequired(true);

		Option inrO = new Option("r", "right input file");
		inrO.setArgs(1);
		inrO.setRequired(true);

		Option ingzO = new Option("igz", "input file is GZipped");
		ingzO.setArgs(0);

		Option outO = new Option("o", "output file");
		outO.setArgs(1);
		outO.setRequired(true);

		Option helpO = new Option("h", "print help");

		Options options = new Options();
		options.addOption(inlO);
		options.addOption(inrO);
		options.addOption(ingzO);
		options.addOption(outO);
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
		String inr = cmd.getOptionValue(inrO.getOpt());
		boolean gzIn = cmd.hasOption(ingzO.getOpt());

		// open the output
		String out = cmd.getOptionValue(outO.getOpt());

		diffGraph(inl, inr, gzIn, out);
	}

	private static void diffGraph(String inl, String inr, boolean gzIn, String out) throws IOException {
		InputStream ils = new FileInputStream(inl);
		if (gzIn)
			ils = new GZIPInputStream(ils);
		BufferedReader inputl = new BufferedReader(new InputStreamReader(ils, "utf-8"));
		System.err.println("Reading from " + inl);

		InputStream irs = new FileInputStream(inr);
		if (gzIn)
			irs = new GZIPInputStream(irs);
		BufferedReader inputr = new BufferedReader(new InputStreamReader(irs, "utf-8"));
		System.err.println("Reading from " + inr);

		OutputStream os = new FileOutputStream(out);
		PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os), "utf-8"));
		System.err.println("Writing to " + out + "\n");

		String leftTriple = inputl.readLine();
		String rightTriple = inputr.readLine();
		long ltripleCount = 0L;
		long rtripleCount = 0L;

		HashMap<String, Integer> removed = new HashMap<>();
		HashMap<String, Integer> noChanged = new HashMap<>();
		HashMap<String, Integer> added = new HashMap<>();

		while (leftTriple != null || rightTriple != null) {
			int k;
			if (leftTriple == null) {
				k = 1;
			} else if (rightTriple == null) {
				k = -1;
			} else {
				k = leftTriple.compareTo(rightTriple);
			}
			if (k < 0) {
				parseAndRegister(leftTriple, removed);
				ltripleCount++;
				leftTriple = inputl.readLine();
			} else if (k > 0) {
				parseAndRegister(rightTriple, added);
				rtripleCount++;
				rightTriple = inputr.readLine();
			} else {
				parseAndRegister(leftTriple, noChanged);
				ltripleCount++;
				rtripleCount++;
				leftTriple = inputl.readLine();
				rightTriple = inputr.readLine();
			}
			if (ltripleCount % TICKS == 0L) {
				System.err.println("Read" + ltripleCount + " left triples");
				System.err.println(MemStats.getMemStats() + "\n");
				System.out.println();
			}
		}
		for (String str : Sets.union(removed.keySet(), added.keySet()))
			printWriter.println(str + "\t" + ((double) (removed.getOrDefault(str, 0) + added.getOrDefault(str, 0)))
					/ (removed.getOrDefault(str, 0) + added.getOrDefault(str, 0) + noChanged.getOrDefault(str, 0)));
		int i = 0;
		for (Iterator<Integer> changedIter = Iterables.concat(removed.values(), added.values()).iterator(); changedIter.hasNext();) {
			i += changedIter.next();
		}
		int j = i;
		for (Iterator<Integer> noChangedIter = noChanged.values().iterator(); noChangedIter.hasNext();) {
			j += noChangedIter.next();
		}
		printWriter.println("<V>\t" + (i / j));
		inputl.close();
		inputr.close();
		printWriter.close();
	}

	private static void parseAndRegister(String triple, Map<String, Integer> predicateMap) {
		Pattern pattern = Pattern.compile(TRIPLE_REGEX);
		String pred = null;
		Matcher matcher = pattern.matcher(triple);
		if (matcher.matches()) {
			pred = matcher.group(2);
		} else {
			System.err.println(triple);
		}
		predicateMap.putIfAbsent(pred, 0);
		predicateMap.put(pred, predicateMap.get(pred) + 1);
	}
}