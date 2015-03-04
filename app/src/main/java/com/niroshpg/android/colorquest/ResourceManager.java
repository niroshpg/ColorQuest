package com.niroshpg.android.colorquest;

import android.content.Context;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

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

    private static final int DEFAULT_FONT_SIZE = 20;
    private static Font largeFont;
    private static Font smallFont;


    public static DisplayMetrics getDisplayMetrics(Context context)
    {
        WindowManager windowManager=(WindowManager)(context).getSystemService(Context.WINDOW_SERVICE);

        Display display = windowManager.getDefaultDisplay();

        DisplayMetrics displayMetrics = new DisplayMetrics();

        display.getMetrics(displayMetrics);
        return displayMetrics;
    }

    public static void loadResources(Context context,FontManager fontManager, TextureManager textureManager)
    {
        DisplayMetrics displayMetrics = getDisplayMetrics(context);
        final float fontSize = DEFAULT_FONT_SIZE * displayMetrics.density ;

        font = FontFactory.create(fontManager,textureManager, 1024, 1024, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), fontSize * 1.2f);
        font.load();

        largeFont = FontFactory.create(fontManager,textureManager, 1024, 1024, TextureOptions.BILINEAR, Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL), fontSize * 2f);
        largeFont.load();

        smallFont = FontFactory.create(fontManager,textureManager, 1024, 1024, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), fontSize * 1.2f);
        smallFont.load();

        tileFont = FontFactory.create(fontManager,textureManager, 1024, 1024, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), fontSize * 1.5f);
        tileFont.load();


        tileContentFont = FontFactory.create(fontManager,textureManager, 1024, 1024, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), fontSize);
        tileContentFont.load();
    }

    public static Font getFont() {
        return font;
    }

    public static Font getTileFont() {
        return tileFont;
    }

    public static Font getTileContentFont() {
        return tileContentFont;
    }

    public static Font getLargeFont() {
        return largeFont;
    }
}
