package sandbox;

import java.text.ParseException;
import java.util.*;
import java.util.function.Function;

public class LL1 {

    private final Symbol start;
    private final Map<Symbol,Integer> nonterminals = new HashMap<>();
    private final Map<Symbol,Integer> terminals = new HashMap<>();
    private final Production[][] table;

    public LL1(final Grammar g) {
        start = g.getStart();

        final int height = g.getNonTerminals().size();
        final int width = g.getTerminals().size();

        // could use Map<Symbol, Map<Symbol, Production>> instead
        table = new Production[height][width];

        // assign each nonterminal to a row
        for (int i=0; i< g.getNonTerminals().size(); i++) {
            final List<Symbol> nts = new ArrayList<>(g.getNonTerminals());
            nonterminals.put(nts.get(i), i);
        }

        // assign each terminal to a column
        for (int i=0; i< g.getTerminals().size(); i++) {
            final List<Symbol> nts = new ArrayList<>(g.getTerminals());
            terminals.put(nts.get(i), i);
        }

        final FirstAndFollow faf = new FirstAndFollow(g);

        g.getNonTerminals().forEach(nt -> {
            g.get(nt).forEach(p -> {
                faf.first(p).stream()
                        .filter(Symbol::isTerminal)
                        .forEach(t -> {
                            final int row = nonterminals.get(nt);
                            final int col = terminals.get(t);
                            if (table[row][col] == null) {
                                table[row][col] = p;
                            } else {
                                throw new IllegalStateException("not an LL(1) grammar");
                            }
                        });
            });
        });
    }

    // TODO build parse tree
    public <T> ParseTree<T> parse(List<T> tokens, Function<T,Symbol> toSymbol) {

        final Stack<Symbol> parseStack = new Stack<>();
        parseStack.push(start);

        final Iterator<T> iter = tokens.iterator();
        T nextT = iter.next();

        while (true) {
            //System.out.println("parseStack="+parseStack);
            //System.out.println("nextT="+nextT);

            final Symbol nextS = nextT == null ? Symbol.$ : toSymbol.apply(nextT);

            if (nextS.equals(Symbol.$) && parseStack.isEmpty()) { break; }

            final Symbol top = parseStack.peek();

            if (top.isTerminal()) {
                if (top.equals(nextS)) {
                    nextT = iter.hasNext() ? iter.next() : null;
                    parseStack.pop();
                } else {
                    throw new IllegalStateException(top + " != " + nextS);
                }
            } else {
                final int row = nonterminals.get(top);
                final int col = terminals.get(nextS);
                final Production p = table[row][col];
                if (p == null) { throw new IllegalStateException(nextT.toString()); }
                parseStack.pop();
                final Stack<Symbol> tmp = new Stack<>();
                p.getRhs().stream()
                        .filter(s -> s != Symbol.ε) // don't push ε onto parse stack
                        .forEach(tmp::push);
                while (!tmp.isEmpty()) { parseStack.push(tmp.pop()); }
            }
        }

        return null; // TODO
    }
}