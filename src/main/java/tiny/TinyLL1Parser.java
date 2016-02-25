/* Joshua Graydus | February 2016 */
package tiny;

import data.Either;
import parser.*;

import java.util.ArrayList;
import java.util.LinkedList;
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

    /*  LL(1) grammar for TINY.  note left recursion removed from stmt-sequence, exp,
        simple-exp, and term.  Also, if-stmt factored into if part and else part.

        program        -> stmt-sequence
        stmt-sequence  -> statement stmt-sequence'
        stmt-sequence' -> ';' statement stmt-sequence' | epsilon
        statement      -> if-stmt | read-stmt | write-stmt | assign-stmt | repeat-stmt
        if-stmt        -> 'if' exp 'then' stmt-sequence else-part
        else-part      -> 'end' | 'else' stmt-sequence 'end'
        repeat-stmt    -> 'repeat' stmt-sequence 'until' exp
        assign-stmt    -> identifier ':=' exp
        read-stmt      -> 'read' identifier
        write-stmt     -> 'write' exp
        exp            -> simple-exp exp'
        exp'           -> comparison-op simple-exp | epsilon
        comparison-op  -> '<' | '='
        simple-exp     -> term simple-exp'
        simple-exp'    -> add-op term simple-exp' | epsilon
        add-op         -> '+' | '-'
        term           -> factor term'
        term'          -> mul-op factor term' | epsilon
        mul-op         -> '*' | '/'
        factor         -> '(' exp ')' | number | identifier                            */

    // production rules
    private final List<Production> ps = new ArrayList<Production>(){{
        add(new Production(program, asList(stmtSequence)));
        add(new Production(stmtSequence, asList(statement, stmtSequence_)));
        add(new Production(stmtSequence_, asList(semicolon, statement, stmtSequence_)));
        add(new Production(stmtSequence_, asList(Symbol.epsilon)));
        add(new Production(statement, asList(ifStmt)));
        add(new Production(statement, asList(readStmt)));
        add(new Production(statement, asList(writeStmt)));
        add(new Production(statement, asList(assignStmt)));
        add(new Production(statement, asList(repeatStmt)));
        add(new Production(ifStmt, asList(ifS, exp, then, stmtSequence, elsePart)));
        add(new Production(elsePart, asList(end)));
        add(new Production(elsePart, asList(elseS, stmtSequence, end)));
        add(new Production(repeatStmt, asList(repeat, stmtSequence, untilS, exp)));
        add(new Production(assignStmt, asList(identifier, assign, exp)));
        add(new Production(readStmt, asList(read, identifier)));
        add(new Production(writeStmt, asList(write, exp)));
        add(new Production(exp, asList(simpleExp, exp_)));
        add(new Production(exp_, asList(comparisonOp, simpleExp)));
        add(new Production(exp_, asList(Symbol.epsilon)));
        add(new Production(comparisonOp, asList(lt)));
        add(new Production(comparisonOp, asList(eq)));
        add(new Production(simpleExp, asList(term, simpleExp_)));
        add(new Production(simpleExp_, asList(addOp, term, simpleExp_)));
        add(new Production(simpleExp_, asList(Symbol.epsilon)));
        add(new Production(addOp, asList(plus)));
        add(new Production(addOp, asList(minus)));
        add(new Production(term, asList(factor, term_)));
        add(new Production(term_, asList(mulOp, factor, term_)));
        add(new Production(term_, asList(Symbol.epsilon)));
        add(new Production(mulOp, asList(times)));
        add(new Production(mulOp, asList(div)));
        add(new Production(factor, asList(left, exp, right)));
        add(new Production(factor, asList(number)));
        add(new Production(factor, asList(identifier)));
    }};

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

    /** @return if an error occurs, a String describing the problem. otherwise, an abstract syntax tree
     *  for the given input */
    public Either<String,Ast> parse(final List<Token> input) {
        // the scanner provides tokens for comments and eof. these are not part of the grammar
        final List<Token> in = input.stream()
                .filter(t -> t.type != Token.Type.COMMENT)
                .filter(t -> t.type != Token.Type.END_OF_FILE)
                .collect(toList());

        final Either<List<Token>,ParseTree<Token>> result = parser.parse(in);

        if (result.getLeft().isPresent()) {
            // errors occurred during parsing
            return Either.left(errorReport(result.getLeft().get()));
        }

        final ParseTree<Token> tree = result.getRight().get();

        try {
            return Either.right(toSyntaxTree(tree));
        } catch (final TinyParseException e) {
            // an error occurred while building the ast
            final Token t = e.getToken();
            final String msg = t == null || t.getSrc() == null ? "unknown" : t.getSrc().toString();
            return Either.left(msg);
        }
    }

    /* 'errors' is a list of tokens at which errors occurred.  returns a single formatted String
     * which describes the errors */
    private String errorReport(final List<Token> errors) {
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

    /* builds an abstract syntax tree from a full parse tree */
    private Ast toSyntaxTree(final ParseTree<Token> parseTree) {
        switch (parseTree.getSymbol().toString()) {
            case "program": return program(parseTree);
            case "stmt-sequence": return stmtSequence(parseTree);
            case "statement":  return statement(parseTree);
            case "if-stmt": return ifStmt(parseTree);
            case "repeat-stmt": return repeatStmt(parseTree);
            case "assign-stmt": return assignStmt(parseTree);
            case "read-stmt": return readStmt(parseTree);
            case "write-stmt": return writeStmt(parseTree);
            case "exp": return exp(parseTree);
            case "simple-exp": return simpleExp(parseTree);
            case "term": return term(parseTree);
            case "factor": return factor(parseTree);
            case "number": return number(parseTree);
            case "identifier": return identifier(parseTree);
            default:
                throw new IllegalStateException("should not reach here");
        }
    }

    // program -> stmt-sequence
    private Ast program(final ParseTree<Token> parseTree) {
        return toSyntaxTree(parseTree.getChildren().get(0));
    }

    // stmt-sequence -> statement stmt-sequence'
    // stmt-sequence' -> ';' statement stmt-sequence' | epsilon
    private Ast stmtSequence(final ParseTree<Token> parseTree) {
        final List<Ast> stmts = new LinkedList<>();
        List<ParseTree<Token>> children = parseTree.getChildren();
        stmts.add(toSyntaxTree(children.get(0)));
        children = children.get(1).getChildren();
        while (!children.isEmpty()) {
            stmts.add(toSyntaxTree(children.get(1)));
            children = children.get(2).getChildren();
        }
        return new Ast.Statements(null, stmts);
    }

    // statement -> if-stmt | read-stmt | write-stmt | assign-stmt | repeat-stmt
    private Ast statement(final ParseTree<Token> parseTree) {
        return toSyntaxTree(parseTree.getChildren().get(0));
    }

    // if-stmt -> 'if' exp 'then' stmt-sequence else-part
    // else-part -> 'end' | 'else' stmt-sequence 'end'
    private Ast ifStmt(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        final Ast exp = toSyntaxTree(children.get(1));
        final Ast then = toSyntaxTree(children.get(3));
        final ParseTree<Token> elsePart = children.get(4);
        final Token t = children.get(0).getT();
        return elsePart.getChildren().size() == 3
            ? new Ast.IfThenElse(t, exp, then, toSyntaxTree(elsePart.getChildren().get(1)))
            : new Ast.IfThen(t, exp, then);
    }

    // repeat-stmt -> 'repeat' stmt-sequence 'until' exp
    private Ast repeatStmt(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        final Token t = children.get(0).getT();
        return new Ast.Repeat(t, toSyntaxTree(children.get(1)), toSyntaxTree(children.get(3)));
    }

    // assign-stmt -> identifier ':=' exp
    private Ast assignStmt(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        final Token t = children.get(1).getT();
        return new Ast.Assign(t, toSyntaxTree(children.get(0)), toSyntaxTree(children.get(2)));
    }

    // read-stmt -> 'read' identifier
    private Ast readStmt(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        final Token t = children.get(0).getT();
        return new Ast.Read(t, toSyntaxTree(children.get(1)));
    }

    // write-stmt -> 'write' exp
    private Ast writeStmt(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        final Token t = children.get(0).getT();
        return new Ast.Write(t, toSyntaxTree(children.get(1)));
    }

    // exp -> simple-exp exp'
    private Ast exp(final ParseTree<Token> parseTree) {
        List<ParseTree<Token>> children = parseTree.getChildren();
        final Ast left = toSyntaxTree(children.get(0));
        children = children.get(1).getChildren();
        if (children.isEmpty()) { return left; }
        final Ast right = toSyntaxTree(children.get(1));
        final Symbol op = children.get(0).getChildren().get(0).getSymbol();
        final Token t = children.get(0).getChildren().get(0).getT();
        if (op.equals(lt)) { return new Ast.LessThan(t,left, right); }
        if (op.equals(eq)) { return new Ast.Equals(t, left, right); }
        throw new TinyParseException(t);
    }

    // simple-exp -> term simple-exp'
    // simple-exp' -> add-op term simple-exp' | epsilon
    private Ast simpleExp(final ParseTree<Token> parseTree) {
        List<ParseTree<Token>> children = parseTree.getChildren();
        Ast result = toSyntaxTree(children.get(0));
        children = children.get(1).getChildren();
        while (!children.isEmpty()) {
            final Ast term = toSyntaxTree(children.get(1));
            final Symbol op = children.get(0).getChildren().get(0).getSymbol();
            final Token t = children.get(0).getChildren().get(0).getT();
            if (op.equals(plus)) { result = new Ast.Plus(t, result, term); }
            else if (op.equals(minus)) { result = new Ast.Minus(t, result, term); }
            else { throw new TinyParseException(t); }
            children = children.get(2).getChildren();
        }
        return result;
    }

    // term -> factor term'
    // term' -> mul-op factor term' | epsilon
    private Ast term(final ParseTree<Token> parseTree) {
        List<ParseTree<Token>> children = parseTree.getChildren();
        Ast result = toSyntaxTree(children.get(0));
        children = children.get(1).getChildren();
        while (!children.isEmpty()) {
            final Ast factor = toSyntaxTree(children.get(1));
            final Symbol op = children.get(0).getChildren().get(0).getSymbol();
            final Token t = children.get(0).getChildren().get(0).getT();
            if (op.equals(times)) { result = new Ast.Times(t, result, factor); }
            else if (op.equals(div)) { result = new Ast.Div(t, result, factor); }
            else { throw new TinyParseException(t); }
            children = children.get(2).getChildren();
        }
        return result;
    }

    // factor -> '(' exp ')' | number | identifier
    private Ast factor(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        return children.size() == 3 ? toSyntaxTree(children.get(1)) : toSyntaxTree(children.get(0));
    }

    private Ast number(final ParseTree<Token> parseTree) {
        final Token t = parseTree.getT();
        return new Ast.Num(t, ((Token.Num)t).getValue());
    }

    private Ast identifier(final ParseTree<Token> parseTree) {
        final Token t = parseTree.getT();
        return new Ast.Id(t, ((Token.Identifier)t).getValue());
    }
}