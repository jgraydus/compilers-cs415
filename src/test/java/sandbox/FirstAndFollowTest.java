package sandbox;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class FirstAndFollowTest {

    /* grammar:
         S -> a
     */
    @Test
    public void firstTest1() {
        final Symbol S = new Symbol.NonTerminal("S");
        final Symbol a = new Symbol.Terminal("a");
        final Production p1 = new Production(S, asList(a));
        final Grammar g = new Grammar(S, asList(p1));
        final FirstAndFollow firstAndFollow = new FirstAndFollow(g);
        final Set<Symbol> firstS = firstAndFollow.first(S);
        assertEquals(1, firstS.size());
        assertTrue(firstS.contains(a));
    }

    /* grammar:
         S -> S a | a
     */
    @Test
    public void firstTest2() {
        final Symbol S = new Symbol.NonTerminal("S");
        final Symbol a = new Symbol.Terminal("a");
        final Production p1 = new Production(S, asList(S, a));
        final Production p2 = new Production(S, asList(a));
        final Grammar g = new Grammar(S, asList(p1, p2));
        final FirstAndFollow firstAndFollow = new FirstAndFollow(g);
        final Set<Symbol> firstS = firstAndFollow.first(S);
        assertEquals(1, firstS.size());
        assertTrue(firstS.contains(a));
    }

    /* grammar:
         S -> S a | a b
     */
    @Test
    public void firstTest3() {
        final Symbol S = new Symbol.NonTerminal("S");
        final Symbol a = new Symbol.Terminal("a");
        final Symbol b = new Symbol.Terminal("b");
        final Production p1 = new Production(S, asList(S, a));
        final Production p2 = new Production(S, asList(a, b));
        final Grammar g = new Grammar(S, asList(p1, p2));
        final FirstAndFollow firstAndFollow = new FirstAndFollow(g);
        final Set<Symbol> firstS = firstAndFollow.first(S);
        assertEquals(1, firstS.size());
        assertTrue(firstS.contains(a));
    }

    /* grammar:
         S -> A
         A -> a b
    */
    @Test
    public void firstTest4() {
        final Symbol S = new Symbol.NonTerminal("S");
        final Symbol A = new Symbol.NonTerminal("A");
        final Symbol a = new Symbol.Terminal("a");
        final Symbol b = new Symbol.Terminal("b");
        final Production p1 = new Production(S, asList(A));
        final Production p2 = new Production(A, asList(a, b));
        final Grammar g = new Grammar(S, asList(p1, p2));
        final FirstAndFollow firstAndFollow = new FirstAndFollow(g);
        final Set<Symbol> firstS = firstAndFollow.first(S);
        assertEquals(1, firstS.size());
        assertTrue(firstS.contains(a));
        final Set<Symbol> firstA = firstAndFollow.first(A);
        assertEquals(1, firstA.size());
        assertTrue(firstA.contains(a));
    }

    /* grammar:
         S -> A b
         A -> a | ε
    */
    @Test
    public void firstTest5() {
        final Symbol S = new Symbol.NonTerminal("S");
        final Symbol A = new Symbol.NonTerminal("A");
        final Symbol a = new Symbol.Terminal("a");
        final Symbol b = new Symbol.Terminal("b");
        final Production p1 = new Production(S, asList(A, b));
        final Production p2 = new Production(A, asList(a));
        final Production p3 = new Production(A, asList(Symbol.ε));
        final Grammar g = new Grammar(S, asList(p1, p2, p3));
        final FirstAndFollow firstAndFollow = new FirstAndFollow(g);
        final Set<Symbol> firstS = firstAndFollow.first(S);
        assertEquals(2, firstS.size());
        assertTrue(firstS.contains(a));
        assertTrue(firstS.contains(b));
        final Set<Symbol> firstA = firstAndFollow.first(A);
        assertEquals(2, firstA.size());
        assertTrue(firstA.contains(a));
        assertTrue(firstA.contains(Symbol.ε));
    }

    /* grammar:
         S -> A B
         A -> a | ε
         B -> S | b
     */
    @Test
    public void firstTest6() {
        final Symbol S = new Symbol.NonTerminal("S");
        final Symbol A = new Symbol.NonTerminal("A");
        final Symbol B = new Symbol.NonTerminal("B");
        final Symbol a = new Symbol.Terminal("a");
        final Symbol b = new Symbol.Terminal("b");
        final Production p1 = new Production(S, asList(A, B));
        final Production p2 = new Production(A, asList(a));
        final Production p3 = new Production(A, asList(Symbol.ε));
        final Production p4 = new Production(B, asList(S));
        final Production p5 = new Production(B, asList(b));
        final Grammar g = new Grammar(S, asList(p1, p2, p3, p4, p5));
        final FirstAndFollow firstAndFollow = new FirstAndFollow(g);
        final Set<Symbol> firstS = firstAndFollow.first(S);
        assertEquals(2, firstS.size());
        assertTrue(firstS.contains(a));
        assertTrue(firstS.contains(b));
        final Set<Symbol> firstA = firstAndFollow.first(A);
        assertEquals(2, firstA.size());
        assertTrue(firstA.contains(a));
        assertTrue(firstA.contains(Symbol.ε));
        final Set<Symbol> firstB = firstAndFollow.first(B);
        assertEquals(2, firstB.size());
        assertTrue(firstB.contains(a));
        assertTrue(firstB.contains(b));
    }

    /* grammar:
         expr -> expr + term | expr - term | term
         term -> term * factor | term / factor | factor
         factor -> ( expr ) | num | name
     */
    @Test
    public void firstTest7() {
        final Symbol expr = new Symbol.NonTerminal("expr");
        final Symbol term = new Symbol.NonTerminal("term");
        final Symbol factor = new Symbol.NonTerminal("factor");
        final Symbol plus = new Symbol.Terminal("+");
        final Symbol minus = new Symbol.Terminal("-");
        final Symbol mult = new Symbol.Terminal("x");
        final Symbol div = new Symbol.Terminal("/");
        final Symbol left = new Symbol.Terminal("(");
        final Symbol right = new Symbol.Terminal(")");
        final Symbol num = new Symbol.Terminal("num");
        final Symbol name = new Symbol.Terminal("name");
        final Production p1 = new Production(expr, asList(expr, plus, term));
        final Production p2 = new Production(expr, asList(expr, minus, term));
        final Production p3 = new Production(expr, asList(term));
        final Production p4 = new Production(term, asList(term, mult, factor));
        final Production p5 = new Production(term, asList(term, div, factor));
        final Production p6 = new Production(term, asList(factor));
        final Production p7 = new Production(factor, asList(left, expr, right));
        final Production p8 = new Production(factor, asList(num));
        final Production p9 = new Production(factor, asList(name));
        final Grammar g = new Grammar(expr, asList(p1, p2, p3, p4, p5, p6, p7, p8, p9));
        final FirstAndFollow firstAndFollow = new FirstAndFollow(g);
        assertEquals(3, firstAndFollow.first(expr).size());
        assertTrue(firstAndFollow.first(expr).contains(left));
        assertTrue(firstAndFollow.first(expr).contains(num));
        assertTrue(firstAndFollow.first(expr).contains(name));
        assertEquals(3, firstAndFollow.first(term).size());
        assertTrue(firstAndFollow.first(term).contains(left));
        assertTrue(firstAndFollow.first(term).contains(num));
        assertTrue(firstAndFollow.first(term).contains(name));
    }

    /* grammar:
         expr -> term expr'
         expr' -> + term expr' | - term expr' | ε
         term -> factor term'
         term' -> * factor term' | / factor term' | ε
    */
    @Test
    public void firstTest8() {
        final Symbol expr = new Symbol.NonTerminal("expr");
        final Symbol expr_ = new Symbol.NonTerminal("expr'");
        final Symbol term = new Symbol.NonTerminal("term");
        final Symbol term_ = new Symbol.NonTerminal("term'");
        final Symbol plus = new Symbol.Terminal("+");
        final Symbol minus = new Symbol.Terminal("-");
        final Symbol mult = new Symbol.Terminal("x");
        final Symbol div = new Symbol.Terminal("/");
        final Symbol factor = new Symbol.Terminal("factor");
        final Production p1 = new Production(expr, asList(term, expr_));
        final Production p2 = new Production(expr_, asList(plus, term, expr_));
        final Production p3 = new Production(expr_, asList(minus, term, expr_));
        final Production p4 = new Production(expr_, asList(Symbol.ε));
        final Production p5 = new Production(term, asList(factor, term_));
        final Production p6 = new Production(term_, asList(mult, factor, term_));
        final Production p7 = new Production(term_, asList(div, factor, term_));
        final Production p8 = new Production(term_, asList(Symbol.ε));
        final Grammar g = new Grammar(expr, asList(p1, p2, p3, p4, p5, p6, p7, p8));
        final FirstAndFollow firstAndFollow = new FirstAndFollow(g);
        assertEquals(1, firstAndFollow.first(expr).size());
        assertTrue(firstAndFollow.first(expr).contains(factor));
        assertEquals(3, firstAndFollow.first(expr_).size());
        assertTrue(firstAndFollow.first(expr_).contains(plus));
        assertTrue(firstAndFollow.first(expr_).contains(minus));
        assertTrue(firstAndFollow.first(expr_).contains(Symbol.ε));
        assertEquals(1, firstAndFollow.first(term).size());
        assertTrue(firstAndFollow.first(term).contains(factor));
        assertEquals(3, firstAndFollow.first(term_).size());
        assertTrue(firstAndFollow.first(term_).contains(mult));
        assertTrue(firstAndFollow.first(term_).contains(div));
        assertTrue(firstAndFollow.first(term_).contains(Symbol.ε));
    }


    /* grammar:
         S -> A
         A -> B | ε
         B -> C | ε
         C -> A a | B b | c
     */
    @Test
    public void firstTest9() {
        final Symbol S = new Symbol.NonTerminal("S");
        final Symbol A = new Symbol.NonTerminal("A");
        final Symbol B = new Symbol.NonTerminal("B");
        final Symbol C = new Symbol.NonTerminal("C");
        final Symbol a = new Symbol.Terminal("a");
        final Symbol b = new Symbol.Terminal("b");
        final Symbol c = new Symbol.Terminal("c");

        final Production p1 = new Production(S, asList(A));
        final Production p2 = new Production(A, asList(B));
        final Production p3 = new Production(A, asList(Symbol.ε));
        final Production p4 = new Production(B, asList(C));
        final Production p5 = new Production(B, asList(Symbol.ε));
        final Production p6 = new Production(C, asList(A, a));
        final Production p7 = new Production(C, asList(B, b));
        final Production p8 = new Production(C, asList(c));

        final Grammar g = new Grammar(S, asList(p1, p2, p3, p4, p5, p6, p7, p8));
        final FirstAndFollow firstAndFollow = new FirstAndFollow(g);

        assertEquals(4, firstAndFollow.first(S).size());
        assertTrue(firstAndFollow.first(S).contains(a));
        assertTrue(firstAndFollow.first(S).contains(b));
        assertTrue(firstAndFollow.first(S).contains(c));
        assertTrue(firstAndFollow.first(S).contains(Symbol.ε));
    }

    /* grammar:
         S -> A B
         A -> a | ε
         B -> S | b
    */
    @Test
    public void followTest1() {
        final Symbol S = new Symbol.NonTerminal("S");
        final Symbol A = new Symbol.NonTerminal("A");
        final Symbol B = new Symbol.NonTerminal("B");
        final Symbol a = new Symbol.Terminal("a");
        final Symbol b = new Symbol.Terminal("b");
        final Production p1 = new Production(S, asList(A, B));
        final Production p2 = new Production(A, asList(a));
        final Production p3 = new Production(A, asList(Symbol.ε));
        final Production p4 = new Production(B, asList(S));
        final Production p5 = new Production(B, asList(b));
        final Grammar g = new Grammar(S, asList(p1, p2, p3, p4, p5));
        final FirstAndFollow firstAndFollow = new FirstAndFollow(g);
        assertEquals(1, firstAndFollow.follow(S).size());
        assertTrue(firstAndFollow.follow(S).contains(Symbol.$));
        assertEquals(2, firstAndFollow.follow(A).size());
        assertTrue(firstAndFollow.follow(A).containsAll(asList(a, b)));
        assertEquals(1, firstAndFollow.follow(B).size());
        assertTrue(firstAndFollow.follow(B).contains(Symbol.$));
    }

    /* grammar:
         expr -> term expr'
         expr' -> + term expr' | - term expr' | ε
         term -> factor term'
         term' -> * factor term' | / factor term' | ε
         factor -> ( expr ) | num | name
    */
    @Test
    public void followTest2() {
        final Symbol expr = new Symbol.NonTerminal("expr");
        final Symbol expr_ = new Symbol.NonTerminal("expr'");
        final Symbol term = new Symbol.NonTerminal("term");
        final Symbol term_ = new Symbol.NonTerminal("term'");
        final Symbol plus = new Symbol.Terminal("+");
        final Symbol minus = new Symbol.Terminal("-");
        final Symbol mult = new Symbol.Terminal("x");
        final Symbol div = new Symbol.Terminal("/");
        final Symbol factor = new Symbol.NonTerminal("factor");
        final Symbol left = new Symbol.Terminal("(");
        final Symbol right = new Symbol.Terminal(")");
        final Symbol num = new Symbol.Terminal("num");
        final Symbol name = new Symbol.Terminal("name");
        final Production p1 = new Production(expr, asList(term, expr_));
        final Production p2 = new Production(expr_, asList(plus, term, expr_));
        final Production p3 = new Production(expr_, asList(minus, term, expr_));
        final Production p4 = new Production(expr_, asList(Symbol.ε));
        final Production p5 = new Production(term, asList(factor, term_));
        final Production p6 = new Production(term_, asList(mult, factor, term_));
        final Production p7 = new Production(term_, asList(div, factor, term_));
        final Production p8 = new Production(term_, asList(Symbol.ε));
        final Production p9 = new Production(factor, asList(left, expr, right));
        final Production p10 = new Production(factor, asList(num));
        final Production p11 = new Production(factor, asList(name));
        final Grammar g = new Grammar(expr, asList(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11));
        final FirstAndFollow firstAndFollow = new FirstAndFollow(g);

        assertEquals(1, firstAndFollow.follow(Symbol.goal).size());
        assertTrue(firstAndFollow.follow(Symbol.goal).contains(Symbol.$));

        assertEquals(2, firstAndFollow.follow(expr).size());
        assertTrue(firstAndFollow.follow(expr).containsAll(asList(Symbol.$, right)));

        assertEquals(2, firstAndFollow.follow(expr_).size());
        assertTrue(firstAndFollow.follow(expr_).containsAll(asList(Symbol.$, right)));

        assertEquals(4, firstAndFollow.follow(term).size());
        assertTrue(firstAndFollow.follow(term).containsAll(asList(Symbol.$, plus, minus, right)));

        assertEquals(4, firstAndFollow.follow(term_).size());
        assertTrue(firstAndFollow.follow(term_).containsAll(asList(Symbol.$, plus, minus, right)));

        assertEquals(6, firstAndFollow.follow(factor).size());
        assertTrue(firstAndFollow.follow(factor).containsAll(asList(Symbol.$, plus, minus, mult, div, right)));
    }

    /* grammar:
         expr -> term expr'
         expr' -> + term expr' | - term expr' | ε
         term -> factor term'
         term' -> * factor term' | / factor term' | ε
         factor -> ( expr ) | num | name
    */
    @Test
    public void firstProduction1() {
        final Symbol expr = new Symbol.NonTerminal("expr");
        final Symbol expr_ = new Symbol.NonTerminal("expr'");
        final Symbol term = new Symbol.NonTerminal("term");
        final Symbol term_ = new Symbol.NonTerminal("term'");
        final Symbol plus = new Symbol.Terminal("+");
        final Symbol minus = new Symbol.Terminal("-");
        final Symbol mult = new Symbol.Terminal("x");
        final Symbol div = new Symbol.Terminal("/");
        final Symbol factor = new Symbol.NonTerminal("factor");
        final Symbol left = new Symbol.Terminal("(");
        final Symbol right = new Symbol.Terminal(")");
        final Symbol num = new Symbol.Terminal("num");
        final Symbol name = new Symbol.Terminal("name");
        final Production p1 = new Production(expr, asList(term, expr_));
        final Production p2 = new Production(expr_, asList(plus, term, expr_));
        final Production p3 = new Production(expr_, asList(minus, term, expr_));
        final Production p4 = new Production(expr_, asList(Symbol.ε));
        final Production p5 = new Production(term, asList(factor, term_));
        final Production p6 = new Production(term_, asList(mult, factor, term_));
        final Production p7 = new Production(term_, asList(div, factor, term_));
        final Production p8 = new Production(term_, asList(Symbol.ε));
        final Production p9 = new Production(factor, asList(left, expr, right));
        final Production p10 = new Production(factor, asList(num));
        final Production p11 = new Production(factor, asList(name));
        final Grammar g = new Grammar(expr, asList(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11));
        final FirstAndFollow firstAndFollow = new FirstAndFollow(g);

        assertEquals(3, firstAndFollow.first(p4).size());
        assertTrue(firstAndFollow.first(p4).containsAll(asList(Symbol.ε, Symbol.$, right)));

        assertEquals(5, firstAndFollow.first(p8).size());
        assertTrue(firstAndFollow.first(p8).containsAll(asList(Symbol.ε, Symbol.$, plus, minus, right)));
    }
}