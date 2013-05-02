package com.deceptivestudios.tests;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGESprite;
import com.deceptivestudios.engine.state.DGEState;
import com.deceptivestudios.engine.state.DGEStateManager;

public class StartupState extends DGEState
{
	private int _texture;
	private DGESprite _logo;
	private float _width, _height;
	private float _time;
	private int _state;
	
	public static final int FadeIn = 0;
	public static final int Display = 1;
	public static final int FadeOut = 2;
	public static final int Done = 3;
	
	public static float Fade = 0.5f;
	public static float Show = 3f;
	
	@Override
	public boolean Initialize(String data) 
	{
		_texture = _dge.Texture_Load("deceptive-logo.png");
		
		_logo = new DGESprite(_texture, 0, 0, 256, 256);
		_logo.SetHotSpot(128, 128);
		_logo.SetColor(1, 1, 1, 0);

		_width = (Integer) _dge.System_GetState(DGE.DGE_SCREENWIDTH);
		_height = (Integer) _dge.System_GetState(DGE.DGE_SCREENHEIGHT);
		
		SetState(FadeIn);

		return true;
	}

	@Override
	public boolean Render() 
	{
		_dge.Gfx_BeginScene();
		_dge.Gfx_Clear(0, 0, 0, 1);
		
		_logo.Render(_width / 2, _height / 2);

		_dge.Gfx_EndScene();

		return true;
	}

	@Override
	public boolean Update(float delta)
	{
		float a = 0;
		
		_time += delta;
		
		switch (_state)
		{
			case FadeIn:
			{
				if (_time < Fade)
					a = _time / Fade;
				else
					SetState(Display);
			} break;
			
			case Display:
			{
				if (_time >= Show || _dge.Input_GetTap())
					SetState(FadeOut);
				else
					a = 1;
			} break;
			
			case FadeOut:
			{
				if (_time < Fade)
					a = 1f - (_time / Fade);
				else
					SetState(Done);
			} break;
			
			case Done:
			{
				DGEStateManager.GetManager().SetState("Menu");
				return true;
			}
		}
		
		_logo.SetColor(1, 1, 1, a);
		
		return true;
	}
	
	private void SetState(int state)
	{
		_time = 0f;
		_state = state;
	}
}
