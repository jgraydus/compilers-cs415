/* Joshua Graydus | February 2016 */
package parser;

import data.Either;

import java.util.*;
import java.util.function.Function;

import static data.Either.left;
import static data.Either.right;

public class LL1Parser<T> extends Parser<T> {

    private final Production[][] table;
    
    /**
     * @param g a grammar
     * @param toSymbol a function that associates each possible input token
     *                 of type T to a Symbol object in the grammar g
     */
    public LL1Parser(final Grammar g, final Function<T,Symbol> toSymbol) {
        super(g, toSymbol);

        // generate the parse table

        final int height = g.getNonTerminals().size();
        final int width = g.getTerminals().size();
        table = new Production[height][width];

        g.getNonTerminals().forEach(nt -> {
            g.get(nt).forEach(p -> {
                firstAndFollow.first(p).stream()
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

    /** {@inheritDoc} */
    @Override
    public Either<List<T>, ParseTree<T>> parse(final List<T> tokens) {
        final List<T> errors = new LinkedList<>();
        final ParseTree<T> result = parse(tokens.iterator(), errors);
        return errors.isEmpty() ? right(result) : left(errors);
    }

    private ParseTree<T> parse(final Iterator<T> iter, final List<T> errors) {
        // start with a parse stack containing a root node for the start symbol
        final Stack<ParseTree<T>> parseStack = new Stack<>();
        final ParseTree<T> root = new ParseTree<>(start, null);
        parseStack.push(root);

        // no input
        if (!iter.hasNext()) { return root; }

        // nextT will hold the next token in the input.  this will give the lookahead symbol
        //final Iterator<T> iter = tokens.iterator();
        T nextT = iter.hasNext() ? iter.next() : null;

        while (true) {
            // get the grammar symbol corresponding to the next input token.  if the input is empty,
            // use the special symbol $
            final Symbol lookahead = nextT == null ? Symbol.$ : toSymbol.apply(nextT);

            // the accept condition:  no more input and parse stack is empty
            if (lookahead.equals(Symbol.$) && parseStack.isEmpty()) { break; }

            // empty parse stack at this point is unrecoverable error
            if (parseStack.isEmpty()) {
                errors.add(nextT);
                return root;
            }

            final ParseTree<T> top = parseStack.peek();
            final Symbol topS = top.getSymbol();

            // when the top of the parse stack is a terminal, it must match the lookahead.  if so, then
            // pop the parse stack and advance the input.  otherwise report an error
            if (topS.isTerminal()) {
                if (topS.equals(lookahead)) {
                    top.setT(nextT); // first add the token to the current parse tree node
                    nextT = iter.hasNext() ? iter.next() : null;
                    parseStack.pop();
                } else {
                    // report current token as an error and start over with the next token
                    errors.add(nextT);
                    return parse(iter, errors);
                }
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
                    // report current token as an error and start over with the next token
                    errors.add(nextT);
                    return parse(iter, errors);
                }
                parseStack.pop();
                final Stack<ParseTree<T>> tmp = new Stack<>();
                p.getRhs().stream()
                        // remove epsilon. this effectively disregards epsilon productions
                        .filter(s -> s != Symbol.epsilon)
                        // create a new ParseTree node for each symbol that is being added
                        .map(s -> new ParseTree<T>(s, null))
                        .forEach(t -> {
                            // add all the new parse tree nodes as children to the parse tree at the top of the stack
                            top.addChild(t);
                            tmp.push(t);
                        });
                // the tmp stack is used so that the new parse stack entries can be pushed in reverse order
                while (!tmp.isEmpty()) { parseStack.push(tmp.pop()); }
            }
        }

        // success!
        return root;
    }
}