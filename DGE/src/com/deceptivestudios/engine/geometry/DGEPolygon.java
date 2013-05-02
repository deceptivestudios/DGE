package com.deceptivestudios.engine.geometry;

import java.util.Vector;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.entity.DGEEntityEx;
import com.deceptivestudios.engine.entity.DGEProperties;
import com.deceptivestudios.engine.entity.DGEResourceReader;
import com.deceptivestudios.engine.helper.DGEColor;
import com.deceptivestudios.engine.helper.DGEQuad;
import com.deceptivestudios.engine.helper.DGERect;
import com.deceptivestudios.engine.helper.DGESprite;
import com.deceptivestudios.engine.helper.DGETriple;
import com.deceptivestudios.engine.helper.DGEVector;
import com.deceptivestudios.engine.polygon.DGEPolygonData;
import com.deceptivestudios.engine.polygon.DGEPolygonMap;
import com.deceptivestudios.engine.polygon.DGEPolygonRenderer;
import com.deceptivestudios.engine.polygon.DGEPolygonTextureData;
import com.deceptivestudios.engine.polygon.DGEPolygonVertex;

public class DGEPolygon extends DGEShape
{
    protected DGE _dge;

	protected boolean _isEntity;

	protected DGERect _aabb;
	protected DGEVector _center;
	protected DGEVector _hotspot;

	protected DGESprite _sprite;
	
	protected DGEPolygonMap _map;

    protected Vector<DGEQuad> _borderQuads;
    protected Vector<DGETriple> _borderTriangles;
    protected Vector<DGEQuad> _shadowQuads;

    public Vector<DGEPolygonVertex> Vertices;
    public Vector<DGETriangle> Triangles;

    public DGEPolygonData TextureData;
    public DGEProperties EntityData;
	
	public DGEPolygon()
	{
		this(null);
	}
	
	public DGEPolygon(DGEPolygonMap map)
	{
		super(DGEShape.ShapeType.Polygon);
		
		_dge = DGE.Interface(DGE.DGE_VERSION);
		
		_map = map;
		
		_aabb = new DGERect();
		_center = new DGEVector();
		_hotspot = new DGEVector();
		
		_sprite = null;
		
		_borderQuads = new Vector<DGEQuad>();
		_borderTriangles = new Vector<DGETriple>();
		_shadowQuads = new Vector<DGEQuad>();
		
		Vertices = new Vector<DGEPolygonVertex>();
		Triangles = new Vector<DGETriangle>();
		
		TextureData = new DGEPolygonData();
		EntityData = new DGEProperties();
	}
	
	public void Copy(DGEPolygon polygon)
	{
		for (int i = 0; i < polygon.VertexCount(); i++)
		{
			Vertices.add(new DGEPolygonVertex(polygon.Vertices.get(i)));
		}
		
		for (int i = 0; i < polygon.TriangleCount(); i++)
		{
			AddTriangle(new DGETriangle(polygon.Triangles.get(i)));
		}
		
		_aabb = polygon._aabb;
		_center = polygon.GetCenter();
		
		_hotspot = polygon.GetHotSpot();
		
		if (polygon.GetSprite() != null)
			_sprite = new DGESprite(polygon.GetSprite());
		
		TextureData = polygon.TextureData;
		EntityData = polygon.EntityData;
		
		GenerateBorder();
	}
	
	public int VertexCount() { return Vertices.size(); }
	
	public DGEPolygonVertex AddVertex(DGEVector pos)
	{
		DGEPolygonVertex ret = new DGEPolygonVertex(pos);
		
		if (VertexCount() > 0)
			Vertices.get(VertexCount() - 1).Next = ret;
		
		Vertices.add(ret);
		
		return ret;
	}

    public void RemoveVertex(DGEPolygonVertex which)
    {
    	for (int i = 0; i < VertexCount(); i++)
    	{
    		if (Vertices.get(i) == which)
    		{
    			if (i > 0)
    				Vertices.get(i - 1).Next = (i + 1 >= VertexCount()) ? null : Vertices.get(i + 1);
    			
    			Vertices.remove(i);
    		}
    	}
    }
    
    public void ClearVertices()
    {
    	Vertices.clear();
    }
    
	private int TriangleCount() { return Triangles.size(); }
	
	private void AddTriangle(DGETriangle triangle)
	{
		Triangles.add(triangle);
	}
	
	private void ClearTriangles()
	{
		Triangles.clear();
	}
	
	public DGETriangle TestGetTriangle(DGEVector point)
	{
	    if (_aabb.TestPoint(point))
	    {
	        for (int i = 0; i< TriangleCount(); i++)
	        {
	            if (Triangles.get(i).Contains(point))
	                return Triangles.get(i);
	        }
	    }
	    
	    return null;
	}
	
	private DGEPolygonTextureData GetTexture(String filename)
	{
		DGEPolygonTextureData data = new DGEPolygonTextureData();
		
		data.Texture = _map.GetTexture(filename);
		
		if (data.Texture != 0)
		{
			data.OriginalSize.x = _dge.Texture_GetWidth(data.Texture);
			data.OriginalSize.y = _dge.Texture_GetHeight(data.Texture);
		}
		
		data.Filename = filename;
		
		return data;
	}
	
	public void SetTexture(int texture)
	{
		SetTexture(texture, -1);
	}
	
	public void SetTexture(int texture, int blend)
	{
		TextureData.Texture.Texture = texture;
		
		if (blend > -1)
			TextureData.Blend = blend;
		
		for (int i = 0; i < TriangleCount(); i++)
		{
			DGETriangle t = Triangles.get(i);
			
			t.Triple.texture = TextureData.Texture.Texture;
			t.Triple.blend = TextureData.Blend;
		}
	}
	
	public void SetColor(DGEColor color)
	{
		SetColor(color.r, color.g, color.b, color.a);
	}
	
	public void SetColor(float r, float g, float b, float a)
	{
		TextureData.Color = new DGEColor(r, g, b, a);
		
		for (int i = 0; i < TriangleCount(); i++)
		{
			DGETriangle t = Triangles.get(i);
			
			for (int j = 0; j < 3; j++)
			{
				t.Triple.vertices[j].r = r;
				t.Triple.vertices[j].g = g;
				t.Triple.vertices[j].b = b;
				t.Triple.vertices[j].a = a;
			}
		}
	}
	
