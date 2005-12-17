/*
 * @(#)CommandOptionException.java	$Rev: 3 $ $Release: 0.5.0 $
 *
 * copyright(c) 2005 kuwata-lab all rights reserved.
 */

package kwalify;

/**
 * exception class thrown if command-line option is wrong
 * 
 * @revision    $Rev: 3 $
 * @release     $Release: 0.5.0 $
 */
public class CommandOptionException extends KwalifyException {
    private String _error_symbol = null;
    private char _option;

    public CommandOptionException(String message, char option, String error_symbol) {
        super(message);
        _option = option;
        _error_symbol = error_symbol;
    }

    public String getErrorSymbol() { return _error_symbol; }
    public void setErrorSymbol(String error_symbol) { _error_symbol = error_symbol; }

    public char getOption() { return _option; }
    public void setOption(char option) { _option = option; }

}
