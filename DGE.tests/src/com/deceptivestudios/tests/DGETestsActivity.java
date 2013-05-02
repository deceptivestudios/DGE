package com.deceptivestudios.tests;

import android.os.Bundle;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEFont;
import com.deceptivestudios.engine.internal.DGEInterface;
import com.deceptivestudios.engine.state.DGEStateManager;

public class DGETestsActivity extends DGEInterface
{
	private static final String LOG_TAG = DGETestsActivity.class.getSimpleName();
	private DGE _dge;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	
        _dge = DGE.Interface(DGE.DGE_VERSION);
       
        _dge.System_SetState(DGE.DGE_LOG, LOG_TAG);
        _dge.System_SetState(DGE.DGE_INTERFACE, this);
        
        if (!_dge.System_Initiate())
        	finish();
    }

	@Override
	public boolean Create() 
	{
		Test.Font = new DGEFont("font1.fnt");
		
		if (!Test.Font.Validate())
			return false;
		
		Test.Width = (Integer) _dge.System_GetState(DGE.DGE_SCREENWIDTH);
		Test.Height = (Integer) _dge.System_GetState(DGE.DGE_SCREENHEIGHT);

		DGEStateManager.GetManager().RegisterState("Startup", new StartupState());
		DGEStateManager.GetManager().RegisterState("Menu", new MenuState());
		DGEStateManager.GetManager().RegisterState("Test", new TestState());
		
		DGEStateManager.GetManager().SetState("Startup");
		
    	_dge.System_Start();
    	
		return true;
	}
    
	@Override
	public boolean Render() 
	{
		return DGEStateManager.GetManager().Render();
	}

	private float _update = 0f;
	
	@Override
	public boolean Update() 
	{
		float dt = _dge.Timer_GetDelta();
		
		_update -= dt;
		
		if (_update <= 0)
		{
			_update = 0.2f;
			Test.FPS = (float) _dge.Timer_GetFPS();
		}

		if (!DGEStateManager.GetManager().Update(dt))
		{
			DGEStateManager.GetManager().Reset();
			finish();
			
			return false;
		}
		
		return true;
	}
}