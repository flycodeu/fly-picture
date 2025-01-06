package com.fly.flyPicture.mapper;

import com.fly.flyPicture.model.entity.Picture;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;

/**
* @author flycode
* @description 针对表【picture(图片)】的数据库操作Mapper
* @createDate 2025-01-06 10:00:44
* @Entity com.fly.flyPicture.model.entity.Picture
*/
@Mapper
public interface PictureMapper extends BaseMapper<Picture> {

}




