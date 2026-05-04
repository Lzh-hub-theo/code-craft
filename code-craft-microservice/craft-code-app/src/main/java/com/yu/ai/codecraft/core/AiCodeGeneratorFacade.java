package com.yu.ai.codecraft.core;

import cn.hutool.json.JSONUtil;
import com.yu.ai.codecraft.ai.AiCodeGeneratorService;
import com.yu.ai.codecraft.ai.AiCodeGeneratorServiceFactory;
import com.yu.ai.codecraft.ai.model.HtmlCodeResult;
import com.yu.ai.codecraft.ai.model.MultiFileCodeResult;
import com.yu.ai.codecraft.ai.model.message.AiResponseMessage;
import com.yu.ai.codecraft.ai.model.message.ToolExecutedMessage;
import com.yu.ai.codecraft.ai.model.message.ToolRequestMessage;
import com.yu.ai.codecraft.constant.AppConstant;
import com.yu.ai.codecraft.core.builder.VueProjectBuilder;
import com.yu.ai.codecraft.core.parser.CodeParserExecutor;
import com.yu.ai.codecraft.core.saver.CodeFileSaverExecutor;
import com.yu.ai.codecraft.exception.BusinessException;
import com.yu.ai.codecraft.exception.ErrorCode;
import com.yu.ai.codecraft.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * Ai代码生成器门面,组合生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    VueProjectBuilder vueProjectBuilder;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }

        return switch (codeGenTypeEnum) {
            case HTML -> {
                //根据appId获取相应的AI服务实例
                AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
//                yield CodeFileSaver.saveHtmlCodeResult(result);
            }
            case MULTI_FILE -> {
                AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
//                yield CodeFileSaver.saveMultiFileCodeResult(result);
            }
            default -> {
                String errorMessage = "不支持生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }

        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);

        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream, appId);
            }
            default -> {
                String errorMessage = "不支持生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream
                .doOnNext(chunk -> {
                    //实时收集代码片段
                    codeBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    //流式返回完成后保存代码
                    try {
                        String completeCode = codeBuilder.toString();
                        //使用执行器解析代码
                        Object parseResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                        //使用执行器保存代码
                        File saveDir = CodeFileSaverExecutor.executeSaver(parseResult, codeGenType, appId);
                        log.info("保存成功，路径为：" + saveDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败：{}", e.getMessage());
                    }
                });
    }

    /**
     * 将TokenStream转换为Flux<String>, 并传递工具调用信息
     *
     * @param tokenStream TokenStream对象
     * @return Flux<String>流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse chatResponse) -> {
                        //同步构造 Vue 项目
                        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "vue_project_" + appId;
                        vueProjectBuilder.buildProject(projectPath);
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });

    }

//    ==========以下为旧代码，已弃用=============
//
//    /**
//     * 生成HTML模式的代码并保存
//     *
//     * @param userMessage 用户提示词
//     * @return 保存的目录
//     */
//    private File generateAndSaveHtmlCode(String userMessage) {
//        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
//        return CodeFileSaver.saveHtmlCodeResult(result);
//    }
//
//    /**
//     * 生成MULTI_FILE模式的代码并保存
//     *
//     * @param userMessage 用户提示词
//     * @return 保存的目录
//     */
//    private File generateAndSaveMultiFileCode(String userMessage) {
//        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
//        return CodeFileSaver.saveMultiFileCodeResult(result);
//    }
//
//
//    /**
//     * 生成 HTML 模式的代码并保存（流式）
//     *
//     * @param userMessage
//     * @return
//     */
//    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
//        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
//        //当流式返回生成代码完成后，在保存代码
//        StringBuilder codeBuilder = new StringBuilder();
//        return result
//                .doOnNext(chunk -> {
//                    //实时收集代码片段
//                    codeBuilder.append(chunk);
//                })
//                .doOnComplete(() -> {
//                    //流式返回完成后保存代码
//                    try {
//                        String completeHtmlCode = codeBuilder.toString();
//                        HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
//                        //保存代码到文件
//                        File saveDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
//                        log.info("保存成功，路径为：" + saveDir.getAbsolutePath());
//                    } catch (Exception e) {
//                        log.error("保存失败：{}", e.getMessage());
//                    }
//                });
//    }
//
//    /**
//     * 生成多文件模式的代码并保存
//     *
//     * @param userMessage 用户提示词
//     * @return 保存的目录
//     */
//    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
//        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
//        //当流式返回生成代码完成后，再保存代码
//        StringBuilder codeBuilder = new StringBuilder();
//        return result
//                .doOnNext(chunk -> {
//                    //实时收集代码片段
//                    codeBuilder.append(chunk);
//                })
//                .doOnComplete(() -> {
//                    //流式返回完成后保存代码
//                    try {
//                        String completeMultiFileCode = codeBuilder.toString();
//                        MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
//                        //保存代码到文件
//                        File saveDir = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
//                        log.info("保存成功，路径为：" + saveDir.getAbsolutePath());
//                    } catch (Exception e) {
//                        log.error("保存失败：{}", e.getMessage());
//                    }
//                });
//    }
}
