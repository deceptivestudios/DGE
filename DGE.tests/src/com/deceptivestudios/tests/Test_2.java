package com.deceptivestudios.tests;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGESprite;
import com.deceptivestudios.engine.particle.DGEParticleSystem;

public class Test_2 extends Test
{
	private int _sound;
	private int _texture;
	private DGEParticleSystem _particle;
	private DGESprite _sprite, _particleSprite;
	private float _x, _y;
	private float _dx, _dy;
	private float _friction = 0.98f;

	@Override
	public boolean Create() 
	{
		_texture = _dge.Texture_Load("particles.png");
		
		if (_texture == 0)
			return true;

		_sound = _dge.Effect_Load("bling.wav");
		
		_x = Width / 2;
		_y = Height / 2;
		
		_sprite = new DGESprite(_texture, 96, 64, 32, 32);
		_sprite.SetColor(1, 0.65f, 0, 1);
		_sprite.SetHotSpot(16,16);
		
		_particleSprite = new DGESprite(_texture, 32, 32, 32, 32);
		_particleSprite.SetBlendMode(DGE.DGE_BLEND_COLORMUL | DGE.DGE_BLEND_ALPHAADD | DGE.DGE_BLEND_NOZWRITE);
		_particleSprite.SetHotSpot(16, 16);
		
		_particle = new DGEParticleSystem("trail.psi", _particleSprite);
		_particle.FireAt(_x, _y);
		
		_ready = true;
		
		return true;
	}
	
	@Override
	public boolean Render()
	{
		_dge.Gfx_Clear(0f, 0f, 0f);
		
		_particle.Render();
		_sprite.Render(_x, _y);
		
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

		_particle.Info.Emission = (int) (Math.abs(_dx * 20f) + Math.abs(_dy * 20f)) * 2;
		_particle.MoveTo(_x, _y);
		_particle.Update(delta);
		
		return true;
	}
	
	private void boom() 
	{
		int pan = (int) ((_x - (Width / 2f)) / 4);
		float pitch = (_dx * _dx + _dy * _dy) * 0.0005f + 0.2f;
		
		_dge.Effect_PlayEx(_sound, 100, pan, pitch);
	}
}
