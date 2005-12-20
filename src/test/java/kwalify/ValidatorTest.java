package kwalify;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import java.util.*;
import java.text.*;

/**
 *  @revision    $Rev: 4 $
 *  @release     $Release: 0.5.1 $
 *  @copyright   copyright(c) 2005 kuwata-lab all rights reserved.
 */
public class ValidatorTest extends TestCase {

    private String _schema;
    private String _valid;
    private String _invalid;
    private String _error;

    private void doTest() throws Exception {
        YamlParser parser = new YamlParser(_schema);
        Map schema = (Map)parser.parse();
        Validator validator = new Validator(schema);
        String actual;
        if (_valid != null) {
            actual = doValidate(validator, _valid);
            assertEquals("", actual);
        }
        if (_invalid != null) {
            actual = doValidate(validator, _invalid);
            assertEquals(_error, actual);
        }
    }

    private String doValidate(Validator validator, String input) throws Exception {
        YamlParser parser = new YamlParser(input);
        Object doc = parser.parse();
        List errors = validator.validate(doc);
        StringBuffer sb = new StringBuffer();
        MessageFormat format = new MessageFormat("{0}: (line {1})[{2}] {3}\n");
        if (errors != null) {
            parser.setErrorsLineNumber(errors);
            Collections.sort(errors);
            for (Iterator it = errors.iterator(); it.hasNext(); ) {
                ValidationException err = (ValidationException)it.next();
                String symbol = convertSymbol(err.getErrorSymbol());
                Object[] params = { symbol, new Integer(err.getLineNumber()), err.getPath(), err.getMessage() };
                String s = format.format(params);
                sb.append(s);
            }
        }
        return sb.toString();
    }

