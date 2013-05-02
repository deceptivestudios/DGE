package com.deceptivestudios.tests;


import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGESprite;
import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.physics.box2d.collision.shapes.ShapeType;
import com.deceptivestudios.engine.physics.box2d.dynamics.Body;

public class Test_7 extends Test 
{
	private int _texture;
	private DGESprite _circle, _square;
	private Body[] _circles, _squares;
	private float _gravity = -9.8f;
	private float _x, _y;
	private boolean _physics = false;
	
	@Override
	public boolean Create() 
	{
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
		_circles = new Body[50];
		_squares = new Body[50];
		
		int circles = 0, squares = 0;
		
		for (int i = 0; i < 5; i++)
		{
			for (int j = 0; j < 20; j++)
			{
				if (j % 2 == 0)
				{
					_circles[circles] = _dge.Physics_CreateDynamicBody(ShapeType.CIRCLE, 150 + (j * 32) - (i * 16), (i * 32) + 64, 32, 32, 1, 0.3f);
					circles++;
				}
				else
				{
					_squares[squares] = _dge.Physics_CreateDynamicBody(ShapeType.POLYGON, 150 + (j * 32) - (i * 16), (i * 32) + 64, 32, 32, 1, 0.3f);
					squares++;
				}
			}
		}
		
		_dge.Physics_CreateFixedBody(ShapeType.POLYGON, -16,   -32,    Width + 32, 32);
		_dge.Physics_CreateFixedBody(ShapeType.POLYGON, -16,   Height, Width + 32, 32);
		
		_dge.Physics_CreateFixedBody(ShapeType.POLYGON, -32,   -16,    32,         Height + 32);
		_dge.Physics_CreateFixedBody(ShapeType.POLYGON, Width, -16,    32,         Height + 32);
		
		_ready = true;
		
		return true;
	}

	@Override
	public boolean Render() 
	{
		_dge.Gfx_Clear(0f, 0f, 0f);
		
		if (_physics)
		{
			_dge.Physics_Render(DGE.DGE_PHYSICS_ALL);
		}
		else
		{
			for (int i = 0; i < 50; i++)
			{
				if (_circles[i] != null)
				{
					DGEVector vec = _dge.Physics_GetBodyPosition(_circles[i]);
					_circle.Render(vec.x, vec.y);
				}
			}

			for (int i = 0; i < 50; i++)
			{
				if (_squares[i] != null)
				{
					Body body = _squares[i];
					DGEVector vec = _dge.Physics_GetBodyPosition(_squares[i]);
					
					_square.RenderEx(vec.x, vec.y, body.getAngle());
				}
			}
		}
		
		return true;
	}

	@Override
	public boolean Update(float delta) 
	{
		_dge.Physics_Update(delta);

		if (_dge.Input_GetDoubleTap())
			_physics = !_physics;
		
		float[] tilt = _dge.Input_GetTilt();
		
		if (tilt != null)
		{
			_x = _gravity * tilt[1];
			_y = _gravity * tilt[2];
			
			_dge.Physics_SetGravity(_x, _y);
		}
		
		return true;
	}

}
