package com.deceptivestudios.engine.helper;

public class DGEAnimation extends DGESprite
{
	public static final int Forward  = 0;
	public static final int Reverse  = 1;
	public static final int PingPong = 2;
	public static final int Loop     = 4;

	private int _orig_width;
	private boolean _playing;

	private float _speed;
	private float _sinceLastFrame;

	private int _mode;
	private int	_delta;
	private int	_frames;
	private int _curFrame;
	
	public DGEAnimation(int texture, int nframes, float fps, float x, float y, float w, float h)
	{
		this(texture, nframes, fps, x, y, w, h, Forward | Loop);
	}
	
	public DGEAnimation(int texture, int nframes, float fps, float x, float y, float w, float h, int mode)
	{
		super(texture, x, y, w, h);

		_orig_width = _dge.Texture_GetWidth(texture, true);

		_sinceLastFrame = -1.0f;
		_speed = 1.0f / fps;
		_playing = false;
		_frames = nframes;

		_mode = mode;
		_delta = 1;
		
		SetFrame(0);
	}
	
	public DGEAnimation(DGEAnimation copy)
	{
		super(copy);

		_orig_width	    = copy._orig_width;
		_playing        = copy._playing; 
		_speed          = copy._speed; 
		_sinceLastFrame = copy._sinceLastFrame;
		_mode           = copy._mode;
		_delta          = copy._delta;
		_frames         = copy._frames;
		_curFrame       = copy._curFrame;
	}
	
	public void Play()
	{
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
	
	public void Stop() 
	{ 
		_playing = false;
	}
	
	public void Resume() 
	{ 
		_playing = true; 
	}
	
	public void Update(float deltaTime)
	{
		if (!_playing) 
			return;

		if (_sinceLastFrame == -1.0f)
			_sinceLastFrame = 0.0f;
		else
			_sinceLastFrame += deltaTime;

		while(_sinceLastFrame >= _speed)
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
			else if(_curFrame + _delta < 0)
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
	
	public boolean IsPlaying() 
	{ 
		return _playing; 
	}

	public void SetTexture(int texture) 
	{ 
		super.SetTexture(texture); 
		_orig_width = _dge.Texture_GetWidth(texture, true); 
	}
	
	public void SetTextureRect(float x1, float y1, float x2, float y2) 
	{ 
		super.SetTextureRect(x1, y1, x2, y2); 
		SetFrame(_curFrame); 
	}
	
	public void SetMode(int mode)
	{
		_mode = mode;

		if ((mode & Reverse) == Reverse)
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
	
	public void SetSpeed(float fps)
	{ 
		_speed = 1.0f / fps; 
	}
	
	public void SetFrame(int n)
	{
		float tx1, ty1, tx2, ty2;
		boolean bX, bY, bHS;
		int ncols = (int) (_orig_width / _width);

		n = n % _frames;
		
		if (n < 0) 
			n = _frames + n;
		
		_curFrame = n;

		tx1 = _tx + n * _width;
		ty1 = _ty;

		if (tx1 > _orig_width - _width)
		{
			n -= (int) ((_orig_width - _tx) / _width);
			tx1 = _width * (n % ncols);
			ty1 += _height * (1 + n / ncols);
		}

		tx2 = tx1 + _width;
		ty2 = ty1 + _height;

		tx1 /= _tex_width;
		ty1 /= _tex_height;
		tx2 /= _tex_width;
		ty2 /= _tex_height;

		_quad.vertices[0].tx = tx1; _quad.vertices[0].ty = ty1;
		_quad.vertices[1].tx = tx2; _quad.vertices[1].ty = ty1;
		_quad.vertices[2].tx = tx2; _quad.vertices[2].ty = ty2;
		_quad.vertices[3].tx = tx1; _quad.vertices[3].ty = ty2;

		bX = _bXFlip; bY = _bYFlip; bHS = _bHSFlip;
		_bXFlip = false; _bYFlip = false;
		
		SetFlip(bX,bY,bHS);	
	}
	
	public void SetFrames(int n)
	{ 
		_frames = n; 
	}

	public int GetMode() 
	{
		return _mode; 
	}
	
	public float GetSpeed()
	{ 
		return 1.0f / _speed; 
	}
	
	public int GetFrame()
	{ 
		return _curFrame; 
	}
	
	public int GetFrames()
	{ 
		return _frames; 
	}
}
