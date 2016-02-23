/* Joshua Graydus | February 2016 */
package parser;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class ParseTree<T> {
    private final Symbol s;
    private T t;
    private final List<ParseTree<T>> children = new LinkedList<>();

    public ParseTree(final Symbol s, final T t) {
        this.s = s;
        this.t = t;
    }

    public void setT(final T t) { this.t = t; }
    public T getT() { return t; }
    public Symbol getSymbol() { return s; }
    public List<ParseTree<T>> getChildren() { return unmodifiableList(children); }
    public void addChild(final ParseTree<T> child) { children.add(child); }

    @Override public String toString() {  return string(0); }

    private String string(final int depth) {
        final StringBuilder sb = new StringBuilder();
        for (int i=0; i<depth; i++) { sb.append("   "); }
        sb.append("Symbol="+s+", Token="+t);
        children.forEach(child -> {
            sb.append("\n");
            sb.append(child.string(depth + 1));
        });
        return sb.toString();
    }
}