/*
 * @(#)SchemaException.java	$Rev: 3 $ $Release: 0.5.0 $
 *
 * copyright(c) 2005 kuwata-lab all rights reserved.
 */

package kwalify;

/**
 * exception class thrown by Rule constructor
 * 
 * @revision    $Rev: 3 $
 * @release     $Release: 0.5.0 $
 */
public class SchemaException extends BaseException {

    public SchemaException(String message, String ypath, Object value, Rule rule, String errorSymbol) {
        super(message, ypath, value, rule, errorSymbol);
    }

}
