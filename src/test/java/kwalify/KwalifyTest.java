package kwalify;

import junit.framework.*;

/**
 *  test suite for all testcases
 *
 *  @revision    $Rev: 4 $
 *  @release     $Release: 0.5.1 $
 *  @copyright   copyright(c) 2005 kuwata-lab all rights reserved.
 */
public class KwalifyTest {


    private static Class[] __classes = {
        YamlParserTest.class,
        RuleTest.class,
        ValidatorTest.class,
        MetaValidatorTest.class,
        MainTest.class,
    };

    public static void main(String[] args) {
        TestSuite suite = new TestSuite();
        for (int i = 0; i < __classes.length; i++) {
            suite.addTest(new TestSuite(__classes[i]));
        }
        junit.textui.TestRunner.run(suite);
    }
}
