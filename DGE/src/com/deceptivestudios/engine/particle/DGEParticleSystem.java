package com.deceptivestudios.engine.particle;

import java.io.IOException;
import android.util.FloatMath;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.helper.DGERect;
import com.deceptivestudios.engine.helper.DGESprite;
import com.deceptivestudios.engine.helper.DGEVector;

public class DGEParticleSystem 
{
	public DGEParticleSystemInfo Info;
	
	private DGEParticleSystem()
	{
		_dge = DGE.Interface(DGE.DGE_VERSION);

		_vecLocation = new DGEVector();
		_vecPrevLocation = new DGEVector();
		
		_tx = _ty = 0;
		_scale = 1.0f;

		_emissionResidue = 0.0f;
		_particlesAlive = 0;
		_age = -2.0f;

		_rectBoundingBox = new DGERect();
		_rectBoundingBox.Clear();
		
		_updateBoundingBox = false;
	}
	
	public DGEParticleSystem(String filename, DGESprite sprite)
	{
		this();
		
		try
		{
			Info = new DGEParticleSystemInfo(filename);
		}
		catch (IOException e)
		{
			return;
		} 
		
		Info.Sprite = sprite;
	}
	
	public DGEParticleSystem(DGEParticleSystemInfo psi)
	{
		this();
		
		Info = new DGEParticleSystemInfo(psi);
	}
	
	public DGEParticleSystem(DGEParticleSystem ps)
	{
		Info = new DGEParticleSystemInfo(ps.Info);
		
		_vecLocation = new DGEVector(ps._vecLocation);
		_vecPrevLocation = new DGEVector(ps._vecPrevLocation);
		
		_tx = ps._tx;
		_ty = ps._ty;
		
		_scale = ps._scale;

		_emissionResidue = ps._emissionResidue;
		_particlesAlive = ps._particlesAlive;
		_age = ps._emissionResidue;

		_rectBoundingBox = new DGERect(ps._rectBoundingBox.p1, ps._rectBoundingBox.p2);
		_updateBoundingBox = ps._updateBoundingBox;
	}

	public void Render()
	{
		DGEColor savedColor = Info.Sprite.GetColor();
		DGEColor color = new DGEColor(savedColor, 1);
		
		for(int i = 0; i < _particlesAlive; i++)
		{
			if (_particles[i] == null)
				continue;
			
			if (Info.ColorStart.r < 0)
			{
				Info.Sprite.SetColor(color);
			}
			else
			{
				Info.Sprite.SetColor(_particles[i].Color);
			}
			
			Info.Sprite.RenderEx(_particles[i].Location.x * _scale + _tx, _particles[i].Location.y * _scale + _ty, _particles[i].Spin * _particles[i].Age, _particles[i].Size * _scale);
		}

		Info.Sprite.SetColor(savedColor);
	}

	public void FireAt(float x, float y)
	{
		Stop();
		MoveTo(x,y);
		Fire();
	}

	public void Fire()
	{
		if (Info.Lifetime == -1.0f) 
			_age = -1.0f;
		else 
			_age = 0.0f;
	}
	
	public boolean IsFiring()
	{
		return (_age != -2.0f);
	}

	public void Stop()
	{
		Stop(false);
	}
	
	public void Stop(boolean bKillParticles)
	{
		_age = -2.0f;
		
		if (bKillParticles) 
		{
			_particlesAlive = 0;
			_rectBoundingBox.Clear();
		}
	}

