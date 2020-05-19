package cl.uchile.dcc.query.cardinality;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Join extends Operator{

	private IOperator child1;
	private IOperator child2;
	public Join() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public double getCardinality() {
		// Cardinality
		double card1 = child1.getCardinality();
		double card2 = child2.getCardinality();
		Set<String> vars1 = child1.getVariables();
		Set<String> vars2 = child2.getVariables();
		Set<String> intersection = new HashSet<String>(vars1); // use the copy constructor
		intersection.retainAll(vars2);
		String joinvar = intersection.iterator().next();
		int dist1 = child1.getVariableStats(joinvar).getLeft();
		int dist2 = child2.getVariableStats(joinvar).getLeft();
		double card = card1 * card2 / Math.max(dist1, dist2);
		
		// Stats
		TableStats ts = new TableStats();
		ts.setCardinality(card);		
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
		ts.addVariable(joinvar, new ImmutablePair<>(Math.min(dist1, dist2), null));
		ts.setCardinality(card);
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
