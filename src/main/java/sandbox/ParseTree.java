package sandbox;

import java.util.LinkedList;
import java.util.List;

public class ParseTree<T> {
    private final Symbol s;
    private final T t;
    private final List<ParseTree<T>> children = new LinkedList<>();

    public ParseTree(final Symbol s, final T t) {
        this.s = s;
        this.t = t;
    }

    public Symbol getSymbol() {
        return s;
    }

    public void addChild(final ParseTree<T> child) {
        children.add(child);
    }
}