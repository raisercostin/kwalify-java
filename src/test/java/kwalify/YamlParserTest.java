package kwalify;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 *  @revision    $Rev: 4 $
 *  @release     $Release: 0.5.1 $
 *  @copyright   copyright(c) 2005 kuwata-lab all rights reserved.
 */
public class YamlParserTest extends TestCase {

    private String _input;
    private String _expected;
    private Class  _exception_class;

    private void doTest() throws Exception {
        YamlParser parser = new YamlParser(_input);
        Object data;
        String actual;
        if (_exception_class == null) {
            data = parser.parse();
            actual = Util.inspect(data);
            assertEquals(_expected, actual);
        } else {
            try {
                data = parser.parse();
                fail(_exception_class.getName() + " is expected but not thrown.");
            } catch (Exception ex) {
                if (ex.getClass() == _exception_class) {
                    // OK
                } else {
                    throw ex;
                }
            }
        }
    }

    public static void main(String[] args) {
        TestRunner.run(YamlParserTest.class);
    }

    //-----

    /** basic sequence */
    public void testSequence1() throws Exception {
        _input = ""
            + "- aaa\n"
            + "- bbb\n"
            + "- ccc\n"
            ;
        _expected = ""
            + "[\"aaa\", \"bbb\", \"ccc\"]"
            ;
        doTest();
    }

    /** nested sequence */
    public void testSequence2() throws Exception {
        _input = ""
            + "- A\n"
            + "-\n"
            + "  - B1\n"
            + "  - B2\n"
            + "  -\n"
            + "    - B2.1\n"
            + "    - B2.2\n"
            + "- C\n"
            ;
        _expected = ""
            + "[\"A\", [\"B1\", \"B2\", [\"B2.1\", \"B2.2\"]], \"C\"]"
            ;
        doTest();
    }

    /** null item of sequence */
    public void testSequence3() throws Exception {
        _input = ""
            + "- A\n"
            + "-\n"
            + "- C\n"
            + "-\n"
            + "-\n"
            + "-\n"
            + "- G\n"
            ;
        _expected = ""
            + "[\"A\", nil, \"C\", nil, nil, nil, \"G\"]"
            ;
        doTest();
    }

    /** null item of nested sequence */
    public void testSequence4() throws Exception {
        _input = ""
            + "-\n"
            + "  -\n"
            + "    -\n"
            + "    -\n"
            + "    -\n"
            + "-\n"
            ;
        _expected = ""
            + "[[[nil, nil, nil]], nil]"
            ;
        doTest();
    }

    /** sequence with empty lines */
    public void testSequence5() throws Exception {
        _input = ""
            + "\n"
            + "- A\n"
            + "\n"
            + "-\n"
            + "\n"
            + "\n"
            + "    - B\n"
            + "\n"
            + "\n"
            + "-\n"
            ;
        _expected = ""
            + "[\"A\", [\"B\"], nil]"
            ;
        doTest();
    }

    /** syntax error - invalid indent of sequence. */
    public void testSequence6() throws Exception {
        _input = ""
            + "- AAA\n"
            + "  - BBB1\n"
            + "  - BBB2\n"
            + "- CCC\n"
            ;
        _expected = ""
            ;
        _exception_class = kwalify.SyntaxException.class;
        doTest();
    }

    /** syntax error - sequence item is exepcted. */
    public void testSequence7() throws Exception {
        _input = ""
            + "- \n"
            + "  - a1\n"
            + "  - a2\n"
            + "  a3\n"
            + "-\n"
            + "  - b1\n"
            + "  - b2\n"
            + "  b3\n"
            ;
        _expected = ""
            ;
        _exception_class = kwalify.SyntaxException.class;
        doTest();
    }

    /** basic mapping */
    public void testMapping1() throws Exception {
        _input = ""
            + "A: foo\n"
            + "B: bar\n"
            + "C  : baz\n"
            ;
        _expected = ""
            + "{\"A\"=>\"foo\", \"B\"=>\"bar\", \"C\"=>\"baz\"}"
            ;
        doTest();
    }

