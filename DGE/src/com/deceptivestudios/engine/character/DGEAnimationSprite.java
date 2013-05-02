package com.deceptivestudios.engine.character;

import java.util.Vector;

import com.deceptivestudios.engine.entity.DGEResourceReader;
import com.deceptivestudios.engine.helper.DGERect;
import com.deceptivestudios.engine.helper.DGEResourceManager;
import com.deceptivestudios.engine.helper.DGESprite;
import com.deceptivestudios.engine.helper.DGEVector;

public class DGEAnimationSprite 
{
	private class Frame
	{
		public String Filename;
		public String Filepath;
		public DGESprite Sprite;
	}
	
	protected Vector<Frame> _frames;
	protected int _currentFrame;
	
	public String Name;
	public DGEVector[] Vertices;
	public boolean Selected;

	public DGEAnimationSprite(DGEResourceReader file)
	{
		this(file, "");
	}

	public DGEAnimationSprite(DGEResourceReader file, String baseCharacterDirectory)
	{
		this(file, baseCharacterDirectory, null);
	}
	
	public DGEAnimationSprite(DGEResourceReader file, String baseCharacterDirectory, DGEResourceManager resourceManager)
	{
		
	}
	
	public DGERect Rect()
	{
		return null;
	}
	
	public DGEVector GetCenter()
	{
		return null;
	}
	
	public DGESprite GetSprite()
	{
		return null;
	}
}
