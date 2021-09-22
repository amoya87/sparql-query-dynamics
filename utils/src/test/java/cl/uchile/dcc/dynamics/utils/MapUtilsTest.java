package cl.uchile.dcc.dynamics.utils;

import static org.junit.Assert.*;

import java.util.HashMap;

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
	
	@Test
	public void test() {
		assertEquals("3=4,1=2,4=1,2=1", MapUtils.topk2String(new HashMap<Integer, Integer>() {{
	        put(3, 4);
	        put(1, 2);
	        put(2, 1);
	        put(4, 1);
	    }}, 4));
	}
	
	@Test
	public void test1() {
		assertEquals("3=2,1=2,5=1,4=1,2=1", MapUtils.topk2String(new HashMap<Integer, Integer>() {{
			put(1, 2);
			put(3, 2);
	        put(5, 1);
	        put(2, 1);
	        put(4, 1);
	    }}, 8));
	}
	
	@Test
	public void test2() {
		assertEquals("3=4,1=2", MapUtils.topk2String(new HashMap<Integer, Integer>() {{
	        put(3, 4);
	        put(1, 2);
	        put(2, 1);
	        put(4, 1);
	    }}, 2));
	}

}