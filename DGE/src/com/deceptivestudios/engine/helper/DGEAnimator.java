package com.deceptivestudios.engine.helper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.internal.DGEResource;

public class DGEAnimator extends DGESprite
{
	public static final int Forward  = 0;
	public static final int Reverse  = 1;
	public static final int PingPong = 2;
	public static final int Loop     = 4;
	
	public static final int None = 0;
	public static final int Up = 1;
	public static final int Down = 2;
	public static final int Left = 4;
	public static final int Right = 8;
	
	private static final String HeaderTag = "[DGEANIMATOR]";
	private static final String TextureTag = "Texture";
	private static final String AnimationTag = "[Animation]";
	private static final String DataTag = "Data";
	private static final String FrameTag = "Frame";
	private static final String HotspotTag = "Hotspot";
	
	// default texture
	private int _defaultTexture;
	
	private String _activeAnimation;
	private int _activeTexture;
	
	private boolean _playing;

	private float _speed;
	private float _sinceLastFrame;

	private int _mode;
	private int	_delta;
	private int	_frames;
	private int _curFrame;
	
	private HashMap<String, AnimationData> _animations;
	private DGE _dge;
	private String _path;
	private boolean _valid;

	public DGEAnimator(String filename, float x, float y)
	{
		this();
		
		if (filename.contains("/"))
			_path = filename.substring(0, filename.lastIndexOf("/")) + "/";
		else
			_path = "";
		
		DGEResource resource = _dge.Resource_Load(filename);
		
		if (resource == null)
			return;
		
		ByteArrayInputStream bais = new ByteArrayInputStream(resource.data);
		BufferedReader reader = new BufferedReader(new InputStreamReader(bais));
		
		String line;
		boolean headerFound = false;
		boolean animationFound = false;
		boolean animDataFound = false;
		
		String currentAnim = "";
		
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
				else if (line.equalsIgnoreCase(AnimationTag))
				{
					animationFound = true;
					animDataFound = false;
				}
				else
				{
					String[] detail = line.split("=");
					
					if (detail.length == 2)
					{
						String command = detail[0];
						String data = detail[1];
						
						if (command.equalsIgnoreCase(TextureTag))
						{
							_defaultTexture = _dge.Texture_Load(_path + data);
							
							if (_defaultTexture == 0)
							{
								_dge.System_Log("Animator '%s' is missing the texture '%s'", filename, (_path + data));
								return;
							}
							else
							{
								SetTexture(_defaultTexture);
								_activeTexture = _defaultTexture;
							}
						}
						else if (animationFound)
						{
							if (!animDataFound)
							{
								if (command.equalsIgnoreCase(DataTag))
								{
									animDataFound = true;
									
									String[] animData = data.split(",");
									
									switch (animData.length)
									{
										case 1:
										{
											AddAnimation(data);
											currentAnim = data;
										} break;
										
										case 2:
										{
											AddAnimation(animData[0], -1, Integer.parseInt(animData[1]));
											currentAnim = animData[0];
										} break;
										
										case 3:
										{
											AddAnimation(animData[0], -1, Integer.parseInt(animData[1]), Integer.parseInt(animData[2]));
											currentAnim = animData[0];
										} break;
										
										case 4:
										{
											int texture = _dge.Texture_Load(_path + animData[3]);
											
											if (texture == 0)
											{
												_dge.System_Log("Animator '%s' is missing the texture '%s'", filename, (_path + animData[3]));
												return;
											}
											else
											{
												AddAnimation(animData[0], texture, Integer.parseInt(animData[1]), Integer.parseInt(animData[2]));
												currentAnim = animData[0];
											}
										} break;
									}
								}
							}
							else if (command.equalsIgnoreCase(FrameTag))
							{
								String[] frameData = data.split(",");
								
								if (frameData.length != 4 || !AddFrame(currentAnim, Float.parseFloat(frameData[0]), Float.parseFloat(frameData[1]), Float.parseFloat(frameData[2]), Float.parseFloat(frameData[3])))
								{
									_dge.System_Log("Animator '%s' has a frame with errors", filename);
									return;
								}
							}
							else if (command.equalsIgnoreCase(HotspotTag))
							{
								String[] hotspotData = data.split(",");
								
								if (hotspotData.length != 2 || !SetAnimationHotSpot(currentAnim, Float.parseFloat(hotspotData[0]), Float.parseFloat(hotspotData[1])))
								{
									_dge.System_Log("Animator '%s' has hotspot data with errors", filename);
									return;
								}
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
	
	public void Play()
	{
		if (!_valid)
			return;
		
		if (IsPlaying())
			return;
		
		if (_activeAnimation.length() == 0)
			return;
		
		_playing = true;
		_sinceLastFrame = -1.0f;
		
		if ((_mode & Reverse) == Reverse)
		{
			_delta = -1;
			SetFrame(_frames - 1);
		}
		else
		{
			_delta = 1;
			SetFrame(0);
		}
	}
	
	public void Play(String animation)
	{
		if (_activeAnimation != animation)
		{
			AnimationData data = GetAnimation(animation);
			
			if (data != null)
			{
				_activeAnimation = animation;
				
				int texture = data.GetTexture();
				
				// change texture if required
				if ((texture != -1 && _activeTexture != texture) || 
					(texture == -1 && _activeTexture != _defaultTexture))
				{
					if (texture == -1)
						SetTexture(_defaultTexture);
					else
						SetTexture(texture);
					
					_activeTexture = GetTexture();
				}
				
				_mode = data.GetMode();
				_speed = data.GetSpeed();
				_frames = data.GetAnimationLength();
				
				DGEVector hotspot = data.GetHotSpot();
				
				SetHotSpot(hotspot.x, hotspot.y);
				
				_playing = false;
			}
		}
		
		Play();
	}
	
	public boolean HasAnimations()
	{
		return !_animations.isEmpty();
	}
	
	public void Stop() 
	{ 
		_playing = false;
	}
	
	public void Resume() 
	{
		if (_activeAnimation.length() > 0)
			_playing = true; 
	}
	
	public void Update(float deltaTime)
	{
		if (!_valid)
			return;
		
		if (!_playing)
			return;

		if (_sinceLastFrame == -1.0f)
			_sinceLastFrame = 0.0f;
		else
			_sinceLastFrame += deltaTime;
		
		while (_sinceLastFrame >= _speed)
		{
			_sinceLastFrame -= _speed;

			if (_curFrame + _delta == _frames)
			{
				switch (_mode)
				{
					case Forward:
					case Reverse | PingPong:
					{
						_playing = false;
					} break;

					case Forward | PingPong:
					case Forward | PingPong | Loop:
					case Reverse | PingPong | Loop:
					{
						_delta = -_delta;
					} break;
				}
			}
			else if (_curFrame + _delta < 0)
			{
				switch(_mode)
				{
					case Reverse:
					case Forward | PingPong:
					{
						_playing = false;
					} break;

					case Reverse | PingPong:
					case Reverse | PingPong | Loop:
					case Forward | PingPong | Loop:
					{
						_delta = -_delta;
					} break;
				}
			}

			if (_playing) 
				SetFrame(_curFrame + _delta);
		}
	}
	
	public String GetAnimation()
	{
		return _activeAnimation;
	}
	
	public boolean IsPlaying() 
	{ 
		return _playing; 
	}
	
	public boolean IsPlaying(String animation)
	{
		if (_activeAnimation == animation)
			return IsPlaying();
		
		return false;
	}
	
	public void SetFrame(int n)
	{
		if (!_valid)
			return;
		
		float tx1, ty1, tx2, ty2;
		boolean bX, bY, bHS;

		n = n % _frames;
		
		if (n < 0) 
			n = _frames + n;
		
		_curFrame = n;
		
		DGERect frame = GetFrame(_activeAnimation, _curFrame);
		
		if (frame != null)
		{
			tx1 = frame.p1.x;	ty1 = frame.p1.y;
			tx2 = frame.p2.x;	ty2 = frame.p2.y;
	
			_width = tx2 - tx1;
			_height = ty2 - ty1;
			
			tx1 /= _tex_width;	ty1 /= _tex_height;
			tx2 /= _tex_width;	ty2 /= _tex_height;
	
			_quad.SetTextureOffset(tx1, ty1, tx2, ty2);
	
			bX = _bXFlip; bY = _bYFlip; bHS = _bHSFlip;
			_bXFlip = false; _bYFlip = false;
			
			SetFlip(bX, bY, bHS);
		}
	}

	public int GetMode() 
	{
		return _mode; 
	}
	
	public float GetSpeed()
	{ 
		return (1.0f / _speed); 
	}
	
	public int GetFrame()
	{ 
		return _curFrame; 
	}
	
	private boolean AddAnimation(String animation)
	{
		return AddAnimation(animation, -1, 15);
	}
	
	private boolean AddAnimation(String animation, int texture, int fps)
	{
		return AddAnimation(animation, texture, fps, Forward);
	}
	
	private boolean AddAnimation(String animation, int texture, int fps, int mode)
	{
		if (!_animations.containsKey(animation))
			_animations.put(animation, new AnimationData(texture, fps, mode));
		
		return _animations.containsKey(animation);
	}
	
	private AnimationData GetAnimation(String animation)
	{
		if (!_animations.containsKey(animation))
			return null;
		
		return _animations.get(animation);
	}
	
	private boolean AddFrame(String animation, float x1, float y1, float x2, float y2)
	{
		return AddFrame(animation, new DGEVector(x1, y1), new DGEVector(x2, y2));
	}
	
	private boolean AddFrame(String animation, DGEVector start, DGEVector end)
	{
		AnimationData data = GetAnimation(animation);
		
		if (data == null)
			return false;
		
		return data.AddFrame(start, end);
	}
	
	private DGERect GetFrame(String animation, int frame)
	{
		AnimationData data = GetAnimation(animation);
		
		if (data == null)
			return null;
		
		return data.GetFrame(frame);
	}

	private boolean SetAnimationHotSpot(String animation, float x, float y)
	{
		AnimationData data = GetAnimation(animation);
		
		if (data == null)
			return false;
		
		data.SetHotSpot(x, y);
		return true;
	}
	
	private DGEAnimator()
	{
		super(0, 0, 0, 1, 1);
		
		_dge = DGE.Interface(DGE.DGE_VERSION);
		_animations = new HashMap<String, AnimationData>();
		
		_valid = false;
	}
	
	private class AnimationData
	{
		private int _texture, _fps, _mode;
		private float _hotspotX, _hotspotY;
		private Vector<DGERect> _frames;
		
		public AnimationData(int texture, int fps, int mode)
		{
			_texture = texture;
			_fps = fps;
			_mode = mode;
			
			_hotspotX = 0;
			_hotspotY = 0;
			
			_frames = new Vector<DGERect>();
		}
		
		public void SetTexture(int texture)
		{
			_texture = texture;
		}
		
		public int GetTexture()
		{
			return _texture;
		}
		
		public void SetSpeed(int fps)
		{
			_fps = fps;
		}
		
		public float GetSpeed()
		{
			return (1.0f / (float) _fps);
		}
		
		public void SetMode(int mode)
		{
			_mode = mode;
		}
		
		public int GetMode()
		{
			return _mode;
		}
		
		public void SetHotSpot(float x, float y)
		{
			_hotspotX = x;
			_hotspotY = y;
		}
		
		public DGEVector GetHotSpot()
		{
			return new DGEVector(_hotspotX, _hotspotY);
		}
		
		public boolean AddFrame(DGEVector start, DGEVector end)
		{
			return _frames.add(new DGERect(start, end));
		}
		
		public DGERect GetFrame(int frame)
		{
			if (frame >= _frames.size())
				return null;
			
			return _frames.get(frame);
		}
		
		public int GetAnimationLength()
		{
			return _frames.size();
		}
	}
}
