package com.deceptivestudios.tests;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.helper.DGEFont;
import com.deceptivestudios.engine.helper.DGERect;
import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.state.DGEState;
import com.deceptivestudios.engine.state.DGEStateManager;

public class MenuState extends DGEState
{
	@Override
	public boolean Initialize(String data) 
	{
		_dge.System_SetState(DGE.DGE_SLEEPENABLED, true);
		
		return true;
	}

	@Override
	public boolean Render() 
	{
		_dge.Gfx_BeginScene();
		_dge.Gfx_Clear(0, 0, 0, 1);
		
		int offset = 200;
		int startx = 100; 
		int starty = 100;
		
		// render numbers on each
		for (int i = 0; i < Test.TotalTests; i++)
		{
			Test.Font.Render(startx, starty, DGEFont.Left, "Test %d", i + 1);
			
			startx += offset;
			
			if ((startx + offset) > Test.Width)
			{
				startx = 100;
				starty += 100;
			}
		}
		
		Test.Font.SetColor(1, 1, 1, 1);
		Test.Font.Render(5, 5, DGEFont.Left, "Delta: %.3f\nFPS: %d\nDraw Calls: %d", _dge.Timer_GetDelta(), (int) Math.ceil(Test.FPS), _dge.Gfx_GetDrawCalls());
		
		_dge.Gfx_EndScene();
		
		return true;
	}

	@Override
	public boolean Update(float delta) 
	{
		if (_dge.Input_Back())
			return false;
		
		// tap screen on which tutorial?
		if (_dge.Input_GetTap())
		{
			float[] position = _dge.Input_GetPosition();
			DGEVector point = new DGEVector(position[0], position[1]);

			int offset = 200;
			int startx = 100; 
			int starty = 100;
			
			// render numbers on each
			for (int i = 0; i < Test.TotalTests; i++)
			{
				DGERect rect = new DGERect(startx, starty, startx + 120, starty + 40);
				
				if (rect.TestPoint(point))
				{
					DGEStateManager.GetManager().PushState("Test", String.format("%d", i + 1));
					break;
				}
				
				startx += offset;
				
				if ((startx + offset) > Test.Width)
				{
					startx = 100;
					starty += 100;
				}
			}
		}
		
		return true;
	}

}
