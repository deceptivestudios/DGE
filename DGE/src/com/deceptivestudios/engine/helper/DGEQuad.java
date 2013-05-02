package com.deceptivestudios.engine.helper;

public class DGEQuad extends DGEMultiple 
{
	public DGEQuad()
	{
		super(4);
	}
	
	public DGEQuad(float width, float height)
	{
		super(4);
		SetSize(width, height);
	}
	
	public DGEQuad(DGEQuad copy)
	{
		super(copy);
	}
	
	public void SetSize(float width, float height)
	{
		float x = vertices[0].x;
		float y = vertices[0].y;
		
		vertices[0].x = x;         vertices[0].y = y; 
		vertices[1].x = x + width; vertices[1].y = y; 
		vertices[2].x = x + width; vertices[2].y = y + height; 
		vertices[3].x = x;         vertices[3].y = y + height;
	}
	
	public void SetPosition(float x, float y)
	{
		float w = vertices[2].x - vertices[0].x;
		float h = vertices[2].y - vertices[0].y;
		
		vertices[0].x = x;     vertices[0].y = y;
		vertices[1].x = x + w; vertices[1].y = y;
		vertices[2].x = x + w; vertices[2].y = y + h;
		vertices[3].x = x;     vertices[3].y = y + h;
	}
	
	public void SetTextureOffset(float t1, float t2)
	{
		vertices[0].tx = t1; vertices[0].ty = t1;
		vertices[1].tx = t2; vertices[1].ty = t1;
		vertices[2].tx = t2; vertices[2].ty = t2;
		vertices[3].tx = t1; vertices[3].ty = t2;
	}
	
	public void SetTextureOffset(float tx1, float ty1, float tx2, float ty2)
	{
		vertices[0].tx = tx1; vertices[0].ty = ty1;
		vertices[1].tx = tx2; vertices[1].ty = ty1;
		vertices[2].tx = tx2; vertices[2].ty = ty2;
		vertices[3].tx = tx1; vertices[3].ty = ty2;
	}
	
	public void SetColor(float r, float g, float b, float a)
	{
		for (int i = 0; i < 4; i++)
		{
			vertices[i].r = r;
			vertices[i].g = g;
			vertices[i].b = b;
			vertices[i].a = a;
		}
	}
}
