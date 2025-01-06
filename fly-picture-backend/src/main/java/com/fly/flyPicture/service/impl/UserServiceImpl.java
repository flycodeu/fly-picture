package com.fly.flyPicture.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fly.flyPicture.constant.UserConstant;
import com.fly.flyPicture.exception.BusinessException;
import com.fly.flyPicture.exception.ErrorCode;
import com.fly.flyPicture.exception.ThrowUtils;
import com.fly.flyPicture.model.dto.user.UserAddDto;
import com.fly.flyPicture.model.dto.user.UserQueryDto;
import com.fly.flyPicture.model.dto.user.UserUpdateDto;
import com.fly.flyPicture.model.entity.User;
import com.fly.flyPicture.model.enums.UserRoleEnum;
import com.fly.flyPicture.model.vo.UserLoginVo;
import com.fly.flyPicture.model.vo.UserVo;
import com.fly.flyPicture.service.UserService;
import com.fly.flyPicture.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author flycode
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-12-24 15:21:38
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验参数，使用validate进行判断
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. 判断账号是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        // 3. 密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserName("无名");
        user.setUserPassword(encryptPassword);
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，请重试");
        }
        return user.getId();
    }

    @Override
    public UserLoginVo userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //  1. 校验参数

        // 2. 判断用户是否存在
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUserAccount, userAccount));
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);

        // 3. 校验密码
        String dbUserPassword = user.getUserPassword();
        String encryptPassword = getEncryptPassword(userPassword);
        if (!dbUserPassword.equals(encryptPassword)) {
            log.info("user login failed,userAccount:{}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }

        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);

        // 4.登录成功
        return this.getLoginUserVo(user);
    }

    /**
     * 返回分装的用户信息
     *
     * @param user
     * @return
     */
    @Override
    public UserLoginVo getLoginUserVo(User user) {
        if (user == null) {
            return null;
        }
        UserLoginVo userLoginVo = new UserLoginVo();
        BeanUtils.copyProperties(user, userLoginVo);
        return userLoginVo;
    }

    @Override
    public UserVo getUserVo(User user) {
        if (user == null) {
            return null;
        }
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        return userVo;
    }

    @Override
    public List<UserVo> getUserVoList(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        List<UserVo> userVoList = userList.stream().map(this::getUserVo).collect(Collectors.toList());
        return userVoList;
    }

    @Override
    public User getLoginUserByRequest(HttpServletRequest request) {
        // 1. 判断是否登录过
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) attribute;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 2. 用户登录有对应信息
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public Boolean userLogOut(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (attribute == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    public String getEncryptPassword(String userPassword) {
        final String SALT = "flycodeu";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }


    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryDto userQueryDto) {
        if (userQueryDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryDto.getId();
        String userAccount = userQueryDto.getUserAccount();
        String userName = userQueryDto.getUserName();
        String userProfile = userQueryDto.getUserProfile();
        String userRole = userQueryDto.getUserRole();
        String sortField = userQueryDto.getSortField();
        String sortOrder = userQueryDto.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public Boolean addUser(UserAddDto userAddDto) {
        if (userAddDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新增用户参数为空");
        }
        User user = new User();
        BeanUtils.copyProperties(userAddDto, user);
        String encryptPassword = getEncryptPassword(UserConstant.DEFAULT_PASSWROD);
        user.setUserPassword(encryptPassword);
//        long count = this.count(new QueryWrapper<User>().eq("userAccount", user.getUserAccount()));
//        if (count > 0) {
//         throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号名已存在");
//        }
        Long count = this.baseMapper.userCount(userAddDto.getUserAccount());
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号名已存在");
        }
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "新增用户失败");
        }
        return true;
    }

    @Override
    public Boolean updateUser(UserUpdateDto userUpdateDto) {
        if (userUpdateDto == null || userUpdateDto.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新用户参数为空");
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateDto, user);
        boolean update = this.updateById(user);
        if (!update) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新用户失败");
        }
        return true;
    }

}




