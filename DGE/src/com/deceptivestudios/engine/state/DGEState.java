package com.deceptivestudios.engine.state;

import com.deceptivestudios.engine.DGE;

public abstract class DGEState 
{
	protected static DGE _dge;

	public DGEState()
	{
		_dge = DGE.Interface(DGE.DGE_VERSION);
	}
	
	public abstract boolean Initialize(String data);
	public abstract boolean Render();
	public abstract boolean Update(float delta);
}
