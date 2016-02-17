package sandbox;

import java.util.*;
import java.util.function.Function;

public class LL1<T> {

    private final Symbol start;
    private final Map<Symbol,Integer> nonterminals = new HashMap<>();
    private final Map<Symbol,Integer> terminals = new HashMap<>();
    private final Production[][] table;
    private final Function<T,Symbol> toSymbol;

    /**
     * @param g a grammar
     * @param toSymbol a function that associates each possible input token
     *                 of type T to a Symbol object in the grammar g
     */
    public LL1(final Grammar g, Function<T,Symbol> toSymbol) {
        this.toSymbol = toSymbol;
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

        // generate the parse table
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
                                final StringBuilder sb = new StringBuilder();
                                sb.append("\nnot an LL(1) grammar\n");
                                sb.append("there are multiple productions for ");
                                sb.append(nt);
                                throw new IllegalStateException(sb.toString());
                            }
                        });
            });
        });
    }

    /** @return a full parse tree for the given input */
    public ParseTree<T> parse(List<T> tokens) {

        // start with a parse stack containing a root node for the start symbol
        final Stack<ParseTree<T>> parseStack = new Stack<>();
        final ParseTree<T> root = new ParseTree<>(start, null);
        parseStack.push(root);

        // nextT will hold the next token in the input.  this will give the lookahead symbol
        final Iterator<T> iter = tokens.iterator();
        T nextT = iter.hasNext() ? iter.next() : null;

        while (true) {
            // get the grammar symbol corresponding to the next input token.  if the input is empty,
            // use the special symbol $
            final Symbol lookahead = nextT == null ? Symbol.$ : toSymbol.apply(nextT);

            // the accept condition:  no more input and parse stack is empty
            if (lookahead.equals(Symbol.$) && parseStack.isEmpty()) { break; }

            final ParseTree<T> top = parseStack.peek();
            final Symbol topS = top.getSymbol();

            // when the top of the parse stack is a terminal, it must match the lookahead.  if so, then
            // pop the parse stack and advance the input.  otherwise report an error
            if (topS.isTerminal()) {
                if (topS.equals(lookahead)) {
                    top.setT(nextT); // first add the token to the current parse tree node
                    nextT = iter.hasNext() ? iter.next() : null;
                    parseStack.pop();
                } else { throw new IllegalStateException(topS + " != " + lookahead); }
            }
            // if the current symbol at the top of the stack is a nonterminal, find and use an appropriate
            // production based on the lookahead
            else {
                // find the appropriate production
                final int row = nonterminals.get(topS);
                final int col = terminals.get(lookahead);
                final Production p = table[row][col];
                // if there is no entry in the table, the input is not in the language of the grammar
                if (p == null) {
                    throw new IllegalStateException(nextT.toString());
                }
                parseStack.pop();
                final Stack<ParseTree<T>> tmp = new Stack<>();
                p.getRhs().stream()
                        // remove ε. this effectively disregards epsilon productions
                        .filter(s -> s != Symbol.ε)
                        // create a new ParseTree node for each symbol that is being added
                        .map(s -> new ParseTree<T>(s, null))
                        .forEach(t -> {
                            // add all the new parse tree nodes as children to parse tree at the top of the stack
                            top.addChild(t);
                            tmp.push(t);
                        });
                // the tmp stack is used so that the new parseStack entries can be pushed in reverse order
                while (!tmp.isEmpty()) { parseStack.push(tmp.pop()); }
            }
        }

        return root;
    }
}