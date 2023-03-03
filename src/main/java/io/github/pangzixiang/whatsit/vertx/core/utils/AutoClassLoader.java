package io.github.pangzixiang.whatsit.vertx.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class AutoClassLoader {

    private static final List<Class<?>> allClz = new ArrayList<>();

    private static final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    private static final String PROTOCOL_FILE = "file";
    private static final String PROTOCOL_JAR = "jar";
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

    public static List<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation) {
        List<Class<?>> result = allClz.stream().filter(clz -> clz.isAnnotationPresent(annotation)).toList();
        log.info("Succeed to query Classes {} by Annotation [{}]", result, annotation.getSimpleName());
        return result;
    }

    public static List<Class<?>> getClassesByAbstractClass(Class<?> abstractClass) {
        List<Class<?>> result = allClz.stream().filter(clz -> abstractClass.isAssignableFrom(clz) && clz != abstractClass).toList();
        log.info("Succeed to query Classes {} by Abstract Class [{}]", result, abstractClass.getSimpleName());
        return result;
    }

    public static List<Class<?>> getClassesByCustomFilter(Predicate<? super Class<?>> predicate) {
        List<Class<?>> result = allClz.stream().filter(predicate).toList();
        log.info("Succeed to query Classes {} by custom filter", result);
        return result;
    }

    private static void loadAllClasses() {
        try {
            Enumeration<URL> dirs = classLoader.getResources("");
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if (protocol.equals(PROTOCOL_FILE)) {
                    String path = url.getFile();
                    getClassesInPath(path, path);
                } else if (protocol.equals(PROTOCOL_JAR)) {
                    JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                    Enumeration<JarEntry> entries = jarFile.entries();;
                    while (entries.hasMoreElements()) {
                        JarEntry jarEntry = entries.nextElement();
                        String name = jarEntry.getName();
                        if (name.endsWith(DOT_CLASS)) {
                            name = convertJarEntryNameToClassName(name);
                            try {
                                Class<?> clz = Class.forName(name);
                                if (!clz.isInterface()) {
                                    allClz.add(clz);
                                }
                            } catch (Exception | LinkageError ignored) {}
                        }
                    }
                } else {
                    log.warn("Skip not support protocol [{}] in [{}]", protocol, url);
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
                        String name = convertPathToClassName(basePath, file);
                        try {
                            Class<?> clz = Class.forName(name);
                            if (!clz.isInterface()) {
                                allClz.add(clz);
                            }
                        } catch (Exception | LinkageError ignored) {}
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
}
