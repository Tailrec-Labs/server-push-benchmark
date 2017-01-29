package io.tailrec.research.serverpush;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Hussachai Puripunpinyo
 */
public class ResultWriter implements Logging {

    private ExecutorService writerThread;

    private BufferedWriter writer;

    private AtomicInteger counter;

    public ResultWriter(File file) throws Exception {
        if(file.isDirectory()) {
            throw new IOException("The output file cannot be a directory");
        } else {
            File parentFile = file.getParentFile();
            if(!parentFile.exists()) {
                parentFile.mkdirs();
            }
        }
        counter = new AtomicInteger();
        writerThread = Executors.newSingleThreadExecutor();

        println("Creating file: " + file.getName());
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
    }

    public void write(String text) {
        final BufferedWriter finalWriter = writer;
        writerThread.submit(() -> {
            // task will be executed sequentially; thus, it's safe to modify the value here.
            int n = counter.incrementAndGet();
            try {
                print(".");
                finalWriter.append(text);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                counter.decrementAndGet();
            }
        });
    }

    public void close() throws IOException {
        while(true) {
            if (counter.get() > 0) {
                try {
                    Thread.sleep(100);
                } catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
        writerThread.shutdown();
        writer.close();
    }

}
