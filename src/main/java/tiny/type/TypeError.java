/* Joshua Graydus | February 2016 */
package tiny.type;

import tiny.Token;

public class TypeError {
    private final Type expected;
    private final Type actual;
    private final Token token;


    public TypeError(final Type expected, final Type actual, final Token token) {
        this.expected = expected;
        this.actual = actual;
        this.token = token;
    }

    @Override
    public String toString() {
        return "Type error: expected " + expected + ", found " + actual + "\n" + token.getSrc().toString();
    }
}