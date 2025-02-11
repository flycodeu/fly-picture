package com.fly.flyPicture.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fly.flyPicture.model.entity.Picture;
import com.fly.flyPicture.model.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 返回图片信息
 *
 * @author flycode
 */
@Data
public class PictureVo implements Serializable {

    private static final long serialVersionUID = -2655776231733327342L;
    /**
     * id
     */
    private Long id;

    /**
     * 图片 url
     */
    private String url;

    /**
     *  图片缩略图 url
     */
    private String thumbnailUrl;

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

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 上传者用户信息
     */
    private UserVo user;


    private Long spaceId;
    /**
     * 图片vo转换为实体类
     *
     * @param pictureVo
     * @return
     */
    public static Picture voToObj(PictureVo pictureVo) {
        if (pictureVo == null) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureVo, picture);
        // 转换tags
        picture.setTags(JSONUtil.toJsonStr(pictureVo.getTags()));
        return picture;
    }

    /**
     * 实体类转换为Vo
     *
     * @param picture
     * @return
     */
    public static PictureVo objToVo(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVo pictureVo = new PictureVo();
        BeanUtils.copyProperties(picture, pictureVo);
        pictureVo.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVo;
    }
}
