package cm;

import data.Either;
import data.Pair;
import org.junit.Test;
import tiny.tm.Instruction;
import token.CharacterSource;
import token.Error;
import token.Source;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class CmCodeGenTest {

    static String getTestProgram(final String filename) {
        final BufferedReader reader =
                new BufferedReader(new InputStreamReader(CmScannerTest.class.getResourceAsStream(filename)));
        return String.join( "\n", reader.lines().collect(toList()));
    }

    @Test
    public void gcd() {
        final String program = getTestProgram("test.cm");
        final Either<Error<Character>, Pair<List<Token>, Source<Character>>> tokens = new CmScanner()
                .tokenize(new CharacterSource(program));
        final CmParser parser = new CmParser();
        final Ast ast = parser.parse(tokens.getRight().get().getLeft()).getRight().get();
        final CmCodeGen codeGen = new CmCodeGen();
        final List<Instruction> instrs = codeGen.emit(ast);
        System.out.println(instrs);
    }

    @Test
    public void sort() {
        final String program = getTestProgram("sort.cm");
        final Either<Error<Character>, Pair<List<Token>, Source<Character>>> tokens = new CmScanner()
                .tokenize(new CharacterSource(program));
        final CmParser parser = new CmParser();
        final Ast ast = parser.parse(tokens.getRight().get().getLeft()).getRight().get();
        final CmCodeGen codeGen = new CmCodeGen();
        final List<Instruction> instrs = codeGen.emit(ast);
        System.out.println(instrs);
    }
}
