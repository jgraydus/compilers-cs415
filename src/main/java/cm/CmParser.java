/* Joshua Graydus | March 2016 */
package cm;

import data.Either;
import parser.*;

import java.util.*;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static parser.Symbol.NonTerminal;
import static parser.Symbol.Terminal;

public class CmParser {
    // non-terminals
    private final Symbol program = new NonTerminal("program");
    private final Symbol declarationList = new NonTerminal("declaration-list");
    private final Symbol declaration = new NonTerminal("declaration");
    private final Symbol varDeclaration = new NonTerminal("var-declaration");
    private final Symbol typeSpecifier = new NonTerminal("type-specifier");
    private final Symbol funDeclaration = new NonTerminal("fun-declaration");
    private final Symbol params = new NonTerminal("params");
    private final Symbol compoundStmt = new NonTerminal("compound-stmt");
    private final Symbol paramList = new NonTerminal("param-list");
    private final Symbol param = new NonTerminal("param");
    private final Symbol localDeclarations = new NonTerminal("local-declarations");
    private final Symbol statementList = new NonTerminal("statement-list");
    private final Symbol statement = new NonTerminal("statement");
    private final Symbol expressionStmt = new NonTerminal("expression-stmt");
    private final Symbol selectionStmt = new NonTerminal("selection-stmt");
    private final Symbol iterationStmt = new NonTerminal("iteration-stmt");
    private final Symbol returnStmt = new NonTerminal("return-stmt");
    private final Symbol expression = new NonTerminal("expression");
    private final Symbol var = new NonTerminal("var");
    private final Symbol simpleExpression = new NonTerminal("simple-expression");
    private final Symbol additiveExpression = new NonTerminal("additive-expression");
    private final Symbol relop = new NonTerminal("relop");
    private final Symbol addop = new NonTerminal("addop");
    private final Symbol term = new NonTerminal("term");
    private final Symbol mulop = new NonTerminal("mulop");
    private final Symbol factor = new NonTerminal("factor");
    private final Symbol call = new NonTerminal("call");
    private final Symbol args = new NonTerminal("args");
    private final Symbol argList = new NonTerminal("argList");

    // terminals
    private final Symbol id = new Terminal("id");
    private final Symbol semicolon = new Terminal(";");
    private final Symbol num = new Terminal("num");
    private final Symbol leftBracket = new Terminal("[");
    private final Symbol rightBracket = new Terminal("]");
    private final Symbol intS = new Terminal("int");
    private final Symbol voidS = new Terminal("void");
    private final Symbol leftParen = new Terminal("(");
    private final Symbol rightParen = new Terminal(")");
    private final Symbol comma = new Terminal(",");
    private final Symbol leftBrace = new Terminal("{");
    private final Symbol rightBrace = new Terminal("}");
    private final Symbol ifS = new Terminal("if");
    private final Symbol elseS = new Terminal("else");
    private final Symbol whileS = new Terminal("while");
    private final Symbol returnS = new Terminal("return");
    private final Symbol assign = new Terminal("=");
    private final Symbol lessOrEqual = new Terminal("<=");
    private final Symbol less = new Terminal("<");
    private final Symbol greaterOrEqual = new Terminal(">=");
    private final Symbol greater = new Terminal(">");
    private final Symbol equal = new Terminal("==");
    private final Symbol notEqual = new Terminal("!=");
    private final Symbol plus = new Terminal("+");
    private final Symbol minus = new Terminal("-");
    private final Symbol times = new Terminal("*");
    private final Symbol div = new Terminal("/");

