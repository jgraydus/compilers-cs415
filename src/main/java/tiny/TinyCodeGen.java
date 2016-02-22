package tiny;

import data.Pair;
import tiny.tm.Instruction;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static tiny.tm.Instruction.*;

public class TinyCodeGen {
    // the program counter register
    private static final int PCREG = 7;
    // the stack pointer register
    private static final int SPREG = 6;
    // register for temporary use
    private static final int TEMPREG = 5;

    public String generate(final Ast ast) {
        final Pair<Map<String,Integer>,Integer> p = makeSymbolTable(ast);
        final Map<String,Integer> symbolTable = p.getLeft();
        final int sp = p.getRight();
        final List<Instruction> instructions = new ArrayList<>();
        // the first instruction assigns the stack pointer
        instructions.add(new Ldc(SPREG,sp));
        instructions.addAll(emit(ast, symbolTable));
        instructions.add(new Halt());
        System.out.println(ast);
        System.out.println(instructions);
        return instructions.toString();
    }

    private Pair<Map<String,Integer>,Integer> makeSymbolTable(final Ast ast) {
        final Map<String,Integer> result = new HashMap<>();
        final AtomicInteger counter = new AtomicInteger(0);
        makeSymbolTable(ast, counter, result);
        return Pair.of(result, counter.get());
    }

