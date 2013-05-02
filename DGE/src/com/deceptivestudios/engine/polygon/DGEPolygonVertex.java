package com.deceptivestudios.engine.polygon;

import com.deceptivestudios.engine.helper.DGEVector;

public class DGEPolygonVertex 
{
	public DGEVector Position;
	public DGEPolygonVertex Next;
	public float Normal;
	
	public DGEPolygonVertex(DGEVector pos)
	{
		Normal = 0;
		Position = pos;
		Next = null;
	}

	public DGEPolygonVertex(DGEPolygonVertex copy)
	{
		Normal = copy.Normal;
		Position = copy.Position;
		Next = null;
	}
}
