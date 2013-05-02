package com.deceptivestudios.engine.entity;

import com.deceptivestudios.engine.geometry.DGETriangle;
import com.deceptivestudios.engine.helper.DGEQuad;
import com.deceptivestudios.engine.helper.DGETriple;
import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.polygon.DGEPolygonVertex;

public abstract class DGEEntityEx extends DGEEntity
{
	private float _rotation;
	private float _scale;
	
	public DGEEntityEx(int layer, boolean stationary)
	{
		super(layer, stationary);
		
		_rotation = 0;
		_scale = 1f;
	}
	
	private void ApplyTransform(float angle_dt)
	{
		_rotation += angle_dt;
		
		for (int i = 0; i < Vertices.size(); i++)
		{
			DGEPolygonVertex v = Vertices.get(i);
			DGEVector m = Vertices.get(i).Position.subtract(_hotspot);
			m.rotate(angle_dt);
			
			v.Position = m.add(_hotspot);
		}
		
		for (int i = 0; i < Triangles.size(); i++)
		{
			DGETriangle t = Triangles.get(i);
			
			for (int j = 0; j < 3; j++)
			{
				DGEVector p = new DGEVector(t.Triple.vertices[j].x, t.Triple.vertices[j].y);
				DGEVector m = p.subtract(_hotspot);
				m.rotate(angle_dt);
				
				t.Triple.vertices[j].x = _hotspot.x + m.x;
				t.Triple.vertices[j].y = _hotspot.y + m.y;
			}
		}
		
		for (int i = 0; i < _borderQuads.size(); i++)
		{
			DGEQuad q = _borderQuads.get(i);
			
			for (int j = 0; j < 4; j++)
			{
				DGEVector p = new DGEVector(q.vertices[j].x, q.vertices[j].y);
				DGEVector m = p.subtract(_hotspot);
				m.rotate(angle_dt);
				
				q.vertices[j].x = _hotspot.x + m.x;
				q.vertices[j].y = _hotspot.y + m.y;
			}
		}
		
		for (int i = 0; i < _borderTriangles.size(); i++)
		{
			DGETriple t = _borderTriangles.get(i);
			
			for (int j = 0; j < 3; j++)
			{
				DGEVector p = new DGEVector(t.vertices[j].x, t.vertices[j].y);
				DGEVector m = p.subtract(_hotspot);
				m.rotate(angle_dt);
				
				t.vertices[j].x = _hotspot.x + m.x;
				t.vertices[j].y = _hotspot.y + m.y;
			}
		}
		
		for (int i = 0; i < _shadowQuads.size(); i++)
		{
			DGEQuad q = _shadowQuads.get(i);
			
			for (int j = 0; j < 4; j++)
			{
				DGEVector p = new DGEVector(q.vertices[j].x, q.vertices[j].y);
				DGEVector m = p.subtract(_hotspot);
				m.rotate(angle_dt);
				
				q.vertices[j].x = _hotspot.x + m.x;
				q.vertices[j].y = _hotspot.y + m.y;
			}
		}
	}

	public DGEVector GetRotatedPosition(DGEVector pos, float r)
	{
		DGEVector m = pos.subtract(_hotspot);
		m.rotate(r);
		
		return _hotspot.add(m);
	}

    public void SetHotSpot(DGEVector hotspot)
    {
    	super.SetHotSpot(hotspot);

        _hotspot = new DGEVector(_aabb.p1.x, _aabb.p1.y);
        _hotspot.addLocal(hotspot);
    }

	public void Shift(DGEVector amount)
	{
		super.Shift(amount);
		_hotspot.addLocal(amount);
	}

	public DGEVector GetPosition() { return _hotspot; }
	
	public void SetPosition(DGEVector pos)
	{
		Shift(pos.subtract(_hotspot));
	}

	public void Rotate(float angle) { ApplyTransform(angle); }
	public void SetRotation(float angle) { ApplyTransform(angle - _rotation); }
	public float GetRotation() { return _rotation; }
	//public void SetScale(float scale) { _scale = scale; ApplyTransform(1); }
}
