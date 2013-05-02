package com.deceptivestudios.engine.polygon;

import java.util.Vector;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.entity.DGEEntity;
import com.deceptivestudios.engine.entity.DGEEntityEx;
import com.deceptivestudios.engine.geometry.DGECircle;
import com.deceptivestudios.engine.geometry.DGEIntersect;
import com.deceptivestudios.engine.geometry.DGELine;
import com.deceptivestudios.engine.geometry.DGEPolygon;
import com.deceptivestudios.engine.geometry.DGERectangle;
import com.deceptivestudios.engine.geometry.DGEShape;
import com.deceptivestudios.engine.geometry.DGEShape.ShapeType;
import com.deceptivestudios.engine.helper.DGERect;
import com.deceptivestudios.engine.helper.DGEVector;

public class DGEPolygonGrid 
{
	private DGE _dge;
	
	private DGERect _area;
	private DGEVector _cellsize;
	
	private int _gsX, _gsY, _gsCount;
	private int _prevX1, _prevY1, _prevX2, _prevY2;
	
	private Vector<DGEPolygonCell> _cells;
	private Vector<DGEPolygonCellData> _polys;
	private Vector<DGEPolygonCellData> _visible;
	private Vector<DGEPolygonCellData> _regionVisible;
	
	private DGERect _viewport;

	public DGEPolygonGrid(DGERect area, DGERect viewport)
	{
		this(area, viewport, new DGEVector(1024, 768));
	}
	
	public DGEPolygonGrid(DGERect area, DGERect viewport, DGEVector cellsize)
	{
		_dge = DGE.Interface(DGE.DGE_VERSION);
		
		_cells = new Vector<DGEPolygonCell>();
		_polys = new Vector<DGEPolygonCellData>();
		_visible = new Vector<DGEPolygonCellData>();
		_regionVisible = new Vector<DGEPolygonCellData>();
		
		_viewport = viewport;
		
		_area = area;
		_cellsize = cellsize;
		
		if ((_area.p2.x - _area.p1.x) < _cellsize.x)
			_area.p2.x = _area.p1.x + _cellsize.x;
		
		if ((_area.p2.y - _area.p1.y) < _cellsize.y)
			_area.p2.y = _area.p1.y + _cellsize.y;
		
		_gsX = (int) ((_area.p2.x - _area.p1.x) / _cellsize.x) + (((int) (_area.p2.x - _area.p1.x) % (int) _cellsize.x == 0) ? 0 : 1);
		_gsY = (int) ((_area.p2.y - _area.p1.y) / _cellsize.y) + (((int) (_area.p2.y - _area.p1.y) % (int) _cellsize.y == 0) ? 0 : 1);
		_gsCount = _gsX * _gsY;
		
		_prevX1 = _prevY1 = _prevX2 = _prevY2 = -16000;
		
		_cells.setSize(_gsCount);
		
		for (int y = 0; y < _gsY; y++)
		{
			for (int x = 0; x < _gsX; x++)
			{
				DGEPolygonCell cell = new DGEPolygonCell();
				
				cell.Rect.Set((x * _cellsize.x) + _area.p1.x, (y * _cellsize.y) + _area.p1.y,
							  ((x + 1) * _cellsize.x) + _area.p1.x, ((y + 1) * _cellsize.y) + _area.p1.y);
				
				_cells.set((y * _gsX) + x, cell);
			}
		}
	}
	
	public void Prepare()
	{
		Prepare(null);
	}
	
