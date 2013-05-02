package com.deceptivestudios.engine.entity;

import java.util.HashMap;
import java.util.Map.Entry;

import com.deceptivestudios.engine.helper.DGEColor;

public class DGEProperties 
{
	public String Classname;
	
	protected HashMap<String, String> _settings;
	
	public DGEProperties()
	{
		_settings = new HashMap<String, String>();
	}
	
	public DGEProperties(DGEProperties props)
	{
		this();
		Copy(props);
	}
	
	public void AddProperty(String name, String value)
	{
		_settings.put(name, value);
	}
	
	public void AddProperty(String name, int value)
	{
		_settings.put(name, String.format("%d", value));
	}
	
	public void AddProperty(String name, float value)
	{
		_settings.put(name, String.format("%.6f", value));
	}
	
	public void AddProperty(String name, DGEColor value)
	{
		int a = (int) (value.a * 255f);
		int r = (int) (value.a * 255f);
		int g = (int) (value.a * 255f);
		int b = (int) (value.a * 255f);
		
		_settings.put(name, String.format("%02X%02X%02X%02X", a, r, g, b));
	}

	public Entry<String, String> GetProperty(String name) 
	{
		for (Entry<String, String> setting : _settings.entrySet())
		{
			if (setting.getKey().equalsIgnoreCase(name))
				return setting;
		}
		
		return null;
	}
	
	public String GetString(String name)
	{
		Entry<String, String> setting = GetProperty(name);
		
		if (setting != null)
			return setting.getValue();
		
		return "";
	}
	
	public float GetFloat(String name)
	{
		Entry<String, String> setting = GetProperty(name);
		
		if (setting != null)
			return Float.parseFloat(setting.getValue());
		
		return 0;
	}
	
	public int GetInt(String name)
	{
		Entry<String, String> setting = GetProperty(name);
		
		if (setting != null)
			return Integer.parseInt(setting.getValue());
		
		return 0;
	}
	
	public DGEColor GetColor(String name)
	{
		Entry<String, String> setting = GetProperty(name);
		
		if (setting != null)
			return DGEColor.ParseARGB(setting.getValue());
		
		return new DGEColor(1, 1, 1, 1);
	}
	
	public boolean EditProperty(String name, String value)
	{
		Entry<String, String> setting = GetProperty(name);
		
		if (setting != null)
		{
			_settings.put(name, value);
			return true;
		}
		
		return false;
	}
	
	public boolean EraseProperty(String name)
	{
		return (_settings.remove(name) != null);
	}
	
	public void Copy(DGEProperties props)
	{
		_settings.clear();
		
		for (Entry<String, String> setting : _settings.entrySet())
			_settings.put(setting.getKey(), setting.getValue());
		
		Classname = props.Classname;
	}
	
	public void Load(DGEResourceReader reader)
	{
		_settings.clear();
		
		Classname = reader.ReadString();
		
		int properties = reader.ReadInt();
		
		for (int i = 0; i < properties; i++)
		{
			// currently unused
			String datatype = reader.ReadString();
			String name = reader.ReadString();
			String value = reader.ReadString();
			
			AddProperty(name, value);
		}
	}

	public int GetPropertyCount() 
	{
		return _settings.size();
	}
}
