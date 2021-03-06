package com.deceptivestudios.engine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;


import com.deceptivestudios.engine.audio.DGESoundManager;
import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.helper.DGEMultiple;
import com.deceptivestudios.engine.helper.DGEPackage;
import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.internal.DGEInterface;
import com.deceptivestudios.engine.internal.DGERenderer;
import com.deceptivestudios.engine.internal.DGEResource;
import com.deceptivestudios.engine.internal.DGETexture;
import com.deceptivestudios.engine.internal.DGEView;
import com.deceptivestudios.engine.physics.box2d.callbacks.ContactListener;
import com.deceptivestudios.engine.physics.box2d.collision.AABB;
import com.deceptivestudios.engine.physics.box2d.collision.shapes.CircleShape;
import com.deceptivestudios.engine.physics.box2d.collision.shapes.PolygonShape;
import com.deceptivestudios.engine.physics.box2d.collision.shapes.Shape;
import com.deceptivestudios.engine.physics.box2d.collision.shapes.ShapeType;
import com.deceptivestudios.engine.physics.box2d.common.Vec2;
import com.deceptivestudios.engine.physics.box2d.dynamics.Body;
import com.deceptivestudios.engine.physics.box2d.dynamics.BodyDef;
import com.deceptivestudios.engine.physics.box2d.dynamics.BodyType;
import com.deceptivestudios.engine.physics.box2d.dynamics.Fixture;
import com.deceptivestudios.engine.physics.box2d.dynamics.FixtureDef;
import com.deceptivestudios.engine.physics.box2d.dynamics.World;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioManager;
import android.net.Uri;
import android.opengl.GLUtils;
import android.os.Environment;
import android.util.FloatMath;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

/**
 * This class provides simple GLES 2D rendering, audio, touch, physics and networking.
 * @author Jonathan Lowden
 */
public class DGE
{
	public static final int DGE_VERSION = 0x010;
	
	public static final int DGE_WINDOWED = 1;
	public static final int DGE_ZBUFFER = 2;
	public static final int DGE_TEXTUREFILTER = 3;
	public static final int DGE_SHOWSPLASH = 4;
	public static final int DGE_SOUNDENABLED = 5;
	public static final int DGE_SOUNDCHANNELS = 6;
	public static final int DGE_SCREENWIDTH = 7;
	public static final int DGE_SCREENHEIGHT = 8;
	public static final int DGE_FPS = 9;
	public static final int DGE_TITLE = 10;
	public static final int DGE_LOG = 11;
	public static final int DGE_SETTINGS = 12;
	public static final int DGE_INTERFACE = 13;
	public static final int DGE_SLEEPENABLED = 14;
	
	public static final int DGE_LINE = 2;
	public static final int DGE_TRIANGLE = 3;
	public static final int DGE_QUAD = 4;

	public static final int DGE_BLEND_COLORADD = 1;
	public static final int DGE_BLEND_COLORMUL = 0;
	public static final int DGE_BLEND_ALPHABLEND = 2;
	public static final int DGE_BLEND_ALPHAADD = 0;
	public static final int DGE_BLEND_ZWRITE = 4;
	public static final int DGE_BLEND_NOZWRITE = 0;
	public static final int DGE_BLEND_DEFAULT = (DGE_BLEND_COLORMUL | DGE_BLEND_ALPHABLEND | DGE_BLEND_NOZWRITE);
	public static final int DGE_BLEND_DEFAULT_Z = (DGE_BLEND_COLORMUL | DGE_BLEND_ALPHABLEND | DGE_BLEND_ZWRITE);
	
	public static final float M_PI   = 3.14159265358979323846f;
	public static final float M_PI_2 = 1.57079632679489661923f;
	public static final float M_PI_4 = 0.785398163397448309616f;
	public static final float M_1_PI = 0.318309886183790671538f;
	public static final float M_2_PI = 0.636619772367581343076f;
	
	/**
	 * The DGE class is not directly instantiated but is instead accessed via this function.
	 * @param version - use DGE.DGE_VERSION
	 * @return the DGE instance
	 */
	public final static DGE Interface(int version)
	{
		if (version != DGE_VERSION)
			return null;
		
		if (_instance == null)
			_instance = new DGE();
		
		return _instance;
	}
	
	private boolean _ready = false;
	
	/**
	 * Initialises the system ready for use.  After calling this, the start function is called
	 * @return boolean, success or failure.
	 */
	public final boolean System_Initiate()
	{
		if (_ready)
			return true;
		
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		Date date = new Date();

		System_Log("Deceptive Game Engine");
		System_Log("=====================\n");
		System_Log("Version %X.%X\n", DGE_VERSION >> 8, DGE_VERSION & 0xFF);
		System_Log("Date: %s\n", dateFormat.format(date));
		System_Log("Application: %s", System_GetState(DGE_TITLE));
		System_Log("OS: Android %s\n", android.os.Build.VERSION.RELEASE);
		
		if (_interface == null)
			return false;
		
		_interface.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
		if (_sleepEnabled)
			_interface.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		if (!_windowed)
			_interface.requestWindowFeature(Window.FEATURE_NO_TITLE);

    	CreateView();
    	_interface.setContentView(_view);
    	
    	if (_soundEnabled)
    		InitialiseSound();
		
		System_Log("Initialisation complete\n");
		
		_time = 0.0f;
		_deltaTime = 0.0f;
		
		_ready = true;
		
		return true;
	}
	
	public final boolean System_Start()
	{
		if (_interface == null || !_ready)
			return false;
		
		System_Resume();
		
		return true;
	}
	
	public final void System_Shutdown()
	{
		System_Suspend();

		Channel_StopAll();
		Music_FreeAll();
		
		Resource_RemoveAllPacks();
		
		_instance = null;
	}

	public final void System_Suspend()
	{
		if (_view != null)
			_view.onPause();
	}
	
	public final void System_Resume()
	{
		if (_view != null)
			_view.onResume();
	}
	
	public final void System_Log(String format, Object... args)
	{
		Log.i(_log, String.format(format, args));
	}
	
	public final boolean System_Launch(String url)
	{
		if (_interface == null)
			return false;
		
		if (!url.startsWith("http://") && !url.startsWith("https://"))
			url = "http://" + url;
		
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		_interface.startActivity(browserIntent);
		
		return true;
	}
	
	private String _snapshot = "";
	
	public final void System_Snapshot(String filename)
	{
		File dir = new File(String.format("%s/Pictures/%s", Environment.getExternalStorageDirectory().getAbsolutePath(), _title));
		dir.mkdirs();
		
		_snapshot = String.format("%s/%s", dir.toString(), filename);
	}
	
	private boolean _windowed = false;
	private boolean _zBuffer = false;
	private boolean _textureFilter = true;
	private boolean _showSplash = true;
	private boolean _soundEnabled = true;
	private boolean _sleepEnabled = false;
	
	public final void System_SetState(int state, boolean value)
	{
		switch (state)
		{
			case DGE_WINDOWED:
			{
				_windowed = value;
			} break;

			case DGE_ZBUFFER:
			{
				_zBuffer = value;
			} break;

			case DGE_TEXTUREFILTER:
			{
				_textureFilter = value;
			} break;

			case DGE_SHOWSPLASH:
			{
				_showSplash = value;
			} break;
			
			case DGE_SOUNDENABLED:
			{
				_soundEnabled = value;
			}
			
			case DGE_SLEEPENABLED:
			{
				_sleepEnabled = value;
			}
		}		
	}
	
	private int _width;
	private int _height;
	private int _cfps = 0;
	private int _soundChannels = 10;
	
	public final void System_SetState(int state, int value)
	{
		switch (state)
		{
			case DGE_SCREENWIDTH:
			{
				_width = value;
			} break;

			case DGE_SCREENHEIGHT:
			{
				_height = value;
			} break;
			
			case DGE_FPS:
			{
				_cfps = value;
			} break;
			
			case DGE_SOUNDCHANNELS:
			{
				_soundChannels = value;
			} break;
		}
	}
	
	private String _title = "DGE";
	private String _log = "DGE";
	private String _settings = "";
	
