package com.deceptivestudios.engine.physics.box2d.dynamics;

import com.deceptivestudios.engine.physics.box2d.common.MathUtils;
import com.deceptivestudios.engine.physics.box2d.common.Vec2;

public class Rope 
{
	private int _count;
	private Vec2[] _ps;
	private Vec2[] _p0s;
	private Vec2[] _vs;

	float[] _ims;

	float[] _Ls;
	float[] _as;

	private Vec2 _gravity;
	private float _damping;

	private float _stretching;
	private float _bending;
	
	public Rope()
	{
		_count = 0;
		_ps = null;
		_p0s = null;
		_vs = null;
		
		_ims = null;
		
		_Ls = null;
		_as = null;
		
		_gravity = new Vec2();
		_gravity.setZero();
		
		_stretching = 1.0f;
		_bending = 0.1f;
	}
	
	public void initialize(RopeDef def)
	{
		_count = def.count;
		
		_ps = new Vec2[_count];
		_p0s = new Vec2[_count];
		_vs = new Vec2[_count];
		_ims = new float[_count];

		for (int i = 0; i < _count; ++i)
		{
			_ps[i] = new Vec2(def.vertices[i]);
			_p0s[i] = new Vec2(def.vertices[i]);
			_vs[i] = new Vec2();

			float m = def.masses[i];
			
			if (m > 0.0f)
				_ims[i] = 1.0f / m;
			else
				_ims[i] = 0.0f;
		}

		int count2 = _count - 1;
		int count3 = _count - 2;
		
		_Ls = new float[count2];
		_as = new float[count3];

		for (int i = 0; i < count2; ++i)
		{
			Vec2 p1 = _ps[i];
			Vec2 p2 = _ps[i+1];
			
			_Ls[i] = (p2.sub(p1)).length();
		}

		for (int i = 0; i < count3; ++i)
		{
			Vec2 p1 = _ps[i];
			Vec2 p2 = _ps[i + 1];
			Vec2 p3 = _ps[i + 2];

			Vec2 d1 = p2.sub(p1);
			Vec2 d2 = p3.sub(p2);

			float a = Vec2.cross(d1, d2);
			float b = Vec2.dot(d1, d2);

			_as[i] = (float) Math.atan2(a, b);
		}

		_gravity = def.gravity;
		_damping = def.damping;
		
		_stretching = def.stretching;
		_bending = def.bending;
	}
	
	public void step(float timeStep, int iterations)
	{
		if (timeStep == 0.0)
			return;

		float d = (float) Math.exp(-timeStep * _damping);

		for (int i = 0; i < _count; ++i)
		{
			_p0s[i] = _ps[i];
			
			if (_ims[i] > 0.0f)
				_vs[i].addLocal(_gravity.mul(timeStep));
			
			_vs[i].mulLocal(d);
			_ps[i].addLocal(_vs[i].mul(timeStep));
		}

		for (int i = 0; i < iterations; ++i)
		{
			solveC2();
			solveC3();
			solveC2();
		}

		float inv_timeStep = 1.0f / timeStep;
		
		for (int i = 0; i < _count; ++i)
			_vs[i] = _ps[i].sub(_p0s[i]).mul(inv_timeStep);
	}
	
	public int getVertexCount()
	{
		return _count;
	}
	
	public Vec2[] getVertices()
	{
		return _ps;
	}

	public void setAngle(float angle)
	{
		int count3 = _count - 2;
		
		for (int i = 0; i < count3; ++i)
			_as[i] = angle;
	}

	private void solveC2()
	{
		int count2 = _count - 1;

		for (int i = 0; i < count2; ++i)
		{
			Vec2 p1 = _ps[i];
			Vec2 p2 = _ps[i + 1];

			Vec2 d = p2.sub(p1);
			float L = d.normalize();

			float im1 = _ims[i];
			float im2 = _ims[i + 1];

			if (im1 + im2 == 0.0f)
				continue;

			float s1 = im1 / (im1 + im2);
			float s2 = im2 / (im1 + im2);

			p1.subLocal(d.mul(_stretching * s1 * (_Ls[i] - L)));
			p2.addLocal(d.mul(_stretching * s2 * (_Ls[i] - L)));

			_ps[i] = p1;
			_ps[i + 1] = p2;
		}
	}
	
	private void solveC3()
	{
		int count3 = _count - 2;

		for (int i = 0; i < count3; ++i)
		{
			Vec2 p1 = _ps[i];
			Vec2 p2 = _ps[i + 1];
			Vec2 p3 = _ps[i + 2];

			float m1 = _ims[i];
			float m2 = _ims[i + 1];
			float m3 = _ims[i + 2];

			Vec2 d1 = p2.sub(p1);
			Vec2 d2 = p3.sub(p2);

			float L1sqr = d1.lengthSquared();
			float L2sqr = d2.lengthSquared();

			if (L1sqr * L2sqr == 0.0f)
				continue;

			float a = Vec2.cross(d1, d2);
			float b = Vec2.dot(d1, d2);

			float angle = (float) Math.atan2(a, b);

			Vec2 Jd1 = d1.skew().mul(-1.0f / L1sqr);
			Vec2 Jd2 = d2.skew().mul(1.0f / L2sqr);

			Vec2 J1 = Jd1.negate();
			Vec2 J2 = Jd1.sub(Jd2);
			Vec2 J3 = new Vec2(Jd2);

			float mass = (m1 * Vec2.dot(J1, J1)) + (m2 * Vec2.dot(J2, J2)) + (m3 * Vec2.dot(J3, J3));
			
			if (mass == 0.0f)
				continue;

			mass = 1.0f / mass;

			float C = angle - _as[i];

			while (C > MathUtils.PI)
			{
				angle -= 2 * MathUtils.PI;
				C = angle - _as[i];
			}

			while (C < -MathUtils.PI)
			{
				angle += 2.0f * MathUtils.PI;
				C = angle - _as[i];
			}

			float impulse = - _bending * mass * C;

			p1.addLocal(J1.mul(m1 * impulse));
			p2.addLocal(J2.mul(m2 * impulse));
			p3.addLocal(J3.mul(m3 * impulse));

			_ps[i] = p1;
			_ps[i + 1] = p2;
			_ps[i + 2] = p3;
		}
	}
}