	public void SetMap(DGEPolygonMap map)
	{
		_map = map;
	}
	
	public void Rotate(DGEVector center, float rotation)
	{
		for (int i = 0; i < VertexCount(); i++)
		{
			DGEVector m = Vertices.get(i).Position.subtract(center);
			m.rotate(rotation);
			
			Vertices.get(i).Position = center.add(m);
			Vertices.get(i).Normal += rotation;
		}
		
		for (int i = 0; i < TriangleCount(); i++)
		{
			DGETriangle t = Triangles.get(i);
			
			for (int j = 0; j < 3; j++)
			{
				DGEVector m = t.Vertices[j].subtract(center);
				m.rotate(rotation);
				
				t.Vertices[j] = center.add(m);
				t.Triple.vertices[j].x = center.x + m.x;
				t.Triple.vertices[j].y = center.y + m.y;
			}
		}
	}
	
	public void Shift(DGEVector amount)
	{
		for (int i = 0; i < VertexCount(); i++)
			Vertices.get(i).Position.addLocal(amount);
		
		for (int i = 0; i < TriangleCount(); i++)
		{
			DGETriangle t = Triangles.get(i);
			
			t.Vertices[0].addLocal(amount);
			t.Vertices[1].addLocal(amount);
			t.Vertices[2].addLocal(amount);
			
			for (int j = 0; j < 3; j++)
			{
				t.Triple.vertices[j].x += amount.x;
				t.Triple.vertices[j].y += amount.y;
			}
		}
		
		_aabb.p1.x += amount.x; _aabb.p1.y += amount.y;
		_aabb.p2.x += amount.x; _aabb.p2.y += amount.y;
		_center.addLocal(amount);
		
		for (int i = 0; i < _borderQuads.size(); i++)
		{
			DGEQuad q = _borderQuads.get(i);
			
			for (int j = 0; j < 4; j++)
			{
				q.vertices[j].x += amount.x;
				q.vertices[j].y += amount.y;
			}
		}
		
		for (int i = 0; i < _borderTriangles.size(); i++)
		{
			DGETriple t = _borderTriangles.get(i);
			
			for (int j = 0; j < 3; j++)
			{
				t.vertices[j].x += amount.x;
				t.vertices[j].y += amount.y;
			}
		}
		
		for (int i = 0; i < _shadowQuads.size(); i++)
		{
			DGEQuad q = _shadowQuads.get(i);
			
			for (int j = 0; j < 4; j++)
			{
				q.vertices[j].x += amount.x;
				q.vertices[j].y += amount.y;
			}
		}
	}
	
	private void CalculateNormals()
	{
		for (int i = 0; i < VertexCount(); i++)
		{
			DGEPolygonVertex current = Vertices.get(i);
			DGEPolygonVertex next = (i == VertexCount() - 1) ? Vertices.get(0) : Vertices.get(i + 1);
			
			DGEVector line = current.Position.subtract(next.Position);
			current.Normal = line.angle() + DGE.M_PI_2;
			
			if (current.Normal > (DGE.M_PI * 2))
				current.Normal -= (DGE.M_PI * 2);
		}
	}
	
	private void CalculateAABB()
	{
		if (Vertices.size() > 0)
		{
			DGEVector min = new DGEVector(Vertices.get(0).Position);
			DGEVector max = new DGEVector(Vertices.get(0).Position);
			
			if (IsPoint())
			{
				if (_sprite != null)
					max.addLocal(new DGEVector(_sprite.GetWidth(), _sprite.GetHeight()));
				else
					max.addLocal(new DGEVector(64, 64));
			}
			else
			{
				if (Vertices.size() > 1)
				{
					for (int i = 0; i < VertexCount(); i++)
					{
						DGEVector p = Vertices.get(i).Position;
						
						if (p.x < min.x) min.x = p.x;
						if (p.y < min.y) min.y = p.y;
						if (p.x > max.x) max.x = p.x;
						if (p.y > max.y) max.y = p.y;
					}
					_aabb.p1.x -= 4;
				}
			}
			
			_aabb = new DGERect(min.x, min.y, max.x, max.y);
			_center = new DGEVector(_aabb.p1.x + ((_aabb.p2.x - _aabb.p1.x) / 2f), _aabb.p1.y + ((_aabb.p2.y - _aabb.p1.y) / 2f));
			
			_aabb.p1.x -= _hotspot.x; _aabb.p1.y -= _hotspot.y; 
			_aabb.p2.x -= _hotspot.x; _aabb.p2.y -= _hotspot.y;
			
			_center.subtractLocal(_hotspot);
		}
		else
		{
			_aabb = new DGERect();
		}
		
	}
	
