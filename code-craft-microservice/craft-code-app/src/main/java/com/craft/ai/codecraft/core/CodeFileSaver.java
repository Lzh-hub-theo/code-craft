package com.craft.ai.codecraft.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.yu.ai.codecraft.ai.model.HtmlCodeResult;
import com.yu.ai.codecraft.ai.model.MultiFileCodeResult;
import com.yu.ai.codecraft.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static com.yu.ai.codecraft.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

@Deprecated
public class CodeFileSaver {

    //文件保存根目录
    private static final String FILE_SAVE_ROOT_DIR = CODE_OUTPUT_ROOT_DIR;

    /**
     * 保存 HtmlCodeResult
     * @param result
     * @return
     */
    public static File saveHtmlCodeResult(HtmlCodeResult result){
        //构建唯一目录路径
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        //写入单个文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        return new File(baseDirPath);
    }

    /**
     * 保存 MultiFileCodeResult
     * @param result
     * @return
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult result){
        //构建唯一目录路径
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        //写入单个文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        writeToFile(baseDirPath, "script.js", result.getJsCode());
        return new File(baseDirPath);
    }

    /**
     * 构建唯一目录路径 ： tmp/code_output/bizType_雪花ID
     * @param bizType
     * @return
     */
    private static String buildUniqueDir(String bizType){
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     * @param dirName
     * @param fileName
     * @param content
     */
    private static void writeToFile(String dirName, String fileName, String content){
        String filePath = dirName + File.separator + fileName;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}
