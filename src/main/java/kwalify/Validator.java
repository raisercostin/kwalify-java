/*
 * @(#)Validator.java	$Rev: 3 $ $Release: 0.5.1 $
 *
 * copyright(c) 2005 kuwata-lab all rights reserved.
 */

package kwalify;

import java.util.Map;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *  validation engine
 *
 *  ex.
 *  <pre>
 *
 *    // load YAML document
 *    String str = Util.readFile("document.yaml");
 *    YamlParser parser = new YamlParser(str);
 *    Object document = parser.parse();
 *
 *    // load schema
 *    Object schema = YamlUtil.loadFile("schema.yaml");
 *
 *    // generate validator and validate document
 *    Validator validator = new Validator(shema);
 *    List errors = validator.validate(document);
 *
 *    // show errors
 *    if (errors != null && errors.size() > 0) {
 *        parser.setErrorsLineNumber(errors);
 *        java.util.Collections.sort(errors);
 *        for (Iterator it = errors.iterator(); it.hasNext(); ) {
 *            ValidationError error = (ValidationError)it.next();
 *            int linenum = error.getLineNumber();
 *            String path = error.getPath();
 *            String mesg = error.getMessage();
 *            String s = "- (" + linenum + ") [" + path + "] " + mesg;
 *            System.err.println(s);
 *        }
 *    }
 *  </pre>
 *
 *  @version   $Rev: 3 $
 *  @release   $Release: 0.5.1 $
 */
public class Validator {
    private Rule _rule;

    public Validator(Map schema) throws SchemaException {
        _rule = new Rule(schema);
    }

    public Validator(Object schema) throws SchemaException {
        _rule = new Rule(schema);
    }

    public Rule getRule() { return _rule; }
    //public void setRule(Rule rule) { _rule = rule; }

    public List validate(Object value) {
        String path = "";
        List errors = new ArrayList();
        Map done = new IdentityHashMap();
        validateDocument(value, _rule, path, errors, done);
        return errors;
    }

    protected void validateHook(Object value, Rule rule, String path, List errors) {
        // nothing
    }

    private void validateDocument(Object value, Rule rule, String path, List errors, Map done) {
        if (Types.isCollection(value)) {
            if (done.get(value) != null) {
                return;
            }
            done.put(value, Boolean.TRUE);
        }
        if (rule.isRequired() && value == null) {
            Object[] args = new Object[] { Types.typeName(rule.getType()) };
            errors.add(validationError("required.novalue", rule, path, value, args));
            return;
        }
        //Class klass = rule.getTypeClass();
        //if (klass != null && value != null && !klass.isInstance(value)) {
        if (value != null && ! Types.isCorrectType(value, rule.getType())) {
            Object[] args = new Object[] { Types.typeName(rule.getType()) };
            errors.add(validationError("type.unmatch", rule, path, value, args));
            return;
        }
        //
        int n = errors.size();
        if (rule.getSequence() != null) {
            assert value == null || value instanceof List;
            validateSequence((List)value, rule, path, errors, done);
        } else if (rule.getMapping() != null) {
            assert value == null || value instanceof Map;
            validateMapping((Map)value, rule, path, errors, done);
        } else {
            validateScalar(value, rule, path, errors, done);
        }
        if (errors.size() != n) {
            return;
        }
        //
        validateHook(value, rule, path, errors);
    }

    protected ValidationException validationError(String error_symbol, Rule rule, String path, Object value, Object[] args) {
        String msg = Messages.buildMessage(error_symbol, value, args);
        return new ValidationException(msg, path, value, rule, error_symbol);
    }


    private void validateScalar(Object value, Rule rule, String path, List errors, Map done) {
        assert rule.getSequence() == null;
        assert rule.getMapping() == null;
        if (rule.getAssert() != null) {
            //boolean result = evaluate(rule.getAssert());
            //if (! result) {
            //    errors.add("asset.failed", rule, path, value, new Object[] { rule.getAssert() });
            //}
        }
        if (rule.getEnum() != null) {
            if (! rule.getEnum().contains(value)) {
                int rindex = path.lastIndexOf('/');
                String keyname = rindex >= 0 ? path.substring(rindex + 1) : path;
                //if (Util.matches(keyname, "\\A\\d+\\z") keyname = "enum";
                errors.add(validationError("enum.notexist", rule, path, value, new Object[] { keyname }));
            }
        }
        //
        if (value == null) {
            return;
        }
        //
        if (rule.getPattern() != null) {
            if (! Util.matches(value.toString(), rule.getPatternRegexp())) {
                errors.add(validationError("pattern.unmatch", rule, path, value, new Object[] { rule.getPattern() }));
            }
        }
        if (rule.getRange() != null) {
            assert Types.isScalar(value);
            Map range = rule.getRange();
            Object v;
            if ((v = range.get("max")) != null && Util.compareValues(v, value) < 0) {
                errors.add(validationError("range.toolarge", rule, path, value, new Object[] { v.toString() }));
            }
            if ((v = range.get("min")) != null && Util.compareValues(v, value) > 0) {
                errors.add(validationError("range.toosmall", rule, path, value, new Object[] { v.toString() }));
            }
            if ((v = range.get("max-ex")) != null && Util.compareValues(v, value) <= 0) {
                errors.add(validationError("range.toolargeex", rule, path, value, new Object[] { v.toString() }));
            }
            if ((v = range.get("min-ex")) != null && Util.compareValues(v, value) >= 0) {
                errors.add(validationError("range.toosmallex", rule, path, value, new Object[] { v.toString() }));
            }
        }
        if (rule.getLength() != null) {
            assert value instanceof String;
            Map length = rule.getLength();
            int len = value.toString().length();
            Integer v;
            if ((v = (Integer)length.get("max")) != null && v.intValue() < len) {
                errors.add(validationError("length.toolong", rule, path, value, new Object[] { new Integer(len), v }));
            }
            if ((v = (Integer)length.get("min")) != null && v.intValue() > len) {
                errors.add(validationError("length.tooshort", rule, path, value, new Object[] { new Integer(len), v }));
            }
            if ((v = (Integer)length.get("max-ex")) != null && v.intValue() <= len) {
                errors.add(validationError("length.toolongex", rule, path, value, new Object[] { new Integer(len), v }));
            }
            if ((v = (Integer)length.get("min-ex")) != null && v.intValue() >= len) {
                errors.add(validationError("length.tooshortex", rule, path, value, new Object[] { new Integer(len), v }));
            }
        }
    }