    /** nested mapping */
    public void testMapping2() throws Exception {
        _input = ""
            + "A: 10\n"
            + "B:\n"
            + "  B1:\n"
            + "    B1-1: 21\n"
            + "    B1-2: 22\n"
            + "    B1-3: 23\n"
            + "C: 30\n"
            ;
        _expected = ""
            + "{\"A\"=>10, \"B\"=>{\"B1\"=>{\"B1-1\"=>21, \"B1-2\"=>22, \"B1-3\"=>23}}, \"C\"=>30}"
            ;
        doTest();
    }

    /** null item in mapping */
    public void testMapping3() throws Exception {
        _input = ""
            + "A:\n"
            + "B:\n"
            + "  B1:\n"
            + "    B1-2:\n"
            + "C:\n"
            ;
        _expected = ""
            + "{\"A\"=>nil, \"B\"=>{\"B1\"=>{\"B1-2\"=>nil}}, \"C\"=>nil}"
            ;
        doTest();
    }

    /** mapping with empty lines */
    public void testMapping4() throws Exception {
        _input = ""
            + "\n"
            + "A: 1\n"
            + "\n"
            + "B: \n"
            + "\n"
            + "\n"
            + "  B1:\n"
            + "\n"
            + "\n"
            + "\n"
            + "    B1a: 2\n"
            + "C: 3\n"
            ;
        _expected = ""
            + "{\"A\"=>1, \"B\"=>{\"B1\"=>{\"B1a\"=>2}}, \"C\"=>3}"
            ;
        doTest();
    }

    /** parse error - invalid indent of mapping. */
    public void testMapping5() throws Exception {
        _input = ""
            + "A: 10\n"
            + "B: 20\n"
            + "  B1: 21\n"
            + "  B2: 22\n"
            + "C: 30\n"
            ;
        _expected = ""
            ;
        _exception_class = kwalify.SyntaxException.class;
        doTest();
    }

    /** parse error - mapping item is expected. */
    public void testMapping6() throws Exception {
        _input = ""
            + "A:\n"
            + "  a1: 1\n"
            + "  a2: 2\n"
            + "  a3  3\n"
            + "B:\n"
            + "  b1: 1\n"
            + "  b2: 2\n"
            + "  b3  3\n"
            ;
        _expected = ""
            ;
        _exception_class = kwalify.SyntaxException.class;
        doTest();
    }

    /** seq of mapping */
    public void testCombination1() throws Exception {
        _input = ""
            + "-\n"
            + "  x: 10\n"
            + "  y: 20\n"
            + "-\n"
            + "  x: 15\n"
            + "  y: 25\n"
            ;
        _expected = ""
            + "[{\"x\"=>10, \"y\"=>20}, {\"x\"=>15, \"y\"=>25}]"
            ;
        doTest();
    }

    /** seq of mapping (in same line) */
    public void testCombination2() throws Exception {
        _input = ""
            + "- x: 10\n"
            + "  y: 20\n"
            + "- x: 15\n"
            + "  y: 25\n"
            ;
        _expected = ""
            + "[{\"x\"=>10, \"y\"=>20}, {\"x\"=>15, \"y\"=>25}]"
            ;
        doTest();
    }

    /** seq of seq of seq */
    public void testCombination3() throws Exception {
        _input = ""
            + "- - - a\n"
            + "    - b\n"
            + "- - - c\n"
            + "    - d\n"
            ;
        _expected = ""
            + "[[[\"a\", \"b\"]], [[\"c\", \"d\"]]]"
            ;
        doTest();
    }

    /** map of sequence */
    public void testCombination4() throws Exception {
        _input = ""
            + "A:\n"
            + "  - 1\n"
            + "  - 2\n"
            + "  - 3\n"
            + "B:\n"
            + "  - 4\n"
            + "  - 5\n"
            + "  - 6\n"
            ;
        _expected = ""
            + "{\"A\"=>[1, 2, 3], \"B\"=>[4, 5, 6]}"
            ;
        doTest();
    }

