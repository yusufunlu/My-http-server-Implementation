Project from business Aspect
This project aims to provide http-server which is built by just using JDK
Main considerideration is creating performant and concurrent game score http services
There are 3 business service requirements as following
1-Generate session
2-Saving the games score based on generated sessions
3-Listing the high scores based game levels

Project from technical aspect
1- SimpleHttpServer class is to provide http server.
    I have created custom ThreadPoolExecutor and ThreadFactory in order to have more control on resource management
    Named threads
    Used some dependancy injection and missed some since it can take time
    Used magic numbers in class which wrong like 50000 queue capacity
    I was planning to use PriorityBlockingQueue to prioritize some threads than others but I didn't
2- RootHandler: since all required http enpoints start with "/<number>/<path>" pattern 2 options can be implemented
    First is regex which is not supported by HttpContext.createContext
    Second is using single root handler and distributing the requests
    I could use some handlers while distributing the requests in RootHandler also but using methods like createSession solved my issue
    I have planned to use carry the state with sessionkey like jwt. I tried to create sessionkey which has meaning for backend and no meaning for clients.
    That is why I have tried some hashing, encoding util methods : createHashForUser(),encodeBase64()
    Correct way was encrpt with a constant salt time based sessionkey and metas like expire time,userinfo. So I could use access userid after decrypt it

3- UserScore: It is required to use TreeSet which support sorting. I have tried ConcurrentSkipListSet also but solved concurrency issue by synchronized
4- Util: mostly static helper methods and constants which can be fed *.yml or *.json file in real prod app
5- HttpServerTest: I didn't use Junit or Mockito which are I am familiar with.
    I have chosed thread based performance end-to-end test which cover more but miss atomic tests(bottom of the pyramid)
    Main concern was concurrency for the test.
    So saving the responses of services and verify them wouldn't be able to solve because which thread works earlier or later is unknown
    I could verify the last hightScoreList after all threads are finished. But the hightScoreList strings should be concurrent. Tests for concurrency problems are hard.
    I followed the logs of tests and services. For level10 high scores count should be 10





