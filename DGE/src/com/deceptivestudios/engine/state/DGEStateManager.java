package com.deceptivestudios.engine.state;

import java.util.HashMap;
import java.util.Stack;

public class DGEStateManager 
{
	private HashMap<String, DGEState> _states;
	private Stack<String> _state;
	
	private boolean _stateChanged = true;
	private String _data;
	
	public boolean Render()
	{
		if (_state.isEmpty())
			return false;
		
		boolean result = Render(_state.peek(), _stateChanged);
		_stateChanged = false;
		
		return result;
	}
	
	public boolean Render(String layer)
	{
		return Render(layer, false);
	}
	
	private boolean Render(String layer, boolean changed)
	{
		DGEState state = GetState(layer);
		
		if (state == null)
			return true;

		if (changed)
			state.Initialize(_data);
		
		return state.Render();
	}
	
	public boolean Update(float delta)
	{
		if (_stateChanged)
			return true;
		
		DGEState state = GetCurrentState();
		
		if (state != null)
			return state.Update(delta);
		
		return true;
	}
	
	public void Reset()
	{
		_state = new Stack<String>();
		_states = new HashMap<String, DGEState>();
	}
	
	public boolean RegisterState(String name, DGEState state)
	{
		if (_states.containsKey(name))
			return false;
		
		_states.put(name, state);
		
		return true;
	}
	
	public DGEState GetState(String name)
	{
		if (name == null || name == "" || _states.isEmpty())
			return null;
		
		return _states.get(name);
	}
	
	public boolean SetState(String name)
	{
		return SetState(name, null);
	}
	
	public boolean SetState(String name, String init)
	{
		if (!_states.containsKey(name))
			return false;
		
		_stateChanged = true;
		_data = init;
		
		Stack<String> newState = new Stack<String>();
		newState.push(name);
		
		_state = newState;

		return true;
	}
	
	public boolean PopState()
	{
		if (_state.isEmpty())
			return false;
		
		_state.pop();
		
		return !_state.isEmpty();
	}
	
	public boolean PopState(String init)
	{
		if (_state.isEmpty())
			return false;
		
		_state.pop();
		
		_stateChanged = true;
		_data = init;
		
		return !_state.isEmpty();
	}
	
	public boolean PushState(String name)
	{
		return PushState(name, null);
	}
	
	public boolean PushState(String name, String init)
	{
		if (!_states.containsKey(name))
			return false;

		_stateChanged = true;
		_data = init;
		
		_state.push(name);
		
		return true;
	}
	
	public DGEState GetCurrentState()
	{
		if (_state.isEmpty())
			return null;
		
		return GetState(_state.peek());
	}
	
	private static DGEStateManager _instance;
	
	public static DGEStateManager GetManager()
	{
		if (_instance == null)		
			_instance = new DGEStateManager();

		return _instance;
	}
	
	private DGEStateManager()
	{
		Reset();
	}
}
