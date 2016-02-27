/* Joshua Graydus | February 2016 */
package tiny;

import data.Pair;
import tiny.tm.Instruction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static tiny.tm.Instruction.*;

/** quicky hacked together code generator targeting the tiny machine language */
public class TinyCodeGenTM  implements TinyCodeGen {
    // the program counter register
    private static final int PCREG = 7;
    // the stack pointer register
    private static final int SPREG = 6;
    // points to "data section" i.e. static memory location.  actually just assigned a 0
    private static final int DATA = 5;
    // register 4 is assigned the value 1 for increment and decrement procedures
    private static final int INCDEC = 4;

    private static final int R0 = 0;
    private static final int R1 = 1;

    public String generate(final Ast ast) {
        // the symbol table and the first address available for the stack
        final Pair<Map<String,Integer>,Integer> p = makeSymbolTable(ast);
        final Map<String,Integer> symbolTable = p.getLeft();
        final int sp = p.getRight();
        final List<Instruction> instructions = new ArrayList<>();
        // the first instruction assigns the stack pointer
        instructions.add(new Ldc(SPREG,sp));
        instructions.add(new Ldc(DATA,0));
        instructions.add(new Ldc(INCDEC,1));
        instructions.addAll(emit(ast, symbolTable));
        // must end the program with a HALT instruction
        instructions.add(new Halt());
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
        throw new IllegalStateException("unhandled Ast case");
    }

    /** generate the instructions for each statement and concatenate them all together */
    private List<Instruction> emit(final Ast.Statements statements, final Map<String,Integer> symbolTable) {
        final List<Instruction> result = new ArrayList<>();
        for (final Ast child : statements.getChildren()) { result.addAll(emit(child, symbolTable)); }
        return result;
    }

    /** load the value for the id into register 0 */
    private List<Instruction> emit(final Ast.Id id, final Map<String,Integer> symbolTable) {
        final int address = symbolTable.get(id.getName());
        return asList(new Ld(R0, address, DATA));
    }

    /** load the constant into register 0 */
    private List<Instruction> emit(final Ast.Num num, final Map<String,Integer> symbolTable) {
        return asList(new Ldc(R0, num.getValue()));
    }

    /** read from stdin and store into memory location associated with id */
    private List<Instruction> emit(final Ast.Read read, final Map<String,Integer> symbolTable) {
        final String id = ((Ast.Id)read.getIdentifier()).getName();
        final int address = symbolTable.get(id);
        final List<Instruction> result = new ArrayList<>();
        result.addAll(push(R0));         // save register 0
        result.addAll(asList(
                new In(R0),              // read from stdin
                new St(R0,address,DATA)  // store value to address
        ));
        result.addAll(pop(R0));          // restore register 0
        return result;
    }

    /** compute the given expression and write it to stdout */
    private List<Instruction> emit(final Ast.Write write, final Map<String,Integer> symbolTable) {
        final List<Instruction> exp = emit(write.getExp(), symbolTable);
        final List<Instruction> result = new ArrayList<>();
        result.addAll(push(R0));    // save register 0
        result.addAll(exp);         // compute exp
        result.add(new Out(R0));    // write result to stdout
        result.addAll(pop(R0));     // restore register 0
        return result;
    }

    private List<Instruction> emit(final Ast.IfThen ifThen, final Map<String,Integer> symbolTable) {
        final List<Instruction> ifPart = emit(ifThen.getIfPart(), symbolTable);
        final List<Instruction> thenPart = emit(ifThen.getThenPart(), symbolTable);
        final List<Instruction> result = new ArrayList<>();
        result.addAll(push(R0));    // save register 0
        result.addAll(ifPart);      // compute the boolean test
        result.add(new Jeq(R0,thenPart.size()+1,PCREG));  // if false (0), jump past then part
        result.addAll(thenPart);
        result.addAll(pop(R0));     // restore register 0
        return result;
    }

    private List<Instruction> emit(final Ast.IfThenElse ifThenElse, final Map<String,Integer> symbolTable) {
        final List<Instruction> ifPart = emit(ifThenElse.getIfPart(), symbolTable);
        final List<Instruction> thenPart = emit(ifThenElse.getThenPart(), symbolTable);
        final List<Instruction> elsePart = emit(ifThenElse.getElsePart(), symbolTable);
        final List<Instruction> result = new ArrayList<>();
        result.addAll(push(R0));    // save register 0
        result.addAll(ifPart);      // compute the boolean test
        result.add(new Jeq(R0,thenPart.size()+2,PCREG));  // if false (0), jump past then part
        result.addAll(thenPart);
        result.add(new Jne(R0,elsePart.size()+1,PCREG));  // jump past else part
        result.addAll(elsePart);
        result.addAll(pop(R0));     // restore register 0
        return result;
    }

    /** evaluate an exp and store it in the memory associated with the given id */
    private List<Instruction> emit(final Ast.Assign assign, final Map<String,Integer> symbolTable) {
        final Ast.Id id = (Ast.Id) assign.getIdentifier();
        final int address = symbolTable.get(id.getName());
        final List<Instruction> exp = emit(assign.getExp(), symbolTable);
        final List<Instruction> result = new ArrayList<>();
        result.addAll(push(R0));     // save register 0
        result.addAll(exp);          // compute exp
        result.add(new St(R0,address,DATA));  // store result to address
        result.addAll(pop(R0));      // restore register 0
        return result;
    }

