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
    //Map<Integer,Long>  tilesIdMap = new HashMap<>();
    private MODE mode = MODE.PLAY;
    private int cameraHeight =-1;
    private long score = 0L;
    private long bestScore = 0L;
    private Rectangle backgroundCentre ;
    private Text scoreLableText;
    private Text bestScoreLabelText;

    public long getScore() {
        return score;
    }

    public long getBestScore() {
        return bestScore;
    }

    public void setBestScore(long bestScore) {
        this.bestScore = bestScore;
    }

    public MODE getMode() {
        return mode;
    }

    public void setMode(MODE mode) {
        this.mode = mode;
    }

    public enum MODE{
        PLAY,
        OVER,
        UNKNOWN
    }
    private Text statusText;// new Text(cameraHeight/15f + 420, cameraHeight - 500, ResourceManager.getFont(), "Game Over !", "Game Over !     ".length(), vertexBufferObjectManager);

    private List<StatusEventListener> statusEventListener = new ArrayList<>();

    public Board(Scene mScene, Engine mEngine, float tileWidth, VertexBufferObjectManager vbufMgr, float scale,int theCameraHieght) {
        this.scene = mScene;
        this.engine = mEngine;
        this.tileWidth = tileWidth;
        this.vertexBufferObjectManager = vbufMgr;
        this.scale = scale;
        this.cameraHeight = theCameraHieght;
        UniqueIdGenerator.reset();
        Rectangle backgroundHeading = new Rectangle(cameraHeight/90f,cameraHeight/90f,0.54f*cameraHeight,cameraHeight*0.72f,vertexBufferObjectManager);
        backgroundHeading.setColor(0.9f,0.9f,0.9f,1f);
        //mScene.attachChild(backgroundHeading);


        backgroundCentre = new Rectangle(0.05f*cameraHeight,cameraHeight *.18f,0.48f*cameraHeight,0.48f*cameraHeight,vertexBufferObjectManager);
        backgroundCentre.setColor(0.9f,0.9f,0.9f,0.5f);

       // mScene.attachChild(backgroundCentre);

        Rectangle backgroundBottom = new Rectangle(cameraHeight/90f,cameraHeight - 0.14f*cameraHeight,0.54f*cameraHeight,0.13f*cameraHeight,vertexBufferObjectManager);
        backgroundBottom.setColor(0.9f,0.9f,0.9f,1f);
       // mScene.attachChild(backgroundBottom);

        final Text commandButtonText = new Text(25, 30,ResourceManager.getFont() , "New Game", "New Game".length(), vertexBufferObjectManager);

        Rectangle commandButton = new Rectangle(cameraHeight*0.42f , (cameraHeight/90f)*4,tileWidth * SCALE*1.1f, tileWidth * SCALE *.75f,vertexBufferObjectManager){
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
        commandButton.setColor(0.95f,0.95f,0.95f,.8f);
        commandButton.attachChild(commandButtonText);
        scene.registerTouchArea(commandButton);
       // mScene.attachChild(commandButton);



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

        final Text scoreText = new Text(cameraHeight/15f, 0.75f*cameraHeight ,ResourceManager.getLargeFont() , "Score:", "Score: XXXXXXXXXX".length(), vertexBufferObjectManager);
        final Text bestScoreText = new Text(cameraHeight/15f + 0.25f*cameraHeight, 0.75f*cameraHeight, ResourceManager.getLargeFont(), "Best:", "Best: XXXXXXXXXX".length(), vertexBufferObjectManager);
        final Text titleText = new Text(cameraHeight/20f , 0.045f*cameraHeight, ResourceManager.getTileFont(), "Color Quest", "Color Quest".length(), vertexBufferObjectManager);
        statusText = new Text(.1f*cameraHeight, 0.35f*cameraHeight, ResourceManager.getLargeFont(), "Game Over !", "Game Over !     ".length(), vertexBufferObjectManager);
        scoreLableText = new Text(cameraHeight/15f, 0.70f*cameraHeight,ResourceManager.getTileContentFont() , "SCORE", "SCORE".length(), vertexBufferObjectManager);
        bestScoreLabelText = new Text(cameraHeight/15f + 0.25f*cameraHeight, 0.70f*cameraHeight,ResourceManager.getTileContentFont() , "BEST SCORE", "BEST SCORE".length(), vertexBufferObjectManager);

        mScene.attachChild(scoreText);
        mScene.attachChild(bestScoreText);
        //mScene.attachChild(titleText);
        statusText.setText("");
        //mScene.attachChild(statusText);

        scene.registerUpdateHandler(new TimerHandler(1 / 20.0f, true, new ITimerCallback() {
            @Override
            public void onTimePassed(final TimerHandler pTimerHandler) {
                scoreText.setText( String.valueOf(score));
                bestScoreText.setText(String.valueOf(bestScore) );
            }
        }));

    }

    protected void restartGame()
    {
        final Engine.EngineLock engineLock = engine.getEngineLock();
        engineLock.lock();
        for(Tile aTile : tiles)
        {
            aTile.detachChildren();
            scene.detachChild(aTile);
            scene.unregisterTouchArea(aTile);
        }
        scene.detachChild(statusText);
        scene.detachChild(backgroundCentre);
        scene.detachChild(scoreLableText);
        scene.detachChild(bestScoreLabelText);
        tiles.clear();

        engineLock.unlock();
        //tilesIdMap.clear();
        score = 0;

        statusText.setText("");

        for(int i=0;i<INITIAL_TILES;i++)
        {
            addTile(getNextFreeLocation());
        }

        notifyStatus();
    }

    private void addTile(int[] position) {

        if(!isTileInPosition(position))
        {
            Long id = UniqueIdGenerator.getNextId();
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
            //tilesIdMap.put(generateIndex(position),id);
          //  dump();
           // Log.i(LOG_TAG,"screen point = " + screenPoint[0]+","+screenPoint[1]+" - grid point : "+ position[0]+","+position[1] );
        }
    }
    private void addBackgroundTile(int[] position,Color color) {

       // if(!isTileInPosition(position)) {
            float[] screenPoint = CoordinateConversionUtility.tilePointToScreenPoint(position, tileWidth);
            Rectangle rectangle = new Rectangle(screenPoint[0], screenPoint[1], tileWidth * SCALE, tileWidth * SCALE, vertexBufferObjectManager);
            rectangle.setColor(color);
            scene.attachChild(rectangle);
        //}
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

//    private boolean isIdInMap(Long theId)
//    {
//        boolean found = false;
//        for(Long id: tilesIdMap.values())
//        {
//            if(id == theId)
//            {
//                found = true;
//                break;
//            }
//        }
//        return found;
//    }
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
                int [] position = new int[]{i, j};
                if(!isTileInPosition(position))
                {
                    availableIndexList.add(generateIndex(position));
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

    private boolean isTileInPosition(int [] position)
    {
        boolean found = false;

        for(Tile aTile : tiles)
        {
            int[] gridPosition = aTile.getGridPosition();

            if(gridPosition[0] == position[0] && gridPosition[1] == position[1])
            {
                found = true;
                break;
            }
        }

        return found;
    }

    private Tile findTileInPosition(int [] position)
    {
        Tile found = null;

        for(Tile aTile : tiles)
        {
            int[] gridPosition = aTile.getGridPosition();

            if(gridPosition[0] == position[0] && gridPosition[1] == position[1])
            {
                found = aTile;
                break;
            }
        }

        return found;
    }
    private int[] moveTilePositionByGivenOffset(int[] currentPosition, Integer[] offset)
    {
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
        return newPosition;
    }


    @Override
    public void onTileMove(Long id, Tile.DIRECTION direction) {
        if(mode == MODE.PLAY) {
            Tile tile = findTile(id);
            Integer[] offset = Tile.DIRECTION_OFFSET_MAP.get(direction);

            if (tile != null) {
                int[] currentPosition = tile.getGridPosition();
                int[] newPosition;
                int [] previousPosition = currentPosition;
                while(true )
                {
                    newPosition = moveTilePositionByGivenOffset(previousPosition, offset);
                    if(newPosition[0] == previousPosition[0] && newPosition[1] == previousPosition[1]
                            || isTileInPosition(newPosition))
                    {
                        break;
                    }
                    previousPosition = newPosition;
                }

                float[] newScreenPoint = CoordinateConversionUtility.tilePointToScreenPoint(newPosition, tileWidth);
                //tile.setPosition(newScreenPoint[0], newScreenPoint[1]);
                //tile.setGridPosition(newPosition);

                Integer indexForNewPosition = generateIndex(newPosition);
                Tile existingTile = findTileInPosition(new int[]{newPosition[0],newPosition[1]});
                if (existingTile != null) {

                    if (tile.getColorType() == existingTile.getColorType()) {
                        tile.setPosition(newScreenPoint[0], newScreenPoint[1]);
                        tile.setGridPosition(newPosition);
                        Long numberExisting = existingTile.getNumber();
                        final Engine.EngineLock engineLock = engine.getEngineLock();
                        engineLock.lock();
                        //dump();
		            /* Now it is save to remove the entity! */
                        scene.unregisterTouchArea(existingTile);
                        existingTile.detachChildren();
                        scene.detachChild(existingTile);

                        tiles.remove(existingTile);
                        //tilesIdMap.remove(indexForNewPosition);
                        //tilesIdMap.remove(generateIndex(currentPosition));
                        //tilesIdMap.put(indexForNewPosition, id);
                       // dump();
                        TileColorType colorType = tile.getColorType();
                        TileColorType nextColorType = getNextColorType(colorType);

                        tile.setColor(Utility.getColorForType(nextColorType));
                        tile.setColorType(nextColorType);
                        Long number = tile.getNumber();
                        tile.setNumber(number+numberExisting);

                        tile.getTileContentText().setText(String.valueOf(tile.getNumber()));
                        score += (number+numberExisting);
                        if(tile.getColorType() == TileColorType.VIOLATE)
                        {
                            shrinkBoard();
                        }
                        engineLock.unlock();
//                        addTileWithColor(
//                                existingTile.getGridPosition(),
//                                Utility.getColorForType(getNextColorType(existingTile.getColorType()))
//                        );
                    }
                    else
                    {
                        float[] currentScreenPoint = CoordinateConversionUtility.tilePointToScreenPoint(previousPosition, tileWidth);
                        tile.setPosition(currentScreenPoint[0], currentScreenPoint[1]);
                        tile.setGridPosition(previousPosition);
                    }
                } else { // new position is empty, update id map to reflect the tile move
                    tile.setPosition(newScreenPoint[0], newScreenPoint[1]);
                    tile.setGridPosition(newPosition);
                    //tilesIdMap.remove(generateIndex(currentPosition));
                   // tilesIdMap.put(indexForNewPosition, id);
                }


                Log.i(LOG_TAG, "new screen point = " + newScreenPoint[0] + "," + newScreenPoint[1] + " - new grid point : " + newPosition[0] + "," + newPosition[1]);

            }
            int[] nextPosition = getNextFreeLocation();
            if (nextPosition[0] != -1 && nextPosition[1] != -1) {
                addTile(nextPosition);
            }
            else
            {
                if(!checkValidMoves()) {
                    mode = MODE.OVER;
                    statusText.setText("Game Over!");
                    if (score > bestScore) {
                        bestScore = score;
                    }

                    scene.attachChild(backgroundCentre);
                    scene.attachChild(statusText);
                    scene.attachChild(scoreLableText);
                    scene.attachChild(bestScoreLabelText);

                    notifyStatus();
                }
            }
        }
    }

    private void shrinkBoard() {

        Map<TileColorType, List<Tile>> colorTypeTpPositionMap = new HashMap<>();
        List<Long> tilesToDelete = new ArrayList<>();

        for(Tile aTile : tiles)
        {
            TileColorType tileColorType = aTile.getColorType();
            List<Tile> list = colorTypeTpPositionMap.get(tileColorType);
            if(list == null)
            {
                list = new ArrayList<>();
                list.add(aTile);
                colorTypeTpPositionMap.put(tileColorType,list);
            }
            else {
                list.add(aTile);
            }

        }

        for( List<Tile> positionList : colorTypeTpPositionMap.values())
        {
            int index =0;
            Long selectedTileId = null;
            int newNumber = 0;
            for(Tile aTile : positionList)
            {
                if(index == 0 )
                {
                    selectedTileId = aTile.getId();
                }
                else
                {
                    newNumber += aTile.getNumber();
                    tilesToDelete.add(aTile.getId());
                }
                index++;

            }
            Tile selectedTile = findTile(selectedTileId);
            if(selectedTile != null)
            {
                selectedTile.setNumber(new Long(newNumber));
            }

            for(Long aId : tilesToDelete)
            {
                Tile aTile = findTile(aId);
                if(aTile != null && aTile.getColorType() != TileColorType.VIOLATE)
                {
                    scene.unregisterTouchArea(aTile);
                    scene.detachChild(aTile);
                    tiles.remove(aTile);
                }
            }
        }

        //return validMoves;
    }

//    private void dump()
//    {
//        Log.i(LOG_TAG,"tiles:");
//        for(Tile aTile : tiles)
//        {
//            Log.i(LOG_TAG,String.valueOf(aTile.getId()) + " ");
//        }
//        Log.i(LOG_TAG,"tilesIdMap:");
//        for(Long id : tilesIdMap.values())
//        {
//            Log.i(LOG_TAG,String.valueOf(id) + " ");
//        }
//    }


    private boolean checkValidMoves()
    {
        boolean validMoves = false;
        Map<TileColorType,List<Integer[]>> colorTypeTpPositionMap = new HashMap<>();

        for(Tile aTile : tiles)
        {
            TileColorType tileColorType = aTile.getColorType();
            int [] position = aTile.getGridPosition();
            List<Integer[]> list = colorTypeTpPositionMap.get(tileColorType);
            if(list == null)
            {
                list = new ArrayList<>();
                list.add(new Integer[]{position[0],position[1]});
                colorTypeTpPositionMap.put(tileColorType,list);
            }
            else {
                list.add(new Integer[]{position[0], position[1]});
            }

        }

        for( List<Integer[]> positionList : colorTypeTpPositionMap.values())
        {
            for(Integer[] position : positionList)
            {
                int x = position[0];
                int y = position[1];
                for(Integer[] aPosition : positionList)
                {
                    int x1 = aPosition[0];
                    int y1 = aPosition[1];
                    if(!(x==x1 && y == y1))
                    {
                        if((x==x1 && Math.abs(y-y1)==1) || ( y==y1 && Math.abs(x-x1)==1))
                        {
                            validMoves = true;
                            break;
                        }
                    }

                }
                if(validMoves)
                {
                    break;
                }
            }
        }

        return validMoves;
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
