package io.github.pangzixiang.whatsit.vertx.core.utils;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class ClassScannerUtils {

    private static ScanResult scanResult = null;

    private static <T> List<T> getClassScanResult(Function<ScanResult, List<T>> function) {
        if (scanResult == null) {
            scanResult = new ClassGraph()
                    .disableRuntimeInvisibleAnnotations()
                    .disableNestedJarScanning()
                    .enableClassInfo()
                    .enableAnnotationInfo()
                    .enableMemoryMapping()
                    .scan();
        }
        return function.apply(scanResult);
    }

    /**
     * Gets classes by custom filter.
     *
     * @param predicate the predicate
     * @return the classes by custom filter
     */
    public static List<Class<?>> getClassesByCustomFilter(Predicate<ClassInfo> predicate) {
        long start = System.currentTimeMillis();
        List<Class<?>> result = getClassScanResult(scanResult -> scanResult.getAllClasses()
                .stream()
                .filter(predicate)
                .map(classInfo -> classInfo.loadClass(true))
                .collect(Collectors.toList()));
        log.debug("Succeed to query Classes {} by custom filter in {} ms", result, System.currentTimeMillis() - start);
        return result;
    }

    public static void closeScanResult() {
        if (scanResult != null) {
            scanResult.close();
            scanResult = null;
        }
    }
}
