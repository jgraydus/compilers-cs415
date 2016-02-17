package sandbox;

import org.junit.Test;

import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class LL1Test {

    @Test
    public void test1() {
        /*  S -> b A
            A -> a A | ε    */

        final Symbol S = new Symbol.NonTerminal("S");
        final Symbol A = new Symbol.NonTerminal("A");
        final Symbol a = new Symbol.Terminal("a");
        final Symbol b = new Symbol.Terminal("b");

        final Production p1 = new Production(S, asList(b, A));
        final Production p2 = new Production(A, asList(a, A));
        final Production p3 = new Production(A, asList(Symbol.ε));

        final Symbol start = S;
        final List<Production> ps = asList(p1, p2, p3);
        final Grammar g = new Grammar(start, ps);

        final Function<String,Symbol> toSymbol = str -> str.equals("a") ? a : b;

        final List<String> sentence = asList("b", "a");

        final LL1 ll1 = new LL1(g);

        ll1.parse(sentence, toSymbol);

        // TODO verify parse tree
    }

    @Test
    public void test2() {
        /*  S -> ( S ) S | a | ε */

        final Symbol S = new Symbol.NonTerminal("S");
        final Symbol left = new Symbol.Terminal("(");
        final Symbol right = new Symbol.Terminal(")");
        final Symbol a = new Symbol.Terminal("a");

        final Production p1 = new Production(S, asList(left, S, right, S));
        final Production p2 = new Production(S, asList(a));
        final Production p3 = new Production(S, asList(Symbol.ε));

        final Symbol start = S;
        final List<Production> ps = asList(p1, p2, p3);
        final Grammar g = new Grammar(start, ps);

        final Function<String,Symbol> toSymbol = str -> {
            switch (str) {
                case "(": return left;
                case ")": return right;
                case "a": return a;
                default: throw new IllegalArgumentException(str);
            }
        };

        final List<String> sentence = asList("(", "(", "a", ")", "a", ")");

        final LL1 ll1 = new LL1(g);

        ll1.parse(sentence, toSymbol);

        // TODO verify parse tree
    }
}
