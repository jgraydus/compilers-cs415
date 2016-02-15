package token;

import org.junit.Test;
import data.Either;
import data.Pair;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static token.Tokenizer.*;

public class CharacterSourceTest {

    @Test
    public void toString_1() {
        final Tokenizer<Character,Character> test = character('a').and(character('z').or(whitespace()).many()).and(character('b'));
        final Source<Character> src = new CharacterSource("a    zzz\n zz \n zzzz\n  zzzz  c");
        final Either<Error<Character>,Pair<List<Character>,Source<Character>>> result = test.tokenize(src);
        assertTrue(result.getLeft().isPresent());
        final Source<Character> srcAfter = result.getLeft().get().getSource();
        assertEquals("at line 4:\n  zzzz  c\n        ^", srcAfter.toString());
    }
}
