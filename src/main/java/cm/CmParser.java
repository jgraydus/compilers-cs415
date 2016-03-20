/* Joshua Graydus | March 2016 */
package cm;

import data.Either;
import parser.*;
import tiny.Ast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
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

        // TODO build AST
        return null;
    }
}