package com.deceptivestudios.engine.helper;

import com.deceptivestudios.engine.DGE;

public abstract class DGEGUIObject 
{
	int id;
	
	boolean enabled;
	boolean stationary;
	boolean visible;
	
	DGERect rect;
	DGEColor color;

	DGEGUI gui;
	DGEGUIObject next;
	DGEGUIObject prev;
	
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
	
	protected static DGE _dge;
}