    private void makeSymbolTable(final Ast ast, final AtomicInteger next, final Map<String,Integer> table) {
        if (ast instanceof Ast.Id) {
            final Ast.Id id = (Ast.Id) ast;
            final String name = id.getName();
            if (!table.containsKey(name)) { table.put(id.getName(), next.getAndIncrement()); }
        }
        else if (ast instanceof Ast.Statements) {
            final Ast.Statements statements = (Ast.Statements) ast;
            statements.getChildren().forEach(s -> makeSymbolTable(s, next, table));
        }
        else {
            for (final Field field : ast.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (Ast.class.isAssignableFrom(field.getType())) {
                    try {
                        makeSymbolTable((Ast)field.get(ast), next, table);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private List<Instruction> emit(final Ast ast, final Map<String,Integer> symbolTable) {
        if (ast instanceof Ast.Assign) { return emit((Ast.Assign)ast, symbolTable); }
        if (ast instanceof Ast.Div) { return emit((Ast.Div)ast, symbolTable); }
        if (ast instanceof Ast.Equals) { return emit((Ast.Equals)ast, symbolTable); }
        if (ast instanceof Ast.Id) { return emit((Ast.Id)ast, symbolTable); }
        if (ast instanceof Ast.IfThen) { return emit((Ast.IfThen)ast, symbolTable); }
        if (ast instanceof Ast.IfThenElse) { return emit((Ast.IfThenElse)ast, symbolTable); }
        if (ast instanceof Ast.LessThan) { return emit((Ast.LessThan)ast, symbolTable); }
        if (ast instanceof Ast.Minus) { return emit((Ast.Minus)ast, symbolTable); }
        if (ast instanceof Ast.Num) { return emit((Ast.Num)ast, symbolTable); }
        if (ast instanceof Ast.Plus) { return emit((Ast.Plus)ast, symbolTable); }
        if (ast instanceof Ast.Read) { return emit((Ast.Read)ast, symbolTable); }
        if (ast instanceof Ast.Repeat) { return emit((Ast.Repeat)ast, symbolTable); }
        if (ast instanceof Ast.Statements) { return emit((Ast.Statements)ast, symbolTable); }
        if (ast instanceof Ast.Times) { return emit((Ast.Times)ast, symbolTable); }
        if (ast instanceof Ast.Write) { return emit((Ast.Write)ast, symbolTable); }
        throw new IllegalStateException();
    }

    private List<Instruction> emit(final Ast.Statements statements, final Map<String,Integer> symbolTable) {
        final List<Instruction> result = new ArrayList<>();
        for (final Ast child : statements.getChildren()) {
            result.addAll(emit(child, symbolTable));
        }
        return result;
    }

    private List<Instruction> emit(final Ast.Id id, final Map<String,Integer> symbolTable) {
        final int address = symbolTable.get(id.getName());
        return asList(
                new Ldc(TEMPREG, 0),
                new Ld(0, address, TEMPREG)
        );
    }

    private List<Instruction> emit(final Ast.Num num, final Map<String,Integer> symbolTable) {
        return asList(new Ldc(0, num.getValue()));
    }

    private List<Instruction> emit(final Ast.Read read, final Map<String,Integer> symbolTable) {
        final String id = ((Ast.Id)read.getIdentifier()).getName();
        final int address = symbolTable.get(id);
        return asList(
                new In(TEMPREG),
                new Ldc(4, 0),
                new St(TEMPREG, address, 4)
        );
    }

    private List<Instruction> emit(final Ast.Write write, final Map<String,Integer> symbolTable) {
        final List<Instruction> exp = emit(write.getExp(), symbolTable);
        final List<Instruction> result = new ArrayList<>();
        result.addAll(exp);
        result.add(new Out(0));
        return result;
    }

    private List<Instruction> emit(final Ast.IfThen ifThen, final Map<String,Integer> symbolTable) {
        final List<Instruction> ifPart = emit(ifThen.getIfPart(), symbolTable);
        final List<Instruction> thenPart = emit(ifThen.getThenPart(), symbolTable);
        final List<Instruction> result = new ArrayList<>();
        // first execute the conditional.  the result will be in register 0
        result.addAll(ifPart);
        // if register 0 is false (0), then skip the instruction in the 'then' part
        result.add(new Jeq(0, thenPart.size()+1, PCREG));
        // otherwise, continue into the 'then' part
        result.addAll(thenPart);
        return result;
    }

    private List<Instruction> emit(final Ast.IfThenElse ifThenElse, final Map<String,Integer> symbolTable) {
        final List<Instruction> ifPart = emit(ifThenElse.getIfPart(), symbolTable);
        final List<Instruction> thenPart = emit(ifThenElse.getThenPart(), symbolTable);
        final List<Instruction> elsePart = emit(ifThenElse.getElsePart(), symbolTable);
        final List<Instruction> result = new ArrayList<>();
        // first execute the conditional
        result.addAll(ifPart);
        // if the result is 0 (false), then jump to the else part
        result.add(new Jeq(0, thenPart.size()+2, PCREG));
        result.addAll(thenPart);
        result.add(new Ldc(0, 1)); //put true in register 0 so we skip the else part
        // if the result was true, jump past the else part
        result.add(new Jne(0, elsePart.size()+1, PCREG));
        return result;
    }

    private List<Instruction> emit(final Ast.Assign assign, final Map<String,Integer> symbolTable) {
        final Ast.Id id = (Ast.Id) assign.getIdentifier();
        final int address = symbolTable.get(id.getName());
        final List<Instruction> exp = emit(assign.getExp(), symbolTable);
        final List<Instruction> result = new ArrayList<>();
        result.addAll(exp);
        result.addAll(asList(
                new Ldc(TEMPREG, 0),
                new St(0, address, TEMPREG)
        ));
        return result;
    }

    private List<Instruction> emit(final Ast.Plus plus, final Map<String,Integer> symbolTable) {
        final List<Instruction> left = emit(plus.getLeft(), symbolTable);
        final List<Instruction> right = emit(plus.getRight(), symbolTable);
        return math(left, right, new Add(0,1,0));
    }

    private List<Instruction> emit(final Ast.Minus minus, final Map<String,Integer> symbolTable) {
        final List<Instruction> left = emit(minus.getLeft(), symbolTable);
        final List<Instruction> right = emit(minus.getRight(), symbolTable);
        return math(left, right, new Sub(0,1,0));
    }

    private List<Instruction> emit(final Ast.Times times, final Map<String,Integer> symbolTable) {
        final List<Instruction> left = emit(times.getLeft(), symbolTable);
        final List<Instruction> right = emit(times.getRight(), symbolTable);
        return math(left, right, new Mul(0,1,0));
    }

    private List<Instruction> emit(final Ast.Div div, final Map<String,Integer> symbolTable) {
        final List<Instruction> left = emit(div.getLeft(), symbolTable);
        final List<Instruction> right = emit(div.getRight(), symbolTable);
        return math(left, right, new Div(0,1,0));
    }

    private List<Instruction> math(final List<Instruction> left,
                                   final List<Instruction> right,
                                   final Instruction instr) {
        final List<Instruction> result = new ArrayList<>();
        // first compute the left side
        result.addAll(left);
        // then save the result
        result.addAll(asList(
                new St(0, 0, SPREG),      // store to top of stack
                new Ldc(0, 1),            // increment stack pointer
                new Add(SPREG, SPREG, 0)
        ));
        // then compute the right side
        result.addAll(right);
        result.addAll(asList(
                new Ldc(1, 1),            // pop stack into register 1
                new Sub(SPREG, SPREG, 1),
                new Ld(1, 0, SPREG),
                instr                     // carry out the operation, e.g. add, sub, etc.
        ));
        return result;
    }

    private List<Instruction> emit(final Ast.LessThan lessThan, final Map<String,Integer> symbolTable) {
        final List<Instruction> left = emit(lessThan.getLeft(), symbolTable);
        final List<Instruction> right = emit(lessThan.getRight(), symbolTable);
        final List<Instruction> result = math(left, right, new Sub(0,1,0));
        result.addAll(asList(
                new Jlt(0, 3, PCREG),
                new Ldc(0, 0),// false
                new Jeq(0, 2, PCREG),
                new Ldc(0, 1) // true
        ));
        return result;
    }

    private List<Instruction> emit(final Ast.Equals equals, final Map<String,Integer> symbolTable) {
        final List<Instruction> left = emit(equals.getLeft(), symbolTable);
        final List<Instruction> right = emit(equals.getRight(), symbolTable);
        final List<Instruction> result = math(left, right, new Sub(0,1,0));
        result.addAll(asList(
                new Jeq(0, 2, PCREG),
                new Jne(0, 2, PCREG),
                new Ldc(0, 1) // true
        ));
        return result;
    }

    private List<Instruction> emit(final Ast.Repeat repeat, final Map<String,Integer> symbolTable) {
        final List<Instruction> statements = emit(repeat.getBody(), symbolTable);
        final List<Instruction> exp = emit(repeat.getExp(), symbolTable);
        final List<Instruction> result = new ArrayList<>();
        result.addAll(asList(
                new St(PCREG, 0, SPREG), // save the current program counter
                new Ldc(TEMPREG, 1),            // increment the stack pointer
                new Add(SPREG, SPREG, TEMPREG)
        ));
        result.addAll(statements);
        result.addAll(exp);
        result.addAll(asList(
                new Jne(0, 4, PCREG), // if true, stop repeating by jumping past instructions that reset the pc
                new Ldc(TEMPREG, 1),            // pop the stack back into the program counter
                new Sub(SPREG, SPREG, TEMPREG),
                new Ld(PCREG, 0, SPREG)
        ));
        return result;
    }
}