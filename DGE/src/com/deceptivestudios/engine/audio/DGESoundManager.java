package com.deceptivestudios.engine.audio;

import com.deceptivestudios.engine.DGE;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

import java.util.Vector;

public class DGESoundManager implements OnLoadCompleteListener, OnPreparedListener
{
	private DGE _dge;
	private SoundPool _soundPool;
	private AssetManager _assets;
	private AudioManager _audioManager;
	
	private Vector<DGEEffect> _effects;
	private Vector<DGEMusic> _music;
	private Vector<DGEChannel> _channels;
	
	private int _channelId, _musicId;
	
	public DGESoundManager(AssetManager assets, AudioManager manager)
	{
		_dge = DGE.Interface(DGE.DGE_VERSION);
		
		_assets = assets;
		_audioManager = manager;
		
		_audioManager.getMode();
		
		_soundPool = new SoundPool((Integer) _dge.System_GetState(DGE.DGE_SOUNDCHANNELS), AudioManager.STREAM_MUSIC, 0);
		_soundPool.setOnLoadCompleteListener(this);
		
		_effects = new Vector<DGEEffect>();
		_music = new Vector<DGEMusic>();
		_channels = new Vector<DGEChannel>();
		
		_dge.System_Log("Sound: SoundManager initialised\n");
		
		_channelId = 1;
		_musicId = 1;
	}
	
