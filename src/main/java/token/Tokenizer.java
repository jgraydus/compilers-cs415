/* Joshua Graydus | January 2016 */
package token;

import data.Either;
import data.Pair;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static data.Either.left;
import static data.Either.right;
import static data.Pair.of;

/**
 * A general interface for a function that reads items from a source and produces tokens, and a number of convenience
 * functions for combining tokenizers into new tokenizers.  The result of tokenize is an
 * {@code Either<Error<A>,Pair<List<T>,Source<A>>>}.  In the case of failure, the {@code Either} will contain the left
 * type, {@code Error<A>}.  On success, it will contain the right type {@code Pair<List<T>,Source<A>>}, where
 * {@code List<T>} is a list consisting of all the tokens (of type {@code T}) that were read (in the order that they
 * were read), and {@code Source<A>} will be the remaining unread portion of the input source.
 *
 * @param <T> type of the tokens in the output
 * @param <A> type of the items in the input
 */
@FunctionalInterface
public interface Tokenizer<T,A> {
    Either<Error<A>, Pair<List<T>, Source<A>>> tokenize(Source<A> source);

    /** @return a tokenizer that does nothing.  it simply passes through the source it was given */
    static <S,B> Tokenizer<S,B> succeed() {
        return source -> right(of(emptyList(), source));
    }

    /** @return a tokenizer that always fails */
    static <S,B> Tokenizer<S,B> fail() {
        return source -> left(new Error<>(source));
    }

    /** @return a tokenizer that tries 'this', but if it fails it then tries 'other' */
    default Tokenizer<T,A> or(final Tokenizer<T,A> other) {
        return source -> {
            final Either<Error<A>, Pair<List<T>, Source<A>>> result = tokenize(source);
            return result.getRight().isPresent() ? result : other.tokenize(source);
        };
    }

    /** @return a tokenizer that combines 'this' with 'other' such that 'this' must succeed and then 'other' must
     *  succeed.  the result of both 'this' and 'other' are concatenated. */
    default Tokenizer<T,A> and(final Tokenizer<T,A> other) {
        return source -> {
            // first try 'this'
            final Either<Error<A>, Pair<List<T>, Source<A>>> result1 = tokenize(source);
            // and if it succeeds, then try 'other'
            if (result1.getRight().isPresent()) {
                final Source<A> next = result1.getRight().get().getRight();
                final Either<Error<A>, Pair<List<T>, Source<A>>> result2 = other.tokenize(next);
                // if 'other' succeeds, then combine their results and succeed
                if (result2.getRight().isPresent()) {
                    final List<T> r1 = result1.getRight().get().getLeft();
                    final List<T> r2 = result2.getRight().get().getLeft();
                    final Source<A> last = result2.getRight().get().getRight();
                    return right(of(concat(r1.stream(), r2.stream()).collect(toList()), last));
                }
                // if 'other' fails, return its failure
                else { return result2; }
            }
            // if 'this' fails, return its failure
            else { return result1; }
        };
    }

    /** @returns the "negation" of 'this'.  i.e. a tokenizer that succeeds when 'this' fails and vice-versa.  this only
     *  works when the next item read from the Source is the token to be produced, hence the Tokenizer returned has
     *  the same type parameter for both item read and token produced */
    default Tokenizer<A,A> not() {
        return source -> {
            final Either<Error<A>, Pair<List<T>, Source<A>>> result = tokenize(source);
            // if 'this' succeeds, return failure
            if (result.getRight().isPresent()) {
                return Tokenizer.<A,A>fail().tokenize(source);
            }
            // if 'this' fails, return success
            else {
                final Pair<Optional<A>, Source<A>> next = source.getNext();
                if (next.getLeft().isPresent()) {
                    final A a = next.getLeft().get();
                    final Source<A> src = next.getRight();
                    return right(of(singletonList(a), src));
                }
                // unless the source is empty
                else { return Tokenizer.<A,A>fail().tokenize(source); }
            }
        };
    }

