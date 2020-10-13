package cl.uchile.dcc.query.operator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public class TableStats {
	
	public TableStats(double cardinality) {
		super();
		this.cardinality = cardinality;
		variables = new HashMap<>();
	}

	private double cardinality;
	// Map<varName, Map<uniqValues,Map<value,Frequency>>>
	Map<String, Pair<Integer, Map<Integer, Integer>>> variables;

	public double getCardinality() {
		return this.cardinality;
	}

	public void setCardinality(Number cardinality) {
		this.cardinality = ((Number) cardinality).doubleValue() ;
	}

	public Pair<Integer, Map<Integer, Integer>> getVariableStats(String varName) {
		return variables.get(varName);
	}
	
	public Set<String> getVars() {
		return variables.keySet();
	}

	public void addVariable(String varName, Pair<Integer, Map<Integer, Integer>> sVar) {
		this.variables.put(varName, sVar);
	}
}