package org.github.toxrink.ws;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.github.toxrink.metric.JMXMetricUtils;

@ServerEndpoint("/ws/jmx/metric")
@ApplicationScoped
public class JMXMetricWs {
    private static final Log LOG = LogFactory.getLog(JMXMetricWs.class);

    private static final ExecutorService exec = Executors.newFixedThreadPool(10);

    /**
     * 连接建立执行
     * 
     * @param session
     *                    Session
     */
    @OnOpen
    public void onOpen(Session session) {
        session.getAsyncRemote().sendText("200");
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        try {
            session.close();
        } catch (IOException e) {
            LOG.error("", e);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message
     *                    客户端发送过来的消息
     * @param session
     *                    Session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        exec.execute(() -> {
            if (session.isOpen()) {
                String info = JMXMetricUtils.getMetricJSON(message);
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(message + "#" + info);
                }
            }
        });
    }

    /**
     * 错误处理
     * 
     * @param session
     *                    Session
     * @param error
     *                    错误
     */
    @OnError
    public void onError(Session session, Throwable error) {
        LOG.error("", error);
        try {
            session.close();
        } catch (IOException e) {
            LOG.error("", e);
        }
    }

}
