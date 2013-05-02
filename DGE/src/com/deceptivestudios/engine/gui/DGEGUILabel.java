package com.deceptivestudios.engine.gui;

import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.helper.DGEFont;
import com.deceptivestudios.engine.helper.DGERect;

public class DGEGUILabel extends DGEGUIObject
{
	private int _align;
	private DGEFont _font;
	private String _text;
	
	public DGEGUILabel(int id, float x, float y, DGEFont font, String text)
	{
		this(id, x, y, font, text, 1, 1, 1, 1);
	}
	
	public DGEGUILabel(int id, float x, float y, DGEFont font, String text, DGEColor color)
	{
		this(id, x, y, font, text, color, DGEFont.Left);
	}
	
	public DGEGUILabel(int id, float x, float y, DGEFont font, String text, DGEColor color, int align)
	{
		this(id, x, y, font, text, color.r, color.g, color.b, color.a, align);
	}
	
	public DGEGUILabel(int id, float x, float y, DGEFont font, String text, float r, float g, float b, float a)
	{
		this(id, x, y, font, text, r, g, b, a, DGEFont.Left);
	}
	
	public DGEGUILabel(int id, float x, float y, DGEFont font, String text, float r, float g, float b, float a, int align)
	{
		this.id = id;
		this.stationary = true;
		this.visible = true;
		this.enabled = true;
		this.color = new DGEColor(r, g, b, a);

		this._x = x; this._y = y;
		
		_font = font;
		_align = align & DGEFont.HorizontalMask;
		
		SetText(text);
	}
	
	public void SetAlign(int align)
	{
		_align = align & DGEFont.HorizontalMask;
		
		float w = _rect.p2.x - _rect.p1.x;
		float h = _rect.p2.y - _rect.p1.y;
		
		switch (_align)
		{
			case DGEFont.Left:
			{
				_rect.Set(_x, _y, _x + w, _y + h);
			} break;
			
			case DGEFont.Center:
			{
				_rect.Set(_x - w / 2, _y, _x + w / 2, _y + h);
			} break;
			
			case DGEFont.Right:
			{
				_rect.Set(_x - w, _y, _x, _y + h);
			} break;
		}
	}
	
	public void SetText(String text)
	{
		_text = text;
		
		float w = _font.GetStringWidth(text);
		float h = _font.GetHeight() * _font.GetScale() * CountLines(text);
		
		_rect = new DGERect(_x, _y, _x + w, _y + h);
	}
	
	public void SetText(int value)
	{
		SetText(String.format("%d", value));
	}
	
	@Override
	public void SetPosition(float x, float y)
	{
		_x = x;
		_y = y;
		
		SetAlign(_align);
	}
	
	@Override
	public void Render() 
	{
		_font.SetColor(color);
		_font.Render(_x, _y, _align, _text);
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
	@Override
	public boolean Touched(boolean down) { return false; }
}
