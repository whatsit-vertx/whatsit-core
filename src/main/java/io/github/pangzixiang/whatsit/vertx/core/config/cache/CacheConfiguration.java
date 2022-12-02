package io.github.pangzixiang.whatsit.vertx.core.config.cache;

import lombok.Data;

/**
 * The type Cache configuration.
 */
@Data
public class CacheConfiguration {
    private boolean enable = false;
    private int expireTime = 60;
    private int maxSize = 10;
    private int initSize = 1;
}
