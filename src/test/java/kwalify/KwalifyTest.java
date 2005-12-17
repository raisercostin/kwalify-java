package kwalify;

import junit.framework.*;

/**
 *  test suite for all testcases
 *
 *  @revision    $Rev: 2 $
 *  @release     $Release: 0.5.0 $
 *  @copyright   copyright(c) 2005 kuwata-lab all rights reserved.
 */
public class KwalifyTest {


    private static Class[] classes = {
        YamlParserTest.class,
        RuleTest.class,
        ValidatorTest.class,
        MetaValidatorTest.class,
        MainTest.class,
    };

    public static void main(String[] args) {
        TestSuite suite = new TestSuite();
        for (int i = 0; i < classes.length; i++) {
            suite.addTest(new TestSuite(classes[i]));
        }
        junit.textui.TestRunner.run(suite);
    }
}
