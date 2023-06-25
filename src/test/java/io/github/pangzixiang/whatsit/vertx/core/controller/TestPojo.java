package io.github.pangzixiang.whatsit.vertx.core.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestPojo {
    private String key;
    private String value;
}
