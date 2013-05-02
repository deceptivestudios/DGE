package com.deceptivestudios.engine.helper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.internal.DGEResource;

public class DGEFont 
{
	public static final int Left = 0;
	public static final int Right = 1;
	public static final int Center = 2;
	public static final int HorizontalMask = Left | Right | Center;
	
	public static final int Top = 0;
	public static final int Middle = 4;
	public static final int Bottom = 8;
	public static final int VerticalMask = Top | Middle | Bottom;

	public static final int TotalCharacters = 256;
	
	private static final String HeaderTag = "[DGEFONT]";
	private static final String BitmapTag = "Bitmap";
	private static final String CharacterTag = "Char";
	
	private String _path;
	
	public DGEFont(String filename)
	{
		this();
		
		if (filename.contains("/"))
			_path = filename.substring(0, filename.lastIndexOf("/")) + "/";
		else
			_path = "";
		
		_height = 0.0f;
		_scale = 1.0f;
		_proportion = 1.0f;
		_rotation = 0.0f;
		_tracking = 0.0f;
		_spacing = 1.0f;
		_texture = 0;

		_z = 0.5f;
		_blend = DGE.DGE_BLEND_COLORMUL | DGE.DGE_BLEND_ALPHABLEND | DGE.DGE_BLEND_NOZWRITE;
		_color = new DGEColor(1, 1, 1, 1);

		_letters = new DGESprite[TotalCharacters];
		_pre = new float[TotalCharacters];
		_post = new float[TotalCharacters];
		
		_valid = false;
		
		DGEResource resource = _dge.Resource_Load(filename);
		
		if (resource == null)
			return;
		
		ByteArrayInputStream bais = new ByteArrayInputStream(resource.data);
		BufferedReader reader = new BufferedReader(new InputStreamReader(bais));
		
		String line;
		boolean headerFound = false;
		boolean textureFound = false;
		
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
						_dge.System_Log("Font '%s' has incorrect format", filename);
						return;
					}
				}
				else
				{
					String[] detail = line.split("=");
					
					if (detail.length == 2)
					{
						String command = detail[0];
						String data = detail[1];
						
						if (command.equalsIgnoreCase(BitmapTag))
						{
							textureFound = true;
							_texture = _dge.Texture_Load(_path + data);
							
							if (_texture == 0)
							{
								_dge.System_Log("Font '%s' is missing the texture", filename);
								return;
							}
						}
						else if (command.equalsIgnoreCase(CharacterTag))
						{
							if (!textureFound)
							{
								_dge.System_Log("Font '%s' is missing the texture", filename);
								return;
							}
							
							//String[] charData = data.split(",");
							String[] charData = data.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
							
							if (charData.length == 7)
							{
								char c = charData[0].charAt(1);
								
								int x = Integer.parseInt(charData[1]);
								int y = Integer.parseInt(charData[2]);
								int w = Integer.parseInt(charData[3]);
								int h = Integer.parseInt(charData[4]);
								int b = Integer.parseInt(charData[5]);
								int a = Integer.parseInt(charData[6]);
								
								_letters[c] = new DGESprite(_texture, x, y, w, h);
								
								_pre[c] = b;
								_post[c] = a;
								
								if (h > _height)
									_height = h;
							}
						}
					}
				}
			}
			
			_valid = true;
		}
		catch (IOException e)
		{
			_dge.System_Log("Error reading from Font '%s'", filename);
		}
	}

	private boolean _valid;
	
	public boolean Validate()
	{
		return _valid;
	}
	
	public DGERect Render(float x, float y, int align, String format, Object... args)
	{
		if (!_valid)
			return null;
		
		DGERect rect = new DGERect();
		String string = String.format(format, args);
		
		char c;
		float fx = x;

		int halign = align & HorizontalMask;
		int valign = align & VerticalMask;
		
		if (valign != Top)
		{
			if ((valign & Middle) == Middle)
				y -= (int) ((_height * _scale * _spacing) / 2f);
			else
				y -= (int) (_height * _scale * _spacing);
			
			for (int pos = 0; pos < string.length(); pos++)
			{
				if (string.charAt(pos) == '\n')
				{
					if ((valign & Middle) == Middle)
						y -= (int) ((_height * _scale * _spacing) / 2f);
					else
						y -= (int) (_height * _scale * _spacing);
				}
			}
		}
		
		rect.p1.y = y;
		
		if ((halign & Right) == Right) 
			fx -= GetStringWidth(string, false);
		
		if ((halign & Center) == Center) 
			fx -= (int) (GetStringWidth(string, false) / 2.0f);

		rect.p1.x = rect.p2.x = fx;
		rect.p2.y = y + (int) (_height * _scale * _spacing);
		
		for (int pos = 0; pos < string.length(); pos++)
		{
			if (string.charAt(pos) == '\n')
			{
				y += (int) (_height * _scale * _spacing);
				fx = x;
				
				if ((halign & Right) == Right)  
					fx -= GetStringWidth(string.substring(pos + 1), false);
				
				if ((halign & Center) == Center)
					fx -= (int) (GetStringWidth(string.substring(pos + 1), false) / 2.0f);

				if (fx < rect.p1.x)
					rect.p1.x = fx;
				
				rect.p2.y = y + (int) (_height * _scale * _spacing);
			}
			else
			{
				c = string.charAt(pos);
				
				if (_letters[c] == null)
					c = '?';
				
				if (_letters[c] != null)
				{
					fx += _pre[c] * _scale * _proportion;
					_letters[c].RenderEx(fx, y, _rotation, _scale * _proportion, _scale);
					fx += (_letters[c].GetWidth() + _post[c] + _tracking) * _scale * _proportion;
				}

				if (fx > rect.p2.x)
					rect.p2.x = fx;
			}
		}
		
		return rect;
	}
	
	public DGERect Render(float x, float y, int align, boolean shadow, String format, Object... args)
	{
		if (shadow)
		{
			DGEColor current = _color;
			SetColor(current.r - 0.4f, current.g - 0.4f, current.b - 0.4f, current.a);
			
			Render(x + 3, y + 3, align, format, args);
			
			SetColor(current);
		}
		
		return Render(x, y, align, format, args);
	}

	public void SetColor(DGEColor color)
	{
		SetColor(color.r, color.g, color.b, color.a);
	}
	
	public void SetColor(float r, float g, float b, float a)
	{
		r = Math.min(1f, Math.max(0f, r));
		g = Math.min(1f, Math.max(0f, g));
		b = Math.min(1f, Math.max(0f, b));
		a = Math.min(1f, Math.max(0f, a));
		
		_color = new DGEColor(r, g, b, a);
		
		for(int i = 0; i < 256; i++)
		{
			if (_letters[i] != null)
				_letters[i].SetColor(r, g, b, a);
		}		
	}
	
	public void SetZ(float z)
	{
		_z = z;

		for(int i = 0; i < 256; i++)
		{
			if (_letters[i] != null)
				_letters[i].SetZ(z);
		}
	}
	
	public float GetZ()
	{
		return _z;
	}
	
	public void SetBlendMode(int blend)
	{
		_blend = blend;

		for(int i = 0; i < 256; i++)
		{
			if (_letters[i] != null)
				_letters[i].SetBlendMode(blend);
		}
	}
	
	public int GetBlendMode()
	{
		return _blend;
	}
	
	public void SetScale(float scale) 
	{
		_scale = scale;
	}
	
	public void SetProportion(float prop) 
	{
		_proportion = prop; 
	}
	
	public void SetRotation(float rot) 
	{ 
		_rotation = rot;
	}
	
	public void SetTracking(float tracking) 
	{
		_tracking = tracking;
	}
	
	public void SetSpacing(float spacing) 
	{
		_spacing = spacing;
	}

	public DGEColor GetColor()
	{
		return _color;
	}
	
	public float GetScale()
	{
		return _scale;
	}
	
	public float GetProportion()
	{
		return _proportion; 
	}
	
	public float GetRotation()
	{
		return _rotation;
	}
	
	public float GetTracking() 
	{
		return _tracking;
	}
	
	public float GetSpacing()
	{
		return _spacing;
	}

	public DGESprite GetSprite(char chr)
	{
		if (!_valid)
			return null;
		
		return _letters[chr];
	}
	
	public float GetPreWidth(char chr)
	{
		if (!_valid)
			return 0;
		
		return _pre[chr]; 
	}
	
	public float GetPostWidth(char chr)
	{
		if (!_valid)
			return 0;
		
		return _post[chr];
	}
	
	public float GetHeight()
	{ 
		if (!_valid)
			return 0;
		
		return _height; 
	}
	
	public float GetStringWidth(String string)
	{
		if (!_valid)
			return 0;
		
		return GetStringWidth(string, true);
	}
	
	public float GetStringWidth(String string, boolean multiline)
	{
		if (!_valid)
			return 0;
		
		int pos = 0;
		float linew, w = 0;

		while (pos < string.length())
		{
			linew = 0;

			while (pos < string.length() && string.charAt(pos) != '\n')
			{
				char c = string.charAt(pos);
				
				if (_letters[c] == null) 
					c = '?';
				
				if (_letters[c] != null)
					linew += _letters[c].GetWidth() + _pre[c] + _post[c] + _tracking;

				pos++;
			}

			if (!multiline) 
				return linew * _scale * _proportion;

			if (linew > w) 
				w = linew;

			while (pos < string.length() && (string.charAt(pos) == '\n' || string.charAt(pos) == '\r'))
				pos++;
		}

		return w * _scale * _proportion;
	}

	private DGEFont()
	{
		_dge = DGE.Interface(DGE.DGE_VERSION);
	}

	private static DGE _dge;

	private int _texture;
	
	private DGESprite[] _letters;
	private float[] _pre;
	private float[]	_post;
	
	private float _height;
	private float _scale;
	private float _proportion;
	private float _rotation;
	private float _tracking;
	private float _spacing;
	private DGEColor _color;
	private float _z;
	private int _blend;
}
