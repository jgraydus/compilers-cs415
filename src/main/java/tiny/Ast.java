/* Joshua Graydus | February 2016 */
package tiny;

import java.lang.reflect.Field;
import java.util.List;

public abstract class Ast {

    public static class Statements extends Ast {
        private final List<Ast> children;

        public Statements(final List<Ast> children) {
            this.children = children;
        }
    }

    public static class IfThen extends Ast {
        private final Ast ifPart;
        private final Ast thenPart;

        public IfThen(final Ast ifPart, final Ast thenPart) {
            this.ifPart = ifPart;
            this.thenPart = thenPart;
        }
    }

    public static class IfThenElse extends Ast {
        private final Ast ifPart;
        private final Ast thenPart;
        private final Ast elsePart;

        public IfThenElse(final Ast ifPart, final Ast thenPart, final Ast elsePart) {
            this.ifPart = ifPart;
            this.thenPart = thenPart;
            this.elsePart = elsePart;
        }
    }

    public static class Repeat extends Ast {
        private final Ast body;
        private final Ast exp;

        public Repeat(final Ast body, final Ast exp) {
            this.body = body;
            this.exp = exp;
        }
    }

    public static class Assign extends Ast {
        private final Ast identifier;
        private final Ast exp;

        public Assign(final Ast identifier, final Ast exp) {
            this.identifier = identifier;
            this.exp = exp;
        }
    }

    public static class Read extends Ast {
        private final Ast identifier;

        public Read(final Ast identifier) {
            this.identifier = identifier;
        }
    }

    public static class Write extends Ast {
        private final Ast identifier;

        public Write(final Ast identifier) {
            this.identifier = identifier;
        }
    }

    public static class LessThan extends Ast {
        private final Ast left;
        private final Ast right;

        public LessThan(final Ast left, final Ast right) {
            this.left = left;
            this.right = right;
        }
    }

    public static class Equals extends Ast {
        private final Ast left;
        private final Ast right;

        public Equals(final Ast left, final Ast right) {
            this.left = left;
            this.right = right;
        }
    }

    public static class Plus extends Ast {
        private final Ast left;
        private final Ast right;

        public Plus(final Ast left, final Ast right) {
            this.left = left;
            this.right = right;
        }
    }

    public static class Minus extends Ast {
        private final Ast left;
        private final Ast right;

        public Minus(final Ast left, final Ast right) {
            this.left = left;
            this.right = right;
        }
    }

    public static class Times extends Ast {
        private final Ast left;
        private final Ast right;

        public Times(final Ast left, final Ast right) {
            this.left = left;
            this.right = right;
        }
    }

    public static class Div extends Ast {
        private final Ast left;
        private final Ast right;

        public Div(final Ast left, final Ast right) {
            this.left = left;
            this.right = right;
        }
    }

    public static class Num extends Ast {
        private final int value;

        public Num(final int value) {
            this.value = value;
        }
    }

    public static class Id extends Ast {
        private final String value;

        public Id(final String value) {
            this.value = value;
        }
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
                sb.append(((Id)this).value);
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
