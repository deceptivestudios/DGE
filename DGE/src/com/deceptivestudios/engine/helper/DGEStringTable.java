package com.deceptivestudios.engine.helper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.internal.DGEResource;

public class DGEStringTable 
{
	private DGE _dge;
	
	private HashMap<String, String> _strings;
	private static final String HeaderTag = "[DGESTRINGTABLE]";
	private static final String FormatError = "String table %s has incorrect format";
	
	public DGEStringTable(String filename)
	{
		_dge = DGE.Interface(DGE.DGE_VERSION);
		_strings = new HashMap<String, String>();

		DGEResource resource = _dge.Resource_Load(filename);
		
		if (resource == null)
			return;
		
		ByteArrayInputStream bais = new ByteArrayInputStream(resource.data);
		BufferedReader reader = new BufferedReader(new InputStreamReader(bais));
		
		String line;
		String name = "", value = "";
		boolean headerFound = false;
		boolean stringOpen = false;
		
		try
		{
			// Load font description
			while ((line = reader.readLine()) != null) 
			{
				if (!headerFound)
				{
					if (line.startsWith(HeaderTag))
					{
						headerFound = true;
					}
					else
					{
						_dge.System_Log(FormatError, filename);
						return;
					}
				}
				else
				{
					if (!stringOpen)
					{
						// skip comment lines
						if (line.startsWith(";"))
							continue;
						
						if (line.contains("="))
						{
							String[] detail = line.split("=");
							
							if (detail.length == 2)
							{
								name = detail[0].trim();
								value = detail[1].trim();
							}
							else
							{
								name = detail[0].trim();
								value = "";
							}
						}
						else
						{
							continue;
						}
					}
					else
					{
						if (value.length() == 0)
							value = line.trim();
						else
							value = String.format("%s\n%s", value, line.trim());
					}
					
					if (value.length() == 0 || (value.charAt(value.length() - 1) != '"' || value.charAt(value.length() - 2) == '\\'))
						stringOpen = true;
					else
						stringOpen = false;
					
					if (!stringOpen && name.length() > 0 && value.length() > 0)
					{
						_strings.put(name, value.substring(1, value.length() - 1));
						
						name = "";
						value = "";
					}
				}
			}
		}
		catch (Exception e)
		{
			_dge.System_Log(FormatError, filename);
			e.printStackTrace();
		}
	}
	
	public String GetString(String name)
	{
		if (!_strings.containsKey(name))		
			return "";
		
		return _strings.get(name);
	}
}
