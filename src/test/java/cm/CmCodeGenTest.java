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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CmCodeGenTest {

    private final CmScanner scanner = new CmScanner();
    private final CmParser parser = new CmParser();
    private final CmCodeGen codeGen = new CmCodeGen();

    static String getTestProgram(final String filename) {
        final BufferedReader reader =
                new BufferedReader(new InputStreamReader(CmScannerTest.class.getResourceAsStream(filename)));
        return String.join( "\n", reader.lines().collect(toList()));
    }

    @Test
    public void gcd() {
        final String program = getTestProgram("gcd.cm");
        final Ast ast = ast(program);
        final List<Instruction> instrs = codeGen.emit(ast);
        //System.out.println(instrs);
    }

    @Test
    public void sort() {
        final String program = getTestProgram("sort.cm");
        final Ast ast = ast(program);
        final List<Instruction> instrs = codeGen.emit(ast);
        //System.out.println(instrs);
    }

    private Ast ast(final String program) {
        final Either<Error<Character>, Pair<List<Token>, Source<Character>>> tokens = scanner
                .tokenize(new CharacterSource(program));
        final Either<String,Ast> result = parser.parse(tokens.getRight().get().getLeft());
        return result.getRight().get();
    }

    @Test
    public void deadCodeRemoval1() throws Exception {
        final String program =
                "void main(void) {"+
                "  if (false) {"+
                "    42;"+
                "  }"+
                "}";
        final Ast ast = codeGen.removeDeadCode(ast(program)).get();
        final Ast.DeclarationList decs = (Ast.DeclarationList) ast;
        final Ast.FunDeclaration main = (Ast.FunDeclaration) decs.getDeclarations().get(0);
        final Ast.CompoundStatement body = (Ast.CompoundStatement) main.getBody();
        assertTrue(body.getStatements().isEmpty()); // the if statement should be optimized away
    }

    @Test
    public void deadCodeRemoval2() throws Exception {
        final String program =
                "void main(void) {"+
                        "  if (true) {"+
                        "    42;"+
                        "  }"+
                        "}";
        final Ast ast = codeGen.removeDeadCode(ast(program)).get();
        final Ast.DeclarationList decs = (Ast.DeclarationList) ast;
        final Ast.FunDeclaration main = (Ast.FunDeclaration) decs.getDeclarations().get(0);
        final Ast.CompoundStatement body = (Ast.CompoundStatement) main.getBody();
        assertTrue(body.getStatements().get(0) instanceof Ast.CompoundStatement); // condition should be removed
    }

    @Test
    public void deadCodeRemoval3() throws Exception {
        final String program =
                "void main(void) {"+
                        "  if (true) {"+
                        "    42;"+
                        "  } else {" +
                        "    93;"+
                        "  }"+
                        "}";
        final Ast ast = codeGen.removeDeadCode(ast(program)).get();
        final Ast.DeclarationList decs = (Ast.DeclarationList) ast;
        final Ast.FunDeclaration main = (Ast.FunDeclaration) decs.getDeclarations().get(0);
        final Ast.CompoundStatement body = (Ast.CompoundStatement) main.getBody();
        final Ast.Constant c = (Ast.Constant)((Ast.Expression)((Ast.ExpressionStmt)((Ast.CompoundStatement)body
                .getStatements().get(0)).getStatements().get(0)).getExpression().get()).getLeft();
        assertEquals(42, c.getValue());
    }

    @Test
    public void deadCodeRemoval4() throws Exception {
        final String program =
                "void main(void) {"+
                        "  if (false) {"+
                        "    42;"+
                        "  } else {" +
                        "    93;"+
                        "  }"+
                        "}";
        final Ast ast = codeGen.removeDeadCode(ast(program)).get();
        final Ast.DeclarationList decs = (Ast.DeclarationList) ast;
        final Ast.FunDeclaration main = (Ast.FunDeclaration) decs.getDeclarations().get(0);
        final Ast.CompoundStatement body = (Ast.CompoundStatement) main.getBody();
        final Ast.Constant c = (Ast.Constant)((Ast.Expression)((Ast.ExpressionStmt)((Ast.CompoundStatement)body
                .getStatements().get(0)).getStatements().get(0)).getExpression().get()).getLeft();
        assertEquals(93, c.getValue());
    }

    @Test
    public void deadCodeRemoval5() throws Exception {
        final String program =
                "void main(void) {"+
                        "  while (false) {"+
                        "    42;"+
                        "  }" +
                        "}";
        final Ast ast = codeGen.removeDeadCode(ast(program)).get();
        System.out.println(ast);
        final Ast.DeclarationList decs = (Ast.DeclarationList) ast;
        final Ast.FunDeclaration main = (Ast.FunDeclaration) decs.getDeclarations().get(0);
        final Ast.CompoundStatement body = (Ast.CompoundStatement) main.getBody();
        assertTrue(body.getStatements().isEmpty()); // while statement should be removed
    }
}