package com.deceptivestudios.engine.polygon;

import com.deceptivestudios.engine.helper.DGEColor;

public class DGEPolygonData 
{
	public DGEPolygonTextureData Texture;
	
	public int Blend;
	public DGEColor Color;
	
	public boolean Tiled;
	public float OffsetX;
	public float OffsetY;
	public float Scale;
	public boolean World;
	
	public boolean Bordered;
	public DGEPolygonTextureData BorderTexture;
	public float BorderMin;
	public float BorderMax;
	public float BorderHeight;
	public float BorderScale;
	
	public boolean Shadowed;
	public DGEPolygonTextureData ShadowTexture;
	public boolean ShadowFollowBorder;
	public float ShadowHeight;
	public float ShadowScale;
	public DGEColor ShadowColor;
	
	public boolean NormalMapped;
	public DGEPolygonTextureData NormalMapTexture;
}
