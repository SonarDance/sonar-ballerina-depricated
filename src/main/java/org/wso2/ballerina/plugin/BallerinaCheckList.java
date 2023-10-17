package org.wso2.ballerina.plugin;

import org.wso2.ballerina.checks.AllBranchesIdenticalCheck;
import org.wso2.ballerina.checks.AnchorPrecedenceCheck;
import org.wso2.ballerina.checks.AndroidBroadcastingCheck;
import org.wso2.ballerina.checks.ArrayHashCodeAndToStringCheck;
import org.wso2.ballerina.checks.AuthorisingNonAuthenticatedUsersCheck;
import org.wso2.ballerina.checks.BadClassNameCheck;
import org.wso2.ballerina.checks.BadFunctionNameCheck;
import org.wso2.ballerina.checks.BiometricAuthWithoutCryptoCheck;
import org.wso2.ballerina.checks.BooleanInversionCheck;
import org.wso2.ballerina.checks.BooleanLiteralCheck;
import org.wso2.ballerina.checks.CipherBlockChainingCheck;
import org.wso2.ballerina.checks.CipherModeOperationCheck;
import org.wso2.ballerina.checks.ClearTextProtocolCheck;
import org.wso2.ballerina.checks.CodeAfterJumpCheck;
import org.wso2.ballerina.checks.CollapsibleIfStatementsCheck;
import org.wso2.ballerina.checks.CollectionCallingItselfCheck;
import org.wso2.ballerina.checks.CollectionInappropriateCallsCheck;
import org.wso2.ballerina.checks.CollectionSizeAndArrayLengthCheck;
import org.wso2.ballerina.checks.CommentedCodeCheck;
import org.wso2.ballerina.checks.CoroutineScopeFunSuspendingCheck;
import org.wso2.ballerina.checks.CoroutinesTimeoutApiUnusedCheck;
import org.wso2.ballerina.checks.DataHashingCheck;
import org.wso2.ballerina.checks.DebugFeatureEnabledCheck;
import org.wso2.ballerina.checks.DeprecatedCodeCheck;
import org.wso2.ballerina.checks.DeprecatedCodeUsedCheck;
import org.wso2.ballerina.checks.DuplicateBranchCheck;
import org.wso2.ballerina.checks.DuplicatedFunctionImplementationCheck;
import org.wso2.ballerina.checks.DuplicatesInCharacterClassCheck;
import org.wso2.ballerina.checks.ElseIfWithoutElseCheck;
import org.wso2.ballerina.checks.EmptyBlockCheck;
import org.wso2.ballerina.checks.EmptyCommentCheck;
import org.wso2.ballerina.checks.EmptyFunctionCheck;
import org.wso2.ballerina.checks.EmptyLineRegexCheck;
import org.wso2.ballerina.checks.EmptyStringRepetitionCheck;
import org.wso2.ballerina.checks.EncryptionAlgorithmCheck;
import org.wso2.ballerina.checks.EqualsArgumentTypeCheck;
import org.wso2.ballerina.checks.EqualsOverriddenWithArrayFieldCheck;
import org.wso2.ballerina.checks.EqualsOverridenWithHashCodeCheck;
import org.wso2.ballerina.checks.ExposedMutableFlowCheck;
import org.wso2.ballerina.checks.ExternalAndroidStorageAccessCheck;
import org.wso2.ballerina.checks.FileHeaderCheck;
import org.wso2.ballerina.checks.FinalFlowOperationCheck;
import org.wso2.ballerina.checks.FixMeCommentCheck;
import org.wso2.ballerina.checks.FlowChannelReturningFunsNotSuspendingCheck;
import org.wso2.ballerina.checks.FunctionCognitiveComplexityCheck;
import org.wso2.ballerina.checks.GraphemeClustersInClassesCheck;
import org.wso2.ballerina.checks.HardcodedCredentialsCheck;
import org.wso2.ballerina.checks.HardcodedIpCheck;
import org.wso2.ballerina.checks.IdenticalBinaryOperandCheck;
import org.wso2.ballerina.checks.IdenticalConditionsCheck;
import org.wso2.ballerina.checks.IfConditionalAlwaysTrueOrFalseCheck;
import org.wso2.ballerina.checks.IgnoredOperationStatusCheck;
import org.wso2.ballerina.checks.InjectableDispatchersCheck;
import org.wso2.ballerina.checks.InvalidRegexCheck;
import org.wso2.ballerina.checks.IsInstanceMethodCheck;
import org.wso2.ballerina.checks.JumpInFinallyCheck;
import org.wso2.ballerina.checks.MainSafeCoroutinesCheck;
import org.wso2.ballerina.checks.MatchCaseTooBigCheck;
import org.wso2.ballerina.checks.MobileDatabaseEncryptionKeysCheck;
import org.wso2.ballerina.checks.NestedMatchCheck;
import org.wso2.ballerina.checks.OneStatementPerLineCheck;
import org.wso2.ballerina.checks.ParsingErrorCheck;
import org.wso2.ballerina.checks.PreparedStatementAndResultSetCheck;
import org.wso2.ballerina.checks.PseudoRandomCheck;
import org.wso2.ballerina.checks.ReceivingIntentsCheck;
import org.wso2.ballerina.checks.RedundantParenthesesCheck;
import org.wso2.ballerina.checks.RedundantSuspendModifierCheck;
import org.wso2.ballerina.checks.RegexComplexityCheck;
import org.wso2.ballerina.checks.ReluctantQuantifierCheck;
import org.wso2.ballerina.checks.ReplaceGuavaWithKotlinCheck;
import org.wso2.ballerina.checks.RobustCryptographicKeysCheck;
import org.wso2.ballerina.checks.RunFinalizersCheck;
import org.wso2.ballerina.checks.ScheduledThreadPoolExecutorZeroCheck;
import org.wso2.ballerina.checks.SelfAssignmentCheck;
import org.wso2.ballerina.checks.ServerCertificateCheck;
import org.wso2.ballerina.checks.StreamNotConsumedCheck;
import org.wso2.ballerina.checks.StringLiteralDuplicatedCheck;
import org.wso2.ballerina.checks.StrongCipherAlgorithmCheck;
import org.wso2.ballerina.checks.StructuredConcurrencyPrinciplesCheck;
import org.wso2.ballerina.checks.SuspendingFunCallerDispatcherCheck;
import org.wso2.ballerina.checks.TabsCheck;
import org.wso2.ballerina.checks.TodoCommentCheck;
import org.wso2.ballerina.checks.TooComplexExpressionCheck;
import org.wso2.ballerina.checks.TooDeeplyNestedStatementsCheck;
import org.wso2.ballerina.checks.TooLongFunctionCheck;
import org.wso2.ballerina.checks.TooLongLambdaCheck;
import org.wso2.ballerina.checks.TooLongLineCheck;
import org.wso2.ballerina.checks.TooManyCasesCheck;
import org.wso2.ballerina.checks.TooManyLinesOfCodeFileCheck;
import org.wso2.ballerina.checks.TooManyParametersCheck;
import org.wso2.ballerina.checks.UnencryptedDatabaseOnMobileCheck;
import org.wso2.ballerina.checks.UnencryptedFilesInMobileApplicationsCheck;
import org.wso2.ballerina.checks.UnicodeAwareCharClassesCheck;
import org.wso2.ballerina.checks.UnnecessaryImportsCheck;
import org.wso2.ballerina.checks.UnpredictableHashSaltCheck;
import org.wso2.ballerina.checks.UnpredictableSecureRandomSaltCheck;
import org.wso2.ballerina.checks.UnusedDeferredResultCheck;
import org.wso2.ballerina.checks.UnusedFunctionParameterCheck;
import org.wso2.ballerina.checks.UnusedLocalVariableCheck;
import org.wso2.ballerina.checks.UnusedPrivateMethodCheck;
import org.wso2.ballerina.checks.UselessIncrementCheck;
import org.wso2.ballerina.checks.VariableAndParameterNameCheck;
import org.wso2.ballerina.checks.VerifiedServerHostnamesCheck;
import org.wso2.ballerina.checks.ViewModelSuspendingFunctionsCheck;
import org.wso2.ballerina.checks.WeakSSLContextCheck;
import org.wso2.ballerina.checks.WebViewJavaScriptSupportCheck;
import org.wso2.ballerina.checks.WebViewsFileAccessCheck;
import org.wso2.ballerina.checks.WrongAssignmentOperatorCheck;

