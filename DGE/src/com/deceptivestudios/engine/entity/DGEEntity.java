package com.deceptivestudios.engine.entity;

import com.deceptivestudios.engine.geometry.DGEPolygon;

public abstract class DGEEntity extends DGEPolygon
{
	private boolean _removeEntity;
    private boolean _isStatic;
    private int _layer;
	
	public DGEEntity(int layer, boolean stationary)
	{
		super();
		
		_isStatic = stationary;
		_layer = layer;
		
		_removeEntity = false;
		_isEntity = true;
	}

	public abstract DGEEntity Spawn();

	public boolean IsStatic() { return _isStatic; }
	public boolean IsDynamic(){ return !_isStatic; }

	public int GetRenderLayer() { return _layer; }

	public void Initialise() { return; }

	public void Update(float dt) { return; }

	public void Trigger() { return; }
	public void Reset() { return; }
	public void Free() { }

	public void Delete() { _removeEntity = true; }
	public boolean Deleted() { return _removeEntity; }
	
	public void Render()
	{
		super.Render();
	}
	
	public void Render(int flags)
	{
		super.Render(flags);
	}
}