    // production rules
    private final List<Production> ps = new ArrayList<Production>(){{
        add(new Production(program, singletonList(declarationList)));
        add(new Production(declarationList, asList(declarationList, declaration)));
        add(new Production(declarationList, singletonList(declaration)));
        add(new Production(declaration, singletonList(varDeclaration)));
        add(new Production(declaration, singletonList(funDeclaration)));
        add(new Production(varDeclaration, asList(typeSpecifier, id, semicolon)));
        add(new Production(varDeclaration, asList(typeSpecifier, id, leftBracket, num, rightBracket, semicolon)));
        add(new Production(typeSpecifier, singletonList(intS)));
        add(new Production(typeSpecifier, singletonList(voidS)));
        add(new Production(funDeclaration, asList(typeSpecifier, id, leftParen, params, rightParen, compoundStmt)));
        add(new Production(params, singletonList(paramList)));
        add(new Production(params, singletonList(voidS)));
        add(new Production(paramList, asList(paramList, comma, param)));
        add(new Production(paramList, singletonList(param)));
        add(new Production(param, asList(typeSpecifier, id)));
        add(new Production(param, asList(typeSpecifier, id, leftBracket, rightBracket)));
        add(new Production(compoundStmt, asList(leftBrace, localDeclarations, statementList, rightBrace)));
        add(new Production(localDeclarations, asList(localDeclarations, varDeclaration)));
        add(new Production(localDeclarations, singletonList(Symbol.epsilon)));
        add(new Production(statementList, asList(statementList, statement)));
        add(new Production(statementList, singletonList(Symbol.epsilon)));
        add(new Production(statement, singletonList(expressionStmt)));
        add(new Production(statement, singletonList(compoundStmt)));
        add(new Production(statement, singletonList(selectionStmt)));
        add(new Production(statement, singletonList(iterationStmt)));
        add(new Production(statement, singletonList(returnStmt)));
        add(new Production(expressionStmt, asList(expression, semicolon)));
        add(new Production(expressionStmt, singletonList(semicolon)));
        add(new Production(selectionStmt, asList(ifS, leftParen, expression, rightParen, statement)));
        add(new Production(selectionStmt, asList(ifS, leftParen, expression, rightParen, statement, elseS, statement)));
        add(new Production(iterationStmt, asList(whileS, leftParen, expression, rightParen, statement)));
        add(new Production(returnStmt, asList(returnS, semicolon)));
        add(new Production(returnStmt, asList(returnS, expression, semicolon)));
        add(new Production(expression, asList(var, assign, expression)));
        add(new Production(expression, singletonList(simpleExpression)));
        add(new Production(var, singletonList(id)));
        add(new Production(var, asList(id, leftBracket, expression, rightBracket)));
        add(new Production(simpleExpression, asList(additiveExpression, relop, additiveExpression)));
        add(new Production(simpleExpression, singletonList(additiveExpression)));
        add(new Production(relop, singletonList(lessOrEqual)));
        add(new Production(relop, singletonList(less)));
        add(new Production(relop, singletonList(greaterOrEqual)));
        add(new Production(relop, singletonList(greater)));
        add(new Production(relop, singletonList(equal)));
        add(new Production(relop, singletonList(notEqual)));
        add(new Production(additiveExpression, asList(additiveExpression, addop, term)));
        add(new Production(additiveExpression, singletonList(term)));
        add(new Production(addop, singletonList(plus)));
        add(new Production(addop, singletonList(minus)));
        add(new Production(term, asList(term, mulop, factor)));
        add(new Production(term, singletonList(factor)));
        add(new Production(mulop, singletonList(times)));
        add(new Production(mulop, singletonList(div)));
        add(new Production(factor, asList(leftParen, expression, rightParen)));
        add(new Production(factor, singletonList(var)));
        add(new Production(factor, singletonList(call)));
        add(new Production(factor, singletonList(num)));
        add(new Production(call, asList(id, leftParen, args, rightParen)));
        add(new Production(args, singletonList(argList)));
        add(new Production(args, singletonList(Symbol.epsilon)));
        add(new Production(argList, asList(argList, comma, expression)));
        add(new Production(argList, singletonList(expression)));
    }};
    
