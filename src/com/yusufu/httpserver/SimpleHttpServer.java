package com.yusufu.httpserver;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("restriction")
public class SimpleHttpServer {

    private HttpServer httpServer;

    /**
     * Instantiates a new simple http server.
     *
     * @param port the port
     * @param context the context
     * @param handler the handler
     */
    public SimpleHttpServer(int port, String context, HttpHandler handler) {
        try {
            //Create HttpServer which is listening on the given port
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            //Create a new context for the given context and handler
            HttpContext httpContext = httpServer.createContext(context, handler);
            //Create a default executor
            //httpServer.setExecutor(null);

            httpContext.setAuthenticator(new Authenticator() {
                @Override
                public Result authenticate(HttpExchange exch) {
                    return null;
                }
            });



            //httpServer.setExecutor(new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>()));
            httpServer.setExecutor(createThreadPoolExecutor());
            //httpServer.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            httpServer.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void start() {
        this.httpServer.start();
    }


    public ThreadPoolExecutor createThreadPoolExecutor() {

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors(),
                1000 * 60,
                TimeUnit.MILLISECONDS,
                createBlockingQueue(),
                getThreadFactory());

        //threadPoolExecutor.setRejectedExecutionHandler(new ShutdownRejectedExecutionHandler(threadPoolExecutor.getRejectedExecutionHandler()));

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

    public BlockingQueue createBlockingQueue() {
        BlockingQueue arrayBlockingQueue = new ArrayBlockingQueue(1024);

        //No upper bound
        BlockingQueue asyncSenderThreadPoolQueue = new LinkedBlockingQueue<Runnable>(50000);
        //sort upon Comparable
        BlockingQueue  priorityBlockingQueue = new PriorityBlockingQueue();

        return asyncSenderThreadPoolQueue;
    }


}