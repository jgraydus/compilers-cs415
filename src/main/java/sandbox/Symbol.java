/* Joshua Graydus | February 2016 */
package sandbox;

public abstract class Symbol {
    private final String str;

    protected Symbol(String str) { this.str = str; }

    /** @return true if this symbol represents a terminal in the grammar. false if it represents a nonterminal */
    public abstract boolean isTerminal();

    /** special symbol used in production rules to indicate that a nonterminal can simply be removed (i.e. replaced
     * with nothing  */
    public static final Symbol ε = new Terminal("ε");

    /* the following special symbols should not be used to specify grammars.  they are added automatically */

    /** special symbol used to indicate successfully completed parse. added automatically */
    public static final Symbol goal = new NonTerminal("goal");

    /** special symbol indicating end of input. added automatically */
    public static final Symbol $ = new Terminal("$");

    @Override final public boolean equals(Object other) { return this == other; }
    @Override final public String toString() { return str; }

    public static class Terminal extends Symbol {
        public Terminal(String str) { super(str); }
        @Override public boolean isTerminal() { return true; }
    }

    public static class NonTerminal extends Symbol {
        public NonTerminal(String str) { super(str); }
        @Override public boolean isTerminal() { return false; }
    }
}