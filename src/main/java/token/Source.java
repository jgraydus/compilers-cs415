/* Joshua Graydus | January 2016 */
package token;

import data.Pair;

import java.util.Optional;

/**
 * Represents a source of items of type {@code A} from which a tokenizer reads.  For correct behavior, concrete
 * implementations of {@code Source<A>} should be immutable.
 *
 * @param <A> the type of the item to be read from the source
 */
public interface Source<A> {
    /**
     * @return a {@code Pair} whose left value is the next item in this source, if there is one, or
     * {@code Optional.empty} if this source has been entirely consumed, and whose right value is a {@code Source<A>}
     * object representing the remaining unread portion of this source.
     */
    Pair<Optional<A>, Source<A>> getNext();
}