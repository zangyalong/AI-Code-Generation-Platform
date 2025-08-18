package com.zangyalong.aicodegenerationplatform.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zangyalong.aicodegenerationplatform.model.dto.app.AppQueryRequest;
import com.zangyalong.aicodegenerationplatform.model.entity.App;
import com.zangyalong.aicodegenerationplatform.model.vo.app.AppVO;

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
}
