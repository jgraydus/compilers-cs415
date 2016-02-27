/* Joshua Graydus | January 2016 */
package tiny;

import data.Either;
import data.Pair;
import tiny.type.TypeError;
import token.CharacterSource;
import token.Error;
import token.Source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/** a compiler from the TINY language to TINY machine instructions */
public class Tiny {

    public static void main(final String... args) {
        if (args.length == 0) {
            System.out.println("provide source file name");
            System.exit(1);
        }
        final String code = new Tiny().compile(args[0]);
        // if output file name was given as an argument, write the generated code to that file
        if (args.length > 1) {
            final String outputFilename = args[1];
            writeOutput(outputFilename, code);
        }
        // otherwise write to stdout
        else {
            System.out.println(code);
        }
    }

    private static void writeOutput(final String outputFilename, final String code) {
        try (final FileWriter out = new FileWriter(new File(outputFilename))) {
            out.write(code);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final TinyScanner scanner = new TinyScanner();
    private final TinyLL1Parser parser = new TinyLL1Parser();
    private final TinyAnalyzer analyzer = new TinyAnalyzer();
    private final TinyCodeGen codeGen = new TinyCodeGenTM();
    // private final TinyCodeGen codeGen = new TinyCodeGenLLVM();

    public String compile(final String filename) {
        final CharacterSource source = new CharacterSource(readFile(filename));
        final List<Token> tokens = scan(source);
        final Ast ast = parse(tokens);
        typeCheck(ast);
        return generateCode(ast);
    }

    private String readFile(final String filename) {
        try (final BufferedReader reader = new BufferedReader(new FileReader(new File(filename)))) {
            return String.join("\n", reader.lines().collect(toList()));
        } catch (Exception e) { return fail(singletonList(e.getMessage())); }
    }

    private List<Token> scan(final CharacterSource source) {
        final Either<Error<Character>, Pair<List<Token>, Source<Character>>> result = scanner.tokenize(source);
        result.getLeft().ifPresent(error -> fail(singletonList(error.getSource().toString())));
        return result.getRight().get().getLeft();
    }

    private Ast parse(final List<Token> tokens) {
        final Either<String,Ast> result = parser.parse(tokens);
        if (result.getLeft().isPresent()) { return fail(singletonList(result.getLeft().get())); }
        return result.getRight().get();
    }

    private void typeCheck(final Ast ast) {
        final Optional<TypeError> result = analyzer.typeCheck(ast);
        if (result.isPresent()) { fail(singletonList(result.get().toString())); }
    }

    private String generateCode(final Ast ast) {
        return codeGen.generate(ast);
    }

    private <T> T fail(final List<String> messages) {
        System.err.println("compilation failed!");
        messages.forEach(System.err::println);
        System.exit(1);
        return null;
    }
}