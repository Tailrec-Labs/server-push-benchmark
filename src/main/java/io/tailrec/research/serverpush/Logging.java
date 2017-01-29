package io.tailrec.research.serverpush;

/**
 * @author Hussachai Puripunpinyo
 */
public interface Logging {

    default void println(String msg) {
        System.out.println(msg);
    }

    default void print(String msg) {
        System.out.print(msg);
    }

    default void printf(String msg, Object... args) {
        System.out.printf(msg, args);
    }

}
