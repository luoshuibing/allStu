package com.changgou.file.controller;

import com.changgou.file.util.FastDFSClient;
import com.changgou.file.util.FastDFSFile;
import entity.Result;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@CrossOrigin
public class FileController {

    /***
     * 文件上传
     * @return
     */
    @PostMapping(value = "/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        // 封装一个FastDFSFile
        FastDFSFile fastDFSFile = new FastDFSFile(file.getOriginalFilename(), // 文件名字
                file.getBytes(),            // 文件字节数组
                StringUtils.getFilenameExtension(file.getOriginalFilename()));// 文件扩展名

        // 文件上传
        String[] uploads = FastDFSClient.upload(fastDFSFile);
        // 组装文件上传地址
        String fileUrl = FastDFSClient.getTrackerUrl() + "/" + uploads[0] + "/" + uploads[1];
        return Result.ok("上传成功", fileUrl);
    }



}
