/*
 * @(#)InvalidTypeException.java	$Rev: 3 $ $Release: 0.5.0 $
 *
 * copyright(c) 2005 kuwata-lab all rights reserved.
 */

package kwalify;

/**
 * exception class thrown by Util.compareValues() when comparing different type values.
 * 
 * @revision    $Rev: 3 $
 * @release     $Release: 0.5.0 $
 */
public class InvalidTypeException extends KwalifyRuntimeException {
    public InvalidTypeException(String message) {
        super(message);
    }
}
