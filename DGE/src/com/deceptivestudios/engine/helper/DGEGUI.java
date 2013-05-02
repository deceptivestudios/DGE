package com.deceptivestudios.engine.helper;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.gui.DGEGUIObject;

public class DGEGUI 
{
	public DGEGUI()
	{
		_dge = DGE.Interface(DGE.DGE_VERSION);
		
		_ctrls = null;
		_ctrlLock = null;
		_ctrlFocus = null;
		_ctrlOver = null;
		
		_touchDown = _touchUp = false;
		_tapped = false;
		_mx = _my = 0f;
		
		_cursor = null;
	}

	public void AddControl(DGEGUIObject ctrl)
	{
		DGEGUIObject last = _ctrls;
		
		ctrl.gui = this;
		
		if (_ctrls == null)
		{
			_ctrls = ctrl;
			
			ctrl.prev = null;
			ctrl.next = null;
		}
		else
		{
			while (last.next != null)
				last = last.next;
			
			last.next = ctrl;
			ctrl.prev = last;
			
			ctrl.next = null;
		}
	}

	public void DeleteControl(int id)
	{
		DGEGUIObject ctrl = _ctrls;
		
		while (ctrl != null)
		{
			if (ctrl.id == id)
			{
				if (ctrl.prev != null)
					ctrl.prev.next = ctrl.next;
				else
					_ctrls = ctrl.next;
				
				if (ctrl.next != null)
					ctrl.next.prev = ctrl.prev;
				
				return;
			}
			
			ctrl = ctrl.next;
		}
	}

	public DGEGUIObject GetControl(int id)
	{
		DGEGUIObject ctrl = _ctrls;
		
		while (ctrl != null)
		{
			if (ctrl.id == id)
				return ctrl;
			
			ctrl = ctrl.next;
		}
		
		return null;
	}

	public void MoveControl(int id, float x, float y)
	{
		DGEGUIObject ctrl = GetControl(id);
		
		if (ctrl != null)
			ctrl.SetPosition(x, y);
	}

	public void ShowControl(int id, boolean visible)
	{
		DGEGUIObject ctrl = GetControl(id);
		
		if (ctrl != null)
			ctrl.visible = visible;
	}

	public void EnableControl(int id, boolean enabled)
	{
		DGEGUIObject ctrl = GetControl(id);
		
		if (ctrl != null)
			ctrl.enabled = enabled;
	}

	public void SetCursor(DGESprite cursor)
	{
		_cursor = cursor;
	}
	
	public final void SetColor(float r, float g, float b)
	{
		SetColor(r, g, b, 1);
	}
	
	public final void SetColor(float r, float g, float b, float a)
	{
		DGEGUIObject ctrl = _ctrls;
		
		while (ctrl != null)
		{
			ctrl.SetColor(r, g, b, a);
			ctrl = ctrl.next;
		}
	}
	
	public void SetFocus(int id)
	{
		DGEGUIObject newFocus = GetControl(id);
		
		if (newFocus == _ctrlFocus)
			return;
		
		if (newFocus == null)
		{
			if (_ctrlFocus != null)
				_ctrlFocus.Focus(false);
			_ctrlFocus = null;
		}
		else if (!newFocus.stationary && newFocus.visible && newFocus.enabled)
		{
			if (_ctrlFocus != null)
				_ctrlFocus.Focus(false);
			
			if (newFocus != null)
				newFocus.Focus(true);
			
			_ctrlFocus = newFocus;
		}
	}
	
	public int GetFocus() 
	{
		if (_ctrlFocus != null)
			return _ctrlFocus.id;
		
		return 0;
	}
	
	public void Enter()
	{
		DGEGUIObject ctrl = _ctrls;
		
		while (ctrl != null)
		{
			ctrl.Enter();
			ctrl = ctrl.next;
		}
	}
	
	public void Leave()
	{
		DGEGUIObject ctrl = _ctrls;
		
		while (ctrl != null)
		{
			ctrl.Leave();
			ctrl = ctrl.next;
		}
	}
		
	public void Reset()
	{
		DGEGUIObject ctrl = _ctrls;

		while (ctrl != null)
		{
			ctrl.Reset();
			ctrl = ctrl.next;
		}

		_ctrlFocus = null;
		_ctrlOver = null;
		_ctrlLock = null;
	}
	
	public void Move(float dx, float dy)
	{
		DGEGUIObject ctrl = _ctrls;
		
		while (ctrl != null)
		{
			ctrl.Move(dx, dy);
			ctrl = ctrl.next;
		}
	}
	
	public int Update(float dt)
	{
		DGEGUIObject ctrl;

		float[] touch = _dge.Input_GetTouch();
		
		if (touch != null)
		{
			_mx = touch[0];
			_my = touch[1];
		}
		
		_touchDown = _dge.Input_GetDown();
		_touchUp = _dge.Input_Released();
		
		// Update all controls
		ctrl = _ctrls;
		
		while (ctrl != null)
		{
			ctrl.Update(dt);
			ctrl = ctrl.next;
		}

		_tapped = _dge.Input_GetTap();
		
		if (_ctrlLock != null)
		{
			ctrl = _ctrlLock;
			
			if(!_touchUp) 
				_ctrlLock = null;
			
			if (ProcessControl(ctrl)) 
				return ctrl.id;
		}
		else
		{
			// Find last (topmost) control
			ctrl = _ctrls;
			
			if (ctrl != null)
			{
				while (ctrl.next != null) 
					ctrl = ctrl.next;
			}

			while (ctrl != null)
			{
				if (ctrl.TestPoint(_mx, _my) && ctrl.enabled)
				{
					if (_ctrlOver != ctrl)
					{
						if (_ctrlOver != null) 
							_ctrlOver.Touched(false);
						
						ctrl.Touched(true);
						_ctrlOver = ctrl;
					}

					if (ProcessControl(ctrl)) 
						return ctrl.id;
					else 
						return 0;
				}
				
				ctrl = ctrl.prev;
			}

			if (_tapped && _ctrlOver != null) 
			{
				_ctrlOver.Touched(false); 
			}
			
			_ctrlOver = null;
		}

		return 0;
	}
		
	public void Render()
	{
		DGEGUIObject ctrl = _ctrls;
		
		while (ctrl != null)
		{
			if (ctrl.visible)
				ctrl.Render();
			
			ctrl = ctrl.next;
		}
		
		if (_cursor != null)
			_cursor.Render(_mx, _my);
	}
	
	private boolean ProcessControl(DGEGUIObject ctrl)
	{
		boolean result = false;
		DGEVector position = ctrl.GetPosition();
		
		if (_touchUp) 
		{ 
			_ctrlLock = ctrl; 
			result = result || ctrl.Touched(true); 
		}
		
		if (_touchDown) 
		{ 
			SetFocus(ctrl.id);
			result = result || ctrl.Touched(false); 
		}
		
		result = result || ctrl.Touch(_mx - position.x, _my - position.y);

		return result;	
	}

	private static DGE _dge;

	private DGEGUIObject _ctrls;
	private DGEGUIObject _ctrlLock;
	private DGEGUIObject _ctrlFocus;
	private DGEGUIObject _ctrlOver;

	private DGESprite _cursor;

	private float _mx, _my;
	private boolean _touchDown;
	private boolean _touchUp;
	private boolean _tapped;
}
