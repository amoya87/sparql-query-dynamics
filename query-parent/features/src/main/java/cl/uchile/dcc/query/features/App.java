package cl.uchile.dcc.query.features;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_NotExists;
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

public class App {

	public static final int HASFILTER = 1;
	public static final int HASLIMIT = 1 << 1;
	public static final int HASNEGATION = 1 << 2;
	public static final int HASUNION = 1 << 3;
	public static final int HASINFINITEPATH = 1 << 4;
	public static final int HASSUBQUERIES = 1 << 5;	
	public static final int HASOPTIONAL = 1 << 6;
	public static final int HASDISTINCT = 1 << 7;
	public static final int HASBIND = 1 << 8;
	public static final int HASHAVING = 1 << 9;
	public static final int HASGROUPBY = 1 << 10;
	public static final int HASORDERBY = 1 << 11;
	public static final int HASOFFSET = 1 << 12;
	public static final int HASVALUES = 1 << 13;
	

	public static void main(String[] args) {
		File path = new File(args[0]);
		File[] files = path.listFiles();
		Arrays.sort(files);
		
		for (File file : files) {
			System.out.printf(file.getName() + ",");
			Query query ;
			try {
				query = QueryFactory.read(file.getAbsolutePath());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				continue;
			}
			Set<Node> predicateSet = new HashSet<>();
			Set<Node> varSet = new HashSet<>();
			Stack<Element> pila = new Stack<>();
			int triplesNum = 0;
			int features = 0;
			pila.push(query.getQueryPattern());
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
						triplesNum++;
						if (t.getSubject().isVariable()) {
							varSet.add(t.getSubject());
	//						System.out.printf("V ");
						} else {
	//						System.out.printf("C ");
						}
						
						if (!t.isTriple()) {
							Set<Node> pSet = new HashSet<>();
	//						System.out.println(file.getName() + "\t" + t);
							if(getPredicatesFromPath(pSet, t.getPath()) == 1) {
								features |= HASINFINITEPATH;
							}
							predicateSet.addAll(pSet);
	//						System.out.printf("P ");
						} else {
							if (!t.getPredicate().isVariable()) {
								predicateSet.add(t.getPredicate());
	//							System.out.printf("C ");
							} else {
								varSet.add(t.getPredicate());
								predicateSet.add(new Node_Variable("V"));
	//							System.out.printf("V ");
							}
						}

						if (t.getObject().isVariable()) {
							varSet.add(t.getObject());
	//						System.out.printf("V\n");
						} else {
	//						System.out.printf("C\n");
						}
						
					}
					}
				} else if (e instanceof ElementFilter) {
					features |= HASFILTER;
					if ((((ElementFilter) e).getExpr() instanceof E_NotExists) || 
							((ElementFilter) e).getExpr() instanceof E_LogicalNot && 
							((E_LogicalNot)((ElementFilter) e).getExpr()).getArg() instanceof E_Bound) {
						features |= HASNEGATION;
					}
				} else if (e instanceof ElementData) {
					features |= HASVALUES;
				} else if (e instanceof ElementBind) {					
					features |= HASBIND;
				} else if (e instanceof ElementUnion) {
					for (Element a : ((ElementUnion) e).getElements()) {
						pila.push(a);
					}
					features |= HASUNION;
				} else if (e instanceof ElementOptional) {
					Element a = ((ElementOptional) e).getOptionalElement();
					pila.push(a);
					features |= HASOPTIONAL;
				} else if (e instanceof ElementMinus) {
					Element a = ((ElementMinus) e).getMinusElement();
					pila.push(a);
					features |= HASNEGATION;
				} else if (e instanceof ElementSubQuery) {
					Query q = ((ElementSubQuery) e).getQuery();
					pila.push(q.getQueryPattern());
					features |= HASSUBQUERIES;
				} else if (e instanceof ElementService) {
					for (Var v : query.getProjectVars()) {
						if(v.getVarName().endsWith("Label")) {
							predicateSet.add(NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"));
						} else if(v.getVarName().endsWith("AltLabel")) {
							predicateSet.add(NodeFactory.createURI("http://www.w3.org/2004/02/skos/core#altLabel"));
						} else if(v.getVarName().endsWith("Description")) {
							predicateSet.add(NodeFactory.createURI("http://schema.org/description"));
						}
					}					
					
				} else {
					System.out.println(e.getClass());
				}
			}

			if (query.hasLimit()) {
				features |= HASLIMIT;
			}
			if (query.isDistinct()) {
				features |= HASDISTINCT;
			}
			if (query.isOrdered()) {
				features |= HASORDERBY;
			}
			if (query.hasOffset()) {
				features |= HASOFFSET;
			}
			
			if (query.hasGroupBy()) {
				features |= HASGROUPBY;
			}
			if (query.hasHaving()) {
				features |= HASHAVING;
			}

			System.out.printf(triplesNum + ",");
			System.out.printf(varSet.size() + ",");
//			System.out.printf( query.getProjectVars().size() + ",");
			System.out.printf(predicateSet.size() + ",");
//			System.out.printf((((features & HASFILTER)  != 0)?1:0) + ",");
//			System.out.printf((((features & HASLIMIT) != 0)?1:0) + ",");
//			System.out.printf((((features & HASNEGATION) != 0)?1:0) + ",");
			System.out.printf((((features & HASUNION) != 0)?1:0) + ",");
//			System.out.printf((((features & HASINFINITEPATH) != 0)?1:0) + ",");
//			System.out.printf((((features & HASSUBQUERIES)  != 0)?1:0) + ",");
			System.out.printf((((features & HASOPTIONAL) != 0)?1:0) + ",");
//			System.out.printf((((features & HASDISTINCT) != 0)?1:0) + ",");
//			System.out.printf((((features & HASBIND) != 0)?1:0) + ",");	
//			System.out.printf((((features & HASHAVING) != 0)?1:0) + ",");			
//			System.out.printf((((features & HASGROUPBY) != 0)?1:0) + ",");
//			System.out.printf((((features & HASORDERBY) != 0)?1:0) + ",");
//			System.out.printf((((features & HASOFFSET) != 0)?1:0) + ",");
			System.out.println(((features & HASVALUES) != 0)?1:0);
					
			
//			System.out.println(predicateSet);
			
//			break;


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
