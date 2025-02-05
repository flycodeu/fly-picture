package com.fly.flyPicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fly.flyPicture.model.dto.picture.PictureQueryDto;
import com.fly.flyPicture.model.dto.picture.PictureReviewDto;
import com.fly.flyPicture.model.dto.picture.PictureUploadBatchDto;
import com.fly.flyPicture.model.dto.picture.PictureUploadDto;
import com.fly.flyPicture.model.dto.user.UserQueryDto;
import com.fly.flyPicture.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fly.flyPicture.model.entity.User;
import com.fly.flyPicture.model.vo.PictureVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author flycode
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-01-06 10:00:44
 */
public interface PictureService extends IService<Picture> {

    /**
     * 文件上传
     *
     * @param inputSource      文件输入源
     * @param pictureUploadDto
     * @param user
     * @return
     */
    PictureVo uploadPicture(Object inputSource, PictureUploadDto pictureUploadDto, User user);


    /**
     * 提取通用查询条件
     *
     * @param pictureQueryDto
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryDto pictureQueryDto);

    PictureVo getPictureVo(Picture picture, HttpServletRequest request);

    Page<PictureVo> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest request);

    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewDto
     * @param loginUser
     */
    void doPictureReview(PictureReviewDto pictureReviewDto, User loginUser);

    /**
     * 自动填充图片上传审核参数
     *
     * @param picture   图片
     * @param loginUser 登录用户
     */
    void filePictureParams(Picture picture, User loginUser);


    /**
     * 批量抓取图片并且上传本地图床
     *
     * @param pictureUploadBatchDto 图片搜索关键词
     * @param loginUser             登录用户，只能是管理员
     * @return 成功获取图片的数量
     */
    Integer uploadPictureBatch(PictureUploadBatchDto pictureUploadBatchDto, User loginUser);


    /**
     * 清理图库数据
     *
     * @param oldPicture
     */
    void clearPicture(Picture oldPicture);
}
