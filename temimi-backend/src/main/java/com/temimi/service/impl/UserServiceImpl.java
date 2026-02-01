package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.constant.BusinessConstants;
import com.temimi.exception.BusinessException;
import com.temimi.exception.BusinessErrorCode;
import com.temimi.mapper.UserMapper;
import com.temimi.model.entity.User;
import com.temimi.service.CoinService;
import com.temimi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CoinService coinService;

    @Value("${file.upload.path:D:/shiyou_upload/}")
    private String uploadBasePath;

    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(BusinessErrorCode.USER_LOGIN_FAILED);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(BusinessErrorCode.USER_LOGIN_FAILED);
        }

        if (user.getState() == BusinessConstants.USER_STATE_BANNED) {
            throw new BusinessException(BusinessErrorCode.USER_ACCOUNT_BANNED);
        } else if (user.getState() == BusinessConstants.USER_STATE_DELETED) {
            throw new BusinessException(BusinessErrorCode.USER_ACCOUNT_BANNED, "账号已注销");
        }

        // 每日登录奖励
        coinService.dailyLoginReward(user.getUid());

        user.setPassword(null);
        return user;
    }

    @Override
    public boolean register(User user) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(user.getUsername());
        if (existingUser != null) {
            throw new BusinessException(BusinessErrorCode.USER_ALREADY_EXISTS);
        }

        // ✅ 修复：在插入前设置nickname临时值，避免NOT NULL约束错误
        String tempNickname = "临时昵称_" + System.currentTimeMillis();
        if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
            user.setNickname(tempNickname);
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        user.setExp(BusinessConstants.STATS_INITIAL_VALUE);
        user.setCoin(0.0);
        user.setVip(BusinessConstants.STATS_INITIAL_VALUE);
        user.setState(BusinessConstants.USER_STATE_NORMAL);
        user.setRole(BusinessConstants.STATS_INITIAL_VALUE);
        user.setAuth(BusinessConstants.STATS_INITIAL_VALUE);
        user.setCreateDate(LocalDateTime.now());

        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException(BusinessErrorCode.DATABASE_ERROR, "注册失败，请稍后重试");
        }

        // ✅ 注册成功后，用uid生成最终昵称
        if (user.getNickname().equals(tempNickname)) {
            user.setNickname("用户_" + user.getUid());
            userMapper.updateById(user);
        }

        return true;
    }

    @Override
    public boolean updateUserInfo(User user) {
        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException(BusinessErrorCode.DATABASE_ERROR, "更新用户信息失败");
        }
        return true;
    }

    @Override
    public List<User> searchUsers(String keyword) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("username", keyword)
                .or()
                .like("nickname", keyword)
                .eq("state", BusinessConstants.USER_STATE_NORMAL);
        return userMapper.selectList(queryWrapper);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public User getUserById(Integer uid) {
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BusinessException(BusinessErrorCode.USER_NOT_FOUND);
        }

        user.setPassword(null);
        return user;
    }

    /**
     * 根据用户ID查询用户（安全版本，不抛异常）
     */
    @Override
    public User getUserByIdSafe(Integer uid) {
        User user = userMapper.selectById(uid);
        if (user != null) {
            user.setPassword(null);
        }
        return user;  // 不存在时返回null，不抛异常
    }

    @Override
    public boolean existsByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        return user != null;
    }
    
    @Override
    public boolean existsByNickname(String nickname) {
        User user = userMapper.selectByNickname(nickname);
        return user != null;
    }

    @Override
    public String uploadAvatar(MultipartFile file, Integer uid) throws IOException {
        // 1. 验证文件
        validateAvatarFile(file);

        // 2. 创建保存目录
        File dir = new File(uploadBasePath + "avatars");
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new BusinessException(BusinessErrorCode.FILE_UPLOAD_FAILED, "创建头像目录失败");
            }
        }

        // 3. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = "avatar_" + uid + "_" + System.currentTimeMillis() + fileExtension;

        // 4. 保存文件
        File destFile = new File(dir, newFilename);
        file.transferTo(destFile);

        // 5. 生成URL - 使用后端服务器的相对路径
        String avatarUrl = "/avatars/" + newFilename;

        // 调试日志：记录保存的文件路径和URL
        System.out.println("=== 头像上传调试信息 ===");
        System.out.println("文件保存路径: " + destFile.getAbsolutePath());
        System.out.println("文件是否存在: " + destFile.exists());
        System.out.println("返回的URL: " + avatarUrl);

        // 6. 更新用户头像URL
        updateUserAvatar(uid, avatarUrl);

        return avatarUrl;
    }

    /**
     * 验证头像文件
     */
    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BusinessErrorCode.FILE_UPLOAD_FAILED, "文件不能为空");
        }

        // 检查文件大小（最大5MB）
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(BusinessErrorCode.FILE_UPLOAD_FAILED, "文件大小不能超过5MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null ||
            (!contentType.equals("image/jpeg") &&
             !contentType.equals("image/jpg") &&
             !contentType.equals("image/png") &&
             !contentType.equals("image/gif"))) {
            throw new BusinessException(BusinessErrorCode.FILE_UPLOAD_FAILED, "只支持 JPG、PNG、GIF 格式的图片");
        }

        // 检查文件扩展名
        String filename = file.getOriginalFilename();
        if (filename == null || !hasValidImageExtension(filename)) {
            throw new BusinessException(BusinessErrorCode.FILE_UPLOAD_FAILED, "文件扩展名不正确");
        }

        // 防止路径遍历攻击
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new BusinessException(BusinessErrorCode.FILE_UPLOAD_FAILED, "文件名包含非法字符");
        }
    }

    /**
     * 检查图片文件扩展名
     */
    private boolean hasValidImageExtension(String filename) {
        String lowerFilename = filename.toLowerCase();
        return lowerFilename.endsWith(".jpg") ||
               lowerFilename.endsWith(".jpeg") ||
               lowerFilename.endsWith(".png") ||
               lowerFilename.endsWith(".gif");
    }

    @Override
    public boolean updateUserAvatar(Integer uid, String avatarUrl) {
        User user = new User();
        user.setUid(uid);
        user.setAvatar(avatarUrl);

        int result = userMapper.updateById(user);
        return result > 0;
    }
    
    @Override
    public boolean validatePassword(Integer uid, String oldPassword) {
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BusinessException(BusinessErrorCode.USER_NOT_FOUND);
        }
        
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }
    
    @Override
    public boolean updatePassword(Integer uid, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        
        User user = new User();
        user.setUid(uid);
        user.setPassword(encodedPassword);
        
        int result = userMapper.updateById(user);
        return result > 0;
    }

    @Override
    public String uploadBackground(MultipartFile file, Integer uid) throws IOException {
        // 1. 验证文件
        validateBackgroundFile(file);

        // 2. 创建保存目录
        File dir = new File(uploadBasePath + "backgrounds");
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new BusinessException(BusinessErrorCode.FILE_UPLOAD_FAILED, "创建背景图目录失败");
            }
        }

        // 3. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = "bg_" + uid + "_" + System.currentTimeMillis() + fileExtension;

        // 4. 保存文件
        File destFile = new File(dir, newFilename);
        file.transferTo(destFile);

        // 5. 生成URL
        String bgUrl = "/backgrounds/" + newFilename;

        // 6. 更新用户背景图URL
        updateBackground(uid, bgUrl);

        return bgUrl;
    }

    @Override
    public boolean updateBackground(Integer uid, String bgUrl) {
        User user = new User();
        user.setUid(uid);
        user.setBackground(bgUrl);
        int result = userMapper.updateById(user);
        return result > 0;
    }

    /**
     * 验证背景图文件
     */
    private void validateBackgroundFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BusinessErrorCode.FILE_UPLOAD_FAILED, "文件不能为空");
        }

        // 检查文件大小（最大20MB）
        long maxSize = 20 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(BusinessErrorCode.FILE_UPLOAD_FAILED, "文件大小不能超过20MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null ||
            (!contentType.equals("image/jpeg") &&
             !contentType.equals("image/jpg") &&
             !contentType.equals("image/png"))) {
            throw new BusinessException(BusinessErrorCode.FILE_UPLOAD_FAILED, "只支持 JPG、PNG 格式的图片");
        }

        // 检查文件扩展名
        String filename = file.getOriginalFilename();
        if (filename == null || !hasValidImageExtension(filename)) {
            throw new BusinessException(BusinessErrorCode.FILE_UPLOAD_FAILED, "文件扩展名不正确");
        }

        // 防止路径遍历攻击
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new BusinessException(BusinessErrorCode.FILE_UPLOAD_FAILED, "文件名包含非法字符");
        }
    }
}