    /** map of sequence (in same line) */
    public void testCombination5() throws Exception {
        _input = ""
            + "A: - 1\n"
            + "   - 2\n"
            + "   - 3\n"
            + "B: - 4\n"
            + "   - 5\n"
            + "   - 6\n"
            ;
        _expected = ""
            + "{\"A\"=>[1, 2, 3], \"B\"=>[4, 5, 6]}"
            ;
        doTest();
    }

    /** map of map of map */
    public void testCombination6() throws Exception {
        _input = ""
            + "A: a: 1: 100\n"
            + "      2: 200\n"
            + "B: b: 3: 300\n"
            + "      4: 400\n"
            ;
        _expected = ""
            + "{\"A\"=>{\"a\"=>{1=>100, 2=>200}}, \"B\"=>{\"b\"=>{3=>300, 4=>400}}}"
            ;
        doTest();
    }

    /** line comment */
    public void testComment1() throws Exception {
        _input = ""
            + "# comment\n"
            + "- A\n"
            + "- B\n"
            + "  # comment\n"
            + "-\n"
            + "    # comment\n"
            + "  - C\n"
            ;
        _expected = ""
            + "[\"A\", \"B\", [\"C\"]]"
            ;
        doTest();
    }

    /** escape line comment */
    public void testComment2() throws Exception {
        _input = ""
            + "# comment\n"
            + "- A\n"
            + "- B:\n"
            + "   \"# comment\"\n"
            + "-\n"
            + "  '# comment'\n"
            ;
        _expected = ""
            + "[\"A\", {\"B\"=>\"# comment\"}, \"# comment\"]"
            ;
        doTest();
    }

    /** line comment with seq and map */
    public void testComment3() throws Exception {
        _input = ""
            + "- A             # comment\n"
            + "- B:            # comment\n"
            + "    C: foo      # comment\n"
            + "    D: \"bar#bar\"    #comment\n"
            ;
        _expected = ""
            + "[\"A\", {\"B\"=>{\"C\"=>\"foo\", \"D\"=>\"bar#bar\"}}]"
            ;
        doTest();
    }

    /** line comment with anchor and alias */
    public void testComment4() throws Exception {
        _input = ""
            + "- &a1           # comment\n"
            + "  foo\n"
            + "- *a1           # comment\n"
            ;
        _expected = ""
            + "[\"foo\", \"foo\"]"
            ;
        doTest();
    }

    /** flow style sequence */
    public void testFlowseq1() throws Exception {
        _input = ""
            + "- [ 10, 20 ]\n"
            + "- [15,25,35]\n"
            ;
        _expected = ""
            + "[[10, 20], [15, 25, 35]]"
            ;
        doTest();
    }

    /** nested flow style sequence */
    public void testFlowseq2() throws Exception {
        _input = ""
            + "1: [ A, [B1, B2]]\n"
            + "2: [[[X]]]\n"
            + "3: [[1,1],[2,\"2\"],['3',3]]\n"
            ;
        _expected = ""
            + "{1=>[\"A\", [\"B1\", \"B2\"]], 2=>[[[\"X\"]]], 3=>[[1, 1], [2, \"2\"], [\"3\", 3]]}"
            ;
        doTest();
    }

    /** flow style sequence with some lines */
    public void testFlowseq3() throws Exception {
        _input = ""
            + "A: [ [10,20],\n"
            + "     [11,21],\n"
            + "     [12,22]]\n"
            + "B: [\n"
            + "  [1.1,\n"
            + "   1.2,\n"
            + "   1.3\n"
            + "  ]\n"
            + "]\n"
            ;
        _expected = ""
            + "{\"A\"=>[[10, 20], [11, 21], [12, 22]], \"B\"=>[[1.1, 1.2, 1.3]]}"
            ;
        doTest();
    }

    /** invalid flow style seq (sequence item required (or last comma is extra).) */
    public void testFlowseq4() throws Exception {
        _input = ""
            + "A: [ [10,20], ]\n"
            ;
        _expected = ""
            ;
        _exception_class = kwalify.SyntaxException.class;
        doTest();
    }

