package io.tailrec.research.serverpush;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.Response;

import java.util.concurrent.CountDownLatch;

/**
 * @author Hussachai Puripunpinyo
 */
public class AsyncCompletionHandlerImpl extends AsyncCompletionHandler {

    private CountDownLatch latch;

    public AsyncCompletionHandlerImpl(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public Response onCompleted(Response response) throws Exception {
        latch.countDown();
        return response;
    }

}
