package com.deceptivestudios.engine.geometry;

import com.deceptivestudios.engine.helper.DGERect;
import com.deceptivestudios.engine.helper.DGEVector;

public class DGERectangle extends DGEShape 
{
	public DGEVector Point1, Point2;
	
	public DGERectangle()
	{
		super(DGEShape.ShapeType.Rectangle);
		
		Point1 = new DGEVector();
		Point2 = new DGEVector();
	}
	
	public DGERectangle(DGEVector p1, DGEVector p2)
	{
		this();
		
		Point1.set(p1);
		Point2.set(p2);
	}

	public DGERectangle(float x1, float y1, float x2, float y2)
	{
		this(new DGEVector(x1, y1), new DGEVector(x2, y2));
	}
	
	public DGERectangle(DGERect rect)
	{
		this(rect.p1, rect.p2);
	}
	
    public DGERect Rect() 
    { 
    	return new DGERect(Point1.x, Point1.y, Point2.x, Point2.y); 
    }
    
    public DGEVector Size() 
    { 
    	return new DGEVector(Point2.x - Point1.x, Point2.y - Point1.y); 
    }

    public void Shift(float x, float y)
    {
        Shift(new DGEVector(x, y));
    }
    
    public void Shift(DGEVector amount)
    {
        Point1 = Point1.add(amount);
        Point2 = Point2.add(amount);
    }
}
