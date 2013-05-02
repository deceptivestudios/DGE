package com.deceptivestudios.engine.polygon;

import com.deceptivestudios.engine.geometry.DGEPolygon;

public class DGEPolygonCellData 
{
	public DGEPolygon Polygon;
	public int Layer;
	public int Sublayer;
	
	public boolean Flag;
	
	public DGEPolygonCellData(DGEPolygon polygon, int layer, int sublayer)
	{
		Polygon = polygon;
		Layer = layer;
		Sublayer = sublayer;
		
		Flag = false;
	}
}
