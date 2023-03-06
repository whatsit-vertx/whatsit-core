package io.github.pangzixiang.whatsit.vertx.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The type Auto class loader.
 */
@Slf4j
public class AutoClassLoader {

    private static final List<Class<?>> allClz = new ArrayList<>();
    private static final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private static final String[] classPaths = Objects.requireNonNullElseGet(System.getProperty("java.class.path"), () -> {
        log.error("Failed to get classpath from system property");
        return "";
    }).split(System.getProperty("path.separator"));
    private static final String DOT_CLASS = ".class";
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String DOT = ".";

    static {
        log.info("Start to load all Classes");
        long start = System.currentTimeMillis();
        loadAllClasses();
        log.info("Succeed to load {} Classes in {} ms", allClz.size(), System.currentTimeMillis() - start);
    }

    private AutoClassLoader() {}

    /**
     * Gets classes by annotation.
     *
     * @param annotation the annotation
     * @return the classes by annotation
     */
    public static List<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation) {
        List<Class<?>> result = allClz.stream().filter(clz -> clz.isAnnotationPresent(annotation)).toList();
        log.info("Succeed to query Classes {} by Annotation [{}]", result, annotation.getSimpleName());
        return result;
    }

    /**
     * Gets classes by abstract class.
     *
     * @param abstractClass the abstract class
     * @return the classes by abstract class
     */
    public static List<Class<?>> getClassesByAbstractClass(Class<?> abstractClass) {
        List<Class<?>> result = allClz.stream().filter(clz -> abstractClass.isAssignableFrom(clz) && clz != abstractClass).toList();
        log.info("Succeed to query Classes {} by Abstract Class [{}]", result, abstractClass.getSimpleName());
        return result;
    }

    /**
     * Gets classes by custom filter.
     *
     * @param predicate the predicate
     * @return the classes by custom filter
     */
    public static List<Class<?>> getClassesByCustomFilter(Predicate<? super Class<?>> predicate) {
        List<Class<?>> result = allClz.stream().filter(predicate).toList();
        log.info("Succeed to query Classes {} by custom filter", result);
        return result;
    }

    private static void loadAllClasses() {
        try {
            for (String classpath: classPaths) {
                File classpathFile = new File(classpath);
                if (classpathFile.exists()) {
                    if (classpathFile.isDirectory()) {
                        getClassesInPath(classpath, classpath);
                    } else if (classpathFile.isFile()) {
                        try (JarFile jarFile = new JarFile(classpathFile)) {
                            Enumeration<JarEntry> entries = jarFile.entries();;
                            while (entries.hasMoreElements()) {
                                JarEntry jarEntry = entries.nextElement();
                                String name = jarEntry.getName();
                                if (name.endsWith(DOT_CLASS)) {
                                    Class<?> clz = convertClassNameToClass(convertJarEntryNameToClassName(name));
                                    if (clz != null) allClz.add(clz);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load Classes...", e);
        }
    }

    private static void getClassesInPath(String basePath, String currentPath) {
        File dir = new File(currentPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles(pathname -> pathname.isDirectory() || pathname.getName().endsWith(DOT_CLASS));
            if (files != null) {
                for (File file: files) {
                    if (file.isDirectory()) {
                        getClassesInPath(basePath, file.getAbsolutePath());
                    } else {
                        Class<?> clz = convertClassNameToClass(convertPathToClassName(basePath, file));
                        if (clz != null) allClz.add(clz);
                    }
                }
            }
        }
    }

    private static String convertPathToClassName(String basePath, File file) {
        return file.getAbsolutePath()
                .replace(new File(basePath).getAbsolutePath() + FILE_SEPARATOR, "")
                .replace(DOT_CLASS, "")
                .replace(FILE_SEPARATOR, DOT);
    }

    private static String convertJarEntryNameToClassName(String name) {
        return name.replace(DOT_CLASS, "").replace("/", DOT);
    }

    private static Class<?> convertClassNameToClass(String name) {
        try {
            Class<?> clz = Class.forName(name, false, classLoader);
            if (!clz.isInterface()) {
                return clz;
            } else {
                return null;
            }
        } catch (Exception | LinkageError e) {
            return null;
        }
    }
}
