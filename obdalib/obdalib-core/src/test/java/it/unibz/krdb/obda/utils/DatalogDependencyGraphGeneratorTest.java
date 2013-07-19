package it.unibz.krdb.obda.utils;

import java.util.ArrayList;
import java.util.List;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import junit.framework.TestCase;

public class DatalogDependencyGraphGeneratorTest extends TestCase {

	OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	public void testOneRule() {
		// ans1(x, y) :- Join(LeftJoin(ans2(x), ans3(y)), ans4(x,y))
		DatalogProgram program = fac.getDatalogProgram();
		Atom head = fac.getAtom(fac.getPredicate("ans1", 2),
				fac.getVariable("x"), fac.getVariable("y"));

		Atom atom1 = fac.getAtom(fac.getPredicate("ans2", 2),
				fac.getVariable("x"), fac.getVariable("y"));
		Atom atom2 = fac.getAtom(fac.getPredicate("ans3", 1),
				fac.getVariable("x"));
		Atom atom3 = fac.getAtom(fac.getPredicate("ans4", 2),
				fac.getVariable("x"), fac.getVariable("y"));

		Atom bodyAtom = fac.getAtom(fac.getJoinPredicate(),
				fac.getAtom(fac.getJoinPredicate(), atom1, atom2), atom3);

		CQIE rule1 = fac.getCQIE(head, bodyAtom);
		program.appendRule(rule1);

		DatalogDependencyGraphGenerator g = new DatalogDependencyGraphGenerator(
				program);

		assertEquals(1, g.getRuleIndex().keySet().size());
		assertEquals(4, g.getPredicateDependencyGraph().vertexSet().size());
		assertEquals(3, g.getPredicateDependencyGraph().edgeSet().size());
		assertEquals(0, g.getRuleDependencyGraph().edgeSet().size());
		assertEquals(3, g.getExtensionalPredicates().size());

	}

	public void testMoreRules01() {
		// ans1(x, y) :- Join(LeftJoin(ans2(x), ans3(y)), ans4(x,y))
		DatalogProgram program = fac.getDatalogProgram();
		Atom a = fac.getAtom(fac.getClassPredicate("A"), fac.getVariable("x"));
		Atom R = fac.getAtom(fac.getObjectPropertyPredicate("R"),
				fac.getVariable("x"), fac.getVariable("y"));
		Atom lj = fac.getAtom(fac.getLeftJoinPredicate(), a, R);
		Atom head = fac.getAtom(fac.getPredicate("ans1", 2),
				fac.getVariable("x"), fac.getVariable("y"));

		Atom atom1 = fac.getAtom(fac.getPredicate("ans2", 2),
				fac.getVariable("x"), fac.getVariable("y"));
		Atom atom2 = fac.getAtom(fac.getPredicate("ans3", 1),
				fac.getVariable("x"));
		Atom atom3 = fac.getAtom(fac.getPredicate("ans4", 2),
				fac.getVariable("x"), fac.getVariable("y"));

		Atom bodyAtom = fac.getAtom(fac.getJoinPredicate(),
				fac.getAtom(fac.getJoinPredicate(), atom1, atom2), atom3);

		CQIE rule1 = fac.getCQIE(head, bodyAtom);
		program.appendRule(rule1);

		// ans2(x) :- T2(x)
		Atom ans2 = fac.getAtom(fac.getPredicate("ans2", 1),
				fac.getVariable("x"));
		Atom t2 = fac.getAtom(fac.getPredicate("T2", 1), fac.getVariable("x"));

		CQIE rule2 = fac.getCQIE(ans2, t2);
		program.appendRule(rule2);

		// ans3(x) :- T1(x)
		Atom ans3 = fac.getAtom(fac.getPredicate("ans3", 1),
				fac.getVariable("x"));
		Atom t1 = fac.getAtom(fac.getPredicate("T1", 1), fac.getVariable("x"));
		CQIE rule3 = fac.getCQIE(ans3, t1);
		program.appendRule(rule3);

		// ans4(x,y) :- T3(x, y)
		Atom ans4 = fac.getAtom(fac.getPredicate("ans4", 2),
				fac.getVariable("x"), fac.getVariable("y"));
		Atom t3 = fac.getAtom(fac.getPredicate("T3", 2), fac.getVariable("x"),
				fac.getVariable("y"));
		CQIE rule4 = fac.getCQIE(ans4, t3);
		program.appendRule(rule4);

		DatalogDependencyGraphGenerator g = new DatalogDependencyGraphGenerator(
				program);

		// {rule1, rule2, rule3, rule4}
		assertEquals(4, g.getRuleIndex().keySet().size());

		// {ans1, ans2, ans3, ans4, t1, t2, t3}
		assertEquals(7, g.getPredicateDependencyGraph().vertexSet().size());

		// [=(ans1,ans4), =(ans1,ans2), =(ans1,ans3), =(ans2,T2), =(ans3,T1),
		// =(ans4,T3)]
		assertEquals(6, g.getPredicateDependencyGraph().edgeSet().size());

		// { (rule1, rule2), (rule1, rule3), (rule1, rule4)}
		assertEquals(3, g.getRuleDependencyGraph().edgeSet().size());
		
		assertEquals(3, g.getExtensionalPredicates().size());
	}

