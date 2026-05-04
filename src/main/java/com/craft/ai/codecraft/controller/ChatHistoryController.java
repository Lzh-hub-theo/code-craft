package com.craft.ai.codecraft.controller;

import com.craft.ai.codecraft.annotation.AuthCheck;
import com.craft.ai.codecraft.common.BaseResponse;
import com.craft.ai.codecraft.common.ResultUtils;
import com.craft.ai.codecraft.constant.UserConstant;
import com.craft.ai.codecraft.exception.ErrorCode;
import com.craft.ai.codecraft.exception.ThrowUtils;
import com.craft.ai.codecraft.model.dto.chathistory.ChatHistoryQueryRequest;
import com.craft.ai.codecraft.model.entity.ChatHistory;
import com.craft.ai.codecraft.model.entity.User;
import com.craft.ai.codecraft.service.ChatHistoryService;
import com.craft.ai.codecraft.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 *  控制层。
 *
 * @author 鱼皮
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 保存。
     *
     * @param chatHistory 
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody ChatHistory chatHistory) {
        return chatHistoryService.save(chatHistory);
    }

    /**
     * 分页查询每个应用的对话历史（游标查询）
     *
     * @param appId 应用ID
     * @param pageSize 每页大小
     * @param lastCreateTime 上次创建时间
     * @param request 请求
     * @return 分页结果
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false)LocalDateTime lastCreateTime,
                                                              HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest){
        ThrowUtils.throwIf(chatHistoryQueryRequest==null, ErrorCode.PARAMS_ERROR);
        int pageNum = chatHistoryQueryRequest.getPageNum();
        int pageSize = chatHistoryQueryRequest.getPageSize();
        //查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }

    /**
     * 根据主键删除。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return chatHistoryService.removeById(id);
    }

    /**
     * 根据主键更新。
     *
     * @param chatHistory 
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody ChatHistory chatHistory) {
        return chatHistoryService.updateById(chatHistory);
    }

    /**
     * 查询所有。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<ChatHistory> list() {
        return chatHistoryService.list();
    }

    /**
     * 根据主键获取。
     *
     * @param id 主键
     * @return 详情
     */
    @GetMapping("getInfo/{id}")
    public ChatHistory getInfo(@PathVariable Long id) {
        return chatHistoryService.getById(id);
    }

    /**
     * 分页查询。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<ChatHistory> page(Page<ChatHistory> page) {
        return chatHistoryService.page(page);
    }

}
