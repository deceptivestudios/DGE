package com.deceptivestudios.engine.entity;

import java.util.Vector;

import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.polygon.DGEPolygonCollision;

public class DGETriggerEntity extends DGEEntity
{
	private Vector<DGEEntity> _targets;
	private float _timer;
	private String _collideEntity;
	private String _targetClass;
	private float _resetTime;
	
	public DGETriggerEntity(String collide, String target, float resetTime)
	{
		super(-1, true);
		
		_collideEntity = collide;
		_targetClass = target;
		_resetTime = resetTime;
	}

	@Override
	public DGETriggerEntity Spawn() 
	{
		return new DGETriggerEntity(_collideEntity, _targetClass, _resetTime);
	}
	
	public void Initialise()
	{
		String target = EntityData.GetString("target");
		Vector<DGEEntity> list = _map.GetDynamicEntityList(target);
		
		if (list != null)
		{
			for (int i = 0; i < list.size(); i++)
			{
				if (list.get(i).EntityData.GetString("name").equalsIgnoreCase(target))
					_targets.add(list.get(i));
			}
		}
		
		_timer = 0;
	}
	
	public void Update(float delta)
	{
		if (_targets.size() == 0)
			return;
		
		if (_timer > 0)
			_timer -= delta;
		
		if (_timer <= 0 && _map.Test(this, new DGEVector(), null, DGEPolygonCollision.Dynamic, _collideEntity).Collides)
		{
			for (int i = 0; i < _targets.size(); i++)
				_targets.get(i).Trigger();
			
			_timer = _resetTime;
		}
	}
	
	public void Render()
	{
		
	}
}
