package cl.uchile.dcc.dynamics.utils;

import static org.junit.Assert.assertEquals;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.lang.LangNTriples;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerFactory;
import org.junit.Test;

public class JenaParserTest {
	
	public static String III = "<http://example.org/#spiderman> <http://www.perceive.net/schemas/relationship/enemyOf> <http://example.org/#green-goblin> .";
	public static String IIL = "<http://example.org/show/218> <http://www.w3.org/2000/01/rdf-schema#label> \"That Seventies Show\" .";
	public static String OLANG = "<http://dbpedia.org/resource/Zoran_Mirkovi%C4%87> <http://dbpedia.org/property/title> \"Zoran Mirkovi\\u0107 managerial positions\"@en .";
	public static String OINT  = "<http://dbpedia.org/resource/Zoran_Mirkovi\\u0107__8> <http://dbpedia.org/ontology/numberOfGoals> \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> .";
	public static String IIB = "<http://example.org/show/218> <http://xmlns.com/foaf/0.1/knows> _:bob .";
	public static String BII = "_:alice <http://xmlns.com/foaf/0.1/knows> <http://example.org/#spiderman> .";
	public static String BIL = "_:b18834162 <http://example.org> \"new york city\" .";
	public static String BIB = "_:b18834162 <http://example.org> _:abc .";	
	public static String S =  "_:b18834162 <http://example.org> _:abc ";	
	public static String SS = "_:b18834162 <http://example.org> _:abc  ";
	public static String P =   "_:b18834162 <http://example.org> _:abc.";
	

	@Test
	public void testOLANG() {
		Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(OLANG);
		LangNTriples parser = new LangNTriples(tokenizer, RiotLib.profile(Lang.NTRIPLES, null), null) ;
		Triple t = parser.next();
		assertEquals("http://dbpedia.org/resource/Zoran_Mirkovi%C4%87", t.getSubject().getURI());
		assertEquals("http://dbpedia.org/property/title", t.getPredicate().getURI());
		assertEquals("Zoran Mirković managerial positions", t.getObject().getLiteralLexicalForm());
	}

	@Test
	public void testOINT() {
		Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(OINT);
		LangNTriples parser = new LangNTriples(tokenizer, RiotLib.profile(Lang.NTRIPLES, null), null) ;
		Triple t = parser.next();
		assertEquals("http://dbpedia.org/resource/Zoran_Mirković__8", t.getSubject().getURI());
		assertEquals("http://dbpedia.org/ontology/numberOfGoals", t.getPredicate().getURI());
		assertEquals("2", t.getObject().getLiteralLexicalForm());
	}
	
	@Test
	public void testIII() {
		Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(III);
		LangNTriples parser = new LangNTriples(tokenizer, RiotLib.profile(Lang.NTRIPLES, null), null) ;
		Triple t = parser.next();
		assertEquals("http://example.org/#spiderman", t.getSubject().getURI());
		assertEquals("http://www.perceive.net/schemas/relationship/enemyOf", t.getPredicate().getURI());
		assertEquals("http://example.org/#green-goblin", t.getObject().getURI());
	}
	
	@Test
	public void testIIB() {
		Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(IIB);
		LangNTriples parser = new LangNTriples(tokenizer, RiotLib.profile(Lang.NTRIPLES, null), null) ;
		Triple t = parser.next();
		assertEquals("http://example.org/show/218", t.getSubject().getURI());
		assertEquals("http://xmlns.com/foaf/0.1/knows", t.getPredicate().getURI());
		assertEquals(t.getObject().getBlankNodeLabel(), t.getObject().getBlankNodeLabel());
	}
}
