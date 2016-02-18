package tiny;

import org.junit.Test;
import token.CharacterSource;

import java.util.List;

public class TinyLL1ParserTest {

    @Test
    public void test1() {
        final TinyScanner scanner = new TinyScanner();
        final TinyLL1Parser parser = new TinyLL1Parser();

        final String program = TinyScannerTest.getTestProgram();

        final List<Token> tokens = scanner.tokenize(new CharacterSource(program))
                .getRight().get().getLeft();

        System.out.println(parser.parse(tokens));
    }
}