	public void Prepare(DGERect viewport)
	{
		DGERect activeViewport = viewport != null ? viewport : _viewport;
		
		int a1x = (int) ((activeViewport.p1.x - _area.p1.x) / _cellsize.x);
		int a1y = (int) ((activeViewport.p1.y - _area.p1.y) / _cellsize.y);
		int a4x = (int) ((activeViewport.p2.x - _area.p1.x) / _cellsize.x);
		int a4y = (int) ((activeViewport.p2.y - _area.p1.y) / _cellsize.y);
		
		if (viewport == null)
		{
			if ((_prevX1 == a1x && _prevX2 == a4x) && (_prevY1 == a1y && _prevY2 == a4y))
				return;
			
			_prevX1 = a1x; _prevX2 = a4x;
			_prevY1 = a1y; _prevY2 = a4y;
		}
		
		Vector<DGEPolygonCellData> activeList = new Vector<DGEPolygonCellData>();
		
		if (a1x < 0) a1x = 0; if (a1y < 0) a1y = 0;
		if (a4x < 0) a4x = 0; if (a4y < 0) a4y = 0;
		if (a1x >= _gsX) a1x = _gsX; if (a1y >= _gsY) a1y = _gsY - 1;
		if (a4x >= _gsX) a4x = _gsX; if (a4y >= _gsY) a4y = _gsY - 1;
		
		for (int y = a1y; y <= a4y; y++)
		{
			for (int x = a1x; x <= a4x; x++)
			{
				DGEPolygonCell cell = _cells.get(x + y * _gsX);
				
				for (int i = 0; i < cell.Objects.size(); i++)
					cell.Objects.get(i).Flag = true;
			}
		}
		
		for (int y = a1y; y <= a4y; y++)
		{
			for (int x = a1x; x <= a4x; x++)
			{
				DGEPolygonCell cell = _cells.get(x + y * _gsX);
				
				for (int i = 0; i < cell.Objects.size(); i++)
				{
					if (cell.Objects.get(i).Flag)
					{
						cell.Objects.get(i).Flag = false;
						activeList.add(i, cell.Objects.get(i));
					}
				}
			}
		}
		
		DGEPolygonCellData temp;
		boolean sorted = false;
		
		for (int s = 0; s < activeList.size(); s++)
		{
			sorted = true;
			
			for (int i = 0; i < activeList.size() - 1; i++)
			{
				if (activeList.get(i).Sublayer > activeList.get(i + 1).Sublayer)
				{
					temp = activeList.get(i + 1);
					activeList.set(i + 1, activeList.get(i));
					activeList.set(i, temp);
					
					sorted = false;
				}
			}
			
			if (sorted)
				break;
		}
		
		if (viewport != null)
			_regionVisible = activeList;
		else
			_visible = activeList;
	}
	
	public void Render()
	{
		Render(-1);
	}
	
	public void Render(int layer)
	{
		Render(layer, 0);
	}
	
	public void Render(int layer, int flags)
	{
		Render(layer, flags, false);
	}
	
	private void Render(int layer, int flags, boolean debug)
	{
		for (int i = 0; i < _visible.size(); i++)
		{
			if (layer == -1 || _visible.get(i).Layer == layer)
			{
				if (_visible.get(i).Polygon.GetAABB().Intersect(_viewport))
					_visible.get(i).Polygon.Render(flags);
			}
		}
		
		if (debug)
		{
			for (float y = _area.p1.y; y < _area.p2.y; y += _cellsize.y)
			{
				for (float x = _area.p1.x; x < _area.p2.x; x += _cellsize.x)
				{
					float g = 0, b = 0;
					DGERect r = new DGERect(x, y, x + _cellsize.x, y + _cellsize.y);
					
					if (!r.Intersect(_viewport))
					{
						g = 1; b = 1;
					}
					
					_dge.Gfx_RenderLine(x, y, x + _cellsize.x, y, 1, g, b, 1);
					_dge.Gfx_RenderLine(x, y, x, y + _cellsize.y, 1, g, b, 1);
				}
			}
		}
	}
	
	public void Update(float delta)
	{
		for (int i = 0; i < _visible.size(); i++)
		{
			if (_visible.get(i).Polygon.IsEntity())
				((DGEEntity) _visible.get(i).Polygon).Update(delta);
		}
	}

    public DGEPolygon TestPoint(DGEVector point)
    {
    	return TestPoint(point, "");
    }
    
    public DGEPolygon TestPoint(DGEVector point, String classFilter)
    {
    	return TestPoint(point, classFilter, -1);
    }
    
    public DGEPolygon TestPoint(DGEVector point, String classFilter, int layer)
    {
    	return TestPoint(point, classFilter, layer, false);
    }
    
