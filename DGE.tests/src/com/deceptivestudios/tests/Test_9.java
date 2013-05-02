package com.deceptivestudios.tests;


import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.physics.box2d.collision.shapes.PolygonShape;
import com.deceptivestudios.engine.physics.box2d.common.Vec2;
import com.deceptivestudios.engine.physics.box2d.dynamics.Body;
import com.deceptivestudios.engine.physics.box2d.dynamics.BodyDef;
import com.deceptivestudios.engine.physics.box2d.dynamics.BodyType;
import com.deceptivestudios.engine.physics.box2d.dynamics.FixtureDef;
import com.deceptivestudios.engine.physics.box2d.dynamics.World;
import com.deceptivestudios.engine.physics.box2d.dynamics.joints.RevoluteJointDef;
import com.deceptivestudios.engine.physics.box2d.dynamics.joints.RopeJointDef;

public class Test_9 extends Test
{
	private World _world;
	private RopeJointDef _ropeDef;
	private float _gravity = -9.8f;

	@Override
	public boolean Create() 
	{
		_dge.Physics_Initialize(0, 0, 32);
		
		//_dge.Physics_CreateFixedBody(ShapeType.POLYGON, -16,   -32,    Width + 32, 32);
		//_dge.Physics_CreateFixedBody(ShapeType.POLYGON, -16,   Height, Width + 32, 32);
		
		//_dge.Physics_CreateFixedBody(ShapeType.POLYGON, -32,   -16,    32,         Height + 32);
		//_dge.Physics_CreateFixedBody(ShapeType.POLYGON, Width, -16,    32,         Height + 32);
		
		_world = _dge.Physics_GetWorld();

		Body ground = null;
		{
			BodyDef bodyDef = new BodyDef();
			ground = _world.createBody(bodyDef);
			
			PolygonShape edge = new PolygonShape();
			edge.setAsEdge(new Vec2(-32, 0), new Vec2(32, 0));
			ground.createFixture(edge, 0);
		}
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(0.125f, 0.5f);
		
		FixtureDef fixture = new FixtureDef();
		fixture.shape = shape;
		fixture.density = 20.0f;
		fixture.friction = 0.2f;
		fixture.filter.categoryBits = 0x0001;
		fixture.filter.maskBits = 0xFFFF & ~0x0002;
		
		RevoluteJointDef joint = new RevoluteJointDef();
		joint.collideConnected = false;
		
		int N = 10;
		float x = 15f;
		
		_ropeDef = new RopeJointDef();
		_ropeDef.localAnchorA.set(new Vec2(x, 0));
		
		Body prevBody = ground;
		
		for (int i = 0; i < N; i++)
		{
			BodyDef bodyDef = new BodyDef();
			bodyDef.type = BodyType.DYNAMIC;
			bodyDef.position.set(x, 0.5f + 1.0f * i);
			
			if (i == N - 1)
			{
				shape.setAsBox(1.5f, 1.5f);
				
				fixture.density = 40f;
				fixture.filter.categoryBits = 0x0002;
				
				bodyDef.position.set(x, 1f * i);
				bodyDef.angularDamping = 0.4f;
			}
			
			Body body = _world.createBody(bodyDef);
			body.createFixture(fixture);
			
			Vec2 anchor = new Vec2(x, i);
			
			if (prevBody != null)
			{
				joint.initialize(prevBody, body, anchor);
				_world.createJoint(joint);
			}
			
			prevBody = body;
		}
		
		float extraLength = 0.01f;

		_ropeDef.localAnchorB.setZero();
		_ropeDef.maxLength = N - 1.0f + extraLength;
		_ropeDef.bodyB = prevBody;
		_ropeDef.bodyA = ground;
		
		_world.createJoint(_ropeDef);
		
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

		float gx, gy;
		float[] tilt = _dge.Input_GetTilt();
		
		if (tilt != null)
		{
			gx = _gravity * tilt[1];
			gy = _gravity * tilt[2];
			
			_dge.Physics_SetGravity(gx, gy);
		}

		return true;
	}

}
