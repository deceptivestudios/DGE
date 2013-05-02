package com.deceptivestudios.engine.geometry;

public class DGEShape
{
	public enum ShapeType
	{
		Line,
		Circle,
		Rectangle,
		Polygon
	}
	
	private ShapeType _shapeType;
	
	public DGEShape(ShapeType shapeType)
	{
		_shapeType = shapeType;
	}

	public ShapeType GetShape()
	{
		return _shapeType;
	}
}
