package com.deceptivestudios.engine.gui;

import com.deceptivestudios.engine.helper.DGEFont;
import com.deceptivestudios.engine.helper.DGERect;

public class DGEGUIText extends DGEGUIObject
{
	private DGEFont _font;
	private float _tx, _ty;
	private int _align;
	private String _text;
	
	public DGEGUIText(int id, float x, float y, float w, float h, DGEFont font)
	{
		this.id = id;
		this.stationary = true;
		this.visible = true;
		this.enabled = true;
		
		this._x = x; this._y = y;
		this._rect = new DGERect(x, y, x + w, y + h);
		
		_font = font;
		_tx = x;
		_ty = y;
		
		_text = "";
	}
	
	public void SetMode(int align)
	{
		_align = align;
		
		int halign = align & DGEFont.HorizontalMask;
		
		if (halign == DGEFont.Right)
			_tx = _rect.p2.x;
		else if (halign == DGEFont.Center)
			_tx = (_rect.p1.x + _rect.p2.x) / 2f;
		else
			_tx = _rect.p1.x;
			
	}

	public void SetText(String text, Object... args)
	{
		_text = String.format(text, args);
	}
	
	@Override
	public void Render() 
	{
		_font.SetColor(color);
		_font.Render(_tx, _ty, _align, _text);
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