	public void Update(float fDeltaTime)
	{
		float M_PI_2 = 1.57079632679489661923f;
		int i = 0;
		float ang;
		DGEVector vecAccel, vecAccel2;

		if (_age >= 0)
		{
			_age += fDeltaTime;
			
			if (_age >= Info.Lifetime) 
				_age = -2.0f;
		}

		// update all alive particles

		if (_updateBoundingBox) 
			_rectBoundingBox.Clear();
		
		for (i = 0; i < _particlesAlive; i++)
		{
			_particles[i].Age += fDeltaTime;
			
			if (_particles[i].Age >= _particles[i].TerminalAge)
			{
				_particlesAlive--;
				
				_particles[i] = _particles[_particlesAlive];
				_particles[_particlesAlive] = null;
				
				i--;

				continue;
			}

			vecAccel = _particles[i].Location.subtract(_vecLocation);
			vecAccel.normalize();
			
			vecAccel2 = new DGEVector(vecAccel);
			vecAccel = vecAccel.multiply(_particles[i].RadialAccel);

			ang = vecAccel2.x;
			vecAccel2.x = -vecAccel2.y;
			vecAccel2.y = ang;

			vecAccel2 = vecAccel2.multiply(_particles[i].TangentialAccel);
			
			_particles[i].Velocity = _particles[i].Velocity.add((vecAccel.add(vecAccel2).multiply(fDeltaTime)));
			_particles[i].Velocity.y += _particles[i].Gravity * fDeltaTime;

			_particles[i].Location = _particles[i].Location.add(_particles[i].Velocity.multiply(fDeltaTime));

			_particles[i].Spin += _particles[i].SpinDelta * fDeltaTime;
			_particles[i].Size += _particles[i].SizeDelta * fDeltaTime;
			_particles[i].Color.addLocal(_particles[i].ColorDelta.mul(fDeltaTime));

			if (_updateBoundingBox) 
				_rectBoundingBox.Encapsulate(_particles[i].Location.x, _particles[i].Location.y);
		}

		// generate new particles

		if (_age != -2.0f)
		{
			float fParticlesNeeded = ((float) Info.Emission) * fDeltaTime + _emissionResidue;
			int nParticlesCreated = (int) Math.ceil(fParticlesNeeded);
			
			_emissionResidue = fParticlesNeeded - nParticlesCreated;
			
			for (i = 0; i < nParticlesCreated; i++)
			{
				if (_particlesAlive >= MaximumParticles) 
					break;
				
				_particles[_particlesAlive] = new DGEParticle();

				_particles[_particlesAlive].Age = 0.0f;
				_particles[_particlesAlive].TerminalAge = _dge.Random_Float(Info.ParticleLifeMin, Info.ParticleLifeMax);

				_particles[_particlesAlive].Location = _vecPrevLocation.add(_vecLocation.subtract(_vecPrevLocation).multiply(_dge.Random_Float(0.0f, 1.0f)));
				_particles[_particlesAlive].Location.x += _dge.Random_Float(-2.0f, 2.0f);
				_particles[_particlesAlive].Location.y += _dge.Random_Float(-2.0f, 2.0f);

				ang = Info.Direction - M_PI_2 + _dge.Random_Float(0, Info.Spread) - Info.Spread / 2.0f;
				
				if (Info.Relative) 
					ang += (_vecPrevLocation.subtract(_vecLocation)).angle() + M_PI_2;
				
				_particles[_particlesAlive].Velocity.x = FloatMath.cos(ang);
				_particles[_particlesAlive].Velocity.y = FloatMath.sin(ang);
				_particles[_particlesAlive].Velocity = _particles[_particlesAlive].Velocity.multiply(_dge.Random_Float(Info.SpeedMin, Info.SpeedMax));

				_particles[_particlesAlive].Gravity = _dge.Random_Float(Info.GravityMin, Info.GravityMax);
				_particles[_particlesAlive].RadialAccel = _dge.Random_Float(Info.RadialAccelMin, Info.RadialAccelMax);
				_particles[_particlesAlive].TangentialAccel = _dge.Random_Float(Info.TangentialAccelMin, Info.TangentialAccelMax);

				_particles[_particlesAlive].Size = _dge.Random_Float(Info.SizeStart, Info.SizeStart + (Info.SizeEnd - Info.SizeStart) * Info.SizeVar);
				_particles[_particlesAlive].SizeDelta = (Info.SizeEnd-_particles[_particlesAlive].Size) / _particles[_particlesAlive].TerminalAge;

				_particles[_particlesAlive].Spin = _dge.Random_Float(Info.SpinStart, Info.SpinStart + (Info.SpinEnd - Info.SpinStart) * Info.SpinVar);
				_particles[_particlesAlive].SpinDelta = (Info.SpinEnd - _particles[_particlesAlive].Spin) / _particles[_particlesAlive].TerminalAge;

				_particles[_particlesAlive].Color.r = _dge.Random_Float(Info.ColorStart.r, Info.ColorStart.r + (Info.ColorEnd.r - Info.ColorStart.r) * Info.ColorVar);
				_particles[_particlesAlive].Color.g = _dge.Random_Float(Info.ColorStart.g, Info.ColorStart.g + (Info.ColorEnd.g - Info.ColorStart.g) * Info.ColorVar);
				_particles[_particlesAlive].Color.b = _dge.Random_Float(Info.ColorStart.b, Info.ColorStart.b + (Info.ColorEnd.b - Info.ColorStart.b) * Info.ColorVar);
				_particles[_particlesAlive].Color.a = _dge.Random_Float(Info.ColorStart.a, Info.ColorStart.a + (Info.ColorEnd.a - Info.ColorStart.a) * Info.AlphaVar);

				_particles[_particlesAlive].ColorDelta.r = (Info.ColorEnd.r - _particles[_particlesAlive].Color.r) / _particles[_particlesAlive].TerminalAge;
				_particles[_particlesAlive].ColorDelta.g = (Info.ColorEnd.g - _particles[_particlesAlive].Color.g) / _particles[_particlesAlive].TerminalAge;
				_particles[_particlesAlive].ColorDelta.b = (Info.ColorEnd.b - _particles[_particlesAlive].Color.b) / _particles[_particlesAlive].TerminalAge;
				_particles[_particlesAlive].ColorDelta.a = (Info.ColorEnd.a - _particles[_particlesAlive].Color.a) / _particles[_particlesAlive].TerminalAge;

				if (_updateBoundingBox) 
					_rectBoundingBox.Encapsulate(_particles[_particlesAlive].Location.x, _particles[_particlesAlive].Location.y);

				_particlesAlive++;
			}
		}

		_vecPrevLocation = _vecLocation;
	}

