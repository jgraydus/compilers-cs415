package parser;

import org.junit.Test;

import java.util.HashSet;
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

    final Lr1Item g1Initial = new Lr1Item(
            new Production(Symbol.goal, singletonList(g1.getStart())),
            0,
            Symbol.$
    );

    final Set<Lr1Item> g1CC0 = new HashSet<>(asList(
            g1Initial,
            new Lr1Item(new Production(list, asList(list, pair)), 0, Symbol.$),
            new Lr1Item(new Production(list, asList(list, pair)), 0, left),
            new Lr1Item(new Production(list, singletonList(pair)), 0, Symbol.$),
            new Lr1Item(new Production(list, singletonList(pair)), 0, left),
            new Lr1Item(new Production(pair, asList(left, pair, right)), 0, Symbol.$),
            new Lr1Item(new Production(pair, asList(left, pair, right)), 0, left),
            new Lr1Item(new Production(pair, asList(left, right)), 0, Symbol.$),
            new Lr1Item(new Production(pair, asList(left, right)), 0, left)
    ));

    @Test
    public void closure1() {
        final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
        final Set<Lr1Item> closure = parser.closure(new HashSet<>(singletonList(g1Initial)));
        assertEquals(g1CC0, closure);
    }

    @Test
    public void goTo1() {
        final LR1Parser<String> parser = new LR1Parser<>(g1, toSymbol1);
        final Set<Lr1Item> expected = new HashSet<>(asList(
                new Lr1Item(new Production(pair, asList(left, pair, right)), 1, Symbol.$),
                new Lr1Item(new Production(pair, asList(left, pair, right)), 1, left),
                new Lr1Item(new Production(pair, asList(left, right)), 1, Symbol.$),
                new Lr1Item(new Production(pair, asList(left, right)), 1, left),
                new Lr1Item(new Production(pair, asList(left, pair, right)), 0, right),
                new Lr1Item(new Production(pair, asList(left, right)), 0, right)
        ));
        final Set<Lr1Item> actual = parser.goTo(g1CC0, left);
        assertEquals(expected, actual);
    }
}