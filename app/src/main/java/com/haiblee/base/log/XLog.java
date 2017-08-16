package com.haiblee.base.log;

/**
 * Created by Dell on 2017/8/1 0001.
 */

public final class XLog {
    public static final int MARK_DEBUG = 1 << 0;
    public static long flag = 0;

    private XLog(){}
    private static ILog sLog;

    public static void init(ILog log) {
        sLog = log;
    }
    public static boolean isDebug(){
        return ((XLog.flag & XLog.MARK_DEBUG) != 0);
    }
    public static void v(String tag, String format, Object... args) {
        v(tag, String.format(format, args));
    }
    public static void v(String tag, String msg) {
        o(ILog.LEVEL_V, tag, msg);
    }
    public static void v(String tag, Throwable e) {
        o(ILog.LEVEL_V, tag, getThrowableMsg(e));
    }


    public static void d(String tag, String format, Object... args) {
        d(tag, String.format(format, args));
    }
    public static void d(String tag, String msg) {
        o(ILog.LEVEL_D, tag, msg);
    }
    public static void d(String tag, Throwable e) {
        o(ILog.LEVEL_D, tag, getThrowableMsg(e));
    }

    public static void i(String tag, String format, Object... args) {
        i(tag, String.format(format, args));
    }
    public static void i(String tag, String msg) {
        o(ILog.LEVEL_I, tag, msg);
    }
    public static void i(String tag, Throwable e) {
        o(ILog.LEVEL_I, tag, getThrowableMsg(e));
    }

    public static void w(String tag, String format, Object... args) {
        w(tag, String.format(format, args));
    }
    public static void w(String tag, String msg) {
        o(ILog.LEVEL_W, tag, msg);
    }
    public static void w(String tag, Throwable e) {
        o(ILog.LEVEL_W, tag, getThrowableMsg(e));
    }

    public static void e(String tag, String format, Object... args) {
        e(tag, String.format(format, args));
    }
    public static void e(String tag, String msg) {
        o(ILog.LEVEL_E, tag, msg);
    }
    public static void e(String tag, Throwable e) {
        o(ILog.LEVEL_E, tag, getThrowableMsg(e));
    }
    public static void o(int level, String tag, String format, Object... args) {
        o(level, tag, String.format(format, args));
    }

    private static String getThrowableMsg(Throwable e) {
        return String.format("%s:%s", e.getClass().getSimpleName(), e.getMessage());
    }

    public static void o(int level, String tag, String msg) {
        sLog.print(level, tag, msg);
    }

}
