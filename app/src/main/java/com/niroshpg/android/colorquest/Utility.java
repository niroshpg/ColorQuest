package com.niroshpg.android.colorquest;

import org.andengine.util.color.Color;

/**
 * Created by Windows User on 22/02/2015.
 */
public class Utility {

    private final static float epsilen = 0.0001f;

    public static Color getColorForType(TileColorType  colorType)
    {
        Color color = null;
        switch (colorType)
        {
            case RED:
                color = Color.RED;
                break;
            case ORANGE:
                color =  new Color(1f,0.65f,0f,1f);
                break;
            case YELLOW:
                color = Color.YELLOW;
                break;
            case GREEN:
                color = Color.GREEN;
                break;
            case BLUE:
                color = Color.BLUE;
                break;
            case INDIGO:
                color = new Color(0f,0f,1f,1f);
                break;
            case VIOLATE:
                color = new Color(0f,0f,1f,1f);
                break;
            default:
                break;
        }

        return color;
    }

    public static TileColorType getColorTypeForColor(Color color)
    {
        TileColorType colorType = null;

        if( ( Math.abs(color.getRed() - 1f) < epsilen ) &&  ( Math.abs(color.getGreen() - 0f)   < epsilen) && (Math.abs(color.getBlue() - 0f) < epsilen))
        {
            colorType = TileColorType.RED;
        }
        if( ( Math.abs(color.getRed() - 1f) < epsilen ) &&  ( Math.abs(color.getGreen() - 0.65f)   < epsilen) && (Math.abs(color.getBlue() - 0f) < epsilen))
        {
            colorType = TileColorType.ORANGE;
        }
        if( ( Math.abs(color.getRed() - 1f) < epsilen ) &&  ( Math.abs(color.getGreen() - 1f)   < epsilen) && (Math.abs(color.getBlue() - 0f) < epsilen))
        {
            colorType = TileColorType.YELLOW;
        }
        if(color == Color.GREEN)
        {
            colorType = TileColorType.GREEN;
        }
        if(color == Color.BLUE)
        {
            colorType = TileColorType.BLUE;
        }
        if((color.getRed() == 0x00) && (color.getBlue() == 0x00) && (color.getGreen() == 0xFF))
        {
            colorType = TileColorType.INDIGO;
        }
        if((color.getRed() == 0x00) && (color.getBlue() == 0x00) && (color.getGreen() == 0xFF))
        {
            colorType = TileColorType.VIOLATE;
        }
        return colorType;
    }
}
