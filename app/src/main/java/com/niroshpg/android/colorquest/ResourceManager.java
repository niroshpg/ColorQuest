package com.niroshpg.android.colorquest;

import android.graphics.Typeface;

import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.FontManager;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;

/**
 * Created by Windows User on 22/02/2015.
 */
public class ResourceManager {

    private static Font font;
    private static Font tileFont;
    private static Font tileContentFont;

    public static void loadResources(FontManager fontManager, TextureManager textureManager)
    {
        font = FontFactory.create(fontManager,textureManager, 512, 512, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 72);
        font.load();

        tileFont = FontFactory.create(fontManager,textureManager, 512, 512, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 96);
        tileFont.load();


        tileContentFont = FontFactory.create(fontManager,textureManager, 512, 512, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 60);
        tileContentFont.load();
    }

    public static Font getFont() {
        return font;
    }

    public static void setFont(Font mFont) {
        ResourceManager.font = mFont;
    }

    public static Font getTileFont() {
        return tileFont;
    }

    public static void setTileFont(Font mTileFont) {
        ResourceManager.tileFont = mTileFont;
    }

    public static Font getTileContentFont() {
        return tileContentFont;
    }

    public static void setTileContentFont(Font mTileContentFont) {
        ResourceManager.tileContentFont = mTileContentFont;
    }
}
