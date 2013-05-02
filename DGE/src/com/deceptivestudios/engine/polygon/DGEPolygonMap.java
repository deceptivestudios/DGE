package com.deceptivestudios.engine.polygon;

import java.util.Vector;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.entity.DGEEntity;
import com.deceptivestudios.engine.entity.DGEEntityEx;
import com.deceptivestudios.engine.entity.DGEResourceReader;
import com.deceptivestudios.engine.geometry.DGECircle;
import com.deceptivestudios.engine.geometry.DGEIntersect;
import com.deceptivestudios.engine.geometry.DGELine;
import com.deceptivestudios.engine.geometry.DGEPolygon;
import com.deceptivestudios.engine.geometry.DGERectangle;
import com.deceptivestudios.engine.geometry.DGEShape;
import com.deceptivestudios.engine.geometry.DGEShape.ShapeType;
import com.deceptivestudios.engine.helper.DGERect;
import com.deceptivestudios.engine.helper.DGEVector;

public class DGEPolygonMap 
{
	private DGE _dge;
	
	private boolean _mipmap;
	private DGERect _view, _viewCustom, _mapSize;
	private DGEVector _viewSize, _viewSizeHalf;
	
	private String _textureBase;
	
	private Vector<DGEPolygon> _background;
	private Vector<DGEPolygon> _middleground;
	private Vector<DGEPolygon> _foreground;
	
	private Vector<DGEEntity> _entities;
	private boolean _safeClear;
	private DGEEntity _clearSpawn;
	private DGEPolygonGrid _grid;
	private int _entityClassIDCounter;
	private Vector<EntityLookup> _entityLookup;
	private Vector<DGEEntity> _entityClasses;
	private Vector<DGEPolygonTextureData> _textureCache;

	private DGEVector _cameraPosition;
	private float _cameraZoom;
	private int _cameraRotation;
	
	public DGEPolygonMap()
	{
		this(new DGEVector(800, 600));
	}
	
	public DGEPolygonMap(DGEVector viewSize)
	{
		this(viewSize, false);
	}
	
	public DGEPolygonMap(DGEVector viewSize, boolean mipmap)
	{
		_dge = DGE.Interface(DGE.DGE_VERSION);
		
		_textureBase = "";
		
		_cameraPosition = new DGEVector();
		_cameraZoom = 1f;
		_cameraRotation = 0;
		
		_safeClear = false;
		_clearSpawn = null;
		_mipmap = mipmap;
		
		_viewSize = viewSize;
		_viewSizeHalf = viewSize.divide(2f);
		
		_view = new DGERect(-_viewSizeHalf.x, -_viewSizeHalf.y, _viewSizeHalf.x, _viewSizeHalf.y);
		_mapSize = new DGERect();
		
		_grid = null;
		
		_background = new Vector<DGEPolygon>();
		_middleground = new Vector<DGEPolygon>();
		_foreground = new Vector<DGEPolygon>();
		
		_entities = new Vector<DGEEntity>();

		_entityClassIDCounter = 0;
		
		_entityClasses = new Vector<DGEEntity>();
		_entityLookup = new Vector<EntityLookup>();
		
		_textureCache = new Vector<DGEPolygonTextureData>();
		
		CreateEntityLookup(new EntityLookup(-1, DGEPolygonLayer.Back, ""));
		CreateEntityLookup(new EntityLookup(-1, DGEPolygonLayer.Middle1, ""));
		CreateEntityLookup(new EntityLookup(-1, DGEPolygonLayer.Middle2, ""));
		CreateEntityLookup(new EntityLookup(-1, DGEPolygonLayer.Front, ""));
		
		Clear();
	}
	
	public DGEPolygon AddPolygon(DGEPolygon polygon)
	{
		return AddPolygon(polygon, DGEPolygonMapLayer.Back);
	}
	
	public DGEPolygon AddPolygon(DGEPolygon polygon, int layer)
	{
		return AddPolygon(polygon, layer, false);
	}
	
