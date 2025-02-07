package com.fly.flyPicture.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 图库编辑
 */
@Data
public class SpaceEditDto implements Serializable {

    private static final long serialVersionUID = 4083762595033681631L;

    /**
     * 空间id
     */
    private Long id;
    /**
     * 空间名称
     */
    private String spaceName;
}
