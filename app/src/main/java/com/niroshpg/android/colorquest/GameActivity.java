package com.niroshpg.android.colorquest;


import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.Toast;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.color.Color;


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

        private BitmapTextureAtlas mBitmapTextureAtlas;
        private ITextureRegion mFaceTextureRegion;
        private Rectangle myRectangle;
        private Rectangle[][] otherRectangle;

        private Camera mCamera;
        private Scene mScene;



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
            Toast.makeText(this, "Touch & Drag the face!", Toast.LENGTH_LONG).show();
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
            ResourceManager.loadResources(this.getFontManager(),this.getTextureManager());

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

            mScene.setTouchAreaBindingOnActionDownEnabled(true);


            return mScene;
        }

        @Override
        public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {

            if(pSceneTouchEvent.isActionDown()) {

                return true;
            }

            return false;
        }


    }
