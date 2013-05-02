package com.deceptivestudios.tests;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEFont;

public abstract class Test 
{
	public static final int TotalTests = 11;
	
	public static DGEFont Font;
	public static int Width, Height;
	public static float FPS;
	
	protected DGE _dge;
	protected boolean _ready = false;
	
	public Test()
	{
		_dge = DGE.Interface(DGE.DGE_VERSION);
	}
	
	public boolean Ready()
	{
		return _ready;
	}
	
	public abstract boolean Create();
	public abstract boolean Render();
	public abstract boolean Update(float delta);
}