	public final void System_SetState(int state, String value)
	{
		switch (state)
		{
			case DGE_TITLE:
			{
				_title = value;
			} break;
			
			case DGE_LOG:
			{
				_log = value;
			} break;
			
			case DGE_SETTINGS:
			{
				_settings = value;
			} break;
		}
	}
	
	public final void System_SetState(int state, Object value) 
	{
		switch (state)
		{
			case DGE_INTERFACE:
			{
				_interface = (DGEInterface) value;
			} break;
		}
	}

	public final Object System_GetState(int state)
	{
		switch (state)
		{
			case DGE_WINDOWED:
			{
				return _windowed;
			}

			case DGE_ZBUFFER:
			{
				return _zBuffer;
			}

			case DGE_TEXTUREFILTER:
			{
				return _textureFilter;
			}
	
			case DGE_SHOWSPLASH:
			{
				return _showSplash;
			}
			
			case DGE_SLEEPENABLED:
			{
				return _sleepEnabled;
			}
			
			case DGE_SOUNDENABLED:
			{
				return _soundEnabled;
			}
			
			case DGE_SOUNDCHANNELS:
			{
				return _soundChannels;
			}
			
			case DGE_SCREENWIDTH:
			{
				return _width;
			}
			
			case DGE_SCREENHEIGHT:
			{
				return _height;
			}
			
			case DGE_FPS:
			{
				return _cfps;
			}
			
			case DGE_TITLE:
			{
				return _title;
			}
			
			case DGE_LOG:
			{
				return _log;
			}
			
			case DGE_SETTINGS:
			{
				return _settings;
			}
			
			case DGE_INTERFACE:
			{
				return _interface;
			}
		}
		
		return null;
	}
	
	//private DGEResourceList _resources;
	private HashMap<String, DGEResource> _resources;
	
	public final DGEResource Resource_Load(String filename)
	{
		return Resource_Load(filename, true);
	}
	
