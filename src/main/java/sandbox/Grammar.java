/* Joshua Graydus | February 2016 */
package sandbox;

import java.util.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Collections.emptyList;

public class Grammar {
    private final Symbol start;
    private final Map<Symbol, List<Production>> productions = new HashMap<>();
    private final Set<Symbol> nonTerminals = new HashSet<>();
    private final Set<Symbol> terminals = new HashSet<>();

    public Grammar(final Symbol start, final Collection<Production> ps) {
        this.start = start;

        // partition the productions by lhs
        for (final Production p : ps) {
            final Symbol s = p.getLhs();
            if (!productions.containsKey(s)) { productions.put(s, new LinkedList<>()); }
            productions.get(s).add(p);
        }

        // to facilitate parsing, add a rule "goal -> start $"
        productions.put(Symbol.goal, singletonList(new Production(Symbol.goal, asList(start, Symbol.$))));

        // create separate sets for terminals and nonterminals
        productions.forEach((k,v) -> {
            nonTerminals.add(k);
            v.forEach(p -> {
                nonTerminals.add(p.getLhs());
                p.getRhs().forEach(s -> {
                    if (s instanceof Symbol.Terminal) { terminals.add(s); }
                    if (s instanceof Symbol.NonTerminal) { nonTerminals.add(s); }
                });
            });
        });
    }

    /** @return the start symbol for this grammar */
    public Symbol getStart() { return start; }

    /** @return a list of productions whose lhs is the symbol s */
    public List<Production> get(final Symbol s) {
        return productions.containsKey(s) ? unmodifiableList(productions.get(s)) : emptyList();
    }

    /** @return a set containing all the nonterminal symbols in this grammar */
    public Set<Symbol> getNonTerminals() { return unmodifiableSet(nonTerminals); }

    /** @return a set containing all the terminal symbols in this grammar */
    public Set<Symbol> getTerminals() { return unmodifiableSet(terminals); }
}