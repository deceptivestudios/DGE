package com.deceptivestudios.tests;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEFont;
import com.deceptivestudios.engine.state.DGEState;
import com.deceptivestudios.engine.state.DGEStateManager;

public class TestState extends DGEState
{
	private Test _current = null;
	
    @SuppressWarnings("rawtypes")
	private boolean StartTutorial(int tutorial)
    {
    	String className = String.format("com.deceptivestudios.tests.Test_%d", tutorial);
    	
    	try
    	{
    		Class theClass = Class.forName(className);
    		_current = (Test) theClass.newInstance();
    		
			if (!_current.Ready())
				_current.Create();
			
			return _current.Ready();
    	}
    	catch (Exception e)
    	{
    		return false;
    	}
    }
    
	@Override
	public boolean Initialize(String data) 
	{
		_dge.System_SetState(DGE.DGE_SLEEPENABLED, false);
		
		int tutorial = Integer.parseInt(data);
		
		if (tutorial < 1 || tutorial > Test.TotalTests)
			return false;
		
		return StartTutorial(tutorial);
	}

	@Override
	public boolean Render() 
	{
		_dge.Gfx_BeginScene();
		_dge.Gfx_Clear(0, 0, 0, 1);
		
		if (_current != null)
			_current.Render();
		
		Test.Font.SetColor(1, 1, 1, 1);
		Test.Font.Render(5, 5, DGEFont.Left, "Delta: %.3f\nFPS: %d\nDraw Calls: %d", _dge.Timer_GetDelta(), (int) Math.ceil(Test.FPS), _dge.Gfx_GetDrawCalls());
		
		_dge.Gfx_EndScene();
		
		return true;
	}

	@Override
	public boolean Update(float delta) 
	{
		boolean back = _dge.Input_Back();
		
		if (_current == null || !_current.Update(delta))
			back = true;
		
		if (back)
		{
			_current = null;
			DGEStateManager.GetManager().PopState("");
		}
		
		return true;
	}

}
