package com.deceptivestudios.engine.helper;

public class DGERect
{
	public DGEVector p1, p2;
	private boolean _clean;

	public DGERect()
	{
		p1 = new DGEVector(); p2 = new DGEVector();
		_clean = true;
	}
	
	public DGERect(float x1, float y1, float x2, float y2)
	{
		p1 = new DGEVector(x1, y1);
		p2 = new DGEVector(x2, y2);
		
		_clean = false; 
	}
	
	public DGERect(DGEVector v1, DGEVector v2)
	{
		p1 = new DGEVector(v1);
		p2 = new DGEVector(v2);
	}
	
	public void Clear() 
	{
		_clean = true;
	}
	
	public boolean IsClean()
	{
		return _clean;
	}
	
	public void Set(float x1, float y1, float x2, float y2)
	{
		p1.set(x1, y1); 
		p2.set(x2, y2);
		
		_clean = false; 
	}
	
	public void SetRadius(float x, float y, float r)
	{
		p1.set(x - r, y - r);
		p2.set(x + r, y + r);
		
		_clean = false; 
	}
	
	public void Encapsulate(float x, float y)
	{
		if (_clean)
		{
			p1.set(x, y);
			p2.set(x, y);
			
			_clean = false;
		}
		else
		{
			if (x < p1.x) 
				p1.x = x;
			
			if (x > p2.x) 
				p2.x = x;
			
			if (y < p1.y) 
				p1.y = y;
			
			if (y > p2.y) 
				p2.y = y;
		}
	}

	public boolean TestPoint(DGEVector p)
	{
		return TestPoint(p.x, p.y);
	}
	
	public boolean TestPoint(float x, float y)
	{
		if (x >= p1.x && x < p2.x && y >= p1.y && y < p2.y) 
			return true;

		return false;
	}

	public boolean IsInside(DGERect other)
	{
		if (other.TestPoint(p1) && other.TestPoint(p2))
			return true;
		
		return false;
	}
	
	public boolean Intersect(DGERect other)
	{
		if (Math.abs(p1.x + p2.x - other.p1.x - other.p2.x) < (p2.x - p1.x + other.p2.x - other.p1.x))
			if(Math.abs(p1.y + p2.y - other.p1.y - other.p2.y) < (p2.y - p1.y + other.p2.y - other.p1.y))
				return true;

		return false;
	}
}
