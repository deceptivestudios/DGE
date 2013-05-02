package com.deceptivestudios.engine.helper;


public class DGEMultiple
{
	public DGEVertex vertices[];
	public int texture;
	public int blend;
	
	public DGEMultiple(int total)
	{
		vertices = new DGEVertex[total];
		
		for (int i = 0; i < total; i++)
		{
			vertices[i] = new DGEVertex();
		}
	}
	
	public DGEMultiple(DGEMultiple copy)
	{
		vertices = new DGEVertex[copy.vertices.length];
		
		for (int i = 0; i < copy.vertices.length; i++)
		{
			vertices[i] = new DGEVertex(copy.vertices[i]); 
		}
	}
	
	public void Move(float x, float y)
	{
		for (int i = 0; i < vertices.length; i++)
		{
			vertices[i].x += x;
			vertices[i].y += y;
		}
	}
}
