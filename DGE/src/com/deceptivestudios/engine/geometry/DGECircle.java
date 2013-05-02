package com.deceptivestudios.engine.geometry;

import com.deceptivestudios.engine.helper.DGERect;
import com.deceptivestudios.engine.helper.DGEVector;

public class DGECircle extends DGEShape 
{
	public DGEVector Center;
	public float Radius;
	
	public DGECircle()
	{
		super(DGEShape.ShapeType.Circle);
		
		Center = new DGEVector();
		Radius = 0;
	}
	
	public DGECircle(DGEVector center, float radius)
	{
		this();
		
		Center.set(center);
		Radius = radius;
	}
	
	public DGECircle(DGECircle other)
	{
		this();
		
		Center.set(other.Center);
		Radius = other.Radius;
	}

	public DGERect Rect()
	{
		return new DGERect(Center.x - Radius, Center.y - Radius, Center.x + Radius, Center.y + Radius);
	}
}
