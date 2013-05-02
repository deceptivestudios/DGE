package com.deceptivestudios.tests;

import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.helper.DGEFont;
import com.deceptivestudios.engine.helper.DGESprite;
import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.polygon.DGEPolygonMap;
import com.deceptivestudios.tests.entity.EntityBackground;
import com.deceptivestudios.tests.entity.EntityPlayer;
import com.deceptivestudios.tests.entity.EntityTurret;

public class Test_11 extends Test
{
	private DGEPolygonMap _map;
	private DGESprite _background;
	private DGEVector _bgoffset;
	private DGEFont _font;
	private EntityPlayer _player;
	private int _warningTexture;
	private DGESprite _warningSprite;
	private DGEVector _warningPosition;
	
	@Override
	public boolean Create() 
	{
		_map = new DGEPolygonMap(new DGEVector(Test.Width, Test.Height), true);
		
		_map.RegisterEntityClass("Player", new EntityPlayer());
		_map.RegisterEntityClass("Turret", new EntityTurret());
		_map.RegisterEntityClass("BackgroundPolygon", new EntityBackground());
		_map.RegisterEntityClass("Bullet", null);
		
		_map.LoadFile("maps/level2.map");
		
		_background = new DGESprite(_dge.Texture_Load("data/background.jpg"), 0, 0, 800, 600);
		
		_font = new DGEFont("data/tahoma13b.fnt");
		_font.SetScale(2.0f);
		_font.SetColor(DGEColor.ParseARGB("FFEEEEEE"));
		
		_bgoffset = new DGEVector();
		
		_warningTexture = _dge.Texture_Load("data/textures/Other/warning.png");
		_warningSprite = new DGESprite(_warningTexture, 0, 0, 64, 64);
		_warningSprite.SetHotSpot(32, 32);
		_warningPosition = new DGEVector(Test.Width / 2, Test.Height / 2);
		
		_ready = true;
		
		return true;
	}

	@Override
	public boolean Render() 
	{
		_background.RenderStretch(0, 0, Test.Width, Test.Height);
		_background.SetTextureRect(_bgoffset.x, _bgoffset.y, 790, 590, false);
		
		_map.Render();

		if (_player != null)
		{
			float velocity = _player.GetVelocity();
			float zoom = _player.GetZoom();
			DGEVector position = new DGEVector(_warningPosition.x + 52 * zoom, _warningPosition.y - 32 * zoom);
			
			if (velocity > 150f)
				_warningSprite.RenderEx(position.x, position.y, 0, zoom, zoom);
		}
		
		return true;
	}

	@Override
	public boolean Update(float delta) 
	{
		_map.Update(delta);
		
		_bgoffset = _map.GetTransform();
		_player = (EntityPlayer) _map.GetDynamicEntityFromProperty("name", "player");

		return true;
	}

}
