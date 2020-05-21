package cl.uchile.dcc.dynamics.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class MapUtilsTest {

	@Test
	public void testExact() {
		assertEquals("a=1,b=2,c=3", MapUtils.string2Topk("a=1,b=2,c=3", 3));
	}
	
	@Test
	public void testLess() {
		assertEquals("a=1,b=2", MapUtils.string2Topk("a=1,b=2", 3));
	}
	
	@Test
	public void testMore() {
		assertEquals("a=1,b=2,c=3", MapUtils.string2Topk("a=1,b=2,c=3,d=4,e=5", 3));
	}

}