package com.deceptivestudios.engine.gui;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.helper.DGEGUI;
import com.deceptivestudios.engine.helper.DGERect;
import com.deceptivestudios.engine.helper.DGEVector;

public abstract class DGEGUIObject 
{
	public int id;
	
	public boolean enabled;
	public boolean stationary;
	public boolean visible;
	
	public DGEColor color;

	public DGEGUI gui;
	public DGEGUIObject next;
	public DGEGUIObject prev;
	
	public DGEGUIObject()
	{ 
		_dge = DGE.Interface(DGE.DGE_VERSION); 
		SetColor(1, 1, 1, 1); 
	}

	public abstract void Render();
	public abstract void Update(float dt);

	public abstract void Enter();
	public abstract void Leave();
	public abstract void Reset();
	
	public abstract boolean IsDone();
	public abstract void Focus(boolean focused);

	public abstract boolean Touch(float x, float y);
	public abstract boolean Touched(boolean down);

	public final void SetColor(float r, float g, float b)
	{
		SetColor(r, g, b, 1);
	}
	
	public final void SetColor(float r, float g, float b, float a)
	{
		SetColor(new DGEColor(r, g, b, a));
	}
	
	public final void SetColor(DGEColor color)
	{ 
		this.color = color; 
	}
	
	protected DGERect _rect;
	protected float _x, _y;
	
	public void SetPosition(float x, float y)
	{
		_x = x; _y = y;
		
		float w = (_rect.p2.x - _rect.p1.x);
		float h = (_rect.p2.x - _rect.p1.x);
		
		_rect.Set(_x, _y, _x + w, _y + h);
	}
	
	public void Move(float dx, float dy)
	{
		_rect.p1.x += dx;
		_rect.p1.y += dy;
		_rect.p2.x += dx;
		_rect.p2.y += dy;
	}
	
	public DGEVector GetPosition()
	{
		return _rect.p1;
	}
	
	public boolean TestPoint(float mx, float my)
	{
		return _rect.TestPoint(mx, my);
	}
	
	protected int CountLines(String text)
	{
		int lines = 1;
		
		for (int i = 0; i < text.length(); i++)
		{
			if (text.charAt(i) == '\n')
				lines++;
		}
		
		return lines;
	}
	
	protected static DGE _dge;
}