	public DGEPolygon AddPolygon(DGEPolygon polygon, int layer, boolean addToGrid)
	{
		GetLayer(layer).add(polygon);
		
		if (polygon.IsEntity())
		{
			Vector<DGEEntity> list = GetDynamicEntityList(polygon.EntityData.Classname);
			
			if (list != null)
				list.add((DGEEntity) polygon);
		}
		
		if (addToGrid && _grid != null)
			_grid.AddPolygon(polygon, layer);
		
		//if (StaticEntityCallback)
		//	StaticEntityCallback(polygon, layer);
		
		return polygon;
	}
	
	private Vector<DGEPolygon> GetLayer(int layer)
	{
		if (layer == DGEPolygonMapLayer.Back)
			return _background;
		if (layer == DGEPolygonMapLayer.Middle)
			return _middleground;
		if (layer == DGEPolygonMapLayer.Front)
			return _foreground;
		
		return null;
	}
	
	public void Clear() 
	{
		Clear(false);
	}
	
	public void Clear(boolean safe) 
	{
		Clear(safe, null);
	}
	
	public void Clear(boolean safe, DGEEntity spawn) 
	{
		_clearSpawn = spawn;
		
		if (safe)
		{
			_safeClear = true;
			return;
		}

		for (int i = DGEPolygonMapLayer.Back; i <= DGEPolygonMapLayer.Front; i++)
		{
			Vector<DGEPolygon> layer = GetLayer(i);
			
			if (layer.size() > 0)
			{
				for (int j = 0; j < layer.size(); j++)
				{
					if (layer.get(j).IsEntity())
						((DGEEntity) layer.get(j)).Free();
				}
				
				layer.clear();
			}
		}
		
		for (int i = 0; i < _entities.size(); i++)
		{
			_entities.get(i).Free();
		}
		
		_entities.clear();
		
		for (int i = 0; i < _textureCache.size(); i++)
			_dge.Texture_Free(_textureCache.get(i).Texture);
		
		_textureCache.clear();
		
		for (int i = 0; i < _entityLookup.size(); i++)
			_entityLookup.get(i).Entities.clear();
		
		_mapSize = new DGERect();
		_grid = null;
		
		if (_clearSpawn != null)
		{
			AddDynamicEntity(_clearSpawn);
			_clearSpawn = null;
		}
	}

	public void RegisterEntityClass(String className, DGEEntity entity)
	{
		if (entity != null)
		{
			entity.EntityData.Classname = className;
			_entityClasses.add(entity);
		}
		
		EntityLookup lookup = new EntityLookup(_entityClassIDCounter, -1, className);
		_entityClassIDCounter++;
		
		CreateEntityLookup(lookup);
	}
	
	private void CreateEntityLookup(EntityLookup entityLookup) 
	{
		_entityLookup.add(entityLookup);
	}

	public int GetTexture(String filename)
	{
		for (int i = 0; i < _textureCache.size(); i++)
		{
			if (_textureCache.get(i).Filename.equalsIgnoreCase(filename))
				return _textureCache.get(i).Texture;
		}
		
		DGEPolygonTextureData texture = new DGEPolygonTextureData();
		
		texture.Filename = filename;
		texture.Texture = _dge.Texture_Load(_textureBase.concat(filename));
		
		if (texture.Texture != 0)
			_textureCache.add(texture);
		
		return texture.Texture;
	}
	
	public void SetCustomTestRegion(DGERect customRegion)
	{
		_viewCustom = customRegion;
		
		if (_grid != null)
			_grid.Prepare(_viewCustom);
	}
	
	public DGEEntity CreateEntityByClassname(String className)
	{
		for (int j = 0; j < _entityClasses.size(); j++)
		{
			if (_entityClasses.get(j).EntityData.Classname.equalsIgnoreCase(className))
				return _entityClasses.get(j).Spawn();
		}
		
		return null;
	}
	
	public void CreateOptimisedGrid(DGERect gridRect)
	{
		CreateOptimisedGrid(gridRect, new DGEVector(1024, 768));
	}
	
