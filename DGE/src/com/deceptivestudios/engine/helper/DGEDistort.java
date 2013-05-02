package com.deceptivestudios.engine.helper;

import com.deceptivestudios.engine.DGE;

public class DGEDistort 
{
	public static final int Node = 0;
	public static final int TopLeft = 1;
	public static final int Center = 2;
	
	private static DGE _dge;

	private DGEVertex[] _displacement;
	private DGEMultiple _quad;
	
	private int	_rows, _cols;
	private float _cellw, _cellh;
	private float _tx, _ty, _width, _height;
    
	private DGEDistort()
	{
		_dge = DGE.Interface(DGE.DGE_VERSION);
	}
	
    public DGEDistort(int cols, int rows)
    {
    	this();
    	
    	_rows = rows;
    	_cols = cols;
    	
    	_cellw = _cellh = 0;
    	
    	_quad = new DGEMultiple(4);
    	
    	_quad.texture = 0;
    	_quad.blend = DGE.DGE_BLEND_COLORMUL | DGE.DGE_BLEND_ALPHABLEND | DGE.DGE_BLEND_ZWRITE;
    	
    	_displacement = new DGEVertex[(rows * cols)];

    	for (int i = 0; i < (rows * cols); i++)
    	{
    		_displacement[i] = new DGEVertex();
    	}
    }
    
    public DGEDistort(DGEDistort copy)
    {
    	this();
    	
    	_rows = copy._rows;
    	_cols = copy._cols;
    	
    	_cellw = copy._cellw;
    	_cellh = copy._cellh;
    	
    	_quad = new DGEMultiple(copy._quad);
    	
    	_displacement = new DGEVertex[(_rows * _cols)];

    	for (int i = 0; i < (_rows * _cols); i++)
    	{
			_displacement[i] = new DGEVertex(copy._displacement[i]);
    	}
    }

    public void Render(float x, float y)
    {
    	for (int j = 0; j < _rows - 1; j++)
    	{
    		for (int i = 0; i < _cols - 1; i++)
    		{
    			int idx = j * _cols + i;
    			
    			if (_displacement[idx] == null)
    				continue;

    			_quad.vertices[0].tx =    _displacement[idx].tx;
    			_quad.vertices[0].ty =    _displacement[idx].ty;
    			_quad.vertices[0].x = x + _displacement[idx].x;
    			_quad.vertices[0].y = y + _displacement[idx].y;
    			_quad.vertices[0].z =     _displacement[idx].z;
    			_quad.vertices[0].r =     _displacement[idx].r;
    			_quad.vertices[0].g =     _displacement[idx].g;
    			_quad.vertices[0].b =     _displacement[idx].b;
    			_quad.vertices[0].a =     _displacement[idx].a;

    			_quad.vertices[1].tx =    _displacement[idx + 1].tx;
    			_quad.vertices[1].ty =    _displacement[idx + 1].ty;
    			_quad.vertices[1].x = x + _displacement[idx + 1].x;
    			_quad.vertices[1].y = y + _displacement[idx + 1].y;
    			_quad.vertices[1].z =     _displacement[idx + 1].z;
    			_quad.vertices[1].r =     _displacement[idx + 1].r;
    			_quad.vertices[1].g =     _displacement[idx + 1].g;
    			_quad.vertices[1].b =     _displacement[idx + 1].b;
    			_quad.vertices[1].a =     _displacement[idx + 1].a;

    			_quad.vertices[2].tx =    _displacement[idx + _cols + 1].tx;
    			_quad.vertices[2].ty =    _displacement[idx + _cols + 1].ty;
    			_quad.vertices[2].x = x + _displacement[idx + _cols + 1].x;
    			_quad.vertices[2].y = y + _displacement[idx + _cols + 1].y;
    			_quad.vertices[2].z =     _displacement[idx + _cols + 1].z;
    			_quad.vertices[2].r =     _displacement[idx + _cols + 1].r;
    			_quad.vertices[2].g =     _displacement[idx + _cols + 1].g;
    			_quad.vertices[2].b =     _displacement[idx + _cols + 1].b;
    			_quad.vertices[2].a =     _displacement[idx + _cols + 1].a;

    			_quad.vertices[3].tx =    _displacement[idx + _cols].tx;
    			_quad.vertices[3].ty =    _displacement[idx + _cols].ty;
    			_quad.vertices[3].x = x + _displacement[idx + _cols].x;
    			_quad.vertices[3].y = y + _displacement[idx + _cols].y;
    			_quad.vertices[3].z =     _displacement[idx + _cols].z;
    			_quad.vertices[3].r =     _displacement[idx + _cols].r;
    			_quad.vertices[3].g =     _displacement[idx + _cols].g;
    			_quad.vertices[3].b =     _displacement[idx + _cols].b;
    			_quad.vertices[3].a =     _displacement[idx + _cols].a;

    			_dge.Gfx_RenderQuad(_quad);
    		}
    	}
    }
    
