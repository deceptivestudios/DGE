package com.deceptivestudios.tests.entity;

import com.deceptivestudios.engine.entity.DGEEntity;
import com.deceptivestudios.engine.helper.DGESprite;
import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.polygon.DGEPolygonLayer;

public class EntityBullet extends DGEEntity 
{
	private DGEVector _position;
	private DGEVector _velocity;

	public EntityBullet(DGEVector position, DGEVector velocity)
	{
		super(DGEPolygonLayer.Front, false);
		
		_position = position;
		_velocity = velocity;
		
		EntityData.Classname = "Bullet";
	}
	
	public EntityBullet Spawn()
	{
		return new EntityBullet(new DGEVector(0, 0), new DGEVector(0, 0));
	}
	
	public void Initialise()
	{
		_sprite = new DGESprite(_dge.Texture_Load("data/ents/bullet.png"), 0, 0, 8, 8);
		SetAABB(_position, new DGEVector(8, 8));
	}

	public void Update(float delta)
	{
		DGEVector position = _position.add(_velocity.multiply(delta));
		
		_position.set(position);
		SetPosition(_position);
		
		if (_map.TestPoint(_position) != null || !_map.TestVisibility(this, 500))
			Delete();
	}
}
