package com.deceptivestudios.engine.physics;


import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.helper.DGESprite;
import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.physics.box2d.collision.shapes.ShapeType;
import com.deceptivestudios.engine.physics.box2d.common.Vec2;
import com.deceptivestudios.engine.physics.box2d.dynamics.Body;

public class DGEPhysicsSprite
{
	protected static DGE _dge;
	
	private DGESprite _sprite;
	private Body _body;
	
	public DGEPhysicsSprite(ShapeType shape, DGESprite sprite, float x, float y, float mass, float friction)
	{
		_dge = DGE.Interface(DGE.DGE_VERSION);
		
		_sprite = sprite;
		
		if (_sprite != null)
			_body = _dge.Physics_CreateDynamicBody(shape, x, y, _sprite.GetWidth(), _sprite.GetHeight(), mass, friction);
	}
	
	public void SetPosition(float x, float y)
	{
		if (_body == null)
			return;
		
		Vec2 position = _dge.Physics_ScreenToWorld(new DGEVector(x, y));
		_body.setTransform(position, 0);
	}
	
	public DGEVector GetPosition()
	{
		if (_body == null)
			return null;
		
		Vec2 position = _body.getPosition();
		return _dge.Physics_WorldToScreen(position);
	}
	
	public void SetColor(DGEColor color)
	{
		if (_sprite != null)
			_sprite.SetColor(color);
	}
	
	public void SetColor(float r, float g, float b)
	{
		SetColor(r, g, b, 1f);
	}
	
	public void SetColor(float r, float g, float b, float a)
	{
		if (_sprite != null)
			_sprite.SetColor(r, g, b, a);
	}
	
	public void Render()
	{
		if (_body != null)
		{
			DGEVector vec = _dge.Physics_GetBodyPosition(_body);
			
			if (_sprite != null)
				_sprite.RenderEx(vec.x, vec.y, _body.getAngle());
		}
	}
}
