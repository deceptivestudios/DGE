package com.deceptivestudios.engine.geometry;

import java.util.Vector;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEVector;

public class DGEIntersect 
{
	public boolean Collides;
	public Vector<DGEVector> Points;
	public Vector<Float> Normals;
	
	public DGEIntersect()
	{
		Collides = false;
		
		Points = new Vector<DGEVector>();
		Normals = new Vector<Float>();
	}
	
	public DGEVector GetSurfaceVector()
	{
		return GetSurfaceVector(0);
	}
	
	public DGEVector GetSurfaceVector(int i)
	{
		if (i >= Normals.size())
			return new DGEVector();
		
		DGEVector surface = new DGEVector(0, 1);
		surface.rotate(Normals.get(i));
		
		return surface;
	}
	
	public void SetResponseVector(DGEVector velocity)
	{
		SetResponseVector(velocity, 1f);
	}
	
	public void SetResponseVector(DGEVector velocity, float bounce)
	{
		DGEVector endVelocity = new DGEVector();
		boolean[] invalidNormals = new boolean[Normals.size()];
		
		for (int i = 0; i < Normals.size(); i++)
		{
			invalidNormals[i] = false;
			DGEVector surface = GetSurfaceVector(i);
			
			if (surface.angle(velocity) < DGE.M_PI_2)
				invalidNormals[i] = true;
		}
		
		Vector<Float> tempNormals = new Vector<Float>();
		tempNormals.addAll(Normals);
		
		Normals.clear();
		
		for (int i = 0; i < tempNormals.size(); i++)
		{
			if (!invalidNormals[i])
				Normals.add(tempNormals.get(i));
		}
		
		if (Normals.size() > 1)
		{
			for (int i = 0; i < Normals.size(); i++)
				endVelocity = endVelocity.add(GetSurfaceVector(i));
			
			endVelocity.normalize();
			
			DGEVector surface = new DGEVector(endVelocity);
			DGEVector Vn = surface.multiply(surface.dot(velocity));
			DGEVector Vt = velocity.subtract(Vn);
			
			endVelocity = Vt.subtract(Vn.multiply(bounce));
		}
		else if (Normals.size() > 0)
		{
			DGEVector surface = GetSurfaceVector(0);
			DGEVector Vn = surface.multiply(surface.dot(velocity));
			DGEVector Vt = velocity.subtract(Vn);
			
			endVelocity = Vt.subtract(Vn.multiply(bounce));
		}
		else
		{
			endVelocity = new DGEVector(velocity);
		}
		
		velocity.set(endVelocity);
	}
}
