/* Joshua Graydus | January 2016 */
package tiny;

import token.Source;

import java.util.List;

/** The tokens of the TINY language */
public class Token {
    protected final Source<Character> src;
    protected final Type type;

    private Token(final Source<Character> src, final Type type) {
        this.src = src;
        this.type = type;
    }

    public Source<Character> getSrc() { return src; }

    public enum Type {
        IF, THEN, ELSE, READ, REPEAT, UNTIL, WRITE, COMMENT, SEMICOLON, END_OF_FILE, EQUAL, PLUS,
        MINUS, TIMES, OVER, LESS_THAN, LEFT_PAREN, RIGHT_PAREN, ASSIGNMENT, END, IDENTIFIER, NUM
    }

    public static class If extends Token { 
        public If(final Source<Character> src) { super(src, Type.IF); } 
    }
    
    public static class Then extends Token { 
        public Then(final Source<Character> src) { super(src, Type.THEN); } 
    }
    
    public static class Else extends Token { 
        public Else(final Source<Character> src) { super(src, Type.ELSE); } 
    }
    
    public static class Read extends Token { 
        public Read(final Source<Character> src) { super(src, Type.READ); } 
    }
    
    public static class Repeat extends Token { 
        public Repeat(final Source<Character> src) { super(src, Type.REPEAT); } 
    }
    
    public static class Until extends Token { 
        public Until(final Source<Character> src) { super(src, Type.UNTIL); } 
    }
    
    public static class Write extends Token { 
        public Write(final Source<Character> src) { super(src, Type.WRITE); } 
    }
    
    public static class Comment extends Token { 
        public Comment(final Source<Character> src) { super(src, Type.COMMENT); } 
    }
    
    public static class Semicolon extends Token { 
        public Semicolon(final Source<Character> src) { super(src, Type.SEMICOLON); } 
    }
    
    public static class EndOfFile extends Token { 
        public EndOfFile(final Source<Character> src) { super(src, Type.END_OF_FILE); } 
    }
    
    public static class Equal extends Token { 
        public Equal(final Source<Character> src) { super(src, Type.EQUAL); } 
    }
    
    public static class Plus extends Token { 
        public Plus(final Source<Character> src) { super(src, Type.PLUS); } 
    }
    
    public static class Minus extends Token { 
        public Minus(final Source<Character> src) { super(src, Type.MINUS); } 
    }
    
    public static class Times extends Token { 
        public Times(final Source<Character> src) { super(src, Type.TIMES); } 
    }
    
    public static class Over extends Token { 
        public Over(final Source<Character> src) { super(src, Type.OVER); } 
    }
    
    public static class LessThan extends Token { 
        public LessThan(final Source<Character> src) { super(src, Type.LESS_THAN); } 
    }
    
    public static class LeftParens extends Token { 
        public LeftParens(final Source<Character> src) { super(src, Type.LEFT_PAREN); } 
    }
    
    public static class RightParens extends Token { 
        public RightParens(final Source<Character> src) { super(src, Type.RIGHT_PAREN); } 
    }
    
    public static class Assignment extends Token { 
        public Assignment(final Source<Character> src) { super(src, Type.ASSIGNMENT); } 
    }
    
    public static class End extends Token { 
        public End(final Source<Character> src) { super(src, Type.END); } 
    }

    public static class Identifier extends Token {
        private final String id;

        public Identifier(final Source<Character> src, final List<Character> chars) {
            super(src, Type.IDENTIFIER);
            final StringBuilder sb = new StringBuilder();
            chars.forEach(sb::append);
            this.id = sb.toString();
        }

        public String getValue() { return id; }

        @Override public String toString() { return this.getClass().getSimpleName() + "[" + id + "]"; }
        @Override public boolean equals(final Object o) { return super.equals(o) && id.equals(((Identifier)o).id); }
    }

    public static class Num extends Token {
        private final int num;

        public Num(final Source<Character> src, final List<Character> strs) {
            super(src, Type.NUM);
            final StringBuilder sb = new StringBuilder();
            strs.forEach(sb::append);
            this.num = Integer.parseInt(sb.toString());
        }

        public int getValue() { return num; }

        @Override public String toString() {return this.getClass().getSimpleName() + "[" + num + "]"; }
        @Override public boolean equals(final Object other) { return super.equals(other) && num == ((Num)other).num; }
    }

    @Override public String toString() { return this.getClass().getSimpleName(); }
    @Override public boolean equals(final Object o) { return this == o || type == ((Token)o).type; }
}