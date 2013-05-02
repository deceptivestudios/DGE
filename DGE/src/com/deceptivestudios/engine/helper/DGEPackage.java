package com.deceptivestudios.engine.helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.content.res.AssetManager;
import android.os.Environment;

import com.deceptivestudios.engine.DGE;
import com.deceptivestudios.engine.internal.DGEResource;

public class DGEPackage 
{
	private class DGEResourceInfo
	{
		public byte[] filename;
		public int offset;
		public int length;
	}
	
	private String _path, _password;
	private AssetManager _manager;
	private Vector<DGEResourceInfo> _header;
	private HashMap<String, DGEResource> _resources;
	
	public DGEPackage(String path, AssetManager manager)
	{
		this(path, manager, null);
	}
	
	public DGEPackage(String path, AssetManager manager, String password)
	{
		_path = path;
		_manager = manager;
		_password = password;
	}
	
	public boolean Contains(String filename)
	{
		if (_header == null)
			Reload();
		
		try 
		{
			for (DGEResourceInfo info : _header)
			{
				String resourceFilename = new String(info.filename, "UTF8");
			
				if (resourceFilename.equals(filename))
					return true;
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public DGEResource Retrieve(String filename)
	{
		// load the file from the detail and extract it 
		try 
		{
			for (DGEResourceInfo info : _header)
			{
				String resourceFilename = new String(info.filename, "UTF8");
				
				if (resourceFilename.equals(filename))
				{
					DGEResource resource = new DGEResource();
					
					resource.resource = filename;
					resource.data = RetrieveResource(info.offset, info.length);
					
					return resource;
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private byte[] RetrieveResource(int offset, int length) 
	{
		try 
		{
			InputStream stream = _manager.open(_path);
			DataInputStream ois = new DataInputStream(stream);
			byte[] buffer = new byte[length];
			
			ois.skipBytes(offset);
			ois.read(buffer);
			
			ois.close();
			
			return Decrypt(buffer);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}

	public boolean Insert(String filename, byte[] data)
	{
		if (_resources == null)
			_resources = new HashMap<String, DGEResource>();
		
		if (_resources.containsKey(filename))
			_resources.remove(filename);

		DGEResource resource = new DGEResource();
		
		resource.resource = filename;
		resource.data = data;
		
		_resources.put(filename, resource);
		
		return false;
	}
	
	public boolean Reload()
	{
		_header = new Vector<DGEResourceInfo>();
		
		DGE dge = DGE.Interface(DGE.DGE_VERSION);
		
		try
		{
			dge.System_Log("Loading package %s...", _path);
			
			InputStream stream = _manager.open(_path);
			DataInputStream ois = new DataInputStream(stream);

			dge.System_Log(" - reading header");
			
			// read the header detail
			// integer total items
			int totalFiles = ReadInt(ois);
	
			dge.System_Log("    - %d total files", totalFiles);
			
			// looped
			for (int i = 0; i < totalFiles; i++)
			{
				DGEResourceInfo info = new DGEResourceInfo();
				
				// integer filename length
				int filenameLength = ReadInt(ois);

				// byte[] filename
				byte[] buffer = new byte[filenameLength];
				ois.readFully(buffer, 0, filenameLength);
				
				// decrypt the filename
				info.filename = Decrypt(buffer);
				// integer data offset
				info.offset = ReadInt(ois);
				// integer data size
				info.length = ReadInt(ois);

				dge.System_Log("    - filename %s (data at %d, filesize %d)", new String(info.filename, "UTF8"), info.offset, info.length);
				
				_header.add(info);
			}
			
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean Build()
	{
		int offset = 0;
		int headerSize = 4;
		int totalFiles = 0;
		
		DGE dge = DGE.Interface(DGE.DGE_VERSION);
		
		// output to the folder with assets.pak added
		String outputFolder = String.format("%s/data/DGE", Environment.getExternalStorageDirectory().getAbsolutePath());
		String outputPath = String.format("%s/%s", outputFolder, _path);
		
		try
		{
			dge.System_Log("Creating package %s...", _path);
			dge.System_Log(" - building header");
			
			Vector<DGEResourceInfo> header = new Vector<DGEResourceInfo>();
			HashMap<DGEResourceInfo, DGEResource> resources = new HashMap<DGEResourceInfo, DGEResource>();
			
			for (Entry<String, DGEResource> data : _resources.entrySet())
			{
				DGEResource resource = new DGEResource();
				
				resource.resource = data.getKey();
				resource.data = Encrypt(data.getValue().data);
				
				if (resource.data == null)
					return false;
					
				DGEResourceInfo info = new DGEResourceInfo();
				
				info.filename = Encrypt(resource.resource.getBytes("UTF8"));
				info.offset = offset;
				info.length = resource.data.length;
				
				dge.System_Log("    - %s (%d bytes)", resource.resource, info.length);
				
				offset += info.length;
				
				header.add(info);
				resources.put(info, resource);
				
				totalFiles++;
				
				headerSize += 12; // three integers at 4-bytes each
				headerSize += info.filename.length;
			}
			
			dge.System_Log("    - total header size %d bytes", headerSize);
			dge.System_Log(" - creating file");
			
			File dir = new File(outputFolder);
			dir.mkdirs();
			
			File file = new File(outputPath);
			FileOutputStream out = new FileOutputStream(file);
			DataOutputStream dos = new DataOutputStream(out);

			dge.System_Log("    - %d total files", totalFiles);
			
			WriteInt(totalFiles, dos);
			
			for (DGEResourceInfo info : header)
			{
				WriteInt(info.filename.length, dos);
				dos.write(info.filename, 0, info.filename.length);
				WriteInt(info.offset + headerSize, dos);
				WriteInt(info.length, dos);
			}
			
			for (DGEResourceInfo info : header)
			{
				DGEResource resource = resources.get(info);
				
				if (resource == null)
					return false;
				
				dos.write(resource.data);
			}
			
			dos.flush();
			dos.close();
			
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	private byte[] Encrypt(byte[] data)
	{
		if (_password == null)
			return data;

		try 
		{
			DESKeySpec keySpec = new DESKeySpec(_password.getBytes("UTF8"));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey key = keyFactory.generateSecret(keySpec);
			
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			
			return cipher.doFinal(data);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private byte[] Decrypt(byte[] data)
	{
		if (_password == null)
			return data;

		try 
		{
			DESKeySpec keySpec = new DESKeySpec(_password.getBytes("UTF8"));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey key = keyFactory.generateSecret(keySpec);
			
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			
			return cipher.doFinal(data);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void WriteInt(int value, DataOutputStream out) throws IOException
	{
		byte[] data = new byte[4];
		
		data[0] = (byte) (value & 0xFF);
		data[1] = (byte) (value >> 8 & 0xFF);
		data[2] = (byte) (value >> 16 & 0xFF);
		data[3] = (byte) (value >> 24);
		
		out.write(data, 0, 4);
	}
	
	private int ReadInt(DataInputStream in) throws IOException
	{
		byte[] data = new byte[4];
		
		in.readFully(data, 0, 4);
		
		return (data[3]) << 24 | (data[2] & 0xFF) << 16 | (data[1] & 0xFF) << 8 | (data[0] & 0xFF);
	}
}