    /** invalid flow style seq (flow style sequence requires ']'). */
    public void testFlowseq5() throws Exception {
        _input = ""
            + "A: [ [10,20]\n"
            + "B: [ [30, 40]]\n"
            ;
        _expected = ""
            ;
        _exception_class = kwalify.SyntaxException.class;
        doTest();
    }

    /** invalid flow style seq (flow style sequence requires ']'). */
    public void testFlowseq6() throws Exception {
        _input = ""
            + "[ 10 ]]\n"
            ;
        _expected = ""
            ;
        _exception_class = kwalify.SyntaxException.class;
        doTest();
    }

    /** flow style map */
    public void testFlowmap1() throws Exception {
        _input = ""
            + "- { A1: 10, A2: 20 }\n"
            + "- {B1: 15, 'B2': 25, \"B3\": 35}\n"
            ;
        _expected = ""
            + "[{\"A1\"=>10, \"A2\"=>20}, {\"B1\"=>15, \"B2\"=>25, \"B3\"=>35}]"
            ;
        doTest();
    }

    /** flow style map nested */
    public void testFlowmap2() throws Exception {
        _input = ""
            + "A: { x: {y: {z: 10}}}\n"
            + "B: { a: 1, b:{c: 2, d: 3, e:{f: 4}}, g: 5}\n"
            ;
        _expected = ""
            + "{\"A\"=>{\"x\"=>{\"y\"=>{\"z\"=>10}}}, \"B\"=>{\"a\"=>1, \"b\"=>{\"c\"=>2, \"d\"=>3, \"e\"=>{\"f\"=>4}}, \"g\"=>5}}"
            ;
        doTest();
    }

    /** flow style map with some lines */
    public void testFlowmap3() throws Exception {
        _input = ""
            + "A: { x:\n"
            + "     {y:\n"
            + "       {z: 10}\n"
            + "     }\n"
            + "   }\n"
            + "B: {\n"
            + "  a: 1,\n"
            + "  b: {\n"
            + "    c: 2,\n"
            + "    d: 3,\n"
            + "    e: {\n"
            + "      f: 4\n"
            + "    }\n"
            + "  },\n"
            + "  g: 5\n"
            + "}\n"
            ;
        _expected = ""
            + "{\"A\"=>{\"x\"=>{\"y\"=>{\"z\"=>10}}}, \"B\"=>{\"a\"=>1, \"b\"=>{\"c\"=>2, \"d\"=>3, \"e\"=>{\"f\"=>4}}, \"g\"=>5}}"
            ;
        doTest();
    }

    /** invalid flow style map (mapping item required (or last comma is extra).) */
    public void testFlowmap4() throws Exception {
        _input = ""
            + "- {A: 10, B: 20, }\n"
            ;
        _expected = ""
            ;
        _exception_class = kwalify.SyntaxException.class;
        doTest();
    }

    /** invalid flow style map (flow style mapping requires '}'). */
    public void testFlowmap5() throws Exception {
        _input = ""
            + "- {A: { x: 10, y: 20 }\n"
            + "- {A: { x: 11, y: 21 }}\n"
            ;
        _expected = ""
            ;
        _exception_class = kwalify.SyntaxException.class;
        doTest();
    }

    /** invalid flow style map (flow style mapping requires ']'). */
    public void testFlowmap6() throws Exception {
        _input = ""
            + "{ x: 10 }}\n"
            ;
        _expected = ""
            ;
        _exception_class = kwalify.SyntaxException.class;
        doTest();
    }

    /** combination of flow style seq and map */
    public void testFlowcombination1() throws Exception {
        _input = ""
            + "[\n"
            + "  {name: '  foo  ',\n"
            + "   e-mail: foo@mail.com},\n"
            + "  {name: ba   z,\n"
            + "   e-mail: ba__z@mail.com   }\n"
            + "]\n"
            ;
        _expected = ""
            + "[{\"e-mail\"=>\"foo@mail.com\", \"name\"=>\"  foo  \"}, {\"e-mail\"=>\"ba__z@mail.com\", \"name\"=>\"ba   z\"}]"
            ;
        doTest();
    }

