package cm;

import data.Either;
import data.Pair;
import org.junit.Test;
import token.CharacterSource;
import token.Error;
import token.Source;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class CmParserTest {

    static String getTestProgram(final String filename) {
        final BufferedReader reader =
                new BufferedReader(new InputStreamReader(CmScannerTest.class.getResourceAsStream(filename)));
        return String.join( "\n", reader.lines().collect(toList()));
    }

    @Test
    public void gcd() {
        final String program = getTestProgram("gcd.cm");
        final Either<Error<Character>, Pair<List<Token>, Source<Character>>> result = new CmScanner()
                .tokenize(new CharacterSource(program));
        final CmParser parser = new CmParser();
        parser.parse(result.getRight().get().getLeft());
        // TODO assert that AST was created
    }

    @Test
    public void sort() {
        final String program = getTestProgram("sort.cm");
        final Either<Error<Character>, Pair<List<Token>, Source<Character>>> result = new CmScanner()
                .tokenize(new CharacterSource(program));
        final CmParser parser = new CmParser();
        parser.parse(result.getRight().get().getLeft());
        // TODO assert that AST was created
    }
}
