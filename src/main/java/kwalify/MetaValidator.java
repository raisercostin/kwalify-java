/*
 * @(#)MetaValidator.java	$Rev: 4 $ $Release: 0.5.1 $
 *
 * copyright(c) 2005 kuwata-lab all rights reserved.
 */

package kwalify;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 *  meta validator to validate schema definition
 *
 *  @revision    $Rev: 4 $
 *  @release     $Release: 0.5.1 $
 */
public class MetaValidator extends Validator {

    public static final String META_SCHEMA = ""
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


    /**
     *
     * ex.
     * <pre>
     *  MetaValidator meta_validator = MetaValidator();
     *  Map schema = YamlUtil.loadFile("schema.yaml");
     *  List errors = meta_validator.validate(schema);
     *  if (errors != null && errors.size() > 0) {
     *    for (Iterator it = errors.iterator(); it.hasNext(); ) {
     *      ValidationException error = (ValidationException)it.next();
     *      System.err.println(" - [" + error.getPath() + "] " + error.getMessage());
     *    }
     *  }
     * </pre>
     */

    private static Validator __instance;

    public static Validator instance() {
        // should not use double checked pattern?
        // but it would work well because __instance is read-only.
        if (__instance == null) {
            synchronized (MetaValidator.class) {
                if (__instance == null) {
                    try {
                        Map schema = (Map)YamlUtil.load(META_SCHEMA);
                        __instance = new MetaValidator(schema);
                    } catch (SyntaxException ex) {
                        assert false;
                    }
                }
            }
        }
        return __instance;
    }

    private MetaValidator(Map schema) {
        super(schema);
    }

