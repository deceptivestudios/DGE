package com.deceptivestudios.engine.geometry;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGETriple;
import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.helper.DGEVertex;

public class DGETriangle 
{
	public DGETriple Triple;
	public DGEVector[] Vertices;
	
	private DGETriangle()
	{
		Triple = new DGETriple();
		Vertices = new DGEVector[3];
	}
	
	public DGETriangle(DGEVector[] vertices)
	{
		this();

		Triple.blend = DGE.DGE_BLEND_DEFAULT;
		
		for (int i = 0; i < 3; i++)
		{
			Triple.vertices[i].z = 0.5f;
			Triple.vertices[i].a = Triple.vertices[0].r = Triple.vertices[0].g = Triple.vertices[0].b = 1f;
			
			Triple.vertices[i].x = vertices[i].x;
			Triple.vertices[i].y = vertices[i].y;
			
			Vertices[i] = new DGEVector(vertices[i]);
		}
	}
	
	public DGETriangle(DGEVector v1, DGEVector v2, DGEVector v3)
	{
		this(new DGEVector[] { v1, v2, v3 });
	}
	
	public DGETriangle(DGETriangle copy)
	{
		this();
		
		Triple.blend = copy.Triple.blend;
		Triple.texture = copy.Triple.texture;
		
		for (int i = 0; i < 3; i++)
		{
			Triple.vertices[i] = new DGEVertex(copy.Triple.vertices[i]);
			Vertices[i] = new DGEVector(copy.Vertices[i]);
		}
	}
	
	public DGEVector GetSize()
	{
		float minx, maxx;
		float miny, maxy;
		
		minx = maxx = Vertices[0].x;
		miny = maxy = Vertices[0].y;
		
		for (int i = 1; i < 3; i++)
		{
			if (Vertices[i].x < minx)
				minx = Vertices[i].x; 

			if (Vertices[i].x > minx)
				maxx = Vertices[i].x; 

			if (Vertices[i].y < miny)
				miny = Vertices[i].y; 

			if (Vertices[i].y > maxy)
				maxy = Vertices[i].y; 
		}
		
		return new DGEVector(maxx - minx, maxy - miny);
	}
	
	public boolean Contains(DGEVector point)
	{
		DGEVector size = GetSize();
		
		if (Intercepts(point, point.add(new DGEVector(-Math.abs(size.x), 0.1f))).Collides &&
			Intercepts(point, point.add(new DGEVector(0.1f, -Math.abs(size.y)))).Collides &&
			Intercepts(point, point.add(new DGEVector(Math.abs(size.x), 0.1f))).Collides &&
			Intercepts(point, point.add(new DGEVector(0.1f, Math.abs(size.x)))).Collides)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public DGEIntersect Intercepts(DGEVector p1, DGEVector p2)
	{
		return Intercepts(new DGELine(p1, p2));
	}

	public DGEIntersect Intercepts(DGELine line)
	{
		DGEIntersect intersect = line.Intersects(new DGELine(Vertices[0], Vertices[1]));
		
		if (!intersect.Collides)
			intersect = line.Intersects(new DGELine(Vertices[1], Vertices[2]));
			
		if ( !intersect.Collides)
			intersect = line.Intersects(new DGELine(Vertices[2], Vertices[0]));
		
		return intersect;
	}
}
