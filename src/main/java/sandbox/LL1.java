package sandbox;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class LL1 {

    class Symbol {
        final String symbol;
        final boolean terminal;
        public Symbol(String symbol, boolean terminal) { this.symbol = symbol; this.terminal = terminal; }
        @Override public String toString() { return symbol; }
    }


    Stack<Symbol> parseStack = new Stack<>();
    Stack<Symbol> inputStack = new Stack<>();

    Symbol s = new Symbol("S", false);
    Symbol lp = new Symbol("(", true);
    Symbol rp = new Symbol(")", true);
    Symbol $ = new Symbol("$", true);

    Map<Symbol, Map<Symbol,List<Symbol>>> table = new HashMap<>();


    public void parse() {
        Map<Symbol,List<Symbol>> row = new HashMap<>();
        row.put(lp, asList(lp, s, rp, s));
        row.put(rp, emptyList());
        row.put($, emptyList());
        table.put(s, row);

        parseStack.push($);
        parseStack.push(s);

        inputStack.push($);
        inputStack.push(rp);
        inputStack.push(rp);
        inputStack.push(lp);
        inputStack.push(rp);
        inputStack.push(lp);
        inputStack.push(lp);

        while (parseStack.peek() != $) {
            System.out.println(">>>inputStack=" + inputStack);
            System.out.println(">>>parseStack-" + parseStack);

            if (parseStack.peek().terminal && parseStack.peek() == inputStack.peek()) {
                System.out.println("matching " + inputStack.pop());
                parseStack.pop();
            } else if (!parseStack.peek().terminal && inputStack.peek().terminal) {
                Symbol A = parseStack.peek();
                Symbol a = inputStack.peek();
                List<Symbol> rule = table.get(A).get(a);
                System.out.println("generating " + rule);
                parseStack.pop();
                for (int i=rule.size()-1; i>=0; i--) { parseStack.push(rule.get(i)); }
            }
        }

        if (parseStack.peek() == $ && inputStack.peek() == $) {
            System.out.println("ACCEPT");
        } else {
            System.out.println("ERROR");
        }

    }

    public static void main(String... args) {
        LL1 ll1 = new LL1();
        ll1.parse();
    }

}