    /** parse_blocktext */
    public void testBlocktext01() throws Exception {
        _input = ""
            + "- text1: |\n"
            + "   foo\n"
            + "   bar\n"
            + "   baz\n"
            + "- text2: |\n"
            + "      aaa\n"
            + "       bbb\n"
            + "        ccc\n"
            + "- |\n"
            + " foo\n"
            + " bar\n"
            + " baz\n"
            + "- |\n"
            + "      aaa\n"
            + "       bbb\n"
            + "        ccc\n"
            ;
        _expected = ""
            + "[{\"text1\"=>\"foo\\nbar\\nbaz\\n\"}, {\"text2\"=>\"aaa\\n bbb\\n  ccc\\n\"}, \"foo\\nbar\\nbaz\\n\", \"aaa\\n bbb\\n  ccc\\n\"]"
            ;
        doTest();
    }

    /** block text with '|+' or '|-' */
    public void testBlocktext02() throws Exception {
        _input = ""
            + "- text1: |\n"
            + "    A\n"
            + "\n"
            + "    B\n"
            + "    C\n"
            + "\n"
            + "\n"
            + "- text2: |+\n"
            + "    A\n"
            + "\n"
            + "    B\n"
            + "    C\n"
            + "\n"
            + "\n"
            + "- text3: |-\n"
            + "    A\n"
            + "\n"
            + "    B\n"
            + "    C\n"
            ;
        _expected = ""
            + "[{\"text1\"=>\"A\\n\\nB\\nC\\n\"}, {\"text2\"=>\"A\\n\\nB\\nC\\n\\n\\n\"}, {\"text3\"=>\"A\\n\\nB\\nC\"}]"
            ;
        doTest();
    }

    /** block text with '|n' */
    public void testBlocktext03() throws Exception {
        _input = ""
            + "- text1: |2\n"
            + "    A\n"
            + "\n"
            + "   B\n"
            + "    C\n"
            + "\n"
            + "\n"
            + "- text2: |+2\n"
            + "    A\n"
            + "\n"
            + "   B\n"
            + "    C\n"
            + "\n"
            + "\n"
            + "- text3: |-2\n"
            + "    A\n"
            + "\n"
            + "   B\n"
            + "    C\n"
            ;
        _expected = ""
            + "[{\"text1\"=>\"  A\\n\\n B\\n  C\\n\"}, {\"text2\"=>\"  A\\n\\n B\\n  C\\n\\n\\n\"}, {\"text3\"=>\"  A\\n\\n B\\n  C\"}]"
            ;
        doTest();
    }

    /** block text with '| foo' */
    public void testBlocktext04() throws Exception {
        _input = ""
            + "- text1: | foo  \n"
            + "   A\n"
            + "\n"
            + "   B\n"
            + "   C\n"
            + "\n"
            + "- |  foo\n"
            + "  A\n"
            + "   B\n"
            + "    C\n"
            ;
        _expected = ""
            + "[{\"text1\"=>\"foo  A\\n\\nB\\nC\\n\"}, \"fooA\\n B\\n  C\\n\"]"
            ;
        doTest();
    }

    /** block text with '#' (comment) */
    public void testBlocktext05() throws Exception {
        _input = ""
            + "#\n"
            + "  - text1: |\n"
            + "     A\n"
            + "     #\n"
            + "     B\n"
            + "    #\n"
            + "    text2: |\n"
            + "     #\n"
            + "     #\n"
            + "    #\n"
            + "  - |\n"
            + "   A\n"
            + "   #\n"
            + "   B\n"
            + "  #\n"
            + "  - |\n"
            + "    #\n"
            + "    #\n"
            + "#\n"
            + "  - x\n"
            ;
        _expected = ""
            + "[{\"text1\"=>\"A\\n#\\nB\\n\", \"text2\"=>\"#\\n#\\n\"}, \"A\\n#\\nB\\n\", \"#\\n#\\n\", \"x\"]"
            ;
        doTest();
    }