    private void validateSequence(List sequence, Rule seq_rule, String path, List errors, Map done) {
        assert seq_rule.getSequence() instanceof List;
        assert seq_rule.getSequence().size() == 1;
        if (sequence == null) {
            return;
        }
        Rule rule = (Rule)seq_rule.getSequence().get(0);
        int i = 0;
        for (Iterator it = sequence.iterator(); it.hasNext(); i++) {
            Object val = it.next();
            validateDocument(val, rule, path + "/" + i, errors, done);  // validate recursively
        }
        if (rule.getType().equals("map")) {
            Map mapping = rule.getMapping();
            List unique_keys = new ArrayList();
            for (Iterator it = mapping.keySet().iterator(); it.hasNext(); ) {
                Object key = it.next();
                Rule map_rule = (Rule)mapping.get(key);
                if (map_rule.isUnique() || map_rule.isIdent()) {
                    unique_keys.add(key);
                }
            }
            //
            if (unique_keys.size() > 0) {
                for (Iterator it = unique_keys.iterator(); it.hasNext(); ) {
                    Object key = it.next();
                    Map table = new HashMap();  // val => index
                    int j = 0;
                    for (Iterator it2 = sequence.iterator(); it2.hasNext(); j++) {
                        Map map = (Map)it2.next();
                        Object val = map.get(key);
                        if (val == null) {
                            continue;
                        }
                        if (table.containsKey(val)) {
                            String curr_path = path + "/" + j + "/" + key;
                            String prev_path = path + "/" + table.get(val) + "/" + key;
                            errors.add(validationError("value.notunique", rule, curr_path, val, new Object[] { prev_path }));
                        } else {
                            table.put(val, new Integer(j));
                        }
                    }
                }
            }
        } else if (rule.isUnique()) {
            Map table = new HashMap();  // val => index
            int j = 0;
            for (Iterator it = sequence.iterator(); it.hasNext(); j++) {
                Object val = it.next();
                if (val == null) {
                    continue;
                }
                if (table.containsKey(val)) {
                    String curr_path = path + "/" + j;
                    String prev_path = path + "/" + table.get(val);
                    errors.add(validationError("value.notunique", rule, curr_path, val, new Object[] { prev_path }));
                } else {
                    table.put(val, new Integer(j));
                }
            }
        }
    }


    private void validateMapping(Map mapping, Rule map_rule, String path, List errors, Map done) {
        assert map_rule.getMapping() instanceof Map;
        if (mapping == null) {
            return;
        }
        Map m = map_rule.getMapping();
        for (Iterator it = m.keySet().iterator(); it.hasNext(); ) {
            Object key = it.next();
            Rule rule = (Rule)m.get(key);
            if (rule.isRequired() && !mapping.containsKey(key)) {
                errors.add(validationError("required.nokey", rule, path, mapping, new Object[] { key }));
            }
        }
        for (Iterator it = mapping.keySet().iterator(); it.hasNext(); ) {
            Object key = it.next();
            Object val = mapping.get(key);
            Rule rule = (Rule)m.get(key);
            if (rule == null) {
                errors.add(validationError("key.undefined", rule, path + "/" + key, mapping, new Object[] { key.toString() + ":" }));
            } else {
                validateDocument(val, rule, path + "/" + key, errors, done);  // validate recursively
            }
        }
    }

/*
    public static void main(String[] args) throws Exception {
        Map schema = (Map)YamlUtil.loadFile("schema.yaml");
        Validator validator = new Validator(schema);
        String filename = args.length > 0 ? args[0] : "document.yaml";
        Object document = YamlUtil.loadFile(filename);
        List errors = validator.validate(document);
        if (errors != null && errors.size() > 0) {
            for (Iterator it = errors.iterator(); it.hasNext(); ) {
                ValidationException error = (ValidationException)it.next();
                //String s = "- [" + error.getPath() + "] " + error.getMessage();
                String s = "- <" + error.getErrorSymbol() + ">[" + error.getPath() + "] " + error.getMessage();
                System.out.println(s);
            }
        } else {
            System.out.println("validtion OK.");
        }
    }
*/

}