	private void GenerateBorder()
	{
		if (VertexCount() == 0)
			return;
		
		if (!TextureData.Bordered && !TextureData.Shadowed)
			return;
		
		_borderQuads.clear();
		_borderTriangles.clear();
		_shadowQuads.clear();
		
		DGEQuad border = new DGEQuad();
		DGEQuad shadow = new DGEQuad();
		DGETriple bordert = new DGETriple();
		
		border.blend = shadow.blend = bordert.blend = DGE.DGE_BLEND_ALPHABLEND + DGE.DGE_BLEND_COLORMUL + DGE.DGE_BLEND_NOZWRITE;
		border.texture = bordert.texture = TextureData.BorderTexture.Texture;
		shadow.texture = TextureData.ShadowTexture.Texture;
		
		for (int i = 0; i < 4; i++)
		{
			border.vertices[i].z = 0.5f;
			border.vertices[i].a = border.vertices[i].r = border.vertices[i].g = border.vertices[i].b = 1f;
			
			shadow.vertices[i].z = 0.5f;
			shadow.vertices[i].a = TextureData.ShadowColor.a;
			shadow.vertices[i].r = TextureData.ShadowColor.r;
			shadow.vertices[i].g = TextureData.ShadowColor.g;
			border.vertices[i].b = TextureData.ShadowColor.b;
			
			if (i < 3)
			{
				bordert.vertices[i].z = 0.5f;
				bordert.vertices[i].a = bordert.vertices[i].r = bordert.vertices[i].g = bordert.vertices[i].b = 1f;
			}
		}
		
		float perimeter = 0;
		float perimeterInner = 0;
		float perimeterOuter = 0;
		boolean lastTriangle = false;
		
		DGEVector normal, normalPrev, normalNext;
		DGEVector line, triline, triline2;
		float textureScale;
		
		for (int i = 0; i < VertexCount(); i++)
		{
			DGEPolygonVertex previous = (i == 0) ? Vertices.get(VertexCount() - 1) : Vertices.get(i - 1);
			DGEPolygonVertex current = Vertices.get(i);
			DGEPolygonVertex next = (i >= VertexCount() - 1) ? Vertices.get(0) : Vertices.get(i + 1);
			
			if (next != null && previous != null)
			{
				textureScale = ((TextureData.BorderHeight * 200f) / (TextureData.BorderTexture.OriginalSize.y)) * TextureData.BorderScale;
				
				normal = new DGEVector(0, TextureData.BorderHeight * 200f);
				normal.rotate(current.Normal - DGE.M_PI_2);
				
				normalPrev = new DGEVector(0, TextureData.BorderHeight * 200f);
				normalPrev.rotate(previous.Normal - DGE.M_PI_2);
				
				normalNext = new DGEVector(0, TextureData.BorderHeight * 200f);
				normalNext.rotate(next.Normal - DGE.M_PI_2);
				
				line = next.Position.subtract(current.Position);
				
				boolean render = false;
				
				if (TextureData.BorderMin > TextureData.BorderMax)
				{
	                if (current.Normal >= TextureData.BorderMin || current.Normal <= TextureData.BorderMax) 
	                	render = true;
	            }
	            else
	            {
	                if (current.Normal >= TextureData.BorderMin && current.Normal <= TextureData.BorderMax)
	                	render = true;
	            }
				
				border.vertices[0].x = current.Position.x;
				border.vertices[0].y = current.Position.y;
				border.vertices[0].tx = perimeter;
				border.vertices[0].ty = 1f - (1f / TextureData.BorderTexture.OriginalSize.y) * 2f;
				
				border.vertices[1].x = next.Position.x;
				border.vertices[1].y = next.Position.y;
				border.vertices[1].tx = perimeter + (line.length() / TextureData.BorderTexture.OriginalSize.x) / textureScale;
				border.vertices[1].ty = 1f - (1f / TextureData.BorderTexture.OriginalSize.y) * 2f;
				
				border.vertices[2].x = next.Position.x + normal.x;
				border.vertices[2].y = next.Position.y + normal.y;
				border.vertices[2].tx = perimeter + (line.length() / TextureData.BorderTexture.OriginalSize.x) / textureScale;
				border.vertices[2].ty = (1f / TextureData.BorderTexture.OriginalSize.y);
				
				border.vertices[3].x = current.Position.x + normal.x;
				border.vertices[3].y = current.Position.y + normal.y;
				border.vertices[3].tx = perimeter;
				border.vertices[3].ty = (1f / TextureData.BorderTexture.OriginalSize.y);
				
				perimeter += (line.length() / TextureData.BorderTexture.OriginalSize.x) / textureScale;
				perimeterOuter += (line.length() / TextureData.BorderTexture.OriginalSize.x) / textureScale;
				
				if (render && TextureData.Bordered)
					_borderQuads.add(new DGEQuad(border));
				
				textureScale = ((TextureData.ShadowHeight * 200f) / (TextureData.ShadowTexture.OriginalSize.y)) * TextureData.ShadowScale;
				
				normal = new DGEVector(0, TextureData.ShadowHeight * 120f);
				normal.rotate(current.Normal - DGE.M_PI_2);
				
				normalPrev = new DGEVector(0, TextureData.ShadowHeight * 120f);
				normalPrev.rotate(previous.Normal - DGE.M_PI_2);
				
				normalNext = new DGEVector(0, TextureData.ShadowHeight * 120f);
				normalNext.rotate(next.Normal - DGE.M_PI_2);
				
				triline = normal.subtract(normalPrev);
				triline2 = normalNext.subtract(normal);
				
				shadow.vertices[0].x = current.Position.x;
				shadow.vertices[0].y = current.Position.y;
				shadow.vertices[0].tx = perimeterInner;
				shadow.vertices[0].ty = 1f - (1f / TextureData.ShadowTexture.OriginalSize.y) * 2f;
				
				shadow.vertices[1].x = next.Position.x;
				shadow.vertices[1].y = next.Position.y;
				shadow.vertices[1].tx = perimeterInner + (line.length() / TextureData.ShadowTexture.OriginalSize.x) / textureScale;
				shadow.vertices[1].ty = 1f - (1f / TextureData.ShadowTexture.OriginalSize.y) * 2f;
				
				shadow.vertices[2].x = next.Position.x - normal.x - triline2.x / 2f;
				shadow.vertices[2].y = next.Position.y - normal.y - triline2.y / 2f;
				shadow.vertices[2].tx = perimeterInner + (line.length() / TextureData.ShadowTexture.OriginalSize.x) / textureScale;
				shadow.vertices[2].ty = (1f / TextureData.ShadowTexture.OriginalSize.y);
				
				shadow.vertices[3].x = current.Position.x - normalPrev.x - triline.x / 2f;
				shadow.vertices[3].y = current.Position.y - normalPrev.y - triline.y / 2f;
				shadow.vertices[3].tx = perimeterInner;
				shadow.vertices[3].ty = (1f / TextureData.ShadowTexture.OriginalSize.y);

				perimeterInner += (line.length() / TextureData.ShadowTexture.OriginalSize.x) / textureScale;
				
	            if (TextureData.Shadowed && ( !TextureData.ShadowFollowBorder || (TextureData.ShadowFollowBorder && render)))
	                _shadowQuads.add(new DGEQuad(shadow));
	            
	            DGEVector topline = (next.Position.add(normal)).subtract(next.Position.add(normalNext));
				textureScale = ((TextureData.BorderHeight * 200f) / (TextureData.BorderTexture.OriginalSize.y)) * TextureData.BorderScale;
				
	            bordert.vertices[0].x = next.Position.x;
	            bordert.vertices[0].y = next.Position.y;
	            bordert.vertices[0].tx = perimeter + ((topline.length() / TextureData.BorderTexture.OriginalSize.x) / textureScale) / 2f;
	            bordert.vertices[0].ty = 1f - 1f / TextureData.BorderTexture.OriginalSize.y;
				
	            bordert.vertices[1].x = next.Position.x + normal.x;
	            bordert.vertices[1].y = next.Position.y + normal.y;
	            bordert.vertices[1].tx = perimeterOuter;
	            bordert.vertices[1].ty = 1f - 1f / TextureData.BorderTexture.OriginalSize.y;
	            
				perimeter += (topline.length() / TextureData.BorderTexture.OriginalSize.x) / textureScale;
				perimeterOuter += (topline.length() / TextureData.BorderTexture.OriginalSize.x) / textureScale;
				
	            bordert.vertices[2].x = next.Position.x + normal.x;
	            bordert.vertices[2].y = next.Position.y + normal.y;
	            bordert.vertices[2].tx = perimeterOuter;
	            bordert.vertices[2].ty = 1f / TextureData.BorderTexture.OriginalSize.y;
	            
	            if (render && TextureData.Bordered)
	            {
	            	_borderTriangles.add(new DGETriple(bordert));
	            	lastTriangle = true;
	            }
	            else if (!render && lastTriangle)
	            {
	            	_borderTriangles.remove(_borderTriangles.size() - 1);
	            	lastTriangle = false;
	            }
			}
		}
	}
	
