/*
 * @(#)Main.java	$Rev: 3 $ $Release: 0.5.0 $
 *
 * copyright(c) 2005 kuwata-lab all rights reserved.
 */

package kwalify;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.regex.Matcher;
import java.io.IOException;

/**
 * class for main program
 * 
 * @revision    $Rev: 3 $
 * @release     $Release: 0.5.0 $
 */
public class Main {

    private String _command;
    private boolean _flag_help     = false;
    private boolean _flag_version  = false;
    private boolean _flag_silent   = false;
    private boolean _flag_meta     = false;
    private boolean _flag_untabify = false;
    private boolean _flag_linenum  = false;
    private boolean _flag_debug    = false;
    private String  _schema_filename = null;
    private Map _properties = new HashMap();


    public String inspect() {
        StringBuffer sb = new StringBuffer();
        sb.append("command       : ").append(_command       ).append('\n');
        sb.append("flag_help     : ").append(_flag_help     ).append('\n');
        sb.append("flag_version  : ").append(_flag_version  ).append('\n');
        sb.append("flag_silent   : ").append(_flag_silent   ).append('\n');
        sb.append("flag_meta     : ").append(_flag_meta     ).append('\n');
        sb.append("flag_untabify : ").append(_flag_untabify ).append('\n');
        sb.append("flag_linenum  : ").append(_flag_linenum  ).append('\n');
        sb.append("flag_debug    : ").append(_flag_debug    ).append('\n');
        sb.append("schema_filename : ").append(_schema_filename).append('\n');
        sb.append("properties:\n");
        for (Iterator it = _properties.keySet().iterator(); it.hasNext(); ) {
            Object key = it.next();
            Object val = _properties.get(key);
            sb.append("  ").append(key).append(": ").append(val).append('\n');
        }
        return sb.toString();
    }

    private static final String REVISION = "$Release: 0.5.0 $";
    private static final String HELP = ""
        + "Usage1: %s [-hvstl] -f schema.yaml document.yaml [document2.yaml ...]\n"
        + "Usage2: %s [-hvstl] -m schema.yaml [schema2.yaml ...]\n"
        + "  -h, --help      :  help\n"
        + "  -v              :  version\n"
        + "  -s              :  silent\n"
        + "  -f schema.yaml  :  schema definition file\n"
        + "  -m              :  meta-validation mode\n"
        + "  -t              :  expand tab character automatically\n"
        + "  -l              :  show linenumber when errored (experimental)\n"
        ;


    public Main(String command) {
        _command = command;
    }


    public String execute(String[] args) throws IOException, CommandOptionException, SyntaxException {
        // parse command-line options
        String[] filenames = parseOptions(args);

        // help or version
        StringBuffer sb = new StringBuffer();
        if (_flag_version) {
            sb.append(version()).append('\n');
        }
        if (_flag_help) {
            sb.append(help());
        }
        if (sb.length() > 0) {
            return sb.toString();
        }

        // main
        String s = null;
        if (_flag_meta) {
            s = metaValidate(filenames);
        } else if (_schema_filename == null) {
            throw optionError("command.option.noaction", '\0');
        } else if (_flag_debug) {
            s = inspectSchemaFile(_schema_filename);
        } else {
            s = validate(_schema_filename, filenames);
        }

        //
        return s;
    }


    private String[] parseOptions(String[] args) throws CommandOptionException {
        Object[] ret = null;
        try {
            ret = Util.parseCommandOptions(args, "hvsmtlD", "f", null);
        } catch (CommandOptionException ex) {
            String error_symbol = ex.getErrorSymbol();
            if (error_symbol.equals("command.option.noarg")) {
                switch (ex.getOption()) {
                case 'f':   error_symbol = "command.option.noschema";  break;
                default:
                    assert false;
                }
            }
            throw optionError(error_symbol, ex.getOption());
        }
        //
        Map options        = (Map)ret[0];
        Map properties     = (Map)ret[1];
        String[] filenames = (String[])ret[2];
        //
        _flag_help     = options.get("h") != null;
        _flag_version  = options.get("v") != null;
        _flag_silent   = options.get("s") != null;
        _flag_meta     = options.get("m") != null;
        _flag_untabify = options.get("t") != null;
        _flag_linenum  = options.get("l") != null;
        _flag_debug    = options.get("D") != null;
        _schema_filename = (String)options.get("f");
        //
        _properties = properties;
        if (_properties.get("help") != null) {
            _flag_help = true;
        }
        //
        return filenames;
    }


