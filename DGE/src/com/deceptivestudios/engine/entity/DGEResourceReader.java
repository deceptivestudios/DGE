package com.deceptivestudios.engine.entity;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.internal.DGEResource;

public class DGEResourceReader
{
	protected DGE _dge;
	protected int _position;
	protected String _resource;
	protected DGEResource _data;
	
	public DGEResourceReader(String filename)
	{
		_dge = DGE.Interface(DGE.DGE_VERSION);
		
		_resource = filename;
		_data = _dge.Resource_Load(_resource);
		
		_position = 0;
	}

	public void Close()
	{
		_dge.Resource_Free(_resource);
	}
	
	public boolean Failed()
	{
		return _data == null;
	}
	
	public int ReadInt()
	{
		int value = 0;
		
		value += (_data.data[_position + 0] & 0xFF);
		value += (_data.data[_position + 1] & 0xFF) << 8;
		value += (_data.data[_position + 2] & 0xFF) << 16;
		value += (_data.data[_position + 3] & 0xFF) << 24;
		
		_position += 4;
		
		return value;
	}
	
	public float ReadFloat()
	{
		return Float.intBitsToFloat(ReadInt());
	}
	
	public String ReadString()
	{
		return ReadString(ReadInt());
	}
	
	public String ReadString(int length)
	{
		if (length == 0)
			return "";
		
		String value = new String(_data.data, _position, length);
		
		_position += length;
		
		return value.substring(0, length - 1);
	}

	public DGEColor ReadColor() 
	{
		return DGEColor.ParseARGB(ReadInt());
	}

	public boolean ReadBool() 
	{
		boolean value = false;
		
		if (_data.data[_position] != 0)
			value = true;

		_position += 1;

		return value;
	}
}
