package com.niroshpg.android.colorquest;

import android.util.Log;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Windows User on 13/02/2015.
 */
public class Board implements  TileEventListener{
    public static final int GRID_SZ = 4;
    private static final int INITIAL_TILES = 2;
    public static final float SCALE = 1.5f;
    private static final String LOG_TAG = Board.class.getSimpleName();
    private float tileWidth;
    private Scene scene;
    private Engine engine;
    private VertexBufferObjectManager vertexBufferObjectManager;
    private Random randomGenerator = new Random();
    private float scale;
    private int[][] grid = new int[GRID_SZ][GRID_SZ];
    List<Tile> tiles = new ArrayList<>();
    Map<Integer,Long>  tilesIdMap = new HashMap<>();
    private MODE mode = MODE.PLAY;
    private int cameraHeight =-1;
    private long score = 0L;
    public enum MODE{
        PLAY,
        OVER,
        UNKNOWN
    }
    final Text statusText = new Text(cameraHeight/15f + 420, cameraHeight - 500, ResourceManager.getFont(), "Game Over !", "Game Over !     ".length(), vertexBufferObjectManager);

    private List<StatusEventListener> statusEventListener = new ArrayList<>();

    public Board(Scene mScene, Engine mEngine, float tileWidth, VertexBufferObjectManager vbufMgr, float scale,int theCameraHieght) {
        this.scene = mScene;
        this.engine = mEngine;
        this.tileWidth = tileWidth;
        this.vertexBufferObjectManager = vbufMgr;
        this.scale = scale;
        this.cameraHeight = theCameraHieght;

        Rectangle backgroundHeading = new Rectangle(cameraHeight/90f,20,0.54f*cameraHeight,275,vertexBufferObjectManager);
        backgroundHeading.setColor(0.9f,0.9f,0.9f,1f);
        mScene.attachChild(backgroundHeading);


        Rectangle backgroundCentre = new Rectangle(cameraHeight/90f,320,0.54f*cameraHeight,cameraHeight - 620,vertexBufferObjectManager);
        backgroundCentre.setColor(0.9f,0.9f,0.9f,1f);
        mScene.attachChild(backgroundCentre);

        Rectangle backgroundBottom = new Rectangle(cameraHeight/90f,cameraHeight - 250,0.54f*cameraHeight,230,vertexBufferObjectManager);
        backgroundBottom.setColor(0.9f,0.9f,0.9f,1f);
        mScene.attachChild(backgroundBottom);

        final Text commandButtonText = new Text(30, 60,ResourceManager.getFont() , "New Game", "New Game".length(), vertexBufferObjectManager);

        Rectangle commandButton = new Rectangle(cameraHeight/20f + cameraHeight*0.4f , 40,tileWidth * SCALE, tileWidth * SCALE,vertexBufferObjectManager){
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if(mode == MODE.OVER)
                {
                    mode = MODE.PLAY;
                }
                restartGame();
                return true;
            }
        };
        commandButtonText.setText("New");
        commandButton.setColor(0.7f,0.7f,0.7f,1f);
        commandButton.attachChild(commandButtonText);
        scene.registerTouchArea(commandButton);
        mScene.attachChild(commandButton);

        int[][] position = new int[GRID_SZ][GRID_SZ];
        Color bgColor = new Color(0.8f,0.8f,0.8f,1f);
        for(int i =0 ;i< GRID_SZ;i++)
        {
            for(int j =0 ;j< GRID_SZ;j++) {
                position[i] = new int[]{i,j};
                addBackgroundTile(position[i],bgColor);
            }
        }

        for(int i=0;i<INITIAL_TILES;i++)
        {
            addTile(getNextFreeLocation());
        }

        notifyStatus();

        final Text scoreText = new Text(cameraHeight/15f, cameraHeight - 500,ResourceManager.getFont() , "Score:", "Score: XXXXXXXXXX".length(), vertexBufferObjectManager);
        final Text bestScoreText = new Text(cameraHeight/15f + 420, cameraHeight - 500, ResourceManager.getFont(), "Best:", "Best: XXXXXXXXXX".length(), vertexBufferObjectManager);
        final Text titleText = new Text(cameraHeight/20f , 80, ResourceManager.getTileFont(), "Color Quest", "Color Quest".length(), vertexBufferObjectManager);

