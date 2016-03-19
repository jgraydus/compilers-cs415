package tiny;

import org.junit.Test;
import token.CharacterSource;

import java.util.List;

public class TinyLR1ParserTest {

    @Test
    public void test1() {
        final TinyScanner scanner = new TinyScanner();
        final TinyLR1Parser parser = new TinyLR1Parser();

        final String program = TinyScannerTest.getTestProgram("gcd.tny");

        final List<Token> tokens = scanner.tokenize(new CharacterSource(program))
                .getRight().get().getLeft();

//        System.out.println(parser.parse(tokens));

        //final Either<String,Ast> tree = parser.parse(tokens);

        //System.out.println(tree.getRight().get());

        // TODO
    }
}