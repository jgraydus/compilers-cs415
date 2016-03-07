/* Joshua Graydus | March 2016 */
package cm;

import token.Source;

import java.util.List;

import static java.util.stream.Collectors.toList;

/** The tokens of the C-- language */
public abstract class Token {
    protected final Source<Character> src;
    protected final Type type;

    private Token(final Source<Character> src, final Type type){
        this.src = src;
        this.type = type;
    }

    public enum Type {
        ELSE, IF, INT, RETURN, VOID, WHILE,
        PLUS, MINUS, MULTIPLY, DIVIDE, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL,
        EQUAL, NOT_EQUAL, ASSIGN, SEMICOLON, COMMA, LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET, LEFT_BRACE,
        RIGHT_BRACE, COMMENT,
        ID, NUM
    }

    public static class Else extends Token {
        public Else(final Source<Character> src) { super(src, Type.ELSE); }
    }

    public static class If extends Token {
        public If(final Source<Character> src) { super(src, Type.IF); }
    }

    public static class Int extends Token {
        public Int(final Source<Character> src) { super(src, Type.INT); }
    }

    public static class Return extends Token {
        public Return(final Source<Character> src) { super(src, Type.RETURN); }
    }

    public static class Void extends Token {
        public Void(final Source<Character> src) { super(src, Type.VOID); }
    }

    public static class While extends Token {
        public While(final Source<Character> src) { super(src, Type.WHILE); }
    }

    public static class Plus extends Token {
        public Plus(final Source<Character> src) { super(src, Type.PLUS); }
    }

    public static class Minus extends Token {
        public Minus(final Source<Character> src) { super(src, Type.MINUS); }
    }

    public static class Multiply extends Token {
        public Multiply(final Source<Character> src) { super(src, Type.MULTIPLY); }
    }

    public static class Divide extends Token {
        public Divide(final Source<Character> src) { super(src, Type.DIVIDE); }
    }

    public static class LessThan extends Token {
        public LessThan(final Source<Character> src) { super(src, Type.LESS_THAN); }
    }

    public static class LessThanOrEqual extends Token {
        public LessThanOrEqual(final Source<Character> src) { super(src, Type.LESS_THAN_OR_EQUAL); }
    }

    public static class GreaterThan extends Token {
        public GreaterThan(final Source<Character> src) { super(src, Type.GREATER_THAN); }
    }

    public static class GreaterThanOrEqual extends Token {
        public GreaterThanOrEqual(final Source<Character> src) { super(src, Type.GREATER_THAN_OR_EQUAL); }
    }

    public static class Equal extends Token {
        public Equal(final Source<Character> src) { super(src, Type.EQUAL); }
    }

    public static class NotEqual extends Token {
        public NotEqual(final Source<Character> src) { super(src, Type.NOT_EQUAL); }
    }

    public static class Assign extends Token {
        public Assign(final Source<Character> src) { super(src, Type.ASSIGN); }
    }

    public static class Semicolon extends Token {
        public Semicolon(final Source<Character> src) { super(src, Type.SEMICOLON); }
    }

    public static class Comma extends Token {
        public Comma(final Source<Character> src) { super(src, Type.COMMA); }
    }

    public static class LeftParen extends Token {
        public LeftParen(final Source<Character> src) { super(src, Type.LEFT_PAREN); }
    }

    public static class RightParen extends Token {
        public RightParen(final Source<Character> src) { super(src, Type.RIGHT_PAREN); }
    }

    public static class LeftBracket extends Token {
        public LeftBracket(final Source<Character> src) { super(src, Type.LEFT_BRACKET); }
    }

    public static class RightBracket extends Token {
        public RightBracket(final Source<Character> src) { super(src, Type.RIGHT_BRACKET); }
    }

    public static class LeftBrace extends Token {
        public LeftBrace(final Source<Character> src) { super(src, Type.LEFT_BRACE); }
    }

    public static class RightBrace extends Token {
        public RightBrace(final Source<Character> src) { super(src, Type.RIGHT_BRACE); }
    }

    public static class Comment extends Token {
        public Comment(final Source<Character> src) { super(src, Type.COMMENT); }
    }

    public static class Id extends Token {
        private final String name;

        public Id(final Source<Character> src, final List<Character> chars) {
            super(src, Type.ID);
            name = String.join("", chars.stream().map(c -> c.toString()).collect(toList()));
        }

        public String getName() { return name; }

        @Override public String toString() { return this.getClass().getSimpleName() + "[" + name + "]"; }
        @Override public boolean equals(final Object o) { return super.equals(o) && name.equals(((Id)o).name); }
    }

    public static class Num extends Token {
        private final int value;

        public Num(final Source<Character> src, final List<Character> chars) {
            super(src, Type.NUM);
            value = Integer.parseInt(String.join("", chars.stream().map(c -> c.toString()).collect(toList())));
        }

        public int getValue() { return value; }

        @Override public String toString() {return this.getClass().getSimpleName() + "[" + value + "]"; }
        @Override public boolean equals(final Object other) { return super.equals(other) && value == ((Num)other).value; }
    }

    @Override public String toString() { return this.getClass().getSimpleName(); }
    @Override public boolean equals(final Object o) { return this == o || type == ((Token)o).type; }
}