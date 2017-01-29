package io.tailrec.research.serverpush;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketTextListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

/**
 * @author Hussachai Puripunpinyo
 */
class WebSocketTextListenerImpl implements WebSocketTextListener, Logging{

    private int id;

    private ResultWriter writer;

    private ObjectMapper mapper;

    private BenchmarkOptions options;

    private Object blocker;

    private CountDownLatch latch;

    public WebSocketTextListenerImpl(int id, ResultWriter writer,
                                     ObjectMapper mapper, BenchmarkOptions options,
                                     Object blocker, CountDownLatch latch) {

        this.id = id;
        this.writer = writer;
        this.mapper = mapper;
        this.options = options;
        this.blocker = blocker;
        this.latch = latch;
    }

    @Override
    public void onMessage(String json) {
        try {
            ChatMessage message = mapper.readValue(json, ChatMessage.class);
            writer.write(message.toResult());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(!options.isConcurrent()){
                synchronized (blocker) {
                    blocker.notify();
                }
            }
            latch.countDown();
        }
    }

    @Override
    public void onOpen(WebSocket websocket) {
        println("Connection id: " + id + " is now open!");
    }

    @Override
    public void onClose(WebSocket websocket) {
        println("Connection id: " + id + " is closing...");
    }

    @Override
    public void onError(Throwable t) {
        println("Connection id: " + id + " got an exception: " + t.getMessage());
    }
}