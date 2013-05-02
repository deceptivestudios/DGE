package com.deceptivestudios.engine.helper;

public class DGEVertex
{
	public float x, y, z;       // screen position    
	public float r, g, b, a; // colour
	public float tx, ty;     // texture coordinates
	
	public DGEVertex()
	{
		x = 0f; 
		y = 0f;
		z = 0.5f;
		
		r = 1f;
		g = 1f;
		b = 1f;
		a = 1f;
		
		tx = 0f;
		ty = 0f;
	}
	
	public DGEVertex(DGEVertex copy)
	{
		x = copy.x;
		y = copy.y;
		z = copy.z;
		
		r = copy.r;
		g = copy.g;
		b = copy.b;
		a = copy.a;
		
		tx = copy.tx;
		ty = copy.ty;
	}
}
