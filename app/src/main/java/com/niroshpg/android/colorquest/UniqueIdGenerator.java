package com.niroshpg.android.colorquest;

/**
 * Created by Windows User on 21/02/2015.
 */
public class UniqueIdGenerator {
    private static long uid = 0;

    public UniqueIdGenerator()
    {

    }

    public static long getNextId()
    {
        return uid++;
    }
}
