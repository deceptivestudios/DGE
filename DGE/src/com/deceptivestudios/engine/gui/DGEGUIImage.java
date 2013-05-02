package com.deceptivestudios.engine.gui;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEMultiple;
import com.deceptivestudios.engine.helper.DGERect;

public class DGEGUIImage extends DGEGUIObject 
{
	private DGEMultiple _quad;
	private float _width, _height;
	
	public DGEGUIImage(int id, float x, float y, String filename)
	{
		this.id = id;
		this.stationary = true;
		this.visible = true;
		this.enabled = true;
		
		int texture = _dge.Texture_Load(filename);
		
		_width = _dge.Texture_GetWidth(texture);
		_height = _dge.Texture_GetHeight(texture);
		
		_x = x; _y = y;
		_rect = new DGERect(_x, _y, _x + _width, _y + _height);
		
		_quad = new DGEMultiple(4);
		
		_quad.blend = DGE.DGE_BLEND_ALPHABLEND | DGE.DGE_BLEND_COLORMUL | DGE.DGE_BLEND_ZWRITE;
		_quad.texture = texture;
		
		_quad.vertices[0].tx = 0; _quad.vertices[0].ty = 0;
		_quad.vertices[1].tx = 1; _quad.vertices[1].ty = 0;
		_quad.vertices[2].tx = 1; _quad.vertices[2].ty = 1;
		_quad.vertices[3].tx = 0; _quad.vertices[3].ty = 1;
		
		SetPosition(x, y);
	}
	
	public void SetPosition(float x, float y)
	{
		_quad.vertices[0].x = x;          _quad.vertices[0].y = y; 
		_quad.vertices[1].x = x + _width; _quad.vertices[1].y = y; 
		_quad.vertices[2].x = x + _width; _quad.vertices[2].y = y + _height; 
		_quad.vertices[3].x = x;          _quad.vertices[3].y = y + _height; 
	}
	
	@Override
	public void Render() 
	{
		_dge.Gfx_RenderQuad(_quad);
	}

	@Override
	public void Update(float dt) { }
	@Override
	public void Enter() { }
	@Override
	public void Leave() { }
	@Override
	public void Reset() { }
	@Override
	public boolean IsDone() { return true; }
	@Override
	public void Focus(boolean focused) { }
	@Override
	public boolean Touch(float x, float y) { return false; }
	@Override
	public boolean Touched(boolean down) { return false; }
}
