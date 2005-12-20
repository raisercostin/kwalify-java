package kwalify;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import java.lang.reflect.*;
import java.io.*;

/**
 *  @revision    $Rev: 4 $
 *  @release     $Release: 0.5.1 $
 *  @copyright   copyright(c) 2005 kuwata-lab all rights reserved.
 */
public class MainTest extends TestCase {

    private String _command = "kwalify";
    private String _name;
    private String[] _args;
    private String _inspect;
    private Class _exception_class;
    private String _error_symbol;
    private String _expected;
    private String _schema;
    private String _document;
    private String _valid;
    private String _valid_out;
    private String _invalid;
    private String _invalid_out;

    private static String __dirname = "tmp.dir";

    static {
        File tmpdir = new File(__dirname);
        if (! tmpdir.exists()) {
            tmpdir.mkdir();
        }
    }

    private void doValidationTest() throws Exception {
        String schema_filename  = null;
        String valid_filename   = null;
        String invalid_filename = null;
        try {
            if (_schema == null) {
                throw new Exception("schema is not defined.");
            }
            schema_filename = _name + ".schema";
            Util.writeFile(schema_filename, _schema);
            //
            if (_valid == null) {
                throw new Exception("valid is not defined.");
            }
            valid_filename = _name + ".valid";
            Util.writeFile(valid_filename, _valid);
            //
            if (_invalid == null) {
                throw new Exception("invalid is not defined.");
            }
            invalid_filename = _name + ".invalid";
            Util.writeFile(invalid_filename, _invalid);
            //
            Main main;
            String output;
            main = new Main(_command);
            _args = new String[] { "-lf", schema_filename, valid_filename };
            output = main.execute(_args);
            assertEquals(_valid_out, output);
            //
            main = new Main(_command);
            _args = new String[] { "-lf", schema_filename, invalid_filename };
            output = main.execute(_args);
            assertEquals(_invalid_out, output);
        } finally {
            String[] filenames = { schema_filename, valid_filename, invalid_filename };
            for (int i = 0; i < filenames.length; i++) {
                Util.moveFile(filenames[i], __dirname);
            }
        }
    }

    private void doExecuteTest() throws Exception {
        if (_args == null) {
            throw new Exception("args is required when method is 'execute'.");
        }
        if (_expected == null) {
            throw new Exception("expected is required when method is 'execute'.");
        }
        //
        if (_schema != null) {
            Util.writeFile(_name + ".schema", _schema);
        }
        if (_document != null) {
            Util.writeFile(_name + ".document", _document);
        }
        //
        try {
            Main main = new Main(_command);
            String actual = main.execute(_args);
            if (_exception_class == null) {
                assertEquals(_expected, actual);
            } else {
                fail("exception " + _exception_class.getName() + " expected but not throwned.");
            }
        } catch (Exception ex) {
            if (ex.getClass() == _exception_class) {  // OK
                if (ex instanceof CommandOptionException) {
                    assertEquals(_error_symbol, ((CommandOptionException)ex).getErrorSymbol());
                }
            } else {  // NG
                throw ex;
            }
        } finally {
            if (_schema != null) {
                Util.moveFile(_name + ".schema", __dirname);
            }
            if (_document != null) {
                Util.moveFile(_name + ".document", __dirname);
            }
        }
    }

    private void doParseOptionsTest() throws Exception {
        if (_inspect != null) {
            try {
                Main main = new Main(_command);
                String[] filenames = invokeParseOptions(main, _args);
                //String[] filenames = main.parseOptions(_args);
                StringBuffer sb = new StringBuffer();
                sb.append(main.inspect());
                sb.append("filenames:\n");
                for (int i = 0; i < filenames.length; i++) {
                    sb.append("  - ").append(filenames[i]).append('\n');
                }
                String actual = sb.toString();
                if (_exception_class == null) {
                    assertEquals(_inspect, actual);
                } else {
                    fail("exception " + _exception_class.getName() + " expected but not happened.");
                }
            } catch (Exception ex) {
                if (ex instanceof InvocationTargetException) {
                    ex = (Exception)ex.getCause();
                }
                if (ex.getClass() == _exception_class) {
                    // OK
                    if (ex instanceof CommandOptionException) {
                        assertEquals(_error_symbol, ((CommandOptionException)ex).getErrorSymbol());
                    }
                } else {
                    // NG
                    throw ex;
                }
            }
        }
    }


