package com.deceptivestudios.engine.helper;

import android.graphics.Color;

public class DGEColor 
{
	public float r, g, b, a;
	private float[] _hsl;
	
	public DGEColor()
	{
		set(0, 0, 0, 0);
	}

	public DGEColor(DGEColor color)
	{
		set(color);
	}
	
	public DGEColor(DGEColor color, float a)
	{
		set(color, a);
	}
	
	public DGEColor(float r, float g, float b)
	{
		set(r, g, b, 1);
	}
	
	public DGEColor(float r, float g, float b, float a)
	{
		set(r, g, b, a);
	}
	
	public DGEColor set(DGEColor color)
	{
		return set(color.r, color.g, color.b, color.a);
	}
	
	public DGEColor set(DGEColor color, float a)
	{
		return set(color.r, color.g, color.b, a);
	}
	
	public DGEColor set(float r, float g, float b)
	{
		return set(r, g, b, 1);
	}
	
	public DGEColor set(float r, float g, float b, float a)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		
		return this;
	}
	
	public DGEColor set(String color)
	{
		String sa = color.substring(0, 2);
		String sr = color.substring(2, 4);
		String sg = color.substring(4, 6);
		String sb = color.substring(6, 8);
		
		float a = ParseChannel(sa);
		float r = ParseChannel(sr);
		float g = ParseChannel(sg);
		float b = ParseChannel(sb);
		
		return set(r, g, b, a);
	}
	
	public void clamp()
	{
		r = clamp(r);
		g = clamp(g);
		b = clamp(b);
		a = clamp(a);
	}
	
	private float clamp(float color)
	{
		return Math.max(0, Math.min(1, color));
	}
	
	public int getARGB()
	{
		clamp();
		
		int red = (int) (r * 255);
		int green = (int) (g * 255);
		int blue = (int) (b * 255);
		int alpha = (int) (a * 255);
		
		return Color.argb(alpha, red, green, blue);
	}
	
	public DGEColor addLocal(DGEColor color)
	{
		this.r += color.r; 
		this.g += color.g; 
		this.b += color.b;
		this.a += color.a;
		
		return this;
	}
	
	public DGEColor add(DGEColor color)
	{
		DGEColor target = new DGEColor(this);
		target.addLocal(color);
		return target;
	}
	
	public DGEColor addLocal(float scalar)
	{
		this.r += scalar; 
		this.g += scalar; 
		this.b += scalar;
		this.a += scalar;
		
		return this;
	}
	
	public DGEColor add(float scalar)
	{
		DGEColor target = new DGEColor(this);
		return target.addLocal(scalar);
	}
	
	public DGEColor subLocal(DGEColor color)
	{
		this.r -= color.r; 
		this.g -= color.g; 
		this.b -= color.b;
		this.a -= color.a;
		
		return this;
	}
	
	public DGEColor sub(DGEColor color)
	{
		DGEColor target = new DGEColor(this);
		target.subLocal(color);
		return target;
	}
	
	public DGEColor subLocal(float scalar)
	{
		addLocal(-scalar);
		
		return this;
	}
	
	public DGEColor sub(float scalar)
	{
		return add(-scalar);
	}
	
	public DGEColor mulLocal(DGEColor color)
	{
		this.r *= color.r; 
		this.g *= color.g; 
		this.b *= color.b;
		this.a *= color.a;
		
		return this;
	}
	
	public DGEColor mul(DGEColor color)
	{
		DGEColor target = new DGEColor(this);
		return target.mulLocal(color);
	}

	public DGEColor mulLocal(float scalar)
	{
		this.r *= scalar; 
		this.g *= scalar; 
		this.b *= scalar;
		this.a *= scalar;
		
		return this;
	}
	
	public DGEColor mul(float scalar)
	{
		DGEColor target = new DGEColor(this);
		return target.mulLocal(scalar);
	}
	
	public DGEColor divLocal(DGEColor color)
	{
		this.r /= color.r; 
		this.g /= color.g; 
		this.b /= color.b;
		this.a /= color.a;
		
		return this;
	}
	
	public DGEColor div(DGEColor color)
	{
		DGEColor target = new DGEColor(this);
		return target.divLocal(color);
	}

	public DGEColor divLocal(float scalar)
	{
		this.r /= scalar; 
		this.g /= scalar; 
		this.b /= scalar;
		this.a /= scalar;
		
		return this;
	}
	
	public DGEColor div(float scalar)
	{
		DGEColor target = new DGEColor(this);
		return target.divLocal(scalar);
	}

	public boolean equal(DGEColor other)
	{
		return r == other.r && g == other.g && b == other.b && a == other.a;
	}

	public boolean notequal(DGEColor other)
	{
		return !equal(other);
	}
	
	public static float ParseChannel(String channel)
	{
		if (channel.length() > 2)
			return 0;
		else
			return ((float) Integer.parseInt(channel, 16)) / 255f;
	}
	
	public static DGEColor ParseARGB(int color)
	{
		return ParseARGB(String.format("%08X", (0xFFFFFFFF & color)));
	}
	
	public static DGEColor ParseARGB(String color) 
	{
		String sa = color.substring(0, 2);
		String sr = color.substring(2, 4);
		String sg = color.substring(4, 6);
		String sb = color.substring(6, 8);
		
		float a = ParseChannel(sa);
		float r = ParseChannel(sr);
		float g = ParseChannel(sg);
		float b = ParseChannel(sb);
		
		return new DGEColor(r, g, b, a);
	}
	
	public static DGEColor ParseRGBA(int color)
	{
		return ParseRGBA(String.format("%08X", (0xFFFFFFFF & color)));
	}
	
	public static DGEColor ParseRGBA(String color) 
	{
		String sr = color.substring(0, 2);
		String sg = color.substring(2, 4);
		String sb = color.substring(4, 6);
		String sa = color.substring(6, 8);
		
		float r = ParseChannel(sr);
		float g = ParseChannel(sg);
		float b = ParseChannel(sb);
		float a = ParseChannel(sa);
		
		return new DGEColor(r, g, b, a);
	}
	
	public static DGEColor ParseRGB(int color)
	{
		return ParseRGB(String.format("%06X", (0xFFFFFF & color)));
	}
	
	public static DGEColor ParseRGB(String color) 
	{
		String sr = color.substring(0, 2);
		String sg = color.substring(2, 4);
		String sb = color.substring(4, 6);
		
		float r = ParseChannel(sr);
		float g = ParseChannel(sg);
		float b = ParseChannel(sb);
		
		return new DGEColor(r, g, b, 1);
	}
}
