/* Joshua Graydus | February 2016 */
package tiny.tm;

import java.util.Map;
import java.util.function.Function;

/** the instruction set for the TINY machine */
public abstract class Instruction {
    private final int arg0;
    private final int arg1;
    private final int arg2;
    private final String name;
    private final String comment;

    private Instruction(final int arg0, final int arg1, final int arg2, final String name) {
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.name = name;
        this.comment = "";
    }

    private Instruction(final int arg0, final int arg1, final int arg2, final String name, final String comment) {
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.name = name;
        this.comment = comment;
    }

    @Override
    public String toString() {
        return String.format("%s (%d) (%d) (%d) \"%s\"", name, arg0, arg1, arg2, comment);
    }

    /** stop execution of the program */
    public static class Halt extends Instruction {
        public Halt() { super(0,0,0,"HALT"); }
        public Halt(final String comment) { super(0,0,0,"HALT",comment); }
    }

    /** reg[r] <- integer value read from standard input */
    public static class In extends Instruction {
        public In(final int r) { super(r,0,0,"IN"); }
        public In(final int r, final String comment) { super(r,0,0,"IN", comment); }
    }

    /** reg[r] -> written to standard output */
    public static class Out extends Instruction {
        public Out(final int r) { super(r,0,0,"OUT"); }
        public Out(final int r, final String comment) { super(r,0,0,"OUT",comment); }
    }

    /** reg[r] <- reg[s] + reg[t] */
    public static class Add extends Instruction {
        public Add(final int r, final int s, final int t) { super(r,s,t,"ADD"); }
        public Add(final int r, final int s, final int t, final String comment) { super(r,s,t,"ADD",comment); }
    }

    /** reg[r] <- reg[s] - reg[t] */
    public static class Sub extends Instruction {
        public Sub(final int r, final int s, final int t) { super(r,s,t,"SUB"); }
        public Sub(final int r, final int s, final int t, final String comment) { super(r,s,t,"SUB",comment); }
    }

    /** reg[r] <- reg[s] * reg[t] */
    public static class Mul extends Instruction {
        public Mul(final int r, final int s, final int t) { super(r,s,t,"MUL"); }
        public Mul(final int r, final int s, final int t, final String comment) { super(r,s,t,"MUL",comment); }
    }

    /** reg[r] <- reg[s] / reg[t] */
    public static class Div extends Instruction {
        public Div(final int r, final int s, final int t) { super(r,s,t,"DIV"); }
        public Div(final int r, final int s, final int t, final String comment) { super(r,s,t,"DIV",comment); }
    }

    /* for instruction of the form AAA r,d(s)
     * a = d + reg[s] */

    /** reg[r] <- dMem[a] */
    public static class Ld extends Instruction {
        public Ld(final int r, final int d, final int s) { super(r,d,s,"LD"); }
        public Ld(final int r, final int d, final int s, final String comment) { super(r,d,s,"LD",comment); }
    }

    /** reg[r] <- a */
    public static class Lda extends Instruction {
        public Lda(final int r, final int d, final int s) { super(r,d,s,"LDA"); }
        public Lda(final int r, final int d, final int s, final String comment) { super(r,d,s,"LDA", comment); }
    }

    /** reg[r] <- d */
    public static class Ldc extends Instruction {
        public Ldc(final int r, final int d) { super(r,d,0,"LDC"); }
        public Ldc(final int r, final int d, final String comment) { super(r,d,0,"LDC",comment); }
    }

    /** dMem[a] <- reg[r] */
    public static class St extends Instruction {
        public St(final int r, final int d, final int s) { super(r,d,s,"ST"); }
        public St(final int r, final int d, final int s, final String comment) { super(r,d,s,"ST",comment); }
    }

    /** if reg[r] < 0 then reg[PC_REG] <- a */
    public static class Jlt extends Instruction {
        public Jlt(final int r, final int d, final int s) { super(r,d,s,"JLT"); }
        public Jlt(final int r, final int d, final int s, final String comment) { super(r,d,s,"JLT", comment); }
    }

    /** if reg[r] <= 0 then reg[PC_REG] <- a */
    public static class Jle extends Instruction {
        public Jle(final int r, final int d, final int s) { super(r,d,s,"JLE"); }
        public Jle(final int r, final int d, final int s, final String comment) { super(r,d,s,"JLE", comment); }
    }

    /** if reg[r] >= 0 then reg[PC_REG] <- a */
    public static class Jge extends Instruction {
        public Jge(final int r, final int d, final int s) { super(r,d,s,"JGE"); }
        public Jge(final int r, final int d, final int s, final String comment) { super(r,d,s,"JGE", comment); }
    }

    /** if reg[r] > 0 then reg[PC_REG] <- a */
    public static class Jgt extends Instruction {
        public Jgt(final int r, final int d, final int s) { super(r,d,s,"JGT"); }
        public Jgt(final int r, final int d, final int s, final String comment) { super(r,d,s,"JGT", comment); }
    }

    /** if reg[r] == 0 then reg[PC_REG] <- a */
    public static class Jeq extends Instruction {
        public Jeq(final int r, final int d, final int s) { super(r,d,s,"JEQ"); }
        public Jeq(final int r, final int d, final int s, final String comment) { super(r,d,s,"JEQ", comment); }
    }

    /** if reg[r] != 0 then reg[PC_REG] <- a */
    public static class Jne extends Instruction {
        public Jne(final int r, final int d, final int s) { super(r,d,s,"JNE"); }
        public Jne(final int r, final int d, final int s, final String comment) { super(r,d,s,"JNE", comment); }
    }

    /** unconditional jump: reg[PC_REG] <- a */
    public static class Jmp extends Instruction {
        public Jmp(final int d, final int s) { super(0,d,s,"JMP"); }
        public Jmp(final int d, final int s, final String comment) { super(0,d,s,"JMP", comment); }
    }

    /** not an actual instruction.  used as placeholder for instructions that need information about function addresses
     * that isn't available at the time the instruction is being emitted */
    public static class Tmp extends Instruction {
        final Function<Map<String,Integer>,Instruction> create;
        public Tmp(final Function<Map<String,Integer>,Instruction> create) {
            super(0,0,0,"TMP");
            this.create = create;
        }
        public Instruction create(Map<String,Integer> functions) { return create.apply(functions); }
    }

    public static class Nop extends Instruction {
        public Nop(final String comment) { super(0,0,0,"NOP",comment); }
    }
}