package com.craft.ai.codecraft.core.saver;

import cn.hutool.core.util.StrUtil;
import com.craft.ai.codecraft.ai.model.HtmlCodeResult;
import com.craft.ai.codecraft.exception.BusinessException;
import com.craft.ai.codecraft.exception.ErrorCode;
import com.craft.ai.codecraft.model.enums.CodeGenTypeEnum;

/**
 * HTML代码文件保存器
 *
 * @author yupi
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        // 保存HTML文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        //HTML代码不能为空
        if(StrUtil.isBlank(result.getHtmlCode())){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容给不能为空");
        }
    }
}
