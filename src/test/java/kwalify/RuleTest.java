package kwalify;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import java.util.*;

/**
 *  @revision    $Rev: 4 $
 *  @release     $Release: 0.5.1 $
 *  @copyright   copyright(c) 2005 kuwata-lab all rights reserved.
 */
public class RuleTest extends TestCase {

    private String _input;
    //private String _expected;
    private Class  _exception_class;

    private void doTest() throws Exception {
        YamlParser parser = new YamlParser(_input);
        Map schema = (Map)parser.parse();
        //Rule rule;
        if (_exception_class == null) {
            new Rule(schema);  //rule = new Rule(schema);
        } else {
            try {
                new Rule(schema); // rule = new Rule(schema);
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
        TestRunner.run(RuleTest.class);
    }

    //-----

    /** basic sequence */
    public void testSequence1() throws Exception {
        _input = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: str\n"
            ;
        doTest();
    }

    /** basic mapping */
    public void testMapping1() throws Exception {
        _input = ""
            + "type:       map\n"
            + "mapping:\n"
            + " \"name\":\n"
            + "    type:      str\n"
            + "    required:  yes\n"
            + " \"email\":\n"
            + "    type:      str\n"
            + "    pattern:   /@/\n"
            + " \"age\":\n"
            + "    type:      int\n"
            + " \"birth\":\n"
            + "    type:      date\n"
            ;
        doTest();
    }

    /** mapping of sequence */
    public void testSeqmap1() throws Exception {
        _input = ""
            + "type:      seq\n"
            + "sequence:\n"
            + "  - type:      map\n"
            + "    mapping:\n"
            + "     \"name\":\n"
            + "        type:      str\n"
            + "        required:  true\n"
            + "     \"email\":\n"
            + "        type:      str\n"
            ;
        doTest();
    }

    /** mapping of sequence */
    public void testMapseq1() throws Exception {
        _input = ""
            + "type:      map\n"
            + "mapping:\n"
            + " \"company\":\n"
            + "    type:      str\n"
            + "    required:  yes\n"
            + " \"email\":\n"
            + "    type:      str\n"
            + " \"employees\":\n"
            + "    type:      seq\n"
            + "    sequence:\n"
            + "      - type:    map\n"
            + "        mapping:\n"
            + "         \"code\":\n"
            + "            type:      int\n"
            + "            required:  yes\n"
            + "         \"name\":\n"
            + "            type:      str\n"
            + "            required:  yes\n"
            + "         \"email\":\n"
            + "            type:      str\n"
            ;
        doTest();
    }

    /** unique constraint */
    public void testUnique1() throws Exception {
        _input = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type:     map\n"
            + "    required: yes\n"
            + "    mapping:\n"
            + "     \"name\":\n"
            + "        type:     str\n"
            + "        required: yes\n"
            + "        unique:   yes\n"
            + "     \"email\":\n"
            + "        type:     str\n"
            + "     \"groups\":\n"
            + "        type:     seq\n"
            + "        sequence:\n"
            + "          - type: str\n"
            + "            unique:   yes\n"
            ;
        doTest();
    }

    /** some constraints */
    public void testConstraints1() throws Exception {
        _input = ""
            + "name:      address-book\n"
            + "desc:      Address book\n"
            + "type:      seq\n"
            + "sequence:\n"
            + "  - type:      map\n"
            + "    mapping:\n"
            + "     \"name\":\n"
            + "        type:       str\n"
            + "        required:   yes\n"
            + "     \"email\":\n"
            + "        type:       str\n"
            + "        required:   yes\n"
            + "        pattern:    /@/\n"
            + "     \"password\":\n"
            + "        type:       str\n"
            + "        length:     { max: 16, min: 8 }\n"
            + "     \"age\":\n"
            + "        type:       int\n"
            + "        range:      { max: 30, min: 18 }\n"
            + "        # or assert: 18 <= val && val <= 30\n"
            + "     \"blood\":\n"
            + "        type:       str\n"
            + "        enum:\n"
            + "          - A\n"
            + "          - B\n"
            + "          - O\n"
            + "          - AB\n"
            + "     \"birth\":\n"
            + "        type:       date\n"
            + "     \"memo\":\n"
            + "        type:       any\n"
            ;
        doTest();
    }

    /** default rule */
    public void testDefault1() throws Exception {
        _input = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"properties\":\n"
            + "    type: map\n"
            + "    mapping:\n"
            + "     =:\n"
            + "        type: any\n"
            ;
        doTest();
    }

    /** default rule */
    public void testDefault2() throws Exception {
        _input = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: map\n"
            + "    mapping:\n"
            + "     \"name\":\n"
            + "        type: str\n"
            + "        required: yes\n"
            + "     \"email\":\n"
            + "        type: str\n"
            + "     \"birth\":\n"
            + "        type: date\n"
            + "     =:\n"
            + "        type: any\n"
            ;
        doTest();
    }

    /** merge rule */
    public void testMerge1() throws Exception {
        _input = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"group\":\n"
            + "    type: map\n"
            + "    mapping:\n"
            + "     \"name\": &name\n"
            + "        type: str\n"
            + "        required: yes\n"
            + "     \"email\": &email\n"
            + "        type: str\n"
            + "        pattern: /@/\n"
            + "        required: no\n"
            + " \"user\":\n"
            + "    type: map\n"
            + "    mapping:\n"
            + "     \"name\":\n"
            + "        <<: *name             # merge\n"
            + "        length: { max: 16 }   # append\n"
            + "     \"email\":\n"
            + "        <<: *email            # merge\n"
            + "        required: yes         # overwrite\n"
            ;
        doTest();
    }

    /** sharing rule with anchor and alias */
    public void testAnchor1() throws Exception {
        _input = ""
            + "desc:   Bookshelf\n"
            + "type:   seq\n"
            + "sequence:\n"
            + "  - type:   map\n"
            + "    mapping:\n"
            + "     \"title\":\n"
            + "        type:   str\n"
            + "        required: yes\n"
            + "     \"author\":  &persons       # anchor\n"
            + "        type:   seq\n"
            + "        sequence:\n"
            + "          - type: str\n"
            + "     \"translator\": *persons     # alias\n"
            + "     \"publisher\":\n"
            + "        type:   str\n"
            + "     \"year\":\n"
            + "        type:   int\n"
            ;
        doTest();
    }

    /** recursive definition of rule */
    public void testAnchor2() throws Exception {
        _input = ""
            + "&task                  # anchor\n"
            + "desc:   WBS\n"
            + "type:   map\n"
            + "mapping:\n"
            + " \"name\":      { type: str, required: yes }\n"
            + " \"assigned\":  { type: str }\n"
            + " \"deadline\":  { type: date }\n"
            + " \"subtasks\":\n"
            + "    type:  seq\n"
            + "    sequence:\n"
            + "      - *task          # alias\n"
            ;
        doTest();
    }

    /** schema for meta-validator */
    public void testMeta_schema() throws Exception {
        _input = ""
            + "name:      MAIN\n"
            + "type:      map\n"
            + "required:  yes\n"
            + "mapping:   &main-rule\n"
            + " \"name\":\n"
            + "    type:      str\n"
            + " \"desc\":\n"
            + "    type:      str\n"
            + " \"type\":\n"
            + "    type:      str\n"
            + "    #required:  yes\n"
            + "    enum:\n"
            + "      - seq\n"
            + "      #- sequence\n"
            + "      #- list\n"
            + "      - map\n"
            + "      #- mapping\n"
            + "      #- hash\n"
            + "      - str\n"
            + "      #- string\n"
            + "      - int\n"
            + "      #- integer\n"
            + "      - float\n"
            + "      - number\n"
            + "      #- numeric\n"
            + "      - bool\n"
            + "      #- boolean\n"
            + "      - text\n"
            + "      - date\n"
            + "      - time\n"
            + "      - timestamp\n"
            + "      #- object\n"
            + "      - any\n"
            + "      - scalar\n"
            + "      #- collection\n"
            + " \"required\":\n"
            + "    type:      bool\n"
            + " \"enum\":\n"
            + "    type:      seq\n"
            + "    sequence:\n"
            + "      - type:     scalar\n"
            + "        unique:   yes\n"
            + " \"pattern\":\n"
            + "    type:      str\n"
            + " \"assert\":\n"
            + "    type:      str\n"
            + "    pattern:   /\\bval\\b/\n"
            + " \"range\":\n"
            + "    type:      map\n"
            + "    mapping:\n"
            + "     \"max\":\n"
            + "        type:     scalar\n"
            + "     \"min\":\n"
            + "        type:     scalar\n"
            + "     \"max-ex\":\n"
            + "        type:     scalar\n"
            + "     \"min-ex\":\n"
            + "        type:     scalar\n"
            + " \"length\":\n"
            + "    type:      map\n"
            + "    mapping:\n"
            + "     \"max\":\n"
            + "        type:     int\n"
            + "     \"min\":\n"
            + "        type:     int\n"
            + "     \"max-ex\":\n"
            + "        type:     int\n"
            + "     \"min-ex\":\n"
            + "        type:     int\n"
            + " \"ident\":\n"
            + "    type:      bool\n"
            + " \"unique\":\n"
            + "    type:      bool\n"
            + " \"sequence\":\n"
            + "    name:      SEQUENCE\n"
            + "    type:      seq\n"
            + "    sequence:\n"
            + "      - type:      map\n"
            + "        mapping:   *main-rule\n"
            + "        name:      MAIN\n"
            + "        #required:  yes\n"
            + " \"mapping\":\n"
            + "    name:      MAPPING\n"
            + "    type:      map\n"
            + "    mapping:\n"
            + "      =:\n"
            + "        type:      map\n"
            + "        mapping:   *main-rule\n"
            + "        name:      MAIN\n"
            + "        #required:  yes\n"
            ;
        doTest();
    }

    //-----

}
