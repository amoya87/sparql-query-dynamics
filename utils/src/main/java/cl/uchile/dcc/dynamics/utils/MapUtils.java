package cl.uchile.dcc.dynamics.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;


// Program to Increment a Map value in Java 8 and above
public class MapUtils
{
	public static<K> void increment(Map<K,Integer> map, K key) {
		if (map.computeIfPresent(key, (k, v) -> v + 1) == null)
			map.put(key, 1);
	}
	
	public static Map<String, String> string2Map(String mapAsString) {
	    return Splitter.on(',').withKeyValueSeparator('=').split(mapAsString);
	}
	
	public static Map<String, String> string2Map(String mapAsString, int k) {
		String subStringk = string2Topk(mapAsString, k);
	    return Splitter.on(',').withKeyValueSeparator('=').split(subStringk);
	}
	
	public static String string2Topk(String mapAsString, int k) {
		String[] cads = mapAsString.split(",", k + 1);
		if (cads.length > k) {
			cads = Arrays.copyOf(cads, k);
		}
		
		String cad = String.join(",", cads);
		return cad;
	}
	
	public static String map2String(Map<Integer, ?> map) {
	    return Joiner.on(",").withKeyValueSeparator("=").join(map);
	}
	
	public static <T> String toHex(T bytes) {
		// Get complete hashed password in hex format
		StringBuilder sb = new StringBuilder();
		if (bytes instanceof List) {
			List<Byte> bites = (List<Byte>) bytes;
			for (Byte byt : bites) {
				sb.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
			}
		} else {
			sb.append(bytes.toString());
		}
		
		return sb.toString();
	}
	
	public static <T> String topk2String(Map<T, Integer> values, int k) {

		ValueComparator bvc = new ValueComparator(values);
		TreeMap<T, Integer> sortedMap = new TreeMap<T, Integer>(bvc);
		sortedMap.putAll(values);
		StringBuilder query = new StringBuilder();
		int i = 0;
		for (Iterator it = sortedMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<T, Integer> entry = (Map.Entry<T, Integer>) it.next();
			i++;
			T key = null;
			Integer value = null;
			if (i == 1) {
				key = entry.getKey();
				value = entry.getValue();
				query.append(toHex(key));
				query.append("=");
				query.append(value);
			} else if (i <= k) {
				query.append(",");
				key = entry.getKey();
				value = entry.getValue();
				query.append(toHex(key));
				query.append("=");
				query.append(value);
			} else {
				break;
			}
/*
			if (value == 1) {
				break;
			}*/
		}
		return query.toString();
	}
	
	public static List<Byte> md5Code(String passwordToHash){
		// Create MessageDigest instance for MD5
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Add password bytes to digest
		try {
			md.update(passwordToHash.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Get the hash's bytes
		byte[] bytes = md.digest();

		List<Byte> list = IntStream.range(0, bytes.length).mapToObj(i -> bytes[i]).collect(Collectors.toList());

		return list;
	}
}