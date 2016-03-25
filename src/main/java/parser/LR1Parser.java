/* Joshua Graydus | March 2016 */
package parser;

import data.Either;
import data.Pair;
import logging.Logger;

import java.util.*;
import java.util.function.Function;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;

/** LR1 parser generator */
public class LR1Parser<T> extends Parser<T> {
    private final Logger logger = new Logger();

    private final Grammar g;
    private final ParseTables tables;

    public LR1Parser(final Grammar g, final Function<T,Symbol> toSymbol) {
        super(g, toSymbol);
        this.g = g;
        tables = buildParseTables();
    }

    @Override
    public Either<List<T>,ParseTree<T>> parse(final List<T> tokens) {
        final List<T> errors = new LinkedList<>();
        final ParseTree<T> result = parse(tokens, errors);
        if (errors.isEmpty()) { return Either.right(result);
        } else { return Either.left(errors); }
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
            logger.trace("state=" + state + "| token=" + token + "| symbol=" + symbol);

            final Action action = tables.getAction(state, symbol);
            logger.trace("    >" + action);

            if (action == null) {
                errors.add(token);
                return parseStack.pop();
            }

            if (action instanceof Reduce) {
                final Reduce reduce = (Reduce) action;
                final List<Symbol> production = reduce.production.getRhs().stream()
                        .filter(s -> !s.equals(Symbol.epsilon)).collect(toList());
                final Symbol a = reduce.production.getLhs();
                final int size = production.size();
                final ParseTree<T> t = new ParseTree<>(a, token);
                final Stack<ParseTree<T>> temp = new Stack<>(); // to add the children in the correct order
                for (int i=0; i<size; i++) {
                    stateStack.pop();
                    temp.push(parseStack.pop());
                }
                for (int i=0; i<size; i++) { t.addChild(temp.pop()); }
                parseStack.push(t);
                stateStack.push(tables.getTransition(stateStack.peek(), a));
                logger.trace("    >goto " + stateStack.peek());
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

    ParseTables buildParseTables() {
        logger.trace("building parse tables");
        final CanonicalCollection cc = canonicalCollection();
        final ParseTables tables = new ParseTables();

        cc.getSets().forEach((i,cci) -> {
            logger.trace("canonical collection set #" + i);
            cci.forEach(item -> {
                logger.trace("item: " + item);
                final List<Symbol> unseen = item.getSymbolsAfterDot();
                if (// if the dot isn't at the end of the production
                        !unseen.isEmpty() &&
                        //and this isn't an epsilon production
                        !unseen.get(0).equals(Symbol.epsilon) &&
                        // and a transition exists from the current state on the next symbol of the production
                        cc.getTransitions().containsKey(Pair.of(i,unseen.get(0)))) {
                    // then add a shift action if the next symbol of the production is a terminal
                    final Symbol c = unseen.get(0);
                    if (c.isTerminal()) {
                        final int j = cc.getTransitions().get(Pair.of(i,c));
                        tables.addAction(i, c, new Shift(j));
                        logger.trace("adding a shift action from " + i + " to " + j + " on " + c);
                    }
                } else if (unseen.isEmpty() && isTarget(item)) {
                    tables.addAction(i, Symbol.$, new Accept());
                    logger.trace("adding an accept action for state " + i);
                } else if ((unseen.isEmpty() || unseen.get(0).equals(Symbol.epsilon))) {
                    tables.addAction(i, item.lookAhead, new Reduce(item.production));
                    logger.trace("adding a reduce action from " + i + " using rule " + item.production);
                } else {
                    throw new IllegalStateException("something went terribly wrong while building parse tables");
                }
            });
            g.getNonTerminals().forEach(nt -> {
                final Pair<Integer,Symbol> key = Pair.of(i,nt);
                if (cc.getTransitions().containsKey(key)) {
                    final int j = cc.getTransitions().get(key);
                    tables.addTransition(i, nt, j);
                    logger.trace("adding a goto table entry from " + i + " to " + j + " for reduction to " + nt);
                } else {
                    logger.trace("there is no transition from " + i + " on a reduction to " + nt);
                }
            });
        });

        return tables;
    }

    private boolean isTarget(final LR1Item item) {
        return item.production.getLhs().equals(Symbol.goal) && item.getLookAhead().equals(Symbol.$);
    }

    static class ParseTables {
        private final Logger logger = new Logger();
        private final Map<Pair<Integer,Symbol>,Action> actionTable = new HashMap<>();
        private final Map<Pair<Integer,Symbol>,Integer> gotoTable = new HashMap<>();

        void addAction(final int state, final Symbol symbol, final Action action) {
            final Pair<Integer,Symbol> key = Pair.of(state,symbol);
            // in order to handle ambiguities such as the dangling else problem, if a shift-reduce conflict occurs,
            // then the shift action will be kept and the reduce action will be thrown out
            if (actionTable.containsKey(key) && !actionTable.get(key).equals(action)) {
                final Action other = actionTable.get(key);
                if (action instanceof Shift && other instanceof Reduce) {
                    logger.debug("shift-reduce conflict -- replacing reduce action with shift action");
                    actionTable.put(key, action);
                } else if (action instanceof Reduce && other instanceof Shift) {
                    logger.debug("shift-reduce conflict -- discarding reduce action in favor of shift action");
                } else if (action instanceof Reduce && other instanceof Reduce) {
                    final String message = "\nreduce-reduce conflict!\n" +
                            "state=" + state + "\n" +
                            "symbol=" + symbol + "\n" +
                            "existing action=" + other + "\n" +
                            "new action=" + action;
                    throw new IllegalStateException(message);
                }
            } else {
                actionTable.put(key, action);
            }
        }

        Action getAction(final int state, final Symbol symbol) {
            final Pair<Integer,Symbol> key = Pair.of(state,symbol);
            return actionTable.get(key);
        }

        void addTransition(final int from, final Symbol on, final int to) {
            final Pair<Integer,Symbol> key = Pair.of(from,on);
            if (gotoTable.containsKey(key) && !gotoTable.get(key).equals(to)) {
                throw new IllegalStateException("attempt to replace an existing entry in goto table");
            } else {
                gotoTable.put(key,to);
            }
        }

        int getTransition(final int state, final Symbol symbol) {
            final Pair<Integer,Symbol> key = Pair.of(state,symbol);
            if (!gotoTable.containsKey(key)) {
                throw new IllegalStateException("there is no entry in the goto table for " + key);
            }
            return gotoTable.get(key);
        }
    }

    private static class Action {}

    private static class Accept extends Action {
        @Override public String toString() { return "accept"; }
    }

    private static class Shift extends Action {
        final int nextState;
        Shift(final int nextState) { this.nextState = nextState; }
        @Override public String toString() { return "shift:" + nextState; }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Shift) {
                final Shift other = (Shift) obj;
                return nextState == other.nextState;
            }
            return false;
        }
    }

