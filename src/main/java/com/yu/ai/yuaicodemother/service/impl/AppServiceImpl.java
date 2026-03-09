package com.yu.ai.yuaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yu.ai.yuaicodemother.core.AiCodeGeneratorFacade;
import com.yu.ai.yuaicodemother.exception.BusinessException;
import com.yu.ai.yuaicodemother.exception.ErrorCode;
import com.yu.ai.yuaicodemother.exception.ThrowUtils;
import com.yu.ai.yuaicodemother.model.dto.app.AppQueryRequest;
import com.yu.ai.yuaicodemother.model.entity.App;
import com.yu.ai.yuaicodemother.mapper.AppMapper;
import com.yu.ai.yuaicodemother.model.entity.User;
import com.yu.ai.yuaicodemother.model.enums.CodeGenTypeEnum;
import com.yu.ai.yuaicodemother.model.vo.AppVO;
import com.yu.ai.yuaicodemother.model.vo.UserVO;
import com.yu.ai.yuaicodemother.service.AppService;
import com.yu.ai.yuaicodemother.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yu.ai.yuaicodemother.constant.AppConstant.*;

/**
 * 服务层实现。
 *
 * @author 鱼皮
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        //1，参数校验
        ThrowUtils.throwIf(appId==null||appId<=0, ErrorCode.PARAMS_ERROR,"应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR,"提示词不能为空");
        //2，获取应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR);
        //3，判断应用是否属于当前用户
        if(!app.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        //4，获取生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throwIf(codeGenTypeEnum==null,ErrorCode.PARAMS_ERROR,"不支持的代码生成类型");
        //5，调用门面生成代码
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message,codeGenTypeEnum,appId);
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
        if(CollUtil.isEmpty(appList)){
            return new ArrayList<>();
        }
        //批量获取用户信息，避免 N + 1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app ->{
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public String deployApp(Long appId, User LoginUser) {
        // 1，参数校验
        ThrowUtils.throwIf(appId==null||appId<=0, ErrorCode.PARAMS_ERROR,"应用ID不能为空");
        ThrowUtils.throwIf(LoginUser==null,ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2，查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        // 3，判断用户是否有权限部署，仅本人可部署
        Long userId = app.getUserId();
        if(userId==null||!userId.equals(LoginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4，检查是否有deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成6位deployKey(大小写字母+数字)
        if(StrUtil.isBlank(deployKey)){
            deployKey = RandomUtil.randomString(6);
        }
        // 5，获取代码生成类型，构造原目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName=codeGenType+"_"+appId;
        String sourceDirPath = CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6，检查目录是否存在
        File sourceDir = new File(sourceDirPath);
        if(!FileUtil.exist(sourceDir)||!FileUtil.isDirectory(sourceDir)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7，复制文件到部署目录
        String deployDirPath = CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        File deployDir = new File(deployDirPath);
        FileUtil.copyContent(sourceDir, deployDir, true);
        // 8，更新app的deployKey和部署时间
        App updateApp = new App();
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        updateApp.setId(appId);
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult,ErrorCode.OPERATION_ERROR,"更新应用部署信息失败");
        // 9，放回可访问的 URL
        return String.format("%s/%s/", CODE_DEPLOY_HOST, deployKey);
    }
}
