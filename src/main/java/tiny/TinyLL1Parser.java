package tiny;

import data.Either;
import parser.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class TinyLL1Parser {
    // nonterminals
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

    // terminals
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

    /*
        LL(1) grammar for TINY.  note left recursion removed from stmt-sequence, exp,
        simple-exp, and term.  Also, if-stmt was factored into the if part and else part.

        program        -> stmt-sequence
        stmt-sequence  -> statement stmt-sequence'
        stmt-sequence' -> ';' statement stmt-sequence' | ε
        statement      -> if-stmt | read-stmt | write-stmt | assign-stmt | repeat-stmt
        if-stmt        -> 'if' exp 'then' stmt-sequence else-part
        else-part      -> 'end' | 'else' stmt-sequence 'end'
        repeat-stmt    -> 'repeat' stmt-sequence 'until' exp
        assign-stmt    -> identifier ':=' exp
        read-stmt      -> 'read' identifier
        write-stmt     -> 'write' exp
        exp            -> simple-exp exp'
        exp'           -> comparison-op exp' | ε
        comparison-op  -> '<' | '='
        simple-exp     -> term simple-exp'
        simple-exp'    -> add-op term simple-exp' | ε
        add-op         -> '+' | '-'
        term           -> factor term'
        term'          -> mul-op factor term' | ε
        mul-op         -> '*' | '/'
        factor         -> '(' exp ')' | number | identifier
     */


    // production rules
    private final List<Production> ps = new ArrayList<>();
    {
        ps.add(new Production(program, asList(stmtSequence)));
        ps.add(new Production(stmtSequence, asList(statement, stmtSequence_)));
        ps.add(new Production(stmtSequence_, asList(semicolon, statement, stmtSequence_)));
        ps.add(new Production(stmtSequence_, asList(Symbol.ε)));
        ps.add(new Production(statement, asList(ifStmt)));
        ps.add(new Production(statement, asList(readStmt)));
        ps.add(new Production(statement, asList(writeStmt)));
        ps.add(new Production(statement, asList(assignStmt)));
        ps.add(new Production(statement, asList(repeatStmt)));
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

    // associate token types to grammar symbols
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
            case REPEAT: return repeat;
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

    private final LL1Parser<Token> parser;

    public TinyLL1Parser() {
        final Grammar g = new Grammar(program, ps);
        parser = new LL1Parser<>(g, toSymbol);
    }

    public ParseTree<Token> parse(final List<Token> input) {
        // the scanner provides tokens for comments and eof.  these are not part of the grammar
        final List<Token> in = input.stream()
                .filter(t -> t.type != Token.Type.COMMENT)
                .filter(t -> t.type != Token.Type.END_OF_FILE)
                .collect(toList());

        final Either<List<Token>,ParseTree<Token>> result = parser.parse(in);

        result.getLeft().ifPresent(errors -> {
            throw new RuntimeException(errorReport(errors));
        });

        return result.getRight().get();
    }

    private String errorReport(List<Token> errors) {
        final StringBuilder sb = new StringBuilder();
        for (final Token t : errors) {
            sb.append("\n");
            sb.append("unexpected token ");
            sb.append(t.toString());
            sb.append(" ");
            sb.append(t.src.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
