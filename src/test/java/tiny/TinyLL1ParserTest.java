package tiny;

import data.Either;
import org.junit.Test;
import parser.ParseTree;
import token.CharacterSource;

import java.util.List;

public class TinyLL1ParserTest {

    @Test
    public void test1() {
        final TinyScanner scanner = new TinyScanner();
        final TinyLL1Parser parser = new TinyLL1Parser();

        final String program = TinyScannerTest.getTestProgram("gcd.tny");

        final List<Token> tokens = scanner.tokenize(new CharacterSource(program))
                .getRight().get().getLeft();

        final Either<String,Ast> tree = parser.parse(tokens);

        //System.out.println(tree.getRight().get());
    }
}