    private final Function<Token,Symbol> toSymbol = tok -> {
        switch (tok.type) {
            case ELSE: return elseS;
            case IF: return ifS;
            case INT: return intS;
            case RETURN: return returnS;
            case VOID: return voidS;
            case WHILE: return whileS;
            case PLUS: return plus;
            case MINUS: return minus;
            case MULTIPLY: return times;
            case DIVIDE: return div;
            case LESS_THAN: return less;
            case LESS_THAN_OR_EQUAL: return lessOrEqual;
            case GREATER_THAN: return greater;
            case GREATER_THAN_OR_EQUAL: return greaterOrEqual;
            case EQUAL: return equal;
            case NOT_EQUAL: return notEqual;
            case ASSIGN: return assign;
            case SEMICOLON: return semicolon;
            case COMMA: return comma;
            case LEFT_PAREN: return leftParen;
            case RIGHT_PAREN: return rightParen;
            case LEFT_BRACKET: return leftBracket;
            case RIGHT_BRACKET: return rightBracket;
            case LEFT_BRACE: return leftBrace;
            case RIGHT_BRACE: return rightBrace;
            case ID: return id;
            case NUM: return num;
            case END_OF_FILE: return Symbol.$;
            default: throw new IllegalArgumentException("unknown token: " + tok);
        }
    };

    private final Parser<Token> parser;
    
    public CmParser() {
        final Grammar g = new Grammar(program, ps);
        parser = new LR1Parser<>(g, toSymbol);
    }

    public Either<String,Ast> parse(final List<Token> input) {
        // the scanner provides tokens for comments which is not part of the grammar
        final List<Token> in = input.stream()
                .filter(t -> t.type != Token.Type.COMMENT)
                .collect(toList());

        final Either<List<Token>,ParseTree<Token>> result = parser.parse(in);

        // if failed while building parse tree, then return an error message
        if (result.getLeft().isPresent()) {
            return Either.left(String.join("\n", result.getLeft().get().stream()
                    .map(token -> token.src == null ? token.toString() : token.src.toString()).collect(toList())));
        }
        // otherwise, convert parse tree to abstract syntax tree
        return Either.right(toSyntaxTree(result.getRight().get()));
    }

    // program -> declaration-list
    private Ast toSyntaxTree(final ParseTree<Token> parseTree) {
        return new Ast.DeclarationList(declarationList(parseTree.getChildren().get(0)));
    }

    // declaration-list -> declaration-list declaration | declaration
    private List<Ast> declarationList(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        if (children.size() == 1) {
            final Ast decl = declaration(children.get(0));
            return new ArrayList<>(singletonList(decl));
        } else {
            final List<Ast> decls = declarationList(children.get(0));
            decls.add(declaration(children.get(1)));
            return decls;
        }
    }

    private Ast.TypeSpecifier typeSpecifier(final ParseTree<Token> parseTree) {
        final ParseTree<Token> child = parseTree.getChildren().get(0);
        if (child.getSymbol().equals(intS)) { return Ast.TypeSpecifier.INT; }
        if (child.getSymbol().equals(voidS)) { return Ast.TypeSpecifier.VOID; }
        throw new IllegalStateException();
    }

    // declaration -> var-declaration | fun-declaration
    private Ast declaration(final ParseTree<Token> parseTree) {
        final ParseTree<Token> child = parseTree.getChildren().get(0);
        if (child.getSymbol().equals(varDeclaration)) { return varDeclaration(child); }
        if (child.getSymbol().equals(funDeclaration)) { return funDeclaration(child); }
        throw new IllegalStateException();
    }

    private String id(final ParseTree<Token> parseTree) {
        return ((Token.Id) parseTree.getT()).getName();
    }

    private int number(final ParseTree<Token> parseTree) {
        return ((Token.Num) parseTree.getT()).getValue();
    }

    // var-declaration -> type-specifier id ; | type-specifier id [ num ] ;
    private Ast varDeclaration(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        final Ast.TypeSpecifier type = typeSpecifier(children.get(0));
        final String name = id(children.get(1));
        final Optional<Integer> size = children.size() == 3
                ? Optional.empty()
                : Optional.of(number(children.get(3)));
        return new Ast.VarDeclaration(type, name, size);
    }

    // fun-declaration -> type-specifier id ( params ) compound-stmt
    private Ast funDeclaration(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        final Ast.TypeSpecifier type = typeSpecifier(children.get(0));
        final String name = id(children.get(1));
        final List<Ast> params = params(children.get(3));
        final Ast body = compoundStmt(children.get(5));
        return new Ast.FunDeclaration(type, name, params, body);
    }

