package tiny;

import org.junit.Test;
import token.CharacterSource;

import java.util.List;

public class TinyLL1Test {

    @Test
    public void test1() {
        final TinyScanner scanner = new TinyScanner();
        final TinyLL1 parser = new TinyLL1();

        final String program = TinyScannerTest.getTestProgram();

        final List<Token> tokens = scanner.tokenize(new CharacterSource(program))
                .getRight().get().getLeft();

        System.out.println(parser.parse(tokens));
    }
}
