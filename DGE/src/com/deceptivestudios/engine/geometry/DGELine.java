package com.deceptivestudios.engine.geometry;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGERect;
import com.deceptivestudios.engine.helper.DGEVector;

public class DGELine extends DGEShape
{
	public DGEVector Point1, Point2;
	
    public DGELine()
    {
    	super(DGEShape.ShapeType.Line);
    	
    	Point1 = new DGEVector();
    	Point2 = new DGEVector();
    }
    
    public DGELine(float x1, float y1, float x2, float y2)
    {
    	this(new DGEVector(x1, y1), new DGEVector(x2, y2));
    }
    
    public DGELine(DGEVector p1, DGEVector p2)
    {
    	this();
    	
    	Point1.set(p1);
        Point2.set(p2);
    }
    
    public DGEVector NearestPoint(DGEVector point)
    {
    	DGEVector result = new DGEVector();
    	long dot_ta, dot_tb;
    	
    	dot_ta = (long) ((point.x - Point1.x) * (Point2.x - Point1.x) + (point.y - Point1.y) * (Point2.y - Point1.y));
    	
    	if (dot_ta <= 0)
    	{
    		result.x = Point1.x;
    		result.y = Point1.y;
    		
    		return result;
    	}
    	
    	dot_tb = (long) ((point.x - Point2.x) * (Point1.x - Point2.x) + (point.y - Point2.y) * (Point1.y - Point2.y));
    	
    	if (dot_tb <= 0)
    	{
    		result.x = Point2.x;
    		result.y = Point2.y;
    		
    		return result;
    	}
    	
    	result.x = Point1.x + ((Point2.x - Point1.x) * dot_ta) / (dot_ta + dot_tb);
    	result.x = Point1.y + ((Point2.y - Point1.y) * dot_ta) / (dot_ta + dot_tb);
    	
    	return result;
    }

    public DGEIntersect Intersects(DGEVector pos1, DGEVector pos2)
    {
    	return Intersects(new DGELine(pos1, pos2));
    }

    public DGEIntersect Intersects(DGELine line)
    {
    	DGEIntersect intersect = new DGEIntersect();
    	DGEVector result = new DGEVector();
    	
    	float d, r;
    	
    	DGEVector v1to2 = Point2.subtract(Point1);
    	DGEVector v3to4 = line.Point2.subtract(line.Point1);
    	
    	if (v1to2.y / v1to2.x != v3to4.y / v3to4.x)
    	{
    		d = v1to2.x * v3to4.y - v1to2.y * v3to4.x;
    		
    		if (d != 0)
    		{
    			DGEVector v3to1 = Point1.subtract(line.Point1);
    			r = (v3to1.y * v3to4.x - v3to1.x * v3to4.y) / d;
    			
    			v3to1 = Point1.add(Point2.subtract(Point1).multiply(r));
    			
    			result.x = v3to1.x;
    			result.y = v3to1.y;
    			
                DGERect r1  = new DGERect(Math.min(Point1.x, Point2.x), Math.min(Point1.y, Point2.y), Math.max(Point2.x, Point1.x), Math.max(Point2.y, Point1.y));
                DGERect r2  = new DGERect(Math.min(line.Point1.x, line.Point2.x), Math.min(line.Point1.y, line.Point2.y), Math.max(line.Point2.x, line.Point1.x), Math.max(line.Point2.y, line.Point1.y));

                // Test r1
                boolean test1 = false;
                
                if (r1.p1.y == r1.p2.y)
                    test1 = ((result.x >= r1.p1.x && result.x <= r1.p2.x) && (result.y == r1.p1.y));
                else if (r1.p1.x == r1.p2.x) //r1 is vertical
                    test1 = ((result.y >= r1.p1.y && result.y <= r1.p2.y) && (result.x == r1.p1.x));
                else
                    test1 = r1.TestPoint(result.x, result.y);

                // Test r2
                boolean test2 = false;
                
                if (r2.p1.y == r2.p2.y)
                	test2 = ((result.x >= r2.p1.x && result.x <= r2.p2.x) && (result.y == r2.p1.y));
                else if (r2.p1.x == r2.p2.x) //r2 is vertical
                    test2 = ((result.y >= r2.p1.y && result.y <= r2.p2.y) && (result.x == r2.p1.x));
                else
                    test2 = r2.TestPoint(result.x, result.y);

                intersect.Collides = test1 && test2;

                if (intersect.Collides)
                {
                    // Set Collision Normal
                	intersect.Points.add(result);
                	intersect.Normals.add(Point2.subtract(Point1).angle() + DGE.M_PI);
                }
    		}
    	}
    	
    	return intersect;
    }

    public DGEIntersect Intersects(DGECircle circle)
    {
    	DGEIntersect result = new DGEIntersect();
    	DGEVector point = NearestPoint(circle.Center);

    	if ((new DGELine(circle.Center, point)).Length() < circle.Radius)
    	{
    		result.Collides = true;
    		result.Points.add(point);
    		result.Normals.add((Point2.subtract(Point1)).angle() + DGE.M_PI);
    	}
    	
    	return result;
    }
    
    public DGERect Rect()
    {
    	return new DGERect(Math.min(Point1.x, Point2.x), Math.min(Point1.y, Point2.y), Math.max(Point1.x, Point2.x), Math.max(Point1.y, Point2.y));
    }

    public float Length()
    {
        return (Point2.subtract(Point1)).length();
    }

    public float Angle()
    {
        return (Point2.subtract(Point1)).angle();
    }
}
