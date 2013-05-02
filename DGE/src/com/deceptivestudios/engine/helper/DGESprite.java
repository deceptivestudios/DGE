package com.deceptivestudios.engine.helper;

import android.util.FloatMath;

import com.deceptivestudios.engine.DGE;

public class DGESprite 
{
	protected static DGE _dge;
	
	protected DGEQuad _quad;
	
	protected float _tx, _ty, _width, _height;
	protected float _tex_width, _tex_height;
	protected float _hotX, _hotY;
	
	protected boolean _bXFlip, _bYFlip, _bHSFlip;
	
	protected DGESprite()
	{
		_dge = DGE.Interface(DGE.DGE_VERSION);
	}

	public DGESprite(String file, float texx, float texy, float w, float h)
	{
		this();
		
		int texture = _dge.Texture_Load(file);
		Create(texture, texx, texy, w, h);
	}
	
	public DGESprite(int texture, float texx, float texy, float w, float h)
	{
		this();
		Create(texture, texx, texy, w, h);
	}
	
	private void Create(int texture, float texx, float texy, float w, float h)
	{
		float texx1, texy1, texx2, texy2;

		_tx = texx; _ty = texy;
		_width = w; _height = h;

		if (texture != 0)
		{
			_tex_width = (float) _dge.Texture_GetWidth(texture);
			_tex_height = (float) _dge.Texture_GetHeight(texture);
		}
		else
		{
			_tex_width = 1.0f;
			_tex_height = 1.0f;
		}

		_hotX = 0; _hotY = 0;
		_bXFlip = false; _bYFlip = false; _bHSFlip = false;
		
		_quad = new DGEQuad();
		_quad.texture = texture;

		texx1 = texx / _tex_width;			texy1 = texy / _tex_height;
		texx2 = (texx + w) / _tex_width;	texy2 = (texy + h) / _tex_height;
		
		_quad.SetTextureOffset(texx1, texy1, texx2, texy2);

		_quad.blend = DGE.DGE_BLEND_DEFAULT;
	}
	
	public DGESprite(DGESprite copy)
	{
		this();
		
		_quad = new DGEQuad(copy._quad);
		
		_tx = copy._tx;
		_ty = copy._ty;
		
		_width = copy._width;
		_height = copy._height;
		
		_tex_width = copy._tex_width;
		_tex_height = copy._tex_height;
		
		_hotX = copy._hotX;
		_hotY = copy._hotY;
		
		_bXFlip = copy._bXFlip;
		_bYFlip = copy._bYFlip;
		_bHSFlip = copy._bHSFlip;
	}
	
	public void Render(float x, float y)
	{
		float tempx1, tempy1, tempx2, tempy2;

		tempx1 = x - _hotX;
		tempy1 = y - _hotY;
		tempx2 = x + _width - _hotX;
		tempy2 = y + _height - _hotY;

		_quad.vertices[0].x = tempx1; _quad.vertices[0].y = tempy1;
		_quad.vertices[1].x = tempx2; _quad.vertices[1].y = tempy1;
		_quad.vertices[2].x = tempx2; _quad.vertices[2].y = tempy2;
		_quad.vertices[3].x = tempx1; _quad.vertices[3].y = tempy2;

		_dge.Gfx_RenderQuad(_quad);
	}

	public void RenderEx(float x, float y, float rot)
	{
		RenderEx(x, y, rot, 1f);
	}
	
	public void RenderEx(float x, float y, float rot, float hscale)
	{
		RenderEx(x, y, rot, hscale, 0f);
	}
	
	public void RenderEx(float x, float y, float rot, float hscale, float vscale)
	{
		float tx1, ty1, tx2, ty2;
		float sint, cost;

		if (vscale == 0) 
			vscale = hscale;

		tx1 = -_hotX * hscale;
		ty1 = -_hotY * vscale;
		tx2 = (_width - _hotX) * hscale;
		ty2 = (_height - _hotY) * vscale;

		if (rot != 0.0f)
		{
			cost = FloatMath.cos(rot);
			sint = FloatMath.sin(rot);
				
			_quad.vertices[0].x  = tx1 * cost - ty1 * sint + x;
			_quad.vertices[0].y  = tx1 * sint + ty1 * cost + y;	

			_quad.vertices[1].x  = tx2 * cost - ty1 * sint + x;
			_quad.vertices[1].y  = tx2 * sint + ty1 * cost + y;	

			_quad.vertices[2].x  = tx2 * cost - ty2 * sint + x;
			_quad.vertices[2].y  = tx2 * sint + ty2 * cost + y;	

			_quad.vertices[3].x  = tx1 * cost - ty2 * sint + x;
			_quad.vertices[3].y  = tx1 * sint + ty2 * cost + y;	
		}
		else
		{
			_quad.vertices[0].x = tx1 + x; _quad.vertices[0].y = ty1 + y;
			_quad.vertices[1].x = tx2 + x; _quad.vertices[1].y = ty1 + y;
			_quad.vertices[2].x = tx2 + x; _quad.vertices[2].y = ty2 + y;
			_quad.vertices[3].x = tx1 + x; _quad.vertices[3].y = ty2 + y;
		}

		_dge.Gfx_RenderQuad(_quad);
	}

