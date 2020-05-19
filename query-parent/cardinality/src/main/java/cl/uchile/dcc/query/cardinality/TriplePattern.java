package cl.uchile.dcc.query.cardinality;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public class TriplePattern extends Operator{
	
	private double cardinality;

	public TriplePattern(TableStats tripleStat, double card) {
		super(tripleStat);
		cardinality = card;
	}

	@Override
	public double getCardinality() {
		// TODO Auto-generated method stub
		return cardinality;
	}

	@Override
	public Set<String> getVariables() {
		return stats.getVars();
	}

	@Override
	public Pair<Integer, Map<Integer, Integer>> getVariableStats(String varName) {
		return stats.getVariableStats(varName);
	}

}
