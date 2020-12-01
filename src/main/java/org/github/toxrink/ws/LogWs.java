package org.github.toxrink.ws;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
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
import org.github.toxrink.model.CollectInfo;
import org.github.toxrink.utils.CollectUtils;
import org.github.toxrink.utils.EnvUtils;

import x.os.CmdWrapper;
import x.os.FileWatcher;

@ServerEndpoint("/ws/log")
@ApplicationScoped
public class LogWs {
    private static final Log LOG = LogFactory.getLog(LogWs.class);

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
        Optional<CollectInfo> ci = CollectUtils.getCollectInfoById(message);
        if (ci.isPresent()) {
            exec.execute(() -> {
                File file = CollectUtils.getLogFileById(message);
                if (null != file && session.isOpen()) {
                    CmdWrapper.tailf(new FileWatcherImpl(session), file, 1024 * 2, 0);
                } else {
                    session.getAsyncRemote().sendText("Log file does not exist !!!");
                    LOG.warn(message + " log file does not exist");
                    try {
                        session.close();
                    } catch (IOException e) {
                        LOG.error("", e);
                    }
                }
            });
        }
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

    static class FileWatcherImpl implements FileWatcher {

        private Session session;

        public FileWatcherImpl(Session session) {
            this.session = session;
        }

        @Override
        public void push(String msg) {
            try {
                session.getAsyncRemote().sendText(new String(msg.getBytes("ISO-8859-1"), EnvUtils.UTF8));
            } catch (IOException e) {
                LOG.error("", e);
                try {
                    session.close();
                } catch (IOException e1) {
                    LOG.error("", e1);
                }
            }
        }

        @Override
        public boolean isStop() {
            return !session.isOpen();
        }

    }

}
