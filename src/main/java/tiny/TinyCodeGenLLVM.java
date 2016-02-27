package tiny;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * experimental LLVM backend for TINY. this code is not pretty, but it works
 * */
public class TinyCodeGenLLVM implements TinyCodeGen {

    private static class Counter {
        private int counter;
        public Counter(final int counter) { this.counter = counter; }
        public int next() { return counter++; }
    }

    public String generate(final Ast ast) {
        final Counter uid = new Counter(0);
        final List<String> result = new ArrayList<>();
        result.add(pre());
        // allocate a variable for each identifier
        getIdentifiers(ast).forEach(id -> result.add("  %" + id + " = alloca i32"));
        result.addAll(emit(ast, "", uid));
        result.add(post());
        return String.join("\n", result);
    }

    private Set<String> getIdentifiers(final Ast ast) {
        final Set<String> result = new HashSet<>();
        getIdentifiers(ast, result);
        return result;
    }

    private void getIdentifiers(final Ast ast, final Set<String> identifiers) {
        if (ast instanceof Ast.Id) {
            final Ast.Id id = (Ast.Id) ast;
            final String name = id.getName();
            if (!identifiers.contains(name)) { identifiers.add(id.getName()); }
        }
        else if (ast instanceof Ast.Statements) {
            final Ast.Statements statements = (Ast.Statements) ast;
            statements.getChildren().forEach(s -> getIdentifiers(s, identifiers));
        }
        else {
            for (final Field field : ast.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (Ast.class.isAssignableFrom(field.getType())) {
                    try {
                        getIdentifiers((Ast)field.get(ast), identifiers);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private List<String> emit(final Ast ast, final String var, final Counter uid) {
        if (ast instanceof Ast.Assign) { return emit((Ast.Assign)ast, var, uid); }
        if (ast instanceof Ast.Div) { return emit((Ast.Div)ast, var, uid); }
        if (ast instanceof Ast.Equals) { return emit((Ast.Equals)ast, var, uid); }
        if (ast instanceof Ast.Id) { return emit((Ast.Id)ast, var, uid); }
        if (ast instanceof Ast.IfThen) { return emit((Ast.IfThen)ast, var, uid); }
        if (ast instanceof Ast.IfThenElse) { return emit((Ast.IfThenElse)ast, var, uid); }
        if (ast instanceof Ast.LessThan) { return emit((Ast.LessThan)ast, var, uid); }
        if (ast instanceof Ast.Minus) { return emit((Ast.Minus)ast, var, uid); }
        if (ast instanceof Ast.Num) { return emit((Ast.Num)ast, var, uid); }
        if (ast instanceof Ast.Plus) { return emit((Ast.Plus)ast, var, uid); }
        if (ast instanceof Ast.Read) { return emit((Ast.Read)ast, var, uid); }
        if (ast instanceof Ast.Repeat) { return emit((Ast.Repeat)ast, var, uid); }
        if (ast instanceof Ast.Statements) { return emit((Ast.Statements)ast, var, uid); }
        if (ast instanceof Ast.Times) { return emit((Ast.Times)ast, var, uid); }
        if (ast instanceof Ast.Write) { return emit((Ast.Write)ast, var, uid); }
        throw new IllegalStateException("unhandled Ast case");
    }

    /** generate the instructions for each statement and concatenate them all together */
    private List<String> emit(final Ast.Statements statements, final String var, final Counter uid) {
        final List<String> result = new ArrayList<>();
        for (final Ast child : statements.getChildren()) { result.addAll(emit(child, var, uid)); }
        return result;
    }

    private List<String> emit(final Ast.Id id, final String var, final Counter uid) {
        return singletonList(
                // load the value of the variable which id represents into var
                "  %" + var + " = load i32* %" + id.getName() 
        );
    }

    private List<String> emit(final Ast.Num num, final String var, final Counter uid) {
        final String n = "num" + uid.next();
        return asList(
                // allocate memory for var
                "  %" + n + " = alloca i32",
                // store the value of num into the variable
                "  store i32 " + num.getValue() + ", i32* %" + n,
                "  %" + var + " = load i32* %" + n
        );
    }

    private String scanf(final String id) {
        return "i32 (i8*, ...)* " +
                "@__isoc99_scanf(i8* getelementptr inbounds ([3 x i8]* @.str, i32 0, i32 0), i32* %" + id + ")";
    }

    /** read from stdin and store into memory location associated with id */
    private List<String> emit(final Ast.Read read, final String var, final Counter uid) {
        final String id = ((Ast.Id)read.getIdentifier()).getName();
        return singletonList("  %read" + uid.next() + " = call " + scanf(id));
    }

    private String printf(final String var, final Counter uid) {
        return "  %tmp" + uid.next() +
                " = call i32 (i8*, ...)* " +
                "@printf(i8* getelementptr inbounds ([4 x i8]* @.str1, i32 0, i32 0), i32 %" + var + ")";
    }

    /** compute the given expression and write it to stdout */
    private List<String> emit(final Ast.Write write, final String var, final Counter uid) {
        final List<String> result = new ArrayList<>();
        final String tmp = "tmp" + uid.next();
        // first compute the expression
        result.addAll(emit(write.getExp(), tmp, uid));
        // then print the result
        result.add(printf(tmp, uid));
        return result;
    }

    private List<String> emit(final Ast.IfThen ifThen, final String var, final Counter uid) {
        final List<String> result = new ArrayList<>();
        final int n = uid.next();
        final String cond = "cond" + n;
        final String then = "then" + n;
        final String end = "end" + n;
        // evaluate the condition
        result.addAll(emit(ifThen.getIfPart(), cond, uid));
        result.addAll(asList(
                // if condition is true, choose the 'then' branch.  otherwise, jump to 'end'
                "  br i1 %" + cond + ", label %" + then + ", label %" + end,
                // label 'then' branch
                then + ":"
        ));
        // generate code for the 'then' branch'
        result.addAll(emit(ifThen.getThenPart(), "", uid));
        result.add("  br label %" + end);
        // 'end' label
        result.add(end + ":");
        return result;
    }

    private List<String> emit(final Ast.IfThenElse ifThenElse, final String var, final Counter uid) {
        final List<String> result = new ArrayList<>();
        final int n = uid.next();
        final String cond = "cond" + n;
        final String then = "then" + n;
        final String else_ = "else" + n;
        final String end = "end" + n;
        // evaluate the condition
        result.addAll(emit(ifThenElse.getIfPart(), cond, uid));
        result.addAll(asList(
                // if the condition is true, choose the 'then' branch.  otherwise, jump to 'else' branch
                "  br i1 %" + cond + ", label %" + then + ", label %" + else_,
                // label 'then' branch
                then + ":"
        ));
        // generate code for 'then' branch
        result.addAll(emit(ifThenElse.getThenPart(), "", uid));
        // jump over 'else' branch
        result.add("  br label %" + end);
        // label for 'else' branch
        result.add(else_ + ":");
        // generate code for 'else' branch
        result.addAll(emit(ifThenElse.getElsePart(), "", uid));
        result.add("  br label %" + end);
        // 'end' label
        result.add(end + ":");
        return result;
    }

    /** evaluate an exp and store it in the memory associated with the given id */
    private List<String> emit(final Ast.Assign assign, final String var, final Counter uid) {
        final List<String> result = new ArrayList<>();
        final String tmp = "tmp" + uid.next();
        final String id = ((Ast.Id)assign.getIdentifier()).getName();
        result.addAll(emit(assign.getExp(), tmp, uid));
        result.add("  store i32 %" + tmp + ", i32* %" + id );
        return result;
    }

    private List<String> emit(final Ast.Plus plus, final String var, final Counter uid) {
        final List<String> result = new ArrayList<>();
        final int n = uid.next();
        final String left = "left" + n;
        final String right = "right" + n;
        result.addAll(emit(plus.getLeft(), left, uid));
        result.addAll(emit(plus.getRight(), right, uid));
        result.add("  %" + var + " = add i32 %" + left + ", %" + right);
        return result;
    }

    private List<String> emit(final Ast.Minus minus, final String var, final Counter uid) {
        final List<String> result = new ArrayList<>();
        final int n = uid.next();
        final String left = "left" + n;
        final String right = "right" + n;
        result.addAll(emit(minus.getLeft(), left, uid));
        result.addAll(emit(minus.getRight(), right, uid));
        result.add("  %" + var + " = sub i32 %" + left + ", %" + right);
        return result;
    }

    private List<String> emit(final Ast.Times times, final String var, final Counter uid) {
        final List<String> result = new ArrayList<>();
        final int n = uid.next();
        final String left = "left" + n;
        final String right = "right" + n;
        result.addAll(emit(times.getLeft(), left, uid));
        result.addAll(emit(times.getRight(), right, uid));
        result.add("  %" + var + " = mul i32 %" + left + ", %" + right);
        return result;
    }

    private List<String> emit(final Ast.Div div, final String var, final Counter uid) {
        final List<String> result = new ArrayList<>();
        final int n = uid.next();
        final String left = "left" + n;
        final String right = "right" + n;
        result.addAll(emit(div.getLeft(), left, uid));
        result.addAll(emit(div.getRight(), right, uid));
        result.add("  %" + var + " = sdiv i32 %" + left + ", %" + right);
        return result;
    }

    private List<String> emit(final Ast.LessThan lessThan, final String var, final Counter uid) {
        final List<String> result = new ArrayList<>();
        final int n = uid.next();
        final String left = "left" + n;
        final String right = "right" + n;
        result.addAll(emit(lessThan.getLeft(), left, uid));
        result.addAll(emit(lessThan.getRight(), right, uid));
        result.add("  %" + var + " = icmp slt i32 %" + left + ", %" + right);
        return result;
    }

    private List<String> emit(final Ast.Equals equals, final String var, final Counter uid) {
        final List<String> result = new ArrayList<>();
        final int n = uid.next();
        final String left = "left" + n;
        final String right = "right" + n;
        result.addAll(emit(equals.getLeft(), left, uid));
        result.addAll(emit(equals.getRight(), right, uid));
        result.add("  %" + var + " = icmp eq i32 %" + left + ", %" + right);
        return result;
    }

    /** generate code for a repeat loop */
    private List<String> emit(final Ast.Repeat repeat, final String var, final Counter uid) {
        final List<String> result = new ArrayList<>();
        final int n = uid.next();
        final String rep = "repeat" + n;
        final String cond = "cond" + n;
        final String end = "end" + n;
        result.addAll(asList(
                "  br label %" + rep,
                rep + ":"
        ));
        result.addAll(emit(repeat.getBody(), "", uid));
        result.addAll(emit(repeat.getExp(), cond, uid));
        result.addAll(asList(
                "  br i1 %" + cond + ", label %" + end + ", label %" + rep,
                end + ":"
        ));
        return result;
    }

    private String pre() {
        return  // this is to silence a clang warning.  remove if using different target
                "target triple = \"x86_64-pc-linux-gnu\"\n\n" +
                // string constant for scanf("%d",&x)
                "@.str = private unnamed_addr constant [3 x i8] c\"%d\\00\"\n" +
                // string constant for printf("%d\n",x)
                "@.str1 = private unnamed_addr constant [4 x i8] c\"%d\\0A\\00\"\n\n" +
                // start of main function
                "define i32 @main() {\n" +
                // start of code
                "entry:";
    }

    private String post() {
        return  // the end of the main function
                "  ret i32 0 \n}\n\n" +
                // declaration of scanf from stdio
                "declare i32 @__isoc99_scanf(i8*, ...) \n" +
                // declaration of printf from stdio
                "declare i32 @printf(i8*, ...) ";
    }
}