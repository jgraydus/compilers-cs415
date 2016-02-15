/* Joshua Graydus | January 2016 */
package tiny;

import token.Error;
import token.Source;
import token.Tokenizer;
import data.Either;
import data.Pair;

import java.util.List;

import static tiny.Token.*;
import static token.Tokenizer.*;

/** An implementation of Tokenizer that converts TINY source code into tokens. */
public class TinyScanner implements Tokenizer<Token,Character> {
    /* comments - begin with { and end with }, and they cannot be nested */
    private final Tokenizer<Token,Character> comment = fromTo(character('{'), character('}')).convert(Comment::new);
    /* keywords - necessary to look ahead one character to avoid accepting part of identifier that starts with the same string */
    private final Tokenizer<Token,Character> read = string("read").and(letter().not().peek()).convert(Read::new);
    private final Tokenizer<Token,Character> write = string("write").and(letter().not().peek()).convert(Write::new);
    private final Tokenizer<Token,Character> repeat = string("repeat").and(letter().not().peek()).convert(Repeat::new);
    private final Tokenizer<Token,Character> until = string("until").and(letter().not().peek()).convert(Until::new);
    private final Tokenizer<Token,Character> ifT = string("if").and(letter().not().peek()).convert(If::new);
    private final Tokenizer<Token,Character> then = string("then").and(letter().not().peek()).convert(Then::new);
    private final Tokenizer<Token,Character> elseT = string("else").and(letter().not().peek()).convert(Else::new);
    private final Tokenizer<Token,Character> end = string("end").and(letter().not().or(emptySource()).peek()).convert(End::new);
    private final Tokenizer<Token,Character> keyword = oneOf(read, write, repeat, until, ifT, then, elseT, end);
    /* operators */
    private final Tokenizer<Token,Character> equal = character('=').convert(Equal::new);
    private final Tokenizer<Token,Character> assignment = string(":=").convert(Assignment::new);
    private final Tokenizer<Token,Character> plus = character('+').convert(Plus::new);
    private final Tokenizer<Token,Character> minus = character('-').convert(Minus::new);
    private final Tokenizer<Token,Character> times = character('*').convert(Times::new);
    private final Tokenizer<Token,Character> over = character('/').convert(Over::new);
    private final Tokenizer<Token,Character> lessThan = character('<').convert(LessThan::new);
    private final Tokenizer<Token,Character> operator = oneOf(equal, assignment, plus, minus, times, over, lessThan);
    /* identifiers */
    private final Tokenizer<Token,Character> identifier = letter().atLeast(1).convert(Identifier::new);
    /* literals */
    private final Tokenizer<Token,Character> number = digit().atLeast(1).convert(Num::new);
    /* other */
    private final Tokenizer<Token,Character> semicolon = character(';').convert(Semicolon::new);
    private final Tokenizer<Token,Character> leftParens = character('(').convert(LeftParens::new);
    private final Tokenizer<Token,Character> rightParens = character(')').convert(RightParens::new);
    private final Tokenizer<Token,Character> parens = oneOf(leftParens, rightParens);
    private final Tokenizer<Token,Character> endOfFile = Tokenizer.<Object,Character>emptySource().convert(EndOfFile::new);

    /* complete */
    private final Tokenizer<Token,Character> tinyTokenizer =
            oneOf(whitespace(), comment, keyword, operator, identifier, number, semicolon, parens).many().and(endOfFile);
    
    @Override
    public Either<Error<Character>, Pair<List<Token>, Source<Character>>> tokenize(Source<Character> source) {
        return tinyTokenizer.tokenize(source);
    }
}