    public DGEPolygon TestPoint(DGEVector point, String classFilter, int layer, boolean customRegion)
    {
    	Vector<DGEPolygonCellData> activeList = customRegion ? _regionVisible : _visible;
    	
    	for (int i = (int) activeList.size() - 1; i >= 0; i--)
    	{
    		if ((layer == -1 || activeList.get(i).Layer == layer) &&
    			(classFilter.length() == 0 || activeList.get(i).Polygon.EntityData.Classname.equalsIgnoreCase(classFilter)))
    		{
    			if (activeList.get(i).Polygon.TestPoint(point))
    				return activeList.get(i).Polygon;
    		}
    	}
    	
    	return null;
    }

    // Test 
    public DGEIntersect Test(DGEShape shape)
    {
    	return Test(shape, new DGEVector());
    }
    
    public DGEIntersect Test(DGEShape shape, DGEVector offset)
    {
    	return Test(shape, offset, null);
    }
    
    public DGEIntersect Test(DGEShape shape, DGEVector offset, Vector<DGEPolygon> returnPolygon)
    {
    	return Test(shape, offset, returnPolygon, DGEPolygonCollision.Static);
    }
    
    public DGEIntersect Test(DGEShape shape, DGEVector offset, Vector<DGEPolygon> returnPolygon, int testType)
    {
    	return Test(shape, offset, returnPolygon, testType, "");
    }
    
    public DGEIntersect Test(DGEShape shape, DGEVector offset, Vector<DGEPolygon> returnPolygon, int testType, String classFilter)
    {
    	return Test(shape, offset, returnPolygon, testType, classFilter, -1);
    }
    
    public DGEIntersect Test(DGEShape shape, DGEVector offset, Vector<DGEPolygon> returnPolygon, int testType, String classFilter,  int layer)
    {
    	return Test(shape, offset, returnPolygon, testType, classFilter, layer, false);
    }
    
    public DGEIntersect Test(DGEShape shape, DGEVector offset, Vector<DGEPolygon> returnPolygon, int testType, String classFilter,  int layer, boolean customRegion)
    {
    	DGEIntersect ret, fin;
    	Vector<DGEPolygonCellData> activeList = customRegion ? _regionVisible : _visible;
    	
    	fin = new DGEIntersect();
    	
    	for (int i = 0; i < activeList.size(); i++)
    	{
    		if ((layer == -1 || activeList.get(i).Layer == layer) &&
    			(classFilter.length() == 0 || activeList.get(i).Polygon.EntityData.Classname.equalsIgnoreCase(classFilter)))
    		{
    			if (shape.GetShape() == ShapeType.Line)
    				ret = activeList.get(i).Polygon.Intersects((DGELine) shape, offset);
    			else if (shape.GetShape() == ShapeType.Circle)
    				ret = activeList.get(i).Polygon.Intersects((DGECircle) shape, offset);
    			else if (shape.GetShape() == ShapeType.Rectangle)
    				ret = activeList.get(i).Polygon.Intersects((DGERectangle) shape, offset);
    			else
    				ret = activeList.get(i).Polygon.Intersects((DGEPolygon) shape, offset);
    			
    			if (ret.Collides)
    			{
    				if (returnPolygon != null)
    					returnPolygon.add(activeList.get(i).Polygon);
    				
    				if ((testType & DGEPolygonCollision.Multiple) != DGEPolygonCollision.Multiple)
    					return ret;
    				
    				for (int j = 0; j < ret.Normals.size(); j++)
    					fin.Normals.add(ret.Normals.get(j));
    				
    				for (int j = 0; j < ret.Points.size(); j++)
    					fin.Points.add(ret.Points.get(j));
    				
    				fin.Collides = true;
    			}
    		}
    	}
    	
    	return fin;
    }

    // TestEx - Includes rotation
    public DGEIntersect TestEx(DGEEntityEx polygon)
    {
    	return TestEx(polygon, new DGEVector());
    }

    public DGEIntersect TestEx(DGEEntityEx polygon, DGEVector offset)
    {
    	return TestEx(polygon, offset, 0);
    }

