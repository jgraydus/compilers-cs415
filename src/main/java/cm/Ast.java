/* Joshua Graydus | March 2016 */
package cm;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class Ast {

    public enum TypeSpecifier { INT, VOID }
    public enum Operator { PLUS, MINUS, TIMES, DIVIDE, LEQ, LT, GEQ, GT, EQ, NEQ }

    public static class DeclarationList extends Ast {
        private final List<Ast> declarations;

        public DeclarationList(final List<Ast> declarations) {
            this.declarations = declarations;
        }
    }

    public static class VarDeclaration extends Ast {
        private final TypeSpecifier type;
        private final String name;
        private final Optional<Integer> size; // empty if not an array

        public VarDeclaration(final TypeSpecifier type, final String name, final Optional<Integer> size) {
            this.type = type;
            this.name = name;
            this.size = size;
        }
    }

    public static class FunDeclaration extends Ast {
        private final TypeSpecifier type;
        private final String name;
        private final List<Ast> params;
        private final Ast body;

        public FunDeclaration(final TypeSpecifier type,
                              final String name,
                              final List<Ast> params,
                              final Ast body) {
            this.type = type;
            this.name = name;
            this.params = params;
            this.body = body;
        }
    }

    public static class Param extends Ast {
        private final TypeSpecifier type;
        private final String name;
        private final boolean isArray;

        public Param(final TypeSpecifier type, final String name, final boolean isArray) {
            this.type = type;
            this.name = name;
            this.isArray = isArray;
        }
    }

    public static class CompoundStatement extends Ast {
        private final List<Ast> localDeclarations;
        private final List<Ast> statements;

        public CompoundStatement(final List<Ast> localDeclarations, final List<Ast> statements) {
            this.localDeclarations = localDeclarations;
            this.statements = statements;
        }
    }

    public static class Assignment extends Ast {
        final Ast var;
        final Ast expression;

        public Assignment(final Ast var, final Ast expression) {
            this.var = var;
            this.expression = expression;
        }
    }

    public static class Expression extends Ast {
        final Ast left;
        final Optional<Operator> op;
        final Optional<Ast> right;

        public Expression(final Ast left, final Optional<Operator> op, final Optional<Ast> right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }
    }

    public static class Var extends Ast {
        final String name;
        final Optional<Ast> expression;

        public Var(final String name, final Optional<Ast> expression) {
            this.name = name;
            this.expression = expression;
        }
    }

    public static class ExpressionStmt extends Ast {
        final Optional<Ast> expression;

        public ExpressionStmt(final Optional<Ast> expression) {
            this.expression = expression;
        }
    }

    public static class Constant extends Ast {
        private final int value;

        public Constant(final int value) {
            this.value = value;
        }
    }

    public static class Call extends Ast {
        private final String name;
        private final List<Ast> args;

        public Call(final String name, final List<Ast> args) {
            this.name = name;
            this.args = args;
        }
    }

    public static class IfThen extends Ast {
        private final Ast condition;
        private final Ast thenPart;

        public IfThen(final Ast condition, final Ast thenPart) {
            this.condition = condition;
            this.thenPart = thenPart;
        }
    }

    public static class IfThenElse extends Ast {
        private final Ast condition;
        private final Ast thenPart;
        private final Ast elsePart;

        public IfThenElse(final Ast condition, final Ast thenPart, final Ast elsePart) {
            this.condition = condition;
            this.thenPart = thenPart;
            this.elsePart = elsePart;
        }
    }

    public static class While extends Ast {
        private final Ast condition;
        private final Ast body;

        public While(final Ast condition, final Ast body) {
            this.condition = condition;
            this.body = body;
        }
    }

    public static class Return extends Ast {
        final Optional<Ast> expression;

        public Return(final Optional<Ast> expression) {
            this.expression = expression;
        }
    }

    @Override
    public String toString() {
        try { return string(0); }
        catch (final Exception e) { throw new RuntimeException(e); }
    }

    /* converts an AST into a string using indentation to indicate subtrees */
    private String string(final int depth) throws Exception {
        final String indent = indent(depth);
        final StringBuilder sb = new StringBuilder();

        final List<String> attributes = new ArrayList<>();
        final List<String> children = new ArrayList<>();
        attributes.add(this.getClass().getSimpleName());

        for (final Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            final Class type = field.getType();
            if (Ast.class.isAssignableFrom(type)) {
                final Ast ast = (Ast) field.get(this);
                children.add(ast.string(depth+1));
            }
            else if (Collection.class.isAssignableFrom(type)) {
                final ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                if (Ast.class.isAssignableFrom((Class)genericType.getActualTypeArguments()[0])) {
                    final Collection<Ast> asts = (Collection<Ast>) field.get(this);
                    for (final Ast ast : asts) {
                        children.add(ast.string(depth+1));
                    }
                }
            }
            else if (Optional.class.isAssignableFrom(type)) {
                final ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                if (Ast.class.isAssignableFrom((Class)genericType.getActualTypeArguments()[0])) {
                    final Optional<Ast> opt = (Optional<Ast>) field.get(this);
                    if (opt.isPresent()) {
                        children.add(opt.get().string(depth+1));
                    }
                }
                if (Ast.Operator.class.isAssignableFrom((Class)genericType.getActualTypeArguments()[0])) {
                    final Optional<Ast.Operator> opt = (Optional<Ast.Operator>) field.get(this);
                    if (opt.isPresent()) {
                        children.add(indent(depth+1) + opt.get().toString() + "\n");
                    }
                }
            }
            else {
                final Object obj = field.get(this);
                attributes.add(obj.toString());
            }
        }

        sb.append(indent);
        sb.append(String.join(" | ", attributes));
        sb.append("\n");
        children.forEach(sb::append);

        return sb.toString();
    }

    private String indent(final int depth) {
        final StringBuilder sb = new StringBuilder();
        for (int i=0; i<depth*4; i++) { sb.append(" "); }
        return sb.toString();
    }
}