package com.deceptivestudios.engine.physics.box2d.dynamics.joints;

import com.deceptivestudios.engine.physics.box2d.common.Mat22;
import com.deceptivestudios.engine.physics.box2d.common.Settings;
import com.deceptivestudios.engine.physics.box2d.common.Vec2;
import com.deceptivestudios.engine.physics.box2d.dynamics.Body;
import com.deceptivestudios.engine.physics.box2d.dynamics.TimeStep;
import com.deceptivestudios.engine.physics.box2d.pooling.IWorldPool;

public class RopeJoint extends Joint 
{
	// solver shared
	protected Vec2 _localAnchorA = new Vec2();
	protected Vec2 _localAnchorB = new Vec2();
	protected float _maxLength;
	protected float _length;
	protected float _impulse;
	protected float _mass;
	
	// solver temp
	protected Vec2 _u = new Vec2();
	protected Vec2 _rA = new Vec2();
	protected Vec2 _rB = new Vec2();
	protected Vec2 _localCenterA = new Vec2();
	protected Vec2 _localCenterB = new Vec2();
	protected LimitState _state;
	
	protected RopeJoint(IWorldPool argPool, RopeJointDef def) 
	{
		super(argPool, def);
		
		_localAnchorA.set(def.localAnchorA);
		_localAnchorB.set(def.localAnchorB);
		
		_maxLength = def.maxLength;
		
		_mass = 0.0f;
		_impulse = 0.0f;
		_state = LimitState.INACTIVE;
		_length = 0.0f;
	}

	@Override
	public void getAnchorA(Vec2 argOut) 
	{
		m_bodyA.getWorldPointToOut(_localAnchorA, argOut);
	}

	@Override
	public void getAnchorB(Vec2 argOut) 
	{
		m_bodyB.getWorldPointToOut(_localAnchorB, argOut);
	}
	
	@Override
	public void getReactionForce(float inv_dt, Vec2 argOut) 
	{
		argOut.set(_u.mul(_impulse * inv_dt));
	}

	@Override
	public float getReactionTorque(float inv_dt)
	{
		return 0.0f;
	}

	public float getMaxLength()
	{
		return _maxLength;
	}
	
	public void setMaxLength(float length)
	{
		_maxLength = length;
	}
	
	public LimitState getLimitState()
	{
		return _state;
	}

	@Override
	public void initVelocityConstraints(TimeStep step) 
	{
		Body bA = m_bodyA;
		Body bB = m_bodyB;
		
		_rA = Mat22.mul(bA.getTransform().R, _localAnchorA.sub(bA.getLocalCenter()));
		
		_u.set(bB.m_sweep.c.add(_rB).sub(bA.m_sweep.c).sub(_rA));
		_length = _u.length();
		
		float C = _length = _maxLength;
		
		if (C > 0.0f)
			_state = LimitState.AT_UPPER;
		else
			_state = LimitState.INACTIVE;
		
		if (_length > Settings.linearSlop)
		{
			_u.mulLocal(1.0f / _length);
		}
		else
		{
			_u.setZero();
			_mass = 0.0f;
			_impulse = 0.0f;
			
			return;
		}
		
		float crA = Vec2.cross(_rA, _u);
		float crB = Vec2.cross(_rB, _u);
		float invMass = bA.m_invMass + bA.m_invI * crA * crA + bB.m_invMass + bB.m_invI * crB * crB;
		
		_mass = invMass != 0.0f ? 1.0f / invMass : 0.0f;
		
		if (step.warmStarting)
		{
			_impulse *= step.dtRatio;
			
			final Vec2 P = pool.popVec2();
			
			P.set(_u.mul(_impulse));
			
			bA.m_linearVelocity.subLocal(P.mul(bA.m_invMass));
			bA.m_angularVelocity -= bA.m_invI * Vec2.cross(_rA, P);

			bB.m_linearVelocity.addLocal(P.mul(bB.m_invMass));
			bB.m_angularVelocity += bB.m_invI * Vec2.cross(_rB, P);
			
			pool.pushVec2(1);
		}
		else
		{
			_impulse = 0f;
		}
	}

	@Override
	public void solveVelocityConstraints(TimeStep step) 
	{
		Body bA = m_bodyA;
		Body bB = m_bodyB;
		
		final Vec2 vpA = pool.popVec2();
		final Vec2 vpB = pool.popVec2();
		
		vpA.set(bA.m_linearVelocity.add(Vec2.cross(bA.m_angularVelocity, _rA)));
		vpB.set(bB.m_linearVelocity.add(Vec2.cross(bB.m_angularVelocity, _rB)));
		
		float C = _length - _maxLength;
		float Cdot = Vec2.dot(_u, vpB.sub(vpA));
		
		if (C < 0.0f)
			Cdot += step.inv_dt * C;
		
		float impulse = -_mass * Cdot;
		float oldImpulse = _impulse;
		
		_impulse = Math.min(0, _impulse + impulse);
		impulse = _impulse - oldImpulse;
		
		final Vec2 P = pool.popVec2();
		P.set(_u.mul(impulse));
		
		bA.m_linearVelocity.subLocal(P.mul(bA.m_invMass));
		bA.m_angularVelocity -= bA.m_invI * Vec2.cross(_rA, P);
		bB.m_linearVelocity.addLocal(P.mul(bB.m_invMass));
		bB.m_angularVelocity += bB.m_invI * Vec2.cross(_rB, P);
		
		pool.pushVec2(3);
	}

	@Override
	public boolean solvePositionConstraints(float baumgarte)
	{
		Body bA = m_bodyA;
		Body bB = m_bodyB;
		
		final Vec2 rA = pool.popVec2();
		final Vec2 rB = pool.popVec2();
		final Vec2 u = pool.popVec2();
		final Vec2 P = pool.popVec2();
		
		rA.set(Mat22.mul(bA.getTransform().R, _localAnchorA.sub(bA.getLocalCenter())));
		rB.set(Mat22.mul(bB.getTransform().R, _localAnchorB.sub(bB.getLocalCenter())));
		
		u.set(bB.m_sweep.c.add(rB).sub(bA.m_sweep.c).sub(rA));
		
		float length = u.normalize();
		float C = length - _maxLength;
		
		C = Math.max(0.0f, Math.min(C, Settings.maxLinearCorrection));
		
		float impulse = -_mass * C;
		P.set(u.mul(impulse));
		
		bA.m_sweep.c.subLocal(P.mul(bA.m_invMass));
		bA.m_sweep.a -= bA.m_invI * Vec2.cross(rA, P);
		bB.m_sweep.c.addLocal(P.mul(bB.m_invMass));
		bB.m_sweep.a += bB.m_invI * Vec2.cross(rB, P);
		
		bA.synchronizeTransform();
		bB.synchronizeTransform();
		
		pool.pushVec2(4);
		
		return ((length - _maxLength) < Settings.linearSlop);
	}
}
