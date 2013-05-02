package com.deceptivestudios.tests;

import java.util.Date;

import android.util.FloatMath;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.helper.DGEDistort;
import com.deceptivestudios.engine.helper.DGEFont;
import com.deceptivestudios.engine.helper.DGESprite;

public class Test_6 extends Test
{
	private class StarObject
	{
		float x, y;
		float r, g, b, a;
		float scale;
	}
	
	private int _numStars, _seaDivisions;
	private float _skyHeight, _starsHeight, _orbitsRadius;
	private StarObject[] _stars;
	private float[] _seaPhase;
	
	private int _state;
	private float[] _speed = { 0.0f, 0.1f, 0.2f, 0.4f, 0.8f, 1.6f, 3.2f, 6.4f, 12.8f, 25.6f }; 
	private float _time;
	
	private int[] _sequence = { 0, 0, 1, 2, 2, 2, 1, 0, 0 };
	private int   _sequenceId;
	private float _sequenceResidue;
	
	private static final int SkyTop = 0; 
	private static final int SkyBottom = 1; 
	private static final int SeaTop = 2; 
	private static final int SeaBottom = 3; 
	
	private DGEColor[][] _colors = {
		{ DGEColor.ParseARGB("FF15092A"), DGEColor.ParseARGB("FF6C6480"), DGEColor.ParseARGB("FF89B9D0") }, // sky top
		{ DGEColor.ParseARGB("FF303E57"), DGEColor.ParseARGB("FFAC7963"), DGEColor.ParseARGB("FFCAD7DB") }, // sky bottom
		{ DGEColor.ParseARGB("FF3D546B"), DGEColor.ParseARGB("FF927E76"), DGEColor.ParseARGB("FF86A2AD") }, // sea top
		{ DGEColor.ParseARGB("FF1E394C"), DGEColor.ParseARGB("FF2F4E64"), DGEColor.ParseARGB("FF2F4E64") }  // sea bottom
	};
	
	private int _texture;
	private DGESprite _sky, _sun, _moon, _glow, _seaGlow, _star;
	private DGEDistort _sea;
	private float _sunX, _sunY, _sunScale, _sunGlowScale;
	private float _moonX, _moonY, _moonScale, _moonGlowScale;
	private float _seaGlowX, _seaGlowScaleX, _seaGlowScaleY;

	private DGEColor[] _colorBackground;
	private DGEColor _colorWhite = new DGEColor(1,1,1,1);
	private DGEColor _colorSeaGlow = new DGEColor();
	private DGEColor _colorSun = new DGEColor(), _colorSunGlow = new DGEColor();
	private DGEColor _colorMoon = new DGEColor(), _colorMoonGlow = new DGEColor();
	
	@Override
	public boolean Create() 
	{
		return (_ready = InitialiseSimulation());
	}

	@Override
	public boolean Render()
	{
		if (!_ready)
			return Create();
		
		int hrs, mins, secs;
		float remainder;
		
		hrs = (int) Math.floor(_time);
		remainder = (_time - hrs) * 60.0f;
		mins = (int) Math.floor(remainder);
		secs = (int) Math.floor((remainder - mins) * 60.0f);
		
		RenderSimulation();
		
		Font.Render(Width - 5, 5, DGEFont.Right, "%02d:%02d:%02d", hrs, mins, secs);
		
		return true;
	}

	@Override
	public boolean Update(float delta) 
	{
		if (_dge.Input_GetDoubleTap())
		{
			if (_state < (_speed.length - 1))
				_state++;
		}
		
		if (_dge.Input_GetTap())
		{
			if (_state > 0)
				_state--;
		}
		
		UpdateSimulation(delta);

		return true;
	}

	private float GetTime()
	{
		Date now = new Date();
		float value;
		
		value = now.getSeconds();
		value = now.getMinutes() + (value / 60f);
		value = now.getHours() + (value / 60f);
		
		return value;
	}
	
