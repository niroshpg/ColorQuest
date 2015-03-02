package com.niroshpg.android.colorquest;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.view.Display;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import java.io.IOException;
import java.io.InputStream;


public class GameActivity extends SimpleBaseGameActivity implements IOnSceneTouchListener{
        // ===========================================================
        // Constants
        // ===========================================================

        private static final int CAMERA_WIDTH_MAX = 1024;
        private static final int CAMERA_HEIGHT_MAX = 600;
        private static final int GRID_SZ = 4;

        private static int cameraWidth = CAMERA_WIDTH_MAX;
        private static int cameraHeight = CAMERA_HEIGHT_MAX;


        // ===========================================================
        // Fields
        // ===========================================================



        private ITexture mTexture;
        private ITextureRegion mBannerTextureRegion;

        private ITexture mButtonTexture;
        private ITextureRegion mButtonTextureRegion;

        private Camera mCamera;
        private Scene mScene;

        private static final float DEFAULT_SCALE = 0.6f;

        Board board;

        // ===========================================================
        // Constructors
        // ===========================================================

        // ===========================================================
        // Getter & Setter
        // ===========================================================

        // ===========================================================
        // Methods for/from SuperClass/Interfaces
        // ===========================================================

        @Override
        public EngineOptions onCreateEngineOptions() {
            final Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);
            cameraWidth = outMetrics.widthPixels;
            cameraHeight =outMetrics.heightPixels;
            if (cameraWidth > CAMERA_WIDTH_MAX) {
                final float ratio = (float) cameraHeight / (float) cameraWidth;
                cameraWidth = CAMERA_WIDTH_MAX;
                cameraHeight = (int) (cameraWidth * ratio);
            }
            mCamera = new Camera(0, 0, cameraWidth, cameraHeight);
            EngineOptions options =new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(cameraWidth, cameraHeight), mCamera);
            int interval = 1;
            options.getTouchOptions().setTouchEventIntervalMilliseconds(interval);

            return options;
        }

        @Override
        public void onCreateResources() {
            BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

            //this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 64, 64, TextureOptions.BILINEAR);
            //this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "player.png", 0, 0);
            //this.mBitmapTextureAtlas.load();

            //otherRectangle = new Rectangle[GRID_SZ][GRID_SZ];
            ResourceManager.loadResources(this,this.getFontManager(),this.getTextureManager());

            try {
                this.mTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
                    @Override
                    public InputStream open() throws IOException {
                        return getAssets().open("gfx/ic_launcher.png");
                    }
                });

                this.mTexture.load();
                this.mBannerTextureRegion = TextureRegionFactory.extractFromTexture(this.mTexture);

                this.mButtonTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
                    @Override
                    public InputStream open() throws IOException {
                        return getAssets().open("gfx/ic_button.png");
                    }
                });

                this.mButtonTexture.load();
                this.mButtonTextureRegion = TextureRegionFactory.extractFromTexture(this.mButtonTexture);

            } catch (IOException e) {
                Debug.e(e);
            }

        }

        @Override
        public Scene onCreateScene() {
            this.mEngine.registerUpdateHandler(new FPSLogger());

            mScene = new Scene();
            // mScene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
            mScene.setBackground(new Background(Color.WHITE));
            mScene.setOnSceneTouchListener(this);



            float scale = cameraHeight/16f; //12f
            float tileWidth = cameraHeight/16f; //12f
            VertexBufferObjectManager vbufMgr = this.getVertexBufferObjectManager();
            board = new Board(mScene,mEngine,tileWidth,vbufMgr,scale,cameraHeight);
            loadPreferences();
            mScene.setTouchAreaBindingOnActionDownEnabled(true);

            final Sprite banner = new Sprite(cameraHeight/20f , 0.045f*cameraHeight, this.mBannerTextureRegion, this.getVertexBufferObjectManager());
            banner.setScale(DEFAULT_SCALE * ResourceManager.getDisplayMetrics(this).density);
            mScene.attachChild(banner);

            final Sprite playButton = new Sprite(cameraHeight*0.38f , (cameraHeight/90f)*4, this.mButtonTextureRegion, this.getVertexBufferObjectManager()){
                @Override
                public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                    if(board.getMode() == Board.MODE.OVER)
                    {
                        board.setMode( Board.MODE.PLAY);
                    }
                    board.restartGame();
                    return true;
                }
            };
            playButton.setScale(DEFAULT_SCALE * ResourceManager.getDisplayMetrics(this).density);
            mScene.attachChild(playButton);


            return mScene;
        }

        @Override
        public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {

            if(pSceneTouchEvent.isActionDown()) {

                return true;
            }

            return false;
        }

    @Override
    protected void onPause() {
        super.onPause();
        //setting preferences
        if(board != null) {
            SharedPreferences prefs = this.getSharedPreferences("colorQuestBestScoreKey", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("bestScore", board.getBestScore());
            editor.commit();
        }
    }

    private void loadPreferences()
    {
        SharedPreferences prefs = this.getSharedPreferences("colorQuestBestScoreKey", Context.MODE_PRIVATE);
        long bestScore = prefs.getLong("bestScore", 0); //0 is the default value
        if(board != null)
        {
            board.setBestScore(bestScore);
        }
    }

    @Override
    protected synchronized void onResume() {
        super.onResume();
        loadPreferences();
    }
}