    private String[] invokeParseOptions(Main main, String[] args) throws Exception {
        Class[] argtypes = { new String[] {}.getClass() };
        Method method = Main.class.getDeclaredMethod("parseOptions", argtypes);
        method.setAccessible(true);
        Object result = method.invoke(main, new Object[] { args });
        String[] filenames = (String[])result;
        return filenames;
    }


/*    
    private Object invokePrivateMethod(Object obj, String method_name, Class[] argtypes, Object[] argvalues) throws Exception {
        Method method = obj.getClass().getDeclaredMethod(method_name, argtypes);
        method.setAccessible(true);
        Object result = method.invoke(obj, argvalues);
        return result;
    }
*/

    public static void main(String[] args) {
        TestRunner.run(MainTest.class);
    }

    //-----

    /** test Main#parseOptions() */
    public void testParseoptions1() throws Exception {
        _name = "parseOptions1";
        _args = new String[] {"-hvsmtlEDf", "schema.yaml", "document.yaml", "document2.yaml"};
        _inspect = ""
            + "command       : kwalify\n"
            + "flag_help     : true\n"
            + "flag_version  : true\n"
            + "flag_silent   : true\n"
            + "flag_meta     : true\n"
            + "flag_untabify : true\n"
            + "flag_emacs    : true\n"
            + "flag_linenum  : true\n"
            + "flag_debug    : true\n"
            + "schema_filename : schema.yaml\n"
            + "properties:\n"
            + "filenames:\n"
            + "  - document.yaml\n"
            + "  - document2.yaml\n"
            ;
        doParseOptionsTest();
    }

    /** -ffilename */
    public void testParseoptions2() throws Exception {
        _name = "parseOptions2";
        _args = new String[] {"-lfschema.yaml"};
        _inspect = ""
            + "command       : kwalify\n"
            + "flag_help     : false\n"
            + "flag_version  : false\n"
            + "flag_silent   : false\n"
            + "flag_meta     : false\n"
            + "flag_untabify : false\n"
            + "flag_emacs    : false\n"
            + "flag_linenum  : true\n"
            + "flag_debug    : false\n"
            + "schema_filename : schema.yaml\n"
            + "properties:\n"
            + "filenames:\n"
            ;
        doParseOptionsTest();
    }

    /** '--help' is equal to '-h' */
    public void testParseoptions3() throws Exception {
        _name = "parseOptions3";
        _args = new String[] {"--help", "document.yaml"};
        _inspect = ""
            + "command       : kwalify\n"
            + "flag_help     : true\n"
            + "flag_version  : false\n"
            + "flag_silent   : false\n"
            + "flag_meta     : false\n"
            + "flag_untabify : false\n"
            + "flag_emacs    : false\n"
            + "flag_linenum  : false\n"
            + "flag_debug    : false\n"
            + "schema_filename : null\n"
            + "properties:\n"
            + "  help: true\n"
            + "filenames:\n"
            + "  - document.yaml\n"
            ;
        doParseOptionsTest();
    }

    /** '-E' turns on '-l' */
    public void testParseoptions4() throws Exception {
        _name = "parseOptions4";
        _args = new String[] {"-E", "document.yaml"};
        _inspect = ""
            + "command       : kwalify\n"
            + "flag_help     : false\n"
            + "flag_version  : false\n"
            + "flag_silent   : false\n"
            + "flag_meta     : false\n"
            + "flag_untabify : false\n"
            + "flag_emacs    : true\n"
            + "flag_linenum  : true\n"
            + "flag_debug    : false\n"
            + "schema_filename : null\n"
            + "properties:\n"
            + "filenames:\n"
            + "  - document.yaml\n"
            ;
        doParseOptionsTest();
    }

    /** invalid command-line option */
    public void testOptionerror1() throws Exception {
        _name = "optionError1";
        _args = new String[] {"-hvi"};
        _inspect = ""
            + "*"
            ;
        _exception_class = CommandOptionException.class;
        _error_symbol = "command.option.invalid";
        doParseOptionsTest();
    }

    /** no argument of '-f' */
    public void testOptionerror2() throws Exception {
        _name = "optionError2";
        _args = new String[] {"-f"};
        _inspect = ""
            + "*"
            ;
        _exception_class = CommandOptionException.class;
        _error_symbol = "command.option.noschema";
        doParseOptionsTest();
    }