    public DGEIntersect TestEx(DGEEntityEx polygon, DGEVector offset, float rotation)
    {
    	return TestEx(polygon, offset, rotation, null);
    }

    public DGEIntersect TestEx(DGEEntityEx polygon, DGEVector offset, float rotation, DGEPolygon returnPoly)
    {
    	return TestEx(polygon, offset, rotation, returnPoly, DGEPolygonCollision.Static);
    }

    public DGEIntersect TestEx(DGEEntityEx polygon, DGEVector offset, float rotation, DGEPolygon returnPoly, int testType)
    {
    	return TestEx(polygon, offset, rotation, returnPoly, testType, "");
    }

    public DGEIntersect TestEx(DGEEntityEx polygon, DGEVector offset, float rotation, DGEPolygon returnPoly, int testType, String classFilter)
    {
    	return TestEx(polygon, offset, rotation, returnPoly, testType, classFilter, -1);
    }

    public DGEIntersect TestEx(DGEEntityEx polygon, DGEVector offset, float rotation, DGEPolygon returnPoly, int testType, String classFilter,  int layerFilter)
    {
    	return TestEx(polygon, offset, rotation, returnPoly, testType, classFilter, layerFilter, false);
    }

    public DGEIntersect TestEx(DGEEntityEx polygon, DGEVector offset, float rotation, DGEPolygon returnPoly, int testType, String classFilter,  int layerFilter, boolean customRegion)
    {
    	DGEIntersect ret;
    	DGEIntersect fin = new DGEIntersect();
    	
    	Vector<DGEPolygonCellData> activeList = customRegion ? _regionVisible : _visible;
    	
    	for (int i = 0; i < activeList.size(); i++)
    	{
    		DGEPolygonCellData current = activeList.get(i);
    		
    		if ((layerFilter == -1 || current.Layer == layerFilter) &&
    			(classFilter.length() == 0 || current.Polygon.EntityData.Classname.equalsIgnoreCase(classFilter)))
			{
    			ret = current.Polygon.Intersects(polygon, offset, rotation);
    			
    			if (ret.Collides)
    			{
    				if (returnPoly != null)
    					returnPoly.Copy(current.Polygon);

    				if ((testType & DGEPolygonCollision.Multiple) != DGEPolygonCollision.Multiple)
    					return ret;
    				
    				for (int j = 0; j < ret.Normals.size(); j++)
    					fin.Normals.add(ret.Normals.get(j));
    				
    				for (int j = 0; j < ret.Points.size(); j++)
    					fin.Points.add(ret.Points.get(j));
    				
    				fin.Collides = true;
    			}
			}
    	}
    	
    	return fin;
    }

    public void AddPolygon(DGEPolygon polygon, int layer)
    {
    	DGEPolygonCellData cellData = new DGEPolygonCellData(polygon, layer, _polys.size());
    	_polys.add(cellData);
    	
    	int a1x = (int) ((polygon.GetAABB().p1.x - _area.p1.x) / _cellsize.x);
    	int a1y = (int) ((polygon.GetAABB().p1.y - _area.p1.y) / _cellsize.y);
    	int a4x = (int) ((polygon.GetAABB().p2.x - _area.p1.x) / _cellsize.x);
    	int a4y = (int) ((polygon.GetAABB().p2.y - _area.p1.y) / _cellsize.y);
    	
    	if (a1x < 0) a1x = 0; if (a1y < 0) a1y = 0;
    	if (a4x < 0) a4x = 0; if (a4y < 0) a4y = 0;
    	if (a1x >= _gsX) a1x = _gsX - 1; if (a1y >= _gsY) a1y = _gsY - 1;
    	if (a4x >= _gsX) a4x = _gsX - 1; if (a4y >= _gsY) a4y = _gsY - 1;
    	
    	for (int y = a1y; y <= a4y; y++)
    	{
    		for (int x = a1x; x <= a4x; x++)
    		{
    			DGEPolygonCell cell = _cells.get(x + y * _gsX);
    			
    			if (polygon.GetAABB().Intersect(cell.Rect))
    				cell.Objects.add(cellData);
    		}
    	}
    }
}
