package com.deceptivestudios.engine.physics.box2d.dynamics;

import com.deceptivestudios.engine.physics.box2d.common.Vec2;

public class RopeDef 
{
	public Vec2[] vertices = null;
	public int count = 0;
	public float[] masses = null;
	public Vec2 gravity = new Vec2();
	public float damping = 0.1f;
	public float stretching = 0.9f;
	public float bending = 0.1f;
}
