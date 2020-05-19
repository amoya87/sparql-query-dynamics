package cl.uchile.dcc.query.cardinality;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
						Map<String, String> subjMCVStr = MapUtils.string2Map(smcv);
						subjMCVStr.forEach((key, value) -> subjMCV.put(Integer.parseInt(key), Integer.parseInt(value)));
					}
				}

				Pair<Integer, Map<Integer, Integer>> sVar = new ImmutablePair<>(subjectsNum, subjMCV);
				

				if (vals.length > 5) {
					String omcv = vals[5];
					Map<String, String> objMCVStr = MapUtils.string2Map(omcv);
					objMCVStr.forEach((key, value) -> objMCV.put(Integer.parseInt(key), Integer.parseInt(value)));
				}

				Pair<Integer, Map<Integer, Integer>> oVar = new ImmutablePair<>(objectsNum, objMCV);
				
				TableStats predicateStat = new TableStats();
				predicateStat.setCardinality(tripleNum);

				predicateStat.addVariable("s", sVar);
				predicateStat.addVariable("o", oVar);

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

		File[] files = path.listFiles();
		Arrays.sort(files);

		for (File file : files) {
//			System.out.printf(file.getName() + ",");
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
			int btriplesCount = 0;
			pila.push(query.getQueryPattern());
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
							btriplesCount++;
//							System.out.println(t.toString());
							Node s = t.getSubject();							
							Node p = t.getPredicate();
							Node o = t.getObject();
							long tt = (long) dbs.get("<tp>").getCardinality();
							int ts = dbs.get("<tp>").getVariableStats("s").getLeft();							
							int to = dbs.get("<tp>").getVariableStats("o").getLeft();							
							int pt;
							int ps;
							int po;

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
								pt = (int) dbs.get("<" + p.getURI() + ">").getCardinality();
								ps = dbs.get("<" + p.getURI() + ">").getVariableStats("s").getLeft();
								po = dbs.get("<" + p.getURI() + ">").getVariableStats("o").getLeft();
								
								//TODO Eliminar esto cuando las estadísticas estén completas
								ps = (ps == 0)? pt : ps;
								po = (po == 0)? pt : po;
								
								card = pt;
								if (!s.isVariable())
									if (!o.isVariable())
										card = 1d;// pt;
									else {
										card /= ps;
										tripleStat.addVariable(o.getName(), new ImmutablePair<>(po, null));
									}
								else if (!o.isVariable()) {
									card /= po;
									tripleStat.addVariable(s.getName(), new ImmutablePair<>(ps, null));
								} else {
									tripleStat.addVariable(s.getName(), new ImmutablePair<>(ps, null));
									tripleStat.addVariable(o.getName(), new ImmutablePair<>(po, null));
								}
							}
//							System.out.println(card);
							
							IOperator tripleOp = new TriplePattern(tripleStat, card);

							if (btriplesCount == 1) {
								queryOp = tripleOp;
							} else {
								Join joinOp = new Join();
								joinOp.setChild1(queryOp);
								joinOp.setChild2(tripleOp);
								queryOp = joinOp;
							}
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
//			System.out.println(uniquesValues.toString());
			System.out.println(queryOp.getCardinality());
		}
	}

	private static int getPredicatesFromPath(Set<Node> pSet, Path path) {
		int inf = 0;
		Stack<Path> stack = new Stack<>();

		stack.push(path);
		while (!stack.isEmpty()) {
			Path p = stack.pop();
			if (p instanceof P_Path0) {
				if (!((P_Path0) p).getNode().isVariable()) {
					pSet.add(((P_Path0) p).getNode());
				}
				pSet.add(((P_Path0) p).getNode());
			} else if (p instanceof P_Path1) {
				stack.push((Path) ((P_Path1) p).getSubPath());
				if (p instanceof P_OneOrMore1 || p instanceof P_ZeroOrMore1) {
					inf = 1;
				}
			} else if (p instanceof P_Path2) {
				stack.push((Path) ((P_Path2) p).getLeft());
				stack.push((Path) ((P_Path2) p).getRight());
			}
		}
		return inf;
	}
}