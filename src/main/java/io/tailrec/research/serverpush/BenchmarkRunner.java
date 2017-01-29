package io.tailrec.research.serverpush;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.asynchttpclient.*;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketTextListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Hussachai Puripunpinyo
 */
public class BenchmarkRunner implements Logging {

    private BenchmarkOptions options;

    private Config config;

    private ObjectMapper mapper;

    private AsyncHttpClient httpClient;

    public BenchmarkRunner (BenchmarkOptions options) {

        this.options = options;

        this.config = ConfigFactory.parseFile(new File("application.conf"));

        this.mapper = new ObjectMapper();
        this.mapper.setVisibility(mapper.getDeserializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
            .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));

        AsyncHttpClientConfig httpClientConfig = new DefaultAsyncHttpClientConfig.Builder().build();
        this.httpClient = new DefaultAsyncHttpClient(httpClientConfig);

        println("Starting the benchmark with options" + options);
        println("Target server: " + config.getString("server.host") + ":" + config.getString("server.port"));
        println("=============================");
    }

    public void run() throws Exception  {
        String resultDir = config.getString("client.resultDir");
        if(!resultDir.endsWith(File.separator)) {
            resultDir = resultDir + "/";
        }
        String host = config.getString("server.host");
        if(options.isLocalhost()) {
            host = "localhost";
        }
        int port = config.getInt("server.port");
        String architecture = options.getArchitecture();

        CountDownLatch responseLatch = new CountDownLatch(options.getNumberOfRequests() * options.getNumberOfConnections());

        ResultWriter writers[] = new ResultWriter[options.getNumberOfConnections()];
        Object blockers[] = new Object[options.getNumberOfConnections()];
        String channels[] = new String[options.getNumberOfConnections()];
        WebSocket websockets[] = new WebSocket[options.getNumberOfConnections()];

        String execMode = "s";
        if(options.isConcurrent()) {
            execMode = "c";
        }

        for(int i = 0; i < options.getNumberOfConnections(); i++){
            blockers[i] = new Object();
            channels[i] = "channel" + (i + 1);
            String fileName = architecture + "-" + channels[i] + "-" + options.getNumberOfConnections()
                + "-" + options.getNumberOfRequests() + execMode + ".csv";
            writers[i] = new ResultWriter(new File(resultDir + fileName));

            String targetUrl = String.format("ws://%s:%s/%s/ws/%s",
                host, String.valueOf(port), architecture, channels[i]);

            WebSocketTextListener wsListener = new WebSocketTextListenerImpl(i, writers[i],
                mapper, options, blockers[i], responseLatch);
            websockets[i] = httpClient.prepareGet(targetUrl).execute(new WebSocketUpgradeHandler.Builder()
                    .addWebSocketListener(wsListener).build()).get();
            Thread.sleep(500);
        }

        int waitTime = 5;
        println("Wait for the server to initialize its resources for " + waitTime + " seconds");
        Thread.sleep(waitTime*1000);
        println("Ready to fire...");

        ExecutorService threadPool = Executors.newFixedThreadPool(options.getNumberOfConnections() + 1);

        int totalRequests = options.getNumberOfConnections() * options.getNumberOfRequests();
        println("Total number of requests: " + totalRequests);

        final CountDownLatch requestLatch = new CountDownLatch(totalRequests);

        final String publishURL = "http://" + host + ":" + port + "/" + architecture + "/publish";

        for(int i = 0; i < options.getNumberOfConnections(); i++) {
            final String channel = channels[i];
            final Object blocker = blockers[i];
            threadPool.submit(() -> {

                for (int j = 0; j < options.getNumberOfRequests(); j++) {
                    Request request = new RequestBuilder("POST")
                        .setUrl(publishURL)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .addFormParam("label", String.valueOf(j + 1))
                        .addFormParam("channel", channel)
                        .addFormParam("username", "Alice")
                        .addFormParam("body", RandomStringUtils.randomAlphabetic(options.getMessageSize()))
                        .addFormParam("timestamp", String.valueOf(System.currentTimeMillis()))
                        .build();

                    AsyncCompletionHandler completionHandler = new AsyncCompletionHandlerImpl(requestLatch);
                    httpClient.prepareRequest(request).execute(completionHandler);
                    if(!options.isConcurrent()) {
                        // cannot send the next request until the web socket gets the response.
                        try {
                            synchronized (blocker) {
                                blocker.wait();
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        println("Waiting for all requests to complete...");
        requestLatch.await();
        println("Done!");

        println("Waiting for all responses to be received...");
        try {
            responseLatch.await(2, TimeUnit.MINUTES);
        } catch (Exception e) {
            println("Something went wrong - timeout exceeded!");
            println("Expected " + totalRequests + " but got " + responseLatch.getCount());
        }
        println("Done!");

        for (int i = 0; i < options.getNumberOfConnections(); i++) {
            System.out.println("Closing the WebSocket for the channel: " + channels[i]);
            websockets[i].close();
            println("Waiting for the writer thread finish its jobs...");
            writers[i].close();
            println("Done!");
        }

        println("The system is going to shutdown...");

        httpClient.close();
        threadPool.shutdown();

    }

    public static void main(String args[]) throws Exception {

        BenchmarkOptions options = new BenchmarkOptions();
        CmdLineParser parser = new CmdLineParser(options);

        try {

            parser.parseArgument(args);

            new BenchmarkRunner(options).run();

        } catch (CmdLineException e) {
            // handling of wrong arguments
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }

    }
}
