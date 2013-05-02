package com.deceptivestudios.tests;


import com.deceptivestudios.engine.helper.DGERect;
import com.deceptivestudios.engine.helper.DGESprite;
import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.physics.box2d.collision.shapes.ShapeType;
import com.deceptivestudios.engine.physics.box2d.dynamics.Body;

public class Test_8 extends Test
{
	private DGERect _range;
	private DGEVector _position;
	private float _x, _y, _gx, _gy;
	private boolean _down;
	private int _texture;
	private DGESprite _circle, _square;
	private Body[] _bodies;
	private float _gravity = -9.8f;
	private float _follow = 3f;
	
	@Override
	public boolean Create() 
	{
		_range = new DGERect(0, Height * -2, Width * 3f, 0);
		_texture = _dge.Texture_Load("particles.png");
		
		if (_texture == 0)
			return false;
		
		_circle = new DGESprite(_texture, 96, 64, 32, 32);
		_circle.SetColor(1, 0.65f, 0, 1);
		_circle.SetHotSpot(16, 16);
		
		_square = new DGESprite(0, 0, 0, 32, 32);
		_square.SetColor(0, 0, 0.65f, 1);
		_square.SetHotSpot(16, 16);
		
		_dge.Physics_Initialize(0, 9.8f, 32);
		_bodies = new Body[100];
		
		int item = 0;
		
		for (int i = 0; i < 4; i++)
		{
			for (int j = 0; j < 20 + i; j++)
			{
				if (_dge.Random_Int(0, 1) == 0)
					_bodies[item] = _dge.Physics_CreateDynamicBody(ShapeType.CIRCLE, 150 + (j * 32) - (i * 16), (i * 32) + 64, 32, 32, 1, 0.3f);
				else
					_bodies[item] = _dge.Physics_CreateDynamicBody(ShapeType.POLYGON, 150 + (j * 32) - (i * 16), (i * 32) + 64, 32, 32, 1, 0.3f);
				item++;
			}
		}
		
		_dge.Physics_CreateFixedBody(ShapeType.POLYGON, -16,        Height,           Width * 4f + 32, 32);
		_dge.Physics_CreateFixedBody(ShapeType.POLYGON, -16,        Height * -2 - 32, Width * 4f + 32, 32);
		_dge.Physics_CreateFixedBody(ShapeType.POLYGON, -32,        Height * -2 - 16, 32,              Height * 3f + 32);
		_dge.Physics_CreateFixedBody(ShapeType.POLYGON, Width * 4f, Height * -2 - 16, 32,              Height * 3f + 32);
		
		_position = new DGEVector(0, 0);
		_down = false;
		
		_ready = true;
		
		return true;
	}

	@Override
	public boolean Render() 
	{
		_dge.Gfx_Clear(0f, 0f, 0f);
		
		for (int i = 0; i < 100; i++)
		{
			if (_bodies[i] != null)
			{
				Body body = _bodies[i];
				DGEVector vec = _dge.Physics_GetBodyPosition(_bodies[i]).subtract(_position);
				
				if (body.m_fixtureList.m_shape.getType() == ShapeType.CIRCLE)
					_circle.Render(vec.x, vec.y);
				else
					_square.RenderEx(vec.x, vec.y, body.getAngle());
			}
		}
		
		//_dge.Physics_Render(DGE.DGE_PHYSICS_STATIC, _position);

		return true;
	}

	@Override
	public boolean Update(float delta) 
	{
		if (_dge.Input_Released())
		{
			_down = false;
			_follow = 3f;
		}
	
		DGEVector newPosition = new DGEVector(_position);
		
		if (_dge.Input_Touched())
		{
			_down = true;
			float[] position = _dge.Input_GetTouch();
			
			if (position != null)
			{
				_x = position[0];
				_y = position[1];
			}
		}
		else if (_down)
		{
			float[] position = _dge.Input_GetTouch();
			
			if (position != null)
			{
				float x = position[0];
				float y = position[1];
				
				if (x != _x)
					newPosition.x += (_x - x);
				
				if (y != _y)
					newPosition.y += (_y - y);
				
				_x = x; _y = y;
			}
		}
		else
		{
			_follow -= delta;
			
			if (_follow <= 0f)
			{
				DGEVector position = _dge.Physics_GetBodyPosition(_bodies[50]).subtract(new DGEVector(Width / 2, Height / 2)); 
				newPosition.lerp(position, 0.8f);
			}
		}
		
		if (newPosition.x < _range.p1.x)
			newPosition.x = _range.p1.x;
		
		if (newPosition.y < _range.p1.y)
			newPosition.y = _range.p1.y;
		
		if (newPosition.x > _range.p2.x)
			newPosition.x = _range.p2.x;
		
		if (newPosition.y > _range.p2.y)
			newPosition.y = _range.p2.y;
		
		_position = new DGEVector(newPosition);
		
		_dge.Physics_Update(delta);
		
		float[] tilt = _dge.Input_GetTilt();
		
		if (tilt != null)
		{
			_gx = _gravity * tilt[1];
			_gy = _gravity * tilt[2];
			
			_dge.Physics_SetGravity(_gx, _gy);
		}
	
		return true;
	}
}
