package com.fly.flyPicture.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户图片上传
 */
@Data
public class PictureUploadDto implements Serializable {
    private static final long serialVersionUID = -5502707038817266372L;

    private Long id;
}
