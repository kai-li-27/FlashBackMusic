package android.util;

/**
 * Created by kwmag on 3/14/2018.
 * As recommended by https://stackoverflow.com/questions/36787449/how-to-mock-method-e-in-log
 * "How to Mock Method e in Log" obtained 3/14/2018
 */

public class Log {
    public static int d(String tag, String msg) {
        System.out.println("Log.d: " + tag + ": " + msg);
        return 0;
    }

    public static int i(String tag, String msg) {
        System.out.println("Log.i: " + tag + ": " + msg);
        return 0;
    }

    public static int w(String tag, String msg) {
        System.out.println("Log.w: " + tag + ": " + msg);
        return 0;
    }

    public static int e(String tag, String msg) {
        System.out.println("ERROR: " + tag + ": " + msg);
        return 0;
    }
}