    private static class Reduce extends Action {
        final Production production;
        Reduce(final Production production) { this.production = production; }
        @Override public String toString() { return "reduce:" + production; }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Reduce) {
                final Reduce other = (Reduce) obj;
                return production.equals(other.production);
            }
            return false;
        }
    }

    /* compute the closure of a set of LR1 items */
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
    private Set<Symbol> first(final List<Symbol> symbols) {
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

    CanonicalCollection canonicalCollection() {
        final CanonicalCollection cc = new CanonicalCollection();

        final LR1Item initial = new LR1Item(new Production(Symbol.goal, singletonList(g.getStart())), 0, Symbol.$);
        final Set<LR1Item> cc0 = closure(singleton(initial));

        cc.add(cc0);

        boolean done = false;
        while (!done) {
            done = true;
            // for unprocessed set in cc
            for (final Set<LR1Item> cci : cc.getUnprocessed()) {
                // for each item in the current set
                for (final LR1Item item : cci) {
                    final List<Symbol> unseen = item.getSymbolsAfterDot();
                    if (!unseen.isEmpty()) {
                        // if the item is of the form a -> b.xc
                        final Symbol x = unseen.get(0);
                        // calculate the goTo set for the item and the symbol x
                        final Set<LR1Item> temp = goTo(cci, x);
                        // if this set isn't already part of cc, then add it to the set of updates
                        if (!cc.contains(temp)) {
                            cc.add(temp);
                            done = false;
                        }
                        // record the transition from the current cci on the symbol x to this new set
                        cc.addTransition(cci, x, temp);
                    }
                }
            }
        }
        return cc;
    }

    static class CanonicalCollection {
        int nextNumber = 0;
        final Map<Integer,Set<LR1Item>> intToSet = new TreeMap<>();
        final Map<Set<LR1Item>,Integer> setToInt = new HashMap<>();
        final Map<Pair<Integer,Symbol>,Integer> transitions = new HashMap<>();
        List<Set<LR1Item>> unprocessed = new ArrayList<>();

        boolean contains(final Set<LR1Item> set) {
            return setToInt.containsKey(set);
        }

        Map<Integer,Set<LR1Item>> getSets() { return unmodifiableMap(intToSet); }
        Map<Pair<Integer,Symbol>,Integer> getTransitions() { return unmodifiableMap(transitions); }

        Collection<Set<LR1Item>> getUnprocessed() {
            final Collection<Set<LR1Item>> temp = unprocessed;
            unprocessed = new ArrayList<>();
            return temp;
        }

        void add(final Set<LR1Item> set) {
            if (setToInt.containsKey(set)) { throw new IllegalStateException("set is already in cc"); }
            setToInt.put(set,nextNumber);
            intToSet.put(nextNumber,set);
            unprocessed.add(set);
            nextNumber++;
        }

        void addTransition(final Set<LR1Item> from, final Symbol on, final Set<LR1Item> to) {
            if (!setToInt.containsKey(from)) { throw new IllegalStateException("not in cc: " + from); }
            if (!setToInt.containsKey(to)) { throw new IllegalStateException("not in cc: " + to); }
            final int from_ = setToInt.get(from);
            final int to_ = setToInt.get(to);
            final Pair<Integer,Symbol> key = Pair.of(from_,on);
            if (transitions.containsKey(key)) {
                final int previous = transitions.get(key);
                if (previous != to_) { throw new IllegalStateException("attempting to alter an existing transition"); }
            } else {
                transitions.put(Pair.of(from_, on), to_);
            }
        }
    }

    static class LR1Item {
        private final Production production;
        private final int dotPosition;
        private final Symbol lookAhead;

        LR1Item(final Production production,
                       final int dotPosition,
                       final Symbol lookAhead) {
            this.production = production;
            this.dotPosition = dotPosition;
            this.lookAhead = lookAhead;
        }

        /** @return the string of symbols after the dot */
        List<Symbol> getSymbolsAfterDot() {
            final List<Symbol> rhs = production.getRhs();
            return new LinkedList<>(rhs.subList(dotPosition, rhs.size()));
        }

        Symbol getLookAhead() { return lookAhead; }

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