    public void Clear()
    {
    	Clear(1, 1, 1, 1);
	}
    
    public void Clear(float r, float g, float b, float a)
    {
    	Clear(r, g, b, a, 0.5f);
    }
    
    public void Clear(float r, float g, float b, float a, float z)
    {
    	for(int j = 0; j < _rows; j++)
    	{
    		for(int i = 0; i < _cols; i++)
    		{
    			_displacement[j * _cols + i].x = i * _cellw;
    			_displacement[j * _cols + i].y = j * _cellh;
    			_displacement[j * _cols + i].z = z;
    			
    			_displacement[j * _cols + i].r = r;
    			_displacement[j * _cols + i].g = g;
    			_displacement[j * _cols + i].b = b;
    			_displacement[j * _cols + i].a = a;
    		}
    	}
    }

    public void SetTexture(int texture)
    {
    	_quad.texture = texture;
    }
    
    public void SetTextureRect(float x, float y, float w, float h)
    {
    	float tw, th;

    	_tx = x; _ty = y; _width = w; _height = h;

    	if (_quad.texture != 0)
    	{
    		tw = (float) _dge.Texture_GetWidth(_quad.texture);
    		th = (float) _dge.Texture_GetHeight(_quad.texture);
    	}
    	else
    	{
    		tw = w;
    		th = h;
    	}

    	_cellw = w / (_cols - 1);
    	_cellh = h / (_rows - 1);

    	for(int j = 0; j < _rows; j++)
    	{
    		for(int i = 0; i < _cols; i++)
    		{
    			_displacement[j * _cols + i].tx = (x + i * _cellw) / tw;
    			_displacement[j * _cols + i].ty = (y + j * _cellh) / th;

    			_displacement[j * _cols + i].x = i * _cellw;
    			_displacement[j * _cols + i].y = j * _cellh;
    		}
    	}
    }
    
    public void SetBlendMode(int blend)
    {
    	_quad.blend = blend;
    }
    
    public void SetZ(int col, int row, float z)
    {
    	if (row < _rows && col < _cols) 
    		_displacement[row * _cols + col].z = z;
    }
    
    public void SetColor(int col, int row, float r, float g, float b, float a)
    {
    	if (row < _rows && col < _cols) 
    	{
    		_displacement[row * _cols + col].r = r;
    		_displacement[row * _cols + col].g = g;
    		_displacement[row * _cols + col].b = b;
    		_displacement[row * _cols + col].a = a;
    	}
    }
    
    public void SetDisplacement(int col, int row, float dx, float dy, int ref)
    {
    	if (row < _rows && col < _cols)
    	{
    		int offset = row * _cols + col;
    		
    		switch (ref)
    		{
    			case Node:		
    				dx += (float) col * _cellw; 
    				dy += (float) row * _cellh;
    				break;
    				
    			case Center:	
    				dx += _cellw * (float) (_cols - 1) / 2f;
    				dy += _cellh * (float) (_rows - 1) / 2f; 
    				break;
    				
    			case TopLeft:	
    				break;
    		}

    		_displacement[offset].x = dx;
    		_displacement[offset].y = dy;
    	}    
    }

    public int GetTexture()
    {
    	return _quad.texture;
    }
    
    public DGERect GetTextureRect()
    {
    	return new DGERect(_tx, _ty, _width, _height);
    }
    
    public int GetBlendMode()
    {
    	return _quad.blend;
    }
    
    public float GetZ(int col, int row)
    {
    	if (row < _rows && col < _cols) 
    		return _displacement[row * _cols + col].z;
    	else 
    		return 0.0f;
    }
    
    public DGEColor GetColor(int col, int row)
    {
    	int offset = row * _cols + col;
    	
    	if(row < _rows && col < _cols) 
    		return new DGEColor(_displacement[offset].r, _displacement[offset].g, _displacement[offset].b, _displacement[offset].a);
    	else 
    		return new DGEColor();
    }
    
    public DGEVector GetDisplacement(int col, int row, int ref)
    {
    	float dx = 0, dy = 0;
    	
    	if (row < _rows && col < _cols)
    	{
    		switch (ref)
    		{
    			case Node:		
    				dx = _displacement[row * _cols + col].x - col * _cellw;
    				dy = _displacement[row * _cols + col].y - row * _cellh;
					break;

    			case Center:	
    				dx = _displacement[row * _cols + col].x - _cellw * (_cols - 1) / 2;
    				dy = _displacement[row * _cols + col].y - _cellh * (_rows - 1) / 2;
					break;

    			case TopLeft:	
    				dx = _displacement[row * _cols + col].x;
    				dy = _displacement[row * _cols + col].y;
    				break;
    		}
    	}
	
    	return new DGEVector(dx, dy);
    }

	public int GetRows() 
	{
		return _rows; 
	}
	
	public int GetCols() 
	{ 
		return _cols; 
	}
}
