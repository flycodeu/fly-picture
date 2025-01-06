package com.fly.flyPicture.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fly.flyPicture.annotation.AuthCheck;
import com.fly.flyPicture.common.BaseResponse;
import com.fly.flyPicture.common.DeleteRequest;
import com.fly.flyPicture.common.ResultUtils;
import com.fly.flyPicture.constant.UserConstant;
import com.fly.flyPicture.exception.BusinessException;
import com.fly.flyPicture.exception.ErrorCode;
import com.fly.flyPicture.exception.ThrowUtils;
import com.fly.flyPicture.model.dto.picture.*;
import com.fly.flyPicture.model.entity.Picture;
import com.fly.flyPicture.model.entity.User;
import com.fly.flyPicture.model.vo.PictureTagCategory;
import com.fly.flyPicture.model.vo.PictureVo;
import com.fly.flyPicture.service.impl.PictureServiceImpl;
import com.fly.flyPicture.service.impl.UserServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 图片管理
 *
 * @author flycode
 */
@RequestMapping("/picture")
@RestController
public class PictureController {

    @Resource
    private PictureServiceImpl pictureService;
    @Resource
    private UserServiceImpl userService;

    /**
     * 图片上传
     *
     * @param file             文件
     * @param pictureUploadDto 图片id
     * @param request          session
     * @return 封装的图片
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVo> uploadPicture(@RequestPart("file") MultipartFile file, PictureUploadDto pictureUploadDto, HttpServletRequest request) {
        User user = userService.getLoginUserByRequest(request);
        PictureVo pictureVo = pictureService.uploadPicture(file, pictureUploadDto, user);
        return ResultUtils.success(pictureVo);
    }

    /**
     * 根据id删除图片
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserById(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUserByRequest(request);
        Long id = user.getId();
        // 数据是否存在
        Picture oldPicture = pictureService.getById(id);
        // 只有管理员或者当前用户才能删除
        if (!userService.isAdmin(user) || !oldPicture.getUserId().equals(id)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        boolean b = pictureService.removeById(deleteRequest.getId());
        if (!b) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 管理员修改
     *
     * @param pictureUpdateDto
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateDto pictureUpdateDto, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUpdateDto == null || pictureUpdateDto.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateDto, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateDto.getTags()));
        // 校验是否合规
        pictureService.validPicture(picture);
        // 数据是否存在
        Long id = pictureUpdateDto.getId();
        Picture oldPicture = pictureService.getById(id);
        if (oldPicture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 修改数据
        boolean result = pictureService.updateById(picture);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取图片
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPicture(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(picture);
    }

    /**
     * 用户根据id查询
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVo> getPictureVo(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        PictureVo pictureVo = PictureVo.objToVo(picture);
        return ResultUtils.success(pictureVo);
    }

    /**
     * @param pictureQueryDto
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> getPicturePage(@RequestBody PictureQueryDto pictureQueryDto, HttpServletRequest request) {
        int current = pictureQueryDto.getCurrent();
        int pageSize = pictureQueryDto.getPageSize();
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(pictureQueryDto));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页vo
     *
     * @param pictureQueryDto
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVo>> getPictureVoPage(@RequestBody PictureQueryDto pictureQueryDto, HttpServletRequest request) {
        int current = pictureQueryDto.getCurrent();
        int pageSize = pictureQueryDto.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);

        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(pictureQueryDto));

        return ResultUtils.success(pictureService.getPictureVoPage(picturePage, request));
    }

    /**
     * 用户编辑
     *
     * @param pictureEditDto
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditDto pictureEditDto, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditDto == null || pictureEditDto.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditDto, picture);
        // 将tags转换
        List<String> tags = pictureEditDto.getTags();
        picture.setTags(JSONUtil.toJsonStr(tags));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        pictureService.validPicture(picture);
        // 判断id是否存在
        Long id = pictureEditDto.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 设置用户id
        User user = userService.getLoginUserByRequest(request);
        // 仅限管理员和自己才能修改
        String userRole = user.getUserRole();
        if (!userRole.equals(UserConstant.ADMIN_ROLE) && !oldPicture.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 写入数据
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }


    /**
     * 先写固定标签种类
     *
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

}
