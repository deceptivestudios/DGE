package com.deceptivestudios.tests;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEMultiple;

public class Test_1 extends Test
{
	private int _sound;
	private int _texture;
	private DGEMultiple _quad;
	
	private float _x, _y;
	private float _dx, _dy;
	private float _friction = 0.99f;

	@Override
	public boolean Create() 
	{
		_texture = _dge.Texture_Load("particles.png");
		
		if (_texture == 0)
			return false;
		
		_sound = _dge.Effect_Load("bling.wav");
		
		_x = Width / 2;
		_y = Height / 2;
		
		_quad = new DGEMultiple(4);
		
		_quad.vertices[0].tx = 96.0f / 128.0f;  _quad.vertices[0].ty = 64.0f / 128.0f; 
		_quad.vertices[1].tx = 128.0f / 128.0f; _quad.vertices[1].ty = 64.0f / 128.0f; 
		_quad.vertices[2].tx = 128.0f / 128.0f; _quad.vertices[2].ty = 96.0f / 128.0f; 
		_quad.vertices[3].tx = 96.0f / 128.0f;  _quad.vertices[3].ty = 96.0f / 128.0f; 
		
		_quad.texture = _texture;
		
		_quad.blend = DGE.DGE_BLEND_ALPHAADD | DGE.DGE_BLEND_COLORMUL | DGE.DGE_BLEND_ZWRITE;
		
		_ready = true;

		return true;
	}
	
	@Override
	public boolean Render() 
	{
		_dge.Gfx_Clear(0f, 0f, 0f);
		_dge.Gfx_RenderQuad(_quad);
		
		return true;
	}

	@Override
	public boolean Update(float delta) 
	{
		if (_dge.Input_GetTap())
		{
			float[] position = _dge.Input_GetPosition();
			
			_x = position[0];
			_y = position[1];
		}
		
		if (_dge.Input_GetDoubleTap())
		{
			_dge.System_Snapshot("test_1.png");
		}
		
		float[] tilt = _dge.Input_GetTilt();
		
		if (tilt != null) 
		{
			_dx += Math.toDegrees(tilt[1]) * delta;
			_dy += Math.toDegrees(tilt[2]) * delta;
		}
	
		_dx *= _friction; _x -= _dx;
		_dy *= _friction; _y -= _dy;
		
		int width = Width - 16;
		int height = Height - 16;
		
		if (_x > width) 
		{ 
			_x = (width - (_x - width)); 
			_dx = -_dx; 
			boom(); 
		}
		else if (_x < 16)
		{ 
			_x = (16 + 16 - _x); 
			_dx = -_dx; 
			boom(); 
		}
		
		if (_y > height) 
		{ 
			_y = (height - (_y - height)); 
			_dy = -_dy; 
			boom(); 
		}
		else if (_y < 16) 
		{ 
			_y = (16 + 16 - _y); 
			_dy = -_dy; 
			boom(); 
		}
		
		_quad.vertices[0].x = _x - 16; _quad.vertices[0].y = _y - 16;
		_quad.vertices[1].x = _x + 16; _quad.vertices[1].y = _y - 16;
		_quad.vertices[2].x = _x + 16; _quad.vertices[2].y = _y + 16;
		_quad.vertices[3].x = _x - 16; _quad.vertices[3].y = _y + 16;
		
		return true;
	}
	
	private void boom() 
	{
		int pan = (int) ((_x - (Width / 2f)) / 4);
		float pitch = (_dx * _dx + _dy * _dy) * 0.0005f + 0.2f;
		
		_dge.Effect_PlayEx(_sound, 100, pan, pitch);
	}
}
