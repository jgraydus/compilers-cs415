/* Joshua Graydus | January 2016 */
package data;

/**
 * A container for aggregating exactly two values of potentially different types.
 */
public class Pair<L,R> {
    private final L l;
    private final R r;

    private Pair(L l, R r) {
        if (l == null || r == null) throw new IllegalArgumentException("null values not permitted");
        this.l = l;
        this.r = r;
    }

    public L getLeft() {return l;}
    public R getRight() {return r;}

    public static <A,B> Pair<A,B> of(A a, B b) { return new Pair<>(a,b); }
}