    /** action required */
    public void testOptionerror4() throws Exception {
        _name = "optionError4";
        _args = new String[] {"document.yaml"};
        _inspect = ""
            + "*"
            ;
        _exception_class = CommandOptionException.class;
        _error_symbol = "command.option.noaction";
        _expected = "";
        doExecuteTest();
    }

    /** option '-v' */
    public void testVersion() throws Exception {
        _name = "version";
        _args = new String[] {"-vt", "document.yaml"};
        _expected = "0.5.1\n";
        doExecuteTest();
    }

    /** option '-h' */
    public void testHelp() throws Exception {
        _name = "help";
        _args = new String[] {"-hD", "document.yaml"};
        _expected = "Usage1: kwalify [-hvstlE] -f schema.yaml doc.yaml [doc2.yaml ...]\nUsage2: kwalify [-hvstlE] -m schema.yaml [schema2.yaml ...]\n  -h, --help      :  help\n  -v              :  version\n  -s              :  silent\n  -f schema.yaml  :  schema definition file\n  -m              :  meta-validation mode\n  -t              :  expand tab character automatically\n  -l              :  show linenumber when errored (experimental)\n  -E              :  show errors in emacs-style (implies '-l')\n";
        doExecuteTest();
    }

    /** option '-s' (valid) */
    public void testSilent1() throws Exception {
        _name = "silent1";
        _args = new String[] {"-sf", "silent1.schema", "silent1.document"};
        _expected = "";
        _schema = "type:   seq\nsequence:\n  - type:   str\n";
        _document = "- foo\n- bar\n- baz\n";
        doExecuteTest();
    }

    /** option '-s' (invalid) */
    public void testSilent2() throws Exception {
        _name = "silent2";
        _args = new String[] {"-sf", "silent2.schema", "silent2.document"};
        _expected = "silent2.document#1: INVALID\n  - [/1] '123': not a string.\n  - [/2] 'true': not a string.\n";
        _schema = "type:   seq\nsequence:\n  - type:   str\n";
        _document = "- foo\n- bar\n- baz\n---\n- foo\n- 123\n- true\n";
        doExecuteTest();
    }

    /** option '-t' */
    public void testUntabify() throws Exception {
        _name = "untabify";
        _args = new String[] {"-tf", "untabify.schema", "untabify.document"};
        _expected = "untabify.document#0: valid.\n";
        _schema = "type:\t\tseq\nsequence:\n  -\ttype: map\n\tmapping:\n  \t   \"key\":\n\t  \ttype: text\n\t        required:\tyes\n\t   \"value\":\n\t        type: any\n\t       \trequired:  yes\n";
        _document = "#\n\t- key: foo\n\t  value: 123\n        - key: bar\n  \t  value: [a, b, c]\n";
        doExecuteTest();
    }

    /** stream document */
    public void testStream() throws Exception {
        _name = "stream";
        _schema = "type:   seq\nsequence:\n  - type:   str\n";
        _valid = "---\n- foo\n- bar\n- baz\n---\n- aaa\n- bbb\n- ccc\n";
        _invalid = "---\n- foo\n- 123\n- baz\n---\n- aaa\n- bbb\n- true\n";
        _valid_out = "stream.valid#0: valid.\nstream.valid#1: valid.\n";
        _invalid_out = "stream.invalid#0: INVALID\n  - (line 3) [/1] '123': not a string.\nstream.invalid#1: INVALID\n  - (line 8) [/2] 'true': not a string.\n";
        doValidationTest();
    }

    /** meta validation (valid) */
    public void testMeta1() throws Exception {
        _name = "meta1";
        _args = new String[] {"-m", "meta1.schema"};
        _expected = "meta1.schema#0: valid.\n";
        _schema = "type:   seq\nsequence:\n  - type:   str\n";
        _document = "";
        doExecuteTest();
    }

    /** meta validation (invalid) */
    public void testMeta2() throws Exception {
        _name = "meta2";
        _args = new String[] {"-m", "meta2.schema"};
        _expected = "meta2.schema#0: INVALID\n  - [/] type 'map' requires 'mapping:'.\n  - [/] 'sequence:': not available with mapping.\n";
        _schema = "type:   map\nsequence:\n  - type:   str\n";
        _document = "";
        doExecuteTest();
    }

