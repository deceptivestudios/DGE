package com.deceptivestudios.engine.gui;

import com.deceptivestudios.engine.helper.DGERect;
import com.deceptivestudios.engine.helper.DGESprite;

public class DGEGUISlider extends DGEGUIObject 
{
	public static final int Bar = 0;
	public static final int Relative = 1;
	public static final int Slider = 2;

	private boolean _pressed;
	private boolean _vertical;
	private int _mode;
	private float _min, _max, _value;
	private float _sliderw, _sliderh;
	private DGESprite _slider;
	
	public DGEGUISlider(int id, float x, float y, float w, float h, int texture, float tx, float ty, float sw, float sh)
	{
		this(id, x, y, w, h, texture, tx, ty, sw, sh, false);
	}
	
	public DGEGUISlider(int id, float x, float y, float w, float h, int texture, float tx, float ty, float sw, float sh, boolean vertical)
	{
		this.id = id;
		this.stationary = false;
		this.visible = true;
		this.enabled = true;
		
		this._x = x; this._y = y;
		this._rect = new DGERect(x, y, x + w, y + h);
		
		_pressed = false;
		_vertical = vertical;
		_mode = Bar;
		_min = 0; _max = 100; _value = 50;
		_sliderw = sw; _sliderh = sh;
		
		_slider = new DGESprite(texture, tx, ty, sw, sh);
	}
	
	public void SetValue(float value)
	{
		if (value < _min)
			_value = _min;
		else if (value > _max)
			_value = _max;
		else
			_value = value;
	}
	
	@Override
	public void Render() 
	{
		float xx, yy;
		float x1 = 0, y1 = 0, x2 = 0, y2 = 0;
		
		xx = _rect.p1.x + (_rect.p2.x - _rect.p1.x) * (_value - _min) / (_max - _min);
		yy = _rect.p1.y + (_rect.p2.y - _rect.p1.y) * (_value - _min) / (_max - _min);

		if (_vertical)
		{
			switch (_mode)
			{
				case Bar:
				{
					x1 = _rect.p1.x; y1 = _rect.p1.y;
					x2 = _rect.p2.x; y2 = yy;
				} break;
				
				case Relative:
				{
					x1 = _rect.p1.x; y1 = (_rect.p1.y + _rect.p2.y) / 2f;
					x2 = _rect.p2.x; y2 = yy;
				} break;
				
				case Slider:
				{
					x1 = (_rect.p1.x + _rect.p2.x - _sliderw) / 2f; y1 = yy - _sliderh / 2f; 
					x2 = (_rect.p1.x + _rect.p2.x + _sliderw) / 2f; y2 = yy + _sliderh / 2f;
				} break;
			}
		}
		else
		{
			switch (_mode)
			{
				case Bar:
				{
					x1 = _rect.p1.x; y1 = _rect.p1.y;
					x2 = xx;         y2 = _rect.p2.y;
				} break;
				
				case Relative:
				{
					x1 = (_rect.p1.x + _rect.p2.x) / 2f; y1 = _rect.p1.y; 
					x2 = xx;                             y2 = _rect.p2.y;
				} break; 
				
				case Slider:
				{
					x1 = xx - _sliderw / 2f; y1 = (_rect.p1.y + _rect.p2.y - _sliderh) / 2f; 
					x2 = xx + _sliderw / 2f; y2 = (_rect.p1.y + _rect.p2.y + _sliderh) / 2f;
				} break;
			}
		}
		
		_slider.RenderStretch(x1, y1, x2, y2);
	}

	@Override
	public boolean Touch(float x, float y) 
	{
		if (_pressed)
		{
			if (_vertical)
			{
				if (y > _rect.p2.y - _rect.p1.y)
					y = _rect.p2.y - _rect.p1.y;
				
				if (y < 0)
					y = 0;
				
				_value = _min + (_max - _min) * y / (_rect.p2.y - _rect.p1.y);
			}
			else
			{
				if (x > _rect.p2.x - _rect.p1.x)
					x = _rect.p2.x - _rect.p1.x;
				
				if (x < 0)
					x = 0;
				
				_value = _min + (_max - _min) * x / (_rect.p2.x - _rect.p1.x);
			}
			
			return true;
		}

		return false;
	}

	@Override
	public boolean Touched(boolean down) 
	{
		_pressed = down;

		return false;
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
}
