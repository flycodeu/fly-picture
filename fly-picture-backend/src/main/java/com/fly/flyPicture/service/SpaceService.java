package com.fly.flyPicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fly.flyPicture.model.dto.space.SpaceAddDto;
import com.fly.flyPicture.model.dto.space.SpaceQueryDto;
import com.fly.flyPicture.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fly.flyPicture.model.entity.User;
import com.fly.flyPicture.model.vo.SpaceVo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author flycode
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-02-06 10:51:07
 */
public interface SpaceService extends IService<Space> {
    /**
     * 判断空间是否合规
     *
     * @param space
     * @param add
     */
    void validSpace(Space space, boolean add);

    /**
     * 获取空间vo
     *
     * @param space
     * @param request
     * @return
     */
    SpaceVo getSpaceVo(Space space, HttpServletRequest request);

    /**
     * 获取空间vo分页
     *
     * @param page
     * @param request
     * @return
     */
    Page<SpaceVo> getSpaceVoPage(Page<Space> page, HttpServletRequest request);


    /**
     * querywrapper查询请求
     *
     * @param spaceQueryDto
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryDto spaceQueryDto);


    /**
     * 根据空间等级填充空间
     *
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 添加空间
     *
     * @param spaceAddDto
     * @return
     */
    long addSpace(SpaceAddDto spaceAddDto, User loginUser);
}