        mScene.attachChild(scoreText);
        mScene.attachChild(bestScoreText);
        mScene.attachChild(titleText);
        statusText.setText("Ready");
        mScene.attachChild(statusText);

        
        scene.registerUpdateHandler(new TimerHandler(1 / 20.0f, true, new ITimerCallback() {
            @Override
            public void onTimePassed(final TimerHandler pTimerHandler) {
                scoreText.setText("Score: " + score);
                bestScoreText.setText("Best: " + 0);

            }
        }));

    }

    private void restartGame()
    {
        for(Tile aTile : tiles)
        {
            aTile.detachChildren();
            scene.detachChild(aTile);
        }
        tiles.clear();
        tilesIdMap.clear();
        score = 0;

        statusText.setText("");

        for(int i=0;i<INITIAL_TILES;i++)
        {
            addTile(getNextFreeLocation());
        }

        notifyStatus();
    }

    private void addTile(int[] position) {
        Long id = UniqueIdGenerator.getNextId();
        if(!isIdInMap(id))
        {
            float[] screenPoint = CoordinateConversionUtility.tilePointToScreenPoint(position,tileWidth);
            Tile tile = new Tile(screenPoint[0],screenPoint[1],tileWidth*SCALE,tileWidth*SCALE,vertexBufferObjectManager, position,tileWidth);
            tile.setColor(getNextRandomColor());
            tile.setColorType(Utility.getColorTypeForColor(tile.getColor()));
            tile.setId(id);
            tile.setNumber(2L);
            tile.setTileEventListener(this);
          //  String idText = String.valueOf(tile.getNumber());
            String idText =  String.format("%1$" + 5 + "s",tile.getNumber());
            final Text tileContentText = new Text(tile.getWidth()/4,tile.getHeight()/4 ,ResourceManager.getTileContentFont() ,  idText, idText.length(),vertexBufferObjectManager);
            tile.setTileContentText(tileContentText);
            tile.attachChild(tileContentText);
            statusEventListener.add(tile);
            //grid[position[1]][position[0]]=id;
            tiles.add(tile);
            scene.registerTouchArea(tile);
            scene.attachChild(tile);
            tilesIdMap.put(generateIndex(position),id);
           // Log.i(LOG_TAG,"screen point = " + screenPoint[0]+","+screenPoint[1]+" - grid point : "+ position[0]+","+position[1] );
        }
    }
    private void addBackgroundTile(int[] position,Color color) {
        Long id = UniqueIdGenerator.getNextId();
        if(!isIdInMap(id)) {
            float[] screenPoint = CoordinateConversionUtility.tilePointToScreenPoint(position, tileWidth);
            Rectangle rectangle = new Rectangle(screenPoint[0], screenPoint[1], tileWidth * SCALE, tileWidth * SCALE, vertexBufferObjectManager);
            rectangle.setColor(color);
            scene.attachChild(rectangle);
        }
    }



    private Color getNextRandomColor()
    {
        Color color = Color.RED;
        switch (randomGenerator.nextInt(2))
        {
            case 0:
                color = new Color(1, 0, 0,1);
                break;
            case 1:
                color = new Color(1, 0.65f, 0,1);
                break;
            case 2:
                color = new Color(1, 1, 0,1);
                break;
            default:
                break;
        }
        return color;
    }

    private boolean isIdInMap(Long theId)
    {
        boolean found = false;
        for(Long id: tilesIdMap.values())
        {
            if(id == theId)
            {
                found = true;
                break;
            }
        }
        return found;
    }
    private Integer generateIndex(int[] position)
    {
       return new Integer(position[1]*GRID_SZ + position[0]);
    }