	public void MoveTo(float x, float y)
	{
		MoveTo(x, y, false);
	}
	
	public void MoveTo(float x, float y, boolean bMoveParticles)
	{
		if (bMoveParticles)
		{
			float dx = x - _vecLocation.x;
			float dy = y - _vecLocation.y;

			for(int i = 0; i < _particlesAlive; i++)
			{
				_particles[i].Location.x += dx;
				_particles[i].Location.y += dy;
			}

			_vecPrevLocation.x = _vecPrevLocation.x + dx;
			_vecPrevLocation.y = _vecPrevLocation.y + dy;
		}
		else
		{
			if (_age == -2.0)
			{ 
				_vecPrevLocation.x = x; 
				_vecPrevLocation.y = y; 
			}
			else
			{ 
				_vecPrevLocation.x = _vecLocation.x;
				_vecPrevLocation.y = _vecLocation.y; 
			}
		}

		_vecLocation.x = x;
		_vecLocation.y = y;
	}

	public void Transpose(float x, float y) 
	{ 
		_tx = x; 
		_ty = y; 
	}
	
	public void SetScale(float scale) 
	{ 
		_scale = scale; 
	}
	
	public void TrackBoundingBox(boolean bTrack) 
	{ 
		_updateBoundingBox = bTrack; 
	}

	public int GetParticlesAlive()
	{ 
		return _particlesAlive; 
	}
	
	public float GetAge()
	{ 
		return _age; 
	}
	
	public DGEVector GetPosition()
	{ 
		return _vecLocation; 
	}
	
	public DGEVector GetTransposition() 
	{
		return new DGEVector(_tx, _ty);
	}
	
	public float GetScale() 
	{ 
		return _scale; 
	}
	
	public DGERect GetBoundingBox()
	{
		return new DGERect(_rectBoundingBox.p1.multiply(_scale), _rectBoundingBox.p2.multiply(_scale));
	}

	private static DGE _dge;

	private float _age;
	private float _emissionResidue;

	private DGEVector _vecPrevLocation;
	private DGEVector _vecLocation;
	private float _tx, _ty;
	private float _scale;

	private int _particlesAlive;
	private DGERect _rectBoundingBox;
	private boolean _updateBoundingBox;

	private static final int MaximumParticles = 500;
	private DGEParticle[] _particles = new DGEParticle[MaximumParticles];
}