    public void validateHook(Object value, Rule rule, String path, List errors) {
        if (value == null) {
            return;   // realy?
        }
        if (! "MAIN".equals(rule.getName())) {
            return;
        }
        //
        assert value instanceof Map;
        Map map = (Map)value;
        String type = (String)map.get("type");
        if (type == null) {
            type = Types.getDefaultType();
        }
        //Class type_class = Types.typeClass(type);
        //if (type_class == null) {
        //    errors.add(validationError("type.unknown", rule, path + "/type", type, null));
        //}
        //
        //String pattern;
        //if ((pattern = (String)map.get("pattern")) != null) {
        if (map.containsKey("pattern")) {
            String pattern = (String)map.get("pattern");
            Matcher m = Util.matcher(pattern, "\\A\\/(.*)\\/([mi]?[mi]?)\\z");
            String pat = m.find() ? m.group(1) : pattern;
            try {
                Pattern.compile(pat);
            } catch (PatternSyntaxException ex) {
                errors.add(validationError("pattern.syntaxerr", rule, path + "/pattern", pattern, null));
            }
        }
        //
        //List enum_list;
        //if ((enum_list = (List)map.get("enum")) != null) {
        if (map.containsKey("enum")) {
            List enum_list = (List)map.get("enum");
            if (Types.isCollectionType(type)) {
                errors.add(validationError("enum.notscalar", rule, path, "enum:", null));
            } else {
                for (Iterator it = enum_list.iterator(); it.hasNext(); ) {
                    Object elem = it.next();
                    if (! Types.isCorrectType(elem, type)) {
                        errors.add(validationError("enum.type.unmatch", rule, path + "/enum", elem, new Object[] { Types.typeName(type) }));
                    }
                }
            }
        }
        //
        //String assert_str;
        //if ((assert_str = (String)map.get("assert")) != null) {
        if (map.containsKey("assert")) {
            System.err.println("*** warning: sorry, 'assert:' is not supported in current version of Kwalify-java.");
            //String assert_str = (String)map.get("assert");
            //if (! Util.matches(assert_str, "\\bval\\b")) {
            //    errors.add(validationError("assert.noval", rule, path + "/assert", assert_str, null);
            //}
            //try {
            //    Expression.parse(assert_str);
            //} catch (InvalidExpressionException ex) {
            //    errors.add(validationError("assert.syntaxerr", rule, path + "/assert", assert_str, null));
            //}
        }
        //
        //Map range;
        //if ((range = (Map)map.get("range")) != null) {
        if (map.containsKey("range")) {
            Map range = (Map)map.get("range");
            //if (! (range instanceof Map)) {
            //    errors.add(validtionError("range.notmap", rule, path + "/range", range, null));
            //} else
            if (Types.isCollectionType(type) || type.equals("bool") || type.equals("any")) {
                errors.add(validationError("range.notscalar", rule, path, "range:", null));
            } else {
                for (Iterator it = range.keySet().iterator(); it.hasNext(); ) {
                    String k = (String)it.next();
                    Object v = range.get(k);
                    if (! Types.isCorrectType(v, type)) {
                        errors.add(validationError("range.type.unmatch", rule, path + "/range/" + k, v, new Object[] { Types.typeName(type) }));
                    }
                }
            }
            if (range.containsKey("max") && range.containsKey("max-ex")) {
                errors.add(validationError("range.twomax", rule, path + "/range", null, null));
            }
            if (range.containsKey("min") && range.containsKey("min-ex")) {
                errors.add(validationError("range.twomin", rule, path + "/range", null, null));
            }
            Object max    = range.get("max");
            Object min    = range.get("min");
            Object max_ex = range.get("max-ex");
            Object min_ex = range.get("min-ex");
            Object[] args = null;
            //String error_symbol = null;
            if (max != null) {
                if (min != null && Util.compareValues(max, min) < 0) {
                    args = new Object[] { max, min };
                    errors.add(validationError("range.maxltmin", rule, path + "/range", null, args));
                } else if (min_ex != null && Util.compareValues(max, min_ex) <= 0) {
                    args = new Object[] { max, min_ex };
                    errors.add(validationError("range.maxleminex", rule, path + "/range", null, args));
                }
            } else if (max_ex != null) {
                if (min != null && Util.compareValues(max_ex, min) <= 0) {
                    args = new Object[] { max_ex, min };
                    errors.add(validationError("range.maxexlemin", rule, path + "/range", null, args));
                } else if (min_ex != null && Util.compareValues(max_ex, min_ex) <= 0) {
                    args = new Object[] { max_ex, min_ex };
                    errors.add(validationError("range.maxexleminex", rule, path + "/range", null, args));
                }
            }
        }
        //
        //Map length;
        //if ((length = (Map)map.get("length")) != null) {
        if (map.containsKey("length")) {
            Map length = (Map)map.get("length");
            //if (! (length instanceof Map)) {
            //    errors.add(validtionError("length.notmap", rule, path + "/length", length, null));
            //} else
            if (! (type.equals("str") || type.equals("text"))) {
                errors.add(validationError("length.nottext", rule, path, "length:", null));
            }
            //for (Iterator it = length.keySet().iterator(); it.hasNext(); ) {
            //    String k = (String)it.next();
            //    Object v = length.get(k);
            //    if (k == null || ! (k.equals("max") || k.equals("min") || k.equals("max-ex") || k.equals("min-ex"))) {
            //        errors.add(validationError("length.undefined", rule, path + "/length/" + k, "" + k + ":", null));
            //    } else if (! (v instanceof Integer)) {
            //        errors.add(validationError("length.notint", rule, path + "/length/" + k, v, null));
            //    }
            //}
            if (length.containsKey("max") && length.containsKey("max-ex")) {
                errors.add(validationError("length.twomax", rule, path + "/length", null, null));
            }
            if (length.containsKey("min") && length.containsKey("min-ex")) {
                errors.add(validationError("length.twomin", rule, path + "/length", null, null));
            }
            Integer max    = (Integer)length.get("max");
            Integer min    = (Integer)length.get("min");
            Integer max_ex = (Integer)length.get("max-ex");
            Integer min_ex = (Integer)length.get("min-ex");
            Object[] args = null;
            //String error_symbol = null;
            if (max != null) {
                if (min != null && max.compareTo(min) < 0) {
                    args = new Object[] { max, min };
                    errors.add(validationError("length.maxltmin", rule, path + "/length", null, args));
                } else if (min_ex != null && max.compareTo(min_ex) <= 0) {
                    args = new Object[] { max, min_ex };
                    errors.add(validationError("length.maxleminex", rule, path + "/length", null, args));
                }
            } else if (max_ex != null) {
                if (min != null && max_ex.compareTo(min) <= 0) {
                    args = new Object[] { max_ex, min };
                    errors.add(validationError("length.maxexlemin", rule, path + "/length", null, args));
                } else if (min_ex != null && max_ex.compareTo(min_ex) <= 0) {
                    args = new Object[] { max_ex, min_ex };
                    errors.add(validationError("length.maxexleminex", rule, path + "/length", null, args));
                }
            }
        }
        //
        //Boolean unique;
        //if ((unique = (Boolean)map.get("unique")) != null) {
        if (map.containsKey("unique")) {
            Boolean unique = (Boolean)map.get("unique");
            if (unique.booleanValue() == true && Types.isCollectionType(type)) {
                errors.add(validationError("unique.notscalar", rule, path, "unique:", null));
            }
            if (path.length() == 0) {
                errors.add(validationError("unique.onroot", rule, "/", "unique:", null));
            }
        }
        //
        //Boolean ident;
        //if ((ident = (Boolean)map.get("ident")) != null) {
        if (map.containsKey("ident")) {
            Boolean ident = (Boolean)map.get("ident");
            if (ident.booleanValue() == true && Types.isCollectionType(type)) {
                errors.add(validationError("ident.notscalar", rule, path, "ident:", null));
            }
            if (path.length() == 0) {
                errors.add(validationError("ident.onroot", rule, "/", "ident:", null));
            }
        }
        //
        //List seq;
        //if ((seq = (List)map.get("sequence")) != null) {
        if (map.containsKey("sequence")) {
            List seq = (List)map.get("sequence");
            //if (! (seq instanceof List)) {
            //    errors.add(validationError("sequence.notseq", rule, path + "/sequence", seq, null));
            //} else
            if (seq == null || seq.size() == 0) {
                errors.add(validationError("sequence.noelem", rule, path + "/sequence", seq, null));
            } else if (seq.size() > 1) {
                errors.add(validationError("sequence.toomany", rule, path + "/sequence", seq, null));
            } else {
                Object item = seq.get(0);
                assert item instanceof Map;
                Map m = (Map)item;
                Boolean ident2 = (Boolean)m.get("ident");
                if (ident2 != null && ident2.booleanValue() == true && ! "map".equals(m.get("type"))) {
                    errors.add(validationError("ident.notmap", null, path + "/sequence/0", "ident:", null));
                }
            }
        }
        //
        //Map mapping;
        //if ((mapping = (Map)map.get("mapping")) != null) {
        if (map.containsKey("mapping")) {
            Map mapping = (Map)map.get("mapping");
            //if (mapping != null && ! (mapping instanceof Map)) {
            //    errors.add(validationError("mapping.notmap", rule, path + "/mapping", mapping, null));
            //} else
            Object default_value = null;
            if (mapping != null && mapping instanceof Defaultable) {
                default_value = ((Defaultable)mapping).getDefault();
            }
            if (mapping == null || (mapping.size() == 0 && default_value == null)) {
                errors.add(validationError("mapping.noelem", rule, path + "/mapping", mapping, null));
            }
        }
        //
        if (type.equals("seq")) {
            if (! map.containsKey("sequence")) {
                errors.add(validationError("seq.nosequence", rule, path, null, null));
            }
            //if (map.containsKey("enum")) {
            //    errors.add(validationError("seq.conflict", rule, path, "enum:", null));
            //}
            if (map.containsKey("pattern")) {
                errors.add(validationError("seq.conflict", rule, path, "pattern:", null));
            }
            if (map.containsKey("mapping")) {
                errors.add(validationError("seq.conflict", rule, path, "mapping:", null));
            }
            //if (map.containsKey("range")) {
            //    errors.add(validationError("seq.conflict", rule, path, "range:", null));
            //}
            //if (map.containsKey("length")) {
            //    errors.add(validationError("seq.conflict", rule, path, "length:", null));
            //}
        } else if (type.equals("map")) {
            if (! map.containsKey("mapping")) {
                errors.add(validationError("map.nomapping", rule, path, null, null));
            }
            //if (map.containsKey("enum")) {
            //    errors.add(validationError("map.conflict", rule, path, "enum:", null));
            //}
            if (map.containsKey("pattern")) {
                errors.add(validationError("map.conflict", rule, path, "pattern:", null));
            }
            if (map.containsKey("sequence")) {
                errors.add(validationError("map.conflict", rule, path, "sequence:", null));
            }
            //if (map.containsKey("range")) {
            //    errors.add(validationError("map.conflict", rule, path, "range:", null));
            //}
            //if (map.containsKey("length")) {
            //    errors.add(validationError("map.conflict", rule, path, "length:", null));
            //}
        } else {
            if (map.containsKey("sequence")) {
                errors.add(validationError("scalar.conflict", rule, path, "sequence:", null));
            }
            if (map.containsKey("mapping")) {
                errors.add(validationError("scalar.conflict", rule, path, "mapping:", null));
            }
            if (map.containsKey("enum")) {
                if (map.containsKey("range")) {
                    errors.add(validationError("enum.conflict", rule, path, "range:", null));
                }
                if (map.containsKey("length")) {
                    errors.add(validationError("enum.conflict", rule, path, "length:", null));
                }
                if (map.containsKey("pattern")) {
                    errors.add(validationError("enum.conflict", rule, path, "pattern:", null));
                }
            }
        }
    }

/*
    public static void main(String[] args) {
        try {
            // parse schema
            String filename = args.length > 0 ? args[0] : "schema.yaml";
            String schema_str = Util.readFile(filename);
            YamlParser parser = new YamlParser(schema_str);
            Object schema = parser.parse();
            
            // validate schema
            Validator meta_validator = MetaValidator.instance();
            List errors = meta_validator.validate(schema);
            
            // show errors
            if (errors != null && errors.size() > 0) {
                parser.setErrorsLineNumber(errors);
                for (Iterator it = errors.iterator(); it.hasNext(); ) {
                    ValidationException error = (ValidationException)it.next();
                    int linenum = error.getLineNumber();
                    String path = error.getPath();
                    String msg  = error.getMessage();
                    System.out.println("- line " + linenum + ": [" + path + "] " + msg);
                }
            } else {
                System.out.println("meta validation: OK.");
            }
        } catch (SyntaxException ex) {
            ex.printStackTrace();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }
*/

}
