package com.craft.ai.codecraft.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yu.ai.codecraft.model.dto.app.AppAddRequest;
import com.yu.ai.codecraft.model.dto.app.AppQueryRequest;
import com.yu.ai.codecraft.model.entity.App;
import com.yu.ai.codecraft.model.entity.User;
import com.yu.ai.codecraft.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 *  服务层。
 *
 * @author 鱼皮
 */
public interface AppService extends IService<App> {

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    Long createApp(AppAddRequest appAddRequest, User loginUser);

    AppVO getAppVO(App app);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    List<AppVO> getAppVOList(List<App> appList);

    String deployApp(Long appId, User LoginUser);

    void generateAppScreenShotAsync(Long appId, String appUrl);
}
