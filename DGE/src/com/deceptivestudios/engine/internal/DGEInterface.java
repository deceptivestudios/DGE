package com.deceptivestudios.engine.internal;

import com.deceptivestudios.engine.DGE;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;

public abstract class DGEInterface extends Activity
{
	public abstract boolean Create();
	public abstract boolean Render();
	public abstract boolean Update();
	
	protected DGE _dge;
	private boolean _back;
	private boolean _backChecked;
	
	private DGEView _view;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _dge = DGE.Interface(DGE.DGE_VERSION);
        
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }
    
    @Override
    public void setContentView(View view)
    {
    	_view = (DGEView) view;
    	super.setContentView(view);
    }
    
    public boolean Input_Back()
    {
    	_backChecked = true;
    	return _back;
    }
    
    public void Reset()
    {
    	if (_backChecked)
    		_back = false;
    	
    	if (_view != null)
    		_view.Reset();
    }
    
    @Override
    public void onDestroy()
    {
    	_dge.System_Shutdown();
    	_dge = null;
    	
    	super.onStop();
    }
    
    @Override
    public void onBackPressed()
    {
    	_back = true;
    	_backChecked = false;
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	_dge.System_Resume();
    }
    
    @Override
    protected void onPause()
    {
    	super.onPause();
    	_dge.System_Suspend();
    }
}
