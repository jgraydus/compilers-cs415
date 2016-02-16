package sandbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LL1 {

    public final Map<Symbol,Integer> nonterminals = new HashMap<>();
    public final Map<Symbol,Integer> terminals = new HashMap<>();
    public final Production[][] table;

    public LL1(final Grammar g) {

        final int height = g.getNonTerminals().size();
        final int width = g.getTerminals().size();

        // could use Map<Symbol, Map<Symbol, Production>> instead
        table = new Production[height][width];

        // assign each nonterminal to a row
        for (int i=1; i<= g.getNonTerminals().size(); i++) {
            final List<Symbol> nts = new ArrayList<>(g.getNonTerminals());
            nonterminals.put(nts.get(i), i);
        }

        // assign each terminal to a column
        for (int i=1; i<= g.getTerminals().size(); i++) {
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

}