/* Joshua Graydus | February 2016 */
package tiny;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;

/** defines the node types for the abstract syntax tree of the TINY compiler */
public abstract class Ast {
    private final Optional<Token> token;
    private final Map<Class,Object> attributes = new HashMap<>();
    
    private Ast(final Token token) {
        this.token = Optional.ofNullable(token);
    }

    public void addAttribute(final Class key, final Object attribute) {
        attributes.put(key, attribute);
    }

    public <T> Optional<T> getAttribute(final Class<T> key) {
        return Optional.ofNullable((T) attributes.get(key));
    }

    public Optional<Token> getToken() { return token; }

    public static class Statements extends Ast {
        private final List<Ast> children;

        public Statements(final Token token, final List<Ast> children) {
            super(token);
            this.children = children;
        }

        public List<Ast> getChildren() { return unmodifiableList(children); }
    }

    public static class IfThen extends Ast {
        private final Ast ifPart;
        private final Ast thenPart;

        public IfThen(final Token token, final Ast ifPart, final Ast thenPart) {
            super(token);
            this.ifPart = ifPart;
            this.thenPart = thenPart;
        }

        public Ast getIfPart() { return ifPart; }
        public Ast getThenPart() { return thenPart; }
    }

    public static class IfThenElse extends Ast {
        private final Ast ifPart;
        private final Ast thenPart;
        private final Ast elsePart;

        public IfThenElse(final Token token, final Ast ifPart, final Ast thenPart, final Ast elsePart) {
            super(token);
            this.ifPart = ifPart;
            this.thenPart = thenPart;
            this.elsePart = elsePart;
        }

        public Ast getIfPart() { return ifPart; }
        public Ast getThenPart() { return thenPart; }
        public Ast getElsePart() { return elsePart; }
    }

    public static class Repeat extends Ast {
        private final Ast body;
        private final Ast exp;

        public Repeat(final Token token, final Ast body, final Ast exp) {
            super(token);
            this.body = body;
            this.exp = exp;
        }

        public Ast getBody() { return body; }
        public Ast getExp() { return exp; }
    }

    public static class Assign extends Ast {
        private final Ast identifier;
        private final Ast exp;

        public Assign(final Token token, final Ast identifier, final Ast exp) {
            super(token);
            this.identifier = identifier;
            this.exp = exp;
        }

        public Ast getIdentifier() { return identifier; }
        public Ast getExp() { return exp; }
    }

    public static class Read extends Ast {
        private final Ast identifier;

        public Read(final Token token, final Ast identifier) {
            super(token);
            this.identifier = identifier;
        }

        public Ast getIdentifier() { return identifier; }
    }

    public static class Write extends Ast {
        private final Ast exp;

        public Write(final Token token, final Ast exp) {
            super(token);
            this.exp = exp;
        }

        public Ast getExp() { return exp; }
    }

    public static class LessThan extends Ast {
        private final Ast left;
        private final Ast right;

        public LessThan(final Token token, final Ast left, final Ast right) {
            super(token);
            this.left = left;
            this.right = right;
        }

        public Ast getLeft() { return left; }
        public Ast getRight() { return right; }
    }

    public static class Equals extends Ast {
        private final Ast left;
        private final Ast right;

        public Equals(final Token token, final Ast left, final Ast right) {
            super(token);
            this.left = left;
            this.right = right;
        }

        public Ast getLeft() { return left; }
        public Ast getRight() { return right; }
    }

    public static class Plus extends Ast {
        private final Ast left;
        private final Ast right;

        public Plus(final Token token, final Ast left, final Ast right) {
            super(token);
            this.left = left;
            this.right = right;
        }

        public Ast getLeft() { return left; }
        public Ast getRight() { return right; }
    }

    public static class Minus extends Ast {
        private final Ast left;
        private final Ast right;

        public Minus(final Token token, final Ast left, final Ast right) {
            super(token);
            this.left = left;
            this.right = right;
        }

        public Ast getLeft() { return left; }
        public Ast getRight() { return right; }
    }

    public static class Times extends Ast {
        private final Ast left;
        private final Ast right;

        public Times(final Token token, final Ast left, final Ast right) {
            super(token);
            this.left = left;
            this.right = right;
        }

        public Ast getLeft() { return left; }
        public Ast getRight() { return right; }
    }

    public static class Div extends Ast {
        private final Ast left;
        private final Ast right;

        public Div(final Token token, final Ast left, final Ast right) {
            super(token);
            this.left = left;
            this.right = right;
        }

        public Ast getLeft() { return left; }
        public Ast getRight() { return right; }
    }

    public static class Num extends Ast {
        private final int value;

        public Num(final Token token, final int value) {
            super(token);
            this.value = value;
        }

        public int getValue() { return value; }
    }

    public static class Id extends Ast {
        private final String name;

        public Id(final Token token, final String name) {
            super(token);
            this.name = name;
        }

        public String getName() { return name; }
    }

    @Override
    public String toString() {
        return string(0);
    }

    private String string(final int depth) {
        try {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < depth; i++) {
                sb.append("   ");
            }
            sb.append(this.getClass().getSimpleName());
            if (this instanceof Statements) {
                for (final Ast ast : ((Statements)this).children) {
                    sb.append("\n");
                    sb.append(ast.string(depth+1));
                }
            }
            else if (this instanceof Id) {
                sb.append(" [");
                sb.append(((Id)this).name);
                sb.append("]");
            }
            else if (this instanceof Num) {
                sb.append(" [");
                sb.append(((Num)this).value);
                sb.append("]");
            }
            else {
                for (final Field field : this.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    final Ast ast = (Ast) field.get(this);
                    sb.append("\n");
                    sb.append(ast.string(depth+1));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
