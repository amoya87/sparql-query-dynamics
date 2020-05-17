package cl.uchile.dcc.dynamics.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class ComparatorTest {

	@Test
	public void test() {
		String lpso = "P10>";
		String rpso = "P104>";
		int res = lpso.compareTo(rpso);
		assertEquals(true, res>0);
	}

}