	public void CreateOptimisedGrid(DGERect gridRect, DGEVector cellSize)
	{
		_grid = new DGEPolygonGrid(gridRect, _view, cellSize);
	}
	
	public void SetTextureBasePath(String path)
	{
		_textureBase = path;
	}
	
	public Vector<DGEEntity> GetDynamicEntityList(int layer)
	{
		for (int i = 0; i < _entityLookup.size(); i++)
		{
			if (_entityLookup.get(i).Layer == layer)
				return _entityLookup.get(i).Entities;
		}

		return null;
	}

	public Vector<DGEEntity> GetDynamicEntityList(String target)
	{
		for (int i = 0; i < _entityLookup.size(); i++)
		{
			if (_entityLookup.get(i).ClassName.equalsIgnoreCase(target))
				return _entityLookup.get(i).Entities;
		}

		return null;
	}
	
	public DGEVector MapToScreen(DGEVector position)
	{
		return new DGEVector(((position.x - _view.p1.x) / (_view.p2.x - _view.p1.x)) * _viewSize.x,
							 ((position.y - _view.p1.y) / (_view.p2.y - _view.p1.y)) * _viewSize.y);
	}
	
	public DGEVector ScreenToMap(DGEVector position)
	{
		return new DGEVector(_view.p1.x + ((position.x / _viewSize.x) * (_view.p2.x - _view.p1.x)),
							 _view.p1.y + ((position.y / _viewSize.y) * (_view.p2.y - _view.p1.y)));
	}
	
	public void SetTransform(DGEVector position, float zoom)
	{
		_cameraPosition = new DGEVector(position);
		_cameraZoom = zoom;
		
		_view = new DGERect(_cameraPosition.x - (_viewSizeHalf.x / _cameraZoom),
							_cameraPosition.y - (_viewSizeHalf.y / _cameraZoom),
						    _cameraPosition.x + (_viewSizeHalf.x / _cameraZoom), 
						    _cameraPosition.y + (_viewSizeHalf.y / _cameraZoom));
	}
	
	public DGEVector GetTransform()
	{
		return _cameraPosition;
	}
	
	public void Render()
	{
		Render(-1);
	}
	
	public void Render(int layer)
	{
		float x = -_cameraPosition.x + ((_viewSizeHalf.x * 1f) / _cameraZoom);
		float y = -_cameraPosition.y + ((_viewSizeHalf.y * 1f) / _cameraZoom);
		
		_dge.Gfx_SetTransform(0, 0, x, y, _cameraRotation, _cameraZoom, _cameraZoom);
		
		Vector<DGEEntity> list;
		
		if (layer == -1 || layer == DGEPolygonMapLayer.Back)
		{
			list = GetDynamicEntityList(DGEPolygonLayer.Back);
			
			for (int i = 0; i < list.size(); i++)
				list.get(i).Render();
			
			if (_grid != null)
				_grid.Render(DGEPolygonMapLayer.Back);
		}
		
		if (layer == -1 || layer == DGEPolygonMapLayer.Middle)
		{
			list = GetDynamicEntityList(DGEPolygonLayer.Middle1);
			
			for (int i = 0; i < list.size(); i++)
				list.get(i).Render();
			
			if (_grid != null)
				_grid.Render(DGEPolygonMapLayer.Middle);
			
			list = GetDynamicEntityList(DGEPolygonLayer.Middle2);
			
			for (int i = 0; i < list.size(); i++)
				list.get(i).Render();
		}
		
		if (layer == -1 || layer == DGEPolygonMapLayer.Front)
		{
			if (_grid != null)
				_grid.Render(DGEPolygonMapLayer.Front);
			
			list = GetDynamicEntityList(DGEPolygonLayer.Front);
			
			for (int i = 0; i < list.size(); i++)
				list.get(i).Render();
		}
		
		_dge.Gfx_SetTransform();
	}

