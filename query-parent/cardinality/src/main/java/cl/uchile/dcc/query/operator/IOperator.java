package cl.uchile.dcc.query.operator;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public interface IOperator {
	
	public double getCardinality();

	public Set<String> getVariables();
	
	public Pair<Integer, Map<Integer, Integer>> getVariableStats(String varName);

}
