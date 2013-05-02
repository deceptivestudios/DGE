package com.deceptivestudios.engine.particle;

import com.deceptivestudios.engine.helper.DGEVector;

public class DGEParticleManager
{
	public DGEParticleManager()
	{
		_totalParticleSystems = 0;
		_tx = _ty = 0.0f;
	}

	public void Update(float dt)
	{
		for (int i = 0; i < _totalParticleSystems; i++)
		{
			_particleSystems[i].Update(dt);
			
			if (_particleSystems[i].GetAge() == -2.0f && _particleSystems[i].GetParticlesAlive() == 0)
			{
				_particleSystems[i] = _particleSystems[_totalParticleSystems - 1];
				_totalParticleSystems--;
				
				i--;
			}
		}	
	}

	public void Render()
	{
		for (int i = 0; i < _totalParticleSystems; i++) 
			_particleSystems[i].Render();
	}

	public DGEParticleSystem SpawnParticleSystem(DGEParticleSystemInfo psi, float x, float y)
	{
		if (_totalParticleSystems == MaximumParticleSystems) 
			return null;
		
		_particleSystems[_totalParticleSystems] = new DGEParticleSystem(psi);
		
		_particleSystems[_totalParticleSystems].FireAt(x,y);
		_particleSystems[_totalParticleSystems].Transpose(_tx, _ty);
		
		_totalParticleSystems++;
		
		return _particleSystems[_totalParticleSystems - 1];	
	}

	public boolean IsParticleSystemAlive(DGEParticleSystem ps)
	{
		for (int i = 0; i < _totalParticleSystems; i++)
		{
			if (_particleSystems[i] == ps) 
				return true;
		}
		
		return false;
	}

	public void Transpose(float x, float y)
	{
		for (int i = 0; i < _totalParticleSystems; i++)
		{
			_particleSystems[i].Transpose(x,y);
		}
		
		_tx = x; _ty = y;
	}

	public DGEVector GetTransposition()
	{  
		return new DGEVector(_tx, _ty);
	}

	public void KillParticleSystem(DGEParticleSystem ps)
	{
		for (int i = 0; i < _totalParticleSystems;i++)
		{
			if (_particleSystems[i] == ps)
			{
				_particleSystems[i] = _particleSystems[_totalParticleSystems - 1];
				_totalParticleSystems--;
				
				return;
			}
		}
	}

	public void KillAll()
	{
		_totalParticleSystems = 0;
	}
	
	private int _totalParticleSystems;
	private float _tx, _ty;
	
	private static final int MaximumParticleSystems = 100;
	private DGEParticleSystem[] _particleSystems = new DGEParticleSystem[MaximumParticleSystems];
}
