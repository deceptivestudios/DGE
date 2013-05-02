package com.deceptivestudios.engine.physics.box2d.dynamics.joints;

import com.deceptivestudios.engine.physics.box2d.common.Vec2;

public class RopeJointDef extends JointDef 
{
	public RopeJointDef()
	{
		type = JointType.ROPE;
		localAnchorA = new Vec2(-1, 0);
		localAnchorB = new Vec2(1, 0);
		maxLength = 0f;
	}
	
	public Vec2 localAnchorA;
	public Vec2 localAnchorB;
	public float maxLength;
}
