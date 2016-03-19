/* Joshua Graydus | March 2016 */
package cm;

import data.Either;
import data.Pair;
import token.Error;
import token.Source;
import token.Tokenizer;

import java.util.List;

import static cm.Token.*;
import static token.Tokenizer.*;

public class CmScanner implements Tokenizer<Token,Character> {

    // keywords
    private final Tokenizer<Token,Character> elseT = string("else").and(letter().not().peek()).convert(Else::new);
    private final Tokenizer<Token,Character> ifT = string("if").and(letter().not().peek()).convert(If::new);
    private final Tokenizer<Token,Character> intT = string("int").and(letter().not().peek()).convert(Int::new);
    private final Tokenizer<Token,Character> returnT = string("return").and(letter().not().peek()).convert(Return::new);
    private final Tokenizer<Token,Character> voidT = string("void").and(letter().not().peek()).convert(Token.Void::new);
    private final Tokenizer<Token,Character> whileT = string("while").and(letter().not().peek()).convert(While::new);
    private final Tokenizer<Token,Character> keyword = oneOf(elseT, ifT, intT, returnT, voidT, whileT);

    // other reserved characters/strings
    private final Tokenizer<Token,Character> plus = character('+').convert(Plus::new);
    private final Tokenizer<Token,Character> minus = character('-').convert(Minus::new);
    private final Tokenizer<Token,Character> multiply = character('*').convert(Multiply::new);
    private final Tokenizer<Token,Character> divide = character('/').convert(Divide::new);
    private final Tokenizer<Token,Character> lessThan = character('<').and(character('=').not().peek())
            .convert(LessThan::new);
    private final Tokenizer<Token,Character> lessThanOrEqual = string("<=").convert(LessThanOrEqual::new);
    private final Tokenizer<Token,Character> greaterThan = character('>').and(character('=').not().peek())
            .convert(GreaterThan::new);
    private final Tokenizer<Token,Character> greaterThanOrEqual = string(">=").convert(GreaterThanOrEqual::new);
    private final Tokenizer<Token,Character> equal = string("==").convert(Equal::new);
    private final Tokenizer<Token,Character> notEqual = string("!=").convert(NotEqual::new);
    private final Tokenizer<Token,Character> assign = character('=').and(character('=').not().peek())
            .convert(Assign::new);
    private final Tokenizer<Token,Character> semicolon = character(';').convert(Semicolon::new);
    private final Tokenizer<Token,Character> comma = character(',').convert(Comma::new);
    private final Tokenizer<Token,Character> leftParen = character('(').convert(LeftParen::new);
    private final Tokenizer<Token,Character> rightParen = character(')').convert(RightParen::new);
    private final Tokenizer<Token,Character> leftBracket = character('[').convert(LeftBracket::new);
    private final Tokenizer<Token,Character> rightBracket = character(']').convert(RightBracket::new);
    private final Tokenizer<Token,Character> leftBrace = character('{').convert(LeftBrace::new);
    private final Tokenizer<Token,Character> rightBrace = character('}').convert(RightBrace::new);
    private final Tokenizer<Token,Character> special = oneOf(plus, minus, multiply, divide, lessThan, lessThanOrEqual,
            greaterThan, greaterThanOrEqual, equal, notEqual, assign, semicolon, comma, leftParen, rightParen,
            leftBracket, rightBracket, leftBrace, rightBrace);

    private final Tokenizer<Token,Character> identifier = letter().atLeast(1).convert(Id::new);
    private final Tokenizer<Token,Character> number = digit().atLeast(1).convert(Num::new);

    private final Tokenizer<Token,Character> comment =
            character('/').and(character('*')
                    .and(character('*').and(character('/')).not().many())
                    .and(character('*').and(character('/'))))
                    .convert(Comment::new);

    private final Tokenizer<Token,Character> endOfFile =
            Tokenizer.<Object,Character>emptySource().convert(EndOfFile::new);

    @Override
    public Either<Error<Character>, Pair<List<Token>, Source<Character>>> tokenize(final Source<Character> source) {
        return oneOf(whitespace(), comment, keyword, special, identifier, number)
                .many().and(endOfFile).tokenize(source);
    }
}