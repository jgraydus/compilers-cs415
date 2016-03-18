/* Joshua Graydus | March 2016 */
package parser;

import data.Either;
import data.Pair;

import java.util.*;
import java.util.function.Function;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

// TODO
public class LR1Parser<T> extends Parser<T> {

    private final Grammar g;

    private final Map<Pair<Integer,Symbol>,Action> actionTable;
    private final Map<Pair<Integer,Symbol>,Integer> gotoTable;

    public LR1Parser(final Grammar g, final Function<T,Symbol> toSymbol) {
        super(g, toSymbol);
        this.g = g;
        final Pair<Map<Pair<Integer,Symbol>,Action>,Map<Pair<Integer,Symbol>,Integer>> tables = buildParseTables();
        actionTable = tables.getLeft();
        gotoTable = tables.getRight();
    }

    @Override
    public Either<List<T>,ParseTree<T>> parse(final List<T> tokens) {
        final List<T> errors = new LinkedList<>();
        return Either.right(parse(tokens, errors));
    }

    private ParseTree<T> parse(final List<T> tokens, final List<T> errors) {
        final Stack<ParseTree<T>> parseStack = new Stack<>();
        final Stack<Integer> stateStack = new Stack<>();

        stateStack.push(0);

        final Iterator<T> iter = tokens.iterator();

        T token = iter.next();
        Symbol symbol = toSymbol.apply(token);

        while (true) {
            final int state = stateStack.peek();
            final Action action = actionTable.get(Pair.of(state,symbol));

            if (action == null) { throw new IllegalStateException("\nunexpected token " + token.toString()); }

            if (action instanceof Reduce) {
                final Reduce reduce = (Reduce) action;
                int size = reduce.production.getRhs().size();
                final Symbol a = reduce.production.getLhs();
                final ParseTree<T> t = new ParseTree<>(a, null);
                for (int i=0; i<size; i++) {
                    stateStack.pop();
                    t.addChild(parseStack.pop());
                }
                parseStack.push(t);
                stateStack.push(gotoTable.get(Pair.of(stateStack.peek(), a)));
                continue;
            }

            if (action instanceof Shift) {
                final Shift shift = (Shift) action;
                parseStack.push(new ParseTree<>(symbol, token));
                stateStack.push(shift.nextState);
                token = iter.next();
                symbol = toSymbol.apply(token);
                continue;
            }

            if (action instanceof Accept) { break; }
        }

        return parseStack.pop();
    }

    Pair<Map<Pair<Integer,Symbol>,Action>,Map<Pair<Integer,Symbol>,Integer>> buildParseTables() {
        final Pair<List<Set<LR1Item>>,Map<Pair<Set<LR1Item>,Symbol>,Set<LR1Item>>> cc_ = canonicalCollection();
        final List<Set<LR1Item>> cc = cc_.getLeft();
        final Map<Pair<Set<LR1Item>,Symbol>,Set<LR1Item>> transitions = cc_.getRight();

        // explicitly map each cci to its index
        final Map<Set<LR1Item>,Integer> states = new HashMap<>();
        for (int i=0; i<cc.size(); i++) { states.put(cc.get(i), i); }

        final Map<Pair<Integer,Symbol>,Action> actionTable = new HashMap<>();
        final Map<Pair<Integer,Symbol>,Integer> gotoTable = new HashMap<>();

        for (final Set<LR1Item> cci : cc) {
            final int i = states.get(cci);
            for (final LR1Item item : cci) {
                final List<Symbol> unseen = item.getSymbolsAfterDot();
                if (!unseen.isEmpty() && transitions.get(Pair.of(cci,unseen.get(0))) != null) {
                    final Symbol c = unseen.get(0);
                    if (c.isTerminal()) {
                        final int j = states.get(transitions.get(Pair.of(cci, c)));
                        actionTable.put(Pair.of(i, c), new Shift(j));
                    }
                } else if (isTarget(item)) {
                    actionTable.put(Pair.of(i,Symbol.$), new Accept());
                } else if (unseen.isEmpty()) {
                    final Production p = item.production;
                    final Symbol a = item.lookAhead;
                    actionTable.put(Pair.of(i,a), new Reduce(p));
                } else {
                    throw new IllegalStateException();
                }
            }
            for (final Symbol n : g.getNonTerminals()) {
                Optional.ofNullable(states.get(transitions.get(Pair.of(cci,n))))
                        .ifPresent(j -> gotoTable.put(Pair.of(i,n), j));
            }
        }

        return Pair.of(actionTable, gotoTable);
    }

