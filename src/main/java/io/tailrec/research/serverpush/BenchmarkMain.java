package io.tailrec.research.serverpush;

/**
 * @author Hussachai Puripunpinyo
 */
public class BenchmarkMain {

    public static void main(String args[]) throws Exception {

        args = new String[]{"-a", "jms-topic", "-i", "3", "-n", "6", "-c"};

        BenchmarkRunner.main(args);
    }
}
