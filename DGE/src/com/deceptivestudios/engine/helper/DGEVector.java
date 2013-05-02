package com.deceptivestudios.engine.helper;
import android.util.FloatMath;

public class DGEVector 
{
	public float x, y;
	
	public DGEVector() { this(0, 0); }
	public DGEVector(float x, float y) { this.x = x; this.y = y; }
	public DGEVector(DGEVector clone) { this.x = clone.x; this.y = clone.y; }
	
	public DGEVector flip() { return new DGEVector(-x, -y); }
	
	public DGEVector add(DGEVector other) { return new DGEVector(x + other.x, y + other.y); }
	public DGEVector add(float scalar) { return new DGEVector(x + scalar, y + scalar); }
	public DGEVector subtract(DGEVector other) { return new DGEVector(x - other.x, y - other.y); }
	public DGEVector subtract(float scalar) { return new DGEVector(x - scalar, y - scalar); }
	public DGEVector divide(DGEVector other) { return new DGEVector(x / other.x, y / other.y); }
	public DGEVector divide(float scalar) { return new DGEVector(x / scalar, y / scalar); }
	public DGEVector multiply(DGEVector other) { return new DGEVector(x * other.x, y * other.y); }
	public DGEVector multiply(float scalar) { return new DGEVector(x * scalar, y * scalar); }
	
	public void addLocal(DGEVector other) { x += other.x; y += other.y; }
	public void addLocal(float scalar) { x += scalar; y += scalar; }
	public void subtractLocal(DGEVector other) { x -= other.x; y -= other.y; }
	public void subtractLocal(float scalar) { x -= scalar; y -= scalar; }
	public void divideLocal(DGEVector other) { x /= other.x; y /= other.y; }
	public void divideLocal(float scalar) { x /= scalar; y /= scalar; }
	public void multiplyLocal(DGEVector other) { x *= other.x; y *= other.y; }
	public void multiplyLocal(float scalar) { x *= scalar; y *= scalar; }
	
	public boolean equal(DGEVector other) { return (x == other.x && y == other.y); }
	public boolean notequal(DGEVector other) { return !equal(other); }
	
	public void set(DGEVector other) { x = other.x; y = other.y; }
	public void set(float x, float y) { this.x = x; this.y = y; }
	
	public float dot(DGEVector other) { return x * other.x + y * other.y; }
	public float length() { return (float) Math.sqrt(dot(this)); }
	
	public float angle()
	{
		return angle(null);
	}
	
	public float angle(DGEVector other)
	{
		if (other != null)
		{
			this.normalize();
			other.normalize();
			
			return (float) Math.acos(this.dot(other));
		}
		else
		{
			return (float) Math.atan2(y, x);
		}
	}
	
	public void rotate(float a)
	{
		float x = this.x * FloatMath.cos(a) - this.y * FloatMath.sin(a);
		float y = this.x * FloatMath.sin(a) + this.y * FloatMath.cos(a);
		
		this.x = x;
		this.y = y;
	}
	
	public void normalize()
	{
		float rc = invsqrt(dot(this));
		
		this.x *= rc;
		this.y *= rc;
	}
	
	private float invsqrt(float x)
	{
		float xhalf = 0.5f * x;
		int i = Float.floatToIntBits(x); // get bits for floating value
		
		i = 0x5f375a86 - (i >> 1); // gives initial guess y0
		x = Float.intBitsToFloat(i); // convert bits back to float
		x = x * (1.5f - xhalf * x * x); // Newton step, repeating increases accuracy
		
		return x;	
	}
	
	public float distance(DGEVector other)
	{
		return distance(this, other);
	}
	
	public static float distance(DGEVector one, DGEVector two)
	{
		float w = one.x - two.x;
		float h = one.y - two.y;
		float py = w * w + h * h;
		
		return FloatMath.sqrt(py);
	}
	
	public void lerp(DGEVector other, float percent)
	{
		DGEVector lerped = lerp(this, other, percent);
		x = lerped.x; y = lerped.y;
	}
	
	public static DGEVector lerp(DGEVector one, DGEVector two, float percent)
	{
		float x = one.x + (two.x - one.x) * percent;
		float y = one.y + (two.y - one.y) * percent;
		
		return new DGEVector(x, y);
	}
}
