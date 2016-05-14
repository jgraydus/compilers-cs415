Notes for my future self so I can remember how dumb I was.

## Description ##

This is a project for a class on compilers using the book Compiler
Construction Principles and Practice by Kenneth C. Louden. The code
includes compilers for two languages described in the book: the
extremely simple language Tiny and the more complicated C- language.
Neither of these languages are of any practical use.

This project is implemented entirely from scratch in Java 8 with the
exception of a dependency on JUnit. (Also, the tiny VM is implemented
in Haskell, but I consider that a separate project.)

## Scanner, aka Tokenizer, aka Lexer ##

The lexer for both languages was implemented using the facilities
provided by `Tokenizer`. The design of Tokenizer was influenced greatly
by parser combinator libraries such as Haskell's parsec. It presents
an interface for a tokenizer and some default implementation for
primitives such as recognizing strings. It also provides functions
that can combine tokenizers in different ways to build more
complicated tokenizers. Here are some examples from the `TinyScanner`
class.

The keyword `read` is recognized when the string "read" is recognized
*and* then the next character is not a letter. This tokenizer also
transforms the recognized result into an instance of the `Read` class
which represents that token type.

    Tokenizer<Token,Character> read = string("read").and(letter().not().peek()).convert(Read::new);

A comment starts when a "{" character is recognized and stops when a "}"
character is recognized. Tiny does not allow nested comments.

    Tokenizer<Token,Character> comment = fromTo(character('{'), character('}')).convert(Comment::new);

The keyword tokenizer recognizes the input if any of the tokenizers
for the individual keywords recognizes the input.

    Tokenizer<Token,Character> keyword = oneOf(read, write, repeat, until, ifT, then, elseT, end);

## Parser ##

The Tiny language compiler uses an LL1 parser generator. First, each
terminal and non-terminal symbol of the language is defined. For
example, a statement is represented by this symbol object in the
parser:

    Symbol statement = new Symbol.NonTerminal("statement");
    
And an identifier is:

    Symbol identifier = new Symbol.Terminal("identifier");
    
After all the symbols are defined, then the productions of the grammar
are specified. For example, the grammar production

    statement -> if-stmt | read-stmt | write-stmt | assign-stmt | repeat-stmt
    
becomes the following objects:

    new Production(statement, singletonList(ifStmt));
    new Production(statement, singletonList(readStmt));
    new Production(statement, singletonList(writeStmt));
    new Production(statement, singletonList(assignStmt));
    new Production(statement, singletonList(repeatStmt));
 
The productions are put into a list and passed into the constructor
of a `Grammar` object. The `Grammar` object and a function that
associates token types to grammar symbols are passed into the
constructor of the `LL1Parser`. The parser accepts a list of tokens
and returns either an abstract syntax tree or an error.
 
Unfortunately, LL1 parser have some serious drawbacks. The most
annoying of which is probably the fact that left recursive grammars
are not possible. This requires both a clumsy rewrite of parts of the
grammar and also extra work to ensure that the proper associativity of
operations is preserved. Thus, I decided to implement an LR1 parser
generator for the C- compiler. Everything described here regarding the
way the grammar is defined is the same, however C- uses the
`LR1Parser` class.

## Static Analysis ##

Both the Tiny compiler and the C- compiler have a type checker. In
both cases, its rather simple as there are no user defined types. The
process is essentially a tree traversal on the abstract syntax tree.
C- adds a few extra details due to functions and array types. The
types of function arguments and the return value of a function need to
be known, so we need to walk the tree once to gather that information.
We also need to ensure that a function always returns a value of its
type (if it returns a value at all).

## Code Generation ##

