package com.fly.flyPicture.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 前端用于查询空间级别相关信息
 */
@Data
@AllArgsConstructor
public class SpaceLevelDto {

    private int value;

    private String text;

    private long maxCount;

    private long maxSize;
}
