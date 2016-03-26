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

public class CmAnalyzerTest {

    static String getTestProgram(final String filename) {
        final BufferedReader reader =
                new BufferedReader(new InputStreamReader(CmScannerTest.class.getResourceAsStream(filename)));
        return String.join( "\n", reader.lines().collect(toList()));
    }

    @Test
    public void gcd() {
        final String program = getTestProgram("gcd.cm");
        final Either<Error<Character>, Pair<List<Token>, Source<Character>>> tokens = new CmScanner()
                .tokenize(new CharacterSource(program));
        final CmParser parser = new CmParser();
        final Ast ast = parser.parse(tokens.getRight().get().getLeft()).getRight().get();
        final CmAnalyzer analyzer = new CmAnalyzer();
        analyzer.typeCheck(ast);
    }

    @Test
    public void sort() {
        final String program = getTestProgram("sort.cm");
        final Either<Error<Character>, Pair<List<Token>, Source<Character>>> tokens = new CmScanner()
                .tokenize(new CharacterSource(program));
        final CmParser parser = new CmParser();
        final Ast ast = parser.parse(tokens.getRight().get().getLeft()).getRight().get();
        final CmAnalyzer analyzer = new CmAnalyzer();
        analyzer.typeCheck(ast);
    }
}
