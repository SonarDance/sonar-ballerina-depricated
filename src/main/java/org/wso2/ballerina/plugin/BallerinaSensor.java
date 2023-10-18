package org.wso2.ballerina.plugin;

// Kotlin specific imports
import kotlin.OptIn;
import kotlin.time.ExperimentalTime;
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer;
import org.jetbrains.kotlin.com.intellij.psi.PsiFile;
import org.jetbrains.kotlin.config.JvmTarget;
import org.jetbrains.kotlin.config.LanguageVersion;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.diagnostics.Diagnostic;

// Sonar Plugin API imports
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarsource.analyzer.commons.ProgressReport;
import org.sonarsource.performance.measure.PerformanceMeasure;

// Ballerina Specific imports
import org.wso2.ballerina.api.AbstractCheck;
import org.wso2.ballerina.api.InputFileContext;
import org.wso2.ballerina.api.ParseException;
import org.wso2.ballerina.api.regex.RegexCache;
import org.wso2.ballerina.converter.Environment;
import org.wso2.ballerina.converter.KotlinSyntaxStructure;
import org.wso2.ballerina.converter.KotlinTree;
import org.wso2.ballerina.visiting.KotlinFileVisitor;
import org.wso2.ballerina.visiting.KtChecksVisitor;

import static org.wso2.ballerina.plugin.BallerinaPlugin.BALLERINA_LANGUAGE_VERSION;
import static org.wso2.ballerina.plugin.BallerinaPlugin.COMPILER_THREAD_COUNT_PROPERTY;
import static org.wso2.ballerina.plugin.BallerinaPlugin.DEFAULT_BALLERINA_LANGUAGE_VERSION;
import static org.wso2.ballerina.plugin.BallerinaPlugin.BALLERINA_REPOSITORY_KEY;
import static org.wso2.ballerina.plugin.BallerinaCheckList.BALLERINA_CHECKS;
import static org.wso2.ballerina.plugin.BallerinaPlugin.FAIL_FAST_PROPERTY_NAME;
import static org.wso2.ballerina.plugin.BallerinaPlugin.PERFORMANCE_MEASURE_ACTIVATION_PROPERTY;
import static org.wso2.ballerina.plugin.BallerinaPlugin.PERFORMANCE_MEASURE_DESTINATION_FILE;
import static org.wso2.ballerina.plugin.BallerinaPlugin.SONAR_JAVA_BINARIES;
import static org.wso2.ballerina.plugin.BallerinaPlugin.SONAR_JAVA_LIBRARIES;
import static org.wso2.ballerina.plugin.MetricsUtils.measureDuration;
import static org.wso2.ballerina.converter.KotlinCoreEnvironmentToolsKt.bindingContext;

// Other imports
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import static java.util.Collections.emptyList;

class BallerinaSensor implements Sensor {
    private static final Logger LOG = Loggers.get(BallerinaSensor.class);
    private static final Pattern EMPTY_FILE_CONTENT_PATTERN = Pattern.compile("\\s*+");
    private final CheckFactory checkFactory;

    public final Checks<AbstractCheck> checks;

    private final FileLinesContextFactory fileLinesContextFactory;
    private final NoSonarFilter noSonarFilter;
    private final BallerinaLanguage language;

    @OptIn(markerClass = ExperimentalTime.class)
    public BallerinaSensor(CheckFactory checkFactory,  FileLinesContextFactory fileLinesContextFactory,  NoSonarFilter noSonarFilter,  BallerinaLanguage language){
        // Initialize the Ballerina Checks/Visitors/Rules once the scan is triggered
        this.checks = checkFactory.create(BALLERINA_REPOSITORY_KEY);
        this.checks.addAnnotatedChecks(BALLERINA_CHECKS);
        for (AbstractCheck check : this.checks.all()) {
            check.initialize(checks.ruleKey(check));
        }

        this.checkFactory = checkFactory;
        this.fileLinesContextFactory = fileLinesContextFactory;
        this.noSonarFilter = noSonarFilter;
        this.language = language;
    }