	private float WithinBounds(float n, float offset)
	{
		if (n != 0)
			return offset / n;
		
		return 0;
	}
	
	private void CalculateTextureCoordinates(boolean calcAll)
	{
		for (int i = 0; i < TriangleCount(); i++)
		{
			DGETriangle t = Triangles.get(i);
			
			t.Triple.texture = TextureData.Texture.Texture;
			t.Triple.blend = TextureData.Blend;
			
			for (int j = 0; j < 3; j++)
			{
				t.Triple.vertices[j].r = TextureData.Color.r;
				t.Triple.vertices[j].g = TextureData.Color.g;
				t.Triple.vertices[j].b = TextureData.Color.b;
				t.Triple.vertices[j].a = TextureData.Color.a;
			}
			
			if (calcAll)
			{
				if (!TextureData.Tiled)
				{
					t.Triple.vertices[0].tx = t.Vertices[0].x - _aabb.p1.x;
					t.Triple.vertices[0].ty = t.Vertices[0].y - _aabb.p1.y;
					t.Triple.vertices[1].tx = t.Vertices[1].x - _aabb.p1.x;
					t.Triple.vertices[1].ty = t.Vertices[1].y - _aabb.p1.y;
					t.Triple.vertices[2].tx = t.Vertices[2].x - _aabb.p1.x;
					t.Triple.vertices[2].ty = t.Vertices[2].y - _aabb.p1.y;
					
					t.Triple.vertices[0].tx /= (_aabb.p2.x - _aabb.p1.x);
					t.Triple.vertices[0].ty /= (_aabb.p2.y - _aabb.p1.y);
					t.Triple.vertices[1].tx /= (_aabb.p2.x - _aabb.p1.x);
					t.Triple.vertices[1].ty /= (_aabb.p2.y - _aabb.p1.y);
					t.Triple.vertices[2].tx /= (_aabb.p2.x - _aabb.p1.x);
					t.Triple.vertices[2].ty /= (_aabb.p2.y - _aabb.p1.y);
					
					t.Triple.vertices[0].tx /= TextureData.Scale;
					t.Triple.vertices[0].ty /= TextureData.Scale;
					t.Triple.vertices[1].tx /= TextureData.Scale;
					t.Triple.vertices[1].ty /= TextureData.Scale;
					t.Triple.vertices[2].tx /= TextureData.Scale;
					t.Triple.vertices[2].ty /= TextureData.Scale;
					
					t.Triple.vertices[0].tx += TextureData.OffsetX;
					t.Triple.vertices[0].ty += TextureData.OffsetY;
					t.Triple.vertices[1].tx += TextureData.OffsetX;
					t.Triple.vertices[1].ty += TextureData.OffsetY;
					t.Triple.vertices[2].tx += TextureData.OffsetX;
					t.Triple.vertices[2].ty += TextureData.OffsetY;
				}
				else
				{
					t.Triple.vertices[0].tx = WithinBounds(TextureData.Texture.OriginalSize.x * TextureData.Scale, t.Vertices[0].x - _aabb.p1.x) + TextureData.OffsetX;
					t.Triple.vertices[0].ty = WithinBounds(TextureData.Texture.OriginalSize.y * TextureData.Scale, t.Vertices[0].y - _aabb.p1.y) + TextureData.OffsetY;
					t.Triple.vertices[1].tx = WithinBounds(TextureData.Texture.OriginalSize.x * TextureData.Scale, t.Vertices[1].x - _aabb.p1.x) + TextureData.OffsetX;
					t.Triple.vertices[1].ty = WithinBounds(TextureData.Texture.OriginalSize.y * TextureData.Scale, t.Vertices[1].y - _aabb.p1.y) + TextureData.OffsetY;
					t.Triple.vertices[2].tx = WithinBounds(TextureData.Texture.OriginalSize.x * TextureData.Scale, t.Vertices[2].x - _aabb.p1.x) + TextureData.OffsetX;
					t.Triple.vertices[2].ty = WithinBounds(TextureData.Texture.OriginalSize.y * TextureData.Scale, t.Vertices[2].y - _aabb.p1.y) + TextureData.OffsetY;
				}
			}
			
			t.Triple.vertices[0].x = t.Vertices[0].x;
			t.Triple.vertices[0].y = t.Vertices[0].y;
			t.Triple.vertices[1].x = t.Vertices[1].x;
			t.Triple.vertices[1].y = t.Vertices[1].y;
			t.Triple.vertices[2].x = t.Vertices[2].x;
			t.Triple.vertices[2].y = t.Vertices[2].y;
		}
	}

	public boolean TestPoint(DGEVector point)
	{
		if (IsPoint())
			return _aabb.TestPoint(point);
		else
			return TestGetTriangle(point) != null;
	}

	public void Render()
	{
		Render(DGEPolygonRenderer.Standard);
	}
	
