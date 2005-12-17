/*
 * @(#)YamlSyntaxException.java	$Rev: 3 $ $Release: 0.5.0 $
 *
 * copyright(c) 2005 kuwata-lab all rights reserved.
 */

package kwalify;

/**
 *  exception class thrown by YamlParser when syntax of YAML document is wrong
 *
 *  @revision    $Rev: 3 $
 *  @release     $Release: 0.5.0 $
 *  @see         SyntaxException
 */
public class YamlSyntaxException extends SyntaxException {

    public YamlSyntaxException(String message, int linenum) {
        super(message, linenum);
    }

}