	public void RenderStretch(float x1, float y1, float x2, float y2)
	{
		_quad.vertices[0].x = x1; _quad.vertices[0].y = y1;
		_quad.vertices[1].x = x2; _quad.vertices[1].y = y1;
		_quad.vertices[2].x = x2; _quad.vertices[2].y = y2;
		_quad.vertices[3].x = x1; _quad.vertices[3].y = y2;

		_dge.Gfx_RenderQuad(_quad);
	}

	public void Render4V(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3)
	{
		_quad.vertices[0].x = x0; _quad.vertices[0].y = y0;
		_quad.vertices[1].x = x1; _quad.vertices[1].y = y1;
		_quad.vertices[2].x = x2; _quad.vertices[2].y = y2;
		_quad.vertices[3].x = x3; _quad.vertices[3].y = y3;

		_dge.Gfx_RenderQuad(_quad);
	}

	public float GetWidth() 
	{ 
		return _width;
	}
	
	public float GetHeight() 
	{
		return _height; 
	}
	
	public DGERect GetBoundingBox(float x, float y)
	{
		DGERect rect = new DGERect();
		return GetBoundingBox(x, y, rect);
	}
	public DGERect GetBoundingBox(float x, float y, DGERect rect)
	{
		rect.Set(x - _hotX, y - _hotY, x - _hotX + _width, y - _hotY + _height); 
		return rect; 
	}
	
	public DGERect GetBoundingBoxEx(float x, float y, float rot)
	{
		return GetBoundingBoxEx(x, y, rot, 1);
	}
	
	public DGERect GetBoundingBoxEx(float x, float y, float rot, float hscale)
	{
		return GetBoundingBoxEx(x, y, rot, hscale, hscale);
	}
	
	public DGERect GetBoundingBoxEx(float x, float y, float rot, float hscale, float vscale)
	{
		DGERect rect = new DGERect();
		return GetBoundingBoxEx(x, y, rot, hscale, vscale, rect);
	}
	
	public DGERect GetBoundingBoxEx(float x, float y, float rot, float hscale, float vscale, DGERect rect)
	{
		float tx1, ty1, tx2, ty2;
		float sint, cost;

		rect.Clear();
		
		tx1 = -_hotX * hscale;
		ty1 = -_hotY * vscale;
		tx2 = (_width - _hotX) * hscale;
		ty2 = (_height - _hotY) * vscale;

		if (rot != 0.0f)
		{
			cost = FloatMath.cos(rot);
			sint = FloatMath.sin(rot);
				
			rect.Encapsulate(tx1 * cost - ty1 * sint + x, tx1 * sint + ty1 * cost + y);	
			rect.Encapsulate(tx2 * cost - ty1 * sint + x, tx2 * sint + ty1 * cost + y);	
			rect.Encapsulate(tx2 * cost - ty2 * sint + x, tx2 * sint + ty2 * cost + y);	
			rect.Encapsulate(tx1 * cost - ty2 * sint + x, tx1 * sint + ty2 * cost + y);	
		}
		else
		{
			rect.Encapsulate(tx1 + x, ty1 + y);
			rect.Encapsulate(tx2 + x, ty1 + y);
			rect.Encapsulate(tx2 + x, ty2 + y);
			rect.Encapsulate(tx1 + x, ty2 + y);
		}

		return rect;
	}

	public void SetFlip(boolean bX, boolean bY)
	{
		SetFlip(bX, bY, false);
	}
	
	public void SetFlip(boolean bX, boolean bY, boolean bHotSpot)
	{
		float tx, ty;

		if (_bHSFlip && _bXFlip) 
			_hotX = _width - _hotX;
		
		if (_bHSFlip && _bYFlip) 
			_hotY = _height - _hotY;

		_bHSFlip = bHotSpot;
		
		if (_bHSFlip && _bXFlip) 
			_hotX = _width - _hotX;
		
		if (_bHSFlip && _bYFlip) 
			_hotY = _height - _hotY;
		
		if (bX != _bXFlip)
		{
			tx = _quad.vertices[0].tx; _quad.vertices[0].tx = _quad.vertices[1].tx; _quad.vertices[1].tx = tx;
			ty = _quad.vertices[0].ty; _quad.vertices[0].ty = _quad.vertices[1].ty; _quad.vertices[1].ty = ty;
			tx = _quad.vertices[3].tx; _quad.vertices[3].tx = _quad.vertices[2].tx; _quad.vertices[2].tx = tx;
			ty = _quad.vertices[3].ty; _quad.vertices[3].ty = _quad.vertices[2].ty; _quad.vertices[2].ty = ty;

			_bXFlip = !_bXFlip;
		}

		if (bY != _bYFlip)
		{
			tx = _quad.vertices[0].tx; _quad.vertices[0].tx = _quad.vertices[3].tx; _quad.vertices[3].tx = tx;
			ty = _quad.vertices[0].ty; _quad.vertices[0].ty = _quad.vertices[3].ty; _quad.vertices[3].ty = ty;
			tx = _quad.vertices[1].tx; _quad.vertices[1].tx = _quad.vertices[2].tx; _quad.vertices[2].tx = tx;
			ty = _quad.vertices[1].ty; _quad.vertices[1].ty = _quad.vertices[2].ty; _quad.vertices[2].ty = ty;

			_bYFlip = !_bYFlip;
		}
	}
	
