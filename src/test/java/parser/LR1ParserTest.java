package parser;

import data.Either;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static parser.LR1Parser.*;
import static parser.Symbol.NonTerminal;
import static parser.Symbol.Terminal;

@RunWith(Enclosed.class)
public class LR1ParserTest {

    @RunWith(JUnit4.class)
    public static class ParenthesisGrammar {
        final Symbol list = new NonTerminal("list");
        final Symbol pair = new NonTerminal("pair");
        final Symbol left = new Terminal("(");
        final Symbol right = new Terminal(")");
        final Production p1 = new Production(list, asList(list, pair));
        final Production p2 = new Production(list, singletonList(pair));
        final Production p3 = new Production(pair, asList(left, pair, right));
        final Production p4 = new Production(pair, asList(left, right));
        private final Grammar g1 = new Grammar(list, asList(p1, p2, p3, p4));

        private final Function<String, Symbol> toSymbol1 = str -> {
            switch (str) {
                case "(":
                    return left;
                case ")":
                    return right;
                case "eof":
                    return Symbol.$;
                default:
                    throw new IllegalStateException();
            }
        };

        final LR1Item g1Initial = makeItem(Symbol.goal, singletonList(g1.getStart()), 0, Symbol.$);

        final Set<LR1Item> g1CC0 = new HashSet<>(asList(
                g1Initial,
                makeItem(list, asList(list, pair), 0, Symbol.$),
                makeItem(list, asList(list, pair), 0, left),
                makeItem(list, singletonList(pair), 0, Symbol.$),
                makeItem(list, singletonList(pair), 0, left),
                makeItem(pair, asList(left, pair, right), 0, Symbol.$),
                makeItem(pair, asList(left, pair, right), 0, left),
                makeItem(pair, asList(left, right), 0, Symbol.$),
                makeItem(pair, asList(left, right), 0, left)
        ));

        final Set<LR1Item> g1CC1 = new HashSet<>(asList(
                makeItem(Symbol.goal, singletonList(list), 1, Symbol.$),
                makeItem(list, asList(list, pair), 1, Symbol.$),
                makeItem(list, asList(list, pair), 1, left),
                makeItem(pair, asList(left, pair, right), 0, Symbol.$),
                makeItem(pair, asList(left, pair, right), 0, left),
                makeItem(pair, asList(left, right), 0, Symbol.$),
                makeItem(pair, asList(left, right), 0, left)
        ));

        final Set<LR1Item> g1CC2 = new HashSet<>(asList(
                makeItem(list, singletonList(pair), 1, Symbol.$),
                makeItem(list, singletonList(pair), 1, left)
        ));

        final Set<LR1Item> g1CC3 = new HashSet<>(asList(
                makeItem(pair, asList(left, pair, right), 0, right),
                makeItem(pair, asList(left, pair, right), 1, Symbol.$),
                makeItem(pair, asList(left, pair, right), 1, left),
                makeItem(pair, asList(left, right), 0, right),
                makeItem(pair, asList(left, right), 1, Symbol.$),
                makeItem(pair, asList(left, right), 1, left)
        ));

        final Set<LR1Item> g1CC4 = new HashSet<>(asList(
                makeItem(list, asList(list, pair), 2, Symbol.$),
                makeItem(list, asList(list, pair), 2, left)
        ));

        final Set<LR1Item> g1CC5 = new HashSet<>(asList(
                makeItem(pair, asList(left, pair, right), 2, Symbol.$),
                makeItem(pair, asList(left, pair, right), 2, left)
        ));

        final Set<LR1Item> g1CC6 = new HashSet<>(asList(
                makeItem(pair, asList(left, pair, right), 0, right),
                makeItem(pair, asList(left, pair, right), 1, right),
                makeItem(pair, asList(left, right), 0, right),
                makeItem(pair, asList(left, right), 1, right)
        ));

        final Set<LR1Item> g1CC7 = new HashSet<>(asList(
                makeItem(pair, asList(left, right), 2, Symbol.$),
                makeItem(pair, asList(left, right), 2, left)
        ));

        final Set<LR1Item> g1CC8 = new HashSet<>(asList(
                makeItem(pair, asList(left, pair, right), 3, Symbol.$),
                makeItem(pair, asList(left, pair, right), 3, left)
        ));

        final Set<LR1Item> g1CC9 = new HashSet<>(singletonList(
                makeItem(pair, asList(left, pair, right), 2, right)
        ));

        final Set<LR1Item> g1CC10 = new HashSet<>(singletonList(
                makeItem(pair, asList(left, right), 2, right)
        ));

        final Set<LR1Item> g1CC11 = new HashSet<>(singletonList(
                makeItem(pair, asList(left, pair, right), 3, right)
        ));

        private LR1Item makeItem(final Symbol start, final List<Symbol> derives, final int dot, final Symbol lookahead) {
            return new LR1Item(new Production(start, derives), dot, lookahead);
        }

        @Test
        public void closure1() {
            final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
            final Set<LR1Item> closure = parser.closure(new HashSet<>(singletonList(g1Initial)));
            assertEquals(g1CC0, closure);
        }

        @Test
        public void goTo1() {
            final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
            final Set<LR1Item> actual = parser.goTo(g1CC0, list);
            assertEquals(g1CC1, actual);
        }