	public boolean InitialiseSimulation()
	{
		_texture = _dge.Texture_Load("objects.png");
		
		if (_texture == 0)
			return false;
		
		_state = 0;
		_numStars = 100;
		_seaDivisions = 16;
		
		_skyHeight = ((float) Height * 0.6f);
		_starsHeight = ((float) Height * 0.9f);
		_orbitsRadius = ((float) Width * 0.33f);
		
		_sky = new DGESprite(0, 0, 0, Width, Height);
		
		_sea = new DGEDistort(_seaDivisions, _seaDivisions);
		_sea.SetTextureRect(0, 0, Width, Height - _skyHeight);
		
		_sun = new DGESprite(_texture, 81, 0, 114, 114);
		_sun.SetHotSpot(57, 57);
		
		_moon = new DGESprite(_texture, 0, 0, 81, 81);
		_moon.SetHotSpot(40, 40);
		
		_star = new DGESprite(_texture, 72, 81, 9, 9);
		_star.SetHotSpot(5, 5);
		
		_glow = new DGESprite(_texture, 128, 128, 128, 128);
		_glow.SetHotSpot(64, 64);
		_glow.SetBlendMode(DGE.DGE_BLEND_COLORADD | DGE.DGE_BLEND_ALPHABLEND | DGE.DGE_BLEND_NOZWRITE);
		
		_seaGlow = new DGESprite(_texture, 128, 224, 128, 32);
		_seaGlow.SetHotSpot(64, 0);
		_seaGlow.SetBlendMode(DGE.DGE_BLEND_COLORADD | DGE.DGE_BLEND_ALPHAADD | DGE.DGE_BLEND_NOZWRITE);
		
		_stars = new StarObject[_numStars];
		
		for (int i = 0; i < _numStars; i++)
		{
			_stars[i] = new StarObject();
			
			_stars[i].x = _dge.Random_Float(0, Width);
			_stars[i].y = _dge.Random_Float(0, _starsHeight);
			_stars[i].r = _stars[i].g = _stars[i].b = _stars[i].a = 1f; 
			_stars[i].scale = _dge.Random_Float(0.5f, 1.5f);
		}
		
		_seaPhase = new float[_seaDivisions];

		for (int i = 0; i < _seaDivisions; i++)
		{
			_seaPhase[i] = i + _dge.Random_Float(-15.0f, 15.0f);
		}
		
		_colorBackground = new DGEColor[4];
		
		for (int i = 0; i < 4; i++)
		{
			_colorBackground[i] = new DGEColor();
		}
		
		return true;
	}
	
