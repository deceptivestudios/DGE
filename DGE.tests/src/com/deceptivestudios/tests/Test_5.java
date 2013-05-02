package com.deceptivestudios.tests;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.helper.DGEFont;
import com.deceptivestudios.engine.helper.DGESprite;

public class Test_5 extends Test 
{
	private class SprObject
	{
		public float x, y;
		public float dx, dy;
		public float scale, rotation;
		public float dscale, drotation;
		public DGEColor color;
	}
	
	private int _blend = 0;
	private int _maximumObjects = 2000;
	private int _minimumObjects = 100;
	private int _totalObjects = 400;
	private int _step = 100;
	private SprObject[] _objects;
	private int _texture, _bgTexture;
	private DGESprite _sprite, _bgSprite;
	
	private static int[] _blends = {
		DGE.DGE_BLEND_COLORMUL | DGE.DGE_BLEND_ALPHABLEND | DGE.DGE_BLEND_NOZWRITE,
		DGE.DGE_BLEND_COLORADD | DGE.DGE_BLEND_ALPHABLEND | DGE.DGE_BLEND_NOZWRITE,
		DGE.DGE_BLEND_COLORMUL | DGE.DGE_BLEND_ALPHABLEND | DGE.DGE_BLEND_NOZWRITE,
		DGE.DGE_BLEND_COLORMUL | DGE.DGE_BLEND_ALPHAADD   | DGE.DGE_BLEND_NOZWRITE,
		DGE.DGE_BLEND_COLORMUL | DGE.DGE_BLEND_ALPHABLEND | DGE.DGE_BLEND_NOZWRITE
	};

	private static DGEColor[][] _colors = {
		{ DGEColor.ParseARGB("FFFFFFFF"), DGEColor.ParseARGB("FFFFE080"), DGEColor.ParseARGB("FF80A0FF"), DGEColor.ParseARGB("FFA0FF80"), DGEColor.ParseARGB("FFFF80A0") },
		{ DGEColor.ParseARGB("FF000000"), DGEColor.ParseARGB("FF303000"), DGEColor.ParseARGB("FF000060"), DGEColor.ParseARGB("FF006000"), DGEColor.ParseARGB("FF600000") },
		{ DGEColor.ParseARGB("80FFFFFF"), DGEColor.ParseARGB("80FFE080"), DGEColor.ParseARGB("8080A0FF"), DGEColor.ParseARGB("80A0FF80"), DGEColor.ParseARGB("80FF80A0") },
		{ DGEColor.ParseARGB("80FFFFFF"), DGEColor.ParseARGB("80FFE080"), DGEColor.ParseARGB("8080A0FF"), DGEColor.ParseARGB("80A0FF80"), DGEColor.ParseARGB("80FF80A0") },
		{ DGEColor.ParseARGB("40202020"), DGEColor.ParseARGB("40302010"), DGEColor.ParseARGB("40102030"), DGEColor.ParseARGB("40203010"), DGEColor.ParseARGB("40102030") },
	};
	
	private void SetBlend(int blend)
	{
		if (blend > 4)
			blend = 0;
		
		_blend = blend;
		_sprite.SetBlendMode(_blends[blend]);
		
		for (int i = 0; i < _maximumObjects; i++)
		{
			_objects[i].color = _colors[blend][_dge.Random_Int(0, 4)];
		}
	}

	@Override
	public boolean Create() 
	{
		_texture = _dge.Texture_Load("zazaka.png");
		
		if (_texture == 0)
			return false;
		
		_sprite = new DGESprite(_texture, 0, 0, 64, 64);
		_sprite.SetHotSpot(32, 32);
		
		_bgTexture = _dge.Texture_Load("bg2.png");
		
		_bgSprite = new DGESprite(_bgTexture, 0, 0, Width, Height);
		_bgSprite.SetBlendMode(DGE.DGE_BLEND_COLORADD | DGE.DGE_BLEND_ALPHABLEND | DGE.DGE_BLEND_NOZWRITE);
		_bgSprite.SetColor(0, 0, 0, 1, 0);
		_bgSprite.SetColor(0, 0, 0, 1, 1);
		_bgSprite.SetColor(0, 0, 0.125f, 1, 2);
		_bgSprite.SetColor(0, 0, 0.125f, 1, 3);
		
		_objects = new SprObject[_maximumObjects];
		
		for (int i = 0; i < _maximumObjects; i++)
		{
			_objects[i] = new SprObject();
			
			_objects[i].x = _dge.Random_Float(0, Width);
			_objects[i].y = _dge.Random_Float(0, Height);
			_objects[i].dx = _dge.Random_Float(-200, 200);
			_objects[i].dy = _dge.Random_Float(-200, 200);
			_objects[i].scale = _dge.Random_Float(0.5f, 2.0f);
			_objects[i].dscale = _dge.Random_Float(-1.0f, 1.0f);
			_objects[i].rotation = _dge.Random_Float(0, DGE.M_PI * 2f);
			_objects[i].drotation = _dge.Random_Float(-1.0f, 1.0f);
		}

		SetBlend(0);
		
		_ready = true;
		
		return true;
	}
	
	@Override
	public boolean Render() 
	{
		_bgSprite.Render(0, 0);
		
		for (int i = 0; i < _totalObjects; i++)
		{
			_sprite.SetColor(_objects[i].color);
			_sprite.RenderEx(_objects[i].x, _objects[i].y, _objects[i].rotation, _objects[i].scale);
		}
		
		Font.Render(5, Height - 5, DGEFont.Left | DGEFont.Bottom, "Total Objects: %d", _totalObjects);

		return true;
	}

	@Override
	public boolean Update(float delta) 
	{
		if (_dge.Input_GetTap())
			SetBlend(++_blend);
		
		if (_dge.Input_GetDoubleTap())
		{
			if (_totalObjects == _maximumObjects || _totalObjects == _minimumObjects)
				_step = -_step;
			
			_totalObjects += _step;
		}
		
		for (int i = 0; i < _totalObjects; i++)
		{
			_objects[i].x += _objects[i].dx * delta;
			
			if ((_objects[i].x > Width && _objects[i].dx > 0) ||
				(_objects[i].x < 0 && _objects[i].dx < 0)) 
				_objects[i].dx = -_objects[i].dx;
			
			_objects[i].y += _objects[i].dy * delta;
			
			if ((_objects[i].y > Height && _objects[i].dy > 0) || 
				(_objects[i].y < 0 && _objects[i].dy < 0)) 
				_objects[i].dy = -_objects[i].dy;
			
			_objects[i].scale += _objects[i].dscale * delta;
			
			if ((_objects[i].scale > 2 && _objects[i].dscale > 0) || 
				(_objects[i].scale < 0.5 && _objects[i].dscale < 0)) 
				_objects[i].dscale = -_objects[i].dscale;
			
			_objects[i].rotation += _objects[i].drotation * delta;
		}
		
		return true;
	}
}