I think the simplest code generator is the LLVM backend for the Tiny
language, as it defers much of the work such as register allocation to
clang. I am familiar with x86 assembly, but had no experience with
LLVM. I tried to read a bit of the documentation on LLVM IR first, but
I quickly got confused, so instead I just wrote some simple C programs
and told clang to spit out IR. After filtering out all the noise
(attributes that aren't essential), it became much easier to
understand. Then I hacked together some code that traverses a Tiny
abstract syntax tree and spits out the appropriate LLVM IR. This is
not particularly difficult.

I also implemented a backend for the Tiny compiler to emit code for
the Tiny Machine (well, my own variation of the Tiny Machine). This
was more difficult, since it required figuring out how to use
registers. The Tiny Machine provides eight registers, labeled 0
through 7. Register 7 is the program counter, but no use is specified
for the other registers. I decided on a convention where register 6 is
my stack pointer and registers 0 and 1 are used for computations.
Furthermore, register 0 is always the location of the result of a
computation. So an expression such as

    1 + 2 + 3
    
would create instructions like

    load the constant 1 into register 0
    push register 0
    load the constant 2 into register 0
    pop into register 1
    add registers 0 and 1
    push register 0
    load the constant 3 into register 0
    pop into register 1
    add registers 0 and 1
    
This isn't terribly efficient, but it is easy to implement.

Control structures were a bit challenging. In order to emit code for
an if statement, for example, requires emitting a jump instruction to 
the point after the body of the the if (in the case that the condition
is false), but you don't know where that address is until after you
produce code for the body.

The C- compiler added a whole new dimension of challenge. Implementing
function calls requires a convention for passing arguments, handling
local variables, and returning values. The scheme I came up with is as
follows. I reserved register 5 to be a frame pointer. This is a
pointer into the stack which is a base pointer for the currently
executing function. When a function is invoked, first the return
address is pushed onto the stack. The arguments to the function
are evaluated from right to left and pushed onto the stack. Then the
current frame pointer is pushed onto the stack. At this point, the
frame pointer is changed to point at the value we just pushed onto the
stack (the old frame pointer). Then the size of all local variables is
added to the stack pointer. So the stack looks something like this:

    |        | <- SP
    |    x   |
    |    y   |
    |    z   |
    | old fp | <- FP
    |  arg1  |
    |  arg2  |
    |  arg3  |
    |ret.add.|
    
Here x, y, and z represent local variables while arg1, arg2, and arg3
are arguments to the function. With this scheme, addressing is very
simple. We specify the addresses of local variables with respect to
the FP while the address of arg i is just FP - i. It is quite possible
to have local arrays. This doesn't really complicate anything, though.
We just stack allocate them. This is not a particularly good idea for
large arrays, but there is no heap allocation in C-, so this is the
only choice for local arrays. Arrays as parameters can't work this
way, unfortunately, since that would require knowing the size of the
array statically. Instead, we follow the convention that when an array
is passed as an argument, we don't actually pass the array but instead
the address of the first element.

Returning from a function is not difficult. We keep the Tiny
convention of always putting the result of any operation in register
0, so that nothing special needs to be done for return values. We only
need to restore the frame pointer and stack pointer to their previous
values. This is simple in both cases as the stack pointer can be
changed to the value in the frame pointer minus the number of
arguments, and the frame pointer can be updated back to the value of
the old frame pointer which it is currently pointing at. Not the stack
pointer is pointing at the return address. We pop this into the
program counter and we're done.

## Optimization ##

This is the point at which I realized that going directly from
abstract syntax tree to machine instructions is a **bad idea**. There
are some optimizations that can be done on the ast itself such as
constant folding, but I didn't bother with most of them. I did
implement a form of dead code removal which recognizes and removes
stuff like

    if (false) {
        do stuff here
    }

Most of the optimizations I wanted to do were at a lower level, but I
realized that it would be impossible or beyond my limited capability
to do after I've hard coded my addresses into machine instructions. I
should have used an intermediate representation like LLVM does in
order to facilitate these optimizations. In fact, I've started
rewriting the C- compiler in Haskell in order to do exactly this.