        @Test
        public void goTo2() {
            final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
            final Set<LR1Item> actual = parser.goTo(g1CC0, pair);
            assertEquals(g1CC2, actual);
        }

        @Test
        public void goTo3() {
            final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
            final Set<LR1Item> actual = parser.goTo(g1CC0, left);
            assertEquals(g1CC3, actual);
        }

        @Test
        public void goTo4() {
            final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
            final Set<LR1Item> actual = parser.goTo(g1CC1, pair);
            assertEquals(g1CC4, actual);
        }

        @Test
        public void goTo5() {
            final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
            final Set<LR1Item> actual = parser.goTo(g1CC3, pair);
            assertEquals(g1CC5, actual);
        }

        @Test
        public void goTo6() {
            final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
            final Set<LR1Item> actual = parser.goTo(g1CC3, left);
            assertEquals(g1CC6, actual);
        }

        @Test
        public void goTo7() {
            final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
            final Set<LR1Item> actual = parser.goTo(g1CC3, right);
            assertEquals(g1CC7, actual);
        }

        @Test
        public void goTo8() {
            final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
            final Set<LR1Item> actual = parser.goTo(g1CC5, right);
            assertEquals(g1CC8, actual);
        }

        @Test
        public void goTo9() {
            final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
            final Set<LR1Item> actual = parser.goTo(g1CC6, pair);
            assertEquals(g1CC9, actual);
        }

        @Test
        public void goTo10() {
            final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
            final Set<LR1Item> actual = parser.goTo(g1CC6, right);
            assertEquals(g1CC10, actual);
        }

        @Test
        public void goTo11() {
            final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
            final Set<LR1Item> actual = parser.goTo(g1CC9, right);
            assertEquals(g1CC11, actual);
        }

        @Test
        public void canonicalCollection1() {
            final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
            final CanonicalCollection cc = parser.canonicalCollection();

            final Collection<Set<LR1Item>> expectedCC = new HashSet<>(
                    asList(g1CC0, g1CC1, g1CC2, g1CC3, g1CC4, g1CC5, g1CC6, g1CC7, g1CC8, g1CC9, g1CC10, g1CC11)
            );
            final Collection<Set<LR1Item>> actualCC = cc.getSets().values();

            assertTrue(expectedCC.containsAll(actualCC));
            assertTrue(actualCC.containsAll(expectedCC));
        }

        @Test
        public void tables1() {
            final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);

            final ParseTables tables = parser.buildParseTables();

            //actionTable.forEach((k,v) -> System.out.println(k + ": " + v));
            //System.out.println();
            //gotoTable.forEach((k,v) -> System.out.println(k + ": " + v));

            // TODO assert something
            // cci sets are given different numbers. hard to test against expected results
        }

        @Test
        public void parse1() {
            final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
            ParseTree<String> tree = parser.parse(asList("(", "(", ")", ")", "eof")).getRight().get();
            //System.out.println();
            //System.out.println(tree);
            // TODO
        }
    }

    @RunWith(JUnit4.class)
    public static class ExpressionGrammar {

        final Symbol term = new NonTerminal("term");
        final Symbol factor = new NonTerminal("factor");
        final Symbol number = new Terminal("number");
        final Symbol value = new NonTerminal("value");
        final Symbol id = new Terminal("id");
        final Symbol plus = new Terminal("+");
        final Symbol times = new Terminal("*");

        final Production p21 = new Production(term, asList(term, plus, factor));
        final Production p22 = new Production(term, singletonList(factor));
        final Production p23 = new Production(factor, asList(factor, times, value));
        final Production p24 = new Production(factor, singletonList(value));
        final Production p25 = new Production(value, singletonList(number));
        final Production p26 = new Production(value, singletonList(id));

        final Grammar g2 = new Grammar(term, asList(p21, p22, p23, p24, p25, p26));

        final Function<String, Symbol> toSymbol2 = str -> {
            switch (str) {
                case "term":
                    return term;
                case "factor":
                    return factor;
                case "number":
                    return number;
                case "value":
                    return value;
                case "id":
                    return id;
                case "plus":
                    return plus;
                case "times":
                    return times;
                case "eof":
                    return Symbol.$;
                default:
                    throw new RuntimeException();
            }
        };

        @Test
        public void test001() {
            LR1Parser<String> parser = new LR1Parser<>(g2, toSymbol2);
            Either<List<String>,ParseTree<String>> result = parser
                    .parse(asList("id", "times", "number", "plus", "number", "eof"));
            assertTrue(result.getRight().isPresent());
            //System.out.println();
            //System.out.println(result.getRight().get());
        }
    }

    @RunWith(JUnit4.class)
    public static class AGrammar {
        final Symbol A = new NonTerminal("A");
        final Symbol a = new Terminal("a");

        final Production p1 = new Production(A, asList(A, a));
        final Production p2 = new Production(A, singletonList(Symbol.epsilon));

        final Grammar g = new Grammar(A, asList(p1, p2));

        final Function<String,Symbol> toSymbol = str -> {
            switch (str) {
                case "a": return a;
                case "eof": return Symbol.$;
                default: throw new IllegalStateException();
            }
        };

        @Test
        public void test() {
            final LR1Parser<String> parser = new LR1Parser<>(g, toSymbol);
            parser.parse(asList("a","a","a","eof"));
        }
    }
}