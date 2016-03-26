package cm;

import cm.type.Type;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class CmAnalyzer {

    public void typeCheck(final Ast ast) {
        final Map<String,Type> vars = new HashMap<>();
        final Map<String,Signature> funs = new HashMap<>();
        funs.put("input", new Signature(Type.INT, emptyList()));
        funs.put("output", new Signature(Type.VOID, singletonList(Type.INT)));
        typeCheck(ast, vars, funs);
    }

    private void typeCheck(final Ast ast, final Map<String,Type> vars, final Map<String,Signature> funs) {

        // type check each declaration
        if (ast instanceof Ast.DeclarationList) {
            ((Ast.DeclarationList) ast).getDeclarations().forEach(t -> typeCheck(t, vars, funs));
        }

        // add the type of the variable to the type environment
        if (ast instanceof Ast.VarDeclaration) {
            final Ast.VarDeclaration decl = (Ast.VarDeclaration) ast;
            final String name = decl.getName();
            if (vars.containsKey(name)) { throw new IllegalStateException("variable " + name + " already declared"); }
            switch (decl.getType()) {
                case VOID:
                    if (decl.getSize().isPresent()) { throw new IllegalStateException("void array is not a valid type"); }
                    vars.put(name, Type.VOID);
                    break;
                case INT:
                    vars.put(name, decl.getSize().isPresent() ? Type.INT_ARRAY : Type.INT);
                    break;
            }
        }

        // add the signature of the function to the type environment
        // then create a local type environment including the params and use that to type check the body
        if (ast instanceof Ast.FunDeclaration) {
            final Ast.FunDeclaration fun = (Ast.FunDeclaration) ast;
            final String name = fun.getName();
            final Type returnType = fun.getType() == Ast.TypeSpecifier.VOID ? Type.VOID : Type.INT;
            final List<String> paramNames = new ArrayList<>();
            final List<Type> paramTypes = fun.getParams().stream().map(p -> (Ast.Param)p).map(p -> {
                paramNames.add(p.getName());
                switch (p.getType()) {
                    case VOID:
                        if (p.isArray()) { throw new IllegalStateException("void array is not a valid type"); }
                        return Type.VOID;
                    case INT:
                        return p.isArray() ? Type.INT_ARRAY : Type.INT;
                    default: throw new IllegalStateException();
                }
            }).collect(toList());
            final Signature signature = new Signature(returnType, paramTypes);
            funs.put(name, signature);
            final Map<String,Type> local = new HashMap<>(vars);
            for (int i=0; i < paramNames.size(); i++) {
                local.put(paramNames.get(i), paramTypes.get(i));
            }
            typeCheck(fun.getBody(), local, funs);
        }

        if (ast instanceof Ast.CompoundStatement) {
            final Ast.CompoundStatement stmt = (Ast.CompoundStatement) ast;
            final Map<String,Type> local = new HashMap<>(vars);
            stmt.getLocalDeclarations().stream().map(d -> (Ast.VarDeclaration)d).forEach(decl -> {
                final String name = decl.getName();
                //if (vars.containsKey(name)) { throw new IllegalStateException("variable " + name + " already declared"); }
                switch (decl.getType()) {
                    case VOID:
                        if (decl.getSize().isPresent()) { throw new IllegalStateException("void array is not a valid type"); }
                        local.put(name, Type.VOID);
                        break;
                    case INT:
                        local.put(name, decl.getSize().isPresent() ? Type.INT_ARRAY : Type.INT);
                        break;
                }
            });
            stmt.getStatements().forEach(s -> typeCheck(s, local, funs));
        }

        if (ast instanceof Ast.Assignment) {
            final Ast.Assignment assign= (Ast.Assignment) ast;
            typeCheck(assign.getVar(), vars, funs);
            typeCheck(assign.getExpression(), vars, funs);
            final Type varType = assign.getVar().getAttribute(Type.class).get();
            final Type expType = assign.getExpression().getAttribute(Type.class).get();
            if (varType != expType) {
                throw new IllegalStateException("attempting to assign " + expType + " to " + varType);
            }
            ast.addAttribute(Type.class, varType);
        }

        if (ast instanceof Ast.Expression) {
            final Ast.Expression exp = (Ast.Expression) ast;
            // if the expression is an operation, typecheck the lhs and rhs and then typecheck the operation
            if (exp.getOp().isPresent()) {
                if (!exp.getRight().isPresent()) { throw new IllegalStateException(); }
                typeCheck(exp.getLeft(), vars, funs);
                typeCheck(exp.getRight().get(), vars, funs);
                final Type leftType = exp.getLeft().getAttribute(Type.class).get();
                final Type rightType = exp.getRight().get().getAttribute(Type.class).get();
                final Ast.Operator op = exp.getOp().get();
                if (leftType != Type.INT || rightType != Type.INT) {
                    throw new IllegalStateException();
                }
                switch (op) {
                    // boolean operations
                    case LEQ: case LT: case GEQ: case GT: case EQ: case NEQ:
                        ast.addAttribute(Type.class, Type.BOOL);
                        break;
                    // integer operations
                    case PLUS: case MINUS: case TIMES: case DIVIDE:
                        ast.addAttribute(Type.class, Type.INT);
                        break;
                }
            } else {
                typeCheck(exp.getLeft(), vars, funs);
                exp.getLeft().getAttribute(Type.class).ifPresent(type -> exp.addAttribute(Type.class, type));
            }
        }

        if (ast instanceof Ast.Var) {
            final Ast.Var var = (Ast.Var) ast;
            final String name = var.getName();
            final Type type = vars.get(name);
            if (type == null) { throw new IllegalStateException("no declaration for variable " + name); }
            if (var.getExpression().isPresent()) {
                typeCheck(var.getExpression().get(), vars, funs);
                final Type expType = var.getExpression().get().getAttribute(Type.class).get();
                if (Type.INT != expType) { throw new IllegalStateException("array index must be an integer"); }
                ast.addAttribute(Type.class, Type.INT);
            } else {
                ast.addAttribute(Type.class, type);
            }
        }

        if (ast instanceof Ast.ExpressionStmt) {
            final Ast.ExpressionStmt stmt = (Ast.ExpressionStmt) ast;
            stmt.getExpression().ifPresent(exp -> {
                typeCheck(exp, vars, funs);
                ast.addAttribute(Type.class, exp.getAttribute(Type.class).get());
            });
            if (!stmt.getExpression().isPresent()) { ast.addAttribute(Type.class, Type.VOID); }
        }

        if (ast instanceof Ast.Constant) {
            ast.addAttribute(Type.class, Type.INT);
        }

        if (ast instanceof Ast.Call) {
            final Ast.Call call = (Ast.Call) ast;
            final String name = call.getName();
            call.getArgs().forEach(arg -> typeCheck(arg, vars, funs));
            final Signature signature = funs.get(name);
            if (signature == null) { throw new IllegalStateException("no such function defined: " + name); }
            ast.addAttribute(Type.class, signature.returnType);
            final int numParams = signature.paramTypes.size();
            final int numArgs = call.getArgs().size();
            if (numArgs != numParams) {
                throw new IllegalStateException("wrong number of arguments in call to " + name);
            }
            for (int i=0; i<numArgs; i++) {
                final Type paramType = signature.paramTypes.get(i);
                final Type argType = call.getArgs().get(i).getAttribute(Type.class).get();
                if (argType != paramType) {
                    throw new IllegalStateException("argument type does not match corresponding parameter type");
                }
            }
        }

        if (ast instanceof Ast.IfThen) {
            final Ast.IfThen ifThen = (Ast.IfThen) ast;
            typeCheck(ifThen.getCondition(), vars, funs);
            typeCheck(ifThen.getThenPart(), vars, funs);
            if (Type.BOOL != ifThen.getCondition().getAttribute(Type.class).get()) {
                throw new IllegalStateException("condition must be a boolean expression");
            }
        }

        if (ast instanceof Ast.IfThenElse) {
            final Ast.IfThenElse ifThenElse = (Ast.IfThenElse) ast;
            typeCheck(ifThenElse.getCondition(), vars, funs);
            typeCheck(ifThenElse.getThenPart(), vars, funs);
            typeCheck(ifThenElse.getElsePart(), vars, funs);
            if (Type.BOOL != ifThenElse.getCondition().getAttribute(Type.class).get()) {
                throw new IllegalStateException("condition must be a boolean expression");
            }
        }

        if (ast instanceof Ast.While) {
            final Ast.While while_ = (Ast.While) ast;
            typeCheck(while_.getCondition(), vars, funs);
            typeCheck(while_.getBody(), vars, funs);
            if (Type.BOOL != while_.getCondition().getAttribute(Type.class).get()) {
                throw new IllegalStateException("condition must be a boolean expression");
            }
        }

        if (ast instanceof Ast.Return) {
            final Ast.Return ret = (Ast.Return) ast;
            ret.getExpression().ifPresent(exp -> {
                typeCheck(exp, vars, funs);
                ast.addAttribute(Type.class, exp.getAttribute(Type.class).get());
            });
            if (!ret.getExpression().isPresent()) { ast.addAttribute(Type.class, Type.VOID); }
        }
    }

    static class Signature {
        private final Type returnType;
        private final List<Type> paramTypes;

        public Signature(Type returnType, List<Type> paramTypes) {
            this.returnType = returnType;
            this.paramTypes = paramTypes;
        }
    }
}