	public int Effect_Load(String filename)
	{
		_dge.System_Log("SoundManager: Effect_Load '%s'", filename);
		
		DGEEffect effect = Effect_Get(filename);
		
		if (effect != null)
			return effect.SoundID;
		
		try 
		{
			effect = new DGEEffect();

			effect.Filename = filename;
			effect.Ready = false;
			effect.Play = null;
			
			effect.SoundID = _soundPool.load(_assets.openFd(filename), 1);
			_dge.System_Log(" - assigned sound ID %d", effect.SoundID);
			
			_effects.add(effect);
			
			return effect.SoundID;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public void Effect_Free(int soundId)
	{
		DGEEffect sound = Effect_Get(soundId);
		
		if (sound != null)
		{
			_effects.remove(sound);
			_soundPool.unload(sound.SoundID);
		}
	}
	
	public void Effect_FreeAll()
	{
		for (DGEEffect sound : _effects)
		{
			sound.Ready = false;
			_soundPool.unload(sound.SoundID);
		}
		
		_effects.clear();
	}

	public int Effect_Play(int soundId, float volume, float panning, float pitch, int priority, boolean loop)
	{
		DGEEffect sound = Effect_Get(soundId);

		if (sound == null)
			return 0;
		
		DGEChannel channel = new DGEChannel();
		
		channel.ChannelID = _channelId++;
		
		channel.Volume = Math.min(100, Math.max(0, volume));
		channel.Panning = Math.min(100, Math.max(-100, panning));
		channel.Pitch = pitch;
		channel.Priority = priority;
		channel.Loop = loop;
		
		_channels.add(channel);
		
		if (sound.Ready)
			Channel_Play(sound.SoundID, channel.ChannelID);
		else
			sound.Play = channel;
			
		return channel.ChannelID;
	}
	
	private DGEEffect Effect_Get(int soundId)
	{
		for (DGEEffect sound : _effects)
		{
			if (sound.SoundID == soundId)
				return sound;
		}
		
		return null;
	}
	
	private DGEEffect Effect_Get(String filename)
	{
		for (DGEEffect sound : _effects)
		{
			if (sound.Filename.compareTo(filename) == 0)
				return sound;
		}
		
		return null;
	}
	
	public int Music_Load(String filename)
	{
		_dge.System_Log("SoundManager Music_Load %s", filename);
		
		DGEMusic music = Music_Get(filename);
		
		if (music != null)
			return music.MusicID;
		
		try 
		{
			music = new DGEMusic();
			
			music.MusicID = _musicId++;
			
			music.Filename = filename;			
			music.Play = false;
			music.Ready = false;
			
			music.Player = new MediaPlayer();
			
			music.Player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			music.Player.setDisplay(null);

			AssetFileDescriptor descriptor = _assets.openFd(filename);
			music.Player.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
			descriptor.close();

			music.Player.setOnPreparedListener(this);
			music.Player.prepareAsync();
			
			_music.add(music);
			
			return music.MusicID;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			
			return 0;
		}
	}
	
	public void Music_Free(int musicId)
	{
		DGEMusic music = Music_Get(musicId);
		
		if (music != null)
		{
			_music.remove(music);
			
			music.Player.stop();
			music.Player.release();
		}
	}

	public void Music_FreeAll()
	{
		for (DGEMusic music : _music)
		{
			music.Ready = false;
			
			music.Player.stop();
			music.Player.release();
		}
		
		_music.clear();
	}

	public void Music_Play(int musicId, float volume, float panning, boolean loop)
	{
		_dge.System_Log("SoundManager Music_Play %d (v%f, p%f, l%d)", musicId, volume, panning, loop ? 1: 0);
		DGEMusic music = Music_Get(musicId);
		
		if (music != null)
		{
			music.Volume = Math.min(100, Math.max(0, volume));
			music.Panning = Math.min(100, Math.max(-100, panning));
			music.Loop = loop;
			
			if (music.Ready)
			{
				_dge.System_Log(" - ready, playing now");
				
				float[] volumes = CalculateVolume(music.Volume, music.Panning);
				
				music.Player.setVolume(volumes[0], volumes[1]);
				music.Player.setLooping(music.Loop);
				
				music.Player.seekTo(0);
				
				music.Player.start();
			}
			else
			{
				_dge.System_Log(" - not ready, set to autostart");
				
				music.Play = true;
			}
		}
	}
	
	public void Music_Pause(int musicId)
	{
		DGEMusic music = Music_Get(musicId);
		
		if (music != null && music.Ready)
			music.Player.pause();
	}
	
	public void Music_Resume(int musicId)
	{
		DGEMusic music = Music_Get(musicId);
		
		if (music != null && music.Ready)
			music.Player.start();
	}
	
	public void Music_Stop(int musicId)
	{
		DGEMusic music = Music_Get(musicId);
		
		if (music != null && music.Ready)
		{
			music.Play = false;
			music.Ready = false;
			
			music.Player.stop();

			music.Player.prepareAsync();
		}
	}
	
	public void Music_SetPanning(int musicId, float panning)
	{
		DGEMusic music = Music_Get(musicId);
		
		if (music != null)
		{
			music.Panning = Math.min(100, Math.max(-100, panning));
			
			if (music.Ready)
			{
				float[] volumes = CalculateVolume(music.Volume, music.Panning);
				music.Player.setVolume(volumes[0], volumes[1]);
			}
		}
	}
	
	public void Music_SetVolume(int musicId, float volume)
	{
		DGEMusic music = Music_Get(musicId);
		
		if (music != null)
		{
			music.Volume = Math.min(100, Math.max(0, volume));
			
			if (music.Ready)
			{
				float[] volumes = CalculateVolume(music.Volume, music.Panning);
				music.Player.setVolume(volumes[0], volumes[1]);
			}
		}
	}
	
	public void Music_SetLooping(int musicId, boolean loop)
	{
		DGEMusic music = Music_Get(musicId);
		
		if (music != null && music.Ready)
		{
			music.Loop = loop;

			if (music.Ready)
				music.Player.setLooping(music.Loop);
		}
	}
	
	public void Music_SetPosition(int musicId, int position)
	{
		DGEMusic music = Music_Get(musicId);
		
		if (music != null && music.Ready)
			music.Player.seekTo(position);
	}
	
	public int Music_GetPosition(int musicId)
	{
		DGEMusic music = Music_Get(musicId);
		
		if (music != null && music.Ready)
			return music.Player.getCurrentPosition();
		
		return 0;
	}
	
	private DGEMusic Music_Get(int musicId)
	{
		for (DGEMusic music : _music)
		{
			if (music.MusicID == musicId)
				return music;
		}
		
		return null;
	}
	
	private DGEMusic Music_Get(MediaPlayer player)
	{
		for (DGEMusic music : _music)
		{
			if (music.Player == player)
				return music;
		}
		
		return null;
	}
	
	private DGEMusic Music_Get(String filename)
	{
		for (DGEMusic music : _music)
		{
			if (music.Filename.compareTo(filename) == 0)
				return music;
		}
		
		return null;
	}
	
	public void Channel_SetPanning(int channelId, float panning)
	{
		DGEChannel channel = Channel_Get(channelId);
		
		if (channel != null)
		{
			channel.Panning = Math.min(100, Math.max(-100, panning));
			
			float[] volumes = CalculateVolume(channel.Volume, channel.Panning);
			_soundPool.setVolume(channel.StreamID, volumes[0], volumes[1]);
		}
		
	}
	
	public void Channel_SetVolume(int channelId, float volume)
	{
		DGEChannel channel = Channel_Get(channelId);
		
		if (channel != null)
		{
			channel.Volume = Math.min(100, Math.max(0, volume));
			
			float[] volumes = CalculateVolume(channel.Volume, channel.Panning);
			_soundPool.setVolume(channel.StreamID, volumes[0], volumes[1]);
		}
		
	}
	
	public void Channel_SetPitch(int channelId, float pitch)
	{
		DGEChannel channel = Channel_Get(channelId);
		
		if (channel != null)
		{
			channel.Pitch = pitch;
			_soundPool.setRate(channel.StreamID, channel.Pitch);
		}
	}
	
	public void Channel_Pause(int channelId)
	{
		DGEChannel channel = Channel_Get(channelId);
		
		if (channel != null)
		{
			_soundPool.pause(channel.StreamID);
			channel.Playing = false;
		}
	}
	
	public void Channel_Resume(int channelId) 
	{
		DGEChannel channel = Channel_Get(channelId);
		
		if (channel != null)
		{
			_soundPool.resume(channel.StreamID);
			channel.Playing = true;
		}
	}
	
	public void Channel_Stop(int channelId)
	{
		DGEChannel channel = Channel_Get(channelId);
		
		if (channel != null)
		{
			_soundPool.stop(channel.StreamID);
			channel.Playing = false;
			
			_channels.remove(channel);
		}
	}
	
	public void Channel_PauseAll() 
	{
		for (DGEChannel channel : _channels)
		{
			_soundPool.pause(channel.StreamID);
			channel.Playing = false;
		}
	}
	
	public void Channel_ResumeAll()
	{
		for (DGEChannel channel : _channels)
		{
			_soundPool.resume(channel.StreamID);
			channel.Playing = true;
		}
	}
	
	public void Channel_StopAll() 
	{
		for (DGEChannel channel : _channels)
		{
			_soundPool.stop(channel.StreamID);
			channel.Playing = false;
		}
		
		_channels.clear();
	}
	
	public boolean Channel_IsPlaying(int channelId)
	{
		DGEChannel channel = Channel_Get(channelId);
		
		if (channel != null)
			return channel.Playing;

		return false;
	}
	
	private void Channel_Play(int soundId, int channelId)
	{
		DGEEffect sound = Effect_Get(soundId);
		DGEChannel channel = Channel_Get(channelId);
		
		if (sound == null || channel == null)
			return;
		
		float[] volumeLevels = CalculateVolume(channel.Volume, channel.Panning);
		channel.StreamID = _soundPool.play(sound.SoundID, volumeLevels[0], volumeLevels[1], 1, 0, channel.Pitch);
	}

	private DGEChannel Channel_Get(int channelId)
	{
		for (DGEChannel channel : _channels)
		{
			if (channel.ChannelID == channelId)
				return channel;
		}
		
		return null;
	}
	
	private float[] CalculateVolume(float volume, float panning)
	{
		float[] volumes = new float[2];

		volume = Math.min(100, Math.max(0, volume));
		panning = Math.min(100, Math.max(-100, panning));
		
		volumes[0] = volumes[1] = volume;
		
		if (panning != 0)
		{
			if (panning < 0)
			{
				volumes[1] *= (100f + (float) panning) / 100f;
			}
			else
			{
				volumes[0] *= (100f - (float) panning) / 100f;
			}
		}
		
		return volumes;
	}
	
	@Override
	public void onLoadComplete(SoundPool soundPool, int sampleId, int status) 
	{
		_dge.System_Log("SoundManager onLoadComplete %d (status %d)", sampleId, status);
		DGEEffect sound = Effect_Get(sampleId);
		
		if (sound != null && status == 0)
		{
			sound.Ready = true;
			
			if (sound.Play != null)
				Channel_Play(sound.SoundID, sound.Play.ChannelID);
		}
	}

	@Override
	public void onPrepared(MediaPlayer player) 
	{
		_dge.System_Log("SoundManager onPrepared");
		DGEMusic music = Music_Get(player);
		
		if (music != null)
		{
			music.Ready = true;
			
			if (music.Play)
			{
				float[] volumes = CalculateVolume(music.Volume, music.Panning);
				
				music.Player.setVolume(volumes[0], volumes[1]);
				music.Player.setLooping(music.Loop);
				
				music.Player.start();
			}
		}
	}

}
