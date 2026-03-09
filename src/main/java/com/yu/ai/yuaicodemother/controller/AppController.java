package com.yu.ai.yuaicodemother.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.yu.ai.yuaicodemother.annotation.AuthCheck;
import com.yu.ai.yuaicodemother.common.BaseResponse;
import com.yu.ai.yuaicodemother.common.DeleteRequest;
import com.yu.ai.yuaicodemother.common.ResultUtils;
import com.yu.ai.yuaicodemother.constant.AppConstant;
import com.yu.ai.yuaicodemother.constant.UserConstant;
import com.yu.ai.yuaicodemother.exception.BusinessException;
import com.yu.ai.yuaicodemother.exception.ErrorCode;
import com.yu.ai.yuaicodemother.exception.ThrowUtils;
import com.yu.ai.yuaicodemother.model.dto.app.*;
import com.yu.ai.yuaicodemother.model.entity.User;
import com.yu.ai.yuaicodemother.model.enums.CodeGenTypeEnum;
import com.yu.ai.yuaicodemother.model.enums.UserRoleEnum;
import com.yu.ai.yuaicodemother.model.vo.AppVO;
import com.yu.ai.yuaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import com.yu.ai.yuaicodemother.model.entity.App;
import com.yu.ai.yuaicodemother.service.AppService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 *  控制层。
 *
 * @author 鱼皮
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    /**
     * 应用聊天生成代码（流式）
     * MediaType.TEXT_EVENT_STREAM_VALUE 相当于响应头的 Content-Type: text/event-stream
     * 1，解决SSE空格丢失问题：将原本的返回值封装到JSON中
     * 2，区分是正常结束还是异常中断，主动告诉前端是否生成完成
     *
     * @param appId 应用id
     * @param message 提示词
     * @param request 请求
     * @return 生成结果流
     */
    @GetMapping(value="/chat/gen/code", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       HttpServletRequest request){
        //参数校验
        ThrowUtils.throwIf(appId==null||appId<=0,ErrorCode.PARAMS_ERROR,"应用ID无效");
        ThrowUtils.throwIf(StrUtil.isBlank(message),ErrorCode.PARAMS_ERROR,"提示词不能为空");
        //获取当前用户
        User loginUser = userService.getLoginUser(request);
        //调用服务生成代码（流式）
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
        //转换为ServerSentEvent格式
        return contentFlux
                .map(chunk -> {
                    //将内容包装成json对象
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                })
                .concatWith(Mono.just(
                        //发送结束事件
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }

    @PostMapping("deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest,HttpServletRequest request){
        //参数校验
        ThrowUtils.throwIf(appDeployRequest==null,ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId==null||appId<=0,ErrorCode.PARAMS_ERROR,"应用ID不能为空");
        //获取用户
        User loginUser = userService.getLoginUser(request);
        //调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }

    /**
     * 创建应用
     * @param appAddRequest 创建应用请求
     * @param request 请求
     * @return 应用id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request){
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt),ErrorCode.PARAMS_ERROR,"初始化prompt不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest,app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为initPrompt前12为位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(),12)));
        // 暂时设置为多文件生成
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        // 插入数据库
        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(app.getId());
    }

    /**
     * 更新应用
     * @param appUpdateRequest 更新应用请求
     * @param request 请求
     * @return 更新结果
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest,HttpServletRequest request){
        if(appUpdateRequest == null || appUpdateRequest.getId() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long id = appUpdateRequest.getId();
        //判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null,ErrorCode.NOT_FOUND_ERROR);
        //仅本人可更新
        if(!oldApp.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        App app = new App();
        app.setId(id);
        app.setAppName(appUpdateRequest.getAppName());
        //设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 删除应用
     * @param deleteRequest 删除请求
     * @param request 请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request){
        if(deleteRequest == null || deleteRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long id = deleteRequest.getId();
        //判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        //仅本人或管理员可删除
        if(!oldApp.getUserId().equals(loginUser.getId()) && !UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取应用详情
     *
     * @param id 应用id
     * @return 应用详情
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(long id){
        ThrowUtils.throwIf(id<=0,ErrorCode.PARAMS_ERROR);
        //查询数据库
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null,ErrorCode.NOT_FOUND_ERROR);
        //获取封装类（包含用户信息）
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 分页获取当前用户创建的应用列表
     *
     * @param appQueryRequest 查询请求
     * @param request 请求
     * @return 应用列表
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> ListMyAppVOByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request){
        ThrowUtils.throwIf(appQueryRequest == null,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        //限制每页最多20个
        int pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize>20,ErrorCode.PARAMS_ERROR,"每页最多查询20个应用");
        int pageNum = appQueryRequest.getPageNum();
        //只查询当前用户的应用
        appQueryRequest.setUserId(loginUser.getId());
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        //数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 分页获取精选应用列表
     *
     * @param appQueryRequest 查询请求
     * @param request 请求
     * @return 应用列表
     */
    @PostMapping("/good/list/page/vo")
    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request){
        ThrowUtils.throwIf(appQueryRequest==null, ErrorCode.PARAMS_ERROR);
        int pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize>20,ErrorCode.PARAMS_ERROR,"每页最多查询20个应用");
        int pageNum = appQueryRequest.getPageNum();
        //只查询精选的应用
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        //分页查询
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        //数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 管理员删除应用
     *
     * @param deleteRequest 删除请求
     * @return 删除结果
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole= UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest){
        if(deleteRequest==null || deleteRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = deleteRequest.getId();
        //判断是否存在
        App app = appService.getById(id);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR);
        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 管理员更新应用
     *
     * @param appAdminUpdateRequest 更新请求
     * @return 更新结果
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest){
        if(appAdminUpdateRequest==null||appAdminUpdateRequest.getId()==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = appAdminUpdateRequest.getId();
        //判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp==null,ErrorCode.NOT_FOUND_ERROR);
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest,app);
        //设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员分页获取应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest){
        ThrowUtils.throwIf(appQueryRequest==null,ErrorCode.PARAMS_ERROR);
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        //数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum,pageSize,appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 管理员根据id获取应用详情
     *
     * @param id 应用id
     * @return 应用详情
     */
    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> getAppVOByIdByAdmin(long id){
        ThrowUtils.throwIf(id<=0,ErrorCode.PARAMS_ERROR);
        //查询数据库
        App app = appService.getById(id);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(appService.getAppVO(app));
    }

//    /**
//     * 保存。
//     *
//     * @param app
//     * @return {@code true} 保存成功，{@code false} 保存失败
//     */
//    @PostMapping("save")
//    public boolean save(@RequestBody App app) {
//        return appService.save(app);
//    }
//
//    /**
//     * 根据主键删除。
//     *
//     * @param id 主键
//     * @return {@code true} 删除成功，{@code false} 删除失败
//     */
//    @DeleteMapping("remove/{id}")
//    public boolean remove(@PathVariable Long id) {
//        return appService.removeById(id);
//    }
//
//    /**
//     * 查询所有。
//     *
//     * @return 所有数据
//     */
//    @GetMapping("list")
//    public List<App> list() {
//        return appService.list();
//    }
//
//    /**
//     * 根据主键获取。
//     *
//     * @param id 主键
//     * @return 详情
//     */
//    @GetMapping("getInfo/{id}")
//    public App getInfo(@PathVariable Long id) {
//        return appService.getById(id);
//    }
//
//    /**
//     * 分页查询。
//     *
//     * @param page 分页对象
//     * @return 分页对象
//     */
//    @GetMapping("page")
//    public Page<App> page(Page<App> page) {
//        return appService.page(page);
//    }

}