    // params -> param-list | void
    private List<Ast> params(final ParseTree<Token> parseTree) {
        final ParseTree<Token> child = parseTree.getChildren().get(0);
        if (voidS.equals(child.getSymbol())) { return emptyList(); }
        else { return paramList(child); }
    }

    // param-list -> param-list , param | param
    private List<Ast> paramList(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        if (children.size() == 1) {
            return new ArrayList<>(singletonList(param(children.get(0))));
        } else {
            final List<Ast> params = paramList(children.get(0));
            params.add(param(children.get(2)));
            return params;
        }
    }

    // param -> type-specifier id | type-specifier id [ ]
    private Ast param(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        final Ast.TypeSpecifier type = typeSpecifier(children.get(0));
        final String name = id(children.get(1));
        if (children.size() == 2) { return new Ast.Param(type, name, false); } // not array
        if (children.size() == 4) { return new Ast.Param(type, name, true); }  // array
        throw new IllegalStateException();
    }

    // compound-stmt -> { local-declarations statement-list }
    private Ast compoundStmt(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        final List<Ast> localDeclarations = localDeclarations(children.get(1));
        final List<Ast> statements = statementList(children.get(2));
        return new Ast.CompoundStatement(localDeclarations, statements);
    }

    // local-declarations -> local-declarations var-declaration | empty
    private List<Ast> localDeclarations(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        if (children.isEmpty()) { return new ArrayList<>(); }
        else {
            final List<Ast> decls = localDeclarations(children.get(0));
            decls.add(varDeclaration(children.get(1)));
            return decls;
        }
    }

    // statement-list -> statement-list statement | empty
    private List<Ast> statementList(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        if (children.isEmpty()) { return new ArrayList<>(); }
        else {
            final List<Ast> statements = statementList(children.get(0));
            statements.add(statement(children.get(1)));
            return statements;
        }
    }

    // statement -> expression-stmt | compound-stmt | selection-stmt | iteration-stmt | return-stmt
    private Ast statement(final ParseTree<Token> parseTree) {
        final ParseTree<Token> child = parseTree.getChildren().get(0);
        if (expressionStmt.equals(child.getSymbol())) { return expressionStmt(child); }
        if (compoundStmt.equals(child.getSymbol())) { return compoundStmt(child); }
        if (selectionStmt.equals(child.getSymbol())) { return selectionStmt(child); }
        if (iterationStmt.equals(child.getSymbol())) { return iterationStmt(child); }
        if (returnStmt.equals(child.getSymbol())) { return returnStmt(child); }
        throw new IllegalStateException();
    }

    // expression-stmt -> expression ; | ;
    private Ast expressionStmt(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        if (children.size() == 1) { return new Ast.ExpressionStmt(Optional.empty()); }
        if (children.size() == 2) { return new Ast.ExpressionStmt(Optional.of(expression(children.get(0)))); }
        throw new IllegalStateException();
    }

    // expression -> var = expression | simple-expression
    private Ast expression(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        if (children.size() == 3) {
            final Ast var = var(children.get(0));
            final Ast expression = expression(children.get(2));
            return new Ast.Assignment(var, expression);
        }
        if (children.size() == 1) {
            return simpleExpression(children.get(0));
        }
        throw new IllegalStateException();
    }

    // var -> id | id [ expression ]
    private Ast var(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        final String name = id(children.get(0));
        if (children.size() == 1) { return new Ast.Var(name, Optional.empty()); }
        if (children.size() == 4) { return new Ast.Var(name, Optional.of(expression(children.get(2)))); }
        throw new IllegalStateException();
    }

    // simple-expression -> additive-expression relop additive-expression | additive-expression
    private Ast simpleExpression(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        if (children.size() == 1) {
            final Ast left = additiveExpression(children.get(0));
            return new Ast.Expression(left, Optional.empty(), Optional.empty());
        }
        if (children.size() == 3) {
            final Ast left = additiveExpression(children.get(0));
            final Ast.Operator op = operator(children.get(1));
            final Ast right = additiveExpression(children.get(2));
            return new Ast.Expression(left, Optional.of(op), Optional.of(right));
        }
        throw new IllegalStateException();
    }

