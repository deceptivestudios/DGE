package com.deceptivestudios.tests;


import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.physics.box2d.common.Vec2;
import com.deceptivestudios.engine.physics.box2d.dynamics.Rope;
import com.deceptivestudios.engine.physics.box2d.dynamics.RopeDef;

public class Test_10 extends Test
{
	private int _total = 40;
	private Vec2[] _vertices;
	private float[] _masses;
	
	private Rope _rope;
	private float _angle;
	
	@Override
	public boolean Create() 
	{
		_dge.Physics_Initialize(0, 9.8f, 32);
		
		_vertices = new Vec2[_total];
		_masses = new float[_total];

		for (int i = 0; i < _total; ++i)
		{
			_vertices[i] = new Vec2(0.0f, 20.0f - 0.25f * i);
			_masses[i] = 1.0f;
		}
		
		_masses[0] = 0.0f;
		_masses[1] = 0.0f;

		RopeDef def = new RopeDef();
		
		def.vertices = _vertices;
		def.count = _total;
		def.gravity.set(0.0f, 9.8f);
		def.masses = _masses;
		def.damping = 0.1f;
		def.stretching = 1.0f;
		def.bending = 0.5f;

		_rope = new Rope();
		_rope.initialize(def);

		_angle = 0.0f;
		_rope.setAngle(_angle);
		
		_ready = true;

		return true;
	}

	@Override
	public boolean Render() 
	{
		_dge.Physics_Render(DGE.DGE_PHYSICS_ALL);
		
		return true;
	}

	@Override
	public boolean Update(float delta) 
	{
		_dge.Physics_Update(delta);
		_rope.step(delta, 1);

		return true;
	}

}
