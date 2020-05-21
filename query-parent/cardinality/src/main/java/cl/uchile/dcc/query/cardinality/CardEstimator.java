package cl.uchile.dcc.query.cardinality;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_Path1;
import org.apache.jena.sparql.path.P_Path2;
import org.apache.jena.sparql.path.P_ZeroOrMore1;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementUnion;

import cl.uchile.dcc.dynamics.utils.MapUtils;

public class CardEstimator {

	public static void main(String[] args) {
		File path = new File(args[0]);
		File datapath = new File(args[1]);
		int mcvLenght = Integer.parseInt(args[2]);
		Map<String, TableStats> dbs = new HashMap<>();

		int tp = -1;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(datapath));
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			String line;

			while ((line = br.readLine()) != null) {
				String[] vals = line.split("\t");
				long tripleNum = Long.parseLong(vals[1]);
				int subjectsNum = Integer.parseInt(vals[2]);
				int objectsNum = Integer.parseInt(vals[3]);

				Map<Integer, Integer> subjMCV = new HashMap<Integer, Integer>();
				Map<Integer, Integer> objMCV = new HashMap<Integer, Integer>();

				if (vals.length > 4) {
					String smcv = vals[4];
					if (!smcv.equals("")) {
						Map<String, String> subjMCVStr = MapUtils.string2Map(smcv, mcvLenght);
						subjMCVStr.forEach((key, value) -> subjMCV.put(Integer.parseInt(key), Integer.parseInt(value)));
					}
				}

				if (vals.length > 5) {
					String omcv = vals[5];
					Map<String, String> objMCVStr = MapUtils.string2Map(omcv, mcvLenght);
					objMCVStr.forEach((key, value) -> {
						objMCV.put(Integer.parseInt(key), Integer.parseInt(value));
					});
				}

				TableStats predicateStat = new TableStats();
				predicateStat.setCardinality(tripleNum);

				predicateStat.addVariable("s", new ImmutablePair<>(subjectsNum, subjMCV));
				predicateStat.addVariable("o", new ImmutablePair<>(objectsNum, objMCV));

				dbs.put(vals[0], predicateStat);
				++tp;
			}

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		long tt = (long) dbs.get("<tp>").getCardinality();
		int ts = dbs.get("<tp>").getVariableStats("s").getLeft();
		int to = dbs.get("<tp>").getVariableStats("o").getLeft();

		File[] files = path.listFiles();
		Arrays.sort(files);

		for (File file : files) {
			System.out.printf(file.getName() + ",");
//			System.out.println("------------------------------------------");
			Query query;
			try {
				query = QueryFactory.read(file.getAbsolutePath());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				continue;
			}
			Stack<Element> pila = new Stack<>();
			double card = 0d;
			pila.push(query.getQueryPattern());
			Queue<IOperator> tripleOperators = new LinkedList<>();
			IOperator queryOp = null;
			while (!pila.isEmpty()) {
				Element e = (Element) pila.pop();
				if (e instanceof ElementGroup) {
					for (Element a : ((ElementGroup) e).getElements()) {
						pila.push(a);
					}
				} else if (e instanceof ElementPathBlock) {
					Iterator<TriplePath> triples = ((ElementPathBlock) e).patternElts();
					while (triples.hasNext()) {
						TriplePath t = triples.next();
						if (t.isTriple()) {
//							System.out.println(t.toString());
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

							TableStats tripleStat = new TableStats();

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
//							System.out.println(card);

							IOperator tripleOp = new TriplePattern(tripleStat, card);
							tripleOperators.add(tripleOp);
						}
					}

				} else if (e instanceof ElementFilter) {

				} else if (e instanceof ElementData) {

				} else if (e instanceof ElementBind) {

				} else if (e instanceof ElementUnion) {
					for (Element a : ((ElementUnion) e).getElements()) {
						pila.push(a);
					}
				} else if (e instanceof ElementOptional) {
					Element a = ((ElementOptional) e).getOptionalElement();
					pila.push(a);
				} else if (e instanceof ElementMinus) {
					Element a = ((ElementMinus) e).getMinusElement();
					pila.push(a);
				} else if (e instanceof ElementSubQuery) {
					Query q = ((ElementSubQuery) e).getQuery();
					pila.push(q.getQueryPattern());
				} else if (e instanceof ElementService) {

				} else {
					System.out.println(e.getClass());
					System.out.println(e);
				}
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
//				System.out.println(uniquesValues.toString());
				System.out.println(queryOp.getCardinality());
			}

		}
	}
}