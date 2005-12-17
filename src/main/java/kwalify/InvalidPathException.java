/*
 * @(#)InvalidPathException.java	$Rev: 3 $ $Release: 0.5.0 $
 *
 * copyright(c) 2005 kuwata-lab all rights reserved.
 */

package kwalify;

/**
 * exception class thrown by YamlParser#setErrorsLineNumber() when path is wrong
 *
 * @revision    $Rev: 3 $
 * @release     $Release: 0.5.0 $
 */
public class InvalidPathException extends KwalifyRuntimeException {
    private int _linenum;

    public InvalidPathException(String message) {
        super(message);
    }
}
