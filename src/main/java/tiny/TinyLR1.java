package tiny;

import sandbox.Grammar;
import sandbox.Production;
import sandbox.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class TinyLR1 {
    private final Symbol program = new Symbol.NonTerminal("program");
    private final Symbol stmtSequence = new Symbol.NonTerminal("stmt-sequence");
    private final Symbol statement = new Symbol.NonTerminal("statement");
    private final Symbol ifStmt = new Symbol.NonTerminal("if-stmt");
    private final Symbol repeatStmt = new Symbol.NonTerminal("repeat-stmt");
    private final Symbol assignStmt = new Symbol.NonTerminal("assign-stmt");
    private final Symbol readStmt = new Symbol.NonTerminal("read-stmt");
    private final Symbol writeStmt = new Symbol.NonTerminal("write-stmt");
    private final Symbol exp = new Symbol.NonTerminal("exp");
    private final Symbol comparisonOp = new Symbol.NonTerminal("comparison-op");
    private final Symbol simpleExp = new Symbol.NonTerminal("simple-exp");
    private final Symbol addOp = new Symbol.NonTerminal("add-op");
    private final Symbol term = new Symbol.NonTerminal("term");
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
        ps.add(new Production(stmtSequence, asList(stmtSequence, semicolon, statement)));
        ps.add(new Production(stmtSequence, asList(statement)));
        ps.add(new Production(ifStmt, asList(ifS, exp, then, stmtSequence, end)));
        ps.add(new Production(ifStmt, asList(ifS, exp, then, stmtSequence, elseS, stmtSequence, end)));
        ps.add(new Production(repeatStmt, asList(repeat, stmtSequence, untilS, exp)));
        ps.add(new Production(assignStmt, asList(identifier, assign, exp)));
        ps.add(new Production(readStmt, asList(read, identifier)));
        ps.add(new Production(writeStmt, asList(write, exp)));
        ps.add(new Production(exp, asList(simpleExp, comparisonOp, simpleExp)));
        ps.add(new Production(exp, asList(simpleExp)));
        ps.add(new Production(comparisonOp, asList(lt)));
        ps.add(new Production(comparisonOp, asList(eq)));
        ps.add(new Production(simpleExp, asList(simpleExp, addOp, term)));
        ps.add(new Production(simpleExp, asList(term)));
        ps.add(new Production(addOp, asList(plus)));
        ps.add(new Production(addOp, asList(minus)));
        ps.add(new Production(term, asList(term, mulOp, factor)));
        ps.add(new Production(term, asList(factor)));
        ps.add(new Production(mulOp, asList(times)));
        ps.add(new Production(mulOp, asList(div)));
        ps.add(new Production(factor, asList(left, exp, right)));
        ps.add(new Production(factor, asList(number)));
        ps.add(new Production(factor, asList(identifier)));
    }

    public TinyLR1() {
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
                default: throw new IllegalArgumentException();
            }
        };

    }
}
