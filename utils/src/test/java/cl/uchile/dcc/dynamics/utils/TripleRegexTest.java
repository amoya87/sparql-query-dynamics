package cl.uchile.dcc.dynamics.utils;

import static cl.uchile.dcc.dynamics.utils.Constants.TRIPLE_REGEX;
import static cl.uchile.dcc.dynamics.utils.Constants.TRIPLE_REGEX_LIGHT;
import static org.junit.Assert.assertEquals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class TripleRegexTest {

//	public static String TRIPLE_REGEX = "^(\\S+)\\s+(<[^>]+>)\\s+(.+\\S+)\\s*.$";
	public static String III = "<http1> <http2> <http3> .";
	public static String IIL = "<http1> <http2> \"new york city\" .";
	public static String IIB = "<http1> <http2> _abc .";
	public static String BII = "_abc <http2> <http3> .";
	public static String BIL = "_abc <http2> \"new york city\" .";
	public static String BIB = "_abc <http2> _abc .";	
	public static String S = "_abc <http2> _abc ";	
	public static String SS = "_abc <http2> _abc  ";
	public static String P = "_abc <http2> _abc.";
	public static String PSO = "<http2> _abc _abc.";
	public static String DBP = "<http1> <http 2> <http 3> .";
	public static String OLANG = "<http1> <http 2> \"San Antonio Spurs\"@en .";
	
	
	//fail
	public static String NF = "_abc <http2> _abc";
	
	@Test
	public void testOLANG() {
		Pattern pattern = Pattern.compile(TRIPLE_REGEX);
		Matcher matcher = pattern.matcher(OLANG);
		matcher.matches();
		assertEquals(1, matcher.group(3).hashCode());
	}
	
	@Test
	public void testDBP() {
		Pattern pattern = Pattern.compile(TRIPLE_REGEX);
		Matcher matcher = pattern.matcher(DBP);
		matcher.matches();
		assertEquals(DBP, matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + " .");
	}

	@Test
	public void testIII() {
		Pattern pattern = Pattern.compile(TRIPLE_REGEX);
		Matcher matcher = pattern.matcher(III);
		matcher.matches();
		assertEquals(III, matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + " .");
	}

	@Test
	public void testIIL() {
		Pattern pattern = Pattern.compile(TRIPLE_REGEX);
		Matcher matcher = pattern.matcher(IIL);
		matcher.matches();
		assertEquals(IIL, matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + " .");
	}

	@Test
	public void testIIB() {
		Pattern pattern = Pattern.compile(TRIPLE_REGEX);
		Matcher matcher = pattern.matcher(IIB);
		matcher.matches();
		assertEquals(IIB, matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + " .");
	}

	@Test
	public void testBII() {
		Pattern pattern = Pattern.compile(TRIPLE_REGEX);
		Matcher matcher = pattern.matcher(BII);
		matcher.matches();
		assertEquals(BII, matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + " .");
	}

	@Test
	public void testBIL() {
		Pattern pattern = Pattern.compile(TRIPLE_REGEX);
		Matcher matcher = pattern.matcher(BIL);
		matcher.matches();
		assertEquals(BIL, matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + " .");
	}

	@Test
	public void testBIB() {
		Pattern pattern = Pattern.compile(TRIPLE_REGEX);
		Matcher matcher = pattern.matcher(BIB);
		matcher.matches();
		assertEquals(BIB, matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + " .");
	}
	
	@Test
	public void testS() {
		Pattern pattern = Pattern.compile(TRIPLE_REGEX);
		Matcher matcher = pattern.matcher(S);
		matcher.matches();
		assertEquals(S, matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + " ");
	}
	
	@Test
	public void testSS() {
		Pattern pattern = Pattern.compile(TRIPLE_REGEX);
		Matcher matcher = pattern.matcher(SS);
		matcher.matches();
		assertEquals(SS, matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + "  ");
	}
/*	
	@Test
	public void testNF() {
		Pattern pattern = Pattern.compile(TRIPLE_REGEX);
		Matcher matcher = pattern.matcher(NF);
		matcher.matches();
		assertEquals(NF, matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + "");
	}
	*/
	@Test
	public void testP() {
		Pattern pattern = Pattern.compile(TRIPLE_REGEX);
		Matcher matcher = pattern.matcher(P);
		matcher.matches();
		assertEquals(P, matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + ".");
	}
	
	@Test(expected = IllegalStateException.class)
	public void testPSO() {
		Pattern pattern = Pattern.compile(TRIPLE_REGEX);
		Matcher matcher = pattern.matcher(PSO);
		matcher.matches();
		String s = matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + ".";
	}
	
	@Test
	public void testPSOLight() {
		Pattern pattern = Pattern.compile(TRIPLE_REGEX_LIGHT);
		Matcher matcher = pattern.matcher(PSO);
		matcher.matches();
		assertEquals(PSO, matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3) + ".");
	}

}
