package org.wso2.ballerina.plugin;

import org.sonarsource.performance.measure.PerformanceMeasure;
import org.sonarsource.performance.measure.PerformanceMeasure.Duration;

public class MetricsUtils {
    public static <T> T measureDuration(String what, Action<T> action){
        Duration duration = PerformanceMeasure.start(what);
        T res = action.execute();
        duration.stop();
        return res;
    }

    @FunctionalInterface
    public interface Action<T> {
        T execute();
    }
}
