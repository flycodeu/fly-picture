package com.fly.flyPicture.model.dto.space;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间新增请求
 * @author flycode
 */
@Data
public class SpaceAddDto implements Serializable {

    private static final long serialVersionUID = 5841268770302596535L;
    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

}
