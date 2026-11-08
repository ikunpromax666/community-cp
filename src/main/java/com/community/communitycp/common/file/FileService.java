package com.community.communitycp.common.file;

import com.community.communitycp.common.core.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传服务
 * 落盘策略：{uploadPath}/yyyy/MM/dd/{uuid}.{ext}
 */
@Service
public class FileService {

    /**
     * 允许的图片后缀
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${community.upload.path}")
    private String uploadPath;

    @Value("${community.upload.url-prefix}")
    private String urlPrefix;

    /**
     * 上传图片，返回相对访问路径（如 /uploads/2026/11/07/uuid.jpg）
     */
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的图片");
        }

        // 1. 取后缀并校验
        String original = file.getOriginalFilename();
        if (original == null || !original.contains(".")) {
            throw new BizException("文件名不合法");
        }
        String ext = original.substring(original.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BizException("仅支持 jpg/jpeg/png/gif/webp 格式");
        }

        // 2. 按日期分目录，避免单目录文件过多
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path dir = Paths.get(uploadPath, dateDir);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new BizException("文件保存失败");
        }

        // 3. UUID 重命名，防冲突和中文乱码
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = dir.resolve(fileName);
        try {
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new BizException("文件保存失败");
        }

        // 4. 返回相对路径，由 Controller 拼接成绝对 URL
        return urlPrefix + "/" + dateDir + "/" + fileName;
    }
}
