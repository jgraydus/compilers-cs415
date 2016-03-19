package tiny;

import data.Either;
import org.junit.Test;
import token.CharacterSource;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class TinyLR1ParserTest {

    @Test
    public void test1() {
        final TinyScanner scanner = new TinyScanner();
        final TinyLR1Parser parser = new TinyLR1Parser();

        final String program = TinyScannerTest.getTestProgram("001.tny");

        final List<Token> tokens = scanner.tokenize(new CharacterSource(program))
                .getRight().get().getLeft();

        final Either<String,Ast> result = parser.parse(tokens);

        assertTrue(result.getRight().isPresent()); // parse was successful (produced a syntax tree)
    }
}