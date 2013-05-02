package com.deceptivestudios.engine.gui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.helper.DGEFont;
import com.deceptivestudios.engine.helper.DGERect;
import com.deceptivestudios.engine.helper.DGESprite;

public class DGEGUIListbox extends DGEGUIObject
{
	private DGEFont _font;
	private DGESprite _highlight;
	private DGEColor _textColor, _textHighlight;
	private int _nextId, _selectedItem, _topItem;
	private float _mx, _my;
	private HashMap<Integer, String> _items;
	
	public DGEGUIListbox(int id, float x, float y, float w, float h, DGEFont font, DGEColor tColor, DGEColor thColor, DGEColor hColor)
	{
		this.id = id;
		this.stationary = false;
		this.visible = true;
		this.enabled = true;
		
		this._x = x; this._y = y;
		this._rect = new DGERect(x, y, x + w, y + h);
		
		_font = font;
		
		_highlight = new DGESprite(0, 0, 0, w, _font.GetHeight());
		_highlight.SetColor(hColor);
		
		_textColor = new DGEColor(tColor);
		_textHighlight = new DGEColor(thColor);
		
		_items = new HashMap<Integer, String>();
		
		_topItem = _selectedItem = 0;
		_mx = _my = 0;
		
		_nextId = 0;
	}
	
	public int AddItem(String value)
	{
		_nextId++;
		_items.put(_nextId, value);
		
		return _nextId;
	}
	
	public void DeleteItem(int value)
	{
		_items.remove(value);
	}
	
	public void Clear()
	{
		_nextId = 0;
		_items.clear();
	}
	
	public String GetItemText(int value)
	{
		return _items.get(value);
	}
	
	public int GetSelectedItem()
	{
		return _selectedItem;
	}
	
	public void SetSelectedItem(int value)
	{
		if (_items.containsKey(value))
			_selectedItem = value;
	}
	
	public int GetTopItem()
	{
		return _topItem;
	}
	
	public void SetTopItem(int value)
	{
		_topItem = value;
	}
	
	public int GetNumberItems()
	{
		return _items.size();
	}
	
	public int GetNumberRows()
	{
		return (int) ((_rect.p2.y - _rect.p1.y) / _font.GetHeight());
	}
	
	@Override
	public void Render()
	{
		Iterator<Entry<Integer, String>> itr = _items.entrySet().iterator();
		Map.Entry<Integer, String> pair = null;

		while (itr.hasNext())
		{
			pair = itr.next();

			if (pair.getKey() == _topItem)
				break;
		}
		
		int rows = GetNumberRows();
		
		for (int i = 0; pair != null && i < rows; i++)
		{
			if (pair.getKey() == _selectedItem)
			{
				_highlight.Render(_rect.p1.x, _rect.p1.y + i * _font.GetHeight());
				_font.SetColor(_textHighlight);
			}
			else
			{
				_font.SetColor(_textColor);
			}
			
			_font.Render(_rect.p1.x + 3, _rect.p1.y + i * _font.GetHeight(), DGEFont.Left, pair.getValue());

			if (!itr.hasNext())
				return;
			
			pair = itr.next();
		}
	}

	@Override
	public boolean Touch(float x, float y) 
	{
		_mx = x;
		_my = y;

		return false;
	}

	@Override
	public boolean Touched(boolean down)
	{
		if (down)
		{
			int item = ((int) _my / (int) _font.GetHeight());
			boolean foundTop = false;
			
			Iterator<Entry<Integer, String>> itr = _items.entrySet().iterator();
			
			while (itr.hasNext())
			{
				Map.Entry<Integer, String> pair = itr.next();

				if (foundTop)
				{
					if (item == 0)
					{
						_selectedItem = pair.getKey();
						return true;
					}
					
					item--;
				}
				else
				{
					if (pair.getKey() == _topItem)
						foundTop = true;
				}
				
			}
		}
		
		return false;
	}

	@Override
	public void Enter() { }
	@Override
	public void Focus(boolean focused) { }
	@Override
	public boolean IsDone() { return true; }
	@Override
	public void Leave() { }
	@Override
	public void Reset() { }
	@Override
	public void Update(float dt) { }
}
