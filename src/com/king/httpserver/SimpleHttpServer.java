package com.king.httpserver;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("restriction")
public class SimpleHttpServer {

    //static class only one instance for same port

    private HttpServer httpServer;

    public SimpleHttpServer(int port, String context, HttpHandler handler) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            httpServer.createContext(context, handler);
            httpServer.setExecutor(createThreadPoolExecutor());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        this.httpServer.start();
    }


    private ThreadPoolExecutor createThreadPoolExecutor() {

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors(),
                1000 * 60,
                TimeUnit.MILLISECONDS,
                createBlockingQueue(),
                getThreadFactory());

        //threadPoolExecutor.setRejectedExecutionHandler(new ShutdownRejectedExecutionHandler(threadPoolExecutor.getRejectedExecutionHandler()));
        //Executors.newCachedThreadPool();
        //Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors();
        return threadPoolExecutor;
    }

    private ThreadFactory getThreadFactory() {
        ThreadFactory factory=new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "KingGamesThread_" + this.threadIndex.incrementAndGet());
            }
        };
        return factory;
    }

    private BlockingQueue createBlockingQueue() {
        BlockingQueue arrayBlockingQueue = new ArrayBlockingQueue(1024);

        //No default upper bound,FIFO
        BlockingQueue linkedBlockingQueue = new LinkedBlockingQueue<Runnable>(50000);
        //sort upon Comparable
        BlockingQueue  priorityBlockingQueue = new PriorityBlockingQueue();

        return linkedBlockingQueue;
    }


}