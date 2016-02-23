/* Joshua Graydus | February 2016 */
package parser;

import java.util.List;

/** represents the production rule "lhs -> rhs" */
public class Production {
    private final Symbol lhs;
    private final List<Symbol> rhs;

    public Production(final Symbol lhs, final List<Symbol> rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Symbol getLhs() { return lhs; }
    public List<Symbol> getRhs() { return rhs; }

    @Override public String toString() { return "<" + lhs + " -> " + rhs + ">"; }
}