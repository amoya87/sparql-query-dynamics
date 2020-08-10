package cl.uchile.dcc.query.cardinality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpDiff;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLabel;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpList;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpProcedure;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpPropFunc;
import org.apache.jena.sparql.algebra.op.OpQuad;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import cl.uchile.dcc.query.operator.IOperator;
import cl.uchile.dcc.query.operator.Join;
import cl.uchile.dcc.query.operator.LeftJoin;
import cl.uchile.dcc.query.operator.TableStats;
import cl.uchile.dcc.query.operator.TriplePattern;
import cl.uchile.dcc.query.operator.Union;

public class OpToCardOperator extends OpVisitorBase {

	private Stack<IOperator> stack = new Stack<>();
	private Map<String, TableStats> dbs;
	private long tt;
	private int ts;
	private int to;
	private int tp;
	private int mcvLenght;

	public OpToCardOperator(Map<String, TableStats> dbs, int tp, int mcvLenght) {
		this.dbs = dbs;
		this.tp = tp;
		this.mcvLenght = mcvLenght;
		this.tt = (long) dbs.get("<tp>").getCardinality();
		this.ts = dbs.get("<tp>").getVariableStats("s").getLeft();
		this.to = dbs.get("<tp>").getVariableStats("o").getLeft();
		
	}

	public IOperator getStats() {
		return stack.pop();
	}

	@Override
	public void visit(OpBGP opBGP) {
		IOperator queryOp;
		double card = 0d;
		Queue<IOperator> tripleOperators = new LinkedList<>();
		for (Triple t : opBGP.getPattern()) {
			Node s = t.getSubject();
			Node p = t.getPredicate();
			Node o = t.getObject();
			int pt;
			int ps;
			int po;
			Map<Integer, Integer> mcvs;
			Map<Integer, Integer> mcvo;
			int mcvsTotal;
			int mcvoTotal;

			TableStats tripleStat = new TableStats(-1);

			if (p.isVariable()) {
				tripleStat.addVariable(p.getName(), new ImmutablePair<>(tp, null));
				card = tt;
				if (!s.isVariable())
					if (!o.isVariable())
						card /= tp;
					else {
						card /= ts;
						tripleStat.addVariable(o.getName(), new ImmutablePair<>(to, null));
					}
				else if (!o.isVariable()) {
					card /= to;
					tripleStat.addVariable(s.getName(), new ImmutablePair<>(ts, null));
				} else {
					tripleStat.addVariable(s.getName(), new ImmutablePair<>(ts, null));
					tripleStat.addVariable(o.getName(), new ImmutablePair<>(to, null));
				}
			} else {
				TableStats predicate = dbs.get("<" + p.getURI() + ">");
				pt = (int) predicate.getCardinality();
				ps = predicate.getVariableStats("s").getLeft();
				po = predicate.getVariableStats("o").getLeft();
				mcvo = predicate.getVariableStats("o").getRight();
				mcvs = predicate.getVariableStats("s").getRight();
				mcvsTotal = mcvs.values().stream().reduce(0, Integer::sum);
				mcvoTotal = mcvo.values().stream().reduce(0, Integer::sum);

				// TODO Eliminar esto cuando las estadísticas estén completas
				ps = (ps == 0) ? pt : ps;
				po = (po == 0) ? pt : po;

				card = pt;
				if (!s.isVariable()) {
					if (!o.isVariable())
						card = 1d;// pt;
					else { // C C V
						int val = mcvs.getOrDefault(("<" + s.getURI() + ">").hashCode(), 0);
						if (val != 0) {
							card = val;
						} else {
							card = (card - mcvsTotal) / (ps - mcvLenght);
						}
						tripleStat.addVariable(o.getName(), new ImmutablePair<>((int) card, null));
					}
				} else if (!o.isVariable()) { // V C C
					String oo = (o.isURI()) ? "<" + o.getURI() + ">" : o.getLiteralLexicalForm();
					int val = mcvo.getOrDefault(oo.hashCode(), 0);
					if (val != 0) {
						card = val;
					} else {
						card = (card - mcvoTotal) / (po - mcvLenght);
					}
					tripleStat.addVariable(s.getName(), new ImmutablePair<>((int) card, null));
				} else {
					tripleStat.addVariable(s.getName(), new ImmutablePair<>(ps, null));
					tripleStat.addVariable(o.getName(), new ImmutablePair<>(po, null));
				}
			}

			tripleStat.setCardinality(card);
			IOperator tripleOp = new TriplePattern(tripleStat);
			tripleOperators.add(tripleOp);
		}

		Set<String> seenVar = new HashSet<>();
		boolean seen = false;
		queryOp = tripleOperators.poll();
		if (queryOp != null) {
			seenVar.addAll(queryOp.getVariables());
			IOperator elem = tripleOperators.poll();
			while (elem != null) {
				for (String var : elem.getVariables()) {
					if (seenVar.contains(var)) {
						seen = true;
						break;
					}
				}
				if (seen) {
					Join joinOp = new Join();
					joinOp.setChild1(queryOp);
					joinOp.setChild2(elem);
					queryOp = joinOp;
					seenVar.addAll(elem.getVariables());
				} else {
					tripleOperators.add(elem);
				}
				seen = false;
				elem = tripleOperators.poll();
			}
		}

		stack.push(queryOp);
//		System.out.println(queryOp.getCardinality());
//		System.out.println("OpBGP");
	}

