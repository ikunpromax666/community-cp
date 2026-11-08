package com.community.communitycp.common.controller;

import com.community.communitycp.common.core.Result;
import com.community.communitycp.common.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * 文件接口
 */
@Tag(name = "文件接口")
@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 图片上传
     * 返回绝对 URL（含 scheme/host/port），前端可直接用于 <img> 或保存入库
     */
    @Operation(summary = "图片上传")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String relativePath = fileService.uploadImage(file);
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(relativePath)
                .toUriString();
        return Result.success(url);
    }
}