	public boolean[] GetFlip()
	{
		boolean[] flip = new boolean[2];
		
		flip[0] = _bXFlip; 
		flip[1] = _bYFlip;
	
		return flip;
	}

	public void SetTexture(int texture)
	{
		float tx1,ty1,tx2,ty2;
		float tw,th;

		_quad.texture = texture;

		if (texture != 0)
		{
			tw = (float) _dge.Texture_GetWidth(texture);
			th = (float) _dge.Texture_GetHeight(texture);
		}
		else
		{
			tw = 1.0f;
			th = 1.0f;
		}

		if (tw != _tex_width || th != _tex_height)
		{
			tx1 = _quad.vertices[0].tx * _tex_width;
			ty1 = _quad.vertices[0].ty * _tex_height;
			tx2 = _quad.vertices[2].tx * _tex_width;
			ty2 = _quad.vertices[2].ty * _tex_height;

			_tex_width = tw;
			_tex_height = th;

			tx1 /= tw; ty1 /= th;
			tx2 /= tw; ty2 /= th;

			_quad.vertices[0].tx = tx1; _quad.vertices[0].ty = ty1; 
			_quad.vertices[1].tx = tx2; _quad.vertices[1].ty = ty1; 
			_quad.vertices[2].tx = tx2; _quad.vertices[2].ty = ty2; 
			_quad.vertices[3].tx = tx1; _quad.vertices[3].ty = ty2; 
		}
	}

	public void SetTextureRect(float x, float y, float w, float h)
	{
		SetTextureRect(x, y, w, h, true);
	}

	public void SetTextureRect(float x, float y, float w, float h, boolean adjSize)
	{
		float tx1, ty1, tx2, ty2;
		boolean bX, bY, bHS;

		_tx = x;
		_ty = y;
		
		if (adjSize)
		{
			_width = w;
			_height = h;
		}

		tx1 = _tx / _tex_width; ty1 = _ty / _tex_height;
		tx2 = (_tx + w) / _tex_width; ty2 = (_ty + h) / _tex_height;

		_quad.vertices[0].tx = tx1; _quad.vertices[0].ty = ty1; 
		_quad.vertices[1].tx = tx2; _quad.vertices[1].ty = ty1; 
		_quad.vertices[2].tx = tx2; _quad.vertices[2].ty = ty2; 
		_quad.vertices[3].tx = tx1; _quad.vertices[3].ty = ty2; 

		bX = _bXFlip; bY = _bYFlip; bHS = _bHSFlip;
		
		_bXFlip = false; _bYFlip = false;
		SetFlip(bX, bY, bHS);
	}
	
	public int GetTexture()
	{ 
		return _quad.texture; 
	}
	
	public DGERect GetTextureRect()
	{ 
		return new DGERect(_tx, _ty, _tx + _width, _ty + _height);
	}
	
	public DGEColor GetColor()
	{
		return GetColor(0);
	}
	
	public DGEColor GetColor(int i)
	{
		return new DGEColor(_quad.vertices[i].r, _quad.vertices[i].g, _quad.vertices[i].b, _quad.vertices[i].a);
	}
	
	public void SetColor(DGEColor color) 
	{
		SetColor(color.r, color.g, color.b, color.a);
	}
	
	public void SetColor(DGEColor color, int i) 
	{
		SetColor(color.r, color.g, color.b, color.a, i);
	}
	
	public void SetColor(float r, float g, float b, float a)
	{
		for (int i = 0; i < 4; i++)
		{
			SetColor(r, g, b, a, i);
		}
	}
	
	public void SetColor(float r, float g, float b, float a, int i)
	{
		_quad.vertices[i].r = r;
		_quad.vertices[i].g = g;
		_quad.vertices[i].b = b;
		_quad.vertices[i].a = a;
	}

	public void SetZ(float z)
	{
		for (int i = 0; i < 4; i++)
		{
			SetZ(z, i);
		}
	}
	
	public void SetZ(float z, int i)
	{
		_quad.vertices[i].z = z;
	}

	public float GetZ()
	{
		return GetZ(0);
	}
	
	public float GetZ(int i) 
	{ 
		return _quad.vertices[i].z;
	}
	
	public void SetBlendMode(int blend) 
	{ 
		_quad.blend = blend; 
	}

	public int GetBlendMode()
	{
		return _quad.blend; 
	}
	
	public void SetHotSpot(float x, float y) 
	{ 
		_hotX = x; 
		_hotY = y; 
	}
	
	public DGEVector GetHotSpot()
	{ 
		return new DGEVector(_hotX, _hotY); 
	}
}
