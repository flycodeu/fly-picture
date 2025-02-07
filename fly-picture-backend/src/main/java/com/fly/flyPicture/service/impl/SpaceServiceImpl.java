package com.fly.flyPicture.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fly.flyPicture.exception.BusinessException;
import com.fly.flyPicture.exception.ErrorCode;
import com.fly.flyPicture.exception.ThrowUtils;
import com.fly.flyPicture.model.dto.space.SpaceAddDto;
import com.fly.flyPicture.model.dto.space.SpaceQueryDto;
import com.fly.flyPicture.model.entity.Picture;
import com.fly.flyPicture.model.entity.Space;
import com.fly.flyPicture.model.entity.User;
import com.fly.flyPicture.model.enums.SpaceLevelEnum;
import com.fly.flyPicture.model.enums.UserRoleEnum;
import com.fly.flyPicture.model.vo.PictureVo;
import com.fly.flyPicture.model.vo.SpaceVo;
import com.fly.flyPicture.model.vo.UserVo;
import com.fly.flyPicture.service.SpaceService;
import com.fly.flyPicture.mapper.SpaceMapper;
import com.qcloud.cos.utils.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author flycode
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-02-06 10:51:07
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {
    @Resource
    private UserServiceImpl userService;
    @Resource
    private TransactionTemplate transactionTemplate;

    Map<Long, Object> localMap = new ConcurrentHashMap<>();

    @Override
    public void validSpace(Space space, boolean add) {
        // 判断space是否为空
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR, "空间参数为空");
        // 获取数据
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        // 判断是否新增
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }

            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间等级不能为空");
            }

            if (spaceName.length() > 30) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称长度不能超过30");
            }
        }

        // 修改
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间等级错误");
        }

        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
    }

    @Override
    public SpaceVo getSpaceVo(Space space, HttpServletRequest request) {
        SpaceVo spaceVo = new SpaceVo();
        BeanUtils.copyProperties(space, spaceVo);
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            spaceVo.setUserVo(userService.getUserVo(user));
        }
        return spaceVo;
    }

    @Override
    public Page<SpaceVo> getSpaceVoPage(Page<Space> spacePage, HttpServletRequest request) {
        // 1. 封装分页vo
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVo> spaceVoPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVoPage;
        }
        List<SpaceVo> spaceVoList = spaceList.stream().map(SpaceVo::objToVo).collect(Collectors.toList());
        // 2. 关联用户信息
        // 2.1 查询出当前图片表里面所有的用户id,去重,set
        Set<Long> idSet = spaceVoList.stream().map(SpaceVo::getUserId).collect(Collectors.toSet());
        // 2.2 查询当前图片表用户id集合存在用户表中数据 1,user1  2,user2
        Map<Long, List<User>> userMap = userService.listByIds(idSet).stream().collect(Collectors.groupingBy(User::getId));
        // 2.3 设置用户vo
        spaceVoList.forEach(spaceVo -> {
            Long userId = spaceVo.getUserId();
            User user = null;
            if (userMap.containsKey(userId)) {
                user = userMap.get(userId).get(0);
            }
            UserVo userVo = userService.getUserVo(user);
            spaceVo.setUserVo(userVo);
        });
        spaceVoPage.setRecords(spaceVoList);
        return spaceVoPage;
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryDto spaceQueryDto) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryDto == null) {
            return queryWrapper;
        }
        Long id = spaceQueryDto.getId();
        String spaceName = spaceQueryDto.getSpaceName();
        Integer spaceLevel = spaceQueryDto.getSpaceLevel();
        Long userId = spaceQueryDto.getUserId();
        String sortField = spaceQueryDto.getSortField();
        String sortOrder = spaceQueryDto.getSortOrder();

        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);

        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotNull(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.PARAMS_ERROR);
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        if (spaceLevelEnum != null) {
            // 最大容量为空，设置默认容量
            if (space.getMaxCount() == null) {
                space.setMaxCount(spaceLevelEnum.getMaxCount());
            }
            // 最大空间大小为空，设置默认空间大小
            if (space.getMaxSize() == null) {
                space.setMaxSize(spaceLevelEnum.getMaxSize());
            }
        }
    }

    @Override
    public long addSpace(SpaceAddDto spaceAddDto, User loginUser) {
        // 1. 填充参数默认值
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddDto, space);
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.Common.getValue());
        }
        if (space.getSpaceName() == null) {
            space.setSpaceName("默认空间");
        }
        // 填充大小
        fillSpaceBySpaceLevel(space);
        Long userId = loginUser.getId();
        space.setUserId(userId);

        // 2. 校验参数
        validSpace(space, true);

        // 3. 校验权限，普通用户只能创建普通权限空间
        if (SpaceLevelEnum.Common.getValue() != space.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 4. 控制一个用户只能有一个空间 加锁+事务
        Object lock = localMap.computeIfAbsent(userId, k -> new Object());

        // 但是使用intern不会释放资源
        //String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            Long newSpaceId = transactionTemplate.execute(status -> {
                // 判断该用户是否有空间
                boolean exists = this.lambdaQuery().eq(Space::getUserId, userId).exists();
                // 有空间，报错
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户只能创建一个空间");
                // 没有空间，新增
                try {
                    boolean result = this.save(space);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建空间失败");
                    return space.getId();
                } finally {
                    localMap.remove(userId);
                }
            });
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }
}