    /** show errors in emacs style */
    public void testEmacs() throws Exception {
        _name = "emacs";
        _args = new String[] {"-Ef", "emacs.schema", "emacs.document"};
        _expected = "emacs.document#0: INVALID\nemacs.document:3: [/1/key] '2': not a string.\nemacs.document:4: [/1/val] key 'val:' is undefined.\nemacs.document:5: [/2] key 'key:' is required.\nemacs.document:5: [/2/kye] key 'kye:' is undefined.\n";
        _schema = "type:   seq\nsequence:\n  - type:   map\n    mapping:\n     \"key\":   { type: str, required: yes }\n     \"value\": { type: any }\n";
        _document = "- key: one\n  value: 1\n- key: 2\n  val: two\n- kye: three\n  value:\n";
        doExecuteTest();
    }

    /** basic validation */
    public void testValidation01() throws Exception {
        _name = "validation01";
        _schema = "type:   seq\nsequence:\n  - type:   str\n";
        _valid = "- foo\n- bar\n- baz\n";
        _invalid = "- foo\n- 123\n- baz\n";
        _valid_out = "validation01.valid#0: valid.\n";
        _invalid_out = "validation01.invalid#0: INVALID\n  - (line 2) [/1] '123': not a string.\n";
        doValidationTest();
    }

    /** basic validation */
    public void testValidation02() throws Exception {
        _name = "validation02";
        _schema = "type:       map\nmapping:\n  name:\n    type:      str\n    required:  yes\n  email:\n    type:      str\n    pattern:   /@/\n  age:\n    type:      int\n  birth:\n    type:      date\n";
        _valid = "name:   foo\nemail:  foo@mail.com\nage:    20\nbirth:  1985-01-01\n";
        _invalid = "name:   foo\nemail:  foo(at)mail.com\nage:    twenty\nbirth:  Jun 01, 1985\n";
        _valid_out = "validation02.valid#0: valid.\n";
        _invalid_out = "validation02.invalid#0: INVALID\n  - (line 2) [/email] 'foo(at)mail.com': not matched to pattern /@/.\n  - (line 3) [/age] 'twenty': not a integer.\n  - (line 4) [/birth] 'Jun 01, 1985': not a date.\n";
        doValidationTest();
    }

    /** sequence of mapping */
    public void testValidation03() throws Exception {
        _name = "validation03";
        _schema = "type:      seq\nsequence:\n  - type:      map\n    mapping:\n      name:\n        type:      str\n        required:  true\n      email:\n        type:      str\n";
        _valid = "- name:   foo\n  email:  foo@mail.com\n- name:   bar\n  email:  bar@mail.net\n- name:   baz\n  email:  baz@mail.org\n";
        _invalid = "- name:   foo\n  email:  foo@mail.com\n- naem:   bar\n  email:  bar@mail.net\n- name:   baz\n  mail:   baz@mail.org\n";
        _valid_out = "validation03.valid#0: valid.\n";
        _invalid_out = "validation03.invalid#0: INVALID\n  - (line 3) [/1] key 'name:' is required.\n  - (line 3) [/1/naem] key 'naem:' is undefined.\n  - (line 6) [/2/mail] key 'mail:' is undefined.\n";
        doValidationTest();
    }

    /** mapping of sequence */
    public void testValidation04() throws Exception {
        _name = "validation04";
        _schema = "type:      map\nmapping:\n  company:\n    type:      str\n    required:  yes\n  email:\n    type:      str\n  employees:\n    type:      seq\n    sequence:\n      - type:    map\n        mapping:\n          code:\n            type:      int\n            required:  yes\n          name:\n            type:      str\n            required:  yes\n          email:\n            type:      str\n";
        _valid = "company:    Kuwata lab.\nemail:      webmaster@kuwata-lab.com\nemployees:\n  - code:   101\n    name:   foo\n    email:  foo@kuwata-lab.com\n  - code:   102\n    name:   bar\n    email:  bar@kuwata-lab.com\n";
        _invalid = "company:    Kuwata Lab.\nemail:      webmaster@kuwata-lab.com\nemployees:\n  - code:   A101\n    name:   foo\n    email:  foo@kuwata-lab.com\n  - code:   102\n    name:   bar\n    mail:   bar@kuwata-lab.com\n";
        _valid_out = "validation04.valid#0: valid.\n";
        _invalid_out = "validation04.invalid#0: INVALID\n  - (line 4) [/employees/0/code] 'A101': not a integer.\n  - (line 9) [/employees/1/mail] key 'mail:' is undefined.\n";
        doValidationTest();
    }

