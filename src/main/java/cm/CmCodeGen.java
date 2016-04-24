/* Joshua Graydus | April 2016 */
package cm;

import data.Pair;
import tiny.tm.Instruction;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class CmCodeGen {

    private static final int FP = 5; // frame pointer
    private static final int SP = 6; // stack pointer
    private static final int PC = 7; // program counter
    private static final int AX = 0; // accumulator
    private static final int BX = 1; // temp register
    private static final int CX = 2; // temp register
    private static final int DX = 3; // temp register

    public List<Instruction> emit(final Ast ast) {
        final int preambleSize = 14;

        // give variables unique names
        rename(ast);

        // create environment
        final Pair<Env,Integer> envAndSp = createInitialEnv((Ast.DeclarationList)ast);

        // generate code for functions
        final Pair<Map<String,Integer>,Map<String,List<Instruction>>> fns = createFunctions((Ast.DeclarationList)ast,
                envAndSp.getLeft(), preambleSize);

        final Map<String,Integer> addresses = fns.getLeft();
        final Map<String,List<Instruction>> functions = fns.getRight();

        final Map<Integer,List<Instruction>> byAddress = addresses.keySet().stream()
                .collect(toMap(addresses::get, functions::get));

        final int[] codeSize = {preambleSize};
        byAddress.keySet().forEach(k -> codeSize[0] += byAddress.get(k).size());

        final Instruction[] instrs = new Instruction[codeSize[0]];

        // initialize stack pointer
        instrs[0] = new Instruction.Ldc(SP, envAndSp.getRight());

        // create call to "main" function
        final List<Instruction> main = insertFunctionAddresses(
                emitCall(new Ast.Call(null, "main", emptyList()), envAndSp.getLeft()), addresses);
        main.add(new Instruction.Halt());

        int a = 1;
        for (Instruction instr : main) {
            instrs[a] = instr;
            a++;
        }

        // add the rest of the function code

        byAddress.keySet().stream().forEach(k -> {
            int address = k;
            for (Instruction instr : byAddress.get(k)) {
                instrs[address] = instr;
                address++;
            }
        });

        return asList(instrs);
    }

    /** @return initial environment and initial SP address */
    private Pair<Env,Integer> createInitialEnv(final Ast.DeclarationList decs) {
        final AtomicInteger counter = new AtomicInteger(0);

        final Map<String,Integer> globalVars = new HashMap<>();
        final Set<String> arrayRefs = new HashSet<>();

        decs.getDeclarations().stream()
                .filter(d -> d instanceof Ast.VarDeclaration)
                .map(d -> (Ast.VarDeclaration)d)
                .forEach(varDec -> {
                    final String name = varDec.getAttribute(UniqueName.class).get().getName();
                    globalVars.put(name, counter.getAndAdd(varDec.getSize().orElse(1)));
                    if (varDec.getSize().isPresent()) { arrayRefs.add(name); }
                });

        final Map<String, Ast.FunDeclaration> functions = decs.getDeclarations().stream()
                .filter(d -> d instanceof Ast.FunDeclaration)
                .map(d -> (Ast.FunDeclaration)d)
                .map(d -> {
                    d.getParams().stream().map(p -> (Ast.Param)p).forEach(p -> {
                        if (p.isArray()) arrayRefs.add(p.getAttribute(UniqueName.class).get().getName());
                    });
                    return d;
                })
                .collect(toMap(Ast.FunDeclaration::getName, identity()));

        return Pair.of(new Env(globalVars, functions, emptyMap(), "", arrayRefs), counter.get());
    }

    /** @return a pair of map from function names to address and from function names to instructions */
    private Pair<Map<String,Integer>,Map<String,List<Instruction>>>
    createFunctions(final Ast.DeclarationList decs, final Env env, int start) {
        final AtomicInteger counter = new AtomicInteger(start);
        final Map<String,Integer> addresses = new HashMap<>();
        final Map<String,List<Instruction>> functions = new HashMap<>();
        decs.getDeclarations().stream()
                .filter(d -> d instanceof Ast.FunDeclaration)
                .map(d -> (Ast.FunDeclaration)d)
                .forEach(f -> {
                    final List<Instruction> instrs = emit(f, env);
                    addresses.put(f.getName(), counter.getAndAdd(instrs.size()));
                    functions.put(f.getName(), instrs);
                });
        final Map<String,List<Instruction>> functions_ = functions.keySet().stream()
                .collect(toMap(identity(), f -> insertFunctionAddresses(functions.get(f), addresses)));
        return Pair.of(addresses, functions_);
    }

    private List<Instruction> insertFunctionAddresses(final List<Instruction> instrs, Map<String,Integer> addresses) {
        return instrs.stream().map(instr -> instr instanceof Instruction.Tmp
                ? ((Instruction.Tmp)instr).create(addresses)
                : instr
        ).collect(toList());
    }

    private static class UniqueName {
        private final String name;
        UniqueName(final String name) { this.name = name; }
        public String getName() { return name; }
    }

    /* add a unique name attribute to variables in local scopes */
    private void rename(final Ast ast) {
        rename(ast, new AtomicInteger(0), new HashMap<>());
    }

    private void rename(final Ast ast, final AtomicInteger counter, HashMap<String,UniqueName> env) {
        if (ast instanceof Ast.DeclarationList) {
            ((Ast.DeclarationList) ast).getDeclarations().forEach(d -> rename(d, counter, env));
            return;
        }

        if (ast instanceof Ast.VarDeclaration) {
            final Ast.VarDeclaration vardec = (Ast.VarDeclaration) ast;
            final UniqueName uniqueName = new UniqueName(vardec.getName() + counter.getAndIncrement());
            env.put(vardec.getName(), uniqueName);
            vardec.addAttribute(UniqueName.class, uniqueName);
            return;
        }

        if (ast instanceof Ast.FunDeclaration) {
            final Ast.FunDeclaration fundec = (Ast.FunDeclaration) ast;
            final HashMap<String,UniqueName> newEnv = new HashMap<>(env);
            fundec.getParams().forEach(p -> rename(p, counter, newEnv));
            rename(fundec.getBody(), counter, newEnv);
            return;
        }

        if (ast instanceof Ast.Param) {
            final Ast.Param param = (Ast.Param) ast;
            final UniqueName uniqueName = new UniqueName(param.getName() + counter.getAndIncrement());
            env.put(param.getName(), uniqueName);
            param.addAttribute(UniqueName.class, uniqueName);
            return;
        }

        if (ast instanceof Ast.CompoundStatement) {
            final Ast.CompoundStatement stmt = (Ast.CompoundStatement) ast;
            final HashMap<String,UniqueName> newEnv = new HashMap<>(env);
            stmt.getLocalDeclarations().forEach(d -> rename(d, counter, newEnv));
            stmt.getStatements().forEach(d -> rename(d, counter, newEnv));
            return;
        }

        if (ast instanceof Ast.Assignment) {
            final Ast.Assignment assignment = (Ast.Assignment) ast;
            rename(assignment.getVar(), counter, env);
            rename(assignment.getExpression(), counter, env);
        }

        if (ast instanceof Ast.Expression) {
            final Ast.Expression exp = (Ast.Expression) ast;
            rename(exp.getLeft(), counter, env);
            exp.getRight().ifPresent(e -> rename(e, counter, env));
        }

        if (ast instanceof Ast.Var) {
            final Ast.Var var = (Ast.Var) ast;
            var.addAttribute(UniqueName.class, env.get(var.getName()));
            var.getExpression().ifPresent(exp -> rename(exp, counter, env));
        }

        if (ast instanceof Ast.ExpressionStmt) {
            final Ast.ExpressionStmt expStmt = (Ast.ExpressionStmt) ast;
            expStmt.getExpression().ifPresent(exp -> rename(exp, counter, env));
        }

        if (ast instanceof Ast.Constant) {
            return;
        }

        if (ast instanceof Ast.Call) {
            final Ast.Call call = (Ast.Call) ast;
            call.getArgs().forEach(a -> rename(a, counter, env));
        }

        if (ast instanceof Ast.IfThen) {
            final Ast.IfThen ifThen = (Ast.IfThen) ast;
            rename(ifThen.getCondition(), counter, env);
            rename(ifThen.getThenPart(), counter, env);
        }

        if (ast instanceof Ast.IfThenElse) {
            final Ast.IfThenElse ifThenElse = (Ast.IfThenElse) ast;
            rename(ifThenElse.getCondition(), counter, env);
            rename(ifThenElse.getThenPart(), counter, env);
            rename(ifThenElse.getElsePart(), counter, env);
        }

        if (ast instanceof Ast.While) {
            final Ast.While whileS = (Ast.While) ast;
            rename(whileS.getCondition(), counter, env);
            rename(whileS.getBody(), counter, env);
        }

        if (ast instanceof Ast.Return) {
            final Ast.Return ret = (Ast.Return) ast;
            ret.getExpression().ifPresent(r -> rename(r, counter, env));
        }
    }

    private static class Env {
        final Map<String,Integer> globalVars;
        final Map<String,Ast.FunDeclaration> functions;
        final Map<String,Integer> localVars;
        final String funScope;
        final Set<String> arrayRefs;

        Env(final Map<String,Integer> globalVars,
            final Map<String,Ast.FunDeclaration> functions,
            final Map<String,Integer> localVars,
            final String funScope,
            final Set<String> arrayRefs) {
            this.globalVars = globalVars;
            this.functions = functions;
            this.localVars = localVars;
            this.funScope = funScope;
            this.arrayRefs = arrayRefs;
        }

        Map<String,Integer> getGlobalVars() {
            return new HashMap<>(globalVars);
        }

        int getGlobalVar(final String name) {
            return globalVars.get(name);
        }

        Map<String,Ast.FunDeclaration> getFunctions() {
            return new HashMap<>(functions);
        }

        Ast.FunDeclaration getFunction(final String name) {
            return functions.get(name);
        }

        int getLocalVar(final String name) {
            return localVars.get(name);
        }

        Map<String,Integer> getLocalVars() {
            return new HashMap<>(localVars);
        }

        Env withNewLocalEnv(final Map<String,Integer> localVars, final String funScope) {
            return new Env(globalVars, functions, localVars, funScope, arrayRefs);
        }

        String getFunScope() { return funScope; }
    }

    private List<Instruction> emit(final Ast ast, final Env env) {
        if (ast instanceof Ast.FunDeclaration) {
            return emitFunction((Ast.FunDeclaration)ast, env);
        }
        if (ast instanceof Ast.Constant) {
            final int value = ((Ast.Constant)ast).getValue();
            return singletonList(new Instruction.Ldc(AX, value, "load constant " + value + " into AX"));
        }
        if (ast instanceof Ast.Var) {
            return emitVar((Ast.Var)ast, env);
        }
        if (ast instanceof Ast.Assignment) {
            return emitAssignment((Ast.Assignment)ast, env);
        }
        if (ast instanceof Ast.Call) {
            return emitCall((Ast.Call)ast, env);
        }
        if (ast instanceof Ast.CompoundStatement) {
            return emitCompoundStmt((Ast.CompoundStatement)ast, env);
        }
        if (ast instanceof Ast.Expression) {
            return emitExp((Ast.Expression)ast, env);
        }
        if (ast instanceof Ast.ExpressionStmt) {
            final Optional<Ast> exp = ((Ast.ExpressionStmt) ast).getExpression();
            return exp.isPresent() ? emit(exp.get(), env) : emptyList();
        }
        if (ast instanceof Ast.IfThen) {
            return emitIfThen((Ast.IfThen)ast, env);
        }
        if (ast instanceof Ast.IfThenElse) {
            return emitIfThenElse((Ast.IfThenElse)ast, env);
        }
        if (ast instanceof Ast.While) {
            return emitWhile((Ast.While)ast, env);
        }
        if (ast instanceof Ast.Return) {
            return emitReturn((Ast.Return)ast, env);
        }
        throw new IllegalStateException(ast.toString());
    }

    private List<Instruction> emitFunction(final Ast.FunDeclaration funDec, final Env env) {
        final Env lEnv = env.withNewLocalEnv(newLocalEnv(funDec), funDec.getName());
        final Ast.CompoundStatement stmt = (Ast.CompoundStatement) funDec.getBody();
        // if the function has type void and doesn't end with a return statement, then add one
        if (funDec.getType() == Ast.TypeSpecifier.VOID &&
                !(stmt.getStatements().get(stmt.getStatements().size()-1) instanceof Ast.Return)) {
            stmt.getStatements().add(new Ast.Return(null, Optional.empty()));
        }
        final List<Instruction> instrs = new LinkedList<>();
        instrs.add(new Instruction.Nop("function " + funDec.getName()));
        instrs.addAll(emit(stmt, lEnv));
        return instrs;
    }

    private List<Instruction> emitVar(final Ast.Var var, final Env env) {
        final String name = var.getAttribute(UniqueName.class).get().getName();
        if (env.getLocalVars().containsKey(name)) {
            final int offset = env.getLocalVar(name);
            // var is an array.  we have to evaluate the index to calculate the address
            if (var.getExpression().isPresent()) {
                final List<Instruction> instrs = new LinkedList<>();
                // first evaluate the index
                instrs.add(new Instruction.Nop("calculate index for local array var lookup"));
                instrs.addAll(emitExp((Ast.Expression)var.getExpression().get(), env));
                // if the offset is negative, then the array was passed by reference via a parameter
                if (offset < 0) {
                    // load the address of the array into BX
                    instrs.add(new Instruction.Ld(BX, offset, FP, "load local var param " + name));
                    // add the address of the array to the calculated index
                    instrs.add(new Instruction.Add(AX, AX, BX));
                    // load the value at the address in AX back into AX
                    instrs.add(new Instruction.Ld(AX, 0, AX));
                }
                // otherwise, the array is a local variable and lives on the stack
                else {
                    // add the frame pointer to the index
                    instrs.add(new Instruction.Add(AX, AX, FP, "load local var " + name));
                    // load the value at the address [AX + offset] back into AX
                    instrs.add(new Instruction.Ld(AX, offset, AX));
                }
                return instrs;
            }
            // var is not an array
            else {
                final List<Instruction> instrs = new LinkedList<>();
                instrs.add(new Instruction.Nop("load local var " + name));
                instrs.add(new Instruction.Ld(AX, offset, FP));
                return instrs;
            }
        }
        if (env.getGlobalVars().containsKey(name)) {
            final int address = env.getGlobalVar(name);
            // array
            if (var.getExpression().isPresent()) {
                final List<Instruction> instrs = new LinkedList<>();
                // first evaluate the index
                instrs.add(new Instruction.Nop("calculate index for global array var lookup"));
                instrs.addAll(emitExp((Ast.Expression)var.getExpression().get(), env));
                // load the value at index + address back into AX
                instrs.add(new Instruction.Ld(AX, address, AX));
                return instrs;
            }
            // not array
            else {
                final List<Instruction> instrs = new LinkedList<>();
                instrs.add(new Instruction.Ldc(CX, 0, "load global var " + name + " into AX..."));
                instrs.add(env.arrayRefs.contains(name)
                        ? new Instruction.Lda(AX, address, CX, "by reference")
                        : new Instruction.Ld(AX, address, CX, "by value"));
                return instrs;
            }
        }
        throw new IllegalStateException("variable " + name + " is not bound in the environment");
    }

    private List<Ast.VarDeclaration> varDecs(final Ast ast) {
        if (ast instanceof Ast.FunDeclaration) {
            final Ast.FunDeclaration funDec = (Ast.FunDeclaration) ast;
            return varDecs(funDec.getBody());
        }

        if (ast instanceof Ast.VarDeclaration) {
            return singletonList((Ast.VarDeclaration)ast);
        }

        if (ast instanceof Ast.CompoundStatement) {
            final Ast.CompoundStatement stmt = (Ast.CompoundStatement) ast;
            final List<Ast.VarDeclaration> result = new LinkedList<>();
            stmt.getLocalDeclarations().forEach(dec -> result.addAll(varDecs(dec)));
            stmt.getStatements().forEach(st -> result.addAll(varDecs(st)));
            return result;
        }

        if (ast instanceof Ast.IfThen) {
            final Ast.IfThen ifThen = (Ast.IfThen) ast;
            return varDecs(ifThen.getThenPart());
        }

        if (ast instanceof Ast.IfThenElse) {
            final Ast.IfThenElse ifThenElse = (Ast.IfThenElse) ast;
            final List<Ast.VarDeclaration> result = new LinkedList<>();
            result.addAll(varDecs(ifThenElse.getThenPart()));
            result.addAll(varDecs(ifThenElse.getElsePart()));
            return result;
        }

        if (ast instanceof Ast.While) {
            final Ast.While whileS = (Ast.While) ast;
            return varDecs(whileS.getBody());
        }

        return emptyList();
    }

    private Map<String,Integer> newLocalEnv(final Ast.FunDeclaration funDec) {
        // add each stack allocated variable with its offset
        int offset = 1;  // starts at 1 because previous FP is at address 0
        final Map<String,Integer> env = new HashMap<>();
        for (final Ast.VarDeclaration varDec : varDecs(funDec)) {
            final int size = varDec.getSize().orElse(1);
            env.put(varDec.getAttribute(UniqueName.class).get().getName(), offset);
            offset += size;
        }
        // mark the location the SP should be pointed at after allocating stack space
        env.put("SP", offset);
        // add parameters (in reverse order)
        offset = -1;
        final List<Ast.Param> params = funDec.getParams().stream().map(p -> (Ast.Param)p).collect(toList());
        for (Ast.Param param : params) {
            env.put(param.getAttribute(UniqueName.class).get().getName(), offset);
            offset -= 1;
        }
        return env;
    }

    private List<Instruction> emitCall(final Ast.Call call, final Env env) {
        final String funName = call.getName();
        // special case for "input" and "output" -- just inline them
        if ("input".equals(funName)) {
            final List<Instruction> instrs = new LinkedList<>();
            instrs.add(new Instruction.Nop("call to input"));
            instrs.add(new Instruction.In(AX));
            return instrs;
        }
        if ("output".equals(funName)) {
            final List<Instruction> instrs = new LinkedList<>();
            instrs.add(new Instruction.Nop("call to output"));
            instrs.addAll(emit(call.getArgs().get(0), env));
            instrs.add(new Instruction.Out(AX));
            return instrs;
        }
        final Ast.FunDeclaration funDec = env.getFunction(funName);
        // build a new local environment for the function
        final Env lEnv = env.withNewLocalEnv(newLocalEnv(funDec), funName);
        final List<Instruction> tmp = new LinkedList<>();
        // evaluate each argument in reverse order and push them onto the stack
        final List<Ast> args = call.getArgs();
        final List<Ast.Param> params = funDec.getParams().stream().map(p -> (Ast.Param)p).collect(toList());
        for (int i = args.size()-1; i>=0; i--) {
            tmp.addAll(emitExp((Ast.Expression) args.get(i), env));
            tmp.addAll(push(AX));
        }
        // push FP
        tmp.addAll(push(FP));
        // set FP equal to SP-1 (to point at the previous FP on the stack)
        tmp.add(new Instruction.Lda(FP, -1, SP));
        // increment SP to make room for local variables
        tmp.add(new Instruction.Lda(SP, lEnv.getLocalVars().get("SP")-1, SP));

        final List<Instruction> instrs = new LinkedList<>();
        instrs.add(new Instruction.Nop("call to " + funName));
        // push the return address
        instrs.add(new Instruction.Lda(BX, tmp.size()+6, PC)); // jump past all of the code emitted by call
        instrs.addAll(push(BX)); // this is 3 instructions
        instrs.addAll(tmp);  // do the setup
        instrs.add(new Instruction.Ldc(CX, 0));
        // start executing the function
        instrs.add(new Instruction.Tmp(fs -> new Instruction.Lda(PC, fs.get(funName), CX)));
        return instrs;
    }

    private List<Instruction> emitCompoundStmt(final Ast.CompoundStatement cmpd, final Env env) {
        final List<Instruction> instrs = new LinkedList<>();
        cmpd.getStatements().forEach(stmt -> instrs.addAll(emit(stmt, env)));
        return instrs;
    }

    private List<Instruction> emitAssignment(final Ast.Assignment assign, final Env env) {
        final Ast.Var var = ((Ast.Var)assign.getVar());
        final String name = var.getAttribute(UniqueName.class).get().getName();
        // evaluate the expression
        final List<Instruction> instrs = new LinkedList<>();
        instrs.add(new Instruction.Nop("assignment to " + name));
        instrs.addAll(emitExp((Ast.Expression)assign.getExpression(), env));

        // local var
        if (env.getLocalVars().containsKey(name)) {
            final int offset = env.getLocalVar(name);
            // array
            if (var.getExpression().isPresent()) {
                // save the value
                instrs.addAll(push(AX));
                // first evaluate the index
                instrs.addAll(emitExp((Ast.Expression) var.getExpression().get(), env));
                if (offset < 0) { // param (only address is on stack)
                    // get the array address
                    instrs.add(new Instruction.Ld(BX, offset, FP));
                    // add the index
                    instrs.add(new Instruction.Add(AX,AX,BX));
                    // pop the value being stored to the var into BX
                    instrs.addAll(pop(BX));
                    // and store the value in BX to the address of var (offset + AX)
                    instrs.add(new Instruction.St(BX, 0, AX));
                } else { // array is on stack
                    // add the frame pointer to the calculated index
                    instrs.add(new Instruction.Add(AX, AX, FP));
                    // pop the value being stored to the var into BX
                    instrs.addAll(pop(BX));
                    // and store the value in BX to the address of var (offset + AX)
                    instrs.add(new Instruction.St(BX, offset, AX));
                }
            }
            // not array
            else {
                instrs.add(new Instruction.St(AX, offset, FP));
            }
            return instrs;
        }

        // global var
        if (env.getGlobalVars().containsKey(name)) {
            final int address = env.getGlobalVar(name);
            // array
            if (var.getExpression().isPresent()) {
                // save the value
                instrs.addAll(push(AX));
                // first evaluate the index
                instrs.addAll(emitExp((Ast.Expression)var.getExpression().get(), env));
                instrs.addAll(pop(BX));
                // store the value into the var
                instrs.add(new Instruction.St(BX, address, AX));
            }
            // not array
            else {
                instrs.add(new Instruction.Ldc(0, CX));
                instrs.add(new Instruction.St(AX, address, CX));
            }
            return instrs;
        }
        throw new IllegalStateException("variable " + name + " is not bound in the evironment");
    }

    private List<Instruction> emitIfThen(final Ast.IfThen ifThen, final Env env) {
        final List<Instruction> cond = emitExp((Ast.Expression) ifThen.getCondition(), env);
        final List<Instruction> stmt = emit(ifThen.getThenPart(), env);
        final List<Instruction> instrs = new LinkedList<>();
        instrs.add(new Instruction.Nop("if/then"));
        instrs.addAll(cond);
        instrs.add(new Instruction.Jeq(AX,stmt.size()+1,PC,"if false, jump over statement"));
        instrs.addAll(stmt);
        return instrs;
    }

    private List<Instruction> emitIfThenElse(final Ast.IfThenElse ifThenElse, final Env env) {
        final List<Instruction> cond = emitExp((Ast.Expression) ifThenElse.getCondition(), env);
        final List<Instruction> stmt1 = emit(ifThenElse.getThenPart(), env);
        final List<Instruction> stmt2 = emit(ifThenElse.getElsePart(), env);
        final List<Instruction> instrs = new LinkedList<>();
        instrs.add(new Instruction.Nop("if/then/else"));
        instrs.addAll(cond);
        instrs.add(new Instruction.Jeq(AX,stmt1.size()+2, PC, "if false, jump over first statement"));
        instrs.addAll(stmt1);
        instrs.add(new Instruction.Jmp(stmt2.size()+1, PC, "jump over second statement"));
        instrs.addAll(stmt2);
        return instrs;
    }

    private List<Instruction> emitExp(final Ast.Expression exp, final Env env) {
        if (exp.getOp().isPresent()) {
            final List<Instruction> instrs = new LinkedList<>();
            final Ast left = exp.getLeft();
            final Ast.Operator op = exp.getOp().get();
            final Ast right = exp.getRight().get();
            // evaluate left exp
            instrs.addAll(emit(left, env));
            // push result
            instrs.addAll(push(AX));
            // evaluate right exp
            instrs.addAll(emit(right, env));
            // pop left result into register 1
            instrs.addAll(pop(BX));
            // evaluate operator
            switch (op) {
                case PLUS:
                    instrs.add(new Instruction.Add(AX, BX, AX));
                    break;
                case MINUS:
                    instrs.add(new Instruction.Sub(AX, BX, AX));
                    break;
                case TIMES:
                    instrs.add(new Instruction.Mul(AX, BX, AX));
                    break;
                case DIVIDE:
                    instrs.add(new Instruction.Div(AX, BX, AX));
                    break;
                default:
                    instrs.add(new Instruction.Sub(AX, BX, AX)); // if the values are equal, their difference is 0
                    switch (op) {
                        case LEQ:
                            instrs.add(new Instruction.Jle(AX, 3, PC, "jump over false"));
                            break;
                        case LT:
                            instrs.add(new Instruction.Jlt(AX, 3, PC, "jump over false"));
                            break;
                        case GEQ:
                            instrs.add(new Instruction.Jge(AX, 3, PC, "jump over false"));
                            break;
                        case GT:
                            instrs.add(new Instruction.Jgt(AX, 3, PC, "jump over false"));
                            break;
                        case EQ:
                            instrs.add(new Instruction.Jeq(AX, 3, PC, "jump over false"));
                            break;
                        case NEQ:
                            instrs.add(new Instruction.Jne(AX, 3, PC, "jump over false"));
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                    instrs.addAll(asList(
                            new Instruction.Ldc(AX, 0, "set result to false"),
                            new Instruction.Jmp(2, PC, "jump over true"),
                            new Instruction.Ldc(AX, 1, "set result to true")
                    ));
            }
            return instrs;
        } else {
            return emit(exp.getLeft(), env);
        }
    }

    private List<Instruction> emitWhile(final Ast.While whileS, final Env env) {
        final List<Instruction> cond = emitExp((Ast.Expression)whileS.getCondition(), env);
        final List<Instruction> stmt = emit(whileS.getBody(), env);
        final List<Instruction> instrs = new LinkedList<>();
        instrs.add(new Instruction.Nop("while"));
        instrs.addAll(cond);
        instrs.add(new Instruction.Jeq(0, stmt.size()+2, PC, "if cond is false, jump over statement body / loop jump"));
        instrs.addAll(stmt);
        instrs.add(new Instruction.Jmp(-1 * (cond.size() + 1 + stmt.size()), PC, "jump back to the condition"));
        return instrs;
    }

    private List<Instruction> emitReturn(final Ast.Return ret, final Env env) {
        final int numArgs = env.getFunction(env.funScope).getParams().size();
        final List<Instruction> instrs = new LinkedList<>();
        instrs.add(new Instruction.Nop("return from " + env.funScope));
        if (ret.getExpression().isPresent()) {
            // evaluate the return value
            instrs.addAll(emitExp((Ast.Expression) ret.getExpression().get(), env));
        }
        instrs.add(new Instruction.Lda(SP, -numArgs, FP, "move stack pointer back to beginning of stack frame"));
        instrs.add(new Instruction.Nop("save return address into BX"));
        instrs.addAll(pop(BX));
        instrs.add(new Instruction.Ld(FP, 0, FP, "reset frame pointer to previous frame pointer"));
        instrs.add(new Instruction.Lda(PC, 0, BX, "change PC to return address"));
        return instrs;
    }

    private List<Instruction> push(final int register) {
        return asList(
                new Instruction.Ldc(DX, 1, "PUSH " + register),
                new Instruction.St(register, 0, SP), //push register
                new Instruction.Add(SP, SP, DX) //increment stack pointer
        );
    }

    private List<Instruction> pop(final int register) {
        return asList(
                new Instruction.Ldc(DX, 1, "POP into " + register),
                new Instruction.Sub(SP, SP, DX), // decrement stack pointer
                new Instruction.Ld(register, 0, SP) // pop value into register
        );
    }
}