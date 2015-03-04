package com.niroshpg.android.colorquest;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;

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

        private Board board;
       // private AdView adView;

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
            final float density = ResourceManager.getDisplayMetrics(this).density;

            try {
                this.mTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
                    @Override
                    public InputStream open() throws IOException {
                        if(density > 1)
                        {
                            return getAssets().open("gfx/hdpi/ic_launcher.png");
                        }
                        else
                        {
                            return getAssets().open("gfx/ldpi/ic_launcher.png");
                        }

                    }
                });

                this.mTexture.load();
                this.mBannerTextureRegion = TextureRegionFactory.extractFromTexture(this.mTexture);

                this.mButtonTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
                    @Override
                    public InputStream open() throws IOException {
                        if(density > 1)
                        {
                            return getAssets().open("gfx/hdpi/ic_button.png");
                        }
                        else
                        {
                            return getAssets().open("gfx/ldpi/ic_button.png");
                        }

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
            float density = ResourceManager.getDisplayMetrics(this).density;
            final Sprite banner = new Sprite(tileWidth , 1.1f*tileWidth, this.mBannerTextureRegion, this.getVertexBufferObjectManager());


            //banner.setScale(DEFAULT_SCALE * density);
            mScene.registerTouchArea(banner);
            mScene.attachChild(banner);
    // 3*tileWidth - 1.9f*tileWidth
            // 1*tileWidth +2* 1.9f*tileWidth
            final Sprite playButton = new Sprite(tileWidth*6.0f , 1.1f*tileWidth, this.mButtonTextureRegion, this.getVertexBufferObjectManager()){
                @Override
                public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                    switch (pSceneTouchEvent.getMotionEvent().getAction()) {
                        case MotionEvent.ACTION_UP:
                            if(board.getMode() == Board.MODE.OVER)
                            {
                                board.setMode( Board.MODE.PLAY);
                            }
                            board.restartGame();
                            break;
                    }
                    return true;
                }
            };

           // playButton.setScale(DEFAULT_SCALE * density);

            mScene.attachChild(playButton);
            mScene.registerTouchArea(playButton);
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
/*
    @Override
    protected void onSetContentView() {
        // CREATING the parent FrameLayout //
        final FrameLayout frameLayout = new FrameLayout(this);

        // CREATING the layout parameters, fill the screen //
        final FrameLayout.LayoutParams frameLayoutLayoutParams =
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);

        // CREATING a Smart Banner View //
        PublisherAdView adView = new PublisherAdView(this);
        adView.setAdUnitId(this.getResources().getString(R.string.banner_ad_unit_id));
        adView.setAdSizes(com.google.android.gms.ads.AdSize.BANNER);

        // Doing something I'm not 100% sure on, but guessing by the name //
        adView.refreshDrawableState();
        adView.setVisibility(AdView.VISIBLE);

        // ADVIEW layout, show at the bottom of the screen //
        final FrameLayout.LayoutParams adViewLayoutParams =
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);

        // REQUEST an ad (Test ad) //

        PublisherAdRequest adRequest =
                new PublisherAdRequest.Builder().build();
        //adRequest.addTestDevice("MY_TEST_DEVICE_CODE");
        adView.loadAd(adRequest);

        // RENDER the add on top of the scene //
        this.mRenderSurfaceView = new RenderSurfaceView(this);
        mRenderSurfaceView.setRenderer(mEngine, this);

        // SURFACE layout ? //
        final android.widget.FrameLayout.LayoutParams surfaceViewLayoutParams =
                new FrameLayout.LayoutParams(super.createSurfaceViewLayoutParams().width,super.createSurfaceViewLayoutParams().height);

        // ADD the surface view and adView to the frame //
        frameLayout.addView(this.mRenderSurfaceView, surfaceViewLayoutParams);
        frameLayout.addView(adView, adViewLayoutParams);

        // SHOW AD //
        this.setContentView(frameLayout, frameLayoutLayoutParams);
    }
    */
}

