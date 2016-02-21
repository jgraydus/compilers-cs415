/* Joshua Graydus | February 2016 */
package tiny;

public class TinyParseException extends RuntimeException {
    private final Token token;
    public TinyParseException(final Token token) { this.token = token; }
    public Token getToken() { return token; }
}