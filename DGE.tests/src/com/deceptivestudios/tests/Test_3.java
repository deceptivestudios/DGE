package com.deceptivestudios.tests;

import android.util.FloatMath;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.helper.DGEDistort;

public class Test_3 extends Test
{
	private int _texture;
	private DGEDistort _distortion;
	private int _transform = 0;
	private float _time = 0;

	private static int _rows = 16;
	private static int _columns = 16;
	private static float _cw = 512.0f / (_rows - 1);
	private static float _ch = 512.0f / (_columns - 1);
	private static float _meshx = 144;
	private static float _meshy = 44;

	@Override
	public boolean Create()
	{
		_texture = _dge.Texture_Load("swamp-1.png", 115);
		
		if (_texture == 0)
			return false;
		
		_distortion = new DGEDistort(_columns, _rows);
		
		_distortion.SetTexture(_texture);
		_distortion.SetTextureRect(0,  0, 512, 512);
		_distortion.SetBlendMode(DGE.DGE_BLEND_COLORADD | DGE.DGE_BLEND_ALPHABLEND | DGE.DGE_BLEND_ZWRITE);
		
		_distortion.Clear(0, 0, 0, 1);
		
		_meshx = (Width / 2) - 256;
		_meshy = (Height / 2) - 256;
		
		_ready = true;
		
		return true;
	}
	
	@Override
	public boolean Render() 
	{
		_distortion.Render(_meshx, _meshy);

		return true;
	}

	@Override
	public boolean Update(float delta) 
	{
		float rotation, angle, dx, dy;
		
		if (_dge.Input_GetTap())
		{
			if (++_transform > 2)
				_transform = 0;
			
			_distortion.Clear(0, 0, 0, 1);
		}
		
		_time += delta;
		
		switch (_transform)
		{
			case 0:	
			{
				for (int i = 0; i < _rows; i++)
				{
					for (int j = 0; j < _columns;j++)
					{
						_distortion.SetDisplacement(j, i, FloatMath.cos(_time * 10 + (i + j) / 2) * 5, FloatMath.sin(_time * 10 + (i + j) / 2) * 5, DGEDistort.Node);
					}
				}
			} break;

			case 1:	
			{
				for (int i = 0; i < _rows; i++)
				{
					for (int j = 0; j < _columns;j++)
					{
						int col = (int) ((FloatMath.cos(_time * 5 + (i + j) / 2f) + 1f) * 35f);
						DGEColor color = DGEColor.ParseARGB(0xFF << 24 | col << 16 | col << 8 | col);
						
						_distortion.SetDisplacement(j, i, FloatMath.cos(_time * 5f + j / 2f) * 15f, 0, DGEDistort.Node);
						_distortion.SetColor(j, i, color.r, color.g, color.b, color.a);
					}
				}
			} break;

			case 2:	
			{
				for (int i = 0; i < _rows; i++)
				{
					for (int j = 0; j < _columns;j++)
					{
						rotation = (float) Math.sqrt(Math.pow(j - (float) _columns / 2, 2) + Math.pow(i - (float) _rows / 2, 2));
						angle = rotation * (float) Math.cos(_time * 2) * 0.1f;
						
						dx = (float) Math.sin(angle) * (i * _ch - 256) + (float) Math.cos(angle) * (j * _cw - 256);
						dy = (float) Math.cos(angle) * (i * _ch - 256) - (float) Math.sin(angle) * (j * _cw - 256);

						int col = (int) ((FloatMath.cos(rotation + _time * 4f) + 1f) * 40f);
						DGEColor color = DGEColor.ParseARGB(0xFF << 24 | col << 16 | (col / 2) << 8);
						
						_distortion.SetDisplacement(j,i, dx, dy, DGEDistort.Center);
						_distortion.SetColor(j, i, color.r, color.g, color.b, color.a);
					}
				}
			} break;
		}
		
		return true;
	}
}
