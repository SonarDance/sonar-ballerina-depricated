package org.wso2.ballerina.plugin;

import org.jetbrains.kotlin.config.LanguageVersion;
import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonarsource.kotlin.plugin.KotlinProfileDefinition;

public class BallerinaPlugin implements Plugin {
    // Required Categories
    private static final String GENERAL = "General";
    private static final String BALLERINA_CATEGORY = "Ballerina";

    // Global constants
    public static final String BALLERINA_LANGUAGE_KEY = "ballerina";
    public static final String BALLERINA_LANGUAGE_NAME = "Ballerina";
    public static final String BALLERINA_REPOSITORY_KEY = "ballerina";
    public static final String REPOSITORY_NAME = "SonarAnalyzer";
    public static final String PROFILE_NAME = "Sonar way";
    public static final String BALLERINA_FILE_SUFFIXES_KEY = "sonar.ballerina.file.suffixes";
    public static final String BALLERINA_FILE_SUFFIXES_DEFAULT_VALUE = ".bal";
    public static final String SONAR_JAVA_BINARIES = "sonar.java.binaries";
    public static final String SONAR_JAVA_LIBRARIES = "sonar.java.libraries";
    public static final String FAIL_FAST_PROPERTY_NAME = "sonar.internal.analysis.failFast";
    public static final String PERFORMANCE_MEASURE_ACTIVATION_PROPERTY = "sonar.ballerina.performance.measure";
    public static final String PERFORMANCE_MEASURE_DESTINATION_FILE = "sonar.ballerina.performance.measure.json";
    public static final String BALLERINA_LANGUAGE_VERSION = "sonar.ballerina.source.version";
    public static final LanguageVersion DEFAULT_BALLERINA_LANGUAGE_VERSION = LanguageVersion.KOTLIN_1_5;
    public static final String COMPILER_THREAD_COUNT_PROPERTY = "sonar.ballerina.threads";
    public static final String SKIP_UNCHANGED_FILES_OVERRIDE = "sonar.ballerina.skipUnchanged";

    @Override
    public void define(Context context) {
        context.addExtensions(
        BallerinaLanguage.class,
        BallerinaSensor.class,
        KotlinRulesDefinition.class,
        KotlinProfileDefinition.class,
        PropertyDefinition.builder(BALLERINA_FILE_SUFFIXES_KEY)
            .defaultValue(BALLERINA_FILE_SUFFIXES_DEFAULT_VALUE)
            .name("File Suffixes")
            .description("List of suffixes for files to analyze.")
            .subCategory(GENERAL)
            .category(BALLERINA_CATEGORY)
            .multiValues(true)
            .onQualifiers(Qualifiers.PROJECT)
            .build()
        );
    }
}
