package cl.uchile.dcc.dynamics.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

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
	
	public static String map2String(Map<Integer, ?> map) {
	    return Joiner.on(",").withKeyValueSeparator("=").join(map);
	}
	
	public static String topk2String(Map<Integer, Integer> values, int k) {

		ValueComparator bvc = new ValueComparator(values);
		TreeMap<Integer, Integer> sortedMap = new TreeMap<Integer, Integer>(bvc);
		sortedMap.putAll(values);
		StringBuilder query = new StringBuilder();
		int i = 0;
		for (Iterator it = sortedMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) it.next();
			i++;
			Integer key = null;
			Integer value = null;
			if (i == 1) {
				key = entry.getKey();
				value = entry.getValue();
				query.append(key);
				query.append("=");
				query.append(value);
			} else if (i <= k) {
				query.append(",");
				key = entry.getKey();
				value = entry.getValue();
				query.append(key);
				query.append("=");
				query.append(value);
			} else {
				break;
			}

			if (value == 1) {
				break;
			}
		}
		return query.toString();
	}
}