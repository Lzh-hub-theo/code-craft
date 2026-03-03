package com.yu.ai.yuaicodemother.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yu.ai.yuaicodemother.model.entity.App;
import com.yu.ai.yuaicodemother.mapper.AppMapper;
import com.yu.ai.yuaicodemother.service.AppService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author 鱼皮
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService {

}
