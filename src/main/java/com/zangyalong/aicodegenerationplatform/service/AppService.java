package com.zangyalong.aicodegenerationplatform.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zangyalong.aicodegenerationplatform.model.dto.app.AppAddRequest;
import com.zangyalong.aicodegenerationplatform.model.dto.app.AppQueryRequest;
import com.zangyalong.aicodegenerationplatform.model.entity.App;
import com.zangyalong.aicodegenerationplatform.model.entity.User;
import com.zangyalong.aicodegenerationplatform.model.vo.app.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author mingzang
 */
public interface AppService extends IService<App> {

    public AppVO getAppVO(App app);

    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    public List<AppVO> getAppVOList(List<App> appList);

    public Flux<String> chatToGenCode(Long appId, String message, User loginUser, boolean agent);

    public String deployApp(Long appId, User loginUser);

    public void generateAppScreenshotAsync(Long appId, String appUrl);

    public Long createApp(AppAddRequest appAddRequest, User loginUser);
}
