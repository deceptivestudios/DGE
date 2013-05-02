package com.deceptivestudios.engine.particle;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.helper.DGESprite;
import com.deceptivestudios.engine.internal.DGEResource;

public class DGEParticleSystemInfo
{
	public DGESprite Sprite;    // texture + blend mode
	
	public int Emission; // particles per sec
	public float Lifetime;

	public float ParticleLifeMin;
	public float ParticleLifeMax;

	public float Direction;
	public float Spread;
	public boolean Relative;

	public float SpeedMin;
	public float SpeedMax;

	public float GravityMin;
	public float GravityMax;

	public float RadialAccelMin;
	public float RadialAccelMax;

	public float TangentialAccelMin;
	public float TangentialAccelMax;

	public float SizeStart;
	public float SizeEnd;
	public float SizeVar;

	public float SpinStart;
	public float SpinEnd;
	public float SpinVar;

	public DGEColor ColorStart; // + alpha
	public DGEColor ColorEnd;
	
	public float ColorVar;
	public float AlphaVar;
	
	public DGEParticleSystemInfo()
	{
		
	}
	
	public DGEParticleSystemInfo(String filename) throws IOException
	{
		DGE dge = DGE.Interface(DGE.DGE_VERSION);
		
		DGEResource resource = dge.Resource_Load(filename, false);
		
		ByteArrayInputStream stream = new ByteArrayInputStream(resource.data);
		DataInputStream ois = new DataInputStream(stream);
		
		Import(ois);
	}
	
	public DGEParticleSystemInfo(DGEParticleSystemInfo psi)
	{
		Sprite = psi.Sprite;    // texture + blend mode
		Emission = psi.Emission; // particles per sec
		Lifetime = psi.Lifetime;

		ParticleLifeMin = psi.ParticleLifeMin;
		ParticleLifeMax = psi.ParticleLifeMax;

		Direction = psi.Direction;
		Spread = psi.Spread;
		Relative = psi.Relative;

		SpeedMin = psi.SpeedMin;
		SpeedMax = psi.SpeedMax;

		GravityMin = psi.GravityMin;
		GravityMax = psi.GravityMax;

		RadialAccelMin = psi.RadialAccelMin;
		RadialAccelMax = psi.RadialAccelMax;

		TangentialAccelMin = psi.TangentialAccelMin;
		TangentialAccelMax = psi.TangentialAccelMax;

		SizeStart = psi.SizeStart;
		SizeEnd = psi.SizeEnd;
		SizeVar = psi.SizeVar;

		SpinStart = psi.SpinStart;
		SpinEnd = psi.SpinEnd;
		SpinVar = psi.SpinVar;

		ColorStart = new DGEColor(psi.ColorStart); // + alpha
		ColorEnd = new DGEColor(psi.ColorEnd);
		
		ColorVar = psi.ColorVar;
		AlphaVar = psi.AlphaVar;	
	}
	
	public void Export(DataOutputStream out) throws IOException
	{
		out.write(Sprite.GetTexture()); // not used
		
		WriteInt(Emission, out);
		WriteFloat(Lifetime, out);

		WriteFloat(ParticleLifeMin, out);
		WriteFloat(ParticleLifeMax, out);

		WriteFloat(Direction, out);
		WriteFloat(Spread, out);
		WriteInt(Relative ? 1 : 0, out);

		WriteFloat(SpeedMin, out);
		WriteFloat(SpeedMax, out);

		WriteFloat(GravityMin, out);
		WriteFloat(GravityMax, out);

		WriteFloat(RadialAccelMin, out);
		WriteFloat(RadialAccelMax, out);

		WriteFloat(TangentialAccelMin, out);
		WriteFloat(TangentialAccelMax, out);
		
		WriteFloat(SizeStart, out);
		WriteFloat(SizeEnd, out);
		WriteFloat(SizeVar, out);
		
		WriteFloat(SpinStart, out);
		WriteFloat(SpinEnd, out);
		WriteFloat(SpinVar, out);
		
		WriteFloat(ColorStart.r, out);
		WriteFloat(ColorStart.g, out);
		WriteFloat(ColorStart.b, out);
		WriteFloat(ColorStart.a, out);
		WriteFloat(ColorEnd.r, out);
		WriteFloat(ColorEnd.g, out);
		WriteFloat(ColorEnd.b, out);
		WriteFloat(ColorEnd.a, out);
	
		WriteFloat(ColorVar, out);
		WriteFloat(AlphaVar, out);
	}
	
	private void WriteInt(int value, DataOutputStream out) throws IOException
	{
		byte[] data = new byte[4];
		
		data[0] = (byte) (value >> 24);
		data[1] = (byte) (value >> 16 & 0xFF);
		data[2] = (byte) (value >> 8 & 0xFF);
		data[3] = (byte) (value & 0xFF);
		
		out.write(data, 0, 4);
	}
	
	private void WriteFloat(float value, DataOutputStream out) throws IOException
	{
		WriteInt(Float.floatToIntBits(value), out);
	}
	
	public void Import(DataInputStream in) throws IOException
	{
		in.skipBytes(4); // not used
		
		Emission = ReadInt(in);
		Lifetime = ReadFloat(in);

		ParticleLifeMin = ReadFloat(in);
		ParticleLifeMax = ReadFloat(in);

		Direction = ReadFloat(in);
		Spread = ReadFloat(in);
		Relative = (ReadInt(in) != 0);

		SpeedMin = ReadFloat(in);
		SpeedMax = ReadFloat(in);

		GravityMin = ReadFloat(in);
		GravityMax = ReadFloat(in);

		RadialAccelMin = ReadFloat(in);
		RadialAccelMax = ReadFloat(in);

		TangentialAccelMin = ReadFloat(in);
		TangentialAccelMax = ReadFloat(in);

		SizeStart = ReadFloat(in);
		SizeEnd = ReadFloat(in);
		SizeVar = ReadFloat(in);

		SpinStart = ReadFloat(in);
		SpinEnd = ReadFloat(in);
		SpinVar = ReadFloat(in);

		ColorStart = new DGEColor(ReadFloat(in), ReadFloat(in), ReadFloat(in), ReadFloat(in));
		ColorEnd = new DGEColor(ReadFloat(in), ReadFloat(in), ReadFloat(in), ReadFloat(in));
		
		ColorVar = ReadFloat(in);
		AlphaVar = ReadFloat(in);
	}
	
	private int ReadInt(DataInputStream in) throws IOException
	{
		byte[] data = new byte[4];
		
		in.readFully(data, 0, 4);
		
		return (data[3]) << 24 | (data[2] & 0xFF) << 16 | (data[1] & 0xFF) << 8 | (data[0] & 0xFF);
	}
	
	private float ReadFloat(DataInputStream in) throws IOException
	{
		return Float.intBitsToFloat(ReadInt(in));
	}
}
