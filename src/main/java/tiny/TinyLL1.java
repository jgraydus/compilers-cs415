package tiny;

import sandbox.Grammar;
import sandbox.LL1;
import sandbox.Production;
import sandbox.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class TinyLL1 {
    private final Symbol program = new Symbol.NonTerminal("program");
    private final Symbol stmtSequence = new Symbol.NonTerminal("stmt-sequence");
    private final Symbol stmtSequence_ = new Symbol.NonTerminal("stmt-sequence'");
    private final Symbol statement = new Symbol.NonTerminal("statement");
    private final Symbol ifStmt = new Symbol.NonTerminal("if-stmt");
    private final Symbol elsePart = new Symbol.NonTerminal("else-part");
    private final Symbol repeatStmt = new Symbol.NonTerminal("repeat-stmt");
    private final Symbol assignStmt = new Symbol.NonTerminal("assign-stmt");
    private final Symbol readStmt = new Symbol.NonTerminal("read-stmt");
    private final Symbol writeStmt = new Symbol.NonTerminal("write-stmt");
    private final Symbol exp = new Symbol.NonTerminal("exp");
    private final Symbol exp_ = new Symbol.NonTerminal("exp'");
    private final Symbol comparisonOp = new Symbol.NonTerminal("comparison-op");
    private final Symbol simpleExp = new Symbol.NonTerminal("simple-exp");
    private final Symbol simpleExp_ = new Symbol.NonTerminal("simple-exp'");
    private final Symbol addOp = new Symbol.NonTerminal("add-op");
    private final Symbol term = new Symbol.NonTerminal("term");
    private final Symbol term_ = new Symbol.NonTerminal("term'");
    private final Symbol mulOp = new Symbol.NonTerminal("mul-op");
    private final Symbol factor = new Symbol.NonTerminal("factor");

    private final Symbol semicolon = new Symbol.Terminal(";");
    private final Symbol identifier = new Symbol.Terminal("identifier");
    private final Symbol ifS = new Symbol.Terminal("if");
    private final Symbol then = new Symbol.Terminal("then");
    private final Symbol elseS = new Symbol.Terminal("else");
    private final Symbol end = new Symbol.Terminal("end");
    private final Symbol repeat = new Symbol.Terminal("repeat");
    private final Symbol untilS = new Symbol.Terminal("until");
    private final Symbol assign = new Symbol.Terminal(":=");
    private final Symbol read = new Symbol.Terminal("read");
    private final Symbol write = new Symbol.Terminal("write");
    private final Symbol lt = new Symbol.Terminal("<");
    private final Symbol eq = new Symbol.Terminal("=");
    private final Symbol plus = new Symbol.Terminal("+");
    private final Symbol minus = new Symbol.Terminal("-");
    private final Symbol times = new Symbol.Terminal("*");
    private final Symbol div = new Symbol.Terminal("/");
    private final Symbol left = new Symbol.Terminal("(");
    private final Symbol right = new Symbol.Terminal(")");
    private final Symbol number = new Symbol.Terminal("number");

    private final List<Production> ps = new ArrayList<>();
    {
        ps.add(new Production(program, asList(stmtSequence)));
        ps.add(new Production(stmtSequence, asList(statement, stmtSequence_)));
        ps.add(new Production(stmtSequence_, asList(semicolon, statement, stmtSequence_)));
        ps.add(new Production(stmtSequence_, asList(Symbol.ε)));
        ps.add(new Production(ifStmt, asList(ifS, exp, then, stmtSequence, elsePart)));
        ps.add(new Production(elsePart, asList(end)));
        ps.add(new Production(elsePart, asList(elseS, stmtSequence, end)));
        ps.add(new Production(repeatStmt, asList(repeat, stmtSequence, untilS, exp)));
        ps.add(new Production(assignStmt, asList(identifier, assign, exp)));
        ps.add(new Production(readStmt, asList(read, identifier)));
        ps.add(new Production(writeStmt, asList(write, exp)));
        ps.add(new Production(exp, asList(simpleExp, exp_)));
        ps.add(new Production(exp_, asList(comparisonOp, simpleExp)));
        ps.add(new Production(exp_, asList(Symbol.ε)));
        ps.add(new Production(comparisonOp, asList(lt)));
        ps.add(new Production(comparisonOp, asList(eq)));
        ps.add(new Production(simpleExp, asList(term, simpleExp_)));
        ps.add(new Production(simpleExp_, asList(addOp, term, simpleExp_)));
        ps.add(new Production(simpleExp_, asList(Symbol.ε)));
        ps.add(new Production(addOp, asList(plus)));
        ps.add(new Production(addOp, asList(minus)));
        ps.add(new Production(term, asList(factor, term_)));
        ps.add(new Production(term_, asList(mulOp, factor, term_)));
        ps.add(new Production(term_, asList(Symbol.ε)));
        ps.add(new Production(mulOp, asList(times)));
        ps.add(new Production(mulOp, asList(div)));
        ps.add(new Production(factor, asList(left, exp, right)));
        ps.add(new Production(factor, asList(number)));
        ps.add(new Production(factor, asList(identifier)));
    }

    public TinyLL1() {
        final Grammar g = new Grammar(program, ps);

        final Function<Token,Symbol> toSymbol = token -> {
            switch (token.type) {
                case ASSIGNMENT: return assign;
                case ELSE: return elseS;
                case END: return end;
                case EQUAL: return eq;
                case IDENTIFIER: return identifier;
                case LEFT_PAREN: return left;
                case RIGHT_PAREN: return right;
                case READ: return read;
                case WRITE: return write;
                case REPEAT: return read;
                case UNTIL: return untilS;
                case NUM: return number;
                case LESS_THAN: return lt;
                case PLUS: return plus;
                case MINUS: return minus;
                case TIMES: return times;
                case OVER: return div;
                case THEN: return then;
                case IF: return ifS;
                case SEMICOLON: return semicolon;
                default: throw new IllegalArgumentException(token + " does not correspond to a terminal");
            }
        };

        final LL1<Token> parser = new LL1<>(g, toSymbol);
    }

    public static void main(String... args) {
        new TinyLL1();
    }
}
