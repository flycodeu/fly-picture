package com.fly.flyPicture.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片编辑(用户)
 * @author flycode
 */
@Data
public class PictureEditDto implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;


    private Long spaceId;
    

    private static final long serialVersionUID = 1L;
}
