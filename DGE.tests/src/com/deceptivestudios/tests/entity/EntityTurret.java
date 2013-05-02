package com.deceptivestudios.tests.entity;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.entity.DGEEntityEx;
import com.deceptivestudios.engine.helper.DGESprite;
import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.polygon.DGEPolygonMapLayer;

public class EntityTurret extends DGEEntityEx
{
	protected DGESprite _body, _gun;
	protected EntityPlayer _player;
	
	protected float _timer;
	protected float _angle;
	protected float _shootRate;
	protected float _bulletSpeed;
	
	public EntityTurret()
	{
		super(DGEPolygonMapLayer.Back, false);
	}
	
	public EntityTurret Spawn()
	{
		return new EntityTurret();
	}
	
	public void Initialise()
	{
		_body = new DGESprite(_dge.Texture_Load("data/ents/turret.png"), 0, 0, 64, 64);
		
		_gun = new DGESprite(_dge.Texture_Load("data/ents/gun.png"), 0, 0, 64, 64);
		_gun.SetHotSpot(31, 42);
		
		_player = (EntityPlayer) _map.GetDynamicEntityFromProperty("name", "player");
		
		_timer = 0;
		
		_shootRate = EntityData.GetFloat("shoot_rate");
		
		if (_shootRate == 0)
			_shootRate = 1.5f;
		
		_bulletSpeed = EntityData.GetFloat("bullet_speed");
		
		if (_bulletSpeed == 0)
			_bulletSpeed = 100;
	}
	
	public void Update(float delta)
	{
		if (_player == null)
			return;
		
		_timer -= delta;
		
		DGEVector v = _player.GetPosition().subtract(_center);
		
		if (v.length() < 750)
		{
			_angle = v.angle() + DGE.M_PI_2;
			
			if (_timer <= 0)
			{
				v.normalize();
				_map.AddDynamicEntity(new EntityBullet(_center.add(v.multiply(35f)), v.multiply(_bulletSpeed)));
				
				_timer = _shootRate;
			}
		}
	}
	
	public void Render()
	{
		_gun.RenderEx(_aabb.p1.x + 31, _aabb.p1.y + 42, _angle);
		_body.Render(_aabb.p1.x, _aabb.p1.y);
	}
}