    /** rule and entry */
    public void testValidation05() throws Exception {
        _name = "validation05";
        _schema = "type:      seq                                # new rule\nsequence:\n  - \n    type:      map                            # new rule\n    mapping:\n      name:\n        type:       str                       # new rule\n        required:   yes\n      email:\n        type:       str                       # new rule\n        required:   yes\n        pattern:    /@/\n      password:\n        type:       str                       # new rule\n        length:     { max: 16, min: 8 }\n      age:\n        type:       int                       # new rule\n        range:      { max: 30, min: 18 }\n        # or assert: 18 <= val && val <= 30\n      blood:\n        type:       str                       # new rule\n        enum:\n          - A\n          - B\n          - O\n          - AB\n      birth:\n        type:       date                      # new rule\n      memo:\n        type:       any                       # new rule\n";
        _valid = "- name:     foo\n  email:    foo@mail.com\n  password: xxx123456\n  age:      20\n  blood:    A\n  birth:    1985-01-01\n- name:     bar\n  email:    bar@mail.net\n  age:      25\n  blood:    AB\n  birth:    1980-01-01\n";
        _invalid = "- name:     foo\n  email:    foo(at)mail.com\n  password: xxx123\n  age:      twenty\n  blood:    a\n  birth:    1985-01-01\n- given-name:  bar\n  family-name: Bar\n  email:    bar@mail.net\n  age:      15\n  blood:    AB\n  birth:    1980/01/01\n";
        _valid_out = "validation05.valid#0: valid.\n";
        _invalid_out = "validation05.invalid#0: INVALID\n  - (line 2) [/0/email] 'foo(at)mail.com': not matched to pattern /@/.\n  - (line 3) [/0/password] 'xxx123': too short (length 6 < min 8).\n  - (line 4) [/0/age] 'twenty': not a integer.\n  - (line 5) [/0/blood] 'a': invalid blood value.\n  - (line 7) [/1] key 'name:' is required.\n  - (line 7) [/1/given-name] key 'given-name:' is undefined.\n  - (line 8) [/1/family-name] key 'family-name:' is undefined.\n  - (line 10) [/1/age] '15': too small (< min 18).\n  - (line 12) [/1/birth] '1980/01/01': not a date.\n";
        doValidationTest();
    }

    /** unique constraint */
    public void testValidation06() throws Exception {
        _name = "validation06";
        _schema = "type: seq\nsequence:\n  - type:     map\n    required: yes\n    mapping:\n      name:\n        type:     str\n        required: yes\n        unique:   yes\n      email:\n        type:     str\n      groups:\n        type:     seq\n        sequence:\n          - type: str\n            unique:   yes\n";
        _valid = "- name:   foo\n  email:  admin@mail.com\n  groups:\n    - users\n    - foo\n    - admin\n- name:   bar\n  email:  admin@mail.com\n  groups:\n    - users\n    - admin\n- name:   baz\n  email:  baz@mail.com\n  groups:\n    - users\n";
        _invalid = "- name:   foo\n  email:  admin@mail.com\n  groups:\n    - foo\n    - users\n    - admin\n    - foo\n- name:   bar\n  email:  admin@mail.com\n  groups:\n    - admin\n    - users\n- name:   bar\n  email:  baz@mail.com\n  groups:\n    - users\n";
        _valid_out = "validation06.valid#0: valid.\n";
        _invalid_out = "validation06.invalid#0: INVALID\n  - (line 7) [/0/groups/3] 'foo': is already used at '/0/groups/0'.\n  - (line 13) [/2/name] 'bar': is already used at '/1/name'.\n";
        doValidationTest();
    }

