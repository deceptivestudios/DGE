package com.deceptivestudios.engine.internal;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.deceptivestudios.engine.DGE;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.view.WindowManager;

public class DGERenderer implements GLSurfaceView.Renderer
{
	private GL10 _current;
	private boolean _firstRender = true;
	private int _fps = -1;
	private long _spf;
	private DGE _dge;
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		_current = gl;
		
		_dge = DGE.Interface(DGE.DGE_VERSION);

		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glShadeModel(GL10.GL_SMOOTH);
		
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepthf(1.0f);
		
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);	
	}

	public void onSurfaceChanged(GL10 gl, int w, int h)
	{
		_current = gl;

		_dge.System_SetState(DGE.DGE_SCREENWIDTH, w);
		_dge.System_SetState(DGE.DGE_SCREENHEIGHT, h);
		
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();

		GLU.gluPerspective(gl, 45.0f, w / h, 0.1f, 100.0f);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		_dge.Gfx_Restore();
	}
	
	public void onDrawFrame(GL10 gl)
	{
		_current = gl;
		
		DGEInterface target = (DGEInterface) _dge.System_GetState(DGE.DGE_INTERFACE);

		if (_firstRender)
		{
			_sleepState = (Boolean) _dge.System_GetState(DGE.DGE_SLEEPENABLED);
			
			if (target.Create())
				_firstRender = false;
		}
		else
		{
			SetSleepState((Boolean) _dge.System_GetState(DGE.DGE_SLEEPENABLED));
			FrameReady();
			
			target.Update();
			target.Reset();
			
			target.Render();
		}
	}
	
	private long _renderTimer;
	
	private void FrameReady()
	{
		int fps = (Integer) _dge.System_GetState(DGE.DGE_FPS);
		
		if (fps <= 0)
			return;
		
		if (fps != _fps)
		{
			_fps = fps;
			_spf = (long) (1000f / (float) _fps);
		}
		
		long timer = System.currentTimeMillis();
		long diff = timer - _renderTimer;
		
		if (diff < _spf)
		{
			try 
			{
				Thread.sleep(_spf - diff);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	
		_renderTimer = System.currentTimeMillis();
	}
	
	private boolean _sleepState;
	
	private void SetSleepState(boolean state)
	{
		if (_sleepState != state)
		{
			_sleepState = state;
			
			DGEInterface target = (DGEInterface) _dge.System_GetState(DGE.DGE_INTERFACE);
	
			target.runOnUiThread(new Runnable() 
			{
				public void run()
				{
					DGEInterface target = (DGEInterface) _dge.System_GetState(DGE.DGE_INTERFACE);
		    		boolean sleepState = (Boolean) _dge.System_GetState(DGE.DGE_SLEEPENABLED);
		    		
					if (!sleepState)
						target.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					else
						target.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
			});
		}
	}
	
	public GL10 GL()
	{
		return _current;
	}
}