    /** result placed into register 0 */
    private List<Instruction> emit(final Ast.Plus plus, final Map<String,Integer> symbolTable) {
        final List<Instruction> left = emit(plus.getLeft(), symbolTable);
        final List<Instruction> right = emit(plus.getRight(), symbolTable);
        return operation(left, right, new Add(R0,R0,R1));
    }

    /** result placed into register 0 */
    private List<Instruction> emit(final Ast.Minus minus, final Map<String,Integer> symbolTable) {
        final List<Instruction> left = emit(minus.getLeft(), symbolTable);
        final List<Instruction> right = emit(minus.getRight(), symbolTable);
        return operation(left, right, new Sub(R0,R0,R1));
    }

    /** result placed into register 0 */
    private List<Instruction> emit(final Ast.Times times, final Map<String,Integer> symbolTable) {
        final List<Instruction> left = emit(times.getLeft(), symbolTable);
        final List<Instruction> right = emit(times.getRight(), symbolTable);
        return operation(left, right, new Mul(R0,R0,R1));
    }

    /** result placed into register 0 */
    private List<Instruction> emit(final Ast.Div div, final Map<String,Integer> symbolTable) {
        final List<Instruction> left = emit(div.getLeft(), symbolTable);
        final List<Instruction> right = emit(div.getRight(), symbolTable);
        return operation(left, right, new Div(R0,R0,R1));
    }

    /** test whether left exp is less than right exp. produces either true (1) or false (0) in register 0 */
    private List<Instruction> emit(final Ast.LessThan lessThan, final Map<String,Integer> symbolTable) {
        final List<Instruction> left = emit(lessThan.getLeft(), symbolTable);
        final List<Instruction> right = emit(lessThan.getRight(), symbolTable);
        // subtract right from left
        final List<Instruction> result = operation(left, right, new Sub(0,0,1));
        // if the result is negative, set register 0 to true (1)
        result.addAll(asList(
                new Jlt(R0,3,PCREG),
                new Ldc(R0,0),         // false
                new Jeq(R0,2,PCREG),
                new Ldc(R0,1)          // true
        ));
        return result;
    }

    /** test two expression for equality. produces either true (1) or false (0) in register 0 */
    private List<Instruction> emit(final Ast.Equals equals, final Map<String,Integer> symbolTable) {
        final List<Instruction> left = emit(equals.getLeft(), symbolTable);
        final List<Instruction> right = emit(equals.getRight(), symbolTable);
        final List<Instruction> result = operation(left,right,new Sub(0,0,1));
        result.addAll(asList(
                new Jeq(R0,3,PCREG),
                new Ldc(R0,0),       // false
                new Jeq(R0,2,PCREG),
                new Ldc(R0,1)        // true
        ));
        return result;
    }

    /** generate code for a repeat loop */
    private List<Instruction> emit(final Ast.Repeat repeat, final Map<String,Integer> symbolTable) {
        final List<Instruction> statements = emit(repeat.getBody(), symbolTable);
        final List<Instruction> exp = emit(repeat.getExp(), symbolTable);
        final List<Instruction> result = new ArrayList<>();
        result.addAll(push(0));       // save register 0
        result.addAll(push(PCREG));   // save the program counter at the start of the loop
        result.addAll(statements);    // execute the statements
        result.addAll(exp);           // compute the test expression
        result.addAll(asList(
                new Jne(0,3,PCREG),   // if true (1), then jump past loop instructions
                new Sub(SPREG,SPREG,INCDEC),  // pop saved pc off stack
                new Ld(PCREG,0,SPREG)
        ));
        result.addAll(pop(0));        // discard saved program counter value
        result.addAll(pop(0));        // restore register 0
        return result;
    }

    /* helper functions */

    /** push the value in the given register onto the stack. increment the stack pointer */
    private List<Instruction> push(final int register) {
        return asList(
                new St(register, 0, SPREG),   // store value in register to memory pointed to stack pointer
                new Add(SPREG,SPREG,INCDEC)   // increment stack pointer
        );
    }

    /** pop a value from the stack into the given register. decrement the stack pointer */
    private List<Instruction> pop(final int register) {
        return asList(
                new Sub(SPREG,SPREG,INCDEC),  // decrement stack pointer
                new Ld(register,0,SPREG)      // load value in memory pointed to by stack pointer into register
        );
    }

    /** perform the binary operation combine.  left/right operands will be in register 0/1, respectively */
    private List<Instruction> operation(final List<Instruction> left,
                                        final List<Instruction> right,
                                        final Instruction combine) {
        final List<Instruction> result = new ArrayList<>();
        result.addAll(right);       // compute the right exp
        result.addAll(push(R0));    // save the result
        result.addAll(left);        // compute the left exp
        result.addAll(pop(R1));     // restore the right exp result into register 1
        result.add(combine);        // combine the result
        return result;
    }
}