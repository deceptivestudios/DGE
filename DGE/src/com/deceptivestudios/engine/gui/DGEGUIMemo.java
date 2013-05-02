package com.deceptivestudios.engine.gui;

import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.helper.DGEFont;
import com.deceptivestudios.engine.helper.DGERect;

public class DGEGUIMemo extends DGEGUIObject
{
	private DGEFont _font;
	private String _text;
	
	public DGEGUIMemo(int id, float x, float y, float w, float h, DGEFont font, String text)
	{
		this(id, x, y, w, h, font, text, 1, 1, 1, 1);
	}
	
	public DGEGUIMemo(int id, float x, float y, float w, float h, DGEFont font, String text, DGEColor color)
	{
		this(id, x, y, w, h, font, text, color.r, color.g, color.b, color.a);
	}
	
	public DGEGUIMemo(int id, float x, float y, float w, float h, DGEFont font, String text, float r, float g, float b, float a)
	{
		this.id = id;
		this.stationary = true;
		this.visible = true;
		this.enabled = true;
		this.color = new DGEColor(r, g, b, a);
		
		this._x = x; this._y = y;
		this._rect = new DGERect(x, y, x + w, y + h);
		
		_font = font;
	}
	
	public void SetText(String text)
	{
		_text = text;
	}

	@Override
	public void Render() 
	{
		_font.SetColor(color);
		_font.Render(_x, _y, DGEFont.Left, _text);
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