	public void Render(int flags)
	{
		if (!IsPoint())
		{
			if (_borderQuads.size() < 0 && flags == DGEPolygonRenderer.Standard)
			{
				_dge.Gfx_StartBatch(DGE.Primitive.Triples, TextureData.BorderTexture.Texture, DGE.DGE_BLEND_ALPHABLEND + DGE.DGE_BLEND_COLORMUL + DGE.DGE_BLEND_NOZWRITE);
				
				for (int i = 0; i < _borderTriangles.size(); i++)
					_dge.Gfx_RenderTriple(_borderTriangles.get(i));
				
				_dge.Gfx_FinishBatch(_borderTriangles.size());
				
				_dge.Gfx_StartBatch(DGE.Primitive.Quads, TextureData.BorderTexture.Texture, DGE.DGE_BLEND_ALPHABLEND + DGE.DGE_BLEND_COLORMUL + DGE.DGE_BLEND_NOZWRITE);
				
				for (int i = 0; i < _borderQuads.size(); i++)
					_dge.Gfx_RenderQuad(_borderQuads.get(i));
				
				_dge.Gfx_FinishBatch(_borderQuads.size());
			}
			
			if (Triangles.size() > 0)
			{
				if (flags == DGEPolygonRenderer.Standard)
				{
					_dge.Gfx_StartBatch(DGE.Primitive.Triples, TextureData.Texture.Texture, TextureData.Blend);
					
					for (int i = 0; i < Triangles.size(); i++)
						_dge.Gfx_RenderTriple(Triangles.get(i).Triple);
					
					_dge.Gfx_FinishBatch(Triangles.size());
				}
				else if (flags == DGEPolygonRenderer.NormalMap && TextureData.NormalMapped)
				{
					_dge.Gfx_StartBatch(DGE.Primitive.Triples, TextureData.NormalMapTexture.Texture, TextureData.Blend);
					
					for (int i = 0; i < Triangles.size(); i++)
					{
						DGETriple triple = Triangles.get(i).Triple;
						
						triple.texture = TextureData.NormalMapTexture.Texture;
						_dge.Gfx_RenderTriple(triple);
						triple.texture = TextureData.Texture.Texture;
					}
					
					_dge.Gfx_FinishBatch(Triangles.size());
				}
			}
			
			if (_shadowQuads.size() > 0 && flags == DGEPolygonRenderer.Standard)
			{
				_dge.Gfx_StartBatch(DGE.Primitive.Quads, TextureData.ShadowTexture.Texture, DGE.DGE_BLEND_ALPHABLEND + DGE.DGE_BLEND_COLORMUL + DGE.DGE_BLEND_NOZWRITE);
				
	            for (int i = 0; i < _shadowQuads.size(); i++)
	                _dge.Gfx_RenderQuad(_shadowQuads.get(i));
	                
	            _dge.Gfx_FinishBatch(_shadowQuads.size());
			}
		}
		else
		{
			if (_sprite != null)
			{
				if (flags == DGEPolygonRenderer.Standard)
				{
					_sprite.Render(_aabb.p1.x, _aabb.p2.y);
				}
				else if (flags == DGEPolygonRenderer.NormalMap)
				{
					int texture = _sprite.GetTexture();
					
					_sprite.SetBlendMode(DGE.DGE_BLEND_ALPHABLEND + DGE.DGE_BLEND_COLORADD + DGE.DGE_BLEND_NOZWRITE);
					_sprite.SetTexture(0);
					_sprite.SetColor(0.3f, 0.3f, 0.3f, 1f);
					_sprite.Render(_aabb.p1.x, _aabb.p1.y);
					
					_sprite.SetTexture(texture);
					_sprite.SetColor(1f, 1f, 1f, 1f);
					_sprite.SetBlendMode(DGE.DGE_BLEND_ALPHABLEND + DGE.DGE_BLEND_COLORMUL + DGE.DGE_BLEND_NOZWRITE);
				}
			}
		}
	}
	
	public void RenderOutline()
	{
		if (IsPoint())
		{
			_dge.Gfx_RenderLine(_aabb.p1.x, _aabb.p1.y, _aabb.p2.x, _aabb.p1.y, 1f, 1f, 1f, 0.8f);
			_dge.Gfx_RenderLine(_aabb.p2.x, _aabb.p1.y, _aabb.p2.x, _aabb.p2.y, 1f, 1f, 1f, 0.8f);
			_dge.Gfx_RenderLine(_aabb.p1.x, _aabb.p2.y, _aabb.p2.x, _aabb.p2.y, 1f, 1f, 1f, 0.8f);
			_dge.Gfx_RenderLine(_aabb.p1.x, _aabb.p1.y, _aabb.p1.x, _aabb.p2.y, 1f, 1f, 1f, 0.8f);
		}
		else
		{
			for (int i = 0; i < VertexCount(); i++)
			{
				DGEPolygonVertex current = Vertices.get(i);
				DGEPolygonVertex next = (i == VertexCount() - 1) ? Vertices.get(0) : Vertices.get(i + 1);

				_dge.Gfx_RenderLine(current.Position.x, current.Position.y, next.Position.x, next.Position.y, 1f, 1f, 1f, 0.8f);
			}
		}
	}
	
