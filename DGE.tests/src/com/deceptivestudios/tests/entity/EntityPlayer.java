package com.deceptivestudios.tests.entity;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.entity.DGEEntityEx;
import com.deceptivestudios.engine.geometry.DGEIntersect;
import com.deceptivestudios.engine.geometry.DGEPolygon;
import com.deceptivestudios.engine.helper.DGESprite;
import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.polygon.DGEPolygonCollision;
import com.deceptivestudios.engine.polygon.DGEPolygonLayer;
import com.deceptivestudios.engine.polygon.DGEPolygonMapLayer;
import com.deceptivestudios.tests.Test;

public class EntityPlayer extends DGEEntityEx 
{
	protected int _ship, _flame;
	protected DGESprite _flameSprite;
	
	protected DGEVector _position;
	protected DGEVector _spawnPosition;
	
	protected DGEVector[] _flamePosition;
	
	protected DGEVector _velocity;
	protected float _angle, _zoom, _scaledZoom;
	
	public EntityPlayer()
	{
		super(DGEPolygonLayer.Middle2, false);
	}
	
	public EntityPlayer Spawn()
	{
		return new EntityPlayer();
	}
	
	public void Initialise()
	{
		_position = new DGEVector(_aabb.p1);
		_spawnPosition = new DGEVector(_position);

		_velocity = new DGEVector();
		
		_angle = 0f;
		_zoom = 1.5f;
		_scaledZoom = Test.Height / 512f;
		
		SetHotSpot(new DGEVector(35, 35));
		
		_ship = _dge.Texture_Load("data/ents/ship.png");
		
		_flame = _dge.Texture_Load("data/ents/flame.png");
		_flameSprite = new DGESprite(_flame, 0, 0, 32, 32);
		
		_flamePosition = new DGEVector[3];
		_flamePosition[0] = new DGEVector(_position.x, _position.y + 40);
		_flamePosition[1] = new DGEVector(_position.x - 20, _position.y + 44);
		_flamePosition[2] = new DGEVector(_position.x + 20, _position.y + 44);
		
		SetTexture(_ship);
	}
	
	public void Die()
	{
		_position = new DGEVector(_spawnPosition);
		_velocity = new DGEVector();
		_angle = 0f;
		_zoom = 1.0f;
		
		SetPosition(_position);
		SetRotation(0f);
		
		SetTexture(_ship);
		
		_map.RemoveDynamicEntities("Bullet");
	}
	
	public void Free()
	{
		_dge.Texture_Free(_ship);
	}
	
	public DGEVector GetPosition() { return _center; }
	public float GetVelocity() { return _velocity.length(); }
	public float GetZoom() { return _zoom * _scaledZoom; }
	
	public void Update(float delta)
	{
		boolean landing = true;
		
		_velocity.addLocal(new DGEVector(0, 150f * delta));

		float[] direction = _dge.Input_GetTilt();
		
		if (direction != null)
			_angle = direction[1] + ((_angle - direction[1]) * 0.9f);
		
		if (_dge.Input_GetDown())
		{
			landing = false;
			_velocity.addLocal(new DGEVector((float) Math.cos(GetRotation() + DGE.M_PI_2 + DGE.M_PI), (float) Math.sin(GetRotation() + DGE.M_PI_2 + DGE.M_PI)).multiply(250f * delta));
		}
		
		_flamePosition[0].set(_position.x, _position.y + 40);
		_flamePosition[0].rotate(_angle);
		_flamePosition[1].set(_position.x - 20, _position.y + 44);
		_flamePosition[1].rotate(_angle);
		_flamePosition[2].set(_position.x + 20, _position.y + 44);
		_flamePosition[2].rotate(_angle);
		
		DGEPolygon polygon = new DGEPolygon();
		DGEIntersect test = _map.TestEx(this, _velocity.multiply(delta), _angle * delta, polygon, DGEPolygonCollision.Static | DGEPolygonCollision.Multiple, "", DGEPolygonMapLayer.Middle);
		
		float velocity = _velocity.length();
		
		if (test.Collides)
		{
			if (polygon.EntityData.Classname.equalsIgnoreCase("StartPlatform"))
			{
				if (landing)
				{
					_velocity.set(0, 0);
					_angle = 0;
				}
				else
				{
					test.SetResponseVector(_velocity);
				}
			}
			else if (polygon.EntityData.Classname.equalsIgnoreCase("EndPlatform"))
			{
				if (landing)
				{
					_velocity.set(0, 0);
					_angle = 0;
				}
				else
				{
					test.SetResponseVector(_velocity);
				}
			}
			else if (velocity <= 150)
			{
				test.SetResponseVector(_velocity);
			}
			else
			{
				Die();
			}
		}
		
		_position.addLocal(_velocity.multiply(delta));
		
		SetPosition(_position);
		SetRotation(-_angle);
		
		test = _map.Test(this, _velocity.multiply(delta), null, DGEPolygonCollision.Dynamic | DGEPolygonCollision.Visible, "Bullet");
		
		if (test.Collides)
			Die();
		
		if (velocity > 300f)
			velocity = 300f;
		
		float zoom = 1.5f - velocity / 600f;
		_zoom = zoom + ((_zoom - zoom) * 0.9f); 
		
		_map.SetTransform(_position, _zoom * _scaledZoom);
	}
	
	public void Render()
	{
		//for (int i = 0; i < 3; i++)
		//	_flameSprite.RenderEx(_flamePosition[i].x, _flamePosition[i].y, -_angle);
		
		super.Render();
	}
}
