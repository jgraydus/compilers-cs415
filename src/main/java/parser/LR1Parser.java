/* Joshua Graydus | March 2016 */
package parser;

import data.Either;

import java.util.*;
import java.util.function.Function;

// TODO
public class LR1Parser<T> extends Parser<T> {

    private final Grammar g;

    public LR1Parser(final Grammar g, final Function<T,Symbol> toSymbol) {
        super(g, toSymbol);
        this.g = g;
    }

    @Override
    public Either<List<T>,ParseTree<T>> parse(final List<T> tokens) {
        return null; // TODO
    }

    private ParseTree<T> parse(final List<T> tokens, final List<T> errors) {

        return null; // TODO
    }

    /* compute the closure of a set of LR1 items  TODO this is terribly inefficient and can definitely be improved */
    Set<Lr1Item> closure(final Set<Lr1Item> items) {
        final Set<Lr1Item> result = new HashSet<>();
        // the item itself is in the closure
        result.addAll(items);
        while (true) {
            final Set<Lr1Item> updates = new HashSet<>();
            // for each of the items in the current set of results
            result.forEach(i -> {
                // get the sentence after the dot
                final List<Symbol> unseen = i.getSymbolsAfterDot();
                if (!unseen.isEmpty()) {
                    // if the sentence is not empty and the first symbol is a non-terminal
                    final Symbol s = unseen.get(0);
                    if (!s.isTerminal()) {
                        // append the item's lookahead to the sentence
                        unseen.add(i.getLookAhead());
                        // and calculate the first of the sentence minus the leading non-terminal
                        final Set<Symbol> first = first(unseen.subList(1, unseen.size()));
                        // for every production rule deriving from the non-terminal
                        g.get(s).forEach(p -> {
                            // and every terminal in the previously computed first set
                            first.forEach(b -> {
                                // add a new item
                                updates.add(new Lr1Item(p, 0, b));
                            });
                        });
                    }
                }
            });
            // stop when no new items are generated
            if (result.containsAll(updates)) { break; }
            else { result.addAll(updates); }
        }
        return result;
    }

    /* compute the first set for a string of symbols */
    Set<Symbol> first(final List<Symbol> symbols) {
        final Set<Symbol> result = new HashSet<>();
        // add the first sets of each individual symbol until a set does not contain epsilon
        for (final Symbol symbol : symbols) {
            final Set<Symbol> tmp = firstAndFollow.first(symbol);
            result.addAll(tmp);
            if (!tmp.contains(Symbol.epsilon)) { break; }
        }
        result.remove(Symbol.epsilon);
        return result;
    }

    Set<Lr1Item> goTo(final Set<Lr1Item> items, final Symbol symbol) {
        final Set<Lr1Item> result = new HashSet<>();
        items.forEach(item -> {
            final List<Symbol> unseen = item.getSymbolsAfterDot();
            if (!unseen.isEmpty() && unseen.get(0).equals(symbol)) {
                result.add(new Lr1Item(item.production, item.dotPosition+1, item.lookAhead));
            }
        });
        return closure(result);
    }

    static class Lr1Item {
        private final Production production;
        private final int dotPosition;
        private final Symbol lookAhead;

        public Lr1Item(final Production production,
                       final int dotPosition,
                       final Symbol lookAhead) {
            this.production = production;
            this.dotPosition = dotPosition;
            this.lookAhead = lookAhead;
        }

        /** @return the string of symbols after the dot */
        public List<Symbol> getSymbolsAfterDot() {
            final List<Symbol> rhs = production.getRhs();
            return new LinkedList<>(rhs.subList(dotPosition, rhs.size()));
        }

        public Symbol getLookAhead() { return lookAhead; }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Lr1Item) {
                final Lr1Item other = (Lr1Item) obj;
                return production.equals(other.production) &&
                        dotPosition == other.dotPosition &&
                        lookAhead.equals(other.lookAhead);
            }
            return false;
        }

        @Override
        public int hashCode() { return production.hashCode() + dotPosition + lookAhead.hashCode(); }

        @Override
        public String toString() { return "[" + production + ", " + dotPosition + ", " + lookAhead +"]"; }
    }

}