	@Override
	public void visit(OpQuadPattern quadPattern) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpQuadBlock quadBlock) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpTriple opTriple) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpQuad opQuad) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpPath opPath) {
		System.exit(1);

	}

	@Override
	public void visit(OpTable opTable) {
		int nvalues =opTable.getTable().size();
		Map<Var, List<Node>> b = new HashMap<Var, List<Node>>();
		for (Iterator<Binding> iter = opTable.getTable().rows(); iter.hasNext();) {
			Binding bindingRight = iter.next();
			for (Iterator<Var> vIter = bindingRight.vars(); vIter.hasNext();) {
				Var v = vIter.next();
				Node n = bindingRight.get(v);
				List<Node> l;
				if ( ! b.containsKey(v) ) {
					l = new ArrayList<Node>();
				} else {
					l = b.get(v);
				}
				l.add(n);
				b.put(v, l);					
			}
		}


		TableStats tripleStat = new TableStats(nvalues);
		for (Var v : b.keySet()) {
			tripleStat.addVariable(v.getName(), new ImmutablePair<>(b.get(v).size(), null));
		}
		IOperator queryOp = new TriplePattern(tripleStat);
		stack.push(queryOp);
//		System.out.println(queryOp.getCardinality());
//		System.out.println("OpTable");
	}

	@Override
	public void visit(OpNull opNull) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpProcedure opProc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpPropFunc opPropFunc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpFilter opFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpGraph opGraph) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpService opService) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpDatasetNames dsNames) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpLabel opLabel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpAssign opAssign) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpExtend opExtend) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpJoin opJoin) {
		IOperator rightParam = stack.pop();
		IOperator leftParam = stack.pop();
		Join joinOp = new Join();
		joinOp.setChild1(leftParam);
		joinOp.setChild2(rightParam);
		stack.push(joinOp);
//		System.out.println("OpJoin");
	}

	@Override
	public void visit(OpLeftJoin opLeftJoin) {
		IOperator rightParam = stack.pop();
		IOperator leftParam = stack.pop();
		LeftJoin leftJoinOp = new LeftJoin();
		leftJoinOp.setChild1(leftParam);
		leftJoinOp.setChild2(rightParam);
		stack.push(leftJoinOp);
//		System.out.println("OpLeftJoin");
	}

	@Override
	public void visit(OpUnion opUnion) {
		IOperator rightParam = stack.pop();
		IOperator leftParam = stack.pop();
		Union unionOp = new Union();
		unionOp.setChild1(leftParam);
		unionOp.setChild2(rightParam);
		stack.push(unionOp);
//		System.out.println("OpUnion");
	}

	@Override
	public void visit(OpDiff opDiff) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpMinus opMinus) {
		System.exit(1);

	}

	@Override
	public void visit(OpConditional opCondition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpSequence opSequence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpDisjunction opDisjunction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpList opList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpOrder opOrder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpProject opProject) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpReduced opReduced) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpDistinct opDistinct) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpSlice opSlice) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpGroup opGroup) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OpTopN opTop) {
		// TODO Auto-generated method stub

	}

}
