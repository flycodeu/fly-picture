package com.fly.flyPicture.model.dto.space;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fly.flyPicture.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 图库空间查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryDto extends PageRequest implements Serializable {

    private static final long serialVersionUID = -5457099171811315810L;

    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 创建用户 id
     */
    private Long userId;
}
