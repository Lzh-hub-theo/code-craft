package com.yu.ai.yuaicodemother.core.saver;

import cn.hutool.core.util.StrUtil;
import com.yu.ai.yuaicodemother.ai.model.MultiFileCodeResult;
import com.yu.ai.yuaicodemother.exception.BusinessException;
import com.yu.ai.yuaicodemother.exception.ErrorCode;
import com.yu.ai.yuaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 多文件代码文件保存器
 *
 * @author yupi
 */
public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        //保存HTML文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        //保存CSS文件
        writeToFile(baseDirPath, "style.css", result.getCssCode() == null ? "" : result.getCssCode());
        //保存JavaScript文件
        writeToFile(baseDirPath, "script.js", result.getJsCode() == null ? "" : result.getJsCode());
    }

    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        //至少要有HTML代码，CSS和JS可以为空
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }
}
