package com.craft.ai.codecraft.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yu.ai.codecraft.ai.AiCodeGenTypeRoutingService;
import com.yu.ai.codecraft.ai.AiCodeGenTypeRoutingServiceFactory;
import com.yu.ai.codecraft.core.AiCodeGeneratorFacade;
import com.yu.ai.codecraft.core.builder.VueProjectBuilder;
import com.yu.ai.codecraft.core.handler.StreamHandlerExecutor;
import com.yu.ai.codecraft.exception.BusinessException;
import com.yu.ai.codecraft.exception.ErrorCode;
import com.yu.ai.codecraft.exception.ThrowUtils;
import com.yu.ai.codecraft.innerservice.InnerScreenshotService;
import com.yu.ai.codecraft.innerservice.InnerUserService;
import com.yu.ai.codecraft.mapper.AppMapper;
import com.yu.ai.codecraft.model.dto.app.AppAddRequest;
import com.yu.ai.codecraft.model.dto.app.AppQueryRequest;
import com.yu.ai.codecraft.model.entity.App;
import com.yu.ai.codecraft.model.entity.User;
import com.yu.ai.codecraft.model.enums.ChatHistoryMessageTypeEnum;
import com.yu.ai.codecraft.model.enums.CodeGenTypeEnum;
import com.yu.ai.codecraft.model.vo.AppVO;
import com.yu.ai.codecraft.model.vo.UserVO;
import com.yu.ai.codecraft.service.AppService;
import com.yu.ai.codecraft.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yu.ai.codecraft.constant.AppConstant.*;

/**
 * 服务层实现。
 *
 * @author 鱼皮
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @DubboReference
    private InnerUserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @DubboReference
    private InnerScreenshotService screenshotService;

    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        //1，参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        //2，获取应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        //3，判断应用是否属于当前用户
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        //4，获取应用的生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
        //5，通过校验后，添加用户消息到对话历史
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        //6，调用门面生成代码（流式）
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        //7，收集AI响应内容并在完成后记录到对话历史
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum);
    }

    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser) {
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化prompt不能为空");
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为initPrompt前12为位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 使用ai智能生成代码类型
        AiCodeGenTypeRoutingService routingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
        CodeGenTypeEnum selectedCodeGenType = routingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(selectedCodeGenType.getValue());
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("创建应用成功，ID：{}，类型：{}", app.getId(), selectedCodeGenType.getValue());
        return app.getId();
    }

    /**
     * 删除应用时关联删除对话历史
     *
     * @param id 应用id
     * @return 删除结果
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        //转换为 long 类型
        Long appId = Long.valueOf(id.toString());
        if (appId <= 0) {
            return false;
        }
        //先删除关联的对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            //记录日志但不影响应用删除
            log.error("删除应用关联历史对话失败：{}", e.getMessage());
        }
        //删除应用
        return super.removeById(id);
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        //关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        //批量获取用户信息，避免 N + 1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public String deployApp(Long appId, User LoginUser) {
        // 1，参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(LoginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2，查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3，判断用户是否有权限部署，仅本人可部署
        Long userId = app.getUserId();
        if (userId == null || !userId.equals(LoginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4，检查是否有deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成6位deployKey(大小写字母+数字)
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5，获取代码生成类型，构造原目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6，检查目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!FileUtil.exist(sourceDir) || !FileUtil.isDirectory(sourceDir)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7，Vue 项目特殊处理：执行构建
        if (CodeGenTypeEnum.VUE_PROJECT.getValue().equals(codeGenType)) {
            //vue项目构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue项目构建失败，请检查代码和依赖");
            //检查Dist目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!FileUtil.exist(distDir) || !FileUtil.isDirectory(distDir), ErrorCode.SYSTEM_ERROR, "Vue项目构建完成但未生成dist目录");
            //将dist目录作为部署源
            sourceDir = distDir;
            log.info("vue 项目构建成功，将部署dist目录：{}", distDir.getAbsolutePath());
        }
        // 8，复制文件到部署目录
        String deployDirPath = CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        File deployDir = new File(deployDirPath);
        FileUtil.copyContent(sourceDir, deployDir, true);
        // 9，更新app的deployKey和部署时间
        App updateApp = new App();
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        updateApp.setId(appId);
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 10，构建应用访问 URL
        String appDeployUrl = String.format("%s/%s/", CODE_DEPLOY_HOST, deployKey);
        // 11，异步生成截图并更新应用封面
        generateAppScreenShotAsync(appId, appDeployUrl);
        return appDeployUrl;
    }

    /**
     * 异步生成截图并更新应用封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    @Override
    public void generateAppScreenShotAsync(Long appId, String appUrl) {
        //使用虚拟线程异步执行
        Thread.startVirtualThread(() -> {
            // 调用截图生成服务并上传
            String ScreenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新应用封面字段
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(ScreenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });
    }
}