    /** json */
    public void testValidation12() throws Exception {
        _name = "validation12";
        _schema = "{ \"type\": \"map\",\n  \"required\": true,\n  \"mapping\": {\n    \"name\": {\n       \"type\": \"str\",\n       \"required\": true\n    },\n    \"email\": {\n       \"type\": \"str\"\n    },\n    \"age\": {\n       \"type\": \"int\"\n    },\n    \"gender\": {\n       \"type\": \"str\",\n       \"enum\": [\"M\", \"F\"]\n    },\n    \"favorite\": {\n       \"type\": \"seq\",\n       \"sequence\": [\n          { \"type\": \"str\" }\n       ]\n    }\n  }\n}\n";
        _valid = "{ \"name\": \"Foo\",\n  \"email\": \"foo@mail.com\",\n  \"age\": 20,\n  \"gender\": \"F\",\n  \"favorite\": [\n     \"football\",\n     \"basketball\",\n     \"baseball\"\n  ]\n}\n";
        _invalid = "{ \n  \"mail\": \"foo@mail.com\",\n  \"age\": twenty,\n  \"gender\": \"X\",\n  \"favorite\": [ 123, 456 ]\n}\n";
        _valid_out = "validation12.valid#0: valid.\n";
        _invalid_out = "validation12.invalid#0: INVALID\n  - (line 1) [/] key 'name:' is required.\n  - (line 2) [/mail] key 'mail:' is undefined.\n  - (line 3) [/age] 'twenty': not a integer.\n  - (line 4) [/gender] 'X': invalid gender value.\n  - (line 5) [/favorite/0] '123': not a string.\n  - (line 5) [/favorite/1] '456': not a string.\n";
        doValidationTest();
    }

    /** anchor and alias */
    public void testValidation13() throws Exception {
        _name = "validation13";
        _schema = "type:   seq\nsequence:\n  - &employee\n    type:      map\n    mapping:\n     \"given-name\": &name\n        type:     str\n        required: yes\n     \"family-name\": *name\n     \"post\":\n        enum:\n          - exective\n          - manager\n          - clerk\n     \"supervisor\":  *employee\n";
        _valid = "- &foo\n  given-name:    foo\n  family-name:   Foo\n  post:          exective\n- &bar\n  given-name:    bar\n  family-name:   Bar\n  post:          manager\n  supervisor:    *foo\n- given-name:    baz\n  family-name:   Baz\n  post:          clerk\n  supervisor:    *bar\n- given-name:    zak\n  family-name:   Zak\n  post:          clerk\n  supervisor:    *bar\n";
        _invalid = "- &foo\n  #given-name:    foo\n  family-name:   Foo\n  post:          exective\n- &bar\n  given-name:    bar\n  family-name:   Bar\n  post:          manager\n  supervisor:    *foo\n- given-name:    baz\n  family-name:   Baz\n  post:          clerk\n  supervisor:    *bar\n- given-name:    zak\n  family-name:   Zak\n  post:          clerk\n  supervisor:    *bar\n";
        _valid_out = "validation13.valid#0: valid.\n";
        _invalid_out = "validation13.invalid#0: INVALID\n  - (line 1) [/0] key 'given-name:' is required.\n";
        doValidationTest();
    }

    /** anchor and alias */
    public void testValidation14() throws Exception {
        _name = "validation14";
        _schema = "type: map\nmapping:\n  =:              # default rule\n    type: number\n    range: { max: 1, min: -1 }\n";
        _valid = "value1: 0\nvalue2: 0.5\nvalue3: -0.9\n";
        _invalid = "value1: 0\nvalue2: 1.1\nvalue3: -2.0\n";
        _valid_out = "validation14.valid#0: valid.\n";
        _invalid_out = "validation14.invalid#0: INVALID\n  - (line 2) [/value2] '1.1': too large (> max 1).\n  - (line 3) [/value3] '-2.0': too small (< min -1).\n";
        doValidationTest();
    }

    /** anchor and alias */
    public void testValidation15() throws Exception {
        _name = "validation15";
        _schema = "type: map\nmapping:\n \"group\":\n    type: map\n    mapping:\n     \"name\": &name\n        type: str\n        required: yes\n     \"email\": &email\n        type: str\n        pattern: /@/\n        required: no\n \"user\":\n    type: map\n    mapping:\n     \"name\":\n        <<: *name             # merge\n        length: { max: 16 }   # override\n     \"email\":\n        <<: *email            # merge\n        required: yes         # add\n";
        _valid = "group:\n  name: foo\n  email: foo@mail.com\nuser:\n  name: bar\n  email: bar@mail.com\n";
        _invalid = "group:\n  name: foo\n  email: foo@mail.com\nuser:\n  name: toooooo-looooong-name\n";
        _valid_out = "validation15.valid#0: valid.\n";
        _invalid_out = "validation15.invalid#0: INVALID\n  - (line 4) [/user] key 'email:' is required.\n  - (line 5) [/user/name] 'toooooo-looooong-name': too long (length 21 > max 16).\n";
        doValidationTest();
    }

    //-----

}