import java.util.Arrays;
import java.util.List;

public class BallerinaCheckList {
    public static final List<Class<?>> BALLERINA_CHECKS = Arrays.asList(
            AllBranchesIdenticalCheck.class,
            AnchorPrecedenceCheck.class,
            AndroidBroadcastingCheck.class,
            ArrayHashCodeAndToStringCheck.class,
            AuthorisingNonAuthenticatedUsersCheck.class,
            BadClassNameCheck.class,
            BadFunctionNameCheck.class,
            BiometricAuthWithoutCryptoCheck.class,
            BooleanInversionCheck.class,
            BooleanLiteralCheck.class,
            CipherBlockChainingCheck.class,
            CipherModeOperationCheck.class,
            ClearTextProtocolCheck.class,
            CodeAfterJumpCheck.class,
            CollapsibleIfStatementsCheck.class,
            CollectionCallingItselfCheck.class,
            CollectionSizeAndArrayLengthCheck.class,
            CollectionInappropriateCallsCheck.class,
            CoroutineScopeFunSuspendingCheck.class,
            CommentedCodeCheck.class,
            CoroutinesTimeoutApiUnusedCheck.class,
            DataHashingCheck.class,
            DebugFeatureEnabledCheck.class,
            DeprecatedCodeCheck.class,
            DeprecatedCodeUsedCheck.class,
            DuplicateBranchCheck.class,
            DuplicatedFunctionImplementationCheck.class,
            DuplicatesInCharacterClassCheck.class,
            ElseIfWithoutElseCheck.class,
            EmptyBlockCheck.class,
            EmptyCommentCheck.class,
            EmptyFunctionCheck.class,
            EmptyLineRegexCheck.class,
            EmptyStringRepetitionCheck.class,
            EncryptionAlgorithmCheck.class,
            EqualsArgumentTypeCheck.class,
            EqualsOverriddenWithArrayFieldCheck.class,
            EqualsOverridenWithHashCodeCheck.class,
            ExposedMutableFlowCheck.class,
            ExternalAndroidStorageAccessCheck.class,
            FileHeaderCheck.class,
            FinalFlowOperationCheck.class,
            FixMeCommentCheck.class,
            FlowChannelReturningFunsNotSuspendingCheck.class,
            FunctionCognitiveComplexityCheck.class,
            GraphemeClustersInClassesCheck.class,
            HardcodedCredentialsCheck.class,
            HardcodedIpCheck.class,
            IdenticalBinaryOperandCheck.class,
            IdenticalConditionsCheck.class,
            IfConditionalAlwaysTrueOrFalseCheck.class,
            IgnoredOperationStatusCheck.class,
            InjectableDispatchersCheck.class,
            InvalidRegexCheck.class,
            IsInstanceMethodCheck.class,
            JumpInFinallyCheck.class,
            MainSafeCoroutinesCheck.class,
            MatchCaseTooBigCheck.class,
            MobileDatabaseEncryptionKeysCheck.class,
            NestedMatchCheck.class,
            OneStatementPerLineCheck.class,
            ParsingErrorCheck.class,
            PreparedStatementAndResultSetCheck.class,
            PseudoRandomCheck.class,
            ReceivingIntentsCheck.class,
            RedundantParenthesesCheck.class,
            RedundantSuspendModifierCheck.class,
            RegexComplexityCheck.class,
            ReluctantQuantifierCheck.class,
            ReplaceGuavaWithKotlinCheck.class,
            RobustCryptographicKeysCheck.class,
            RunFinalizersCheck.class,
            ScheduledThreadPoolExecutorZeroCheck.class,
            SelfAssignmentCheck.class,
            ServerCertificateCheck.class,
            StreamNotConsumedCheck.class,
            StringLiteralDuplicatedCheck.class,
            StrongCipherAlgorithmCheck.class,
            StructuredConcurrencyPrinciplesCheck.class,
            SuspendingFunCallerDispatcherCheck.class,
            TabsCheck.class,
            TodoCommentCheck.class,
            TooComplexExpressionCheck.class,
            TooDeeplyNestedStatementsCheck.class,
            TooLongFunctionCheck.class,
            TooLongLambdaCheck.class,
            TooLongLineCheck.class,
            TooManyCasesCheck.class,
            TooManyLinesOfCodeFileCheck.class,
            TooManyParametersCheck.class,
            UnencryptedDatabaseOnMobileCheck.class,
            UnencryptedFilesInMobileApplicationsCheck.class,
            UnicodeAwareCharClassesCheck.class,
            UnnecessaryImportsCheck.class,
            UnpredictableHashSaltCheck.class,
            UnpredictableSecureRandomSaltCheck.class,
            UnusedDeferredResultCheck.class,
            UnusedFunctionParameterCheck.class,
            UnusedLocalVariableCheck.class,
            UnusedPrivateMethodCheck.class,
            UselessIncrementCheck.class,
            VariableAndParameterNameCheck.class,
            VerifiedServerHostnamesCheck.class,
            ViewModelSuspendingFunctionsCheck.class,
            WeakSSLContextCheck.class,
            WebViewJavaScriptSupportCheck.class,
            WebViewsFileAccessCheck.class,
            WrongAssignmentOperatorCheck.class
    );
}