    /** invalid block text */
    public void testBlocktext06() throws Exception {
        _input = ""
            + "- |\n"
            + "   a\n"
            + "  b\n"
            + "  c\n"
            ;
        _expected = ""
            ;
        _exception_class = kwalify.SyntaxException.class;
        doTest();
    }

    /** parse_blocktext (>) */
    public void testBlocktext11() throws Exception {
        _input = ""
            + "- text1: >\n"
            + "   foo\n"
            + "   bar\n"
            + "   baz\n"
            + "- text2: >\n"
            + "      aaa\n"
            + "       bbb\n"
            + "        ccc\n"
            + "- >\n"
            + " foo\n"
            + " bar\n"
            + " baz\n"
            + "- >\n"
            + "      aaa\n"
            + "       bbb\n"
            + "        ccc\n"
            ;
        _expected = ""
            + "[{\"text1\"=>\"foo bar baz\\n\"}, {\"text2\"=>\"aaa  bbb   ccc\\n\"}, \"foo bar baz\\n\", \"aaa  bbb   ccc\\n\"]"
            ;
        doTest();
    }

    /** block text with '>+' or '>-' */
    public void testBlocktext12() throws Exception {
        _input = ""
            + "- text1: >\n"
            + "    A\n"
            + "\n"
            + "    B\n"
            + "    C\n"
            + "\n"
            + "\n"
            + "- text2: >+\n"
            + "    A\n"
            + "\n"
            + "    B\n"
            + "    C\n"
            + "\n"
            + "\n"
            + "- text3: >-\n"
            + "    A\n"
            + "\n"
            + "    B\n"
            + "    C\n"
            ;
        _expected = ""
            + "[{\"text1\"=>\"A\\nB C\\n\"}, {\"text2\"=>\"A\\nB C\\n\\n\\n\"}, {\"text3\"=>\"A\\nB C\"}]"
            ;
        doTest();
    }

    /** block text with '>n' */
    public void testBlocktext13() throws Exception {
        _input = ""
            + "- >2\n"
            + "    A\n"
            + "\n"
            + "   B\n"
            + "   C\n"
            + "\n"
            + "\n"
            + "- >+2\n"
            + "    A\n"
            + "\n"
            + "   B\n"
            + "   C\n"
            + "\n"
            + "\n"
            + "- >-2\n"
            + "    A\n"
            + "\n"
            + "   B\n"
            + "   C\n"
            ;
        _expected = ""
            + "[\"  A\\n B  C\\n\", \"  A\\n B  C\\n\\n\\n\", \"  A\\n B  C\"]"
            ;
        doTest();
    }

    /** block text with '> foo' */
    public void testBlocktext14() throws Exception {
        _input = ""
            + "- text1: > foo  \n"
            + "   A\n"
            + "\n"
            + "   B\n"
            + "   C\n"
            + "\n"
            + "- >  foo\n"
            + "  A\n"
            + "   B\n"
            + "    C\n"
            ;
        _expected = ""
            + "[{\"text1\"=>\"foo  A\\nB C\\n\"}, \"fooA  B   C\\n\"]"
            ;
        doTest();
    }

    /** block text with '#' (comment) */
    public void testBlocktext15() throws Exception {
        _input = ""
            + "#\n"
            + "  - text1: >\n"
            + "     AA\n"
            + "     ##\n"
            + "     BB\n"
            + "    #\n"
            + "    text2: >\n"
            + "     #\n"
            + "     #\n"
            + "    #\n"
            + "  - >\n"
            + "   AA\n"
            + "   ##\n"
            + "   BB\n"
            + "  #\n"
            + "  - >\n"
            + "    #\n"
            + "    #\n"
            + "#\n"
            + "  - x\n"
            ;
        _expected = ""
            + "[{\"text1\"=>\"AA ## BB\\n\", \"text2\"=>\"# #\\n\"}, \"AA ## BB\\n\", \"# #\\n\", \"x\"]"
            ;
        doTest();
    }