    private final Map<Symbol,Ast.Operator> operators = new HashMap<Symbol,Ast.Operator>() {{
        put(less, Ast.Operator.LT);
        put(lessOrEqual, Ast.Operator.LEQ);
        put(greater, Ast.Operator.GT);
        put(greaterOrEqual, Ast.Operator.GEQ);
        put(equal, Ast.Operator.EQ);
        put(notEqual, Ast.Operator.NEQ);
        put(plus, Ast.Operator.PLUS);
        put(minus, Ast.Operator.MINUS);
        put(times, Ast.Operator.TIMES);
        put(div, Ast.Operator.DIVIDE);
    }};

    private Ast.Operator operator(final ParseTree<Token> parseTree) {
        final Symbol s = parseTree.getChildren().get(0).getSymbol();
        return Optional.ofNullable(operators.get(s)).orElseThrow(IllegalStateException::new);
    }

    // additive-expression -> additive-expression addop term | term
    private Ast additiveExpression(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        if (children.size() == 1) {
            return term(children.get(0));
        }
        if (children.size() == 3) {
            final Ast left = additiveExpression(children.get(0));
            final Ast.Operator op = operator(children.get(1));
            final Ast right = term(children.get(2));
            return new Ast.Expression(left, Optional.of(op), Optional.of(right));
        }
        throw new IllegalStateException();
    }

    // term -> term mulop factor | factor
    private Ast term(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        if (children.size() == 1) {
            return factor(children.get(0));
        }
        if (children.size() == 3) {
            final Ast left = term(children.get(0));
            final Ast.Operator op = operator(children.get(1));
            final Ast right = factor(children.get(2));
            return new Ast.Expression(left, Optional.of(op), Optional.of(right));
        }
        throw new IllegalStateException();
    }

    // factor -> ( expression ) | var | call | num
    private Ast factor(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        if (children.size() == 1) {
            final ParseTree<Token> child = children.get(0);
            if (var.equals(child.getSymbol())) { return var(child); }
            if (call.equals(child.getSymbol())) { return call(child); }
            if (num.equals(child.getSymbol())) { return new Ast.Constant(number(child)); }
            throw new IllegalStateException();
        }
        if (children.size() == 3) {
            return expression(children.get(1));
        }
        throw new IllegalStateException();
    }

    // call -> id ( args )
    private Ast call(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        final String name = id(children.get(0));
        final List<Ast> args = args(children.get(2));
        return new Ast.Call(name, args);
    }

    // args -> arg-list | empty
    private List<Ast> args(final ParseTree<Token> parseTree) {
        if (parseTree.getChildren().size() > 0) {
            return argList(parseTree.getChildren().get(0));
        }
        return emptyList();
    }

    // arg-list -> arg-list , expression | expression
    private List<Ast> argList(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        if (children.size() == 1) { return new ArrayList<>(singletonList(expression(children.get(0)))); }
        else {
            final List<Ast> args = argList(children.get(0));
            args.add(expression(children.get(2)));
            return args;
        }
    }

    // selection-stmt -> if ( expression ) statement | if ( expression ) statement else statement
    private Ast selectionStmt(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        if (children.size() == 5) {
            final Ast condition = expression(children.get(2));
            final Ast thenPart = statement(children.get(4));
            return new Ast.IfThen(condition, thenPart);
        }
        if (children.size() == 7) {
            final Ast condition = expression(children.get(2));
            final Ast thenPart = statement(children.get(4));
            final Ast elsePart = statement(children.get(6));
            return new Ast.IfThenElse(condition, thenPart, elsePart);
        }
        throw new IllegalStateException();
    }

    // iteration-stmt -> while ( expression ) statement
    private Ast iterationStmt(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        final Ast condition = expression(children.get(2));
        final Ast body = statement(children.get(4));
        return new Ast.While(condition, body);
    }

    // return-stmt -> return ; | return expression ;
    private Ast returnStmt(final ParseTree<Token> parseTree) {
        final List<ParseTree<Token>> children = parseTree.getChildren();
        if (children.size() == 2) { return new Ast.Return(Optional.empty()); }
        if (children.size() == 3) { return new Ast.Return(Optional.of(expression(children.get(1)))); }
        throw new IllegalStateException();
    }
}