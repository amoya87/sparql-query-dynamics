package cl.uchile.dcc.query.cardinality;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

public class TableStats {
	
	public TableStats() {
		super();
		cardinality = -1;
		variables = new HashMap<>();
	}

	private double cardinality;
	// Map<varName, Map<uniqValues,Map<value,Frequency>>>
	Map<String, Pair<Integer, Map<Integer, Integer>>> variables;

	public double getCardinality() {
		return cardinality;
	}

	public void setCardinality(Number cardinality) {
		this.cardinality = ((Number) cardinality).doubleValue() ;
	}

	public Pair<Integer, Map<Integer, Integer>> getVariableStats(String varName) {
		return variables.get(varName);
	}

	public void addVariables(String varName, Pair<Integer, Map<Integer, Integer>> sVar) {
		this.variables.put(varName, sVar);
	}
}
