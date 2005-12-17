package kwalify;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import java.util.*;
import java.text.*;

/**
 *  @revision    $Rev: 2 $
 *  @release     $Release: 0.5.0 $
 *  @copyright   copyright(c) 2005 kuwata-lab all rights reserved.
 */
public class MetaValidatorTest extends TestCase {

    private String _schema;
    private String _meta_msg;
    private String _rule_msg;


    private void doMetaTest() throws Exception {
        if (_meta_msg == null) {
            return;
        }
        YamlParser parser = new YamlParser(_schema);
        Object schema = parser.parse();
        Validator meta_validator = MetaValidator.instance();
        List errors = meta_validator.validate(schema);
        //System.out.println("*** debug: schema = " + Util.inspect(schema));
        //System.out.println("*** debug: errors = " + Util.inspect(errors));
        parser.setErrorsLineNumber(errors);
        Collections.sort(errors);
        StringBuffer sb = new StringBuffer();
        for (Iterator it = errors.iterator(); it.hasNext(); ) {
            ValidationException ex = (ValidationException)it.next();
            String s = errorToString(ex);
            sb.append(s);
        }
        String actual = sb.toString();
        assertEquals(_meta_msg, actual);
    }


    private void doRuleTest() throws Exception {
        if (_rule_msg == null) {
            return;
        }
        String actual = null;
        YamlParser parser = new YamlParser(_schema);
        Object schema = parser.parse();
        try {
            new Rule(schema);
        } catch (SchemaException ex) {
            actual = errorToString(ex);
        }
        assertEquals(_rule_msg, actual);
    }


    MessageFormat __format = new MessageFormat("{0}: [{1}] {2}\n");


    private String errorToString(BaseException ex) {
        String symbol = ex.getErrorSymbol();
        String path   = ex.getPath();
        String mesg   = ex.getMessage();
        Object[] params = { convertSymbol(symbol), path, mesg };
        String s = __format.format(params);
        return s;
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
        TestRunner.run(MetaValidatorTest.class);
    }

    //-----

    /** schema_notmap */
    public void testMeta_SchemaNotmap() throws Exception {
        _schema = ""
            + "- type: str\n"
            + "- type: int\n"
            ;
        _meta_msg = ""
            + ":type_unmatch       : [/] not a mapping.\n"
            ;
        doMetaTest();
    }
    public void testRule_SchemaNotmap() throws Exception {
        _schema = ""
            + "- type: str\n"
            + "- type: int\n"
            ;
        _rule_msg = ""
            + ":schema_notmap      : [/] schema definition is not a mapping.\n"
            ;
        doRuleTest();
    }

