package cl.uchile.dcc.query.operator;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public class TriplePattern extends Operator{

	public TriplePattern(TableStats tripleStat) {
		super(tripleStat);
	}

	@Override
	public double getCardinality() {
		// TODO Auto-generated method stub
		return this.getStats().getCardinality();
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
