package parser;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static parser.LR1Parser.Lr1Item;
import static parser.Symbol.NonTerminal;
import static parser.Symbol.Terminal;

public class LR1ParserTest {

    final Symbol list = new NonTerminal("list");
    final Symbol pair = new NonTerminal("pair");
    final Symbol left = new Terminal("(");
    final Symbol right = new Terminal(")");
    final Production p1 = new Production(list, asList(list, pair));
    final Production p2 = new Production(list, singletonList(pair));
    final Production p3 = new Production(pair, asList(left, pair, right));
    final Production p4 = new Production(pair, asList(left, right));
    private final Grammar g1 = new Grammar(list, asList(p1, p2, p3, p4));

    private final Function<String,Symbol> toSymbol1 = str -> {
        switch (str) {
            case "(": return left;
            case ")": return right;
            case "eof": return Symbol.$;
            default: throw new IllegalStateException();
        }
    };

    final Lr1Item g1Initial = makeItem(Symbol.goal, singletonList(g1.getStart()), 0, Symbol.$);

    final Set<Lr1Item> g1CC0 = new HashSet<>(asList(
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

    final Set<Lr1Item> g1CC1 = new HashSet<>(asList(
            makeItem(Symbol.goal, singletonList(list), 1, Symbol.$),
            makeItem(list, asList(list, pair), 1, Symbol.$),
            makeItem(list, asList(list, pair), 1, left),
            makeItem(pair, asList(left, pair, right), 0, Symbol.$),
            makeItem(pair, asList(left, pair, right), 0, left),
            makeItem(pair, asList(left, right), 0, Symbol.$),
            makeItem(pair, asList(left, right), 0, left)
    ));

    final Set<Lr1Item> g1CC2 = new HashSet<>(asList(
            makeItem(list, singletonList(pair), 1, Symbol.$),
            makeItem(list, singletonList(pair), 1, left)
    ));

    final Set<Lr1Item> g1CC3 = new HashSet<>(asList(
            makeItem(pair, asList(left, pair, right), 0, right),
            makeItem(pair, asList(left, pair, right), 1, Symbol.$),
            makeItem(pair, asList(left, pair, right), 1, left),
            makeItem(pair, asList(left, right), 0, right),
            makeItem(pair, asList(left, right), 1, Symbol.$),
            makeItem(pair, asList(left, right), 1, left)
    ));

    final Set<Lr1Item> g1CC4 = new HashSet<>(asList(
            makeItem(list, asList(list, pair), 2, Symbol.$),
            makeItem(list, asList(list, pair), 2, left)
    ));

    final Set<Lr1Item> g1CC5 = new HashSet<>(asList(
            makeItem(pair, asList(left, pair, right), 2, Symbol.$),
            makeItem(pair, asList(left, pair, right), 2, left)
    ));

    final Set<Lr1Item> g1CC6 = new HashSet<>(asList(
            makeItem(pair, asList(left, pair, right), 0, right),
            makeItem(pair, asList(left, pair, right), 1, right),
            makeItem(pair, asList(left, right), 0, right),
            makeItem(pair, asList(left, right), 1, right)
    ));

    final Set<Lr1Item> g1CC7 = new HashSet<>(asList(
            makeItem(pair, asList(left, right), 2, Symbol.$),
            makeItem(pair, asList(left, right), 2, left)
    ));

    final Set<Lr1Item> g1CC8 = new HashSet<>(asList(
            makeItem(pair, asList(left, pair, right), 3, Symbol.$),
            makeItem(pair, asList(left, pair, right), 3, left)
    ));

    final Set<Lr1Item> g1CC9 = new HashSet<>(singletonList(
            makeItem(pair, asList(left, pair, right), 2, right)
    ));

    final Set<Lr1Item> g1CC10 = new HashSet<>(singletonList(
            makeItem(pair, asList(left, right), 2, right)
    ));

    final Set<Lr1Item> g1CC11 = new HashSet<>(singletonList(
            makeItem(pair, asList(left, pair, right), 3, right)
    ));

    private Lr1Item makeItem(final Symbol start, final List<Symbol> derives, final int dot, final Symbol lookahead) {
        return new Lr1Item(new Production(start, derives), dot, lookahead);
    }

    @Test
    public void closure1() {
        final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
        final Set<Lr1Item> closure = parser.closure(new HashSet<>(singletonList(g1Initial)));
        assertEquals(g1CC0, closure);
    }

    @Test
    public void goTo1() {
        final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
        final Set<Lr1Item> actual = parser.goTo(g1CC0, list);
        assertEquals(g1CC1, actual);
    }

    @Test
    public void goTo2() {
        final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
        final Set<Lr1Item> actual = parser.goTo(g1CC0, pair);
        assertEquals(g1CC2, actual);
    }

    @Test
    public void goTo3() {
        final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
        final Set<Lr1Item> actual = parser.goTo(g1CC0, left);
        assertEquals(g1CC3, actual);
    }

    @Test
    public void goTo4() {
        final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
        final Set<Lr1Item> actual = parser.goTo(g1CC1, pair);
        assertEquals(g1CC4, actual);
    }

    @Test
    public void goTo5() {
        final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
        final Set<Lr1Item> actual = parser.goTo(g1CC3, pair);
        assertEquals(g1CC5, actual);
    }

    @Test
    public void goTo6() {
        final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
        final Set<Lr1Item> actual = parser.goTo(g1CC3, left);
        assertEquals(g1CC6, actual);
    }

    @Test
    public void goTo7() {
        final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
        final Set<Lr1Item> actual = parser.goTo(g1CC3, right);
        assertEquals(g1CC7, actual);
    }

    @Test
    public void goTo8() {
        final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
        final Set<Lr1Item> actual = parser.goTo(g1CC5, right);
        assertEquals(g1CC8, actual);
    }

    @Test
    public void goTo9() {
        final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
        final Set<Lr1Item> actual = parser.goTo(g1CC6, pair);
        assertEquals(g1CC9, actual);
    }

    @Test
    public void goTo10() {
        final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
        final Set<Lr1Item> actual = parser.goTo(g1CC6, right);
        assertEquals(g1CC10, actual);
    }

    @Test
    public void goTo11() {
        final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
        final Set<Lr1Item> actual = parser.goTo(g1CC9, right);
        assertEquals(g1CC11, actual);
    }

    @Test
    public void canonicalCollection1() {
        final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
        final Map<Set<Lr1Item>,Map<Symbol,Set<Lr1Item>>> cc = parser.canonicalCollection();
        cc.keySet().forEach(System.out::println);
    }
}