/* Joshua Graydus | April 2016 */
package cm;

import data.Either;
import data.Pair;
import tiny.tm.Instruction;
import token.CharacterSource;
import token.Error;
import token.Source;

import java.io.*;
import java.util.List;

public class Cm {

    public static void main(final String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("please provide path to source code file");
            System.exit(0);
        }
        final String fileName = args[0];
        final File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("the file " + fileName + " does not exist");
            System.exit(1);
        }
        System.out.println("reading input file " + fileName);
        final String contents = readFile(file);

        System.out.println("scanning...");
        final CmScanner scanner = new CmScanner();
        final CharacterSource source = new CharacterSource(contents);
        final Either<Error<Character>, Pair<List<Token>, Source<Character>>> scan = scanner.tokenize(source);

        if (scan.getLeft().isPresent()) {
            System.out.println("scanner error");
            System.out.println(scan.getLeft().get().getSource());
            System.exit(1);
        }

        final List<Token> tokens = scan.getRight().get().getLeft();

        System.out.println("parsing...");
        final CmParser parser = new CmParser();
        final Either<String,Ast> parse = parser.parse(tokens);

        if (parse.getLeft().isPresent()) {
            System.out.println("parser error");
            System.out.println(parse.getLeft().get());
        }

        final Ast ast = parse.getRight().get();

        System.out.println("type checking...");
        try {
            final CmAnalyzer analyzer = new CmAnalyzer();
            analyzer.typeCheck(ast);
        } catch (Exception e) {
            System.out.println("typecheck error");
            System.out.println(e);
            System.exit(1);
        }

        System.out.println("generating tm code...");
        final CmCodeGen codeGen = new CmCodeGen();
        final List<Instruction> instructions = codeGen.emit(ast);
        final String code = instructions.toString();

        final String outputFileName = outputFileName(fileName);
        System.out.println("writing output file " + outputFileName + "...");
        final File outputFile = new File(outputFileName);
        writeFile(outputFile, code);

        System.out.println("ok");
    }

    private static String readFile(final File file) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        final StringBuilder sb = new StringBuilder();
        reader.lines().forEach(sb::append);
        return sb.toString();
    }

    private static void writeFile(final File file, final String contents) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(contents);
        writer.flush();
        writer.close();
    }

    private static String outputFileName(final String inputFileName) {
        if (inputFileName.endsWith(".cm")) {
            final String withoutEnding = inputFileName.substring(0, inputFileName.length() - 3);
            return withoutEnding + ".tm";
        } else {
            return inputFileName + ".tm";
        }
    }
}