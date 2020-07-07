package cl.uchile.dcc.query.operator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Union extends Operator{

	private IOperator child1;
	private IOperator child2;
	public Union() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public double getCardinality() {
		// Cardinality
		double card1 = child1.getCardinality();
		double card2 = child2.getCardinality();
		double card = card1 + card2;
		
		// Stats
		
		TableStats ts = new TableStats(card);

		Set<String> vars1 = child1.getVariables();
		Set<String> vars2 = child2.getVariables();
		Set<String> intersection = new HashSet<String>(vars1); // use the copy constructor
		intersection.retainAll(vars2);
		
		for (String var : intersection) {
			int dist1 = child1.getVariableStats(var).getLeft();
			int dist2 = child2.getVariableStats(var).getLeft();
			ts.addVariable(var, new ImmutablePair<>(dist1 + dist2, null));
		}
		
		Set<String> noChanged1 = new HashSet<String>(vars1);
		noChanged1.removeAll(intersection);
		for (String var : noChanged1) {
			ts.addVariable(var, child1.getVariableStats(var));
		}
		Set<String> noChanged2 = new HashSet<String>(vars2);
		noChanged2.removeAll(intersection);
		for (String var : noChanged2) {
			ts.addVariable(var, child2.getVariableStats(var));
		}
		
		this.stats = ts;
		return card;
	}
	public IOperator getChild1() {
		return this.child1;
	}
	public void setChild1(IOperator child1) {
		this.child1 = child1;
	}
	public IOperator getChild2() {
		return this.child2;
	}
	public void setChild2(IOperator child2) {
		this.child2 = child2;
	}
	@Override
	public Set<String> getVariables() {
		return this.stats.getVars();
	}
	@Override
	public Pair<Integer, Map<Integer, Integer>> getVariableStats(String varName) {
		return this.stats.getVariableStats(varName);
	}

}
