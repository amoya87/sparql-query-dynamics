package cl.uchile.dcc.data.diff;

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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class PredicateDynamicStats {

	public static void main(String[] args) throws IOException {
		Option inlO = new Option("l", "left input file");
		inlO.setArgs(1);
		inlO.setRequired(true);

		Option inrO = new Option("r", "right input file");
		inrO.setArgs(1);
		inrO.setRequired(true);

		Option ingzO = new Option("igz", "input file is GZipped");
		ingzO.setArgs(0);

		Option out1 = new Option("o1", "output file1");
		out1.setArgs(1);
		out1.setRequired(true);
		
		Option out2 = new Option("o2", "output file2");
		out2.setArgs(1);
		out2.setRequired(true);

		Option outgzO = new Option("ogz", "output file should be GZipped");
		outgzO.setArgs(0);

		Option helpO = new Option("h", "print help");

		Options options = new Options();
		options.addOption(inlO);
		options.addOption(inrO);
		options.addOption(ingzO);
		options.addOption(out1);
		options.addOption(out2);
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
		String inr = cmd.getOptionValue(inrO.getOpt());
		boolean gzIn = cmd.hasOption(ingzO.getOpt());

		// open the output
		String o1 = cmd.getOptionValue(out1.getOpt());		
		String o2 = cmd.getOptionValue(out2.getOpt());
		boolean gzOut = cmd.hasOption(outgzO.getOpt());

		diffGraph(inl, inr, gzIn, o1, o2, gzOut);
	}

	private static void diffGraph(String inl, String inr, boolean gzIn, String out1,
			String out2, boolean gzOut) throws IOException {
		
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
		
		OutputStream os1 = new FileOutputStream(out1);
		OutputStream os2 = new FileOutputStream(out2);
		if (gzOut) {
			os1 = new GZIPOutputStream(os1);
			os2 = new GZIPOutputStream(os2);
		}
		PrintWriter printWriter1 = new PrintWriter(
				new OutputStreamWriter(new BufferedOutputStream(os1), "utf-8"));
		PrintWriter printWriter2 = new PrintWriter(
				new OutputStreamWriter(new BufferedOutputStream(os2), "utf-8"));
		System.err.println("Writing to " + out1 + "\n");
		String leftTriple = inputl.readLine();
		String rightTriple = inputr.readLine();
		
		long ltripleCount = 0L;
		long rtripleCount = 0L;
		while (leftTriple != null || rightTriple != null) {
			int i;
			if (leftTriple == null) {
				i = 1;
			} else if (rightTriple == null) {
				i = -1;
			} else {
				i = leftTriple.compareTo(rightTriple);
			}
			if (i < 0) {
				printWriter1.println(leftTriple);
				leftTriple = inputl.readLine();
				ltripleCount++;
				continue;
			}
			if (i > 0) {
				printWriter2.println(rightTriple);
				rightTriple = inputr.readLine();
				rtripleCount++;
				continue;
			}
			leftTriple = inputl.readLine();
			rightTriple = inputr.readLine();
			ltripleCount++;
			rtripleCount++;
		}
		System.err.println("Read" + ltripleCount);
		System.err.println("Read" + rtripleCount);
		inputl.close();
		inputr.close();
		printWriter1.close();
		printWriter2.close();
	}
}