	public void RenderPass(int flags)
	{
		float x = -_cameraPosition.x + (_viewSizeHalf.x * 1f / _cameraZoom);
		float y = -_cameraPosition.y + (_viewSizeHalf.y * 1f / _cameraZoom);
		
		_dge.Gfx_SetTransform(0, 0, x, y, _cameraRotation, _cameraZoom, _cameraZoom);
		
		Vector<DGEEntity> list;
		
		list = GetDynamicEntityList(DGEPolygonLayer.Back);
		
		for (int i = 0; i < list.size(); i++)
			list.get(i).Render(flags);
		
		if (_grid != null)
			_grid.Render(DGEPolygonMapLayer.Back);

		list = GetDynamicEntityList(DGEPolygonLayer.Middle1);
		
		for (int i = 0; i < list.size(); i++)
			list.get(i).Render(flags);
		
		if (_grid != null)
			_grid.Render(DGEPolygonMapLayer.Middle);
		
		list = GetDynamicEntityList(DGEPolygonLayer.Middle2);
		
		for (int i = 0; i < list.size(); i++)
			list.get(i).Render(flags);
		
		if (_grid != null)
			_grid.Render(DGEPolygonMapLayer.Front);
		
		list = GetDynamicEntityList(DGEPolygonLayer.Front);
		
		for (int i = 0; i < list.size(); i++)
			list.get(i).Render(flags);
		
		_dge.Gfx_SetTransform();
	}
	
	public void Update(float delta)
	{
		if (_grid != null)
		{
			_grid.Prepare();
			_grid.Update(delta);
		}
		
		for (int i = 0; i < _entities.size(); i++)
		{
			DGEEntity entity = _entities.get(i); 
			entity.Update(delta);
			
			if (entity.Deleted())
			{
				for (int j = 0; j < _entityLookup.size(); j++)
				{
					EntityLookup lookup = _entityLookup.get(j);
					
					for (int k = 0; k < lookup.Entities.size(); k++)
					{
						if (lookup.Entities.get(k) == entity)
							lookup.Entities.remove(k);
					}
				}
				
				_entities.remove(i);
				i--;
			}
		}
		
		if (_safeClear)
		{
			Clear(false, _clearSpawn);
			_safeClear = false;
		}
	}
	
	public DGEPolygon TestPoint(DGEVector point)
	{
		return TestPoint(point, DGEPolygonCollision.Static);
	}
	
	public DGEPolygon TestPoint(DGEVector point, int testType)
	{
		return TestPoint(point, testType, "");
	}
	
	public DGEPolygon TestPoint(DGEVector point, int testType, String classFilter)
	{
		return TestPoint(point, testType, classFilter, -1);
	}
	
	public DGEPolygon TestPoint(DGEVector point, int testType, String classFilter, int layerFilter)
	{
		return TestPoint(point, testType, classFilter, layerFilter, false);
	}

	public DGEPolygon TestPoint(DGEVector point, int testType, String classFilter, int layerFilter, boolean customRegion)
	{
		DGEPolygon ret = null;
		
		if ((testType & DGEPolygonCollision.Static) == DGEPolygonCollision.Static)
			ret = _grid.TestPoint(point, classFilter, layerFilter, customRegion);
		
		if (ret != null)
			return ret;
		
		if ((testType & DGEPolygonCollision.Dynamic) == DGEPolygonCollision.Dynamic)
		{
			Vector<DGEEntity> list = null;
			
			if (classFilter.length() > 0)
				list = GetDynamicEntityList(classFilter);
			else if (layerFilter != -1)
				list = GetDynamicEntityList(layerFilter);
			
			if (list == null)
				list = _entities;
		
			for (int j = 0; j < list.size(); j++)
			{
				DGEEntity entity = list.get(j);
				
				if ((testType & DGEPolygonCollision.Visible) != DGEPolygonCollision.Visible ||
					entity.GetAABB().Intersect(_view))
				{
					if (entity.TestPoint(point))
						return entity;
				}
			}
		}
		
		return ret;
	}

	public DGEIntersect Test(DGEShape shape) 
	{
		return Test(shape, new DGEVector());
	}

	public DGEIntersect Test(DGEShape shape, DGEVector offset) 
	{
		return Test(shape, offset, null);
	}