	public void testMoreRules02(){
		// ans1(x, y) :- Join(LeftJoin(ans2(x), ans3(y)), ans4(x,y))
		DatalogProgram program = fac.getDatalogProgram();
		Atom a = fac.getAtom(fac.getClassPredicate("A"), fac.getVariable("x"));
		Atom R = fac.getAtom(fac.getObjectPropertyPredicate("R"), fac.getVariable("x"), fac.getVariable("y"));
		Atom lj = fac.getAtom(fac.getLeftJoinPredicate(), a, R);
		Atom head = fac.getAtom(fac.getPredicate("ans1", 2), fac.getVariable("x"), fac.getVariable("y"));
		
		Atom atom1 = fac.getAtom(fac.getPredicate("ans2", 2), fac.getVariable("x"), fac.getVariable("y"));
		Atom atom2 = fac.getAtom(fac.getPredicate("ans3", 1), fac.getVariable("x"));
		Atom atom3 = fac.getAtom(fac.getPredicate("ans4", 2), fac.getVariable("x"), fac.getVariable("y"));
		
		Atom bodyAtom = fac.getAtom(fac.getJoinPredicate(), 
				fac.getAtom(fac.getJoinPredicate(), atom1, atom2), atom3);
		
		CQIE rule1 = fac.getCQIE(head, bodyAtom);
		program.appendRule(rule1);
		
		// ans2(x) :- T2(x)
		Atom ans2 = fac.getAtom(fac.getPredicate("ans2", 1), fac.getVariable("x"));
		Atom t2 = fac.getAtom(fac.getPredicate("T2", 1), fac.getVariable("x"));
		
		CQIE rule2 = fac.getCQIE(ans2, t2);
		program.appendRule(rule2);
		
		
		// ans3(x) :- T1(x)
		Atom ans3 = fac.getAtom(fac.getPredicate("ans3", 1), fac.getVariable("x"));
		Atom t1 = fac.getAtom(fac.getPredicate("T1", 1), fac.getVariable("x"));
		CQIE rule3 = fac.getCQIE(ans3, t1);
		program.appendRule(rule3);
		
		
		// ans4(x,y) :- T3(x, y), ans5(y)
		Atom ans4 = fac.getAtom(fac.getPredicate("ans4", 2), fac.getVariable("x"), fac.getVariable("y"));
		Atom ans5 = fac.getAtom(fac.getPredicate("ans5", 1),  fac.getVariable("y"));
		Atom t3 = fac.getAtom(fac.getPredicate("T3", 2), fac.getVariable("x"), fac.getVariable("y"));
		List<Function> body = new ArrayList<Function>();
		body.add(t3);
		body.add(ans5);
		CQIE rule4 = fac.getCQIE(ans4, body);
		program.appendRule(rule4);
		
		// ans5(f(x)) :- T1(x)
		Atom ans5_f_x = fac.getAtom(fac.getPredicate("ans5", 1),
				fac.getAtom(fac.getPredicate("f", 1), fac.getVariable("x")));
		CQIE rule5 = fac.getCQIE(ans5_f_x, t1);
		program.appendRule(rule5);
		
		DatalogDependencyGraphGenerator g = new DatalogDependencyGraphGenerator(program);
		
		// {rule1, rule2, rule3, rule4, rule5}
		assertEquals(5, g.getRuleIndex().keySet().size());
		
		// {ans1, ans2, ans3, ans4, ans5, t1, t2, t3}
		assertEquals(8, g.getPredicateDependencyGraph().vertexSet().size());
		
		// [(ans1,ans4), (ans1,ans2), (ans1,ans3), (ans2,T2), (ans3,T1), (ans4,T3), (ans4, ans5), (ans5, t1)]
		assertEquals(8, g.getPredicateDependencyGraph().edgeSet().size());
		
		// [ (rule1, rule2), (rule1, rule3), (rule1, rule4), (rule4, rule5) ]
		assertEquals(4, g.getRuleDependencyGraph().edgeSet().size());
		
		assertEquals(3, g.getExtensionalPredicates().size());
	}
}
