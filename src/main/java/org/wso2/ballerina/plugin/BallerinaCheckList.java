package org.wso2.ballerina.plugin;

import org.wso2.ballerina.checks.TooManyParametersCheck;

import java.util.Arrays;
import java.util.List;

public class BallerinaCheckList {
    public static final List<Class<?>> BALLERINA_CHECKS = Arrays.asList(
            TooManyParametersCheck.class
    );
}