	public DGEIntersect Test(DGEShape shape, DGEVector offset, Vector<DGEPolygon> returnPolygons) 
	{
		return Test(shape, offset, returnPolygons, DGEPolygonCollision.Static);
	}

	public DGEIntersect Test(DGEShape shape, DGEVector offset, Vector<DGEPolygon> returnPolygons, int testType) 
	{
		return Test(shape, offset, returnPolygons, testType, "");
	}

	public DGEIntersect Test(DGEShape shape, DGEVector offset, Vector<DGEPolygon> returnPolygons, int testType, String classFilter) 
	{
		return Test(shape, offset, returnPolygons, testType, classFilter, -1);
	}

	public DGEIntersect Test(DGEShape shape, DGEVector offset, Vector<DGEPolygon> returnPolygons, int testType, String classFilter, int layerFilter) 
	{
		return Test(shape, offset, returnPolygons, testType, classFilter, layerFilter, false);
	}

	public DGEIntersect Test(DGEShape shape, DGEVector offset, Vector<DGEPolygon> returnPolygons, int testType, String classFilter, int layerFilter, boolean customRegion) 
	{
		DGEIntersect ret = new DGEIntersect();
		
		if ((testType & DGEPolygonCollision.Static) == DGEPolygonCollision.Static)
		{
			ret = _grid.Test(shape, offset, returnPolygons, testType, classFilter, layerFilter, customRegion);
			
			if (ret.Collides)
				return ret;
		}

		if ((testType & DGEPolygonCollision.Dynamic) == DGEPolygonCollision.Dynamic)
		{
			Vector<DGEEntity> list = null;
			
			if (classFilter.length() > 0)
				list = GetDynamicEntityList(classFilter);
			else if (layerFilter != -1)
				list = GetDynamicEntityList(layerFilter);
			
			if (list == null)
				list = _entities;
			
			for (int j = 0; j < list.size(); j++)
			{
				DGEEntity e = list.get(j);
				
				if ((testType & DGEPolygonCollision.Visible) != DGEPolygonCollision.Visible || e.GetAABB().Intersect(_view))
				{
					if (list != _entities || (classFilter.length() == 0 || e.EntityData.Classname.equalsIgnoreCase(classFilter)))
					{
						if (shape != e)
						{
							if (shape.GetShape() == ShapeType.Line)
								ret = e.Intersects((DGELine) shape, offset);
							else if (shape.GetShape() == ShapeType.Circle)
								ret = e.Intersects((DGECircle) shape, offset);
							else if (shape.GetShape() == ShapeType.Rectangle)
								ret = e.Intersects((DGERectangle) shape, offset);
							else
								ret = e.Intersects((DGEPolygon) shape, offset);
							
							if (ret.Collides)
							{
								if (returnPolygons != null)
									returnPolygons.add(e);
								
								return ret;
							}
						}
					}
				}
			}
		}
		
		return ret;
	}
	
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
	
	public DGEIntersect TestEx(DGEEntityEx polygon, DGEVector offset, float rotation, DGEPolygon returnPoly, int testType, String classFilter, int layerFilter)
	{
		return TestEx(polygon, offset, rotation, returnPoly, testType, classFilter, layerFilter, false);
	}
	
	public DGEIntersect TestEx(DGEEntityEx polygon, DGEVector offset, float rotation, DGEPolygon returnPoly, int testType, String classFilter, int layerFilter, boolean customRegion)
	{
		DGEIntersect ret = new DGEIntersect();
		
		if ((testType & DGEPolygonCollision.Static) == DGEPolygonCollision.Static)
			ret = _grid.TestEx(polygon, offset, rotation, returnPoly, testType, classFilter, layerFilter, customRegion);
		
		return ret;
	}
	
