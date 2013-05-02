package com.deceptivestudios.engine.gui;

import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.helper.DGEFont;
import com.deceptivestudios.engine.helper.DGERect;

public class DGEGUIMenuItem extends DGEGUIObject 
{
	private DGEFont _font;
	private int _sound;
	private float _delay;
	private String _title;
	private DGEColor _color, _shadow;
	private DGEColor _scolor, _dcolor, _scolor2, _dcolor2, _sshadow, _dshadow;
	private float _offset, _soffset, _doffset;
	private float _timerOn, _timerOff;
	private boolean _focus = false;
		
	public DGEGUIMenuItem(int id, float x, float y, DGEFont font, String title)
	{
		this(id, x, y, font, title, 0);
	}
	
	public DGEGUIMenuItem(int id, float x, float y, DGEFont font, String title, int sound)
	{
		this(id, x, y, font, title, sound, 0);
	}
	
	public DGEGUIMenuItem(int id, float x, float y, DGEFont font, String title, int sound, float delay)
	{
		this.id = id;
		this.stationary = false;
		this.visible = true;
		this.enabled = true;

		float w = font.GetStringWidth(title);
		float h = font.GetHeight();
		
		this._x = x; this._y = y;
		this._rect = new DGERect(x, y, x + w, y + h);
		
		_font = font;
		
		_sound = sound;
		_delay = delay;
		_title = title;
		
		_color = DGEColor.ParseARGB("FFFFE060");
		_shadow = DGEColor.ParseARGB("30000000");
		
		_scolor = DGEColor.ParseARGB("FFFFE060");
		_dcolor = DGEColor.ParseARGB("00000000");
		_scolor2 = DGEColor.ParseARGB("00000000");
		_dcolor2 = DGEColor.ParseARGB("00000000");
		_sshadow = DGEColor.ParseARGB("00000000");
		_dshadow = DGEColor.ParseARGB("00000000");
	}
	
	@Override
	public void Render() 
	{
		_font.SetColor(_shadow);
		_font.Render(_rect.p1.x + _offset + 3, _rect.p1.y + _offset + 3, DGEFont.Left, _title);
		_font.SetColor(_color);
		_font.Render(_rect.p1.x - _offset, _rect.p1.y - _offset, DGEFont.Left, _title);
	}

	@Override
	public void Update(float dt) 
	{
		if (_timerOff != -1.0f)
		{
			_timerOff += dt;
			
			if(_timerOff >= _delay + 0.1f)
			{
				_color = _scolor2.add(_dcolor2);
				_shadow = _sshadow.add(_dshadow);
				
				_offset = 0.0f;
				_timerOff = -1.0f;
			}
			else
			{
				if (_timerOff < _delay) 
				{ 
					_color = _scolor2; 
					_shadow = _sshadow; 
				}
				else 
				{ 
					_color = _scolor2.add(_dcolor2.mul(_timerOff - _delay).mul(10)); 
					_shadow = _sshadow.add(_dshadow.mul(_timerOff - _delay).mul(10)); 
				}
			}
		}
		else if(_timerOn != -1.0f)
		{
			_timerOn += dt;
			
			if(_timerOn >= 0.2f)
			{
				_color = _scolor.add(_dcolor);
				_offset = _soffset + _doffset;
				
				_timerOn = -1.0f;
			}
			else
			{
				_color = _scolor.add(_dcolor.mul(_timerOn * 5));
				_offset = _soffset + _doffset * _timerOn * 5;
			}
		}
	}

	@Override
	public void Enter() 
	{
		DGEColor tcolor;
		
		_scolor2 = DGEColor.ParseARGB("00FFE060");
		tcolor   = DGEColor.ParseARGB("FFFFE060");
		_dcolor2 = tcolor.sub(_scolor2);
		
		_sshadow = DGEColor.ParseARGB("00000000");
		tcolor   = DGEColor.ParseARGB("30000000");
		_dshadow = tcolor.sub(_sshadow);
		
		_timerOff = 0.0f;
	}

	@Override
	public void Leave()
	{
		DGEColor tcolor;
		
		_scolor2 = DGEColor.ParseARGB("FFFFE060");
		tcolor   = DGEColor.ParseARGB("00FFE060");
		_dcolor2 = tcolor.sub(_scolor2);
		
		_sshadow = DGEColor.ParseARGB("30000000");
		tcolor   = DGEColor.ParseARGB("00000000");
		_dshadow = tcolor.sub(_sshadow);
		
		_timerOff = 0.0f;
	}

	@Override
	public void Reset() 
	{
		
	}

	@Override
	public boolean IsDone() 
	{
		if (_timerOff == -1.0f)
			return true;
		else
			return false;
	}

	@Override
	public void Focus(boolean focused)
	{
		DGEColor tcolor;
		
		if (focused)
		{
			if (!_focus)
				_dge.Effect_Play(_sound);
			
			_scolor = DGEColor.ParseARGB("FFFFE060");
			tcolor = DGEColor.ParseARGB("FFFFFFFF");
			
			_soffset = 0;
			_doffset = 4;
		}
		else
		{
			_scolor = DGEColor.ParseARGB("FFFFFFFF");
			tcolor = DGEColor.ParseARGB("FFFFE060");
			
			_soffset = 4;
			_doffset = -4;
		}
		
		_focus = focused;
		
		_dcolor = tcolor.sub(_scolor);
		_timerOn = 0.0f;
	}

	@Override
	public boolean Touch(float x, float y) 
	{
		return false;
	}

	@Override
	public boolean Touched(boolean down)
	{
		if (down)
		{
			_offset = 4;
			return true;
		}
		else
		{
			_dge.Effect_Play(_sound);
			_offset = 0;
			return false;
		}
	}

}