    private String convertSymbol(String error_symbol) {
        final int symbol_width = 20;
        StringBuffer sb = new StringBuffer();
        sb.append(":");
        String[] array = error_symbol.split("\\.");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append('_');
            }
            sb.append(array[i]);
        }
        for (int i = symbol_width - sb.length(); i > 0; i--) {
            sb.append(' ');
        }
        return sb.toString();
    }


    public static void main(String[] args) {
        TestRunner.run(ValidatorTest.class);
    }

    //-----

    /** sequence test */
    public void testSequence1() throws Exception {
        _schema = ""
            + "type:        seq\n"
            + "required:    true\n"
            + "sequence:\n"
            + "  - type:       str\n"
            + "    required:   true\n"
            ;
        _valid = ""
            + "- foo\n"
            + "- bar\n"
            + "- baz\n"
            ;
        _invalid = ""
            + "- foo\n"
            + "- bar\n"
            + "-\n"
            + "- baz\n"
            + "- 100\n"
            ;
        _error = ""
            + ":required_novalue   : (line 3)[/2] value required but none.\n"
            + ":type_unmatch       : (line 5)[/4] '100': not a string.\n"
            ;
        doTest();
    }

    /** mapping test */
    public void testMapping1() throws Exception {
        _schema = ""
            + "type:        map\n"
            + "required:    true\n"
            + "mapping:\n"
            + "  name:\n"
            + "    type:       str\n"
            + "    required:   true\n"
            + "  email:\n"
            + "    type:       str\n"
            + "    pattern:    /@/\n"
            + "    required:   yes\n"
            + "  age:\n"
            + "    type:       int\n"
            + "  blood:\n"
            + "    type:       str\n"
            + "    enum:\n"
            + "      - A\n"
            + "      - B\n"
            + "      - O\n"
            + "      - AB\n"
            + "  birth:\n"
            + "    type:       date\n"
            ;
        _valid = ""
            + "name:   foo\n"
            + "email:  foo@mail.com\n"
            + "age:    20\n"
            + "blood:  AB\n"
            + "birth:   1985-01-01\n"
            ;
        _invalid = ""
            + "nam:    foo\n"
            + "email:  foo(at)mail.com\n"
            + "age:    twenty\n"
            + "blood:  ab\n"
            + "birth:  Jul 01, 1985\n"
            ;
        _error = ""
            + ":required_nokey     : (line 1)[/] key 'name:' is required.\n"
            + ":key_undefined      : (line 1)[/nam] key 'nam:' is undefined.\n"
            + ":pattern_unmatch    : (line 2)[/email] 'foo(at)mail.com': not matched to pattern /@/.\n"
            + ":type_unmatch       : (line 3)[/age] 'twenty': not a integer.\n"
            + ":enum_notexist      : (line 4)[/blood] 'ab': invalid blood value.\n"
            + ":type_unmatch       : (line 5)[/birth] 'Jul 01, 1985': not a date.\n"
            ;
        doTest();
    }

    /** nest of seq and map */
    public void testNested1() throws Exception {
        _schema = ""
            + "type:        map\n"
            + "required:    true\n"
            + "mapping:\n"
            + "  address-book:\n"
            + "    type:       seq\n"
            + "    required:   true\n"
            + "    sequence:\n"
            + "      - type:   map\n"
            + "        mapping:\n"
            + "          name:\n"
            + "            type:       str\n"
            + "            required:   yes\n"
            + "          email:\n"
            + "            type:       str\n"
            + "            pattern:    /@/\n"
            + "            required:   yes\n"
            + "          age:\n"
            + "            type:       int\n"
            + "          blood:\n"
            + "            type:       str\n"
            + "            enum:\n"
            + "              - A\n"
            + "              - B\n"
            + "              - O\n"
            + "              - AB\n"
            + "          birth:\n"
            + "            type:       date\n"
            ;
        _valid = ""
            + "address-book:\n"
            + "  - name:       foo\n"
            + "    email:      foo@mail.com\n"
            + "    age:        20\n"
            + "    blood:      AB\n"
            + "    birth:      1985-01-01\n"
            + "  - name:       bar\n"
            + "    email:      foo@mail.com\n"
            ;
        _invalid = ""
            + "address-book:\n"
            + "  - name:       foo\n"
            + "    mail:       foo@mail.com\n"
            + "    age:        twenty\n"
            + "    blood:      ab\n"
            + "    birth:      1985/01/01\n"
            + "  - name:       bar\n"
            + "    email:      bar(at)mail.com\n"
            ;
        _error = ""
            + ":required_nokey     : (line 2)[/address-book/0] key 'email:' is required.\n"
            + ":key_undefined      : (line 3)[/address-book/0/mail] key 'mail:' is undefined.\n"
            + ":type_unmatch       : (line 4)[/address-book/0/age] 'twenty': not a integer.\n"
            + ":enum_notexist      : (line 5)[/address-book/0/blood] 'ab': invalid blood value.\n"
            + ":type_unmatch       : (line 6)[/address-book/0/birth] '1985/01/01': not a date.\n"
            + ":pattern_unmatch    : (line 8)[/address-book/1/email] 'bar(at)mail.com': not matched to pattern /@/.\n"
            ;
        doTest();
    }

    /** schema with anchor */
    public void testAnchor1() throws Exception {
        _schema = ""
            + "type:        seq\n"
            + "required:    true\n"
            + "sequence:\n"
            + "  - type:        map\n"
            + "    required:    true\n"
            + "    mapping:\n"
            + "      first-name: &name\n"
            + "        type:       str\n"
            + "        required:   yes\n"
            + "      family-name: *name\n"
            ;
        _valid = ""
            + "- first-name:  foo\n"
            + "  family-name: Foo\n"
            + "- first-name:  bar\n"
            + "  family-name: Bar\n"
            ;
        _invalid = ""
            + "- first-name:  foo\n"
            + "  last-name:   Foo\n"
            + "- first-name:  bar\n"
            + "  family-name: 100\n"
            ;
        _error = ""
            + ":required_nokey     : (line 1)[/0] key 'family-name:' is required.\n"
            + ":key_undefined      : (line 2)[/0/last-name] key 'last-name:' is undefined.\n"
            + ":type_unmatch       : (line 4)[/1/family-name] '100': not a string.\n"
            ;
        doTest();
    }

    /** schema with anchor 2 */
    public void testAnchor2() throws Exception {
        _schema = ""
            + "type:        map\n"
            + "required:    true\n"
            + "mapping:\n"
            + "  title: &name\n"
            + "    type:       str\n"
            + "    required:   true\n"
            + "  address-book:\n"
            + "    type:       seq\n"
            + "    required:   true\n"
            + "    sequence:\n"
            + "      - type:   map\n"
            + "        mapping:\n"
            + "          name: *name\n"
            + "          email:\n"
            + "            type:       str\n"
            + "            required:   yes\n"
            ;
        _valid = ""
            + "title:   my friends\n"
            + "address-book:\n"
            + "  - name:   foo\n"
            + "    email:  foo@mail.com\n"
            + "  - name:   bar\n"
            + "    email:  bar@mail.com\n"
            ;
        _invalid = ""
            + "title:   my friends\n"
            + "address-book:\n"
            + "  - name:  100\n"
            + "    email: foo@mail.com\n"
            + "  - first-name:  bar\n"
            + "    email: bar@mail.com\n"
            ;
        _error = ""
            + ":type_unmatch       : (line 3)[/address-book/0/name] '100': not a string.\n"
            + ":required_nokey     : (line 5)[/address-book/1] key 'name:' is required.\n"
            + ":key_undefined      : (line 5)[/address-book/1/first-name] key 'first-name:' is undefined.\n"
            ;
        doTest();
    }

    /** document with anchor */
    public void testAnchor3() throws Exception {
        _schema = ""
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
        _valid = ""
            + "- &foo\n"
            + "  name:  foo\n"
            + "  post:  exective\n"
            + "- &bar\n"
            + "  name:  bar\n"
            + "  post:  manager\n"
            + "  supervisor: *foo\n"
            + "- &baz\n"
            + "  name:  baz\n"
            + "  post:  clerk\n"
            + "  supervisor: *bar\n"
            + "- &zak\n"
            + "  name:  zak\n"
            + "  post:  clerk\n"
            + "  supervisor: *bar\n"
            ;
        _invalid = ""
            + "- &foo\n"
            + "  name:  100\n"
            + "  post:  exective\n"
            + "  supervisor: *foo\n"
            + "- &bar\n"
            + "  name:  foo\n"
            + "  post:  worker\n"
            + "  supervisor: *foo\n"
            ;
        _error = ""
            + ":type_unmatch       : (line 2)[/0/name] '100': not a string.\n"
            + ":enum_notexist      : (line 7)[/1/post] 'worker': invalid post value.\n"
            ;
        doTest();
    }

    /** range test && bug#????? */
    public void testRange1() throws Exception {
        _schema = ""
            + "type:  map\n"
            + "mapping:\n"
            + " \"max-only\":\n"
            + "    type:     seq\n"
            + "    sequence:\n"
            + "      - type:     number\n"
            + "        required: yes\n"
            + "        range:    { max: 100 }\n"
            + " \"min-only\":\n"
            + "    type:     seq\n"
            + "    sequence:\n"
            + "      - type:     number\n"
            + "        required: yes\n"
            + "        range:    { min: 10.0 }\n"
            + " \"max-and-min\":\n"
            + "    type:     seq\n"
            + "    sequence:\n"
            + "      - type:     number\n"
            + "        required: yes\n"
            + "        range:    { max: 100.0, min: 10.0 }\n"
            ;
        _valid = ""
            + "max-only:\n"
            + "  - 100\n"
            + "  - 100.0\n"
            + "min-only:\n"
            + "  - 10\n"
            + "  - 10.0\n"
            + "max-and-min:\n"
            + "  - 100\n"
            + "  - 10\n"
            + "  - 100.0\n"
            + "  - 10.0\n"
            ;
        _invalid = ""
            + "max-only:\n"
            + "  - 101\n"
            + "  - 100.1\n"
            + "min-only:\n"
            + "  - 9\n"
            + "  - 9.99\n"
            + "max-and-min:\n"
            + "  - 101\n"
            + "  - 100.1\n"
            + "  - 9\n"
            + "  - 9.99\n"
            ;
        _error = ""
            + ":range_toolarge     : (line 2)[/max-only/0] '101': too large (> max 100).\n"
            + ":range_toolarge     : (line 3)[/max-only/1] '100.1': too large (> max 100).\n"
            + ":range_toosmall     : (line 5)[/min-only/0] '9': too small (< min 10.0).\n"
            + ":range_toosmall     : (line 6)[/min-only/1] '9.99': too small (< min 10.0).\n"
            + ":range_toolarge     : (line 8)[/max-and-min/0] '101': too large (> max 100.0).\n"
            + ":range_toolarge     : (line 9)[/max-and-min/1] '100.1': too large (> max 100.0).\n"
            + ":range_toosmall     : (line 10)[/max-and-min/2] '9': too small (< min 10.0).\n"
            + ":range_toosmall     : (line 11)[/max-and-min/3] '9.99': too small (< min 10.0).\n"
            ;
        doTest();
    }

    /** range test (with max-ex and min-ex) */
    public void testRange2() throws Exception {
        _schema = ""
            + "type:  map\n"
            + "mapping:\n"
            + " \"max-ex-only\":\n"
            + "    type:     seq\n"
            + "    sequence:\n"
            + "      - type:     number\n"
            + "        required: yes\n"
            + "        range:    { max-ex: 100 }\n"
            + " \"min-ex-only\":\n"
            + "    type:     seq\n"
            + "    sequence:\n"
            + "      - type:     number\n"
            + "        required: yes\n"
            + "        range:    { min-ex: 10.0 }\n"
            + " \"max-ex-and-min-ex\":\n"
            + "    type:     seq\n"
            + "    sequence:\n"
            + "      - type:     number\n"
            + "        required: yes\n"
            + "        range:    { max-ex: 100.0, min-ex: 10.0 }\n"
            ;
        _valid = ""
            + "max-ex-only:\n"
            + "  - 99\n"
            + "  - 99.99999\n"
            + "min-ex-only:\n"
            + "  - 11\n"
            + "  - 10.00001\n"
            + "max-ex-and-min-ex:\n"
            + "  - 99\n"
            + "  - 11\n"
            + "  - 99.99999\n"
            + "  - 10.00001\n"
            ;
        _invalid = ""
            + "max-ex-only:\n"
            + "  - 100\n"
            + "  - 100.0\n"
            + "min-ex-only:\n"
            + "  - 10\n"
            + "  - 10.0\n"
            + "max-ex-and-min-ex:\n"
            + "  - 100\n"
            + "  - 100.0\n"
            + "  - 10\n"
            + "  - 10.0\n"
            ;
        _error = ""
            + ":range_toolargeex   : (line 2)[/max-ex-only/0] '100': too large (>= max 100).\n"
            + ":range_toolargeex   : (line 3)[/max-ex-only/1] '100.0': too large (>= max 100).\n"
            + ":range_toosmallex   : (line 5)[/min-ex-only/0] '10': too small (<= min 10.0).\n"
            + ":range_toosmallex   : (line 6)[/min-ex-only/1] '10.0': too small (<= min 10.0).\n"
            + ":range_toolargeex   : (line 8)[/max-ex-and-min-ex/0] '100': too large (>= max 100.0).\n"
            + ":range_toolargeex   : (line 9)[/max-ex-and-min-ex/1] '100.0': too large (>= max 100.0).\n"
            + ":range_toosmallex   : (line 10)[/max-ex-and-min-ex/2] '10': too small (<= min 10.0).\n"
            + ":range_toosmallex   : (line 11)[/max-ex-and-min-ex/3] '10.0': too small (<= min 10.0).\n"
            ;
        doTest();
    }

    /** range test (with max, min, max-ex and min-ex) */
    public void testRange3() throws Exception {
        _schema = ""
            + "type:  map\n"
            + "mapping:\n"
            + " \"A\":\n"
            + "    type:     seq\n"
            + "    sequence:\n"
            + "      - type:     number\n"
            + "        required: yes\n"
            + "        range:    { max: 100, min-ex: 10.0 }\n"
            + " \"B\":\n"
            + "    type:     seq\n"
            + "    sequence:\n"
            + "      - type:     number\n"
            + "        required: yes\n"
            + "        range:    { min: 10, max-ex: 100.0 }\n"
            ;
        _valid = ""
            + "A:\n"
            + "  - 100\n"
            + "  - 10.00001\n"
            + "B:\n"
            + "  - 10\n"
            + "  - 99.99999\n"
            ;
        _invalid = ""
            + "A:\n"
            + "  - 100.00001\n"
            + "  - 10.0\n"
            + "B:\n"
            + "  - 9.99999\n"
            + "  - 100.0\n"
            ;
        _error = ""
            + ":range_toolarge     : (line 2)[/A/0] '100.00001': too large (> max 100).\n"
            + ":range_toosmallex   : (line 3)[/A/1] '10.0': too small (<= min 10.0).\n"
            + ":range_toosmall     : (line 5)[/B/0] '9.99999': too small (< min 10).\n"
            + ":range_toolargeex   : (line 6)[/B/1] '100.0': too large (>= max 100.0).\n"
            ;
        doTest();
    }

    /** length test */
    public void testLength1() throws Exception {
        _schema = ""
            + "type:  map\n"
            + "mapping:\n"
            + " \"max-only\":\n"
            + "    type:   seq\n"
            + "    sequence:\n"
            + "      - type:  str\n"
            + "        length:  { max: 8 }\n"
            + " \"min-only\":\n"
            + "    type:   seq\n"
            + "    sequence:\n"
            + "      - type:  str\n"
            + "        length:  { min: 4 }\n"
            + " \"max-and-min\":\n"
            + "    type:   seq\n"
            + "    sequence:\n"
            + "      - type:  str\n"
            + "        length:  { max: 8, min: 4 }\n"
            ;
        _valid = ""
            + "max-only:\n"
            + "  - hogehoge\n"
            + "  - a\n"
            + "  -\n"
            + "min-only:\n"
            + "  - hoge\n"
            + "  - hogehogehogehogehoge\n"
            + "max-and-min:\n"
            + "  - hogehoge\n"
            + "  - hoge\n"
            ;
        _invalid = ""
            + "max-only:\n"
            + "  - hogehoge!\n"
            + "min-only:\n"
            + "  - foo\n"
            + "  -\n"
            + "max-and-min:\n"
            + "  - foobarbaz\n"
            + "  - foo\n"
            ;
        _error = ""
            + ":length_toolong     : (line 2)[/max-only/0] 'hogehoge!': too long (length 9 > max 8).\n"
            + ":length_tooshort    : (line 4)[/min-only/0] 'foo': too short (length 3 < min 4).\n"
            + ":length_toolong     : (line 7)[/max-and-min/0] 'foobarbaz': too long (length 9 > max 8).\n"
            + ":length_tooshort    : (line 8)[/max-and-min/1] 'foo': too short (length 3 < min 4).\n"
            ;
        doTest();
    }

    /** length test (with max-ex and min-ex) */
    public void testLength2() throws Exception {
        _schema = ""
            + "type:  map\n"
            + "mapping:\n"
            + " \"max-ex-only\":\n"
            + "    type:   seq\n"
            + "    sequence:\n"
            + "      - type:  str\n"
            + "        length:  { max-ex: 8 }\n"
            + " \"min-ex-only\":\n"
            + "    type:   seq\n"
            + "    sequence:\n"
            + "      - type:  str\n"
            + "        length:  { min-ex: 4 }\n"
            + " \"max-ex-and-min-ex\":\n"
            + "    type:   seq\n"
            + "    sequence:\n"
            + "      - type:  str\n"
            + "        length:  { max-ex: 8, min-ex: 4 }\n"
            ;
        _valid = ""
            + "max-ex-only:\n"
            + "  - hogehog\n"
            + "  - a\n"
            + "  -\n"
            + "min-ex-only:\n"
            + "  - hoge!\n"
            + "max-ex-and-min-ex:\n"
            + "  - hogehog\n"
            + "  - hoge!\n"
            ;
        _invalid = ""
            + "max-ex-only:\n"
            + "  - hogehoge\n"
            + "min-ex-only:\n"
            + "  - foo!\n"
            + "  -\n"
            + "max-ex-and-min-ex:\n"
            + "  - foobarba\n"
            + "  - foo!\n"
            ;
        _error = ""
            + ":length_toolongex   : (line 2)[/max-ex-only/0] 'hogehoge': too long (length 8 >= max 8).\n"
            + ":length_tooshortex  : (line 4)[/min-ex-only/0] 'foo!': too short (length 4 <= min 4).\n"
            + ":length_toolongex   : (line 7)[/max-ex-and-min-ex/0] 'foobarba': too long (length 8 >= max 8).\n"
            + ":length_tooshortex  : (line 8)[/max-ex-and-min-ex/1] 'foo!': too short (length 4 <= min 4).\n"
            ;
        doTest();
    }

    /** length test (with min, max, max-ex and min-ex) */
    public void testLength3() throws Exception {
        _schema = ""
            + "type:  map\n"
            + "mapping:\n"
            + " \"A\":\n"
            + "    type:   seq\n"
            + "    sequence:\n"
            + "      - type:  str\n"
            + "        length:  { max: 8, min-ex: 4 }\n"
            + " \"B\":\n"
            + "    type:   seq\n"
            + "    sequence:\n"
            + "      - type:  str\n"
            + "        length:  { max-ex: 8, min: 4 }\n"
            ;
        _valid = ""
            + "A:\n"
            + "  - hogehoge\n"
            + "  - hogeh\n"
            + "B:\n"
            + "  - hogehog\n"
            + "  - hoge\n"
            ;
        _invalid = ""
            + "A:\n"
            + "  - hogehoge!\n"
            + "  - hoge\n"
            + "B:\n"
            + "  - hogehoge\n"
            + "  - hog\n"
            ;
        _error = ""
            + ":length_toolong     : (line 2)[/A/0] 'hogehoge!': too long (length 9 > max 8).\n"
            + ":length_tooshortex  : (line 3)[/A/1] 'hoge': too short (length 4 <= min 4).\n"
            + ":length_toolongex   : (line 5)[/B/0] 'hogehoge': too long (length 8 >= max 8).\n"
            + ":length_tooshort    : (line 6)[/B/1] 'hog': too short (length 3 < min 4).\n"
            ;
        doTest();
    }

    /** assert test */
    public void testAssert1() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: map\n"
            + "    mapping:\n"
            + "     \"less-than\":\n"
            + "        type: number\n"
            + "        assert: val < 8\n"
            + "     \"more-than\":\n"
            + "        type: number\n"
            + "        assert: 3 < val\n"
            + "     \"between\":\n"
            + "        type: number\n"
            + "        assert: 3 < val && val < 8\n"
            + "     \"except\":\n"
            + "        type: number\n"
            + "        assert: val < 3 || 8 < val\n"
            ;
        _valid = ""
            + "- less-than: 5\n"
            + "- more-than: 5\n"
            + "- between: 5\n"
            + "- except: 0\n"
            ;
        _error = ""
            + ":assert_failed      : (line 1)[/0/less-than] '8': assertion expression failed (val < 8).\n"
            + ":assert_failed      : (line 2)[/1/more-than] '3': assertion expression failed (3 < val).\n"
            + ":assert_failed      : (line 3)[/2/between] '2.9': assertion expression failed (3 < val && val < 8).\n"
            + ":assert_failed      : (line 4)[/3/except] '3.1': assertion expression failed (val < 3 || 8 < val).\n"
            ;
        doTest();
    }

    /** default type test */
    public void testDeftype1() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: map\n"
            + "    mapping:\n"
            + "     \"name\":\n"
            + "     \"email\":\n"
            ;
        _valid = ""
            + "- name: foo\n"
            + "  email: foo@mail.com\n"
            + "- name: bar\n"
            + "- email: baz@mail.com\n"
            ;
        _invalid = ""
            + "- name: 123\n"
            + "  email: true\n"
            + "- name: 3.14\n"
            + "- email: 2004-01-01\n"
            ;
        _error = ""
            + ":type_unmatch       : (line 1)[/0/name] '123': not a string.\n"
            + ":type_unmatch       : (line 2)[/0/email] 'true': not a string.\n"
            + ":type_unmatch       : (line 3)[/1/name] '3.14': not a string.\n"
            + ":type_unmatch       : (line 4)[/2/email] 'Sun Feb 01 00:00:00 JST 2004': not a string.\n"
            ;
        doTest();
    }

    /** ident constraint test */
    public void testIdent1() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: map\n"
            + "    mapping:\n"
            + "     \"name\":\n"
            + "        ident:  yes\n"
            + "     \"age\":\n"
            + "        type: int\n"
            ;
        _valid = ""
            + "- name: foo\n"
            + "  age:  10\n"
            + "- name: bar\n"
            + "  age:  10\n"
            + "- name: baz\n"
            + "  age:  10\n"
            ;
        _invalid = ""
            + "- name: foo\n"
            + "  age:  10\n"
            + "- name: bar\n"
            + "  age:  10\n"
            + "- name: bar\n"
            + "  age:  10\n"
            ;
        _error = ""
            + ":value_notunique    : (line 5)[/2/name] 'bar': is already used at '/1/name'.\n"
            ;
        doTest();
    }

    /** unique constraint test with map */
    public void testUnique1() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: map\n"
            + "    mapping:\n"
            + "     \"name\":\n"
            + "        unique:  yes\n"
            + "     \"age\":\n"
            + "        type: int\n"
            ;
        _valid = ""
            + "- name: foo\n"
            + "  age:  10\n"
            + "- name: bar\n"
            + "  age:  10\n"
            + "- name: baz\n"
            + "  age:  10\n"
            ;
        _invalid = ""
            + "- name: foo\n"
            + "  age:  10\n"
            + "- name: bar\n"
            + "  age:  10\n"
            + "- name: bar\n"
            + "  age:  10\n"
            ;
        _error = ""
            + ":value_notunique    : (line 5)[/2/name] 'bar': is already used at '/1/name'.\n"
            ;
        doTest();
    }

    /** unique constraint test with seq */
    public void testUnique2() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: str\n"
            + "    unique: yes\n"
            ;
        _valid = ""
            + "- foo\n"
            + "- ~\n"
            + "- bar\n"
            + "- ~\n"
            + "- baz\n"
            ;
        _invalid = ""
            + "- foo\n"
            + "- ~\n"
            + "- bar\n"
            + "- ~\n"
            + "- bar\n"
            ;
        _error = ""
            + ":value_notunique    : (line 5)[/4] 'bar': is already used at '/2'.\n"
            ;
        doTest();
    }

    /** default value of map */
    public void testDefault1() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + "  =:\n"
            + "    type: number\n"
            + "    range: { min: -10, max: 10 }\n"
            ;
        _valid = ""
            + "value1: 0\n"
            + "value2: 10\n"
            + "value3: -10\n"
            ;
        _invalid = ""
            + "value1: 0\n"
            + "value2: 20\n"
            + "value3: -20\n"
            ;
        _error = ""
            + ":range_toolarge     : (line 2)[/value2] '20': too large (> max 10).\n"
            + ":range_toosmall     : (line 3)[/value3] '-20': too small (< min -10).\n"
            ;
        doTest();
    }

    /** merge maps */
    public void testMerge1() throws Exception {
        _schema = ""
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
            + "        length: { max: 16 }   # add\n"
            + "     \"email\":\n"
            + "        <<: *email            # merge\n"
            + "        required: yes         # override\n"
            ;
        _valid = ""
            + "group:\n"
            + "  name: foo\n"
            + "  email: foo@mail.com\n"
            + "user:\n"
            + "  name:  bar\n"
            + "  email: bar@mail.com\n"
            ;
        _invalid = ""
            + "group:\n"
            + "  name: foo\n"
            + "  email: foo@mail.com\n"
            + "user:\n"
            + "  name: toooooo-looooong-naaaame\n"
            ;
        _error = ""
            + ":required_nokey     : (line 4)[/user] key 'email:' is required.\n"
            + ":length_toolong     : (line 5)[/user/name] 'toooooo-looooong-naaaame': too long (length 24 > max 16).\n"
            ;
        doTest();
    }

    //-----

}