	public DGEPolygon GetStaticEntityFromProperty(String name, String value)
	{
		for (int i = DGEPolygonMapLayer.Back; i <= DGEPolygonMapLayer.Front; i++)
		{
			Vector<DGEPolygon> layer = GetLayer(i);
			
			if (layer.size() > 0)
			{
				for (int j = 0; j < layer.size(); j++)
				{
					if (layer.get(j).EntityData.GetString(name).equalsIgnoreCase(value))
						return layer.get(j);
				}
			}
		}
		
		return null;
	}
	
	public DGEPolygon GetDynamicEntityFromProperty(String name, String value)
	{
		for (int j = 0; j < _entities.size(); j++)
		{
			if (_entities.get(j).EntityData.GetString(name).equalsIgnoreCase(value))
				return _entities.get(j);
		}
		
		return null;
	}
	
	public void RemoveDynamicEntities(String entityClass)
	{
		Vector<DGEEntity> list = GetDynamicEntityList(entityClass);
		
		if (list != null)
		{
			for (int i = 0; i < list.size(); i++)
				list.get(i).Delete();
		}
	}
	
	public void TriggerEntities(String entityClass)
	{
		Vector<DGEEntity> list = GetDynamicEntityList(entityClass);
		
		if (list != null)
		{
			for (int i = 0; i < list.size(); i++)
				list.get(i).Trigger();
		}
	}
	
	public void ResetEntities(String entityClass)
	{
		Vector<DGEEntity> list = GetDynamicEntityList(entityClass);
		
		if (list != null)
		{
			for (int i = 0; i < list.size(); i++)
				list.get(i).Reset();
		}
	}
	
	public void AddDynamicEntity(DGEEntity entity)
	{
		AddDynamicEntity(entity, true);
	}
	
	public void AddDynamicEntity(DGEEntity entity, boolean initialiseEntity)
	{
		_entities.add(entity);
		
		Vector<DGEEntity> list = GetDynamicEntityList(entity.EntityData.Classname);
		
		if (list != null)
			list.add(entity);
		else
			_dge.System_Log("AddDynamicEntity Warning: Classname '%s' was not registered and may malfunction", entity.EntityData.Classname);
		
		GetDynamicEntityList(entity.GetRenderLayer()).add(entity);
		
		if (initialiseEntity)
		{
			entity.SetMap(this);
			entity.Initialise();
		}
		
		//if (DynamicEntityCallback)
		//	DynamicEntityCallback(entity);
	}
	
	public boolean LoadFile(String filename)
	{
		return LoadFile(filename, true);
	}
	
	public boolean LoadFile(String filename, boolean clear)
	{
		return LoadFile(filename, clear, new DGEVector());
	}
	
	public boolean LoadFile(String filename, boolean clear, DGEVector offset)
	{
		return LoadFile(filename, clear, offset, 0f);
	}
	