    /** unkown type */
    public void testMeta_TypeUnknown1() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"name1\":\n"
            + "    type: str\n"
            + " \"name2\":\n"
            + "    type: string\n"
            + " \"age1\":\n"
            + "    type: int\n"
            + " \"age2\":\n"
            + "    type: integer\n"
            + " \"desc1\":\n"
            + "    type: text\n"
            + " \"amount1\":\n"
            + "    type: float\n"
            + " \"amount2\":\n"
            + "    type: number\n"
            + " \"birth1\":\n"
            + "    type: date\n"
            + " \"birth2\":\n"
            + "    type: time\n"
            + " \"birth3\":\n"
            + "    type: timestamp\n"
            + " \"data1\":\n"
            + "    type: scalar\n"
            + " \"data2\":\n"
            + "    type: schalar\n"
            ;
        _meta_msg = ""
            + ":enum_notexist      : [/mapping/name2/type] 'string': invalid type value.\n"
            + ":enum_notexist      : [/mapping/age2/type] 'integer': invalid type value.\n"
            + ":enum_notexist      : [/mapping/data2/type] 'schalar': invalid type value.\n"
            ;
        doMetaTest();
    }

    /** unkown type */
    public void testMeta_TypeUnknown2() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"name1\":\n"
            + "    type: str\n"
            + " \"name2\":\n"
            + "    type: string\n"
            ;
        _meta_msg = ""
            + ":enum_notexist      : [/mapping/name2/type] 'string': invalid type value.\n"
            ;
        doMetaTest();
    }
    public void testRule_TypeUnknown2() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"name1\":\n"
            + "    type: str\n"
            + " \"name2\":\n"
            + "    type: string\n"
            ;
        _rule_msg = ""
            + ":type_unknown       : [/mapping/name2/type] 'string': unknown type.\n"
            ;
        doRuleTest();
    }

    /** required is not boolean */
    public void testMeta_RequiredNotbool1() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"name\":\n"
            + "    type: str\n"
            + "    required: 1\n"
            ;
        _meta_msg = ""
            + ":type_unmatch       : [/mapping/name/required] '1': not a boolean.\n"
            ;
        doMetaTest();
    }
    public void testRule_RequiredNotbool1() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"name\":\n"
            + "    type: str\n"
            + "    required: 1\n"
            ;
        _rule_msg = ""
            + ":required_notbool   : [/mapping/name/required] '1': not a boolean.\n"
            ;
        doRuleTest();
    }

    /** pattern_syntaxerr */
    public void testMeta_PatternSyntaxerr() throws Exception {
        _schema = ""
            + "type: str\n"
            + "pattern: /[A-/\n"
            ;
        _meta_msg = ""
            + ":pattern_syntaxerr  : [/pattern] '/[A-/': has regexp error.\n"
            ;
        doMetaTest();
    }
    public void testRule_PatternSyntaxerr() throws Exception {
        _schema = ""
            + "type: str\n"
            + "pattern: /[A-/\n"
            ;
        _rule_msg = ""
            + ":pattern_syntaxerr  : [/pattern] '/[A-/': has regexp error.\n"
            ;
        doRuleTest();
    }

    /** enum_notseq */
    public void testMeta_EnumNotseq() throws Exception {
        _schema = ""
            + "type: str\n"
            + "enum: A, B, C\n"
            ;
        _meta_msg = ""
            + ":type_unmatch       : [/enum] 'A, B, C': not a sequence.\n"
            ;
        doMetaTest();
    }
    public void testRule_EnumNotseq() throws Exception {
        _schema = ""
            + "type: str\n"
            + "enum: A, B, C\n"
            ;
        _rule_msg = ""
            + ":enum_notseq        : [/enum] 'A, B, C': not a sequence.\n"
            ;
        doRuleTest();
    }

    /** enum_notscalar */
    public void testMeta_EnumNotscalar() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "enum:\n"
            + "  - A\n"
            + "  - B\n"
            + "sequence:\n"
            + "  - type: str\n"
            ;
        _meta_msg = ""
            + ":enum_notscalar     : [/] 'enum:': not available with seq or map.\n"
            ;
        doMetaTest();
    }
    public void testRule_EnumNotscalar() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "enum:\n"
            + "  - A\n"
            + "  - B\n"
            + "sequence:\n"
            + "  - type: str\n"
            ;
        _rule_msg = ""
            + ":enum_notscalar     : [/] 'enum:': not available with seq or map.\n"
            ;
        doRuleTest();
    }

    /** enum_duplicate */
    public void testMeta_EnumDuplicate() throws Exception {
        _schema = ""
            + "enum:\n"
            + " - A\n"
            + " - B\n"
            + " - A\n"
            ;
        _meta_msg = ""
            + ":value_notunique    : [/enum/2] 'A': is already used at '/enum/0'.\n"
            ;
        doMetaTest();
    }
    public void testRule_EnumDuplicate() throws Exception {
        _schema = ""
            + "enum:\n"
            + " - A\n"
            + " - B\n"
            + " - A\n"
            ;
        _rule_msg = ""
            + ":enum_duplicate     : [/enum] 'A': duplicated enum value.\n"
            ;
        doRuleTest();
    }

    /** enum_type_unmatch */
    public void testMeta_EnumTypeUnmatch() throws Exception {
        _schema = ""
            + "enum:\n"
            + " - 100\n"
            + " - 200\n"
            ;
        _meta_msg = ""
            + ":enum_type_unmatch  : [/enum] '100': string type expected.\n"
            + ":enum_type_unmatch  : [/enum] '200': string type expected.\n"
            ;
        doMetaTest();
    }
    public void testRule_EnumTypeUnmatch() throws Exception {
        _schema = ""
            + "enum:\n"
            + " - 100\n"
            + " - 200\n"
            ;
        _rule_msg = ""
            + ":enum_type_unmatch  : [/enum] '100': string type expected.\n"
            ;
        doRuleTest();
    }

    /** assert_notstr */
    public void testMeta_AssertNotstr() throws Exception {
        _schema = ""
            + "type: number\n"
            + "assert:  100\n"
            ;
        _meta_msg = ""
            + ":type_unmatch       : [/assert] '100': not a string.\n"
            ;
        doMetaTest();
    }
    public void testRule_AssertNotstr() throws Exception {
        _schema = ""
            + "type: number\n"
            + "assert:  100\n"
            ;
        _rule_msg = ""
            + ":assert_notstr      : [/assert] '100': not a string.\n"
            ;
        doRuleTest();
    }

    /** assert_noval */
    public void testMeta_AssertNoval() throws Exception {
        _schema = ""
            + "type:  int\n"
            + "assert: value > 100\n"
            ;
        _meta_msg = ""
            + ":pattern_unmatch    : [/assert] 'value > 100': not matched to pattern /\\bval\\b/.\n"
            ;
        doMetaTest();
    }
    public void testRule_AssertNoval() throws Exception {
        _schema = ""
            + "type:  int\n"
            + "assert: value > 100\n"
            ;
        _rule_msg = ""
            + ":assert_noval       : [/assert] 'value > 100': 'val' is not used.\n"
            ;
        doRuleTest();
    }

    /** assert_syntaxerr */
    public void testMeta_AssertSyntaxerr() throws Exception {
        _schema = ""
            + "type: int\n"
            + "assert: 0 < val &&\n"
            ;
        _meta_msg = ""
            ;
        doMetaTest();
    }

    /** range_notmap */
    public void testMeta_RangeNotmap() throws Exception {
        _schema = ""
            + "type: int\n"
            + "range: 0 < val && val < 100\n"
            ;
        _meta_msg = ""
            + ":type_unmatch       : [/range] '0 < val && val < 100': not a mapping.\n"
            ;
        doMetaTest();
    }
    public void testRule_RangeNotmap() throws Exception {
        _schema = ""
            + "type: int\n"
            + "range: 0 < val && val < 100\n"
            ;
        _rule_msg = ""
            + ":range_notmap       : [/range] '0 < val && val < 100': not a mapping.\n"
            ;
        doRuleTest();
    }

    /** range_notscalar */
    public void testMeta_RangeNotscalar() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: str\n"
            + "range: { max: 10, min: 10 }\n"
            ;
        _meta_msg = ""
            + ":range_notscalar    : [/] 'range:': is available only with scalar type.\n"
            ;
        doMetaTest();
    }
    public void testRule_RangeNotscalar() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: str\n"
            + "range: { max: 10, min: 10 }\n"
            ;
        _rule_msg = ""
            + ":range_notscalar    : [/] 'range:': is available only with scalar type.\n"
            ;
        doRuleTest();
    }

    /** range_type_unmatch */
    public void testMeta_RangeTypeUnmatch() throws Exception {
        _schema = ""
            + "type:  int\n"
            + "range: { max: 10, min: 1.0 }\n"
            ;
        _meta_msg = ""
            + ":range_type_unmatch : [/range/min] '1.0': not a integer.\n"
            ;
        doMetaTest();
    }
    public void testRule_RangeTypeUnmatch() throws Exception {
        _schema = ""
            + "type:  int\n"
            + "range: { max: 10, min: 1.0 }\n"
            ;
        _rule_msg = ""
            + ":range_type_unmatch : [/range/min] '1.0': not a integer.\n"
            ;
        doRuleTest();
    }

    /** range_undefined */
    public void testMeta_RangeUndefined() throws Exception {
        _schema = ""
            + "type:  int\n"
            + "range: { max: 10, mim: 1 }\n"
            ;
        _meta_msg = ""
            + ":key_undefined      : [/range/mim] key 'mim:' is undefined.\n"
            ;
        doMetaTest();
    }
    public void testRule_RangeUndefined() throws Exception {
        _schema = ""
            + "type:  int\n"
            + "range: { max: 10, mim: 1 }\n"
            ;
        _rule_msg = ""
            + ":range_undefined    : [/range/mim] 'mim:': undefined key.\n"
            ;
        doRuleTest();
    }

    /** range_twomax */
    public void testMeta_RangeTwomax() throws Exception {
        _schema = ""
            + "type:  float\n"
            + "range: { max: 10.0, max-ex: 1.0 }\n"
            ;
        _meta_msg = ""
            + ":range_twomax       : [/range] both 'max' and 'max-ex' are not available at once.\n"
            ;
        doMetaTest();
    }
    public void testRule_RangeTwomax() throws Exception {
        _schema = ""
            + "type:  float\n"
            + "range: { max: 10.0, max-ex: 1.0 }\n"
            ;
        _rule_msg = ""
            + ":range_twomax       : [/range] both 'max' and 'max-ex' are not available at once.\n"
            ;
        doRuleTest();
    }

    /** both 'min' and 'min-ex' are not available at once. */
    public void testMeta_RangeTwomin() throws Exception {
        _schema = ""
            + "type:  float\n"
            + "range: { min: 10.0, min-ex: 1.0 }\n"
            ;
        _meta_msg = ""
            + ":range_twomin       : [/range] both 'min' and 'min-ex' are not available at once.\n"
            ;
        doMetaTest();
    }
    public void testRule_RangeTwomin() throws Exception {
        _schema = ""
            + "type:  float\n"
            + "range: { min: 10.0, min-ex: 1.0 }\n"
            ;
        _rule_msg = ""
            + ":range_twomin       : [/range] both 'min' and 'min-ex' are not available at once.\n"
            ;
        doRuleTest();
    }

    /** 'max:' < 'min:' */
    public void testMeta_RangeMaxltmin() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"EQ\":\n"
            + "    type: int\n"
            + "    range: { max: 10, min: 10 }\n"
            + " \"LG\":\n"
            + "    type:  str\n"
            + "    range: { max: aa, min: xx }\n"
            ;
        _meta_msg = ""
            + ":range_maxltmin     : [/mapping/LG/range] max 'aa' is less than min 'xx'.\n"
            ;
        doMetaTest();
    }
    public void testRule_RangeMaxltmin() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"EQ\":\n"
            + "    type: int\n"
            + "    range: { max: 10, min: 10 }\n"
            + " \"LG\":\n"
            + "    type:  str\n"
            + "    range: { max: aa, min: xx }\n"
            ;
        _rule_msg = ""
            + ":range_maxltmin     : [/mapping/LG/range] max 'aa' is less than min 'xx'.\n"
            ;
        doRuleTest();
    }

    /** 'max:' < 'min-ex:' */
    public void testMeta_RangeMaxleminex1() throws Exception {
        _schema = ""
            + "type: str\n"
            + "range: { max: aa, min-ex: xx }\n"
            ;
        _meta_msg = ""
            + ":range_maxleminex   : [/range] max 'aa' is less than or equal to min-ex 'xx'.\n"
            ;
        doMetaTest();
    }
    public void testRule_RangeMaxleminex1() throws Exception {
        _schema = ""
            + "type: str\n"
            + "range: { max: aa, min-ex: xx }\n"
            ;
        _rule_msg = ""
            + ":range_maxleminex   : [/range] max 'aa' is less than or equal to min-ex 'xx'.\n"
            ;
        doRuleTest();
    }

    /** 'max:' == 'min-ex:' */
    public void testMeta_RangeMaxleminex2() throws Exception {
        _schema = ""
            + "type: int\n"
            + "range: { max: 10, min-ex: 10 }\n"
            ;
        _meta_msg = ""
            + ":range_maxleminex   : [/range] max '10' is less than or equal to min-ex '10'.\n"
            ;
        doMetaTest();
    }
    public void testRule_RangeMaxleminex2() throws Exception {
        _schema = ""
            + "type: int\n"
            + "range: { max: 10, min-ex: 10 }\n"
            ;
        _rule_msg = ""
            + ":range_maxleminex   : [/range] max '10' is less than or equal to min-ex '10'.\n"
            ;
        doRuleTest();
    }

    /** 'max-ex:' < 'min:' */
    public void testMeta_RangeMaxexlemin1() throws Exception {
        _schema = ""
            + "type: str\n"
            + "range: { max-ex: aa, min: xx }\n"
            ;
        _meta_msg = ""
            + ":range_maxexlemin   : [/range] max-ex 'aa' is less than or equal to min 'xx'.\n"
            ;
        doMetaTest();
    }
    public void testRule_RangeMaxexlemin1() throws Exception {
        _schema = ""
            + "type: str\n"
            + "range: { max-ex: aa, min: xx }\n"
            ;
        _rule_msg = ""
            + ":range_maxexlemin   : [/range] max-ex 'aa' is less than or equal to min 'xx'.\n"
            ;
        doRuleTest();
    }

    /** 'max-ex:' == 'min:' */
    public void testMeta_RangeMaxexlemin2() throws Exception {
        _schema = ""
            + "type: int\n"
            + "range: { max-ex: 10, min: 10 }\n"
            ;
        _meta_msg = ""
            + ":range_maxexlemin   : [/range] max-ex '10' is less than or equal to min '10'.\n"
            ;
        doMetaTest();
    }
    public void testRule_RangeMaxexlemin2() throws Exception {
        _schema = ""
            + "type: int\n"
            + "range: { max-ex: 10, min: 10 }\n"
            ;
        _rule_msg = ""
            + ":range_maxexlemin   : [/range] max-ex '10' is less than or equal to min '10'.\n"
            ;
        doRuleTest();
    }

    /** 'max-ex:' < 'min:-ex' */
    public void testMeta_RangeMaxexleminex1() throws Exception {
        _schema = ""
            + "type: str\n"
            + "range: { max-ex: aa, min-ex: xx }\n"
            ;
        _meta_msg = ""
            + ":range_maxexleminex : [/range] max-ex 'aa' is less than or equal to min-ex 'xx'.\n"
            ;
        doMetaTest();
    }
    public void testRule_RangeMaxexleminex1() throws Exception {
        _schema = ""
            + "type: str\n"
            + "range: { max-ex: aa, min-ex: xx }\n"
            ;
        _rule_msg = ""
            + ":range_maxexleminex : [/range] max-ex 'aa' is less than or equal to min-ex 'xx'.\n"
            ;
        doRuleTest();
    }

    /** 'max-ex:' == 'min-ex:' */
    public void testMeta_RangeMaxexleminex2() throws Exception {
        _schema = ""
            + "type: int\n"
            + "range: { max-ex: 10, min-ex: 10 }\n"
            ;
        _meta_msg = ""
            + ":range_maxexleminex : [/range] max-ex '10' is less than or equal to min-ex '10'.\n"
            ;
        doMetaTest();
    }
    public void testRule_RangeMaxexleminex2() throws Exception {
        _schema = ""
            + "type: int\n"
            + "range: { max-ex: 10, min-ex: 10 }\n"
            ;
        _rule_msg = ""
            + ":range_maxexleminex : [/range] max-ex '10' is less than or equal to min-ex '10'.\n"
            ;
        doRuleTest();
    }

    /** length_notmap */
    public void testMeta_LengthNotmap() throws Exception {
        _schema = ""
            + "type:  text\n"
            + "length: [ 10, 4 ]\n"
            ;
        _meta_msg = ""
            + ":type_unmatch       : [/length] not a mapping.\n"
            ;
        doMetaTest();
    }
    public void testRule_LengthNotmap() throws Exception {
        _schema = ""
            + "type:  text\n"
            + "length: [ 10, 4 ]\n"
            ;
        _rule_msg = ""
            + ":length_notmap      : [/length] not a mapping.\n"
            ;
        doRuleTest();
    }

    /** length_nottext */
    public void testMeta_LengthNottext() throws Exception {
        _schema = ""
            + "type: number\n"
            + "length: { max: 10, min: 0 }\n"
            ;
        _meta_msg = ""
            + ":length_nottext     : [/] 'length:': is available only with string or text.\n"
            ;
        doMetaTest();
    }
    public void testRule_LengthNottext() throws Exception {
        _schema = ""
            + "type: number\n"
            + "length: { max: 10, min: 0 }\n"
            ;
        _rule_msg = ""
            + ":length_nottext     : [/] 'length:': is available only with string or text.\n"
            ;
        doRuleTest();
    }

    /** length_notint */
    public void testMeta_LengthNotint() throws Exception {
        _schema = ""
            + "type:  text\n"
            + "length: { max: 10.1 }\n"
            ;
        _meta_msg = ""
            + ":type_unmatch       : [/length/max] '10.1': not a integer.\n"
            ;
        doMetaTest();
    }
    public void testRule_LengthNotint() throws Exception {
        _schema = ""
            + "type:  text\n"
            + "length: { max: 10.1 }\n"
            ;
        _rule_msg = ""
            + ":length_notint      : [/length/max] '10.1': not an integer.\n"
            ;
        doRuleTest();
    }

    /** length_undefined */
    public void testMeta_LengthUndefined() throws Exception {
        _schema = ""
            + "type: text\n"
            + "length: { maximum: 10 }\n"
            ;
        _meta_msg = ""
            + ":key_undefined      : [/length/maximum] key 'maximum:' is undefined.\n"
            ;
        doMetaTest();
    }
    public void testRule_LengthUndefined() throws Exception {
        _schema = ""
            + "type: text\n"
            + "length: { maximum: 10 }\n"
            ;
        _rule_msg = ""
            + ":length_undefined   : [/length/maximum] 'maximum:': undefined key.\n"
            ;
        doRuleTest();
    }

    /** length_twomax */
    public void testMeta_LengthTwomax() throws Exception {
        _schema = ""
            + "type:  text\n"
            + "length: { max: 10, max-ex: 1 }\n"
            ;
        _meta_msg = ""
            + ":length_twomax      : [/length] both 'max' and 'max-ex' are not available at once.\n"
            ;
        doMetaTest();
    }
    public void testRule_LengthTwomax() throws Exception {
        _schema = ""
            + "type:  text\n"
            + "length: { max: 10, max-ex: 1 }\n"
            ;
        _rule_msg = ""
            + ":length_twomax      : [/length] both 'max' and 'max-ex' are not available at once.\n"
            ;
        doRuleTest();
    }

    /** length_twomin */
    public void testMeta_LengthTwomin() throws Exception {
        _schema = ""
            + "type:  str\n"
            + "length: { min: 10, min-ex: 10 }\n"
            ;
        _meta_msg = ""
            + ":length_twomin      : [/length] both 'min' and 'min-ex' are not available at once.\n"
            ;
        doMetaTest();
    }
    public void testRule_LengthTwomin() throws Exception {
        _schema = ""
            + "type:  str\n"
            + "length: { min: 10, min-ex: 10 }\n"
            ;
        _rule_msg = ""
            + ":length_twomin      : [/length] both 'min' and 'min-ex' are not available at once.\n"
            ;
        doRuleTest();
    }

    /** 'max:' < 'min:' */
    public void testMeta_LengthMaxltmin() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"EQ\":\n"
            + "    type: str\n"
            + "    length: { max: 2, min: 2 }\n"
            + " \"LT\":\n"
            + "    type: str\n"
            + "    length: { max: 2, min: 3 }\n"
            ;
        _meta_msg = ""
            + ":length_maxltmin    : [/mapping/LT/length] max '2' is less than min '3'.\n"
            ;
        doMetaTest();
    }
    public void testRule_LengthMaxltmin() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"EQ\":\n"
            + "    type: str\n"
            + "    length: { max: 2, min: 2 }\n"
            + " \"LT\":\n"
            + "    type: str\n"
            + "    length: { max: 2, min: 3 }\n"
            ;
        _rule_msg = ""
            + ":length_maxltmin    : [/mapping/LT/length] max '2' is less than min '3'.\n"
            ;
        doRuleTest();
    }

    /** 'max:' < 'min-ex:' */
    public void testMeta_LengthMaxleminex1() throws Exception {
        _schema = ""
            + "type: str\n"
            + "length: { max: 2, min-ex: 3 }\n"
            ;
        _meta_msg = ""
            + ":length_maxleminex  : [/length] max '2' is less than or equal to min-ex '3'.\n"
            ;
        doMetaTest();
    }
    public void testRule_LengthMaxleminex1() throws Exception {
        _schema = ""
            + "type: str\n"
            + "length: { max: 2, min-ex: 3 }\n"
            ;
        _rule_msg = ""
            + ":length_maxleminex  : [/length] max '2' is less than or equal to min-ex '3'.\n"
            ;
        doRuleTest();
    }

    /** 'max:' == 'min-ex:' */
    public void testMeta_LengthMaxleminex2() throws Exception {
        _schema = ""
            + "type: str\n"
            + "length: { max: 2, min-ex: 2 }\n"
            ;
        _meta_msg = ""
            + ":length_maxleminex  : [/length] max '2' is less than or equal to min-ex '2'.\n"
            ;
        doMetaTest();
    }
    public void testRule_LengthMaxleminex2() throws Exception {
        _schema = ""
            + "type: str\n"
            + "length: { max: 2, min-ex: 2 }\n"
            ;
        _rule_msg = ""
            + ":length_maxleminex  : [/length] max '2' is less than or equal to min-ex '2'.\n"
            ;
        doRuleTest();
    }

    /** 'max-ex:' < 'min:' */
    public void testMeta_LengthMaxexlemin1() throws Exception {
        _schema = ""
            + "type: str\n"
            + "length: { max-ex: 2, min: 3 }\n"
            ;
        _meta_msg = ""
            + ":length_maxexlemin  : [/length] max-ex '2' is less than or equal to min '3'.\n"
            ;
        doMetaTest();
    }
    public void testRule_LengthMaxexlemin1() throws Exception {
        _schema = ""
            + "type: str\n"
            + "length: { max-ex: 2, min: 3 }\n"
            ;
        _rule_msg = ""
            + ":length_maxexlemin  : [/length] max-ex '2' is less than or equal to min '3'.\n"
            ;
        doRuleTest();
    }

    /** 'max-ex:' == 'min:' */
    public void testMeta_LengthMaxexlemin2() throws Exception {
        _schema = ""
            + "type: str\n"
            + "length: { max-ex: 2, min: 2 }\n"
            ;
        _meta_msg = ""
            + ":length_maxexlemin  : [/length] max-ex '2' is less than or equal to min '2'.\n"
            ;
        doMetaTest();
    }
    public void testRule_LengthMaxexlemin2() throws Exception {
        _schema = ""
            + "type: str\n"
            + "length: { max-ex: 2, min: 2 }\n"
            ;
        _rule_msg = ""
            + ":length_maxexlemin  : [/length] max-ex '2' is less than or equal to min '2'.\n"
            ;
        doRuleTest();
    }

    /** 'max-ex:' < 'min-ex:' */
    public void testMeta_LengthMaxexleminex1() throws Exception {
        _schema = ""
            + "type: str\n"
            + "length: { max-ex: 2, min-ex: 3 }\n"
            ;
        _meta_msg = ""
            + ":length_maxexleminex: [/length] max-ex '2' is less than or equal to min-ex '3'.\n"
            ;
        doMetaTest();
    }
    public void testRule_LengthMaxexleminex1() throws Exception {
        _schema = ""
            + "type: str\n"
            + "length: { max-ex: 2, min-ex: 3 }\n"
            ;
        _rule_msg = ""
            + ":length_maxexleminex: [/length] max-ex '2' is less than or equal to min-ex '3'.\n"
            ;
        doRuleTest();
    }

    /** 'max-ex:' == 'min-ex:' */
    public void testMeta_LengthMaxexltminex2() throws Exception {
        _schema = ""
            + "type: str\n"
            + "length: { max-ex: 2, min-ex: 2 }\n"
            ;
        _meta_msg = ""
            + ":length_maxexleminex: [/length] max-ex '2' is less than or equal to min-ex '2'.\n"
            ;
        doMetaTest();
    }
    public void testRule_LengthMaxexltminex2() throws Exception {
        _schema = ""
            + "type: str\n"
            + "length: { max-ex: 2, min-ex: 2 }\n"
            ;
        _rule_msg = ""
            + ":length_maxexleminex: [/length] max-ex '2' is less than or equal to min-ex '2'.\n"
            ;
        doRuleTest();
    }

    /** sequence_notseq */
    public void testMeta_SequenceNotseq() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence: |-\n"
            + "  - type: str\n"
            ;
        _meta_msg = ""
            + ":type_unmatch       : [/sequence] '- type: str': not a sequence.\n"
            ;
        doMetaTest();
    }
    public void testRule_SequenceNotseq() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence: |-\n"
            + "  - type: str\n"
            ;
        _rule_msg = ""
            + ":sequence_notseq    : [/sequence] '- type: str': not a sequence.\n"
            ;
        doRuleTest();
    }

    /** sequence_noelem */
    public void testMeta_SequenceNoelem() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            ;
        _meta_msg = ""
            + ":sequence_noelem    : [/sequence] required one element.\n"
            ;
        doMetaTest();
    }
    public void testRule_SequenceNoelem() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            ;
        _rule_msg = ""
            + ":sequence_noelem    : [/sequence] required one element.\n"
            ;
        doRuleTest();
    }

    /** sequence_toomany */
    public void testMeta_SequenceToomany() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: text\n"
            + "  - type: text\n"
            ;
        _meta_msg = ""
            + ":sequence_toomany   : [/sequence] required just one element.\n"
            ;
        doMetaTest();
    }
    public void testRule_SequenceToomany() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: text\n"
            + "  - type: text\n"
            ;
        _rule_msg = ""
            + ":sequence_toomany   : [/sequence] required just one element.\n"
            ;
        doRuleTest();
    }

    /** mapping_notmap */
    public void testMeta_MappingNotmap() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping: |-\n"
            + " \"name\":\n"
            + "    type: str\n"
            ;
        _meta_msg = ""
            + ":type_unmatch       : [/mapping] '\"name\":\\n   type: str': not a mapping.\n"
            ;
        doMetaTest();
    }
    public void testRule_MappingNotmap() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping: |-\n"
            + " \"name\":\n"
            + "    type: str\n"
            ;
        _rule_msg = ""
            + ":mapping_notmap     : [/mapping] '\"name\":\\n   type: str': not a mapping.\n"
            ;
        doRuleTest();
    }

    /** mapping_noelem */
    public void testMeta_MappingNoelem() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            ;
        _meta_msg = ""
            + ":mapping_noelem     : [/mapping] required at least one element.\n"
            ;
        doMetaTest();
    }
    public void testRule_MappingNoelem() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            ;
        _rule_msg = ""
            + ":mapping_noelem     : [/mapping] required at least one element.\n"
            ;
        doRuleTest();
    }

    /** key_unknown */
    public void testMeta_KeyUnknown() throws Exception {
        _schema = ""
            + "type: str\n"
            + "values:\n"
            + "  - one\n"
            + "  - two\n"
            ;
        _meta_msg = ""
            + ":key_undefined      : [/values] key 'values:' is undefined.\n"
            ;
        doMetaTest();
    }
    public void testRule_KeyUnknown() throws Exception {
        _schema = ""
            + "type: str\n"
            + "values:\n"
            + "  - one\n"
            + "  - two\n"
            ;
        _rule_msg = ""
            + ":key_unknown        : [/values] 'values:': unknown key.\n"
            ;
        doRuleTest();
    }

    /** seq_nosequence */
    public void testMeta_SeqNosequence() throws Exception {
        _schema = ""
            + "type: seq\n"
            ;
        _meta_msg = ""
            + ":seq_nosequence     : [/] type 'seq' requires 'sequence:'.\n"
            ;
        doMetaTest();
    }
    public void testRule_SeqNosequence() throws Exception {
        _schema = ""
            + "type: seq\n"
            ;
        _rule_msg = ""
            + ":seq_nosequence     : [/] type 'seq' requires 'sequence:'.\n"
            ;
        doRuleTest();
    }

    /** type 'seq' and item 'mapping:' */
    public void testMeta_SeqConflict1() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: str\n"
            + "mapping: { name: {type: str} }\n"
            ;
        _meta_msg = ""
            + ":seq_conflict       : [/] 'mapping:': not available with sequence.\n"
            ;
        doMetaTest();
    }
    public void testRule_SeqConflict1() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: str\n"
            + "mapping: { name: {type: str} }\n"
            ;
        _rule_msg = ""
            + ":seq_conflict       : [/] 'mapping:': not available with sequence.\n"
            ;
        doRuleTest();
    }

    /** type 'seq' and item 'pattern:' */
    public void testMeta_SeqConflict2() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: str\n"
            + "pattern: /abc/\n"
            ;
        _meta_msg = ""
            + ":seq_conflict       : [/] 'pattern:': not available with sequence.\n"
            ;
        doMetaTest();
    }
    public void testRule_SeqConflict2() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: str\n"
            + "pattern: /abc/\n"
            ;
        _rule_msg = ""
            + ":seq_conflict       : [/] 'pattern:': not available with sequence.\n"
            ;
        doRuleTest();
    }

    /** type 'seq' and item 'range:' */
    public void testMeta_SeqConflict3() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: str\n"
            + "range:  { max: 10 }\n"
            ;
        _meta_msg = ""
            + ":range_notscalar    : [/] 'range:': is available only with scalar type.\n"
            ;
        doMetaTest();
    }
    public void testRule_SeqConflict3() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: str\n"
            + "range:  { max: 10 }\n"
            ;
        _rule_msg = ""
            + ":range_notscalar    : [/] 'range:': is available only with scalar type.\n"
            ;
        doRuleTest();
    }

    /** type 'seq' and item 'length:' */
    public void testMeta_SeqConflict4() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: str\n"
            + "length: { max: 10 }\n"
            ;
        _meta_msg = ""
            + ":length_nottext     : [/] 'length:': is available only with string or text.\n"
            ;
        doMetaTest();
    }
    public void testRule_SeqConflict4() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: str\n"
            + "length: { max: 10 }\n"
            ;
        _rule_msg = ""
            + ":length_nottext     : [/] 'length:': is available only with string or text.\n"
            ;
        doRuleTest();
    }

    /** type 'seq' and item 'enum:' */
    public void testMeta_SeqConflict5() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: str\n"
            + "enum: [ A, B, C ]\n"
            ;
        _meta_msg = ""
            + ":enum_notscalar     : [/] 'enum:': not available with seq or map.\n"
            ;
        doMetaTest();
    }
    public void testRule_SeqConflict5() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: str\n"
            + "enum: [ A, B, C ]\n"
            ;
        _rule_msg = ""
            + ":enum_notscalar     : [/] 'enum:': not available with seq or map.\n"
            ;
        doRuleTest();
    }

    /** map_nomapping */
    public void testMeta_MapNomapping() throws Exception {
        _schema = ""
            + "type: map\n"
            ;
        _meta_msg = ""
            + ":map_nomapping      : [/] type 'map' requires 'mapping:'.\n"
            ;
        doMetaTest();
    }
    public void testRule_MapNomapping() throws Exception {
        _schema = ""
            + "type: map\n"
            ;
        _rule_msg = ""
            + ":map_nomapping      : [/] type 'map' requires 'mapping:'.\n"
            ;
        doRuleTest();
    }

    /** type 'map' and item 'sequence:' */
    public void testMeta_MapConflict1() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"name\":\n"
            + "    type: str\n"
            + "sequence:\n"
            + "  - type: str\n"
            ;
        _meta_msg = ""
            + ":map_conflict       : [/] 'sequence:': not available with mapping.\n"
            ;
        doMetaTest();
    }
    public void testRule_MapConflict1() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"name\":\n"
            + "    type: str\n"
            + "sequence:\n"
            + "  - type: str\n"
            ;
        _rule_msg = ""
            + ":map_conflict       : [/] 'sequence:': not available with mapping.\n"
            ;
        doRuleTest();
    }

    /** type 'map' and item 'pattern:' */
    public void testMeta_MapConflict2() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"name\":\n"
            + "    type: str\n"
            + "pattern: /foo/\n"
            ;
        _meta_msg = ""
            + ":map_conflict       : [/] 'pattern:': not available with mapping.\n"
            ;
        doMetaTest();
    }
    public void testRule_MapConflict2() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"name\":\n"
            + "    type: str\n"
            + "pattern: /foo/\n"
            ;
        _rule_msg = ""
            + ":map_conflict       : [/] 'pattern:': not available with mapping.\n"
            ;
        doRuleTest();
    }

    /** type 'map' and item 'range:' */
    public void testMeta_MapConflict3() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"name\":\n"
            + "    type: str\n"
            + "range: { min-ex: 5 }\n"
            ;
        _meta_msg = ""
            + ":range_notscalar    : [/] 'range:': is available only with scalar type.\n"
            ;
        doMetaTest();
    }
    public void testRule_MapConflict3() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"name\":\n"
            + "    type: str\n"
            + "range: { min-ex: 5 }\n"
            ;
        _rule_msg = ""
            + ":range_notscalar    : [/] 'range:': is available only with scalar type.\n"
            ;
        doRuleTest();
    }

    /** type 'map' and item 'length:' */
    public void testMeta_MapConflict4() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"name\":\n"
            + "    type: str\n"
            + "length: { min-ex: 5 }\n"
            ;
        _meta_msg = ""
            + ":length_nottext     : [/] 'length:': is available only with string or text.\n"
            ;
        doMetaTest();
    }
    public void testRule_MapConflict4() throws Exception {
        _schema = ""
            + "type: map\n"
            + "mapping:\n"
            + " \"name\":\n"
            + "    type: str\n"
            + "length: { min-ex: 5 }\n"
            ;
        _rule_msg = ""
            + ":length_nottext     : [/] 'length:': is available only with string or text.\n"
            ;
        doRuleTest();
    }

    /** scalar type and item 'sequence:' */
    public void testMeta_ScalarConfict1() throws Exception {
        _schema = ""
            + "type:  int\n"
            + "sequence:\n"
            + "  - type: str\n"
            ;
        _meta_msg = ""
            + ":scalar_conflict    : [/] 'sequence:': not available with scalar type.\n"
            ;
        doMetaTest();
    }
    public void testRule_ScalarConfict1() throws Exception {
        _schema = ""
            + "type:  int\n"
            + "sequence:\n"
            + "  - type: str\n"
            ;
        _rule_msg = ""
            + ":scalar_conflict    : [/] 'sequence:': not available with scalar type.\n"
            ;
        doRuleTest();
    }

    /** scalar type and item 'mapping:' */
    public void testMeta_ScalarConfict2() throws Exception {
        _schema = ""
            + "type:  int\n"
            + "mapping:\n"
            + " \"name\":\n"
            + "    type: str\n"
            ;
        _meta_msg = ""
            + ":scalar_conflict    : [/] 'mapping:': not available with scalar type.\n"
            ;
        doMetaTest();
    }
    public void testRule_ScalarConfict2() throws Exception {
        _schema = ""
            + "type:  int\n"
            + "mapping:\n"
            + " \"name\":\n"
            + "    type: str\n"
            ;
        _rule_msg = ""
            + ":scalar_conflict    : [/] 'mapping:': not available with scalar type.\n"
            ;
        doRuleTest();
    }

    /** item 'enum:' and 'range:' */
    public void testMeta_EnumConflict1() throws Exception {
        _schema = ""
            + "type: str\n"
            + "enum:\n"
            + "  - aa\n"
            + "  - bb\n"
            + "range: { max: xx, min: aa }\n"
            ;
        _meta_msg = ""
            + ":enum_conflict      : [/] 'range:': not available with 'enum:'.\n"
            ;
        doMetaTest();
    }
    public void testRule_EnumConflict1() throws Exception {
        _schema = ""
            + "type: str\n"
            + "enum:\n"
            + "  - aa\n"
            + "  - bb\n"
            + "range: { max: xx, min: aa }\n"
            ;
        _rule_msg = ""
            + ":enum_conflict      : [/] 'range:': not available with 'enum:'.\n"
            ;
        doRuleTest();
    }

    /** item 'enum:' and 'length:' */
    public void testMeta_EnumConflict2() throws Exception {
        _schema = ""
            + "type: str\n"
            + "enum:\n"
            + "  - aa\n"
            + "  - bb\n"
            + "length: { max: 3 }\n"
            ;
        _meta_msg = ""
            + ":enum_conflict      : [/] 'length:': not available with 'enum:'.\n"
            ;
        doMetaTest();
    }
    public void testRule_EnumConflict2() throws Exception {
        _schema = ""
            + "type: str\n"
            + "enum:\n"
            + "  - aa\n"
            + "  - bb\n"
            + "length: { max: 3 }\n"
            ;
        _rule_msg = ""
            + ":enum_conflict      : [/] 'length:': not available with 'enum:'.\n"
            ;
        doRuleTest();
    }

    /** item 'enum:' and 'pattern:' */
    public void testMeta_EnumConflict3() throws Exception {
        _schema = ""
            + "type: str\n"
            + "enum:\n"
            + "  - aa\n"
            + "  - bb\n"
            + "pattern: /0/\n"
            ;
        _meta_msg = ""
            + ":enum_conflict      : [/] 'pattern:': not available with 'enum:'.\n"
            ;
        doMetaTest();
    }
    public void testRule_EnumConflict3() throws Exception {
        _schema = ""
            + "type: str\n"
            + "enum:\n"
            + "  - aa\n"
            + "  - bb\n"
            + "pattern: /0/\n"
            ;
        _rule_msg = ""
            + ":enum_conflict      : [/] 'pattern:': not available with 'enum:'.\n"
            ;
        doRuleTest();
    }

    /** unique_notbool */
    public void testMeta_UniqueNotbool() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: map\n"
            + "    mapping:\n"
            + "     \"id\":\n"
            + "        type:    int\n"
            + "        unique:  'yes'\n"
            + "     \"name\":\n"
            + "        type:    str\n"
            ;
        _meta_msg = ""
            + ":type_unmatch       : [/sequence/0/mapping/id/unique] 'yes': not a boolean.\n"
            ;
        doMetaTest();
    }
    public void testRule_UniqueNotbool() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: map\n"
            + "    mapping:\n"
            + "     \"id\":\n"
            + "        type:    int\n"
            + "        unique:  'yes'\n"
            + "     \"name\":\n"
            + "        type:    str\n"
            ;
        _rule_msg = ""
            + ":unique_notbool     : [/sequence/0/mapping/id/unique] 'yes': not a boolean.\n"
            ;
        doRuleTest();
    }

    /** unique_notscalar */
    public void testMeta_UniqueNotscalar() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: map\n"
            + "    mapping:\n"
            + "     \"id\":\n"
            + "        type:    int\n"
            + "        unique:  yes\n"
            + "     \"values\":\n"
            + "        type:    seq\n"
            + "        unique:  yes\n"
            + "        sequence:\n"
            + "          - type: str\n"
            ;
        _meta_msg = ""
            + ":unique_notscalar   : [/sequence/0/mapping/values] 'unique:': is available only with a scalar type.\n"
            ;
        doMetaTest();
    }
    public void testRule_UniqueNotscalar() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: map\n"
            + "    mapping:\n"
            + "     \"id\":\n"
            + "        type:    int\n"
            + "        unique:  yes\n"
            + "     \"values\":\n"
            + "        type:    seq\n"
            + "        unique:  yes\n"
            + "        sequence:\n"
            + "          - type: str\n"
            ;
        _rule_msg = ""
            + ":unique_notscalar   : [/sequence/0/mapping/values] 'unique:': is available only with a scalar type.\n"
            ;
        doRuleTest();
    }

    /** unique_root */
    public void testMeta_UniqueRoot() throws Exception {
        _schema = ""
            + "type: str\n"
            + "unique: yes\n"
            ;
        _meta_msg = ""
            + ":unique_onroot      : [/] 'unique:': is not available on root element.\n"
            ;
        doMetaTest();
    }
    public void testRule_UniqueRoot() throws Exception {
        _schema = ""
            + "type: str\n"
            + "unique: yes\n"
            ;
        _rule_msg = ""
            + ":unique_onroot      : [/] 'unique:': is not available on root element.\n"
            ;
        doRuleTest();
    }

    /** ident_notbool */
    public void testMeta_IdentNotbool() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: map\n"
            + "    mapping:\n"
            + "     \"id\":\n"
            + "        type:    int\n"
            + "        ident:  'yes'\n"
            + "     \"name\":\n"
            + "        type:    str\n"
            ;
        _meta_msg = ""
            + ":type_unmatch       : [/sequence/0/mapping/id/ident] 'yes': not a boolean.\n"
            ;
        doMetaTest();
    }
    public void testRule_IdentNotbool() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: map\n"
            + "    mapping:\n"
            + "     \"id\":\n"
            + "        type:    int\n"
            + "        ident:  'yes'\n"
            + "     \"name\":\n"
            + "        type:    str\n"
            ;
        _rule_msg = ""
            + ":ident_notbool      : [/sequence/0/mapping/id/ident] 'yes': not a boolean.\n"
            ;
        doRuleTest();
    }

    /** ident_notscalar */
    public void testMeta_IdentNotscalar() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: map\n"
            + "    mapping:\n"
            + "     \"id\":\n"
            + "        type:    int\n"
            + "        ident:  yes\n"
            + "     \"values\":\n"
            + "        type:    seq\n"
            + "        ident:  yes\n"
            + "        sequence:\n"
            + "          - type: str\n"
            ;
        _meta_msg = ""
            + ":ident_notscalar    : [/sequence/0/mapping/values] 'ident:': is available only with a scalar type.\n"
            ;
        doMetaTest();
    }
    public void testRule_IdentNotscalar() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: map\n"
            + "    mapping:\n"
            + "     \"id\":\n"
            + "        type:    int\n"
            + "        ident:  yes\n"
            + "     \"values\":\n"
            + "        type:    seq\n"
            + "        ident:  yes\n"
            + "        sequence:\n"
            + "          - type: str\n"
            ;
        _rule_msg = ""
            + ":ident_notscalar    : [/sequence/0/mapping/values] 'ident:': is available only with a scalar type.\n"
            ;
        doRuleTest();
    }

    /** ident_root */
    public void testMeta_IdentRoot() throws Exception {
        _schema = ""
            + "type: str\n"
            + "ident: yes\n"
            ;
        _meta_msg = ""
            + ":ident_onroot       : [/] 'ident:': is not available on root element.\n"
            ;
        doMetaTest();
    }
    public void testRule_IdentRoot() throws Exception {
        _schema = ""
            + "type: str\n"
            + "ident: yes\n"
            ;
        _rule_msg = ""
            + ":ident_onroot       : [/] 'ident:': is not available on root element.\n"
            ;
        doRuleTest();
    }

    /** ident_notmap */
    public void testMeta_IdentNotmap() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: map\n"
            + "    mapping:\n"
            + "     \"id\":\n"
            + "        type:    int\n"
            + "        ident:  yes\n"
            + "     \"values\":\n"
            + "        type:    seq\n"
            + "        sequence:\n"
            + "          - type: str\n"
            + "            ident: yes\n"
            ;
        _meta_msg = ""
            + ":ident_notmap       : [/sequence/0/mapping/values/sequence/0] 'ident:': is available only with an element of mapping.\n"
            ;
        doMetaTest();
    }
    public void testRule_IdentNotmap() throws Exception {
        _schema = ""
            + "type: seq\n"
            + "sequence:\n"
            + "  - type: map\n"
            + "    mapping:\n"
            + "     \"id\":\n"
            + "        type:    int\n"
            + "        ident:  yes\n"
            + "     \"values\":\n"
            + "        type:    seq\n"
            + "        sequence:\n"
            + "          - type: str\n"
            + "            ident: yes\n"
            ;
        _rule_msg = ""
            + ":ident_notmap       : [/sequence/0/mapping/values/sequence/0] 'ident:': is available only with an element of mapping.\n"
            ;
        doRuleTest();
    }

    //-----

}
