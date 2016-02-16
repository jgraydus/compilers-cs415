package sandbox;

import java.util.LinkedList;
import java.util.List;

public class ParseTree<T> {
    private final T t;
    private final List<ParseTree<T>> children = new LinkedList<>();

    public ParseTree(final T t) {
        this.t = t;
    }

    public void addChild(final ParseTree<T> child) {
        children.add(child);
    }
}