	public boolean LoadFile(String filename, boolean clear, DGEVector offset, float rotation)
	{
		if (clear)
			Clear();
		
		DGEResourceReader file = new DGEResourceReader(filename);
		
		int format = file.ReadInt();
		
		if (format == 314 || format == 315)
		{
			if (format == 315)
			{
				_mapSize.p1.x = file.ReadFloat();
				_mapSize.p1.y = file.ReadFloat();
				_mapSize.p2.x = file.ReadFloat();
				_mapSize.p2.y = file.ReadFloat();
			}
			
			int layerCount = file.ReadInt();
			
			for (int h = 0; h < layerCount; h++)
			{
				int polyCount = file.ReadInt();
				
				for (int i = 0; i < polyCount; i++)
				{
					DGEEntity entity = null;
					DGEPolygon polygon = new DGEPolygon(this);
					polygon.Load(file, format);
					
					for (int j = 0; j < _entityClasses.size(); j++)
					{
						if (polygon.EntityData.Classname.equalsIgnoreCase(_entityClasses.get(j).EntityData.Classname))
						{
							entity = _entityClasses.get(j).Spawn();
							entity.Copy(polygon);
							
							polygon = entity;
							break;
						}
					}
					
					polygon.SetMap(this);
					
					if (entity == null)
						AddPolygon(polygon, h);
					else if (entity.IsStatic())
						AddPolygon(polygon, h);
					else
						AddDynamicEntity(entity, false);

					if (rotation != 0)
						polygon.Rotate(new DGEVector(), rotation);
					
					polygon.Shift(offset);
					
					if (!clear)
					{
						boolean doGrid = false;
						
						if (polygon.IsEntity())
						{
							entity = (DGEEntity) polygon;
							entity.Initialise();
							
							if (entity.IsStatic())
								doGrid = true;
						}
						else
						{
							doGrid = true;
						}
						
						if (doGrid && _grid != null)
							_grid.AddPolygon(polygon, h);
					}
					
					if (_mapSize.p1.x > polygon.GetAABB().p1.x) 
						_mapSize.p1.x = polygon.GetAABB().p1.x;
					
					if (_mapSize.p1.y > polygon.GetAABB().p1.y) 
						_mapSize.p1.y = polygon.GetAABB().p1.y;
					
					if (_mapSize.p2.x < polygon.GetAABB().p2.x) 
						_mapSize.p2.x = polygon.GetAABB().p2.x;
					
					if (_mapSize.p2.y < polygon.GetAABB().p2.y) 
						_mapSize.p2.y = polygon.GetAABB().p2.y;
				}
			}
			
			file.Close();

			if (clear)
			{
				for (int i = DGEPolygonMapLayer.Back; i <= DGEPolygonMapLayer.Front; i++)
				{
					Vector<DGEPolygon> layer = GetLayer(i);
					
					for (int j = 0; j < layer.size(); j++)
					{
						if (layer.get(j).IsEntity())
							((DGEEntity) layer.get(j)).Initialise();
					}
				}
				
				for (int i = 0; i < _entities.size(); i++)
					_entities.get(i).Initialise();
				
				_grid = new DGEPolygonGrid(_mapSize, _view);
				
				for (int i = DGEPolygonMapLayer.Back; i <= DGEPolygonMapLayer.Front; i++)
				{
					Vector<DGEPolygon> layer = GetLayer(i);
					
					for (int j = 0; j < layer.size(); j++)
					{
						DGEPolygon polygon = layer.get(j);
						
						if (polygon.IsEntity())
						{
							DGEEntity entity = (DGEEntity) polygon;
							
							if (entity.IsStatic())
								_grid.AddPolygon(entity, i);
						}
						else
						{
							_grid.AddPolygon(polygon, i);
						}
					}
				}
			}
			
			return true;
		}
		else
		{
			file.Close();
			
			_dge.System_Log("DGEPolygonMap: Load() Error Bad File Format");
			return false;
		}
	}
	
	public boolean TestVisibility(DGEPolygon polygon)
	{
		return TestVisibility(polygon, 0f);
	}
	
	public boolean TestVisibility(DGEPolygon polygon, float padding)
	{
		return polygon.GetAABB().Intersect(new DGERect(_view.p1.x - padding, _view.p1.y - padding, _view.p2.x + padding, _view.p2.y + padding));
	}
	
	public boolean TestVisibility(DGEVector point)
	{
		return TestVisibility(point, 0f);
	}
	
	public boolean TestVisibility(DGEVector point, float padding)
	{
		return new DGERect(_view.p1.x - padding, _view.p1.y - padding, _view.p2.x + padding, _view.p2.y + padding).TestPoint(point);
	}
	
	public void ResetTransform()
	{
		_dge.Gfx_SetTransform(0, 0, (-_cameraPosition.x * _cameraZoom) + _viewSizeHalf.x, (-_cameraPosition.y * _cameraZoom) + _viewSizeHalf.y, _cameraRotation, _cameraZoom, _cameraZoom);
	}
	
	private class EntityLookup
	{
		public int Id;
		public int Layer;
		public String ClassName;
		public Vector<DGEEntity> Entities;
		
		public EntityLookup()
		{
			Id = -1;
			Layer = -1;
			
			Entities = new Vector<DGEEntity>();
		}
		
		public EntityLookup(int id, int layer, String className)
		{
			this();
			
			Id = id;
			Layer = layer;
			ClassName = className;
		}
	}
}
