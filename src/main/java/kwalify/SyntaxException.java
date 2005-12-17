/*
 * @(#)SyntaxException.java	$Rev: 3 $ $Release: 0.5.0 $
 *
 * copyright(c) 2005 kuwata-lab all rights reserved.
 */

package kwalify;

/**
 *  exception class thrown by parser when syntax is wrong.
 *
 *  @revision    $Rev: 3 $
 *  @release     $Release: 0.5.0 $
 *  @see         Parser, YamlSyntaxException
 */
public class SyntaxException extends KwalifyException {
    private int _linenum;

    public SyntaxException(String message, int linenum) {
        super(message);
        _linenum = linenum;
    }

    public int getLineNumer() { return _linenum; }
    public void setLineNumber(int linenum) { _linenum = linenum; }
}
