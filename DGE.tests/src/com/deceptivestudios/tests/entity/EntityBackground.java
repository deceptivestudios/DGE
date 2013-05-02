package com.deceptivestudios.tests.entity;

import com.deceptivestudios.engine.entity.DGEEntity;
import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.polygon.DGEPolygonMapLayer;

public class EntityBackground extends DGEEntity
{
	private DGEVector _lastTransform;
	private DGEVector _speed;
	private boolean _flipX, _flipY;
	
	public EntityBackground()
	{
		super(DGEPolygonMapLayer.Back, false);
	}
	
	public EntityBackground Spawn()
	{
		return new EntityBackground();
	}
	
	public void Initialise()
	{
		_lastTransform = new DGEVector();
		
		_speed = new DGEVector(EntityData.GetFloat("xspeed"), EntityData.GetFloat("yspeed"));
		
		_flipX = EntityData.GetInt("xflip") == 0 ? false : true;
		_flipY = EntityData.GetInt("yflip") == 0 ? false : true;
	}
	
	public void Update(float delta)
	{
		DGEVector transform = _map.GetTransform();
		
		if (_lastTransform.x != 0 && _lastTransform.y != 0)
		{
			transform.subtractLocal(_lastTransform);
			transform.addLocal(_speed);
			
			if (_flipX)
				transform = new DGEVector(-transform.x, transform.y);
			
			if (_flipY)
				transform = new DGEVector(transform.x, -transform.y);
			
			Shift(transform);
		}
		
		_lastTransform = _map.GetTransform();
	}
}