	public void Load(DGEResourceReader file, int format)
	{
		if (file == null)
			return;
		
		int totalVertices = file.ReadInt();
		
		if (totalVertices > 0)
		{
			for (int i = 0; i < totalVertices; i++)
			{
				float px, py, norm;
				
				px = file.ReadFloat();
				py = file.ReadFloat();
				norm = file.ReadFloat();
				
				AddVertex(new DGEVector(px, py)).Normal = norm;
			}
		}
		
		int totalTriangles = file.ReadInt();
		
		if (totalTriangles > 0)
		{
			float []px = new float[3];
			float []py = new float[3];
			float []tx = new float[3];
			float []ty = new float[3];
			
			for (int i = 0; i < totalTriangles; i++)
			{
				for (int j = 0; j < 3; j++)
				{
					px[j] = file.ReadFloat();
					py[j] = file.ReadFloat();
					
					if (format >= 315)
					{
						tx[j] = file.ReadFloat();
						ty[j] = file.ReadFloat();
					}
				}
				
				DGETriangle t = new DGETriangle(new DGEVector(px[0], py[0]), new DGEVector(px[1], py[1]), new DGEVector(px[2], py[2]));
				
				if (format >= 315)
				{
					for (int j = 0; j < 3; j++)
					{
						t.Triple.vertices[j].tx = tx[j];
						t.Triple.vertices[j].ty = ty[j];
					}
				}
				
				AddTriangle(t);
			}
		}
		
		TextureData.Blend = file.ReadInt();
		TextureData.Color = file.ReadColor();
		TextureData.OffsetX = file.ReadFloat();
		TextureData.OffsetY = file.ReadFloat();
		TextureData.Tiled = file.ReadBool();
		TextureData.Scale = file.ReadFloat();
		
		if (format >= 315)
			TextureData.World = file.ReadBool();
		else
			TextureData.World = false;

		TextureData.Bordered = file.ReadBool();
		TextureData.BorderMax = file.ReadFloat();
		TextureData.BorderMin = file.ReadFloat();
		TextureData.BorderHeight = file.ReadFloat();
		TextureData.BorderScale = file.ReadFloat();

		TextureData.Shadowed = file.ReadBool();
		TextureData.ShadowFollowBorder = file.ReadBool();
		TextureData.ShadowColor = file.ReadColor();
		TextureData.ShadowHeight = file.ReadFloat();
		TextureData.ShadowScale = file.ReadFloat();

		String filename;
		filename = file.ReadString();
		
	    if (TextureData.Bordered) 
	    	TextureData.BorderTexture = GetTexture(filename);
	    
		filename = file.ReadString();
    	TextureData.Texture = GetTexture(filename);
	    
		filename = file.ReadString();
		
	    if (TextureData.Shadowed) 
	    	TextureData.ShadowTexture = GetTexture(filename);
	    
	    // set normal texture data
	    TextureData.NormalMapped = false;
	    TextureData.NormalMapTexture = null;
			
		EntityData.Load(file);
		
		CalculateAABB();
		CalculateTextureCoordinates((format < 315));
		GenerateBorder();
		
		if (IsPoint())
		{
			String sprite = EntityData.GetString("sprite");
			
			if (sprite.length() == 0)
				return;
			
			int texture = _map.GetTexture(sprite);
			
			if (texture != 0)
			{
				_sprite = new DGESprite(texture, 0, 0, _dge.Texture_GetWidth(texture), _dge.Texture_GetHeight(texture));
				
				float hotspotX = EntityData.GetFloat("hotspot_x");
				float hotspotY = EntityData.GetFloat("hotspot_y");
				
				if (hotspotX != 0 || hotspotY != 0)
					_hotspot = new DGEVector(hotspotX, hotspotY);
				
				CalculateAABB();
			}
		}
	}
	
	protected void GenerateMesh()
	{
		ClearTriangles();
		
		int c = VertexCount();
		int runCount = 0;
		
		DGEPolygon currentPolygon = new DGEPolygon();
		DGEPolygonVertex currentVertex;
		
		if (c > 3)
		{
			for (int i = 0; i < VertexCount(); i++)
				currentPolygon.AddVertex(Vertices.get(i).Position);
			
			currentVertex = currentPolygon.Vertices.get(0);
			
			while (currentVertex != null)
			{
				runCount++;
				
				if (currentPolygon.VertexCount() > 3)
				{
					boolean mfail = false;
					DGEPolygonVertex s, m ,f;
					
					s = currentVertex;
					m = s.Next;
					
					if (m == null)
					{
						m = currentPolygon.Vertices.get(0);
						mfail = true;
					}
					
					f = m.Next;
					
					if (f == null)
					{
						if (mfail)
							break;
						
						f = currentPolygon.Vertices.get(0);
					}
					
					boolean bokay = true;
					float ca = new DGEVector(f.Position.subtract(s.Position)).angle() + DGE.M_PI - (new DGEVector(m.Position.subtract(s.Position)).angle() + DGE.M_PI);
					
					if ((ca < 0 && ca > -DGE.M_PI) || ca > DGE.M_PI)
					{
						currentVertex = m;
						bokay = false;
					}
					
					DGELine line = new DGELine(s.Position, f.Position);
					
					if (bokay && !Intersects(line).Collides && !currentPolygon.Intersects(line).Collides)
					{
						AddTriangle(new DGETriangle(s.Position, m.Position, f.Position));
						currentPolygon.RemoveVertex(m);
						currentVertex = f;
					}
					else
					{
						currentVertex = m;
					}
				}
				else if (currentPolygon.VertexCount() == 3)
				{
					AddTriangle(new DGETriangle(currentPolygon.Vertices.get(0).Position, currentPolygon.Vertices.get(1).Position, currentPolygon.Vertices.get(2).Position));
					currentPolygon.ClearVertices();
				}
				else
				{
					break;
				}
				
				if (runCount > c * c)
				{
					currentVertex = null;
					break;
				}
			}
		}
		else if (c == 3)
		{
			AddTriangle(new DGETriangle(Vertices.get(0).Position, Vertices.get(1).Position, Vertices.get(2).Position));
			currentPolygon.ClearVertices();
		}
		
		CalculateAABB();
		CalculateNormals();
		CalculateTextureCoordinates(true);
		GenerateBorder();
	}
	
	public void SetAABBSize(DGEVector size)
	{
		_aabb.p2.x = _aabb.p1.x + size.x;
		_aabb.p2.y = _aabb.p1.y + size.y;
		
		_center = new DGEVector(_aabb.p1.x + (size.x / 2), _aabb.p1.y + (size.y / 2));
	}
	
	public void SetAABB(DGEVector position, DGEVector size)
	{
		_aabb.p1.x = position.x;
		_aabb.p1.y = position.y;
		
		SetAABBSize(size);
	}
	
	public DGERect GetAABB() { return _aabb; }
	
	public void SetPosition(DGEVector position)
	{
		Shift(position.subtract(_aabb.p1.add(_hotspot)));
	}
	
	public DGEVector GetPosition()
	{
		return _aabb.p1.add(_hotspot);
	}
	
	public DGESprite GetSprite() { return _sprite; }
	public DGEVector GetCenter() { return _center; }
	
	public DGEVector GetHotSpot() { return _hotspot; }
	public void SetHotSpot(DGEVector hotspot) { _hotspot = hotspot; }
	
	public boolean IsPoint() { return Vertices.size() <= 1; }
	public boolean IsPolygon() { return !IsPoint(); }
	public boolean IsEntity() { return _isEntity; }
	
    
    public DGEIntersect Intersects(DGELine line)
    {
    	return Intersects(line, new DGEVector(0, 0));
    }
    
