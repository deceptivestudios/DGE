package com.deceptivestudios.engine.internal;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class DGEView extends GLSurfaceView
{
	private float[] _gravity = new float[3];
	private float[] _geoMagnets = new float[3];
    private float[] _rotationMatrix = new float[16];
    private float[] _inclinationMatrix = new float[16];
	
	private SensorManager _sensorManager;
	private SensorEventListener _sensorListener = new SensorEventListener()
	{
		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onSensorChanged(SensorEvent event) 
		{
			synchronized (this) 
			{
				switch (event.sensor.getType())
				{
					case Sensor.TYPE_ACCELEROMETER:
					{
						System.arraycopy(event.values, 0, _gravity, 0, 3);
					} break;
					
					case Sensor.TYPE_MAGNETIC_FIELD:
					{
						System.arraycopy(event.values, 0, _geoMagnets, 0, 3);
					} break;
				}
				
				if (SensorManager.getRotationMatrix(_rotationMatrix, _inclinationMatrix, _gravity, _geoMagnets))
				{
					SensorManager.getOrientation(_rotationMatrix, _tilt);
				}
			}
		}
	};

	public DGEView(Context context)
	{
		super(context);
		
		_sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		_touches = new HashMap<Integer, TouchEventData>();
	}
	
	private float[] _tilt = new float[3];
	
	public float[] GetTilt()
	{
		return _tilt;
	}
	
	// sort the toucheventdata by the newest update first
	private class TouchEventDataComparator implements Comparator<TouchEventData>
	{
		@Override
		public int compare(TouchEventData arg0, TouchEventData arg1) 
		{
			if (arg0 == null || arg1 == null)
				return 0;
			
			if (arg0.LastUpdate > arg1.LastUpdate)
				return -1;
			else if (arg0.LastUpdate < arg1.LastUpdate)
				return 1;
			else
				return 0;
		}
	}
	
	public float[] GetTouch()
	{
		if (_touches.size() > 0)
		{
			Vector<Float> touches = new Vector<Float>();
			TouchEventData[] data = _touches.values().toArray(new TouchEventData[_touches.size()]);
			
			Arrays.sort(data, new TouchEventDataComparator());
			
			for (int i = 0; i < data.length; i++)
			{
				if (data[i].CurrentDown != null)
				{
					touches.add(data[i].LastX);
					touches.add(data[i].LastY);
				}
			}
			
			if (touches.size() > 0)
			{
				float[] returnData = new float[touches.size()];
				
				for (int i = 0; i < touches.size(); i++)
				{
					returnData[i] = touches.get(i);
				}
				
				return returnData;
			}
		}

		return new float[] { _lastX, _lastY };
	}
	
	public boolean GetTap()
	{
		boolean singleTap = false;
		
		if (_touches.size() > 0)
		{
			TouchEventData[] data = _touches.values().toArray(new TouchEventData[_touches.size()]);
			
			for (int i = 0; i < data.length && !singleTap; i++)
			{
				if (data[i].SingleTap)
					singleTap = true;
			}
		}
		
		return singleTap;
	}
	
	public boolean GetDoubleTap()
	{
		boolean doubleTap = false;
		
		if (_touches.size() > 0)
		{
			TouchEventData[] data = _touches.values().toArray(new TouchEventData[_touches.size()]);
			
			for (int i = 0; i < data.length && !doubleTap; i++)
			{
				if (data[i].DoubleTap)
					doubleTap = true;
			}
		}
		
		return doubleTap;
	}
	
	public boolean GetDown()
	{
		boolean down = false;
		
		if (_touches.size() > 0)
		{
			TouchEventData[] data = _touches.values().toArray(new TouchEventData[_touches.size()]);
			
			for (int i = 0; i < data.length && !down; i++)
			{
				if (data[i].CurrentDown != null)
					down = true;
			}
		}
		
		return down;
	}
	
	public float[] GetFling()
	{
		if (_touches.size() > 0)
		{
			float[] fling = new float[_touches.size() * 2];
			TouchEventData[] data = _touches.values().toArray(new TouchEventData[_touches.size()]);
			
			for (int i = 0; i < data.length; i++)
			{
				fling[i * 2] =     data[i].ShiftX;
				fling[i * 2 + 1] = data[i].ShiftY;
			}
			
			return fling;
		}
		
		return new float[] { 0, 0 };
	}
	
	public float[] GetAcceleration()
	{
		// get the acceleration minus the effect of gravity
		return new float[] { _gravity[2] - 9.8f, _gravity[1], _gravity[0] };
	}
	
	private class TouchEventData
	{
		public float LastX, LastY;
		public float ShiftX, ShiftY;
		public MotionEvent PreviousUp, CurrentDown;
		public Timer Timer;
		public boolean IgnoreUp;
		public boolean DoubleTap, SingleTap;
		public long LastUpdate;
	}
	
	private float _lastX = 0, _lastY = 0;
	private HashMap<Integer, TouchEventData> _touches;
	
	@Override
	public boolean onTouchEvent(final MotionEvent event)
	{
		final long tapTime = ViewConfiguration.getTapTimeout();
		final int touchSlop = ViewConfiguration.getTouchSlop();
		final int doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout();
		final int action = event.getAction();
		
		float x, y;
		
		boolean handled = false;
		
		switch (action & MotionEvent.ACTION_MASK)
		{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
			{
				for (int i = 0; i < event.getPointerCount(); i++)
				{
					int pointer = event.getPointerId(i);
					TouchEventData data = null;
					
					try
					{
						x = event.getX(pointer);
						y = event.getY(pointer);
						
						if (_touches.containsKey(pointer))
						{
							data = _touches.get(pointer);
							
							if (data.PreviousUp != null)
							{
								if ((event.getEventTime() - data.PreviousUp.getEventTime()) < doubleTapTimeout)
								{
									data.Timer.cancel();
										
									data.DoubleTap = true;
									data.IgnoreUp = true;
								}
								
								data.PreviousUp.recycle();
								data.PreviousUp = null;
							}
						}
						else
						{
							data = new TouchEventData();
						}
	
						data.CurrentDown = MotionEvent.obtain(event);
						
						_lastX = data.LastX = x; 
						_lastY = data.LastY = y;
						
						data.LastUpdate = System.currentTimeMillis();
	
						_touches.put(pointer, data);
					}
					catch (IllegalArgumentException e)
					{
						if (_touches.containsKey(pointer))
							_touches.remove(pointer);
					}
				}
				
				handled = true;
			} break;
			
			case MotionEvent.ACTION_MOVE:
			{
				for (int i = 0; i < event.getPointerCount(); i++)
				{
					int pointer = event.getPointerId(i);
					
					if (_touches.containsKey(pointer))
					{
						TouchEventData data = _touches.get(pointer);
						
						try
						{
							x = event.getX(pointer);
							y = event.getY(pointer);
							
							final int deltaX = (int) (x - data.CurrentDown.getX(pointer));
							final int deltaY = (int) (y - data.CurrentDown.getY(pointer));
							int distance = (deltaX * deltaX) + (deltaY * deltaY);
						
							if (distance > touchSlop)
							{
								data.ShiftX += (data.LastX - x);
								data.ShiftY += (data.LastY - y);
								
								_lastX = data.LastX = x; 
								_lastY = data.LastY = y;
							}
		
							data.LastUpdate = System.currentTimeMillis();
	
							_touches.put(pointer, data);
						}
						catch (IllegalArgumentException e)
						{
							_touches.remove(pointer);
						}
					}
				}
				
				handled = true;
			} break;
			
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
			{
				final int pointerIndex  = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				int pointer = event.getPointerId(pointerIndex);
				
				if (_touches.containsKey(pointer))
				{
					TouchEventData data = _touches.get(pointer);
					
					if (!data.IgnoreUp)
					{
						if ((event.getEventTime() - data.CurrentDown.getEventTime()) < tapTime)
						{
							data.Timer = new Timer();
							data.Timer.schedule(new TapTimerTask(data), doubleTapTimeout);
						}
						else
						{
							data.IgnoreUp = true;
						}
					}
					
					if (!data.IgnoreUp)
						data.PreviousUp = MotionEvent.obtain(event);
					
					data.CurrentDown.recycle();
					data.CurrentDown = null;
					
					data.IgnoreUp = false;
					
					data.LastUpdate = System.currentTimeMillis();

					_touches.put(pointer, data);
				}
				
				handled = true;
			} break;
			
			case MotionEvent.ACTION_CANCEL:
			{
				final int pointerIndex  = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				int pointer = event.getPointerId(pointerIndex);
				
				if (_touches.containsKey(pointer))
				{
					TouchEventData data = _touches.get(pointer);
					
					if (data.CurrentDown != null)
					{
						data.CurrentDown.recycle();
						data.CurrentDown = null;
					}
					
					_touches.remove(pointer);
				}
				
				handled = true;
			} break;
		}
		
		return handled;
	}
	
	private class TapTimerTask extends TimerTask
	{
		private TouchEventData _data;
		
		public TapTimerTask(TouchEventData data)
		{
			_data = data;
		}

		@Override
		public void run() 
		{
			if (_data.CurrentDown == null)
				_data.SingleTap = true;
		}
	}
	
	public void Reset()
	{
		if (_touches.size() > 0)
		{
			TouchEventData[] data = _touches.values().toArray(new TouchEventData[_touches.size()]);
			
			for (int i = 0; i < data.length; i++)
			{
				if (data[i] != null)
				{
					data[i].SingleTap = false;
					data[i].DoubleTap = false;
				}
			}
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		_sensorManager.registerListener(_sensorListener, _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
		_sensorManager.registerListener(_sensorListener, _sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
		_sensorManager.registerListener(_sensorListener, _sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);		
	}
	
	@Override
	public void onPause()
	{
		_sensorManager.unregisterListener(_sensorListener);
		super.onPause();
	}
}
