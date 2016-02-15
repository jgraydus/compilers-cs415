/* Joshua Graydus | January 2016 */
package token;

import data.Pair;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A {@code Source<Character>} which produces items from a given string.
 */
public class CharacterSource implements Source<Character> {
    private final String src;
    private final int cursor; // this is the position in src of the next character to read

    public CharacterSource(String src) {
        if (src == null) throw new IllegalArgumentException("the provided source string is null");
        this.src = src;
        this.cursor = 0;
    }

    private CharacterSource(String src, int cursor) {
        this.src = src;
        this.cursor = cursor;
    }

    /* as this class is immutable, to provide the next character and advance the cursor we just make a new
     * instance with the cursor incremented by one. the same String reference is used. there are no expensive
     * substring operations. */
    public Pair<Optional<Character>, Source<Character>> getNext() {
        return cursor < src.length()
            ? Pair.of(Optional.of(src.charAt(cursor)), new CharacterSource(src, cursor + 1))
            : Pair.of(Optional.empty(), this);
    }

    /** this toString is designed to provide a line number, the line of text, and a ^ showing the cursor in that
     *  line for use in reporting errors. example output:<br>
     *  <br>
     *  <pre>
     *      at line 23:
     *           if (x == 0 && y == 0)) {
     *                                ^
     *  </pre>
     *  */
    @Override
    public String toString() {
        int lineNumber = 1;
        int pos = 0;
        final BufferedReader reader = new BufferedReader(new StringReader(src));
        for (final String line : reader.lines().collect(Collectors.toList())) {
            final int length = line.length() + 1;// add one for the newline removed by reader.lines()
            if (pos + length > cursor) {
                final StringBuilder sb = new StringBuilder();
                // line number
                sb.append("at line ");
                // the src line
                sb.append(lineNumber); sb.append(":\n"); sb.append(line); sb.append("\n");
                // the ^ character
                for (int i = 0; i< cursor -pos; i++) { sb.append(" "); }
                sb.append("^");
                return sb.toString();
            }
            lineNumber++;
            pos += length;
        }
        throw new IllegalStateException("cursor beyond end of source"); // if this happens, there's a bug
    }
}