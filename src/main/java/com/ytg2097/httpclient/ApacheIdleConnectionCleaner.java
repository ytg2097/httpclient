package com.ytg2097.httpclient;

import org.apache.http.conn.HttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-09-03
 **/
public class ApacheIdleConnectionCleaner extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(ApacheIdleConnectionCleaner.class);
    private static volatile ApacheIdleConnectionCleaner instance;
    private static final Map<HttpClientConnectionManager, Long> connMgrMap = new ConcurrentHashMap();
    private volatile boolean isShuttingDown;

    private ApacheIdleConnectionCleaner() {
        super("usr-sdk-apache-idle-connection-cleaner");
        this.setDaemon(true);
    }

    public static void registerConnectionManager(HttpClientConnectionManager connMgr, Long idleTimeMills) {

        if (instance == null) {
            synchronized(ApacheIdleConnectionCleaner.class) {
                if (instance == null) {
                    instance = new ApacheIdleConnectionCleaner();
                    instance.start();
                }
            }
        }

        connMgrMap.put(connMgr, idleTimeMills);
    }

    public static void removeConnectionManager(HttpClientConnectionManager connectionManager) {
        connMgrMap.remove(connectionManager);
        if (connMgrMap.isEmpty()) {
            shutdown();
        }

    }

    public static void shutdown() {
        if (instance != null) {
            instance.isShuttingDown = true;
            instance.interrupt();
            connMgrMap.clear();
            instance = null;
        }

    }

    @Override
    public void run() {
        while(!this.isShuttingDown) {
            try {
                Thread.sleep(60000L);

                connMgrMap.forEach((key,value) -> {
                    try {
                        key.closeIdleConnections(value, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        LOG.warn("close idle connections failed", e);
                    }
                });
            } catch (InterruptedException e) {
                LOG.debug("interrupted.", e);
            } catch (Throwable e) {
                LOG.warn("fatal error", e);
            }
        }

        LOG.debug("Shutting down.");
    }
}
