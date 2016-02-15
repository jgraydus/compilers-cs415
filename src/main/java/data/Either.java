/* Joshua Graydus | January 2016 */
package data;

import java.util.Optional;

/**
 * A container which holds exactly one value, but that value may be either of two types. Use the static factory
 * methods 'left' and 'right' to get instances. Does not allow the value to be null.
 */
public class Either<L,R> {
    private final Optional<L> l;
    private final Optional<R> r;

    private Either(L l, R r) {
        if (l == null && r == null) throw new IllegalArgumentException("null values not permitted");
        this.l = Optional.ofNullable(l);
        this.r = Optional.ofNullable(r);
    }

    public Optional<L> getLeft() { return l; }
    public Optional<R> getRight() { return r; }

    public static <A,B> Either<A,B> left(A a) { return new Either<>(a, null); }
    public static <A,B> Either<A,B> right(B b) { return new Either<>(null, b); }
}