package cl.uchile.dcc.data.card;

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
import java.util.HashSet;
import java.util.Map;
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

import cl.uchile.dcc.dynamics.utils.MapUtils;
import cl.uchile.dcc.dynamics.utils.MemStats;

public class DiffPSortedCardStats {

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

		Option out3 = new Option("i", "output intersection stat");
		out3.setArgs(1);
		out3.setRequired(true);

		Option out4 = new Option("u", "output union stat");
		out4.setArgs(1);
		out4.setRequired(true);

		Option outgzO = new Option("ogz", "output file should be GZipped");
		outgzO.setArgs(0);

		Option kO = new Option("k", "print top-k values in MCV");
		kO.setArgs(1);
		kO.setRequired(false);

		Option tO = new Option("t", "print first t lines to read in");
		tO.setArgs(1);
		tO.setRequired(false);

		Option helpO = new Option("h", "print help");

		Options options = new Options();
		options.addOption(inlO);
		options.addOption(inrO);
		options.addOption(ingzO);
		options.addOption(out1);
		options.addOption(out2);
		options.addOption(out3);
		options.addOption(kO);
		options.addOption(tO);
		options.addOption(out4);
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
		String o3 = cmd.getOptionValue(out3.getOpt());
		String o4 = cmd.getOptionValue(out4.getOpt());
		boolean gzOut = cmd.hasOption(outgzO.getOpt());

		// if we need to print top-k afterwards
		int k = 1000;
		if (cmd.hasOption(kO.getOpt())) {
			k = Integer.parseInt(cmd.getOptionValue(kO.getOpt()));
		}

		// if we need to read top-t triplets
		long t = Long.MAX_VALUE;
		;
		if (cmd.hasOption(tO.getOpt())) {
			t = Long.parseLong(cmd.getOptionValue(tO.getOpt()));
		}

