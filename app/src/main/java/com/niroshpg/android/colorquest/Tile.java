package com.niroshpg.android.colorquest;

import android.view.MotionEvent;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Windows User on 13/02/2015.
 */
public class Tile extends Rectangle implements  StatusEventListener{
    private static final String LOG_TAG = Tile.class.getSimpleName();
    private static final float DELTA = 0.001f;
    private float cellWidth = 0f;

    private Text tileContentText = null;

    Board.MODE currentMode = Board.MODE.PLAY;


    public enum DIRECTION{
        NN,EE,SS,WW,NE,NW,SE,SW,XX
    };

    public static Map<DIRECTION,Integer[]> DIRECTION_OFFSET_MAP = Collections.unmodifiableMap(new HashMap<DIRECTION, Integer[]>() {{
        put(DIRECTION.NN, new Integer[]{0,1});
        put(DIRECTION.EE, new Integer[]{1,0});
        put(DIRECTION.SS, new Integer[]{0,-1});
        put(DIRECTION.WW, new Integer[]{-1,0});
        put(DIRECTION.XX, new Integer[]{0,0});
    }});

    private float lastTouchX;
    private float lastTouchY;
    private DIRECTION lastDirection;

    private int[] gridPosition = new int[]{0,0};


    private Long id;

    private Long number;

    private TileColorType colorType;

    private TileEventListener tileEventListener ;


    public Tile(float pX, float pY, float pWidth, float pHeight, VertexBufferObjectManager pVertexBufferObjectManager) {
        super(pX, pY, pWidth, pHeight, pVertexBufferObjectManager);

    }
    public Tile(float pX, float pY, float pWidth, float pHeight, VertexBufferObjectManager pVertexBufferObjectManager, int[] theGridPosition,float theCellWidth) {
        super(pX, pY, pWidth, pHeight, pVertexBufferObjectManager);
        gridPosition = theGridPosition;
        cellWidth = theCellWidth;
    }

    @Override
    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {

        DIRECTION direction = DIRECTION.XX;
        float x;
        float y;
        if(currentMode == Board.MODE.PLAY) {
            switch (pSceneTouchEvent.getMotionEvent().getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = pSceneTouchEvent.getX();
                    lastTouchY = pSceneTouchEvent.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    x = pSceneTouchEvent.getX();
                    y = pSceneTouchEvent.getY();

//                if(Math.abs(x- lastTouchX) > Math.abs(y-lastTouchY))
//                {
//                    this.setX(x - this.getWidth() / 2);
//
//                }
//                else
//                {
//                    this.setY(y - this.getHeight() / 2);
//
//                }
                    //this.setPosition(x - this.getWidth() / 2, y- this.getHeight() / 2);

                    break;
                case MotionEvent.ACTION_UP:
                    x = pSceneTouchEvent.getX();
                    y = pSceneTouchEvent.getY();
                    this.setPosition(x - this.getWidth() / 2, y - this.getHeight() / 2);
                    lastDirection = getMovedDirection(x, y);
                    if (tileEventListener != null) {
                        tileEventListener.onTileMove(id, lastDirection);
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;

            }
        }
        //this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
        return  true;
    }

    private DIRECTION getMovedDirection( float x,  float y) {
        DIRECTION direction = DIRECTION.XX;
        float dx =  x - lastTouchX;
        float dy =  y - lastTouchY;

        lastTouchX = x;
        lastTouchY = y;

       // if (Math.abs(dx) > DELTA || Math.abs(dy) > DELTA) { // change grid point
            direction  = calculateDirection(dx, dy);
       // }
        return direction;
    }
    private void updatePosition( float x,  float y)
    {
         float dx= lastTouchX - x;
         float dy = lastTouchY - y ;

        lastTouchX = x;
        lastTouchY = y;

        float[] newScreenLocation = new float[]{x,y};
        int[] newGridLocation = new int[2];
        if( Math.abs(dx) > DELTA || Math.abs(dy) > DELTA)
        { // change grid point
            DIRECTION direction = calculateDirection(dx,dy);
            Integer[] offset = DIRECTION_OFFSET_MAP.get(direction);

            if(gridPosition[0] + offset[0] <= Board.GRID_SZ && gridPosition[0] + offset[0] >= 0) {
                newGridLocation[0] =  gridPosition[0] + offset[0];
            }
            else {
                newGridLocation[0] =gridPosition[0];
            }
            if(gridPosition[1] + offset[1] <= Board.GRID_SZ && gridPosition[1] + offset[1] >= 0) {
                newGridLocation[1] =  gridPosition[1] + offset[1];
            }
            else {
                newGridLocation[1] =gridPosition[1];
            }
            newScreenLocation = CoordinateConversionUtility.tilePointToScreenPoint(newGridLocation,cellWidth);

        } // else local move, no change to inputs
        //Log.i(LOG_TAG,"grid point = " + newGridLocation[0]+","+newGridLocation[1]);
        //Log.i(LOG_TAG,"screen point = " + String.valueOf(newScreenLocation[0] - this.getWidth() / 2)+","+(newScreenLocation[1] - this.getHeight() / 2));
        this.setPosition(newScreenLocation[0] - this.getWidth() / 2, newScreenLocation[1] - this.getHeight() / 2);
    }

    private DIRECTION calculateDirection( float dx, float dy)
    {
        DIRECTION direction = DIRECTION.XX;
        float abx = Math.abs(dx);
        float aby = Math.abs(dy);
        if(dy > 0 && abx < aby )
        {
            direction = DIRECTION.NN;
        }
        else if( dx > 0 && abx >aby )
        {
            //Log.i(LOG_TAG,"EE");
            direction = DIRECTION.EE;
        }
        else if(dx < 0  && abx > aby )
        {
            //Log.i(LOG_TAG,"WW");
            direction = DIRECTION.WW;
        }
        else if(dy < 0 && abx < aby )
        {
            //Log.i(LOG_TAG,"SS");
            direction = DIRECTION.SS;
        }

        return  direction;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TileEventListener getTileEventListener() {
        return tileEventListener;
    }

    public void setTileEventListener(TileEventListener tileEventListener) {
        this.tileEventListener = tileEventListener;
    }

    public int[] getGridPosition() {
        return gridPosition;
    }

    public void setGridPosition(int[] gridPosition) {
        this.gridPosition = gridPosition;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public TileColorType getColorType() {
        return colorType;
    }

    public void setColorType(TileColorType colorType) {

        this.colorType = colorType;
    }

    public Text getTileContentText() {
        return tileContentText;
    }

    public void setTileContentText(Text tileContentText) {
        this.tileContentText = tileContentText;
    }

    @Override
    public void onStatusUpdate(Board.MODE mode) {
        currentMode = mode;

    }
}
