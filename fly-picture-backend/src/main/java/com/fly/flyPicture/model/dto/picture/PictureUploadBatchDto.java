package com.fly.flyPicture.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片批量上传Dto
 */
@Data
public class PictureUploadBatchDto implements Serializable {

    private static final long serialVersionUID = 6357819080909315957L;

    /**
     * 搜索文本
     */
    private String searchText;

    /**
     * 搜索数量
     */
    private Integer count = 10;

    /**
     * 图片名称前缀
     */
    private String namePrefix;
}
