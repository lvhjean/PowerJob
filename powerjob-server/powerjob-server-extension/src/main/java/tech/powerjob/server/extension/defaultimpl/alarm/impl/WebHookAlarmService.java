package tech.powerjob.server.extension.defaultimpl.alarm.impl;

import com.alibaba.fastjson.JSONObject;
import tech.powerjob.common.OmsConstant;
import tech.powerjob.common.utils.HttpUtils;
import tech.powerjob.server.persistence.remote.model.UserInfoDO;
import tech.powerjob.server.extension.defaultimpl.alarm.module.Alarm;
import tech.powerjob.server.extension.Alarmable;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * http 回调报警
 *
 * @author tjq
 * @since 11/14/20
 */
@Slf4j
@Service
public class WebHookAlarmService implements Alarmable {

    private static final String HTTP_PROTOCOL_PREFIX = "http://";
    private static final String HTTPS_PROTOCOL_PREFIX = "https://";

    @Override
    public void onFailed(Alarm alarm, List<UserInfoDO> targetUserList) {
        if (CollectionUtils.isEmpty(targetUserList)) {
            return;
        }
        targetUserList.forEach(user -> {
            String webHook = user.getWebHook();
            if (StringUtils.isEmpty(webHook)) {
                return;
            }

            // 自动添加协议头
            if (!webHook.startsWith(HTTP_PROTOCOL_PREFIX) && !webHook.startsWith(HTTPS_PROTOCOL_PREFIX)) {
                webHook = HTTP_PROTOCOL_PREFIX + webHook;
            }
            
            // 基于DingDing机器人和微信机器人消息发送格式定义

            JSONObject content = new JSONObject();
            content.put("content", JSONObject.toJSONString(alarm));
            
            JSONObject body = new JSONObject();
            body.put("msgtype", "text");
            body.put("text", content);

            MediaType jsonType = MediaType.parse(OmsConstant.JSON_MEDIA_TYPE);
            RequestBody requestBody = RequestBody.create(jsonType, body.toJSONString());

            try {
                String response = HttpUtils.post(webHook, requestBody);
                log.info("[WebHookAlarmService] invoke webhook[url={}] successfully, response is {}", webHook, response);
            }catch (Exception e) {
                log.warn("[WebHookAlarmService] invoke webhook[url={}] failed!", webHook, e);
            }
        });
    }
}