		diffGraph(inl, inr, gzIn, o1, o2, o3, o4, gzOut, k, t);

	}

	private static void diffGraph(String inl, String inr, boolean gzIn, String o1, String o2, String i1, String u1,
			boolean gzOut, int k, long t) throws IOException {

		// open the input
		InputStream ils = new FileInputStream(inl);
		if (gzIn) {
			ils = new GZIPInputStream(ils);
		}

		BufferedReader inputl = new BufferedReader(new InputStreamReader(ils, "utf-8"));
		System.err.println("Reading from " + inl);

		InputStream irs = new FileInputStream(inr);
		if (gzIn)
			irs = new GZIPInputStream(irs);

		BufferedReader inputr = new BufferedReader(new InputStreamReader(irs, "utf-8"));
		System.err.println("Reading from " + inr);

		OutputStream os1 = new FileOutputStream(o1);
		OutputStream os2 = new FileOutputStream(o2);
		if (gzOut) {
			os1 = new GZIPOutputStream(os1);
			os2 = new GZIPOutputStream(os2);
		}

		OutputStream os3 = new FileOutputStream(i1);
		OutputStream os4 = new FileOutputStream(u1);
		PrintWriter printWriter1 = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os1), "utf-8"));
		PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os2), "utf-8"));
		PrintWriter printWriter3 = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os3), "utf-8"));
		PrintWriter printWriter4 = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os4), "utf-8"));

		long ltripleCount = 0L;
		long rtripleCount = 0L;
		long itripleCount = 0L;
		long utripleCount = 0L;
		long itriplePredCount = 0L;
		long utriplePredCount = 0L;
		String sl = null;
		String pl = null;
		String ol = null;
		String sr = null;
		String pr = null;
		String or = null;

		String leftTriple = inputl.readLine();
		String rightTriple = inputr.readLine();

		Map<Integer, Integer> iSubjects = new HashMap<>();
		Map<Integer, Integer> iObjects = new HashMap<>();
		Map<Integer, Integer> uSubjects = new HashMap<>();
		Map<Integer, Integer> uObjects = new HashMap<>();
		String lastPredicate = "";
		boolean started = false;
		Pattern pattern = Pattern.compile(TRIPLE_REGEX);
		Matcher lmatcher = null;
		Matcher rmatcher = null;
		Set<String> blacklist = new HashSet<>();
		blacklist.add("<http://schema.org/name>");
		blacklist.add("<http://www.w3.org/2000/01/rdf-schema#label>");
		blacklist.add("<http://www.w3.org/2004/02/skos/core#prefLabel>");
		blacklist.add("<http://schema.org/about>");
		blacklist.add("<http://schema.org/version>");
		blacklist.add("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
		blacklist.add("<http://schema.org/dateModified>");
		blacklist.add("<http://schema.org/description>");
		blacklist.add("<http://www.w3.org/2004/02/skos/core#altLabel>");

		try {
			while ((leftTriple != null || rightTriple != null) && t-- > 0) {

				if (leftTriple != null) {
					lmatcher = pattern.matcher(leftTriple);
					if (lmatcher.matches()) {
						sl = lmatcher.group(1);
						pl = lmatcher.group(2);
						ol = lmatcher.group(3);
					} else
						System.err.println("Error parseando " + leftTriple);
				}

				if (rightTriple != null) {
					rmatcher = pattern.matcher(rightTriple);
					if (rmatcher.matches()) {
						sr = rmatcher.group(1);
						pr = rmatcher.group(2);
						or = rmatcher.group(3);
					} else
						System.err.println("Error parseando " + rightTriple);
				}

				int i;
				if (leftTriple == null) {
					i = 1;
				} else if (rightTriple == null) {
					i = -1;
				} else {
					String lpso = pl + sl + ol;
					String rpso = pr + sr + or;
					i = lpso.compareTo(rpso);
				}

				if (i < 0) { // removed triple
					if (!pl.equals(lastPredicate)) { // finished predicate
						if (started) { // Not first predicate
							flushPredicate(itriplePredCount, utriplePredCount, iSubjects, iObjects, uSubjects, uObjects,
									lastPredicate, printWriter3, printWriter4, k);
							printWriter1.flush();
							printWriter2.flush();
							itripleCount += itriplePredCount;
							utripleCount += utriplePredCount;							
							itriplePredCount = 0;
							utriplePredCount = 0;
						}
						lastPredicate = pl;
						started = true;
					}

					if (!blacklist.contains(pl)) { // Count unique values
						MapUtils.increment(uSubjects, sl.hashCode());
						MapUtils.increment(uObjects, ol.hashCode());
					}
					++utriplePredCount;
					printWriter1.println(leftTriple);
					leftTriple = inputl.readLine();
					ltripleCount++;
					continue;
				}
				if (i > 0) {
					if (!pr.equals(lastPredicate)) {
						if (started) {
							flushPredicate(itriplePredCount, utriplePredCount, iSubjects, iObjects, uSubjects, uObjects,
									lastPredicate, printWriter3, printWriter4, k);
							printWriter1.flush();
							printWriter2.flush();
							itripleCount += itriplePredCount;
							utripleCount += utriplePredCount;
							itriplePredCount = 0;
							utriplePredCount = 0;
						}
						lastPredicate = pr;
						started = true;
					}

					if (!blacklist.contains(pr)) {
						MapUtils.increment(uSubjects, sr.hashCode());
						MapUtils.increment(uObjects, or.hashCode());
					}
					++utriplePredCount;
					printWriter2.println(rightTriple);
					rightTriple = inputr.readLine();
					rtripleCount++;
					continue;
				}

				// No changed
				if (!pl.equals(lastPredicate)) {
					if (started) {
						flushPredicate(itriplePredCount, utriplePredCount, iSubjects, iObjects, uSubjects, uObjects,
								lastPredicate, printWriter3, printWriter4, k);
						printWriter1.flush();
						printWriter2.flush();
						itripleCount += itriplePredCount;
						utripleCount += utriplePredCount;
						itriplePredCount = 0;
						utriplePredCount = 0;
					}
					lastPredicate = pl;
					started = true;
				}

				if (!blacklist.contains(pl)) {
					MapUtils.increment(iSubjects, sl.hashCode());
					MapUtils.increment(iObjects, ol.hashCode());
				}
				++itriplePredCount;
				leftTriple = inputl.readLine();
				rightTriple = inputr.readLine();
				ltripleCount++;
				rtripleCount++;

				if (ltripleCount % TICKS == 0) {
					System.err.println("Readed " + ltripleCount + " ltriples and " + rtripleCount + " rtriples with ("
							+ lastPredicate + ")");
					System.err.println(MemStats.getMemStats() + "\n");
				}

			}
		} catch (Exception e) {
			System.err.println("Readed" + ltripleCount + " ltriples and " + rtripleCount + "rtriples with ("
					+ lastPredicate + ")");
			System.out.println(lastPredicate + ", " + itriplePredCount + ", " + iSubjects.size() + ", " + iObjects.size());
		}
		if (started) {			
			flushPredicate(itriplePredCount, utriplePredCount, iSubjects, iObjects, uSubjects, uObjects, lastPredicate,
					printWriter3, printWriter4, k);
			printWriter1.flush();
			printWriter2.flush();
			itripleCount += itriplePredCount;
			utripleCount += utriplePredCount;
		}
		
		StringBuilder istr = new StringBuilder();
		istr.append("<tp>").append("\t").append(itripleCount).append("\t").append(itripleCount).append("\t")
				.append(itripleCount);
		printWriter3.println(istr);
		
		StringBuilder ustr = new StringBuilder();
		ustr.append("<tp>").append("\t").append(utripleCount).append("\t").append(utripleCount).append("\t")
				.append(utripleCount);		
		printWriter4.println(ustr);

		System.err.println("Read" + ltripleCount);
		System.err.println("Read" + rtripleCount);
		inputl.close();
		inputr.close();
		printWriter1.close();
		printWriter2.close();
		printWriter3.close();
		printWriter4.close();
	}

	private static void flushPredicate(long itripleCount, long otripleCount, Map<Integer, Integer> iSubjects,
			Map<Integer, Integer> iObjects, Map<Integer, Integer> uSubjects, Map<Integer, Integer> uObjects,
			String lastPredicate, PrintWriter i, PrintWriter u, int k) {

		int iSubjectsSize = iSubjects.size();
		int iObjectsSize = iObjects.size();

		if (itripleCount > 0) {
			StringBuilder istr = new StringBuilder();
			istr.append(lastPredicate).append("\t").append(itripleCount).append("\t").append(iSubjectsSize).append("\t")
					.append(iObjectsSize).append("\t");
			if (iSubjectsSize < itripleCount) {
				istr.append(MapUtils.topk2String(iSubjects, k)).append("\t");
			} else {
				istr.append("\t");
			}
			if (iObjectsSize < itripleCount) {
				istr.append(MapUtils.topk2String(iObjects, k));
			}

			i.println(istr);
			i.flush();
		}

		uSubjects.forEach((key, value) -> iSubjects.merge(key, value, Integer::sum));

		uObjects.forEach((key, value) -> iObjects.merge(key, value, Integer::sum));

		long utripleCount = itripleCount + otripleCount;
		int uSubjectsSize = iSubjects.size();
		int uObjectsSize = iObjects.size();

		StringBuilder ustr = new StringBuilder();
		ustr.append(lastPredicate).append("\t").append(utripleCount).append("\t").append(uSubjectsSize).append("\t")
				.append(uObjectsSize).append("\t");

		if (uSubjectsSize < utripleCount) {
			ustr.append(MapUtils.topk2String(iSubjects, k)).append("\t");
		} else {
			ustr.append("\t");
		}
		if (uObjectsSize < utripleCount) {
			ustr.append(MapUtils.topk2String(iObjects, k));
		}

		u.println(ustr);
		u.flush();
		iSubjects.clear();
		iObjects.clear();
		uSubjects.clear();
		uObjects.clear();
	}

}