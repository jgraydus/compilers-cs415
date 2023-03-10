/* Joshua Graydus | February 2016 */
package parser;

public abstract class Symbol {
    private final String str;

    protected Symbol(final String str) { this.str = str; }

    /** @return true if this symbol represents a terminal in the grammar. false if it represents a nonterminal */
    public abstract boolean isTerminal();

    /** special symbol used in production rules to indicate that a nonterminal can simply be removed (i.e. replaced
     * with nothing  */
    public static final Symbol epsilon = new Terminal("epsilon");

    /* the following special symbols should not be used to specify grammars.  they are added automatically */

    /** special symbol used to indicate successfully completed parse. added automatically */
    public static final Symbol goal = new NonTerminal("goal");

    /** special symbol indicating end of input. added automatically */
    public static final Symbol $ = new Terminal("$");

    @Override final public boolean equals(final Object other) { return str.equals(((Symbol)other).str); }
    @Override final public int hashCode() { return str.hashCode(); }
    @Override final public String toString() { return str; }

    public static class Terminal extends Symbol {
        public Terminal(final String str) { super(str); }
        @Override public boolean isTerminal() { return true; }
    }

    public static class NonTerminal extends Symbol {
        public NonTerminal(final String str) { super(str); }
        @Override public boolean isTerminal() { return false; }
    }
}