    /** invalid block text */
    public void testBlocktext16() throws Exception {
        _input = ""
            + "- >\n"
            + "   a\n"
            + "  b\n"
            + "  c\n"
            ;
        _expected = ""
            ;
        _exception_class = kwalify.SyntaxException.class;
        doTest();
    }

    /** parse_anchor, parse_alias */
    public void testAnchor1() throws Exception {
        _input = ""
            + "- &a1 foo\n"
            + "- &a2\n"
            + " bar\n"
            + "- *a1\n"
            + "- *a2\n"
            ;
        _expected = ""
            + "[\"foo\", \"bar\", \"foo\", \"bar\"]"
            ;
        doTest();
    }

    /** parse_anchor, parse_alias */
    public void testAnchor2() throws Exception {
        _input = ""
            + "- A: &a1\n"
            + "   x: 10\n"
            + "   y: 20\n"
            + "- B: bar\n"
            + "- C: *a1\n"
            ;
        _expected = ""
            + "[{\"A\"=>{\"x\"=>10, \"y\"=>20}}, {\"B\"=>\"bar\"}, {\"C\"=>{\"x\"=>10, \"y\"=>20}}]"
            ;
        doTest();
    }

    /** anchor on child node */
    public void testAnchor3() throws Exception {
        _input = ""
            + "- A: &a1\n"
            + "   x: 10\n"
            + "   y: 20\n"
            + "   z: *a1\n"
            ;
        _expected = ""
            + "[{\"A\"=>{\"x\"=>10, \"y\"=>20, \"z\"=>{...}}}]"
            ;
        doTest();
    }

    /** backward anchor */
    public void testAnchor4() throws Exception {
        _input = ""
            + "- *a1\n"
            + "- *a1\n"
            + "- foo\n"
            + "- &a1 bar\n"
            ;
        _expected = ""
            + "[\"bar\", \"bar\", \"foo\", \"bar\"]"
            ;
        doTest();
    }

    /** anchor not found */
    public void testAnchor5() throws Exception {
        _input = ""
            + "- &a1 foo\n"
            + "- bar\n"
            + "- *a2\n"
            ;
        _expected = ""
            ;
        _exception_class = kwalify.SyntaxException.class;
        doTest();
    }

    /** anchor on child node */
    public void testAnchor6() throws Exception {
        _input = ""
            + "type:        seq\n"
            + "sequence:\n"
            + "  - &employee\n"
            + "    type:    map\n"
            + "    mapping:\n"
            + "      name:\n"
            + "        type:   str\n"
            + "      post:\n"
            + "        type:   str\n"
            + "        enum:\n"
            + "          - exective\n"
            + "          - manager\n"
            + "          - clerk\n"
            + "      supervisor: *employee\n"
            ;
        _expected = ""
            + "{\"sequence\"=>[{\"mapping\"=>{\"name\"=>{\"type\"=>\"str\"}, \"post\"=>{\"enum\"=>[\"exective\", \"manager\", \"clerk\"], \"type\"=>\"str\"}, \"supervisor\"=>{...}}, \"type\"=>\"map\"}], \"type\"=>\"seq\"}"
            ;
        doTest();
    }

    /** tag */
    public void testTag1() throws Exception {
        _input = ""
            + "- !str 123\n"
            + "- foo: !text 123\n"
            ;
        _expected = ""
            + "[123, {\"foo\"=>123}]"
            ;
        doTest();
    }

    /** ... (document end) */
    public void testDocend1() throws Exception {
        _input = ""
            + "- aaa\n"
            + "- bbb\n"
            + "...\n"
            + "- ccc\n"
            ;
        _expected = ""
            + "[\"aaa\", \"bbb\"]"
            ;
        doTest();
    }