	public final DGEResource Resource_Load(String filename, boolean persistent)
	{
		if (GetManager() == null)
			return null;
		
		if (_resources == null)
			_resources = new HashMap<String, DGEResource>();
		
		System_Log("Loading resource %s...", filename);
		
		DGEResource target = _resources.get(filename);
		
		if (target != null)
			return target;
		
		if (filename.charAt(0) == '\\' || filename.charAt(0) == '/' || filename.charAt(1) == ':')
		{
			// load from file system
			try
			{
				InputStream stream = new FileInputStream(filename);
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				
				int read;
				byte[] data = new byte[16384];
				
				while ((read = stream.read(data, 0, data.length)) != -1)
				{
					buffer.write(data, 0, read);
				}

				target = new DGEResource();
				
				target.resource = filename;
				target.data = buffer.toByteArray();
				
				if (persistent)
					_resources.put(filename, target);
				
				stream.close();
				
				return target;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		else
		{
			DGEPackage pack = CheckPackages(filename);
			
			if (pack != null)
			{
				DGEResource resource = pack.Retrieve(filename);
				
				if (persistent)
					_resources.put(filename, resource);
				
				return resource;
			}
			else
			{
				// load from resources / packages
				try
				{
					InputStream stream = _assets.open(filename);
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					
					int read;
					byte[] data = new byte[16384];
					
					while ((read = stream.read(data, 0, data.length)) != -1)
					{
						buffer.write(data, 0, read);
					}
	
					target = new DGEResource();
					
					target.resource = filename;
					target.data = buffer.toByteArray();
					
					if (persistent)
						_resources.put(filename, target);
					
					return target;
				}
				catch (Exception e)
				{
					e.printStackTrace();
	
					return null;
				}
			}
		}
	}
	
	public final void Resource_Free(String filename)
	{
		if (_resources == null)
			return;
		
		if (_resources.containsKey(filename))
			_resources.remove(filename);
	}
	
	private HashMap<String, DGEPackage> _packages;
	
	public final boolean Resource_AttachPack(String filename)
	{
		return Resource_AttachPack(filename, null);
	}
	
	public final boolean Resource_AttachPack(String filename, String password)
	{
		if (GetManager() == null)
			return false;
		
		if (_packages == null)
			_packages = new HashMap<String, DGEPackage>();
		
		DGEPackage pack = _packages.get(filename);
		
		if (pack != null)
			_packages.remove(filename);
		
		pack = new DGEPackage(filename, _assets, password);
		_packages.put(filename, pack);
		
		return pack.Reload();
	}
	
	// used to build the pack to store the data
	public final DGEPackage Resource_CreatePack(String filename)
	{
		return new DGEPackage(filename, _assets);
	}
	
	public final DGEPackage Resource_CreatePack(String filename, String password)
	{
		return new DGEPackage(filename, _assets, password);
	}
	
	public final void Resource_RemovePack(String filename)
	{
		if (GetManager() == null || _packages == null)
			return;
		
		DGEPackage pack = _packages.get(filename);
		
		if (pack != null)
			_packages.remove(pack);
	}
	
	public final void Resource_RemoveAllPacks()
	{
		_packages = new HashMap<String, DGEPackage>();
	}
	
	public final String Resource_MakePath()
	{
		return Resource_MakePath("");
	}
	
	public final String Resource_MakePath(String filename)
	{
		return Resource_MakePath("", filename);
	}
	
	public final String Resource_MakePath(String folder, String filename)
	{
		if (folder == "")
			return filename;
		else if (filename == "")
			return folder;
		else
			return folder + "/" + filename;
	}
	
	public final String[] Resource_EnumFiles()
	{
		return Resource_EnumFiles("assets");
	}

	public final String[] Resource_EnumFiles(String folder)
	{
		return Resource_EnumFiles(folder, null);
	}

	public final String[] Resource_EnumFiles(String folder, String wildcard)
	{
		String[] files;
		boolean asset;
		
		if (folder.startsWith("assets/") || folder.equalsIgnoreCase("assets"))
		{
			if (GetManager() == null)
				return null;
			
			try
			{
				folder = folder.substring(6);
				
				if (folder.startsWith("/"))
					folder = folder.substring(1);
				
				files = _assets.list(folder);
				asset = true;
			}
			catch (IOException e)
			{
				return null;
			}
		}
		else
		{
			String path = String.format("%s/%s", Environment.getExternalStorageDirectory().getAbsolutePath(), folder);
			File dir = new File(path);
			
			files = dir.list();
			asset = false;
		}
		
		if (files.length == 0)
			return null;
		
		Vector<String> vec = new Vector<String>();
		
		for (int i = 0; i < files.length; i++)
		{
			if (CheckFile(wildcard, files[i]))
			{
				String path = Resource_MakePath(folder, files[i]);
				
				if (path.startsWith("/"))
					path = path.substring(1);
				
				try
				{
					InputStream in;
					
					if (asset)
						in = _assets.open(path);
					else
						in = new FileInputStream(path);
						
					in.close();

					vec.add(path);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		if (vec.size() > 0)
			return vec.toArray(new String[vec.size()]);
		
		return null;
	}
	
	public final String[] Resource_EnumFolders()
	{
		return Resource_EnumFolders("assets");
	}
	
	public final String[] Resource_EnumFolders(String folder)
	{
		return Resource_EnumFolders(folder, null);
	}

	public final String[] Resource_EnumFolders(String folder, String wildcard)
	{
		String[] folders;
		
		if (folder.startsWith("assets/") || folder.startsWith("assets"))
		{
			if (GetManager() == null)
				return null;
			
			try
			{
				folder = folder.substring(6);
				
				if (folder.startsWith("/"))
					folder = folder.substring(1);
				
				folders = _assets.list(folder);
			}
			catch (IOException e)
			{
				return null;
			}

			if (folders.length == 0)
				return null;
			
			Vector<String> vec = new Vector<String>();
			
			for (int i = 0; i < folders.length; i++)
			{
				if (CheckFile(wildcard, folders[i]))
				{
					String path = Resource_MakePath(folder, folders[i]);
					
					if (path.startsWith("/"))
						path = path.substring(1);
					
					try
					{
						InputStream in = _assets.open(path);
						in.close();
					}
					catch (Exception e)
					{
						vec.add(path);
					}
				}
			}
			
			if (vec.size() > 0)
				return vec.toArray(new String[vec.size()]);
			
			return null;
		}
		else
		{
			String path = String.format("%s/%s", Environment.getExternalStorageDirectory().getAbsolutePath(), folder);
			File dir = new File(path);
			
			File[] files = dir.listFiles();
			Vector<String> vec = new Vector<String>();
			
			for (int i = 0; i < files.length; i++)
			{
				if (files[i].isDirectory())
					vec.add(files[i].toString());
			}
			
			if (vec.size() > 0)
				return vec.toArray(new String[vec.size()]);
			
			return null;
		}
	}
	
	private final DGEPackage CheckPackages(String filename)
	{
		if (_packages == null)
			return null;
		
		System_Log(" - checking packages");
		
		for (Entry<String, DGEPackage> data : _packages.entrySet())
		{
			if (data.getValue().Contains(filename))
			{
				System_Log("    - found %s in %s", filename, data.getKey());
				return data.getValue();
			}
		}
		
		System_Log("    - not found in packages");
		
		return null;
	}
	
	private final boolean CheckFile(String wildcard, String filename)
	{
		if (wildcard == null || wildcard == "")
			return true;
			
		if (filename.matches(WildToRegex(wildcard)))		
			return true;
		
		return false;
	}
	
	private final String WildToRegex(String wildcard)
	{
		StringBuffer s = new StringBuffer(wildcard.length());
		s.append('^');
		
		for (int i = 0, is = wildcard.length(); i < is; i++)
		{
			char c = wildcard.charAt(i);
			
			switch (c)
			{
				case '*':
					s.append(".*");
					break;
				case '?':
					s.append(".");
					break;
				case '(': case ')': case '[': case ']': case '$':
				case '^': case '.': case '{': case '}': case '|':
				case '\\':
					s.append("\\");
					s.append(c);
					break;
					
				default:
					s.append(c);
					break;
			}
		}
		
		s.append('$');
		return s.toString();
	}
	
	public final void Ini_SetBoolean(String section, String name, boolean value)
	{
		if (_settings == "")
			return;

		String key = String.format("%s_%s", section, name);
		SharedPreferences settings = _interface.getSharedPreferences(_settings, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	public final boolean Ini_GetBoolean(String section, String name, boolean def_val)
	{
		if (_settings == "")
			return def_val;

		String key = String.format("%s_%s", section, name);
		SharedPreferences settings = _interface.getSharedPreferences(_settings, 0);
		
		return settings.getBoolean(key, def_val);
	}
	
	public final void Ini_SetInt(String section, String name, int value)
	{
		if (_settings == "")
			return;

		String key = String.format("%s_%s", section, name);
		SharedPreferences settings = _interface.getSharedPreferences(_settings, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putInt(key, value);
		editor.commit();
	}
	
	public final int Ini_GetInt(String section, String name, int def_val)
	{
		if (_settings == "")
			return def_val;

		String key = String.format("%s_%s", section, name);
		SharedPreferences settings = _interface.getSharedPreferences(_settings, 0);
		
		return settings.getInt(key, def_val);
	}
	
	public final void Ini_SetFloat(String section, String name, float value)
	{
		if (_settings == "")
			return;

		String key = String.format("%s_%s", section, name);
		SharedPreferences settings = _interface.getSharedPreferences(_settings, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putFloat(key, value);
		editor.commit();
	}
	
	public final float Ini_GetFloat(String section, String name, float def_val)
	{
		if (_settings == "")
			return def_val;

		String key = String.format("%s_%s", section, name);
		SharedPreferences settings = _interface.getSharedPreferences(_settings, 0);
		
		return settings.getFloat(key, def_val);
	}
	
	public final void Ini_SetString(String section, String name, String value)
	{
		if (_settings == "")
			return;

		String key = String.format("%s_%s", section, name);
		SharedPreferences settings = _interface.getSharedPreferences(_settings, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putString(key, value);
		editor.commit();
	}
	
	public final String Ini_GetString(String section, String name, String def_val)
	{
		if (_settings == "")
			return def_val;

		String key = String.format("%s_%s", section, name);
		SharedPreferences settings = _interface.getSharedPreferences(_settings, 0);
		
		return settings.getString(key, def_val);
	}

	private Random _generator;
	
	public final void Random_Seed()
	{
		Random_Seed(0);
	}

	public final void Random_Seed(int seed)
	{
		_generator = new Random();
		
		if (seed == 0)
		{
			Date date = new Date();
			_generator.setSeed(date.getTime());
		}
		else
		{
			_generator.setSeed(seed);
		}
	}
	
	public final int Random_Int(int min, int max)
	{
		if (_generator == null)
			Random_Seed(0);
		
		return min + _generator.nextInt((max + 1) - min);
	}
	
	public final float Random_Float(float min, float max)
	{
		if (_generator == null)
			Random_Seed(0);
		
		return ((max - min) * _generator.nextFloat()) + min;
	}
	
	private float _time;
	private float _deltaTime;
	private int _fps;
	
	public final float Timer_GetTime()
	{
		return _time;
	}
	
	public final float Timer_GetDelta()
	{
		return _deltaTime;
	}
	
	public final int Timer_GetFPS()
	{
		return _fps;
	}
	
	private long _renderTimer;
	
	private final void UpdateTimer()
	{
		long timer = System.currentTimeMillis();
		
		long diff = timer - _renderTimer;
		float time = ((float) diff / 1000f);
		float delta = time % 1;
		
		if (delta <= 0)
			delta = 0.001f;
		
		_time += time;
		_deltaTime = delta;
		
		_fps = (int) Math.floor(1f / delta);

		_renderTimer = System.currentTimeMillis();
	}
	
	public int Effect_Load(String filename)
	{
		return _soundManager.Effect_Load(filename);
	}
	
	public void Effect_Free(int soundId)
	{
		_soundManager.Effect_Free(soundId);
	}
	
	public void Effect_FreeAll()
	{
		_soundManager.Effect_FreeAll();
	}
	
	public int Effect_Play(int soundId)
	{
		return Effect_PlayEx(soundId);
	}

	public int Effect_PlayEx(int soundId)
	{
		return Effect_PlayEx(soundId, 100);
	}
	
	public int Effect_PlayEx(int soundId, int volume)
	{
		return Effect_PlayEx(soundId, volume, 0);
	}
	
	public int Effect_PlayEx(int soundId, int volume, int pan)
	{
		return Effect_PlayEx(soundId, volume, pan, 1.0f);
	}
	
	public int Effect_PlayEx(int soundId, int volume, int pan, float pitch)
	{
		return Effect_PlayEx(soundId, volume, pan, pitch, false);
	}
	
	public int Effect_PlayEx(int soundId, int volume, int pan, float pitch, boolean loop)
	{
		return _soundManager.Effect_Play(soundId, volume, pan, pitch, 1, loop);
	}

	public int Music_Load(String filename)
	{
		return _soundManager.Music_Load(filename);
	}
	
	public void Music_Free(int musicId)
	{
		_soundManager.Music_Free(musicId);
	}
	
	public void Music_FreeAll()
	{
		_soundManager.Music_FreeAll();
	}
	
	public void Music_Play(int musicId, float volume, float panning, boolean loop)
	{
		_soundManager.Music_Play(musicId, volume, panning, loop);
	}
	
	public void Music_Pause(int musicId)
	{
		_soundManager.Music_Pause(musicId);
	}
	
	public void Music_Resume(int musicId)
	{
		_soundManager.Music_Resume(musicId);
	}
	
	public void Music_Stop(int musicId)
	{
		_soundManager.Music_Stop(musicId);
	}
	
	public void Music_SetPanning(int musicId, float panning)
	{
		_soundManager.Music_SetPanning(musicId, panning);
	}
	
	public void Music_SetVolume(int musicId, float volume)
	{
		_soundManager.Music_SetVolume(musicId, volume);
	}
	
	public void Music_SetLooping(int musicId, boolean loop)
	{
		_soundManager.Music_SetLooping(musicId, loop);
	}
	
	public void Music_SetPosition(int musicId, int position)
	{
		_soundManager.Music_SetPosition(musicId, position);
	}
	
	public int Music_GetPosition(int musicId)
	{
		return _soundManager.Music_GetPosition(musicId);
	}
	
	public void Channel_SetPanning(int channel, int pan)
	{
		_soundManager.Channel_SetPanning(channel, pan);
	}
	
	public void Channel_SetVolume(int channel, int volume)
	{
		_soundManager.Channel_SetVolume(channel, volume);
	}
	
	public void Channel_SetPitch(int channel, float pitch)
	{
		_soundManager.Channel_SetPitch(channel, pitch);
	}
	
	public void Channel_Pause(int channel)
	{
		_soundManager.Channel_Pause(channel);
	}
	
	public void Channel_PauseAll() 
	{
		_soundManager.Channel_PauseAll();
	}
	
	public void Channel_Resume(int channel) 
	{
		_soundManager.Channel_Resume(channel);
	}
	
	public void Channel_ResumeAll()
	{
		_soundManager.Channel_ResumeAll();
	}
	
	public void Channel_Stop(int channel)
	{
		_soundManager.Channel_Resume(channel);
	}
	
	public void Channel_StopAll() 
	{
		_soundManager.Channel_StopAll();
	}
	
	public boolean Channel_IsPlaying(int channel)
	{
		return _soundManager.Channel_IsPlaying(channel);
	}
	
	public float Channel_GetLength(int channel)
	{
		return 0f;
	}
	
	public float Channel_GetPos(int channel)
	{
		return 0f;
	}
	
	public void Channel_SetPos(int channel, float fSeconds)
	{
		
	}
	
	public void Channel_SlideTo(int channel, float time, int volume)
	{
		Channel_SlideTo(channel, time, volume, -101);
	}
	
	public void Channel_SlideTo(int channel, float time, int volume, int pan)
	{
		Channel_SlideTo(channel, time, volume, pan, -1);
	}
	
	public void Channel_SlideTo(int channel, float time, int volume, int pan, float pitch)
	{
		
	}
	
	public boolean Channel_IsSliding(int channel)
	{
		return false;
	}
	
	private DGESoundManager _soundManager;
	
	private void InitialiseSound()
	{
		GetManager();
		
		_soundManager = new DGESoundManager(_assets, (AudioManager) _interface.getSystemService(Context.AUDIO_SERVICE));
	}
	
	public final boolean Input_Back()
	{
		return _interface.Input_Back();
	}
	
	public final boolean Input_GetDown()
	{
		return _view.GetDown();
	}
	
	public final float[] Input_GetTilt()
	{
		return _view.GetTilt();
	}
	
	public final float[] Input_GetPosition()
	{
		return _view.GetTouch();
	}
	
	public final float[] Input_GetTouch()
	{
		if (_view.GetDown())
			return _view.GetTouch();
		
		return null;
	}
	
	public final boolean Input_GetTap()
	{
		return _view.GetTap();
	}
	
	public final boolean Input_GetDoubleTap()
	{
		return _view.GetDoubleTap();
	}
	
	public final float[] Input_GetFling()
	{
		return _view.GetFling();
	}
	
	public final float[] Input_GetAcceleration()
	{
		return _view.GetAcceleration();
	}
	
	public boolean _lastTouchCheck = false;
	public boolean _lastReleaseCheck = true;
	
	public final boolean Input_Touched()
	{
		boolean currentState = _view.GetDown();
		
		if (currentState)
		{
			if (!_lastTouchCheck)
			{
				_lastTouchCheck = true;
				return true;
			}
		}
		else
		{
			_lastTouchCheck = false;
		}
		
		return false;
	}
	
	public final boolean Input_Released()
	{
		boolean currentState = _view.GetDown();
		
		if (!currentState)
		{
			if (_lastReleaseCheck)
			{
				_lastReleaseCheck = false;
				return true;
			}
		}
		else
		{
			_lastReleaseCheck = true;
		}
		
		return false;
	}
	
	public enum Primitive
	{
		Quads,
		Triples,
		Lines
	}
	
	private FloatBuffer _vertices;
	private FloatBuffer _colors;
	private FloatBuffer _uvs;
	private ShortBuffer _indices;
	private int VERTEX_BUFFER_SIZE = 2000;
	private Primitive _primitive;
	private int _primitives;
	private int _texture = 0;
	
	public final boolean Gfx_BeginScene()
	{
		UpdateTimer();

		_drawCalls = 0;
		
		GL10 gl = _renderer.GL();

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		gl.glOrthof(0, _width, _height, 0, -1, 1);
		gl.glViewport(0, 0, _width, _height);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_SCISSOR_TEST);

		gl.glEnable(GL10.GL_BLEND);
		
		_blending = 0;
		SetBlendMode(DGE_BLEND_DEFAULT);
		
		gl.glDepthFunc(GL10.GL_LEQUAL);
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(3 * VERTEX_BUFFER_SIZE * 4);
		vbb.order(ByteOrder.nativeOrder());
		_vertices = vbb.asFloatBuffer();
		
		ByteBuffer cbb = ByteBuffer.allocateDirect(4 * VERTEX_BUFFER_SIZE * 4);
		cbb.order(ByteOrder.nativeOrder());
		_colors = cbb.asFloatBuffer();
		
		ByteBuffer ubb = ByteBuffer.allocateDirect(2 * VERTEX_BUFFER_SIZE * 4);
		ubb.order(ByteOrder.nativeOrder());
		_uvs = ubb.asFloatBuffer();
		
		ByteBuffer ibb = ByteBuffer.allocateDirect(6 * VERTEX_BUFFER_SIZE * 2);
		ibb.order(ByteOrder.nativeOrder());
		_indices = ibb.asShortBuffer();
		
		return true;
	}
	
	public final void Gfx_EndScene()
	{
		RenderBatch(true);
		
		GL10 gl = _renderer.GL();
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPopMatrix();
		
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
		
		// swap buffers?
		if (_snapshot != "")
		{
			try
			{
				File file = new File(_snapshot);
				FileOutputStream out = new FileOutputStream(file);
				
				Bitmap bitmap = SavePixels(0, 0, _width, _height);
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
				
				out.flush();
				out.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			_snapshot = "";
		}
		
		_lastDrawCalls = _drawCalls;
		
		_vertices = null;
		_colors = null;
		_uvs = null;
		_indices = null;
	}
	
	private int _lastDrawCalls, _drawCalls;
	
	public final int Gfx_GetDrawCalls()
	{
		return _lastDrawCalls;
	}
	
	public final void Gfx_Restore()
	{
		RefreshTexture(0);
	}
	
	public final void Gfx_Clear(float r, float g, float b)
	{
		Gfx_Clear(r, g, b, 1f);
	}
	
	public final void Gfx_Clear(float r, float g, float b, float a)
	{
		GL10 gl = _renderer.GL();
		
		gl.glClearColor(r, g, b, a);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
	}

	public final void Gfx_RenderLine(float x1, float y1, float x2, float y2)
	{
		Gfx_RenderLine(x1, y1, x2, y2, 1f, 1f, 1f, 1f);
	}
	
	public final void Gfx_RenderLine(float x1, float y1, float x2, float y2, float r, float g, float b, float a)
	{
		Gfx_RenderLine(x1, y1, x2, y2, r, g, b, a, 0.5f);
	}
	
	public final void Gfx_RenderLine(float x1, float y1, float x2, float y2, float r, float g, float b, float a, float z)
	{
		GL10 gl = _renderer.GL();
		
		if (_vertices != null)
		{
			if (_primitive != Primitive.Lines || _primitives >= VERTEX_BUFFER_SIZE / 2 || _texture != 0 || _blending != DGE_BLEND_DEFAULT)
			{
				RenderBatch(false);
				
				_primitive = Primitive.Lines;
				
				if (_blending != DGE_BLEND_DEFAULT) 
					SetBlendMode(DGE_BLEND_DEFAULT);
				
				if (_texture != 0)
				{
					gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
					_texture = 0;
				}
			}
			
			_vertices.put(x1); _vertices.put(y1); _vertices.put(z); // first point
			_vertices.put(x2); _vertices.put(y2); _vertices.put(z); // second point
			
			_colors.put(r); _colors.put(g); _colors.put(b); _colors.put(a); // first color
			_colors.put(r); _colors.put(g); _colors.put(b); _colors.put(a); // second color
			
			short offset = (short) (_primitives * 2);
			
			_indices.put((short) (offset));
			_indices.put((short) (offset + 1));
			
			_primitives++;
		}
	}
	
	public final void Gfx_RenderTriple(DGEMultiple triple)
	{
		GL10 gl = _renderer.GL();
		
		if (_vertices != null && triple.vertices.length == 3)
		{
			if (_primitive != Primitive.Triples || _primitives >= VERTEX_BUFFER_SIZE / 3 || _texture != triple.texture || _blending != triple.blend)
			{
				RenderBatch(false);
				_primitive = Primitive.Triples;
				
				if (_blending != triple.blend)
					SetBlendMode(triple.blend);
				
				if (triple.texture != _texture)
				{
					gl.glBindTexture(GL10.GL_TEXTURE_2D, triple.texture);
					_texture = triple.texture;
				}
			}
			
			short offset = (short) (_primitives * 3);
			
			for (int i = 0; i < 3; i++)
			{
				_vertices.put(triple.vertices[i].x); _vertices.put(triple.vertices[i].y); _vertices.put(triple.vertices[i].z);
				_colors.put(triple.vertices[i].r);   _colors.put(triple.vertices[i].g);   _colors.put(triple.vertices[i].b);   _colors.put(triple.vertices[i].a);
				_uvs.put(triple.vertices[i].tx);     _uvs.put(triple.vertices[i].ty);
				
				_indices.put((short) (offset + i));
			}
			
			_primitives++;
		}
	}
	
	public final void Gfx_RenderQuad(DGEMultiple quad)
	{
		GL10 gl = _renderer.GL();
		
		if (_vertices != null && quad.vertices.length == 4)
		{
			if (_primitive != Primitive.Quads || _primitives >= VERTEX_BUFFER_SIZE / 4 || _texture != quad.texture || _blending != quad.blend)
			{
				RenderBatch(false);
				_primitive = Primitive.Quads;
				
				if (_blending != quad.blend)
					SetBlendMode(quad.blend);
				
				if (quad.texture != _texture)
				{
					gl.glBindTexture(GL10.GL_TEXTURE_2D, quad.texture);
					_texture = quad.texture;
				}
			}
			
			for (int i = 0; i < 4; i++)
			{
				_vertices.put(quad.vertices[i].x); _vertices.put(quad.vertices[i].y); _vertices.put(quad.vertices[i].z);
				_colors.put(quad.vertices[i].r);   _colors.put(quad.vertices[i].g);   _colors.put(quad.vertices[i].b);   _colors.put(quad.vertices[i].a);
				_uvs.put(quad.vertices[i].tx);     _uvs.put(quad.vertices[i].ty);
			}
			
			short offset = (short) (_primitives * 4);
			
			// first triangle, CWW 0, 1, 2
			_indices.put((short) (offset));
			_indices.put((short) (offset + 1));
			_indices.put((short) (offset + 2));
			
			// second triangle, CWW 0, 2, 3
			_indices.put((short) (offset));
			_indices.put((short) (offset + 2));
			_indices.put((short) (offset + 3));
			
			_primitives++;
		}
	}
	
	public final int Gfx_StartBatch(Primitive primitiveType, int texture, int blend)
	{
		GL10 gl = _renderer.GL();
		
		if (_vertices != null)
		{
			RenderBatch(false);
			_primitive = primitiveType;
			
			int primitiveSize = 4;
			
			if (_primitive == Primitive.Lines)
				primitiveSize = 2;
			else if (_primitive == Primitive.Triples)
				primitiveSize = 3;
			else if (_primitive == Primitive.Quads)
				primitiveSize = 4;
			
			if (_blending != blend)
				SetBlendMode(blend);
			
			if (_texture != texture)
			{
				gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
				_texture = texture;
			}
			
			return (VERTEX_BUFFER_SIZE / primitiveSize);
		}
		
		return 0;
	}
	
	public final void Gfx_FinishBatch(int nprim)
	{
		_primitives = nprim;
	}
	
	public final void Gfx_SetTransform()
	{
		Gfx_SetTransform(0f);
	}

	public final void Gfx_SetTransform(float x)
	{
		Gfx_SetTransform(x, 0f);
	}

	public final void Gfx_SetTransform(float x, float y)
	{
		Gfx_SetTransform(x, y, 0f);
	}

	public final void Gfx_SetTransform(float x, float y, float dx)
	{
		Gfx_SetTransform(x, y, dx, 0f);
	}

	public final void Gfx_SetTransform(float x, float y, float dx, float dy)
	{
		Gfx_SetTransform(x, y, dx, dy, 0f);
	}

	public final void Gfx_SetTransform(float x, float y, float dx, float dy, float rot)
	{
		Gfx_SetTransform(x, y, dx, dy, rot, 0f);
	}

	public final void Gfx_SetTransform(float x, float y, float dx, float dy, float rot, float hscale)
	{
		Gfx_SetTransform(x, y, dx, dy, rot, hscale, 0f);
	}

	public final void Gfx_SetTransform(float x, float y, float dx, float dy, float rot, float hscale, float vscale)
	{
		GL10 gl = _renderer.GL();

		RenderBatch(false);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		if (vscale != 0)
		{
			gl.glTranslatef(-x, -y, 0.0f);
			gl.glScalef(hscale, vscale, 1.0f);
			gl.glRotatef(rot, 0.0f, 0.0f, 1.0f);
			gl.glTranslatef(x + dx, y + dy, 0.0f);
		}

		RenderBatch(false);
	}

	private final void RenderBatch(boolean endScene)
	{
		GL10 gl = _renderer.GL();

		if (_vertices != null)
		{
			if (_primitives > 0)
			{
				_drawCalls++;
				
				gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
				gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
				
				if (_primitive != Primitive.Lines)
					gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

				_vertices.position(0);
				_colors.position(0);
				_uvs.position(0);

				gl.glVertexPointer(3, GL10.GL_FLOAT, 0, _vertices);
				gl.glColorPointer(4, GL10.GL_FLOAT, 0, _colors);
				
				if (_primitive != Primitive.Lines)
					gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, _uvs);

				int totalIndices = _indices.position();
				_indices.position(0);
				
				if (_primitive == Primitive.Lines)
					gl.glDrawElements(GL10.GL_LINES, totalIndices, GL10.GL_UNSIGNED_SHORT, _indices);
				else
					gl.glDrawElements(GL10.GL_TRIANGLES, totalIndices, GL10.GL_UNSIGNED_SHORT, _indices);

				gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
				gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
				
				if (_primitive != Primitive.Lines)
					gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				
				_primitives = 0;
			}

			_vertices.clear();
			_colors.clear();
			_uvs.clear();
			
			_indices.clear();
			
			_texture = 0;
			gl.glBindTexture(GL10.GL_TEXTURE_2D, _texture);
		}
	}

	private int _blending;
	
	private void SetBlendMode(int blend)
	{
		GL10 gl = _renderer.GL();

		if ((blend & DGE_BLEND_ALPHABLEND) != (_blending & DGE_BLEND_ALPHABLEND))
		{
			if ((blend & DGE_BLEND_ALPHABLEND) == DGE_BLEND_ALPHABLEND)
			{
				gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			}
			else
			{
				gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
			}
		}

		if ((blend & DGE_BLEND_COLORADD) != (_blending & DGE_BLEND_COLORADD))
		{
			if ((blend & DGE_BLEND_COLORADD) == DGE_BLEND_COLORADD)
			{
				gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_ADD);
			}
			else
			{
				gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
			}
		}

		if ((blend & DGE_BLEND_ZWRITE) != (_blending & DGE_BLEND_ZWRITE))
		{
			if ((blend & DGE_BLEND_ZWRITE) == DGE_BLEND_ZWRITE)
			{
				gl.glEnable(GL10.GL_DEPTH_TEST);
				gl.glDepthMask(true);
			}
			else
			{
				gl.glDisable(GL10.GL_DEPTH_TEST);
				gl.glDepthMask(false);
			}
		}

		_blending = blend;
	}
	
	private Bitmap SavePixels(int x, int y, int w, int h)
	{
		GL10 gl = _renderer.GL();
		
	    int b[] = new int[w * h];
	    IntBuffer ib = IntBuffer.wrap(b);
	    ib.position(0);
	    
	    gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);
	
	    // The bytes within the ints are in the wrong order for android, but convert into a
	    // bitmap anyway. They're also bottom-to-top rather than top-to-bottom. We'll fix
	    // this up soon using some fast API calls.
	    Bitmap glbitmap = Bitmap.createBitmap(b, w, h, Bitmap.Config.ARGB_4444);
	    
	    ib = null; // we're done with ib
	    b = null; // we're done with b, so allow the memory to be freed
	
	    // To swap the color channels, we'll use a ColorMatrix/ColorMatrixFilter. From the Android docs:
	    //
	    // This is a 5x4 matrix: [ a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t ]
	    // When applied to a color [r, g, b, a] the resulting color is computed as (after clamping):
	    //
	    // R' = a*R + b*G + c*B + d*A + e;
	    // G' = f*R + g*G + h*B + i*A + j;
	    // B' = k*R + l*G + m*B + n*A + o;
	    // A' = p*R + q*G + r*B + s*A + t;
	    //
	    // We want to swap R and B, so the coefficients will be:
	    // R' = B => 0,0,1,0,0
	    // G' = G => 0,1,0,0,0
	    // B' = R => 1,0,0,0,0
	    // A' = A => 0,0,0,1,0
	
	    final float[] cmVals = { 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0 };
	
	    Paint paint = new Paint();
	    paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(cmVals))); // our R<->B swapping paint
	
	    Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444); // the bitmap we're going to draw onto
	    
	    Canvas canvas = new Canvas(bitmap); // we draw to the bitmap through a canvas
	    canvas.drawBitmap(glbitmap, 0, 0, paint); // draw the opengl bitmap onto the canvas, using the color swapping paint
	    
	    glbitmap = null; // we're done with glbitmap, let go of its memory
	
	    // the image is still upside-down, so vertically flip it
	    Matrix matrix = new Matrix();
	    matrix.preScale(1.0f, -1.0f); // scaling: x = x, y = -y, i.e. vertically flip
	    
	    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // new bitmap, using the flipping matrix
	}
	
	private AssetManager _assets;
	
	private AssetManager GetManager()
	{
		if (_assets == null && _interface != null)
			_assets = _interface.getAssets();
		
		return _assets;
	}
	
	private HashMap<String, DGETexture> _textures = null; 
	
	public final int Texture_Create()
	{
		GL10 gl = _renderer.GL();
		
		IntBuffer textureId = IntBuffer.allocate(1);
		gl.glGenTextures(1, textureId);
		
		return textureId.get(0);
	}
	
	public final int Texture_Load(String filename)
	{
		return Texture_Load(filename, 0);
	}
	
	public final int Texture_Load(String filename, float hue)
	{
		return Texture_Load(filename, hue, 0);
	}
	
	public final int Texture_Load(String filename, float hue, float saturation)
	{
		return Texture_Load(filename, hue, saturation, 0);
	}
	
	public final int Texture_Load(String filename, float hue, float saturation, float brightness)
	{
		GL10 gl = _renderer.GL();
		
		if (GetManager() == null || gl == null)
			return 0;
		
		if (_textures == null)
			_textures = new HashMap<String, DGETexture>();
		
		String filenameWithTint = String.format("%s-%.2f-%.2f-%.2f", filename, hue, saturation, brightness);
		
		DGETexture current = _textures.get(filenameWithTint);
		
		if (current != null)
			return current.id;
		
		Bitmap bitmap = LoadTexture(filename, hue, saturation, brightness);
		
		if (bitmap == null)
			return 0;
		
		DGETexture texture = new DGETexture();
		
		texture.width = bitmap.getWidth();
		texture.height = bitmap.getHeight();
		
		texture.id = LoadTextureInto(0, bitmap);
		
		texture.filename = filename;
		
		texture.hue = hue;
		texture.saturation = saturation;
		texture.brightness = brightness;
		
		_textures.put(filenameWithTint, texture);
		
		bitmap.recycle();
		
		return texture.id;
	}

	public final void Texture_Free(int textureId)
	{
		String texture = GetTextureName(textureId);
		
		if (texture != null)
			_textures.remove(texture);
		
		GL10 gl = _renderer.GL();
		
		if (textureId != 0)
			gl.glDeleteTextures(1, new int[] { textureId }, 0);
	}
	
	public final int Texture_GetWidth(int textureId)
	{
		return Texture_GetWidth(textureId, false);
	}

	public final int Texture_GetWidth(int textureId, boolean original)
	{
		int width = 0;
		
		if (original)
		{
			DGETexture texItem = GetTexture(textureId);
			
			if (texItem != null)
				width = texItem.width;
		}
		else
		{
			width = FixedTextureSize(Texture_GetWidth(textureId, true));
		}
		
		return width;
	}

	public final int Texture_GetHeight(int textureId)
	{
		return Texture_GetHeight(textureId, false);
	}

	public final int Texture_GetHeight(int textureId, boolean original)
	{
		int height = 0;
		
		if (original)
		{
			DGETexture texItem = GetTexture(textureId);
			
			if (texItem != null)
				height = texItem.height;
		}
		else
		{
			height = FixedTextureSize(Texture_GetHeight(textureId, true));
		}
		
		return height;
	}

	private String GetTextureName(int textureId)
	{
		Iterator<Entry<String, DGETexture>> it = _textures.entrySet().iterator();
		
		while (it.hasNext())
		{
			Map.Entry<String, DGETexture> pair = it.next();
			DGETexture target = pair.getValue();
			
			if (textureId == 0 || target.id == textureId)
			{
				return pair.getKey();
			}
		}
		
		return null;
	}

	private DGETexture GetTexture(int textureId)
	{
		Iterator<Entry<String, DGETexture>> it = _textures.entrySet().iterator();
		
		while (it.hasNext())
		{
			Map.Entry<String, DGETexture> pair = it.next();
			DGETexture target = pair.getValue();
			
			if (textureId == 0 || target.id == textureId)
			{
				return target;
			}
		}
		
		return null;
	}
	
	private int FixedTextureSize(int size)
	{
		int nsize = 1;
		
		while (nsize < size)
		{
			nsize *= 2;
		}
		
		return nsize;
	}
	
	private final void RefreshTexture(int textureId)
	{
		if (_textures == null)
			return;
		
		Vector<Integer> free = new Vector<Integer>();
		
		for (Entry<String, DGETexture> pair : _textures.entrySet())
		{
			DGETexture target = pair.getValue();
			
			if (textureId == 0 || target.id == textureId)
			{
				Bitmap bitmap = LoadTexture(target.filename, target.hue, target.saturation, target.brightness);
				
				if (bitmap == null)
				{
					free.add(target.id);
				}
				else
				{
					LoadTextureInto(target.id, bitmap);
					bitmap.recycle();
				}
			}
		}
		
		for (int count = 0; count < free.size(); count++)
		{
			Texture_Free(free.get(count));
		}
	}
	
	private final int LoadTextureInto(int textureId, Bitmap data)
	{
		GL10 gl = _renderer.GL();
		
		if (textureId == 0)
			textureId = Texture_Create();
			
		int width = data.getWidth();
		int height = data.getHeight();
		int scaledWidth = FixedTextureSize(width);
		int scaledHeight = FixedTextureSize(height);

		if (width < scaledWidth || height < scaledHeight)
		{
			Bitmap scaledBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, data.getConfig());
			
			Canvas canvas = new Canvas(scaledBitmap);
			canvas.drawBitmap(data, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
			data.recycle();
			
			data = scaledBitmap;
		}
		
		IntBuffer prevTexture = IntBuffer.allocate(1);
		gl.glGetIntegerv(GL11.GL_TEXTURE_BINDING_2D, prevTexture);
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, data, 0);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, prevTexture.get(0));
		
		return textureId;
	}
	
	private final Bitmap LoadTexture(String filename, float hue, float saturation, float brightness)
	{
		if (GetManager() == null)
			return null;
		
		try
		{
			DGEResource resource = Resource_Load(filename, false);
			
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inScaled = false;
			opts.inMutable = true;
			
			Bitmap bitmap = BitmapFactory.decodeByteArray(resource.data, 0, resource.data.length, opts);

			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			
			// make adjustments if required
			if (hue != 0f || saturation != 0f || brightness != 0f)
			{
				int[] pixels = new int[width * height];
				bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
				
				float[] hsv = new float[3];
				
				hue = Math.max(-180f, Math.min(hue, 180f));
				saturation = Math.max(-1f, Math.min(saturation, 1f));
				brightness = Math.max(-1f, Math.min(brightness, 1f));
				
				for (int y = 0; y < height; y++)
				{
					for (int x = 0; x < width; x++)
					{
						int color = pixels[x + y * width];
						
						int red = Color.red(color);
						int green = Color.green(color);
						int blue = Color.blue(color);
						int alpha = Color.alpha(color);
						
						Color.RGBToHSV(red, green, blue, hsv);
						
						// add the hue, rotate it around to be from 0 to 360
						hsv[0] += hue;
						if (hsv[0] > 360f)
							hsv[0] -= 360f;
						if (hsv[0] < 0f)
							hsv[0] += 360f;
						
						// add the saturation, clamp it between 0 and 1
						hsv[1] += saturation;
						hsv[1] = Math.max(0, Math.min(hsv[1], 1f));
	
						// add the brightness, clamp it between 0 and 1
						hsv[2] += brightness;
						hsv[2] = Math.max(0, Math.min(hsv[2], 1f));
						
						pixels[x + y * width] = Color.HSVToColor(alpha, hsv);
					}
				}
				
				bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			}
			
			resource = null;
			System.gc();
			
			return bitmap;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private World _world;
	private Vec2 _gravity;
	private int _ppm; // pixels per meter
	private boolean _physicsEnabled = false;
	
	public boolean Physics_Initialize(float dx, float dy, int ppm)
	{
		return Physics_Initialize(dx, dy, ppm, true);
	}
	
	public boolean Physics_Initialize(float dx, float dy, int ppm, boolean sleep)
	{
		_gravity = new Vec2(dx, dy);
		_world = new World(_gravity, sleep);
		_ppm = ppm;
		
		_physicsEnabled = true;
		
		return true;
	}
	
	public World Physics_GetWorld()
	{
		if (!_physicsEnabled)
			return null;
		
		return _world;
	}
	
	private int _velocityIterations = 6;
	private int _positionIterations = 2;
	
	public void Physics_SetAccuracy(int velocityIterations, int positionIterations)
	{
		if (!_physicsEnabled)
			return;
		
		_velocityIterations = velocityIterations;
		_positionIterations = positionIterations;
	}
	
	public void Physics_SetContactListener(ContactListener listener)
	{
		_world.setContactListener(listener);
	}
	
	public void Physics_SetGravity(float x, float y)
	{
		if (!_physicsEnabled)
			return;
		
		Vec2 gravity = new Vec2(x, y);
		
		if (!_gravity.equals(gravity))
		{
			_gravity = gravity;
			_world.setGravity(_gravity);
		}
	}
	
	public Vec2 Physics_GetGravity()
	{
		if (!_physicsEnabled)
			return null;
		
		return _world.getGravity();
	}
	
	public Vec2 Physics_ScreenToWorld(DGEVector pos)
	{
		float x = pos.x * (1f / (float) _ppm);
		float y = pos.y * (1f / (float) _ppm);
		
		return new Vec2(x, y);
	}
	
	public DGEVector Physics_WorldToScreen(Vec2 pos)
	{
		float x = pos.x * (float) _ppm;
		float y = pos.y * (float) _ppm;
		
		return new DGEVector(x, y);
	}
	
	public DGEVector Physics_GetBodyPosition(Body body)
	{
		return Physics_WorldToScreen(body.getPosition());
	}
	
	public Body Physics_CreateFixedBody(DGEVector[] points)
	{
		if (!_physicsEnabled)
			return null;
		
		// need at least 3 pairs (a triangle)
		if (points.length < 3)
			return null;
				
		Vec2[] vertices = new Vec2[points.length];
		
		for (int i = 0; i < vertices.length; i++)
		{
			vertices[i] = new Vec2();
			
			// convert from pixels to meters
			vertices[i].x = (points[i].x * (1f / (float) _ppm));
			vertices[i].y = (points[i].y * (1f / (float) _ppm));
		}
		
		PolygonShape shape = new PolygonShape();
		shape.set(vertices, vertices.length);

		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(points[0].x, points[0].y);
		
		Body body = _world.createBody(bodyDef);
		body.createFixture(shape, 0.0f);
		
		return body;
	}
	
	public Body Physics_CreateFixedBody(float[] points)
	{
		if (!_physicsEnabled)
			return null;
		
		// need at least 3 pairs (a triangle) and have an even count
		if (points.length < 6 && points.length % 2 == 0)
			return null;
		
		Vec2[] vertices = new Vec2[points.length / 2];
		
		for (int i = 0; i < vertices.length; i++)
		{
			vertices[i] = new Vec2();
			
			// convert from pixels to meters
			vertices[i].x = (points[i * 2 + 0] * (1f / (float) _ppm));
			vertices[i].y = (points[i * 2 + 1] * (1f / (float) _ppm));
		}
		
		PolygonShape shape = new PolygonShape();
		shape.set(vertices, vertices.length);

		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(points[0], points[1]);
		
		Body body = _world.createBody(bodyDef);
		body.createFixture(shape, 0.0f);
		
		return body;
	}
	
	public Body Physics_CreateFixedBody(ShapeType type, float x, float y, float w, float h)
	{
		if (!_physicsEnabled)
			return null;
		
		Shape shape = null;
		
		// offset x and y to center of object
		x += w / 2;
		y += h / 2;

		// convert from pixels to meters
		float xm = (x * (1f / (float) _ppm));
		float ym = (y * (1f / (float) _ppm));
		float wm = (w * (1f / (float) _ppm)) / 2f;
		float hm = (h * (1f / (float) _ppm)) / 2f;
		
		switch (type)
		{
			case CIRCLE:
			{
				shape = new CircleShape();
				shape.m_radius = wm;
			} break;
			
			case POLYGON:
			{
				shape = new PolygonShape();
				((PolygonShape) shape).setAsBox(wm, hm);
			} break;
		}
		
		if (shape == null)
			return null;
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(xm, ym);
		
		Body body = _world.createBody(bodyDef);
		body.createFixture(shape, 0.0f);
		
		return body;
	}

	public Body Physics_CreateDynamicBody(ShapeType type, float x, float y, float w, float h, float mass, float friction)
	{
		return Physics_CreateDynamicBody(type, x, y, w, h, mass, friction, false);
	}
	
	public Body Physics_CreateDynamicBody(ShapeType type, float x, float y, float w, float h, float mass, float friction, boolean fixedRotation)
	{
		if (!_physicsEnabled)
			return null;
		
		Shape shape = null;
		
		// offset x and y to center of object
		x += w / 2;
		y += h / 2;

		// convert from pixels to meters
		float xm = (x * (1f / (float) _ppm));
		float ym = (y * (1f / (float) _ppm));
		float wm = (w * (1f / (float) _ppm)) / 2f;
		float hm = (h * (1f / (float) _ppm)) / 2f;
		
		switch (type)
		{
			case CIRCLE:
			{
				shape = new CircleShape();
				shape.m_radius = wm;
			} break;
			
			case POLYGON:
			{
				shape = new PolygonShape();
				((PolygonShape) shape).setAsBox(wm, hm);
			} break;
		}
		
		if (shape == null)
			return null;
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position.set(xm, ym);
		bodyDef.allowSleep = true;
		bodyDef.fixedRotation = fixedRotation;
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = mass;
		fixtureDef.friction = friction;
		
		Body body = _world.createBody(bodyDef);
		body.createFixture(fixtureDef);
		
		return body;
	}
	
	public Body Physics_CreateTrigger(ShapeType type, float x, float y, float w, float h)
	{
		if (!_physicsEnabled)
			return null;
		
		Body body = null;
		BodyDef bodyDef = null;
		FixtureDef fixtureDef = null;
		Shape shape = null;
		
		// offset x and y to center of object
		x += w / 2;
		y += h / 2;

		// convert from pixels to meters
		float xm = (x * (1f / (float) _ppm));
		float ym = (y * (1f / (float) _ppm));
		float wm = (w * (1f / (float) _ppm)) / 2f;
		float hm = (h * (1f / (float) _ppm)) / 2f;
		
		switch (type)
		{
			case CIRCLE:
			{
				shape = new CircleShape();
				shape.m_radius = wm;
			} break;
			
			case POLYGON:
			{
				shape = new PolygonShape();
				((PolygonShape) shape).setAsBox(wm, hm);
			} break;
		}
		
		bodyDef = new BodyDef();
		bodyDef.position.set(xm, ym);
		
		fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.isSensor = true;
		
		body = _world.createBody(bodyDef);
		body.createFixture(fixtureDef);
		
		return body;
	}
	
	public void Physics_Update(float delta)
	{
		if (!_physicsEnabled || delta == 0)
			return;
		
		_world.step(delta, _velocityIterations, _positionIterations);
	}
	
	public void Physics_Render(int type)
	{
		Physics_Render(type, new DGEVector());
	}
	
	public void Physics_Render(int type, DGEVector offset)
	{
		if (!_physicsEnabled)
			return;
		
		RenderPhysicsWorld(type, offset);
	}
	
	public DGEVector[] Physics_GetVectors(Body body)
	{
		return Physics_GetVectors(body, new DGEVector());
	}
	
	public DGEVector[] Physics_GetVectors(Body body, DGEVector offset)
	{
		Fixture fixture = body.getFixtureList();
		
		while (fixture != null && fixture.m_isSensor)
		{
			fixture = fixture.m_next;
		}
		
		if (fixture == null)
			fixture = body.getFixtureList();
		
		PolygonShape poly = (PolygonShape) fixture.m_shape;
		Vec2[] vertices = new Vec2[poly.getVertexCount()];
		
		for (int j = 0; j < poly.getVertexCount(); j++)
		{
			Vec2 position = poly.getVertex(j);
			
			float angle = body.getAngle();
			float nx = FloatMath.cos(angle) * position.x - FloatMath.sin(angle) * position.y;
			float ny = FloatMath.sin(angle) * position.x + FloatMath.cos(angle) * position.y;
			
			vertices[j] = new Vec2(nx + body.getPosition().x, ny + body.getPosition().y);
		}
		
		return WorldToScreen(vertices, offset);
	}
	
	public static final int DGE_PHYSICS_DYNAMIC = 1;
	public static final int DGE_PHYSICS_KINEMATIC = 2;
	public static final int DGE_PHYSICS_STATIC = 4;
	public static final int DGE_PHYSICS_AABB = 8;
	public static final int DGE_PHYSICS_FORCE = 16;
	public static final int DGE_PHYSICS_DIRECTION = 32;
	public static final int DGE_PHYSICS_ALL = 63;
	
	private void RenderPhysicsWorld(int type, DGEVector offset)
	{
		Body body = _world.getBodyList();
		
		while (body != null)
		{
			DGEColor color = new DGEColor(0, 0, 0, 0);
			boolean renderBody = true;
			
			switch (body.m_type)
			{
				case DYNAMIC:
				{
					if ((type & DGE_PHYSICS_DYNAMIC) != DGE_PHYSICS_DYNAMIC)
						renderBody = false;
					
					if (body.isAwake())
						color = new DGEColor(0, 0.85f, 0, 1);
					else
						color = new DGEColor(0.5f, 0.5f, 0.5f, 1);
				} break;
				
				case KINEMATIC:
				{
					if ((type & DGE_PHYSICS_KINEMATIC) != DGE_PHYSICS_KINEMATIC)
						renderBody = false;
				} break;
				
				case STATIC:
				{
					if ((type & DGE_PHYSICS_STATIC) != DGE_PHYSICS_STATIC)
						renderBody = false;
					
					color = new DGEColor(1, 1, 1, 1);
				} break;
			}
			
			Fixture fixture = body.getFixtureList();
			
			while (fixture != null)
			{
				if (renderBody)
				{
					switch (fixture.m_shape.m_type)
					{
						case CIRCLE:
						{
							float radius = fixture.m_shape.m_radius * (float) _ppm;
							RenderCircle(Physics_GetBodyPosition(body).subtract(offset), radius, -body.getAngle(), color);
						} break;
						
						case POLYGON:
						{
							PolygonShape poly = (PolygonShape) fixture.m_shape;
							Vec2[] vertices = new Vec2[poly.getVertexCount()];
							
							for (int j = 0; j < poly.getVertexCount(); j++)
							{
								Vec2 position = poly.getVertex(j);
								
								float angle = body.getAngle();
								float nx = FloatMath.cos(angle) * position.x - FloatMath.sin(angle) * position.y;
								float ny = FloatMath.sin(angle) * position.x + FloatMath.cos(angle) * position.y;
								
								vertices[j] = new Vec2(nx + body.getPosition().x, ny + body.getPosition().y);
							}
							
							RenderPolygon(WorldToScreen(vertices, offset), color);
						} break;
					}
				}
				
				if ((type & DGE_PHYSICS_AABB) == DGE_PHYSICS_AABB)
				{
					AABB layout = fixture.getAABB();
					Vec2[] vertices = new Vec2[4];
					
					for (int j = 0; j < 4; j++)
						vertices[j] = new Vec2();
					
					layout.getVertices(vertices);
					
					color = new DGEColor(0, 0, 0.85f, 1);
					RenderPolygon(WorldToScreen(vertices, offset), color);
				}
				
				fixture = fixture.getNext();
			}
			
			if ((type & DGE_PHYSICS_FORCE) == DGE_PHYSICS_FORCE && body.isAwake())
			{
				DGEVector source = Physics_WorldToScreen(body.getPosition());
				DGEVector destination = Physics_WorldToScreen(body.m_force.add(body.getPosition()));
				
				Gfx_RenderLine(source.x, source.y, destination.x, destination.y, 1f, 0, 0, 1);
			}
			
			if ((type & DGE_PHYSICS_DIRECTION) == DGE_PHYSICS_DIRECTION && body.isAwake())
			{
				DGEVector source = Physics_WorldToScreen(body.getPosition());
				DGEVector destination = Physics_WorldToScreen(body.m_linearVelocity.add(body.getPosition()));
				
				Gfx_RenderLine(source.x, source.y, destination.x, destination.y, 0.9f, 0.9f, 0, 1);
			}
			
			body = body.getNext();
		}
	}
	
	private DGEVector[] WorldToScreen(Vec2[] positions, DGEVector offset)
	{
		DGEVector[] vertices = new DGEVector[positions.length];
		
		for (int i = 0; i < positions.length; i++)
		{
			vertices[i] = Physics_WorldToScreen(positions[i]).subtract(offset);
		}
		
		return vertices;
	}
	
	private void RenderPolygon(DGEVector[] vertices, DGEColor color)
	{
		for (int i = 0; i < vertices.length; i++)
		{
			int nextVertex = (i + 1) % vertices.length;
			
			Gfx_RenderLine(vertices[i].x,
						   vertices[i].y,
						   vertices[nextVertex].x,
						   vertices[nextVertex].y,
						   color.r, color.g, color.b, color.a);
		}
		
	}
		
	private void RenderCircle(DGEVector position, float radius, float angle, DGEColor color)
	{
		int points = 24 + (int) radius / 20;
		float wedge = ((2f * DGE.M_PI) / (float) points);
		float theta = 0, x = 0, y = 0;
		
		DGEVector[] vertices = new DGEVector[points + 2];
		
		vertices[0] = position;
		
		for (int i = 0; i < points; i++)
		{
			theta = i * wedge + angle;
			
			x = (float) (position.x + radius * FloatMath.cos(theta));
			y = (float) (position.y - radius * FloatMath.sin(theta));
			
			vertices[i + 1] = new DGEVector(x, y);
		}

		x = (float) (position.x + radius * FloatMath.cos(angle));
		y = (float) (position.y - radius * FloatMath.sin(angle));
		
		vertices[points + 1] = new DGEVector(x, y);
		
		RenderPolygon(vertices, color);
	}
	
	private DGEInterface _interface;
	
	// disable direct creation of DGE object
	private static DGE _instance;
	private DGE() 
	{ 
		_interface = null;

		_view = null;
		_renderer = null;
	}
	
	private DGEView _view;
	private DGERenderer _renderer;

	private final void CreateView()
	{
		if (_interface != null && _view == null)
		{
			_view = new DGEView(_interface);
			_renderer = new DGERenderer();
			
			_view.setRenderer(_renderer);
		}
	}
}