    public DGEIntersect Intersects(DGELine line, DGEVector offset)
    {
    	DGERect trect = line.Rect();
    	
    	trect.p1.x += offset.x; trect.p1.y += offset.y; 
    	trect.p2.x += offset.x; trect.p2.y += offset.y;
    	
    	if (!trect.Intersect(_aabb))
    		return new DGEIntersect();
    	
    	if (IsPoint())
    	{
    		DGEIntersect ret;
    		
            ret = new DGELine(_aabb.p1.x - offset.x, _aabb.p1.y - offset.y, _aabb.p2.x - offset.x, _aabb.p1.y - offset.y ).Intersects(line);
            
            if (ret.Collides) 
            	return ret;

            ret = new DGELine(_aabb.p2.x - offset.x, _aabb.p1.y - offset.y, _aabb.p2.x - offset.x, _aabb.p2.y - offset.y ).Intersects(line);
            
            if (ret.Collides) 
            	return ret;

            ret = new DGELine(_aabb.p1.x - offset.x, _aabb.p2.y - offset.y, _aabb.p2.x - offset.x, _aabb.p2.y - offset.y ).Intersects(line);
            
            if (ret.Collides) 
            	return ret;

            ret = new DGELine(_aabb.p1.x - offset.x, _aabb.p1.y - offset.y, _aabb.p1.x - offset.x, _aabb.p2.y - offset.y ).Intersects(line);
            
            if (ret.Collides) 
            	return ret;

    		// lines failed - last ditch attempt is to test line ends
    		// this is bad because we don't get any surface data returned
    		ret.Collides = (_aabb.TestPoint(line.Point1) || _aabb.TestPoint(line.Point2));

            return ret;
    	}
    	
    	DGEIntersect fin = new DGEIntersect();
    	
    	for (int i = 0; i < VertexCount(); i++)
    	{
    		DGEPolygonVertex current = Vertices.get(i);
    		DGEPolygonVertex next = (i == VertexCount() - 1) ? Vertices.get(0) : Vertices.get(i + 1);
    		
    		DGEIntersect ret = new DGELine(current.Position.x - offset.x, current.Position.y - offset.y, next.Position.x - offset.x, next.Position.y - offset.y).Intersects(line);
    		
    		if (ret.Collides)
    		{
    			fin.Points.add(ret.Points.get(0));
    			fin.Normals.add(ret.Normals.get(0));
    			fin.Collides = true;
    		}
    	}
    	
    	return fin;
    }
    
    public DGEIntersect Intersects(DGERectangle rectangle)
    {
    	return Intersects(rectangle, new DGEVector(0, 0));
    }
    
    public DGEIntersect Intersects(DGERectangle rectangle, DGEVector offset)
    {
    	DGERect trect = rectangle.Rect();
    	trect.p1.x += offset.x; trect.p1.y += offset.y;
    	trect.p2.x += offset.x; trect.p2.y += offset.y;
    	
    	if (!trect.Intersect(_aabb))
    		return new DGEIntersect();
    	
    	if (IsPoint())
    	{
    		DGEIntersect ret = new DGEIntersect();
    		
    		if (trect.Intersect(_aabb))
    			ret.Collides = true;
    		
    		return ret;
    	}
    	else
    	{
        	DGEIntersect fin = new DGEIntersect();
        	
    		for (int i = 0; i < VertexCount(); i++)
    		{
    			DGEPolygonVertex current = Vertices.get(i); 
    			DGEPolygonVertex next = (i == VertexCount() - 1) ? Vertices.get(0) : Vertices.get(i + 1);
    			
    			DGELine edge = new DGELine(current.Position.subtract(offset), next.Position.subtract(offset));
    			DGELine aabbLine;
    			
    			DGEIntersect ret;
    			
    			aabbLine = new DGELine(rectangle.Point1.x, rectangle.Point1.y, rectangle.Point2.x, rectangle.Point1.y);
    			ret = edge.Intersects(aabbLine);
    			
    			if (!ret.Collides)
    			{
        			aabbLine = new DGELine(rectangle.Point2.x, rectangle.Point1.y, rectangle.Point2.x, rectangle.Point2.y);
        			ret = edge.Intersects(aabbLine);
    			}
    			
    			if (!ret.Collides)
    			{
        			aabbLine = new DGELine(rectangle.Point1.x, rectangle.Point2.y, rectangle.Point2.x, rectangle.Point2.y);
        			ret = edge.Intersects(aabbLine);
    			}
    			
    			if (!ret.Collides)
    			{
        			aabbLine = new DGELine(rectangle.Point1.x, rectangle.Point1.y, rectangle.Point1.x, rectangle.Point2.y);
        			ret = edge.Intersects(aabbLine);
    			}
    			
    			if (ret.Collides)
    			{
    				fin.Points.add(ret.Points.get(0));
    				fin.Normals.add(ret.Normals.get(0));
    				fin.Collides = true;
    			}
    		}
        	
        	return fin;
    	}
    }
    
    public DGEIntersect Intersects(DGECircle circle)
    {
    	return Intersects(circle, new DGEVector(0, 0));
    }
    
    public DGEIntersect Intersects(DGECircle circle, DGEVector offset)
    {
    	DGERect trect = circle.Rect();
    	trect.p1.x += offset.x; trect.p1.y += offset.y;
    	trect.p2.x += offset.x; trect.p2.y += offset.y;
    	
    	if (!trect.Intersect(_aabb))
    		return new DGEIntersect();
    	
    	if (IsPoint())
    	{
    		DGEIntersect ret;
    		
    		ret = new DGELine(_aabb.p1.x - offset.x, _aabb.p1.y - offset.y, _aabb.p2.x - offset.x, _aabb.p1.y - offset.y).Intersects(circle);
    		
    		if (ret.Collides)
    			return ret;
    		
    		ret = new DGELine(_aabb.p2.x - offset.x, _aabb.p1.y - offset.y, _aabb.p2.x - offset.x, _aabb.p2.y - offset.y).Intersects(circle);
    		
    		if (ret.Collides)
    			return ret;
    		
    		ret = new DGELine(_aabb.p2.x - offset.x, _aabb.p2.y - offset.y, _aabb.p1.x - offset.x, _aabb.p2.y - offset.y).Intersects(circle);
    		
    		if (ret.Collides)
    			return ret;
    		
    		return new DGELine(_aabb.p1.x - offset.x, _aabb.p2.y - offset.y, _aabb.p1.x - offset.x, _aabb.p1.y - offset.y).Intersects(circle);
    	}
    	else
    	{
    		DGEIntersect fin = new DGEIntersect();
    		
    		for (int i = 0; i < VertexCount(); i++)
    		{
    			DGEPolygonVertex current = Vertices.get(i);
    			DGEPolygonVertex next = (i == VertexCount() - 1) ? Vertices.get(0) : Vertices.get(i + 1);
    			
    			DGEIntersect ret = new DGELine(current.Position.subtract(offset), next.Position.subtract(offset)).Intersects(circle);
    			
    			if (ret.Collides)
    			{
    				fin.Points.add(ret.Points.get(0));
    				fin.Normals.add(ret.Normals.get(0));
    				fin.Collides = true;
    			}
    		}
    	
    		if (!fin.Collides)
    		{
    			DGELine tline = new DGELine(circle.Center, new DGEVector(_aabb.p1.x - 100000, _aabb.p1.y - 100000));
    			DGEIntersect linetest = Intersects(tline, offset);
    			
    			if (linetest.Normals.size() % 2 == 1)
    			{
    				fin.Collides = true;
    				
    				DGECircle testCircle = new DGECircle(circle);
    				testCircle.Radius += testCircle.Radius / 2f;
    				
    				fin = Intersects(testCircle);
    			}
    		}
    		
    		return fin;
    	}
    }
    