    // Method which defines which language files the plugin should work with
    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
                .onlyOnLanguage(language.getKey())
                .name(language.getName() + " Sensor");
    }

    @Override
    public void execute(SensorContext sensorContext) {
        PerformanceMeasure.Duration sensorDuration = createPerformanceMeasureReport(sensorContext);

        // Feature to extract out only .bal file from a project for analysis
        FileSystem fileSystem = sensorContext.fileSystem();
        FilePredicate mainFilePredicate = fileSystem.predicates()
                .and(
                        fileSystem.predicates().hasLanguage(language.getKey()),
                        fileSystem.predicates().hasType(InputFile.Type.MAIN)
                );

        // Setting up the files to be analyzed
        List<InputFile> filesToAnalyze;
        Iterable<InputFile> mainFiles = fileSystem.inputFiles(mainFilePredicate);
        // Feature to perform analysis only on changed files
        if (canSkipUnchangedFiles(sensorContext)) {
            filesToAnalyze = StreamSupport.stream(mainFiles.spliterator(), false)
                    .filter(inputFile -> inputFile.status() != InputFile.Status.SAME)
                    .collect(Collectors.toList());

            int totalFiles = (int) StreamSupport.stream(mainFiles.spliterator(), false).count();

            LOG.info("Only analyzing " + filesToAnalyze.size() + " changed Java files out of " + totalFiles + ".");
        } else {
            LOG.debug("The Java analyzer is running in a context where unchanged files cannot be skipped.");
            filesToAnalyze = StreamSupport.stream(mainFiles.spliterator(), false).collect(Collectors.toList());
        }

        List<String> filenames = new java.util.ArrayList<>();
        for (InputFile fileToAnalyze : filesToAnalyze) {
            filenames.add(fileToAnalyze.toString());
        }

        // Stop the Ballerina Plugin scans if there are no .bal files in a project
        if(filenames.isEmpty()){
            return;
        }

        ProgressReport progressReport = new ProgressReport("Progress of the " + language.getName() + " analysis", TimeUnit.SECONDS.toMillis(10));
        
        Boolean success = false;
        try{
            success = analyzeFiles(sensorContext, filesToAnalyze, progressReport, visitors(sensorContext), filenames);
        }finally {
            if(success){
                progressReport.stop();
            }else {
                progressReport.cancel();
            }
        }
        sensorDuration.stop();
    }

    // Ballerina specific implementations should start from this point onwards
    private Boolean analyzeFiles(SensorContext sensorContext, Iterable<InputFile> inputFiles, ProgressReport progressReport, List<KotlinFileVisitor> visitors, List<String> filenames){
        Environment environment = environment(sensorContext);

        try{
            List<KotlinSyntaxStructure> kotlinFiles = new ArrayList<>();

            // Ballerina Parsing Logic to be Implemented here
            for(InputFile inputFile: inputFiles){
                try {
                    KotlinSyntaxStructure syntaxStructure = KotlinSyntaxStructure.of(inputFile.contents(), environment, inputFile);
                    kotlinFiles.add(syntaxStructure);
                } catch (Exception e) {
                    ParseException parseException = toParseException("read", inputFile, e);
                    logParsingError(inputFile, parseException);
                }
            }

            BindingContext bindingContext = null;
            try {
                bindingContext = measureDuration("BindingContext", () -> bindingContext(environment.getEnv(),
                        environment.getClasspath(),
                        kotlinFiles.stream()
                                .map(KotlinSyntaxStructure::getKtFile)
                                .collect(Collectors.toList())));
            } catch (Exception e) {
                LOG.error("Could not generate binding context. Proceeding without semantics.", e);
            }

            // Setting up a variable to hold the final outcome of the binding context operation to be used with
            // lambda expressions
            BindingContext finalBindingContext = bindingContext;

            Map<PsiFile, List<Diagnostic>> diagnostics = measureDuration("Diagnostics", () -> {
                assert finalBindingContext != null;
                return Optional.of(finalBindingContext.getDiagnostics().noSuppression().all())
                        .orElse(emptyList())
                        .stream()
                        .collect(Collectors.groupingBy(diagnostic -> diagnostic.getPsiFile(), Collectors.toList()));
            });

            RegexCache regexCache = new RegexCache();

            progressReport.start(filenames);

            // Since Ballerina does not have an android context we are making it false
            boolean inputFilesIsInAndroidContext = false;
            for (KotlinSyntaxStructure kotlinFile : kotlinFiles) {
                // Prevent the plugin from continuing if the user stops the scanning
                if (sensorContext.isCancelled()) {
                    return false;
                }

                // Context that should be implemented with Ballerina logic to perform reporting
                // rule Violations to SonarQube
                InputFileContext inputFileContext = new InputFileContextImpl(sensorContext
                        , kotlinFile.getInputFile()
                        , inputFilesIsInAndroidContext);

                // Place where analysis per each source file begins
                measureDuration(kotlinFile.getInputFile()
                        .filename()
                        , () -> {
                            assert finalBindingContext != null;
                            return analyseFile(sensorContext
                                    , inputFileContext
                                    , visitors
                                    , new KotlinTree(kotlinFile.getKtFile()
                                            , kotlinFile.getDocument()
                                            , finalBindingContext
                                            , diagnostics.isEmpty() ? emptyList() : diagnostics.get(kotlinFile.getKtFile())
                                            , regexCache)
                                    );
                        }
                );

                progressReport.nextFile();
            }
        }finally {
            Disposer.dispose(environment.getDisposable());
        }
        return true;
    }

    private <T> T analyseFile(SensorContext sensorContext, InputFileContext inputFileContext, List<KotlinFileVisitor> visitors, KotlinTree tree){
        // Try catch is placed for the regular expression check, as Regex does not work with java 11,
        // and we have to rely on the matcher method instead
        try {
            if(!EMPTY_FILE_CONTENT_PATTERN.matcher(inputFileContext.getInputFile().contents()).find()){
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        visitFile(sensorContext, inputFileContext, visitors, tree);
        return null;
    }

    private void visitFile(SensorContext sensorContext, InputFileContext inputFileContext, List<KotlinFileVisitor> visitors, KotlinTree tree) {
        for (KotlinFileVisitor visitor : visitors) {
            String visitorId = visitor.getClass().getSimpleName();
            try {
                 measureDuration(visitorId, () -> {
                     visitor.scan(inputFileContext, tree);

                     // Since there are no return values after a visitor scans the file
                     return null;
                 });
            } catch (Exception e) {
                inputFileContext.reportAnalysisError(e.getMessage(), null);
                LOG.error("Cannot analyze '" + inputFileContext.getInputFile() + "' with '" + visitorId + "': " + e.getMessage(), e);
                if (sensorContext.config().getBoolean(FAIL_FAST_PROPERTY_NAME).orElse(false)) {
                    throw new IllegalStateException("Exception in '" + visitorId + "' while analyzing '"
                            + inputFileContext.getInputFile() + "'", e);
                }
            }
        }
    }

    private List<KotlinFileVisitor> visitors(SensorContext sensorContext) {
        return Collections.singletonList(new KtChecksVisitor(checks));
    }

    private void logParsingError(InputFile inputFile, ParseException e) {
        TextPointer position = e.getPosition();
        String positionMessage = "";
        if(position != null){
            positionMessage = "Parse error at position " + position.line() + ":" + position.lineOffset();
        }
        LOG.error("Unable to parse file: " + inputFile.uri() + "." + positionMessage);

        if(e.getMessage() != null){
            LOG.error(e.getMessage());
        }
    }

    private ParseException toParseException(String action, InputFile inputFile, Throwable cause) {
        return new ParseException("Cannot " + action + " " + inputFile.toString() + ": " + cause.getMessage()
                , cause instanceof ParseException? ((ParseException) cause).getPosition() : null
                , cause);
    }

    private Environment environment(SensorContext sensorContext) {
        Collection<String> sonarJavaBinaries = Arrays.asList(sensorContext.config().getStringArray(SONAR_JAVA_BINARIES));
        Collection<String> sonarJavaLibraries = Arrays.asList(sensorContext.config().getStringArray(SONAR_JAVA_LIBRARIES));
        List<String> classpath = new ArrayList<String>(sonarJavaBinaries);
        classpath.addAll(sonarJavaLibraries);

        LanguageVersion languageVersion = determineKotlinLanguageVersion(sensorContext);

        Integer numberOfThreads = determineNumberOfThreadsToUse(sensorContext);

        return new Environment(classpath, languageVersion, JvmTarget.JVM_11, numberOfThreads);
    }

    private LanguageVersion determineKotlinLanguageVersion(SensorContext context) {
        return context.config()
                .get(BALLERINA_LANGUAGE_VERSION).map(versionString -> {
                    LanguageVersion langVersion = LanguageVersion.fromVersionString(versionString);
                    if (langVersion == null && !versionString.isBlank()) {
                        LOG.warn("Failed to find Ballerina version '" + versionString + "'. Defaulting to " + DEFAULT_BALLERINA_LANGUAGE_VERSION.getVersionString());
                    }
                return langVersion;
                })
                .orElse(DEFAULT_BALLERINA_LANGUAGE_VERSION);
    }

    private Integer determineNumberOfThreadsToUse(SensorContext context){
        return context.config().get(COMPILER_THREAD_COUNT_PROPERTY).map(stringInput -> {
            try {
                int threadCount = Integer.parseInt(stringInput.trim());
                if (threadCount > 0) {
                    return threadCount;
                } else {
                    LOG.warn("Invalid amount of threads specified for " + COMPILER_THREAD_COUNT_PROPERTY + ": '" + stringInput + "'.");
                    return null;
                }
            } catch (NumberFormatException e) {
                LOG.warn(COMPILER_THREAD_COUNT_PROPERTY
                        + " needs to be set to an integer value. Could not interpret '" + stringInput + "' as integer.");
                return null;
            }
        }).orElse(null);
    }

    private Boolean canSkipUnchangedFiles(SensorContext context){
        return context.config()
                .getBoolean(BallerinaPlugin.SKIP_UNCHANGED_FILES_OVERRIDE)
                .orElseGet(() ->{
                    try{
                        return context.canSkipUnchangedFiles();
                    }catch (IncompatibleClassChangeError e){
                        return false;
                    }
                });
    }

    private PerformanceMeasure.Duration createPerformanceMeasureReport(SensorContext context){
        return PerformanceMeasure.reportBuilder()
                .activate(
                        context
                        .config()
                        .get(PERFORMANCE_MEASURE_ACTIVATION_PROPERTY)
                                .filter((it) -> "true".equals(it))
                                .isPresent()
                )
                .toFile(context
                        .config()
                        .get(PERFORMANCE_MEASURE_DESTINATION_FILE)
                        .orElse("/tmp/sonar.ballerina.performance.measure.json")
                )
                .appendMeasurementCost()
                .start("BallerinaSensor");
    }
}