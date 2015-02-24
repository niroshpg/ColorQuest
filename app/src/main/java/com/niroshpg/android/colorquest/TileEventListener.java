package com.niroshpg.android.colorquest;

/**
 * Created by Windows User on 13/02/2015.
 */
public interface TileEventListener {
    public void onTileMove(Long id, Tile.DIRECTION direction);

    public void onStatusUpdate(Board.MODE mode);
}
