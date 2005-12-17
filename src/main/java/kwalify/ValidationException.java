/*
 * @(#)ValidationException.java	$Rev: 3 $ $Release: 0.5.0 $
 *
 * copyright(c) 2005 kuwata-lab all rights reserved.
 */

package kwalify;

/**
 * exception class which represents validation error.
 *
 * @revision    $Rev: 3 $
 * @release     $Release: 0.5.0 $
 */
public class ValidationException extends BaseException {

    public ValidationException(String message, String path, Object value, Rule rule, String error_symbol) {
        super(message, path, value, rule, error_symbol);
    }

    public ValidationException(String message, String path) {
        this(message, path, null, null, null);
    }

}
