package com.deceptivestudios.engine.particle;

import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.helper.DGEVector;

public class DGEParticle
{
	public DGEVector Location;
	public DGEVector Velocity;

	public float Gravity;
	public float RadialAccel;
	public float TangentialAccel;

	public float Spin;
	public float SpinDelta;

	public float Size;
	public float SizeDelta;

	public DGEColor Color;		// + alpha
	public DGEColor ColorDelta;

	public float Age;
	public float TerminalAge;
	
	public DGEParticle()
	{
		Location = new DGEVector();
		Velocity = new DGEVector();
		
		Color = new DGEColor();
		ColorDelta = new DGEColor();
	}
}
