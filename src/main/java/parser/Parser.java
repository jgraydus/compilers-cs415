package parser;

import data.Either;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class Parser<T> {
    protected final Symbol start;
    protected final Map<Symbol,Integer> nonterminals = new HashMap<>();
    protected final Map<Symbol,Integer> terminals = new HashMap<>();
    protected final Function<T,Symbol> toSymbol;
    protected final FirstAndFollow firstAndFollow;

    public Parser(final Grammar g, final Function<T,Symbol> toSymbol) {
        this.toSymbol = toSymbol;
        start = g.getStart();

        // assign each nonterminal to a row
        final List<Symbol> nts = new ArrayList<>(g.getNonTerminals());
        for (int i = 0; i < nts.size(); i++) { nonterminals.put(nts.get(i), i);}

        // assign each terminal to a column
        final List<Symbol> ts = new ArrayList<>(g.getTerminals());
        for (int i = 0; i < ts.size(); i++) { terminals.put(ts.get(i), i); }

        firstAndFollow = new FirstAndFollow(g);
    }

    /** @return either a list of erroneous tokens or a full parse tree of the input */
    public abstract Either<List<T>, ParseTree<T>> parse(final List<T> tokens);
}