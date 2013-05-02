package com.deceptivestudios.engine.polygon;

import java.util.Vector;

import com.deceptivestudios.engine.helper.DGERect;

public class DGEPolygonCell 
{
	public DGERect Rect;
	public Vector<DGEPolygonCellData> Objects;
	
	public DGEPolygonCell()
	{
		Rect = new DGERect();
		Objects = new Vector<DGEPolygonCellData>();
	}
}
