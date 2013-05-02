package com.deceptivestudios.engine.character;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.physics.DGEPhysicsEntity;
import com.deceptivestudios.engine.physics.box2d.dynamics.Filter;
import com.deceptivestudios.engine.physics.box2d.dynamics.World;
import com.deceptivestudios.engine.physics.box2d.dynamics.joints.Joint;

public class DGERagdollPart extends DGEPhysicsEntity
{
	public Joint Joint;
	
	public DGERagdollPart(World world, DGEAnimationSprite sprite, int layer, Filter filter, boolean mirrored)
	{
		this(world, sprite, layer, filter, mirrored, 4.0f);
	}
	
	public DGERagdollPart(World world, DGEAnimationSprite sprite, int layer, Filter filter, boolean mirrored, float density)
	{
		super(world, layer);
		
		Joint = null;
		
		_aabb = sprite.Rect();
		_center = sprite.GetCenter();
		
		EntityData.AddProperty("bone_name", sprite.Name);
		
		DGEVector[] verts = new DGEVector[4];
		
		for (int i = 0; i < 4; i++)
		{
			if (mirrored)
				verts[i] = sprite.Vertices[3 - i];
			else
				verts[i] = sprite.Vertices[i];
		}
		
		InitialisePhysics(verts, filter, 0.1f, density, 0.2f);
		
		TextureData.Blend = DGE.DGE_BLEND_DEFAULT;
		TextureData.Bordered = false;
		TextureData.NormalMapped = false;
		TextureData.Shadowed = false;
		TextureData.Texture.Texture = sprite.GetSprite().GetTexture();
	}
}
