package com.deceptivestudios.engine.gui;

import com.deceptivestudios.engine.helper.DGERect;
import com.deceptivestudios.engine.helper.DGESprite;

public class DGEGUIButton extends DGEGUIObject 
{
	private boolean _trigger;
	private boolean _pressed;
	private boolean _oldState;
	private DGESprite _up, _down;
	
	public DGEGUIButton(int id, float x, float y, float w, float h, int texture, float tx, float ty)
	{
		this.id = id;
		this.stationary = false;
		this.visible = true;
		this.enabled = true;
		
		this._x = x; this._y = y;
		this._rect = new DGERect(x, y, x + w, y + h);
		
		_pressed = false;
		_trigger = false;
		
		_up = new DGESprite(texture, tx, ty, w, h);
		_down = new DGESprite(texture, tx + w, ty, w, h);
	}

	public void SetMode(boolean trigger)
	{
		_trigger = trigger;
	}
	
	public void SetState(boolean pressed)
	{
		_pressed = pressed;
	}
	
	@Override
	public void Render() 
	{
		if (_pressed)
			_down.Render(_rect.p1.x, _rect.p1.y);
		else
			_up.Render(_rect.p1.x, _rect.p1.y);
	}

	@Override
	public boolean Touched(boolean down) 
	{
		if (down)
		{
			_oldState = _pressed;
			_pressed = true;
			
			return false;
		}
		else
		{
			if (_trigger)
				_pressed = !_oldState;
			else
				_pressed = false;
			
			return true;
		}
	}
	
	@Override
	public void Update(float dt) { }
	@Override
	public void Enter() { }
	@Override
	public void Leave() { }
	@Override
	public void Reset() { }
	@Override
	public boolean IsDone() { return true; }
	@Override
	public void Focus(boolean focused) { }
	@Override
	public boolean Touch(float x, float y) { return false; }
}