    /** @return a version of 'this' that does not consume the next item from source and does not produce a result. used
     * in situations where it's necessary to read ahead */
    default <S> Tokenizer<S,A> peek() {
        return source -> {
            final Either<Error<A>,Pair<List<T>,Source<A>>> result = tokenize(source);
            return result.getRight().isPresent()
                    ? Tokenizer.<S,A>succeed().tokenize(source)
                    : Tokenizer.<S,A>fail().tokenize(source);
        };
    }

    /** @return a tokenizer such that if 'from' succeeds, it will continue reading until 'to' succeeds.  the result
     * of 'to' is included */
    static <B> Tokenizer<B,B> fromTo(final Tokenizer<B,B> from, final Tokenizer<B,B> to) {
        return from.and(to.not().many()).and(to);
    }

    /** @return a tokenizer that repeats 'this' zero or more times */
    default Tokenizer<T,A> many() {
        return source -> {
            // try to read the next token
            final Either<Error<A>, Pair<List<T>, Source<A>>> result = tokenize(source);
            // if that fails, just return an empty list of tokens
            if (result.getLeft().isPresent()) {
                return Tokenizer.<T,A>succeed().tokenize(source);
            }
            // otherwise, concatenate the result list with the result of a recursive call
            else {
                final Pair<List<T>, Source<A>> r1 = result.getRight().get();
                // many() will never return Either.left
                final Pair<List<T>, Source<A>> r2 = many().tokenize(r1.getRight()).getRight().get();
                final List<T> tokens = concat(r1.getLeft().stream(), r2.getLeft().stream()).collect(toList());
                return right(of(tokens, r2.getRight()));
            }
        };
    }

    /** @return a tokenizer that repeats 'this' at least n times */
    default Tokenizer<T,A> atLeast(final int n) {
        if (n < 0) { throw new IllegalArgumentException("argument cannot be less than 0"); }
        // if n is 0, then match any number
        if (n == 0) { return many(); }
        // if n > 0, concatenate 'this' with a tokenizer that repeats n-1 times
        else { return and(atLeast(n-1)); }
    }

    /** @return a tokenizer that combines the given tokenizers into a sequence such that they all must succeed */
    static <S,B> Tokenizer<S,B> sequence(final List<Tokenizer<S,B>> tokenizers) {
        // start with a tokenizer that always succeeds
        Tokenizer<S,B> t = succeed();
        // combine the tokenizers with 'and'
        for (final Tokenizer<S,B> tokenizer : tokenizers) { t = t.and(tokenizer); }
        return t;
    }

    /** @return a tokenizer that combines the given tokenizers into a tokenizer such that the first one to succeed
     * yields the result */
    static <S,B> Tokenizer<S,B> oneOf(final List<Tokenizer<S,B>> tokenizers) {
        // start with a tokenizer that always fails
        Tokenizer<S,B> t = fail();
        // combine the tokenizers with 'or'
        for (final Tokenizer<S,B> tokenizer : tokenizers) { t = t.or(tokenizer); }
        return t;
    }

    /* varargs version of previous 'oneOf' for convenience */
    @SafeVarargs
    static <S,B> Tokenizer<S,B> oneOf(final Tokenizer<S,B>... tokenizers) { return oneOf(asList(tokenizers)); }

    /** @return a tokenizer that recognizes a single character */
    static Tokenizer<Character,Character> character(final char c) {
        return source -> {
            // read the next character from the source
            final Pair<Optional<Character>, Source<Character>> next = source.getNext();
            // if the read character is the one we want, succeed
            if (next.getLeft().isPresent() && next.getLeft().get().equals(c)) {
                return right(of(singletonList(c), next.getRight()));
            }
            // otherwise fail
            else { return Tokenizer.<Character,Character>fail().tokenize(source); }
        };
    }

    /** @return a tokenizer that recognizes any upper or lower case letter */
    static Tokenizer<Character,Character> letter() {
        return source -> {
            final Pair<Optional<Character>, Source<Character>> next = source.getNext();
            if (next.getLeft().isPresent()) {
                final char c = next.getLeft().get();
                if (Character.isLetter(c)) { return right(of(singletonList(c), next.getRight())); }
            }
            return Tokenizer.<Character,Character>fail().tokenize(source);
        };
    }

