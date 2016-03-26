/* Joshua Graydus | March 2016 */
package cm;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public abstract class Ast {
    private final Optional<Token> token;
    private final Map<Class, Object> attributes = new HashMap<>();

    private Ast(final Token token) {
        this.token = Optional.ofNullable(token);
    }

    public Optional<Token> getToken() { return token; }

    public <T> void addAttribute(final Class<T> klass, T t) { attributes.put(klass, t); }
    public <T> Optional<T> getAttribute(final Class<T> klass) { return Optional.ofNullable((T) attributes.get(klass)); }

    public enum TypeSpecifier { INT, VOID }
    public enum Operator { PLUS, MINUS, TIMES, DIVIDE, LEQ, LT, GEQ, GT, EQ, NEQ }

    public static class DeclarationList extends Ast {
        private final List<Ast> declarations;

        public DeclarationList(final Token token, final List<Ast> declarations) {
            super(token);
            this.declarations = declarations;
        }

        public List<Ast> getDeclarations() { return declarations; }
    }

    public static class VarDeclaration extends Ast {
        private final TypeSpecifier type;
        private final String name;
        private final Optional<Integer> size; // empty if not an array

        public VarDeclaration(final Token token,
                              final TypeSpecifier type,
                              final String name,
                              final Optional<Integer> size) {
            super(token);
            this.type = type;
            this.name = name;
            this.size = size;
        }

        public TypeSpecifier getType() { return type; }
        public Optional<Integer> getSize() { return size; }
        public String getName() { return name; }
    }

    public static class FunDeclaration extends Ast {
        private final TypeSpecifier type;
        private final String name;
        private final List<Ast> params;
        private final Ast body;

        public FunDeclaration(final Token token,
                              final TypeSpecifier type,
                              final String name,
                              final List<Ast> params,
                              final Ast body) {
            super(token);
            this.type = type;
            this.name = name;
            this.params = params;
            this.body = body;
        }

        public TypeSpecifier getType() { return type; }
        public String getName() { return name; }
        public List<Ast> getParams() { return params; }
        public Ast getBody() { return body; }
    }

    public static class Param extends Ast {
        private final TypeSpecifier type;
        private final String name;
        private final boolean isArray;

        public Param(final Token token, final TypeSpecifier type, final String name, final boolean isArray) {
            super(token);
            this.type = type;
            this.name = name;
            this.isArray = isArray;
        }

        public TypeSpecifier getType() { return type; }
        public String getName() { return name; }
        public boolean isArray() { return isArray; }
    }

    public static class CompoundStatement extends Ast {
        private final List<Ast> localDeclarations;
        private final List<Ast> statements;

        public CompoundStatement(final Token token, final List<Ast> localDeclarations, final List<Ast> statements) {
            super(token);
            this.localDeclarations = localDeclarations;
            this.statements = statements;
        }

        public List<Ast> getLocalDeclarations() { return localDeclarations; }
        public List<Ast> getStatements() { return statements; }
    }

    public static class Assignment extends Ast {
        private final Ast var;
        private final Ast expression;

        public Assignment(final Token token, final Ast var, final Ast expression) {
            super(token);
            this.var = var;
            this.expression = expression;
        }

        public Ast getVar() { return var; }
        public Ast getExpression() { return expression; }
    }

    public static class Expression extends Ast {
        private final Ast left;
        private final Optional<Operator> op;
        private final Optional<Ast> right;

        public Expression(final Token token, final Ast left, final Optional<Operator> op, final Optional<Ast> right) {
            super(token);
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public Ast getLeft() { return left; }
        public Optional<Operator> getOp() { return op; }
        public Optional<Ast> getRight() { return right; }
    }

    public static class Var extends Ast {
        private final String name;
        private final Optional<Ast> expression;

        public Var(final Token token, final String name, final Optional<Ast> expression) {
            super(token);
            this.name = name;
            this.expression = expression;
        }

        public String getName() { return name; }
        public Optional<Ast> getExpression() { return expression; }
    }

    public static class ExpressionStmt extends Ast {
        private final Optional<Ast> expression;

        public ExpressionStmt(final Token token, final Optional<Ast> expression) {
            super(token);
            this.expression = expression;
        }

        public Optional<Ast> getExpression() { return expression; }
    }

    public static class Constant extends Ast {
        private final int value;

        public Constant(final Token token, final int value) {
            super(token);
            this.value = value;
        }

        public int getValue() { return value; }
    }

    public static class Call extends Ast {
        private final String name;
        private final List<Ast> args;

        public Call(final Token token, final String name, final List<Ast> args) {
            super(token);
            this.name = name;
            this.args = args;
        }

        public String getName() { return name; }
        public List<Ast> getArgs() { return args; }
    }

    public static class IfThen extends Ast {
        private final Ast condition;
        private final Ast thenPart;

        public IfThen(final Token token, final Ast condition, final Ast thenPart) {
            super(token);
            this.condition = condition;
            this.thenPart = thenPart;
        }

        public Ast getCondition() { return condition; }
        public Ast getThenPart() { return thenPart; }
    }

    public static class IfThenElse extends Ast {
        private final Ast condition;
        private final Ast thenPart;
        private final Ast elsePart;

        public IfThenElse(final Token token, final Ast condition, final Ast thenPart, final Ast elsePart) {
            super(token);
            this.condition = condition;
            this.thenPart = thenPart;
            this.elsePart = elsePart;
        }

        public Ast getCondition() { return condition; }
        public Ast getThenPart() { return thenPart; }
        public Ast getElsePart() { return elsePart; }
    }

    public static class While extends Ast {
        private final Ast condition;
        private final Ast body;

        public While(final Token token, final Ast condition, final Ast body) {
            super(token);
            this.condition = condition;
            this.body = body;
        }

        public Ast getCondition() { return condition; }
        public Ast getBody() { return body; }
    }

    public static class Return extends Ast {
        private final Optional<Ast> expression;

        public Return(final Token token, final Optional<Ast> expression) {
            super(token);
            this.expression = expression;
        }

        public Optional<Ast> getExpression() { return expression; }
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