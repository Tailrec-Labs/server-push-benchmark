package io.tailrec.research.serverpush;

import org.kohsuke.args4j.Option;

/**
 * @author Hussachai Puripunpinyo
 */
public class BenchmarkOptions {

    @Option(name = "-p", aliases = "--protocol", usage = "target protocol")
    private String protocol = "ws";

    @Option(name = "-a", aliases = "--architecture", usage = "target architecture")
    private String architecture = "eventbus";

    @Option(name = "-i", aliases = "--number-connections", usage = "number of connections")
    private int numberOfConnections = 1;

    @Option(name = "-n", aliases = "--number-requests",
        usage = "number of requests. if threads > 1, total requests will be threads * requests.")
    private int numberOfRequests = 1;

    @Option(name = "-c", aliases = "--concurrent",
            usage = "set this value to make the requests executed in a concurrent mode.")
    private boolean concurrent = false;

    @Option(name = "-s", aliases = "--message-size", usage = "size of message being generated")
    private int messageSize = 32;

    @Option(name = "-l", aliases = "--localhost", usage = "set this value to override a remote host setting")
    private boolean localhost = false;

    @Override
    public String toString() {
        return "BenchmarkOptions{" +
                "protocol='" + protocol + '\'' +
                ", architecture='" + architecture + '\'' +
                ", numberOfConnections=" + numberOfConnections +
                ", numberOfRequests=" + numberOfRequests +
                ", concurrent=" + concurrent +
                ", messageSize=" + messageSize +
                ", localhost=" + localhost +
                '}';
    }

    public String getProtocol() {
        return protocol;
    }

    public String getArchitecture() {
        return architecture;
    }

    public int getNumberOfConnections() {
        return numberOfConnections;
    }

    public int getNumberOfRequests() {
        return numberOfRequests;
    }

    public boolean isConcurrent() {
        return concurrent;
    }

    public int getMessageSize() {
        return messageSize;
    }

    public boolean isLocalhost() {
        return localhost;
    }
}
