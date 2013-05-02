package com.deceptivestudios.engine.physics;

import com.deceptivestudios.engine.entity.DGEEntity;
import com.deceptivestudios.engine.entity.DGEEntityEx;
import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.physics.box2d.collision.shapes.PolygonShape;
import com.deceptivestudios.engine.physics.box2d.common.Vec2;
import com.deceptivestudios.engine.physics.box2d.dynamics.Body;
import com.deceptivestudios.engine.physics.box2d.dynamics.BodyDef;
import com.deceptivestudios.engine.physics.box2d.dynamics.Filter;
import com.deceptivestudios.engine.physics.box2d.dynamics.FixtureDef;
import com.deceptivestudios.engine.physics.box2d.dynamics.World;

public class DGEPhysicsEntity extends DGEEntityEx 
{
	protected Body _body;
	protected World _world;
	
	protected FixtureDef _shapeDef;
	protected BodyDef _bodyDef;
	
	public DGEPhysicsEntity(World world, int layer) 
	{
		super(layer, false);
		
		_world = world;
		_body = null;
	}

	@Override
	public DGEEntity Spawn() { return new DGEPhysicsEntity(_world, GetRenderLayer()); }
	
	public void Free()
	{
		if (_body == null)
			_world.destroyBody(_body);
	}
	
	public void Update(float delta)
	{
		if (_body == null)
			return;
		
		Vec2 pos = _body.getPosition();
		float rot = _body.getAngle();
		
		SetRotation(rot);
		SetPosition(_dge.Physics_WorldToScreen(pos));
	}
	
	public DGEPhysicsEntity Clone()
	{
		DGEPhysicsEntity entity = new DGEPhysicsEntity(_world, GetRenderLayer());
		entity.Copy(this);
		return entity;
	}
	
	public Body GetPhysicsBody()
	{
		return _body;
	}
	
	public void InitialisePhysics(Filter filter)
	{
		InitialisePhysics(filter, 0.3f);
	}
	
	public void InitialisePhysics(Filter filter, float friction)
	{
		InitialisePhysics(filter, friction, 1.0f);
	}
	
	public void InitialisePhysics(Filter filter, float friction, float density)
	{
		InitialisePhysics(filter, friction, density, 0.01f);
	}
	
	public void InitialisePhysics(Filter filter, float friction, float density, float restitution)
	{
		_bodyDef = new BodyDef();
		_bodyDef.position.set(_dge.Physics_ScreenToWorld(_center));
		
		_body = _world.createBody(_bodyDef);
		
		_shapeDef = new FixtureDef();
		
		_shapeDef.density = density;
		_shapeDef.friction = friction;
		_shapeDef.restitution = restitution;
		
		_shapeDef.filter = filter;
		
		int vmax = Triangles.size();
		
		for (int i = 0; i < vmax; i++)
		{
			PolygonShape shape = new PolygonShape();
			shape.m_vertexCount = 3;
			
			for (int j = 0; j < 3; j++) 
			{
				DGEVector position = Triangles.get(i).Vertices[j].subtract(_center);
				shape.m_vertices[j].set(_dge.Physics_ScreenToWorld(position));
			}
			
			_shapeDef.shape = shape;
			_body.createFixture(_shapeDef);
		}
		
		_body.resetMassData();
		
		SetHotSpot(_center.subtract(_aabb.p1));
	}
	
	public void InitialisePhysics(DGEVector[] box, Filter filter)
	{
		InitialisePhysics(box, filter, 0.3f);
	}
	
	public void InitialisePhysics(DGEVector[] box, Filter filter, float friction)
	{
		InitialisePhysics(box, filter, friction, 1.0f);
	}
	
	public void InitialisePhysics(DGEVector[] box, Filter filter, float friction, float density)
	{
		InitialisePhysics(box, filter, friction, density, 0.01f);
	}
	
	public void InitialisePhysics(DGEVector[] box, Filter filter, float friction, float density, float restitution)
	{
		_bodyDef = new BodyDef();
		_bodyDef.position.set(_dge.Physics_ScreenToWorld(_center));
		
		_body = _world.createBody(_bodyDef);
		
		_shapeDef = new FixtureDef();
		
		_shapeDef.density = density;
		_shapeDef.friction = friction;
		_shapeDef.restitution = restitution;
		
		_shapeDef.filter = filter;
		
		PolygonShape shape = new PolygonShape();
		shape.m_vertexCount = 4;
		
		for (int i = 0; i < 4; i++)
			shape.m_vertices[i].set(_dge.Physics_ScreenToWorld(box[i]));

		_shapeDef.shape = shape;
		
		_body.createFixture(_shapeDef);
		_body.resetMassData();
		
		SetHotSpot(_center.subtract(_aabb.p1));
	}
}