    /** ... (document end) in block text */
    public void testDocend2() throws Exception {
        _input = ""
            + "- |\n"
            + "  foo\n"
            + "  ...\n"
            + "  bar\n"
            ;
        _expected = ""
            + "[\"foo\\n...\\nbar\\n\"]"
            ;
        doTest();
    }

    /** --- (document start) */
    public void testDocstart1() throws Exception {
        _input = ""
            + "# comment\n"
            + "---\n"
            + "- foo\n"
            + "- bar\n"
            ;
        _expected = ""
            + "[\"foo\", \"bar\"]"
            ;
        doTest();
    }

    /** --- (document start) with tag */
    public void testDocstart2() throws Exception {
        _input = ""
            + "# comment\n"
            + "--- %YAML !seq\n"
            + "- foo\n"
            + "- bar\n"
            ;
        _expected = ""
            + "[\"foo\", \"bar\"]"
            ;
        doTest();
    }

    /** --- (document start) with tag */
    public void testDocstart3() throws Exception {
        _input = ""
            + "- |\n"
            + "  foo\n"
            + "  ---\n"
            + "  bar\n"
            + "  ---\n"
            + "  baz\n"
            ;
        _expected = ""
            + "[\"foo\\n---\\nbar\\n---\\nbaz\\n\"]"
            ;
        doTest();
    }

    /** map default value */
    public void testDefault1() throws Exception {
        _input = ""
            + "- A: 10\n"
            + "  B: 20\n"
            + "  =: -1\n"
            + "- K:\n"
            + "    x: 10\n"
            + "    y: 20\n"
            + "  =:\n"
            + "    x: 0\n"
            + "    y: 0\n"
            ;
        _expected = ""
            + "[{\"A\"=>10, \"B\"=>20}, {\"K\"=>{\"x\"=>10, \"y\"=>20}}]"
            ;
        doTest();
    }

    /** merge key '<<' */
    public void testMerge1() throws Exception {
        _input = ""
            + "- &a1\n"
            + "  A: 10\n"
            + "  B: 20\n"
            + "- C: 30\n"
            + "  <<: *a1\n"
            + "  A: ~\n"
            ;
        _expected = ""
            + "[{\"A\"=>10, \"B\"=>20}, {\"A\"=>nil, \"B\"=>20, \"C\"=>30}]"
            ;
        doTest();
    }

    /** scalar with sequence */
    public void testScalar1() throws Exception {
        _input = ""
            + "- abc\n"
            + "- 123\n"
            + "- 3.14\n"
            + "- true\n"
            + "- false\n"
            + "- yes\n"
            + "- no\n"
            + "- ~\n"
            + "- null\n"
            + "- \"123\"\n"
            + "- '456'\n"
            + "- 2005-01-01\n"
            + "- :sym\n"
            ;
        _expected = ""
            + "[\"abc\", 123, 3.14, true, false, true, false, nil, nil, \"123\", \"456\", Tue Feb 01 00:00:00 JST 2005, \":sym\"]"
            ;
        doTest();
    }

    /** mapping of scalar */
    public void testScalar2() throws Exception {
        _input = ""
            + "- abc : ABC\n"
            + "- 123 : 123\n"
            + "- 3.14 : 3.14\n"
            + "- true : true\n"
            + "- false : false\n"
            + "- yes : yes\n"
            + "- no : no\n"
            + "- ~ : ~\n"
            + "- null : null\n"
            + "- \"123\" : \"123\"\n"
            + "- '456' : '456'\n"
            + "- 2005-01-01 : 2005-01-01\n"
            + "- :sym : :sym\n"
            ;
        _expected = ""
            + "[{\"abc\"=>\"ABC\"}, {123=>123}, {3.14=>3.14}, {true=>true}, {false=>false}, {true=>true}, {false=>false}, \"~ : ~\", {nil=>nil}, {\"123\"=>\"123\"}, {\"456\"=>\"456\"}, {Tue Feb 01 00:00:00 JST 2005=>Tue Feb 01 00:00:00 JST 2005}, {\":sym\"=>\":sym\"}]"
            ;
        doTest();
    }

    //-----

}