    private String validate(String schema_filename, String[] filenames) throws IOException, SyntaxException {
        // load schema
        String str = Util.readFile(schema_filename);
        if (_flag_untabify) {
            str = Util.untabify(str);
        }
        YamlParser parser = new YamlParser(str);
        Object schema = parser.parse();

        // create validator
        Validator validator = new Validator(schema);

        // validate files
        if (filenames.length == 0) {
            filenames = new String[] { null };
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < filenames.length; i++) {
            String filename = filenames[i];
            String result = validateFile(validator, filename, "validation.valid", "validation.invalid");
            sb.append(result);
        }

        //
        return sb.toString();
    }


    private String validateFile(Validator validator, String filename, String valid_msg_key, String invalid_msg_key) throws IOException, SyntaxException {

        // read file content
        String str;
        if (filename == null) {
            str = Util.readInputStream(System.in);
            filename = "(stdin)";
        } else {
            str = Util.readFile(filename);
        }
        if (_flag_untabify) {
            str = Util.untabify(str);
        }

        //
        StringBuffer sb = new StringBuffer();
        YamlParser parser = new YamlParser(str);
        for (int i = 0; parser.hasNext(); i++) {
            Object doc = parser.parse();
            List errors = validator.validate(doc);
            Object[] args = new Object[] { filename, new Integer(i) };
            String msg;
            if (errors == null || errors.size() == 0) {
                if (! _flag_silent) {
                    msg = Messages.buildMessage(valid_msg_key, args);
                    sb.append(msg).append('\n');
                }
            } else {
                msg = Messages.buildMessage(invalid_msg_key, args);
                sb.append(msg).append('\n');
                appendErrors(sb, errors, parser);
            }
        }
        return sb.toString();
    }


    private void appendErrors(StringBuffer sb, List errors, YamlParser parser) {
        if (parser != null) {
            parser.setErrorsLineNumber(errors);
            Collections.sort(errors);
        }
        for (Iterator it = errors.iterator(); it.hasNext(); ) {
            ValidationException ex = (ValidationException)it.next();
            sb.append("  - ");
            if (_flag_linenum) {
                sb.append("(line ").append(ex.getLineNumber()).append(") ");
            }
            sb.append("[").append(ex.getPath()).append("] ");
            sb.append(ex.getMessage()).append('\n');
        }
    }


    private String metaValidate(String[] filenames) throws IOException, SyntaxException {
        Validator meta_validator = MetaValidator.instance();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < filenames.length; i++) {
            //String s = metaValidate(meta_validator, filenames[i]);
            String s = validateFile(meta_validator, filenames[i], "meta.valid", "meta.invalid");
            sb.append(s);
        }
        return sb.toString();
    }


/*
    private String metaValidate(Validator meta_validator, String filename) throws IOException, SyntaxException {
        // read schema file
        String str;
        if (filename == null) {
            str = Util.readInputStream(System.in);
            filename = "(stdin)";
        } else {
            str = Util.readFile(filename);
        }

        //
        StringBuffer sb = new StringBuffer();
        sb.append(filename).append(": ");

        // parse schema
        YamlParser parser = new YamlParser(str);
        Object schema = parser.parse();
        if (schema == null) {
            sb.append(Messages.message("meta.empty"));
            return sb.toString();
        }

        // meta-validation
        List errors = meta_validator.validate(schema);
        if (errors == null && errors.size() == 0) {
            sb.append(Messages.message("meta.valid"));
        } else {
            appendErrors(sb, errors, parser);
        }
        return sb.toString();
    }
*/


    private String inspectSchemaFile(String schema_filename) throws IOException, SyntaxException {
        String filename = schema_filename;
        String content = filename != null ? Util.readFile(filename) : Util.readInputStream(System.in);
        YamlParser parser = new YamlParser(content);
        Object schema = parser.parse();
        if (schema == null) {
            return null;
        }
        Validator validator = new Validator(schema);  // SchemaException is thrown when schema is wrong
        String s = validator.getRule().inspect();
        if (s.charAt(s.length() - 1) != '\n') {
            s = s + '\n';
        }
        return s;
    }


    private static CommandOptionException optionError(String error_symbol, char option) {
        String message = Messages.buildMessage(error_symbol, null, new Object[] { Character.toString(option) });
        return new CommandOptionException(message, option, error_symbol);
    }


    private String version() {
        Matcher m = Util.matcher(REVISION, "[.\\d]+");
        m.find();
        String version = m.group(0);
        return version;
    }


    private String help() {
        String help = HELP.replaceAll("%s", _command);
        return help;
    }


    public static void main(String[] args) {
        int status = 0;
        try {
            Main main = new Main("kwalify-java");
            String result = main.execute(args);
            System.out.println(result);
        } catch (IOException ex) {
            System.err.println("ERROR: " + ex.getMessage());
            status = 1;
        } catch (CommandOptionException ex) {
            System.err.println("ERROR: " + ex.getMessage());
            status = 1;
        } catch (SyntaxException ex) {
            System.err.println("ERROR: " + ex.getMessage());
            status = 1;
        }
        System.exit(status);
    }

}
