package org.github.toxrink.ws;

import java.io.File;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.github.toxrink.utils.EnvUtils;

import x.os.CmdWrapper;
import x.os.FileWatcher;

@ServerEndpoint("/ws/uilog")
@ApplicationScoped
public class ServerLogWs {
    private static final Log LOG = LogFactory.getLog(ServerLogWs.class);

    private static final ExecutorService exec = Executors.newFixedThreadPool(10);

    @ConfigProperty(name = "quarkus.log.file.path")
    private String serverLogPath;

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
        LOG.info(message + " fetching ui log");
        exec.execute(() -> {
            if (session.isOpen()) {
                File file = new File(serverLogPath);
                if (file.exists()) {
                    CmdWrapper.tailf(new FileWatcherImpl(session), file, 1024 * 2, 0);
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

    static class FileWatcherImpl implements FileWatcher {

        private Session session;

        public FileWatcherImpl(Session session) {
            this.session = session;
        }

        @Override
        public void push(String msg) {
            try {
                session.getAsyncRemote().sendText(new String(msg.getBytes("ISO-8859-1"), EnvUtils.UTF8));
            } catch (Exception e) {
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
