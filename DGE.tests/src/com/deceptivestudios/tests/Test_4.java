package com.deceptivestudios.tests;

import android.util.FloatMath;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.gui.DGEGUIMenuItem;
import com.deceptivestudios.engine.helper.DGEGUI;
import com.deceptivestudios.engine.helper.DGEQuad;
import com.deceptivestudios.engine.helper.DGESprite;

public class Test_4 extends Test
{
	private DGEGUI _gui;
	private DGESprite _sprite;
	
	private int _sound;
	private int _texture;
	private DGEQuad _quad;
	
	@Override
	public boolean Create() 
	{
		_quad = new DGEQuad(Width, Height);
		
		_quad.texture = _dge.Texture_Load("bg.png");
		_quad.blend = DGE.DGE_BLEND_DEFAULT;
		
		_texture = _dge.Texture_Load("cursor.png");
		_sprite = new DGESprite(_texture, 0, 0, 32, 32);
		
		_sound = _dge.Effect_Load("menu.wav");
		
		_gui = new DGEGUI();
		
		_gui.AddControl(new DGEGUIMenuItem(1, Width / 2, Height / 2 - 80, Font, "Play", _sound, 0.0f));
		_gui.AddControl(new DGEGUIMenuItem(2, Width / 2, Height / 2 - 40, Font, "Options", _sound, 0.1f));
		_gui.AddControl(new DGEGUIMenuItem(3, Width / 2, Height / 2, Font, "Instructions", _sound, 0.2f));
		_gui.AddControl(new DGEGUIMenuItem(4, Width / 2, Height / 2 + 40, Font, "Credits", _sound, 0.3f));
		_gui.AddControl(new DGEGUIMenuItem(5, Width / 2, Height / 2 + 80, Font, "Exit", _sound, 0.4f));
		
		_gui.SetCursor(_sprite);
		_gui.SetFocus(1);
		
		_gui.Enter();
		
		_ready = true;
		
		return true;
	}

	@Override
	public boolean Render() 
	{
		_dge.Gfx_RenderQuad(_quad);
		_gui.Render();
		
		return true;
	}

	private float _time = 0f;
	private int _lastId = 1;
	
	@Override
	public boolean Update(float delta) 
	{
		_time += delta;
		
		float tx = 50f * FloatMath.cos(_time / 60f);
		float ty = 50f * FloatMath.sin(_time / 60f);

		_quad.vertices[0].tx = tx;               _quad.vertices[0].ty = ty;
		_quad.vertices[1].tx = tx + Width / 64f; _quad.vertices[1].ty = ty;
		_quad.vertices[2].tx = tx + Width / 64f; _quad.vertices[2].ty = ty + Height / 64f;
		_quad.vertices[3].tx = tx;               _quad.vertices[3].ty = ty + Height / 64f;
		
		int id = _gui.Update(delta);
		
		if (id != _lastId)
		{
			_lastId = id;
			_gui.Enter();
			
			if (_lastId == 5)
				return false;
		}
		
		return true;
	}

}
