package io.github.pangzixiang.whatsit.vertx.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pangzixiang.whatsit.vertx.core.config.ApplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CoreUtils {

    private static final ObjectMapper objectMapper;

    private static final Pattern pattern = Pattern.compile("\\{(.*?)}");

    static {
        objectMapper = new ObjectMapper();
    }

    public static String objectToString(Object o) throws JsonProcessingException {
        return objectMapper.writeValueAsString(o);
    }

    public static <T> T StringToObject(String json, Class<T> clz) {
        return objectMapper.convertValue(json, clz);
    }

    public static String refactorControllerPath(String path, ApplicationConfiguration applicationConfiguration) {
        Matcher matcher = pattern.matcher(path);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = applicationConfiguration.getString(key);
            if (StringUtils.isNotBlank(value)) {
                log.debug("Parsing Router Path {} with [key: {}, value: {}]", path, key, value);
                path = path.replace(matcher.group(0), value);
            } else {
                String err = String.format("Failed to parse router URL [%s]! [key: %s, value: %s]"
                        , path, key, value);
                throw new IllegalArgumentException(err);
            }
        }

        return path.replaceAll("/+", "/");
    }
}