    public DGEIntersect Intersects(DGEPolygon polygon)
    {
    	return Intersects(polygon, new DGEVector(0, 0));
    }
    
    public DGEIntersect Intersects(DGEPolygon polygon, DGEVector offset)
    {
    	DGERect trect = polygon.GetAABB();
    	trect.p1.x += offset.x; trect.p2.x += offset.x;
    	trect.p1.y += offset.y; trect.p2.y += offset.y;
    	
    	if (!trect.Intersect(_aabb))
    		return new DGEIntersect();
    	
    	if (IsPoint() && polygon.IsPoint())
    	{
    		DGEIntersect ret = new DGEIntersect();
    		ret.Collides = polygon.GetAABB().Intersect(_aabb);
    		return ret;
    	}
    	else if (IsPoint())
    	{
    		DGEIntersect ret;
    		
            ret = polygon.Intersects(new DGELine(_aabb.p1.x - offset.x, _aabb.p1.y - offset.y, _aabb.p2.x - offset.x, _aabb.p1.y - offset.y));
            
            if (ret.Collides) 
            	return ret;

            ret = polygon.Intersects(new DGELine(_aabb.p2.x - offset.x, _aabb.p1.y - offset.y, _aabb.p2.x - offset.x, _aabb.p2.y - offset.y ));
            
            if (ret.Collides) 
            	return ret;

            ret = polygon.Intersects(new DGELine(_aabb.p1.x - offset.x, _aabb.p2.y - offset.y, _aabb.p2.x - offset.x, _aabb.p2.y - offset.y ));
            
            if (ret.Collides) 
            	return ret;

        	return polygon.Intersects(new DGELine(_aabb.p1.x - offset.x, _aabb.p1.y - offset.y, _aabb.p1.x - offset.x, _aabb.p2.y - offset.y ));
    	}
    	else if (polygon.IsPoint())
    	{
    		return polygon.Intersects(this, offset.multiply(-1));
    	}
    	
    	DGEIntersect fin = new DGEIntersect();
    	
    	for (int i = 0; i < polygon.VertexCount(); i++)
    	{
    		DGEPolygonVertex current = polygon.Vertices.get(i);
    		DGEPolygonVertex next = (i == polygon.VertexCount() - 1) ? polygon.Vertices.get(0) : polygon.Vertices.get(i + 1);
    		
            DGEIntersect ret = Intersects(new DGELine(current.Position.add(offset),
            										  next.Position.add(offset)));

            if (ret.Collides)
            {
            	fin.Points.add(ret.Points.get(0));
            	fin.Normals.add(ret.Normals.get(0));
            	fin.Collides = true;
            }
    	}
    	
    	return fin;
    }
    
    public DGEIntersect Intersects(DGEEntityEx entity)
    {
    	return Intersects(entity, new DGEVector(0, 0));
    }
    
    public DGEIntersect Intersects(DGEEntityEx entity, DGEVector offset)
    {
    	return Intersects(entity, offset, 0);
    }
    
    public DGEIntersect Intersects(DGEEntityEx entity, DGEVector offset, float rotation)
    {
    	if (IsPoint() && entity.IsPoint())
    	{
    		DGEIntersect ret = new DGEIntersect();
    		ret.Collides = entity.GetAABB().Intersect(_aabb);
    		return ret;
    	}
    	else if (IsPoint())
    	{
    		DGEIntersect ret;
    		
            ret = entity.Intersects(new DGELine(_aabb.p1.x - offset.x, _aabb.p1.y - offset.y, _aabb.p2.x - offset.x, _aabb.p1.y - offset.y));
            
            if (ret.Collides) 
            	return ret;

            ret = entity.Intersects(new DGELine(_aabb.p2.x - offset.x, _aabb.p1.y - offset.y, _aabb.p2.x - offset.x, _aabb.p2.y - offset.y ));
            
            if (ret.Collides) 
            	return ret;

            ret = entity.Intersects(new DGELine(_aabb.p1.x - offset.x, _aabb.p2.y - offset.y, _aabb.p2.x - offset.x, _aabb.p2.y - offset.y ));
            
            if (ret.Collides) 
            	return ret;

        	return entity.Intersects(new DGELine(_aabb.p1.x - offset.x, _aabb.p1.y - offset.y, _aabb.p1.x - offset.x, _aabb.p2.y - offset.y ));
    	}
    	else if (entity.IsPoint())
    	{
    		return entity.Intersects(this, offset.multiply(-1));
    	}
    	
    	DGEIntersect fin = new DGEIntersect();
    	
    	for (int i = 0; i < entity.VertexCount(); i++)
    	{
    		DGEPolygonVertex current = entity.Vertices.get(i);
    		DGEPolygonVertex next = (i == entity.VertexCount() - 1) ? entity.Vertices.get(0) : entity.Vertices.get(i + 1);
    		
            DGEIntersect ret = Intersects(new DGELine(entity.GetRotatedPosition(current.Position, rotation).add(offset),
            										  entity.GetRotatedPosition(next.Position, rotation).add(offset)));

            if (ret.Collides)
            {
            	fin.Points.add(ret.Points.get(0));
            	fin.Normals.add(ret.Normals.get(0));
            	fin.Collides = true;
            }
    	}
    	
    	return fin;
    }
}