	public void UpdateSimulation(float delta)
	{
		float zenith, a, dy, time;
		float posX, s1, s2;
		DGEColor color1 = new DGEColor();
		DGEColor color2 = new DGEColor();
		
		float cellw = (float) (Width / (_seaDivisions - 1));
		
		if (_speed[_state] == 0.0f)
		{
			_time = GetTime();
		}
		else
		{
			_time += delta * _speed[_state];
			
			if (_time >= 24f)
				_time -= 24f;
		}
		
		_sequenceId = (int) (_time / 3f);
		_sequenceResidue = (_time / 3f) - (float) _sequenceId;
		
		zenith = -(_time / 12.0f * DGE.M_PI - DGE.M_PI_2);
		
		for (int i = 0; i < 4; i++)
		{
			color1.set(_colors[i][_sequence[_sequenceId]]);
			color2.set(_colors[i][_sequence[_sequenceId + 1]]);
			
			_colorBackground[i].set(color2.mulLocal(_sequenceResidue).addLocal(color1.mulLocal(1f - _sequenceResidue)));
		}
		
		if (_sequenceId >= 6 || _sequenceId < 2)
		{
			for (int i = 0; i < _numStars; i++)
			{
				a = 1.0f - _stars[i].y / _starsHeight;
				a *= _dge.Random_Float(0.6f, 1.0f);
				
				if (_sequenceId >= 6)
					a *= FloatMath.sin((_time - 18.0f) / 6.0f * DGE.M_PI_2);
				else
					a *= FloatMath.sin((1.0f - _time / 6.0f) * DGE.M_PI_2);
				
				_stars[i].a = a;
			}
		}
		
		if (_sequenceId == 2)
			a = FloatMath.sin(_sequenceResidue * DGE.M_PI_2);
		else if (_sequenceId == 5)
			a = FloatMath.cos(_sequenceResidue * DGE.M_PI_2);
		else if (_sequenceId > 2 && _sequenceId < 5)
			a = 1.0f;
		else
			a = 0.0f;
		
		color1.set("FFEAE1BE");
		color2.set(_colorWhite);
		_colorSun.set(color1.mulLocal(1f - a).addLocal(color2.mulLocal(a)));
		
		a = (FloatMath.cos(_time / 6.0f * DGE.M_PI) + 1.0f) / 2.0f;
		
		if (_sequenceId >= 2 && _sequenceId <= 6)
		{
			_colorSunGlow.set(_colorWhite).mulLocal(a);
			_colorSunGlow.a = 1.0f;
		}
		else
		{
			_colorSunGlow.set(0, 0, 0, 1); // black
		}
		
		_sunX = Width * 0.5f + FloatMath.cos(zenith) * _orbitsRadius;
		_sunY = _skyHeight * 1.2f + FloatMath.sin(zenith) * _orbitsRadius;
		_sunScale = 1.0f - 0.3f * FloatMath.sin((_time - 6.0f) / 12.0f * DGE.M_PI);
		_sunGlowScale = 3.0f * (1.0f - a) + 3.0f;
		
		if (_sequenceId >= 6)
			a = FloatMath.sin((_time - 18.0f) / 6.0f * DGE.M_PI_2);
		else
			a = FloatMath.sin((1.0f - _time / 6.0f) * DGE.M_PI_2);
		
		color1.set("20FFFFFF");
		color2.set(_colorWhite);
		_colorMoon.set(color1.mulLocal(1f - a)).addLocal(color2.mulLocal(a));
		_colorMoonGlow.set(_colorWhite, 0.5f * a);
		
		_moonX = Width * 0.5f + FloatMath.cos(zenith - DGE.M_PI) * _orbitsRadius;
		_moonY = _skyHeight * 1.2f + FloatMath.sin(zenith - DGE.M_PI) * _orbitsRadius;
		_moonScale = 1.0f - 0.3f * FloatMath.sin((_time + 6.0f) / 12.0f * DGE.M_PI);
		_moonGlowScale = a * 0.4f + 0.5f;
		
		if (_time > 19.0f || _time < 4.5f)
		{
			a = 0.2f;
			
			if (_time > 19.0f && _time < 20.0f)
				a *= (_time - 19.0f);
			else if (_time > 3.5f && _time < 4.5f)
				a *= 1.0f - (_time - 3.5f);
			
			_colorSeaGlow.set(_colorMoonGlow);
			_colorSeaGlow.a = a;
			
			_seaGlowX = _moonX;
			_seaGlowScaleX = _moonGlowScale * 3.0f;
			_seaGlowScaleY = _moonGlowScale * 2.0f;
		}
		else if (_time > 6.5f && _time < 19.0f)
		{
			a = 0.3f;
			
			if (_time < 7.5f)
				a *= (_time - 6.5f);
			else if (_time > 18.0f)
				a *= 1.0f - (_time - 18.0f);
			
			_colorSeaGlow.set(_colorSunGlow);
			_colorSeaGlow.a = a;
			
			_seaGlowX = _sunX;
			_seaGlowScaleX = _sunGlowScale;
			_seaGlowScaleY = _sunGlowScale * 0.6f;
		}
		else
		{
			_colorSeaGlow.a = 0.0f;
		}
		
		for (int i = 1; i < _seaDivisions - 1; i++)
		{
			a = (float) i / (float) (_seaDivisions - 1);
			
			color1.set(_colorBackground[SeaTop]).mulLocal(1.0f - a);
			color2.set(_colorBackground[SeaBottom]).mulLocal(a);
			
			color1.addLocal(color2);
			time = 2.0f * _dge.Timer_GetTime();
			
			a *= 20;
			
			for (int j = 0; j < _seaDivisions; j++)
			{
				dy = a * FloatMath.sin(_seaPhase[i] + ((float) j / ((float) _seaDivisions - 1f) - 0.5f) * DGE.M_PI * 16.0f - time);
				
				_sea.SetColor(j, i, color1.r, color1.g, color1.b, color1.a);
				_sea.SetDisplacement(j, i, 0.0f, dy, DGEDistort.Node);
			}
		}
		
		for (int j = 0; j < _seaDivisions; j++)
		{
			color1.set(_colorBackground[SeaTop]);
			color2.set(_colorBackground[SeaBottom]);
			
			_sea.SetColor(j, 0, color1.r, color1.g, color1.b, color1.a);
			_sea.SetColor(j, (_seaDivisions - 1), color2.r, color2.g, color2.b, color2.a);
		}
		
		if (_time > 19.0f || _time < 5.0f) // moon
		{
			a = 0.12f; // intensity
			
			if (_time > 19.0f && _time < 20.0f) 
				a *= (_time - 19.0f);
			else if (_time > 4.0f && _time < 5.0f) 
				a *= 1.0f - (_time - 4.0f);
			
			posX = _moonX;
		}
		else if (_time > 7.0f && _time < 17.0f) // sun
		{
			a = 0.14f; // intensity
			
			if (_time < 8.0f) 
				a *= (_time - 7.0f);
			else if (_time > 16.0f) 
				a *= 1.0f - (_time - 16.0f);
			
			posX = _sunX;
		}
		else
		{
			posX = 0;
			a = 0.0f;
		}

		if (a != 0.0f)
		{
			int k = (int) Math.floor(posX / cellw);
			
			s1 = (1.0f - (posX - (float) k * cellw) / cellw);
			s2 = (1.0f - (((float) k + 1f) * cellw - posX) / cellw);

			if (s1 > 0.7f) 
				s1 = 0.7f;
			
			if (s2 > 0.7f) 
				s2 = 0.7f;

			s1 *= a;
			s2 *= a;
		
			for(int i = 0; i < _seaDivisions; i += 2)
			{
				a = FloatMath.sin(((float) i) / (float) (_seaDivisions - 1) * DGE.M_PI_2);

				color2.set(_colorSun).mulLocal(s1).mulLocal(1.0f - a);
				color1.set(_sea.GetColor(k, i)).addLocal(color2);
				color1.clamp();
				
				_sea.SetColor(k, i, color1.r, color1.g, color1.b, color1.a);
				
				color2.set(_colorSun).mulLocal(s2).mulLocal(1.0f - a);
				color1.set(_sea.GetColor(k + 1, i)).addLocal(color2);
				color1.clamp();
				
				_sea.SetColor(k + 1, i, color1.r, color1.g, color1.b, color1.a);
			}
		}
	}
	
