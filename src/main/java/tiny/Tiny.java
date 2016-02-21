/* Joshua Graydus | January 2016 */
package tiny;

import data.Either;
import data.Pair;
import parser.ParseTree;
import token.CharacterSource;
import token.Source;
import token.Error;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class Tiny {
    private final TinyScanner scanner = new TinyScanner();
    private final TinyLL1Parser parser = new TinyLL1Parser();

    public void compile(final String filename) {
        final CharacterSource source = new CharacterSource(readFile(filename));
        final List<Token> tokens = scan(source);
        final Ast ast = parse(tokens);
        // TODO semantic analysis
        // TODO code generation
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

    private <T> T fail(final List<String> messages) {
        System.err.println("compilation failed.");
        messages.forEach(System.err::println);
        System.exit(1);
        return null;
    }
}