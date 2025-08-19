package com.zangyalong.aicodegenerationplatform.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zangyalong.aicodegenerationplatform.model.dto.chathistory.ChatHistoryQueryRequest;
import com.zangyalong.aicodegenerationplatform.model.entity.ChatHistory;
import com.zangyalong.aicodegenerationplatform.model.entity.User;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author mingzang
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    public boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    public boolean deleteByAppId(Long appId);

    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser);
}
