package tiny;

import data.Either;
import data.Pair;
import org.junit.Test;
import token.CharacterSource;
import token.Error;
import token.Source;
import token.Tokenizer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static tiny.Token.*;

public class TinyScannerTest {

    static String getTestProgram(final String filename) {
        final BufferedReader reader =
                new BufferedReader(new InputStreamReader(TinyScannerTest.class.getResourceAsStream(filename)));
        return String.join( "\n", reader.lines().collect(toList()));
    }

    @Test
    public void test() {
        final Tokenizer<Token,Character> tiny = new TinyScanner();
        final String testProgram = getTestProgram("fact.tny");
        final Source<Character> src = new CharacterSource(testProgram);
        final Either<Error<Character>,Pair<List<Token>,Source<Character>>> result = tiny.tokenize(src);
        assertTrue(result.getRight().isPresent());

        final List<Token> expected = asList(
                new Comment(null),
                new Read(null),
                new Identifier(null, asList('x')),
                new Semicolon(null),
                new Comment(null),
                new If(null),
                new Num(null, asList('0')),
                new LessThan(null),
                new Identifier(null, asList('x')),
                new Then(null),
                new Comment(null),
                new Identifier(null, asList('f','a','c','t')),
                new Assignment(null),
                new Num(null, asList('1')),
                new Semicolon(null),
                new Repeat(null),
                new Identifier(null, asList('f','a','c','t')),
                new Assignment(null),
                new Identifier(null, asList('f','a','c','t')),
                new Times(null),
                new Identifier(null, asList('x')),
                new Semicolon(null),
                new Identifier(null, asList('x')),
                new Assignment(null),
                new Identifier(null, asList('x')),
                new Minus(null),
                new Num(null, asList('1')),
                new Until(null),
                new Identifier(null, asList('x')),
                new Equal(null),
                new Num(null, asList('0')),
                new Semicolon(null),
                new Write(null),
                new Identifier(null, asList('f','a','c','t')),
                new Comment(null),
                new End(null),
                new EndOfFile(null)
        );

        final List<Token> actual = result.getRight().get().getLeft();
        assertEquals(expected, actual);
    }
}