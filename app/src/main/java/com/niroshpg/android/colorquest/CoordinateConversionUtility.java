package com.niroshpg.android.colorquest;

/**
 * Created by Windows User on 14/02/2015.
 */
public class CoordinateConversionUtility {

    public static float[] tilePointToScreenPoint(int[] tilePoint, float tileWidth)
    {
        float [] screenPoint = new float[2];

        screenPoint[0] = 1*tileWidth + 1.9f*tilePoint[0]*tileWidth ;

        screenPoint[1] = 3*tileWidth + 1.9f*tilePoint[1]*tileWidth ;
        return screenPoint;
    }
}
