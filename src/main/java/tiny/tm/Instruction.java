package tiny.tm;

import static java.util.Arrays.asList;

public abstract class Instruction {
    private final int arg0;
    private final int arg1;
    private final int arg2;
    private final String name;

    private Instruction(final int arg0, final int arg1, final int arg2, final String name) {
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("%s %d %d %d", name, arg0, arg1, arg2);
    }

    /** stop execution of the program */
    public static class Halt extends Instruction {
        public Halt() { super(0,0,0,"HALT"); }
    }

    /** reg[r] <- integer value read from standard input */
    public static class In extends Instruction {
        public In(final int r) { super(r,0,0,"IN"); }
    }

    /** reg[r] -> written to standard output */
    public static class Out extends Instruction {
        public Out(final int r) { super(r,0,0,"OUT"); }
    }

    /** reg[r] <- reg[s] + reg[t] */
    public static class Add extends Instruction {
        public Add(final int r, final int s, final int t) { super(r,s,t,"ADD"); }
    }

    /** reg[r] <- reg[s] - reg[t] */
    public static class Sub extends Instruction {
        public Sub(final int r, final int s, final int t) { super(r,s,t,"SUB"); }
    }

    /** reg[r] <- reg[s] * reg[t] */
    public static class Mul extends Instruction {
        public Mul(final int r, final int s, final int t) { super(r,s,t,"MUL"); }
    }

    /** reg[r] <- reg[s] / reg[t] */
    public static class Div extends Instruction {
        public Div(final int r, final int s, final int t) { super(r,s,t,"DIV"); }
    }

    /* for instruction of the form AAA r,d(s)
     * a = d + reg[s] */

    /** reg[r] <- dMem[a] */
    public static class Ld extends Instruction {
        public Ld(final int r, final int d, final int s) { super(r,d,s,"LD"); }
    }

    /** reg[r] <- a */
    public static class Lda extends Instruction {
        public Lda(final int r, final int d, final int s) { super(r,d,s,"LDA"); }
    }

    /** reg[r] <- d */
    public static class Ldc extends Instruction {
        public Ldc(final int r, final int d) { super(r,d,0,"LDC"); }
    }

    /** dMem[a] <- reg[r] */
    public static class St extends Instruction {
        public St(final int r, final int d, final int s) { super(r,d,s,"ST"); }
    }

    /** if reg[r] < 0 then reg[PC_REG] <- a */
    public static class Jlt extends Instruction {
        public Jlt(final int r, final int d, final int s) { super(r,d,s,"JLT"); }
    }

    /** if reg[r] <= 0 then reg[PC_REG] <- a */
    public static class Jle extends Instruction {
        public Jle(final int r, final int d, final int s) { super(r,d,s,"JLE"); }
    }

    /** if reg[r] >= 0 then reg[PC_REG] <- a */
    public static class Jge extends Instruction {
        public Jge(final int r, final int d, final int s) { super(r,d,s,"JGE"); }
    }

    /** if reg[r] > 0 then reg[PC_REG] <- a */
    public static class Jgt extends Instruction {
        public Jgt(final int r, final int d, final int s) { super(r,d,s,"JGT"); }
    }

    /** if reg[r] == 0 then reg[PC_REG] <- a */
    public static class Jeq extends Instruction {
        public Jeq(final int r, final int d, final int s) { super(r,d,s,"JEQ"); }
    }

    /** if reg[r] != 0 then reg[PC_REG] <- a */
    public static class Jne extends Instruction {
        public Jne(final int r, final int d, final int s) { super(r,d,s,"JNE"); }
    }
}