    /** @return a tokenizer that recognizes any digit 0-9 */
    static Tokenizer<Character,Character> digit() {
        return source -> {
            final Pair<Optional<Character>, Source<Character>> next = source.getNext();
            if (next.getLeft().isPresent()) {
                final char c = next.getLeft().get();
                if (Character.isDigit(c)) { return right(of(singletonList(c), next.getRight())); }
            }
            return Tokenizer.<Character,Character>fail().tokenize(source);
        };
    }

    /** @return a tokenizer that recognizes a string from a character source */
    static Tokenizer<String,Character> string(final String str) {
        // convert the string into a list of Tokenizers to recognize the individual characters
        final List<Tokenizer<Character, Character>> charTokenizers = str.chars()
                .mapToObj(i -> (char) i) // Java stream api designers didn't feel characters deserved their own streams
                .map(Tokenizer::character)
                .collect(Collectors.toList());
        // combine them with 'sequence'
        final Tokenizer<Character, Character> t = sequence(charTokenizers);

        return source -> {
            // run the new tokenizer
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> result = t.tokenize(source);
            // and if it succeeds, return a success with the List<Character> replaced by a list containing the string
            if (result.getRight().isPresent()) {
                final Source<Character> src = result.getRight().get().getRight();
                return right(of(singletonList(str), src));
            }
            // otherwise, fail
            else { return Tokenizer.<String, Character>fail().tokenize(source); }
        };
    }

    /** @return a tokenizer that skips whitespace */
    static <B> Tokenizer<B,Character> whitespace() {
        final Tokenizer<Character,Character> t = oneOf(character(' '), character('\n'), character('\t')).atLeast(1);
        /* some trickery here. this can be generic in the type B since it's just returning an empty list on success */
        return source -> {
            final Either<Error<Character>,Pair<List<Character>,Source<Character>>> result = t.tokenize(source);
            return result.getLeft().isPresent()
                ? Tokenizer.<B,Character>fail().tokenize(source)
                : right(of(emptyList(), result.getRight().get().getRight()));
        };
    }

    /** @return a tokenizer that only succeeds when there are no items left in the source.  necessary to determine if
     * the end of the file has been reached */
    static <B,C> Tokenizer<B,C> emptySource() {
        /* generic in both input and output since it neither reads nor produces anything! */
        return source -> source.getNext().getLeft().isPresent()
                ? Tokenizer.<B,C>fail().tokenize(source)
                : Tokenizer.<B,C>succeed().tokenize(source);
    }

    /** @return a tokenizer that uses the result of 'this' to produce a different value */
    default <S> Tokenizer<S,A> convert(final BiFunction<Source<A>,List<T>, S> converter) {
        return source -> {
            // get the result of 'this'
            final Either<Error<A>,Pair<List<T>,Source<A>>> result = this.tokenize(source);
            // if successful, create a new Tokenizer that will yield whatever value converter creates from result
            if (result.getRight().isPresent()) {
                final Pair<List<T>,Source<A>> pair = result.getRight().get();
                final List<T> list = pair.getLeft();
                final Source<A> src = pair.getRight();
                return right(of(singletonList(converter.apply(src, list)), src));
            }
            // otherwise, return a failure
            else { return Tokenizer.<S,A>fail().tokenize(source); }
        };
    }

    /** @return a tokenizer that returns a specific result if 'this' succeeds, ignoring any value produced by 'this' */
    default <S> Tokenizer<S,A> convert(final Function<Source<A>, S> converter) {
        return source -> {
            // get the result of 'this'
            final Either<Error<A>,Pair<List<T>,Source<A>>> result = this.tokenize(source);
            // if successful, create a new Tokenizer that will yield whatever converter provides.
            // note that converter ignores the result of 'this' but consumes the source
            if (result.getRight().isPresent()) {
                final Source<A> src = result.getRight().get().getRight();
                return right(of(singletonList(converter.apply(src)), src));
            }
            // otherwise, return a failure
            else { return Tokenizer.<S,A>fail().tokenize(source); }
        };
    }
}