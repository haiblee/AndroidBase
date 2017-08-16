package com.haiblee.base.log;

/**
 * Created by Dell on 2017/8/1 0001.
 */

public interface ILog {
    public static final int LEVEL_V = 0;
    public static final int LEVEL_D = 1;
    public static final int LEVEL_I = 2;
    public static final int LEVEL_W = 3;
    public static final int LEVEL_E = 4;

    void print(int level, String tag, String msg);
}
