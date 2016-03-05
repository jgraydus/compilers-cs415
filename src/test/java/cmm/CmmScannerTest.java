package cmm;

import data.Either;
import data.Pair;
import org.junit.Test;
import token.CharacterSource;
import token.Error;
import token.Source;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

import static cmm.Token.*;

public class CmmScannerTest {

    static String getTestProgram(final String filename) {
        final BufferedReader reader =
                new BufferedReader(new InputStreamReader(CmmScannerTest.class.getResourceAsStream(filename)));
        return String.join( "\n", reader.lines().collect(toList()));
    }

    @Test
    public void test1() {
        final String program = getTestProgram("gcd.cmm");
        final Either<Error<Character>, Pair<List<Token>, Source<Character>>> result = new CmmScanner()
                .tokenize(new CharacterSource(program));

        assertTrue(result.getRight().isPresent());

        final List<Token> expected = asList(
                new Comment(null),
                new Int(null),
                new Id(null, asList('g', 'c', 'd')),
                new LeftParen(null),
                new Int(null),
                new Id(null, singletonList('u')),
                new Comma(null),
                new Int(null),
                new Id(null, singletonList('v')),
                new RightParen(null),
                new LeftBrace(null),
                new If(null),
                new LeftParen(null),
                new Id(null, singletonList('v')),
                new Equal(null),
                new Num(null, singletonList('0')),
                new RightParen(null),
                new Return(null),
                new Id(null, singletonList('u')),
                new Semicolon(null),
                new Else(null),
                new Return(null),
                new Id(null, asList('g','c','d')),
                new LeftParen(null),
                new Id(null, singletonList('v')),
                new Comma(null),
                new Id(null, singletonList('u')),
                new Minus(null),
                new Id(null, singletonList('u')),
                new Divide(null),
                new Id(null, singletonList('v')),
                new Multiply(null),
                new Id(null, singletonList('v')),
                new RightParen(null),
                new Semicolon(null),
                new Comment(null),
                new RightBrace(null),
                new Token.Void(null),
                new Id(null, asList('m','a','i','n')),
                new LeftParen(null),
                new Token.Void(null),
                new RightParen(null),
                new LeftBrace(null),
                new Int(null),
                new Id(null, singletonList('x')),
                new Semicolon(null),
                new Int(null),
                new Id(null, singletonList('y')),
                new Semicolon(null),
                new Id(null, singletonList('x')),
                new Assign(null),
                new Id(null, asList('i','n','p','u','t')),
                new LeftParen(null),
                new RightParen(null),
                new Semicolon(null),
                new Id(null, singletonList('y')),
                new Assign(null),
                new Id(null, asList('i','n','p','u','t')),
                new LeftParen(null),
                new RightParen(null),
                new Semicolon(null),
                new Id(null, asList('o','u','t','p','u','t')),
                new LeftParen(null),
                new Id(null, asList('g','c','d')),
                new LeftParen(null),
                new Id(null, singletonList('x')),
                new Comma(null),
                new Id(null, singletonList('y')),
                new RightParen(null),
                new RightParen(null),
                new Semicolon(null),
                new RightBrace(null)
        );

        final List<Token> actual = result.getRight().get().getLeft();
        assertEquals(expected, actual);
        //System.out.println(actual);
    }

    @Test
    public void test2() {
        final String program = getTestProgram("sort.cmm");
        final Either<Error<Character>, Pair<List<Token>, Source<Character>>> result = new CmmScanner()
                .tokenize(new CharacterSource(program));
        assertTrue(result.getRight().isPresent());
        final List<Token> actual = result.getRight().get().getLeft();
        //System.out.println(actual);
    }
}