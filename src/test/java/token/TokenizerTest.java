package token;

import org.junit.Test;
import data.Either;
import data.Pair;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class TokenizerTest {

    @Test
    public void succeed() {
        final Tokenizer<Object,Character> succeed = Tokenizer.succeed();
        final Either<Error<Character>, Pair<List<Object>, Source<Character>>> result = succeed.tokenize(new CharacterSource(""));
        assertTrue(result.getRight().isPresent());
        assertTrue(result.getRight().get().getLeft().isEmpty());
    }

    @Test
    public void fail() {
        final Tokenizer<Object,Character> fail = Tokenizer.fail();
        final Either<Error<Character>, Pair<List<Object>, Source<Character>>> result = fail.tokenize(new CharacterSource(""));
        assertFalse(result.getRight().isPresent());
    }

    @Test
    public void or_1() {
        final Tokenizer<Character,Character> fail = Tokenizer.fail();
        final Tokenizer<Character,Character> a = Tokenizer.character('a');
        final Tokenizer<Character,Character> failOrA = fail.or(a);
        final Tokenizer<Character,Character> aOrFail = a.or(fail);
        final Source<Character> src = new CharacterSource("a");

        {
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> failOrAResult = failOrA.tokenize(src);
            assertTrue(failOrAResult.getRight().isPresent());
            assertTrue(failOrAResult.getRight().get().getLeft().contains('a'));
        }

        {
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> aOrFailResult = aOrFail.tokenize(src);
            assertTrue(aOrFailResult.getRight().isPresent());
            assertTrue(aOrFailResult.getRight().get().getLeft().contains('a'));
        }

        assertFalse(failOrA.tokenize(new CharacterSource("z")).getRight().isPresent());
        assertFalse(aOrFail.tokenize(new CharacterSource("z")).getRight().isPresent());

    }

    @Test
    public void or_2() {
        final Tokenizer<Character,Character> a = Tokenizer.character('a');
        final Tokenizer<Character,Character> b = Tokenizer.character('b');
        final Tokenizer<Character,Character> aOrB = a.or(b);
        assertTrue(aOrB.tokenize(new CharacterSource("a")).getRight().isPresent());
        assertTrue(aOrB.tokenize(new CharacterSource("b")).getRight().isPresent());
        assertFalse(aOrB.tokenize(new CharacterSource("z")).getRight().isPresent());
    }

    @Test
    public void and_1() {
        final Tokenizer<Character,Character> succeed = Tokenizer.succeed();
        final Tokenizer<Character,Character> a = Tokenizer.character('a');
        final Tokenizer<Character,Character> succeedAndA = succeed.and(a);
        final Tokenizer<Character,Character> aAndSucceed = a.and(succeed);
        final Source<Character> src = new CharacterSource("a");

        {
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> succeedAndAResult = succeedAndA.tokenize(src);
            assertTrue(succeedAndAResult.getRight().isPresent());
            assertTrue(succeedAndAResult.getRight().get().getLeft().contains('a'));
        }

        {
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> aAndSucceedResult = aAndSucceed.tokenize(src);
            assertTrue(aAndSucceedResult.getRight().isPresent());
            assertTrue(aAndSucceedResult.getRight().get().getLeft().contains('a'));
        }

        assertFalse(succeedAndA.tokenize(new CharacterSource("z")).getRight().isPresent());
        assertFalse(aAndSucceed.tokenize(new CharacterSource("z")).getRight().isPresent());
    }

    @Test
    public void and_2() {
        final Tokenizer<Character,Character> a = Tokenizer.character('a');
        final Tokenizer<Character,Character> b = Tokenizer.character('b');
        final Tokenizer<Character,Character> aAndB = a.and(b);
        final Tokenizer<Character,Character> bAndA = b.and(a);

        {
            final Source<Character> ab = new CharacterSource("ab");
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> aAndBResult = aAndB.tokenize(ab);
            assertTrue(aAndBResult.getRight().isPresent());
            assertTrue(aAndBResult.getRight().get().getLeft().get(0).equals('a'));
            assertTrue(aAndBResult.getRight().get().getLeft().get(1).equals('b'));
        }

        {
            final Source<Character> ba = new CharacterSource("ba");
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> bAndAResult = bAndA.tokenize(ba);
            assertTrue(bAndAResult.getRight().isPresent());
            assertTrue(bAndAResult.getRight().get().getLeft().get(0).equals('b'));
            assertTrue(bAndAResult.getRight().get().getLeft().get(1).equals('a'));
        }

        assertFalse(aAndB.tokenize(new CharacterSource("az")).getRight().isPresent());
        assertFalse(bAndA.tokenize(new CharacterSource("bz")).getRight().isPresent());
    }

    @Test
    public void not() {
        final Tokenizer<Character,Character> notA = Tokenizer.character('a').not();

        {
            final Source<Character> src = new CharacterSource("a");
            assertFalse(notA.tokenize(src).getRight().isPresent());
        }

        {
            final Source<Character> src = new CharacterSource("b");
            final Either<Error<Character>,Pair<List<Character>,Source<Character>>> result = notA.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertTrue(result.getRight().get().getLeft().get(0).equals('b'));
        }
    }

    @Test
    public void fromTo() {
        final Tokenizer<Character,Character> leftBracket = Tokenizer.character('{');
        final Tokenizer<Character,Character> rightBracket = Tokenizer.character('}');
        final Tokenizer<Character,Character> comment = Tokenizer.fromTo(leftBracket, rightBracket);

        {
            final Source<Character> src = new CharacterSource("{this is a comment}");
            final Either<Error<Character>,Pair<List<Character>,Source<Character>>> result = comment.tokenize(src);
            assertTrue(result.getRight().isPresent());
        }

        {
            final Source<Character> src = new CharacterSource("this is not a comment");
            final Either<Error<Character>,Pair<List<Character>,Source<Character>>> result = comment.tokenize(src);
            assertFalse(result.getRight().isPresent());
        }
    }

    @Test
    public void many() {
        final Tokenizer<Character,Character> a = Tokenizer.character('a');
        final Tokenizer<Character,Character> many = a.many();

        {
            final Source<Character> src = new CharacterSource("");
            final Either<Error<Character>,Pair<List<Character>,Source<Character>>> result = many.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertTrue(result.getRight().get().getLeft().isEmpty());
        }

        {
            final Source<Character> src = new CharacterSource("a");
            final Either<Error<Character>,Pair<List<Character>,Source<Character>>> result = many.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertTrue(result.getRight().get().getLeft().contains('a'));
            assertEquals(1, result.getRight().get().getLeft().size());
        }

        {
            final Source<Character> src = new CharacterSource("aaa");
            final Either<Error<Character>,Pair<List<Character>,Source<Character>>> result = many.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertTrue(result.getRight().get().getLeft().contains('a'));
            assertEquals(3, result.getRight().get().getLeft().size());
        }

        {
            final Source<Character> src = new CharacterSource("aaab");
            final Either<Error<Character>,Pair<List<Character>,Source<Character>>> result = many.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertTrue(result.getRight().get().getLeft().contains('a'));
            assertEquals(3, result.getRight().get().getLeft().size());
            assertTrue(result.getRight().get().getRight().getNext().getLeft().get().equals('b'));
        }
    }

    @Test
    public void atLeast() {
        final Tokenizer<Character,Character> a = Tokenizer.character('a');
        final Tokenizer<Character,Character> many = a.atLeast(3);

        {
            final Source<Character> src = new CharacterSource("");
            final Either<Error<Character>,Pair<List<Character>,Source<Character>>> result = many.tokenize(src);
            assertFalse(result.getRight().isPresent());
        }

        {
            final Source<Character> src = new CharacterSource("aa");
            final Either<Error<Character>,Pair<List<Character>,Source<Character>>> result = many.tokenize(src);
            assertFalse(result.getRight().isPresent());
        }

        {
            final Source<Character> src = new CharacterSource("aaa");
            final Either<Error<Character>,Pair<List<Character>,Source<Character>>> result = many.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertTrue(result.getRight().get().getLeft().contains('a'));
            assertEquals(3, result.getRight().get().getLeft().size());
        }

        {
            final Source<Character> src = new CharacterSource("aaaaab");
            final Either<Error<Character>,Pair<List<Character>,Source<Character>>> result = many.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertTrue(result.getRight().get().getLeft().contains('a'));
            assertEquals(5, result.getRight().get().getLeft().size());
            assertTrue(result.getRight().get().getRight().getNext().getLeft().get().equals('b'));
        }
    }

    @Test
    public void sequence() {
        final Tokenizer<Character,Character> a = Tokenizer.character('a');
        final Tokenizer<Character,Character> b = Tokenizer.character('b');
        final Tokenizer<Character,Character> c = Tokenizer.character('c');
        final Tokenizer<Character,Character> abc = Tokenizer.sequence(asList(a,b,c));

        {
            final Source<Character> src = new CharacterSource("abc");
            final Either<Error<Character>,Pair<List<Character>,Source<Character>>> result = abc.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertEquals(3, result.getRight().get().getLeft().size());
            assertTrue(result.getRight().get().getLeft().get(0).equals('a'));
            assertTrue(result.getRight().get().getLeft().get(1).equals('b'));
            assertTrue(result.getRight().get().getLeft().get(2).equals('c'));
        }

        {
            final Source<Character> src = new CharacterSource("abd");
            final Either<Error<Character>,Pair<List<Character>,Source<Character>>> result = abc.tokenize(src);
            assertFalse(result.getRight().isPresent());
        }
    }

    @Test
    public void oneOf() {
        final Tokenizer<Character,Character> a = Tokenizer.character('a');
        final Tokenizer<Character,Character> b = Tokenizer.character('b');
        final Tokenizer<Character,Character> c = Tokenizer.character('c');
        final Tokenizer<Character,Character> abc = Tokenizer.oneOf(asList(a,b,c));

        {
            final Source<Character> src = new CharacterSource("a");
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> result = abc.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertEquals(1, result.getRight().get().getLeft().size());
            assertTrue(result.getRight().get().getLeft().get(0).equals('a'));
        }

        {
            final Source<Character> src = new CharacterSource("b");
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> result = abc.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertEquals(1, result.getRight().get().getLeft().size());
            assertTrue(result.getRight().get().getLeft().get(0).equals('b'));
        }

        {
            final Source<Character> src = new CharacterSource("c");
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> result = abc.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertEquals(1, result.getRight().get().getLeft().size());
            assertTrue(result.getRight().get().getLeft().get(0).equals('c'));
        }

        {
            final Source<Character> src = new CharacterSource("d");
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> result = abc.tokenize(src);
            assertFalse(result.getRight().isPresent());
        }
    }

    @Test
    public void character() {
        final Tokenizer<Character,Character> a = Tokenizer.character('a');

        {
            final Source<Character> src = new CharacterSource("a");
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> result = a.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertTrue(result.getRight().get().getLeft().get(0).equals('a'));
        }

        {
            final Source<Character> src = new CharacterSource("");
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> result = a.tokenize(src);
            assertFalse(result.getRight().isPresent());
        }
    }

    @Test
    public void letter() {
        final Tokenizer<Character,Character> l = Tokenizer.letter();

        {
            final Source<Character> src = new CharacterSource("a");
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> result = l.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertTrue(result.getRight().get().getLeft().get(0).equals('a'));
        }

        {
            final Source<Character> src = new CharacterSource("Z");
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> result = l.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertTrue(result.getRight().get().getLeft().get(0).equals('Z'));
        }

        {
            final Source<Character> src = new CharacterSource("0");
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> result = l.tokenize(src);
            assertFalse(result.getRight().isPresent());
        }
    }

    @Test
    public void digit() {
        final Tokenizer<Character,Character> d = Tokenizer.digit();

        {
            final Source<Character> src = new CharacterSource("0");
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> result = d.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertTrue(result.getRight().get().getLeft().get(0).equals('0'));
        }

        {
            final Source<Character> src = new CharacterSource("9");
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> result = d.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertTrue(result.getRight().get().getLeft().get(0).equals('9'));
        }

        {
            final Source<Character> src = new CharacterSource("a");
            final Either<Error<Character>, Pair<List<Character>, Source<Character>>> result = d.tokenize(src);
            assertFalse(result.getRight().isPresent());
        }
    }

    @Test
    public void string() {
        final Tokenizer<String,Character> str = Tokenizer.string("test");

        {
            final Source<Character> src = new CharacterSource("test");
            final Either<Error<Character>, Pair<List<String>, Source<Character>>> result = str.tokenize(src);
            assertTrue(result.getRight().isPresent());
            assertTrue(result.getRight().get().getLeft().get(0).equals("test"));
        }

        {
            final Source<Character> src = new CharacterSource("fail");
            final Either<Error<Character>, Pair<List<String>, Source<Character>>> result = str.tokenize(src);
            assertFalse(result.getRight().isPresent());
        }
    }

}