    boolean isTarget(final LR1Item item) {
        return item.production.getLhs().equals(Symbol.goal) && item.getLookAhead().equals(Symbol.$);
    }

    static class Action {}

    static class Accept extends Action {}

    static class Shift extends Action {
        final int nextState;
        Shift(final int nextState) { this.nextState = nextState; }
        @Override public String toString() { return "shift:" + nextState; }
    }

    static class Reduce extends Action {
        final Production production;
        Reduce(final Production production) { this.production = production; }
        @Override public String toString() { return "reduce:" + production; }
    }


    /* compute the closure of a set of LR1 items  TODO make this more efficient */
    Set<LR1Item> closure(final Set<LR1Item> items) {
        final Set<LR1Item> result = new HashSet<>();
        // the item itself is in the closure
        result.addAll(items);
        while (true) {
            final Set<LR1Item> updates = new HashSet<>();
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
                                updates.add(new LR1Item(p, 0, b));
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

    Set<LR1Item> goTo(final Set<LR1Item> items, final Symbol symbol) {
        final Set<LR1Item> result = new HashSet<>();
        items.forEach(item -> {
            final List<Symbol> unseen = item.getSymbolsAfterDot();
            if (!unseen.isEmpty() && unseen.get(0).equals(symbol)) {
                result.add(new LR1Item(item.production, item.dotPosition+1, item.lookAhead));
            }
        });
        return closure(result);
    }

    /* this type signature is insane! the method returns a pair of results. the first result
     * is the canonical collection, a List<Set<LR1Item>>. the second result is a map from a
     * Set<LR1Item> and a Symbol to another Set<LR1Item> which gives the transitions between
     * sets. (each Set<LR1Item> represents a state in the parser's DFA) */
    Pair<List<Set<LR1Item>>,Map<Pair<Set<LR1Item>,Symbol>,Set<LR1Item>>> canonicalCollection() {
        final Map<Pair<Set<LR1Item>,Symbol>,Set<LR1Item>> transitions = new HashMap<>();

        final LR1Item initial = new LR1Item(new Production(Symbol.goal, singletonList(g.getStart())), 0, Symbol.$);
        final Set<LR1Item> cc0 = closure(singleton(initial));

        class CCSet extends HashSet<LR1Item> {
            boolean processed = false;
            CCSet(final Set<LR1Item> set) { super(set); }
        }

        final Set<CCSet> cc = new HashSet<>();
        cc.add(new CCSet(cc0));

        boolean done = false;
        while (!done) {
            final Set<CCSet> updates = new HashSet<>();
            // for unprocessed set in cc
            for (final CCSet cci : cc) {
                if (!cci.processed) {
                    cci.processed = true;
                    // for each item in the current set
                    for (final LR1Item item : cci) {
                        final List<Symbol> unseen = item.getSymbolsAfterDot();
                        if (!unseen.isEmpty()) {
                            // if the item is of the form a -> b.xc
                            final Symbol x = unseen.get(0);
                            // calculate the goTo set for the item and the symbol x
                            final CCSet temp = new CCSet(goTo(cci, x));
                            // if this set isn't already part of cc, then add it to the set of updates
                            if (!cc.contains(temp)) { updates.add(temp); }
                            // record the transition from the current cci on the symbol x to this new set
                            final Pair<Set<LR1Item>,Symbol> pair = Pair.of(cci,x);
                            transitions.put(pair,temp);
                        }
                    }
                }
            }
            // if updates is empty, we're finished
            if (updates.isEmpty()) { done = true; }
            // otherwise add all the updates to cc and do another iteration
            else { cc.addAll(updates); }
        }

        // we need to ensure that cc0 is the first set in the list so that it can be identified when
        // preparing to do a parse
        final List<Set<LR1Item>> result = new ArrayList<>();
        result.add(cc0);
        cc.remove(cc0);
        cc.forEach(result::add);

        return Pair.of(result,transitions);
    }

    static class LR1Item {
        private final Production production;
        private final int dotPosition;
        private final Symbol lookAhead;

        public LR1Item(final Production production,
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
            if (obj instanceof LR1Item) {
                final LR1Item other = (LR1Item) obj;
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