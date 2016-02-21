package tiny;

import tiny.type.Type;
import tiny.type.TypeError;

import java.lang.reflect.Field;
import java.util.Optional;

import static java.util.Optional.empty;

public class TinyAnalyzer {

    public Optional<TypeError> typeCheck(final Ast ast) {
        try {
            // base case: literals and identifiers are always INTs
            if (ast instanceof Ast.Num || ast instanceof Ast.Id) {
                ast.addAttribute(Type.class, Type.INT);
                return empty();
            }

            // first type check all children
            if (ast instanceof Ast.Statements) {
                final Ast.Statements statements = (Ast.Statements) ast;
                for (final Ast child : statements.getChildren()) {
                    final Optional<TypeError> result = typeCheck(child);
                    if (result.isPresent()) { return result; }
                }
            } else {
                for (final Field field : ast.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    final Ast child = (Ast) field.get(ast);
                    final Optional<TypeError> result = typeCheck(child);
                    if (result.isPresent()) { return result; }
                }
            }

            // now type check self
            if (ast instanceof Ast.Plus) {
                final Ast.Plus plus = (Ast.Plus) ast;
                final Optional<TypeError> left = expect(plus.getLeft(), Type.INT);
                if (left.isPresent()) { return left; }
                final Optional<TypeError> right = expect(plus.getRight(), Type.INT);
                if (right.isPresent()) { return right; }
                ast.addAttribute(Type.class, Type.INT);
                return empty();
            }
            if (ast instanceof Ast.Minus) {
                final Ast.Minus minus = (Ast.Minus) ast;
                final Optional<TypeError> left = expect(minus.getLeft(), Type.INT);
                if (left.isPresent()) { return left; }
                final Optional<TypeError> right = expect(minus.getRight(), Type.INT);
                if (right.isPresent()) { return right; }
                ast.addAttribute(Type.class, Type.INT);
                return empty();
            }
            if (ast instanceof Ast.Times) {
                final Ast.Times times = (Ast.Times) ast;
                final Optional<TypeError> left = expect(times.getLeft(), Type.INT);
                if (left.isPresent()) { return left; }
                final Optional<TypeError> right = expect(times.getRight(), Type.INT);
                if (right.isPresent()) { return right; }
                ast.addAttribute(Type.class, Type.INT);
                return empty();
            }
            if (ast instanceof Ast.Div) {
                final Ast.Div div = (Ast.Div) ast;
                final Optional<TypeError> left = expect(div.getLeft(), Type.INT);
                if (left.isPresent()) { return left; }
                final Optional<TypeError> right = expect(div.getRight(), Type.INT);
                if (right.isPresent()) { return right; }
                ast.addAttribute(Type.class, Type.INT);
                return empty();
            }
            if (ast instanceof Ast.LessThan) {
                final Ast.LessThan lt = (Ast.LessThan) ast;
                final Optional<TypeError> left = expect(lt.getLeft(), Type.INT);
                if (left.isPresent()) { return left; }
                final Optional<TypeError> right = expect(lt.getRight(), Type.INT);
                if (right.isPresent()) { return right; }
                ast.addAttribute(Type.class, Type.BOOL);
                return empty();
            }
            if (ast instanceof Ast.Equals) {
                final Ast.Equals eq = (Ast.Equals) ast;
                final Optional<TypeError> left = expect(eq.getLeft(), Type.INT);
                if (left.isPresent()) { return left; }
                final Optional<TypeError> right = expect(eq.getRight(), Type.INT);
                if (right.isPresent()) { return right; }
                ast.addAttribute(Type.class, Type.BOOL);
                return empty();
            }
            if (ast instanceof Ast.Assign) {
                final Ast.Assign assign = (Ast.Assign) ast;
                final Optional<TypeError> exp = expect(assign.getExp(), Type.INT);
                if (exp.isPresent()) { return exp; }
                return empty();
            }
            if (ast instanceof Ast.IfThen) {
                final Ast.IfThen ifThen = (Ast.IfThen) ast;
                final Optional<TypeError> ifPart = expect(ifThen.getIfPart(), Type.BOOL);
                if (ifPart.isPresent()) { return ifPart; }
                return empty();
            }
            if (ast instanceof Ast.IfThenElse) {
                final Ast.IfThenElse ifThenElse = (Ast.IfThenElse) ast;
                final Optional<TypeError> ifPart = expect(ifThenElse.getIfPart(), Type.BOOL);
                if (ifPart.isPresent()) { return ifPart; }
                return empty();
            }
            if (ast instanceof Ast.Repeat) {
                final Ast.Repeat repeat = (Ast.Repeat) ast;
                final Optional<TypeError> until = expect(repeat.getExp(), Type.BOOL);
                if (until.isPresent()) { return until; }
                return empty();
            }
            if (ast instanceof Ast.Write) {
                final Ast.Write write = (Ast.Write) ast;
                final Optional<TypeError> exp = expect(write.getExp(), Type.INT);
                if (exp.isPresent()) { return exp; }
                return empty();
            }
            return empty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<TypeError> expect(Ast ast, Type type) {
        final Optional<Type> t = ast.getAttribute(Type.class);
        if (!t.isPresent()) { return Optional.of(new TypeError(type, Type.VOID, ast.getToken().get())); }
        if (t.get() != type) { return Optional.of(new TypeError(type, t.get(), ast.getToken().get())); }
        return empty();
    }
}