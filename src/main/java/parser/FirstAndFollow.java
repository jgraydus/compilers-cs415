/* Joshua Graydus | February 2016 */
package parser;

import java.util.*;

import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;

/** encapsulates the computation of the first and follow sets for the symbols of a grammar */
public class FirstAndFollow {

    private final Map<Symbol, Set<Symbol>> first = new HashMap<>();
    private final Map<Symbol, Set<Symbol>> follow = new HashMap<>();

    public FirstAndFollow(final Grammar g) {
        first(g);  // initialize the first sets
        follow(g); // initialize the follow sets
    }

    /** @return first(s) for the symbol s */
    public Set<Symbol> first(final Symbol s) { return unmodifiableSet(first.get(s)); }

    /** @return follow(s) for the symbol s */
    public Set<Symbol> follow(final Symbol s) { return unmodifiableSet(follow.get(s)); }

    /**
     * The first set for a production <pre>first(A -> B)</pre> is defined as: <br>
     * first(B) if first(B) does not contain epsilon <br>
     * first(B) U follow(A) if first(B) contains epsilon
     * @return <pre>first(A -> B)</pre> */
    public Set<Symbol> first(final Production p) {
        final Set<Symbol> result = new HashSet<>();
        for (final Symbol s : p.getRhs()) {
            result.addAll(first(s));
            if (!first(s).contains(Symbol.epsilon)) { return result; }
        }
        result.addAll(follow(p.getLhs()));
        return result;
    }

    /**
     * compute first(x) for all symbols x in the grammar g <br>
     * <br>
     * first(x), where x is a grammar symbol, is defined as:<br>
     * <ol>
     *     <li>if x is a terminal or epsilon, then first(x) = {x} </li>
     *     <li>if x is a nonterminal, then for each production x -> a_1 ... a_n, first(x)
     *     contains:
     *         <ul>
     *         <li>first(a_1) - {epsilon}</li>
     *         <li>first(a_2) if first(a_1) contains epsilon</li>
     *         <li>first(a_3) if first(a_2) contains epsilon</li>
     *         <li>etc.</li>
     *         <li>epsilon if and only if first(a_i) contains epsilon for all i</li>
     *         </ul>
     *     </li>
     * </ol>
     */
    private void first(final Grammar g) {
        // for each terminal t, first(t) = {t}
        g.getTerminals().forEach(t -> first.put(t, singleton(t)));
        // for each non-terminal nt, initialize first(nt) to an empty set
        g.getNonTerminals().forEach(nt -> first.put(nt, new HashSet<>()));

        // continue this process until no further changes to the first sets occur
        final boolean[] done = { false };
        while (!done[0]) {
            done[0] = true;
            // for each of the nonterminals
            g.getNonTerminals().forEach(nt -> {
                // iterate through every production
                g.get(nt).forEach(p -> {
                    final Set<Symbol> rhs = new HashSet<>();
                    // for a production A -> a_1 a_2 ... a_n, add first(a_i) to the set of first items until
                    // some first(a_i) does not contain epsilon
                    for (final Symbol s : p.getRhs()) {
                        final Set<Symbol> fs = first.get(s);
                        rhs.addAll(fs);
                        if (!fs.contains(Symbol.epsilon)) {
                            rhs.remove(Symbol.epsilon);
                            break;
                        }
                    }
                    final Set<Symbol> fs = first.get(p.getLhs());
                    // found something new. add it and indicate that another iteration of the main loop should happen
                    if (!fs.containsAll(rhs)) {
                        fs.addAll(rhs);
                        done[0] = false;
                    }
                });
            });
        }
    }

    /**
     * compute follow(x) for all nonterminal symbols x in the grammar g<br>
     * <br>
     * follow(x), for a grammar symbol x, contains:<br>
     * <ol>
     *     <li>$ if x is the start symbol</li>
     *     <li>first(b) - {epsilon} for all productions of the form A -> axb</li>
     *     <li>follow(A) for all productions of the form A -> axb where first(b) contains epsilon</li>
     * </ol>
     */
    private void follow(final Grammar g) {
        // for each nonterminal nt, initialize follow(nt) to an empty set
        g.getNonTerminals().forEach(nt -> follow.put(nt, new HashSet<>()));

        // add $ to follow(goal)
        follow.get(Symbol.goal).add(Symbol.$);

        // continue this process until no further changes to the follow sets occur
        final boolean[] done = { false };
        while (!done[0]) {
            done[0] = true;
            // for each nonterminal
            g.getNonTerminals().forEach(nt -> {
                // iterate through every production
                g.get(nt).forEach(p -> {
                    // for a production A -> b_1 b_2 ... b_n
                    final Set<Symbol> tail = new HashSet<>();
                    // set an initial tail set to contain follow(A) as calculated so far
                    tail.addAll(follow.get(nt));
                    // go through each b_i in reverse order
                    reverse(p.getRhs()).forEach(b -> {
                        // if b_i is a nonterminal
                        if (!b.isTerminal()) {
                            // and tail contains items that are not in follow(b_i)
                            if (!follow.get(b).containsAll(tail)) {
                                // add the items to follow(b_i)
                                follow.get(b).addAll(tail);
                                // and indicate that another iteration of the main loop is necessary
                                done[0] = false;
                            }
                            // if first(b_i) contains epsilon, then add first(b_i) minus epsilon to the existing tail set. Since
                            // b_i can derive epsilon, everything in follow(b_i) will also be in the follow sets of the
                            // preceding b's.
                            if (first.get(b).contains(Symbol.epsilon)) {
                                final Set<Symbol> fb = new HashSet<>(first.get(b));
                                fb.remove(Symbol.epsilon);
                                tail.addAll(fb);
                            }
                            // if first(b_i) does not contain epsilon, then tail is reset to just contain first(b_i)
                            else {
                                tail.clear();
                                tail.addAll(first.get(b));
                            }
                        }
                        // if b_i is a terminal, then reset tail to first(b_i) which is just {b_i}
                        else {
                            tail.clear();
                            tail.add(b);
                        }
                    });
                });
            });
        }
    }

    /** reverse a list */
    private <T> List<T> reverse(final List<T> ts) {
        final List<T> l = new ArrayList<>(ts);
        Collections.reverse(l);
        return l;
    }
}