/* Joshua Graydus | January 2016 */
package token;

/**
 * Produced by a tokenizer when it fails. The {@code Source<A>} object at which the failure occurred is
 * provided to facilitate reporting the cause of the problem.
 *
 * @param <A> the type of the item provided by the {@code getNext} method of {@code Source<A>}
 */
public class Error<A> {
    private final Source<A> src;

    public Error(Source<A> src) { this.src = src; }

    public Source<A> getSource() { return src; }
}