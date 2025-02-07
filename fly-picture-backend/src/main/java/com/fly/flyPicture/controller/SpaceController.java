package com.fly.flyPicture.controller;

import cn.hutool.core.util.RandomUtil;
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
import com.fly.flyPicture.model.dto.space.*;
import com.fly.flyPicture.model.entity.Space;
import com.fly.flyPicture.model.entity.User;
import com.fly.flyPicture.model.enums.SpaceLevelEnum;
import com.fly.flyPicture.model.vo.SpaceVo;
import com.fly.flyPicture.service.impl.SpaceServiceImpl;
import com.fly.flyPicture.service.impl.UserServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequestMapping("/space")
@RestController
public class SpaceController {
    //ctrl+shift+-全部折叠
    @Resource
    private SpaceServiceImpl spaceService;
    @Resource
    private UserServiceImpl userService;

    /**
     * 创建空间
     *
     * @param spaceAddDto
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddDto spaceAddDto, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddDto == null, ErrorCode.PARAMS_ERROR, "参数为空");
        User user = userService.getLoginUserByRequest(request);
        long l = spaceService.addSpace(spaceAddDto, user);
        return ResultUtils.success(l);
    }

    /**
     * 根据id删除空间
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteSpaceById(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUserByRequest(request);
        Long id = user.getId();
        // 数据是否存在
        Space oldSpace = spaceService.getById(deleteRequest.getId());
        // 只有管理员或者当前用户才能删除
        if (!userService.isAdmin(user) || !oldSpace.getUserId().equals(id)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        boolean b = spaceService.removeById(deleteRequest.getId());
        if (!b) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 管理员修改空间信息
     *
     * @param spaceUpdateDto
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateDto spaceUpdateDto, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUpdateDto == null || spaceUpdateDto.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateDto, space);
        // 校验是否合规
        spaceService.validSpace(space, false);
        // 设置自动填充参数
        spaceService.fillSpaceBySpaceLevel(space);
        // 数据是否存在
        Long id = spaceUpdateDto.getId();
        Space oldSpace = spaceService.getById(id);
        if (oldSpace == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 修改数据
        boolean result = spaceService.updateById(space);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取空间
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpace(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(space);
    }

    /**
     * 用户根据id查询
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVo> getSpaceVo(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        SpaceVo spaceVo = spaceService.getSpaceVo(space, request);
        return ResultUtils.success(spaceVo);
    }

    /**
     * @param spaceQueryDto
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> getSpacePage(@RequestBody SpaceQueryDto spaceQueryDto, HttpServletRequest request) {
        int current = spaceQueryDto.getCurrent();
        int pageSize = spaceQueryDto.getPageSize();
        Page<Space> spacePage = spaceService.page(new Page<>(current, pageSize), spaceService.getQueryWrapper(spaceQueryDto));
        return ResultUtils.success(spacePage);
    }

    /**
     * 分页vo
     *
     * @param spaceQueryDto
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVo>> getSpaceVoPage(@RequestBody SpaceQueryDto spaceQueryDto, HttpServletRequest request) {
        int current = spaceQueryDto.getCurrent();
        int pageSize = spaceQueryDto.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户查看默认通过数据
        Page<Space> spacePage = spaceService.page(new Page<>(current, pageSize), spaceService.getQueryWrapper(spaceQueryDto));

        return ResultUtils.success(spaceService.getSpaceVoPage(spacePage, request));
    }

    /**
     * 用户编辑
     *
     * @param spaceEditDto
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditDto spaceEditDto, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceEditDto == null || spaceEditDto.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditDto, space);
        // 设置编辑时间
        space.setEditTime(new Date());
        // 数据校验
        spaceService.validSpace(space, false);
        // 自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        // 判断id是否存在
        Long id = spaceEditDto.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 设置用户id
        User user = userService.getLoginUserByRequest(request);


        // 仅限管理员和自己才能修改
        String userRole = user.getUserRole();
        if (!userRole.equals(UserConstant.ADMIN_ROLE) && !oldSpace.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 写入数据
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }

    /**
     * 获取所有的空间等级
     *
     * @param spaceLevelDto
     * @return
     */
    @GetMapping("/list/spaceLevel")
    public BaseResponse<List<SpaceLevelDto>> getSpaceLevel(SpaceLevelDto spaceLevelDto) {
        List<SpaceLevelDto> spaceLevelDtos = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevelDto(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()
                )).collect(Collectors.toList());

        return ResultUtils.success(spaceLevelDtos);
    }

}