	public void RenderSimulation()
	{
		_sky.SetColor(_colorBackground[SkyTop], 0); _sky.SetColor(_colorBackground[SkyTop], 1);
		_sky.SetColor(_colorBackground[SkyBottom], 2); _sky.SetColor(_colorBackground[SkyBottom], 3);
		_sky.Render(0, 0);
		
		if (_sequenceId >= 6 || _sequenceId < 2)
		{
			for (int i = 0; i < _numStars; i++)
			{
				_star.SetColor(_stars[i].r, _stars[i].g, _stars[i].b, _stars[i].a);
				_star.RenderEx(_stars[i].x, _stars[i].y, 0.0f, _stars[i].scale);
			}
		}
		
		_glow.SetColor(_colorSunGlow);
		_glow.RenderEx(_sunX, _sunY, 0.0f, _sunGlowScale);
		_sun.SetColor(_colorSun);
		_sun.RenderEx(_sunX, _sunY, 0.0f, _sunScale);
		
		_glow.SetColor(_colorMoonGlow);
		_glow.RenderEx(_moonX, _moonY, 0.0f, _moonGlowScale);
		_moon.SetColor(_colorMoon);
		_moon.RenderEx(_moonX, _moonY, 0.0f, _moonScale);
		
		_sea.Render(0, _skyHeight);
		_seaGlow.SetColor(_colorSeaGlow);
		_seaGlow.RenderEx(_seaGlowX, _skyHeight, 0.0f, _seaGlowScaleX, _seaGlowScaleY);
	}
}