//    private int[] getPositionForIndex(Integer index)
//    {
//        int[] position = new int[2];
//
//        if(index != null) {
//            String[] keyValues = id.split("\\.");
//            if(keyValues.length == 2) {
//                position[1] = Integer.parseInt(keyValues[0]);
//                position[0] = Integer.parseInt(keyValues[1]);
//            }
//            else
//            {
//                Log.e(LOG_TAG,"error ");
//            }
//        }
//        return position;
//    }

    private int[] getPositionForIndex(Integer index)
    {
        int[] position = new int[2];

        if(index != null && index > 0) {
            position[0] = index % GRID_SZ;
            position[1] = (index - index% GRID_SZ ) /GRID_SZ;
        }
        return position;
    }

    private int[] getNextFreeLocation() {
        int[] nextLocation = new int[]{-1,-1};
        List<Integer> availableIndexList = new ArrayList<>();
        for(int i = 0; i< GRID_SZ;i++)
        {
            for(int j = 0; j< GRID_SZ;j++)
            {
                Integer index = generateIndex(new int[]{i, j});
                if(tilesIdMap.get(index)== null)
                {
                    availableIndexList.add(index);
                }
            }
        }
        if(availableIndexList.size()>0)
        {
            int size = availableIndexList.size();
            int i = randomGenerator.nextInt(size);
            Integer index = availableIndexList.get(i);
            nextLocation = getPositionForIndex(index);

        }

        return nextLocation;
    }

    private Tile findTile(Long id)
    {
        Tile matchingTile = null;
        for(Tile tile : tiles)
        {
            if(tile.getId() == id)
            {
                matchingTile = tile;
                break;
            }
        }
        return matchingTile;
    }



    @Override
    public void onTileMove(Long id, Tile.DIRECTION direction) {
        if(mode == MODE.PLAY) {
            Tile tile = findTile(id);
            Integer[] offset = Tile.DIRECTION_OFFSET_MAP.get(direction);

            if (tile != null) {
                int[] currentPosition = tile.getGridPosition();
                int[] newPosition = new int[2];
                if (currentPosition[0] + offset[0] >= 0 && currentPosition[0] + offset[0] < GRID_SZ) {
                    newPosition[0] = currentPosition[0] + offset[0];
                } else {
                    newPosition[0] = currentPosition[0];
                }
                if (currentPosition[1] + offset[1] >= 0 && currentPosition[1] + offset[1] < GRID_SZ) {
                    newPosition[1] = currentPosition[1] + offset[1];
                } else {
                    newPosition[1] = currentPosition[1];
                }


                float[] newScreenPoint = CoordinateConversionUtility.tilePointToScreenPoint(newPosition, tileWidth);
                //tile.setPosition(newScreenPoint[0], newScreenPoint[1]);
                //tile.setGridPosition(newPosition);

                Integer indexForNewPosition = generateIndex(newPosition);
                Long existingTileId = tilesIdMap.get(indexForNewPosition);
                if (existingTileId != null) {
                    Tile existingTile = findTile(existingTileId);

                    if (tile.getColorType() == existingTile.getColorType()) {
                        tile.setPosition(newScreenPoint[0], newScreenPoint[1]);
                        tile.setGridPosition(newPosition);
                        Long numberExisting = existingTile.getNumber();
                        final Engine.EngineLock engineLock = engine.getEngineLock();
                        engineLock.lock();

		            /* Now it is save to remove the entity! */
                        scene.unregisterTouchArea(existingTile);
                        existingTile.detachChildren();
                        scene.detachChild(existingTile);

                        tiles.remove(existingTile);
                        tilesIdMap.remove(indexForNewPosition);
                        tilesIdMap.remove(generateIndex(currentPosition));
                        tilesIdMap.put(indexForNewPosition, id);

                        TileColorType colorType = tile.getColorType();
                        TileColorType nextColorType = getNextColorType(colorType);

                        tile.setColor(Utility.getColorForType(nextColorType));
                        tile.setColorType(nextColorType);
                        Long number = tile.getNumber();
                        tile.setNumber(number+numberExisting);

                        tile.getTileContentText().setText(String.valueOf(tile.getNumber()));
                        score += 5;
                        engineLock.unlock();
//                        addTileWithColor(
//                                existingTile.getGridPosition(),
//                                Utility.getColorForType(getNextColorType(existingTile.getColorType()))
//                        );
                    }
                    else
                    {
                        float[] currentScreenPoint = CoordinateConversionUtility.tilePointToScreenPoint(currentPosition, tileWidth);
                        tile.setPosition(currentScreenPoint[0], currentScreenPoint[1]);
                        tile.setGridPosition(currentPosition);
                    }
                } else { // new position is empty, update id map to reflect the tile move
                    tile.setPosition(newScreenPoint[0], newScreenPoint[1]);
                    tile.setGridPosition(newPosition);
                    tilesIdMap.remove(generateIndex(currentPosition));
                    tilesIdMap.put(indexForNewPosition, id);
                }


                Log.i(LOG_TAG, "new screen point = " + newScreenPoint[0] + "," + newScreenPoint[1] + " - new grid point : " + newPosition[0] + "," + newPosition[1]);

            }
            int[] nextPosition = getNextFreeLocation();
            if (nextPosition[0] != -1 && nextPosition[1] != -1) {
                addTile(nextPosition);
            }
            else
            {
                mode = MODE.OVER;
                statusText.setText("Game Over!");
                notifyStatus();
            }
        }
    }

    private void notifyStatus()
    {
        for(StatusEventListener aStatusEventListener : statusEventListener)
        {
            aStatusEventListener.onStatusUpdate(mode);
        }
    }

    @Override
    public void onStatusUpdate(MODE mode) {

    }

    private TileColorType getNextColorType(TileColorType currentColorType)
    {
        TileColorType nextColorType = currentColorType;

        switch (currentColorType)
        {
            case RED:
                nextColorType = TileColorType.ORANGE;
                break;
            case ORANGE:
                nextColorType = TileColorType.YELLOW;
                break;
            case YELLOW:
                nextColorType = TileColorType.GREEN;
                break;
            case GREEN:
                nextColorType = TileColorType.BLUE;
                break;
            case BLUE:
                nextColorType = TileColorType.INDIGO;
                break;
            case INDIGO:
                nextColorType = TileColorType.VIOLATE;
                break;
            case VIOLATE:
                nextColorType = TileColorType.VIOLATE;
                break;
            default:
                break;
        }

        return nextColorType;
    }
}
