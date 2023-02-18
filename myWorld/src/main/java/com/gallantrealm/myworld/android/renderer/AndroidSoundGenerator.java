package com.gallantrealm.myworld.android.renderer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.gallantrealm.android.HttpFileCache;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.android.R;
import com.gallantrealm.myworld.client.renderer.ISoundGenerator;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWVector;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

public final class AndroidSoundGenerator implements ISoundGenerator {

	static MediaPlayer song;
	SoundPool soundPool;
	final HashMap<String, Integer> soundMap;
	final HashMap<Integer, StreamInfo> playingStreams = new HashMap<Integer, StreamInfo>();

	static class StreamInfo {
		StreamInfo(String soundName, int streamId, float volume, int priority, float rate) {
			this.soundName = soundName;
			this.streamId = streamId;
			this.volume = volume;
			this.priority = priority;
			this.rate = rate;
		}

		String soundName;
		int streamId;
		float volume;
		int priority;
		float rate;
	}

	Context context;
	AtomicInteger loadingCount = new AtomicInteger();

	@SuppressWarnings("deprecation")
	public AndroidSoundGenerator(Context context) {
		System.out.println(">AndroidSoundGenerator.constructor");
		this.context = context;

		// Initialize the sound pool
		if (Build.VERSION.SDK_INT >= 21) {
			AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
			soundPool = new SoundPool.Builder().setMaxStreams(8).setAudioAttributes(audioAttributes).build();
		} else {
			soundPool = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
		}
		soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				loadingCount.decrementAndGet();
			}
		});
		soundMap = new HashMap<String, Integer>();

		// load all the predefined sounds
		loadSound("awful", R.raw.sound_awful);
		loadSound("bang", R.raw.sound_bang);
		loadSound("beep", R.raw.sound_beep);
		loadSound("bell", R.raw.sound_bell);
		loadSound("bonk1", R.raw.sound_bonk1);
		loadSound("bonk2", R.raw.sound_bonk2);
		loadSound("bonk3", R.raw.sound_bonk3);
		loadSound("bonk4", R.raw.sound_bonk4);
		loadSound("car", R.raw.sound_car);
		loadSound("carCrash", R.raw.sound_car_crash);
		loadSound("carScrape", R.raw.sound_car_scrape);
		loadSound("carSlide", R.raw.sound_car_slide);
		loadSound("chainlink", R.raw.sound_chainlink);
		loadSound("chords", R.raw.sound_chords);
		loadSound("concrete", R.raw.sound_concrete);
		loadSound("down", R.raw.sound_down);
		loadSound("formulaCar", R.raw.sound_formula_car);
		loadSound("grass", R.raw.sound_grass);
		loadSound("guitar", R.raw.sound_guitar);
		loadSound("honk", R.raw.sound_honk);
		loadSound("loosingSound", R.raw.sound_loosing);
		loadSound("movingGrass", R.raw.sound_moving_grass);
		loadSound("musicbox", R.raw.sound_musicbox);
		loadSound("paddle", R.raw.sound_paddle);
		loadSound("phaser", R.raw.sound_phaser);
		loadSound("piano", R.raw.sound_piano);
		loadSound("rush", R.raw.sound_rush);
		loadSound("saucer", R.raw.sound_saucer);
		loadSound("skate", R.raw.sound_skate);
		loadSound("softbell", R.raw.sound_softbell);
		loadSound("splash", R.raw.sound_splash);
		loadSound("success", R.raw.sound_success);
		loadSound("throw", R.raw.sound_throw);
		loadSound("thruster", R.raw.sound_thruster);
		loadSound("thud", R.raw.sound_thud);
		loadSound("tick", R.raw.sound_tick);
		loadSound("timew", R.raw.sound_timew);
		loadSound("trickle", R.raw.sound_trickle);
		loadSound("trumpets", R.raw.sound_trumpets);
		loadSound("underwater", R.raw.sound_underwater);
		loadSound("warp", R.raw.sound_warp);
		loadSound("whack", R.raw.sound_whack);
		loadSound("wind", R.raw.sound_wind);
		loadSound("winningSound", R.raw.sound_winning);
		loadSound("winningSound2", R.raw.sound_winning2);
		loadSound("wood", R.raw.sound_wood);

		// soundGeneratorThread.start();
		System.out.println("<AndroidSoundGenerator.constructor");
	}

	void loadSound(String soundName, int resId) {
		loadingCount.incrementAndGet();
		soundMap.put(soundName, soundPool.load(context, resId, 0));
		System.out.println("AndroidSoundGenerator.loadSound loading sound " + soundName);
	}

	public void loadSound(String urlString) {
		File soundFile = null;
		try {
			soundFile = HttpFileCache.getFile(urlString, context);
			loadingCount.incrementAndGet();
			int soundId = soundPool.load(soundFile.getPath(), 1);
			soundMap.put(urlString, soundId);
			System.out.println("AndroidSoundGenerator.loadSound loading sound " + urlString);
		} catch (IOException e) {
			e.printStackTrace();
			loadingCount.decrementAndGet();
		}

	}

	@Override
	public boolean areSoundsLoaded() {
		return loadingCount.intValue() == 0;
	}

	@Override
	public void playSound(String sound, int priority, WWVector position, float volume, float pitch) {
		if (paused) {
			return;
		}
		if (AndroidClientModel.getClientModel().world == null) {
			return;
		}
		if (!AndroidClientModel.getClientModel().isPlaySoundEffects()) {
			return;
		}
		float distanceFrom;
		if (position != null) {
			WWObject avatar = AndroidClientModel.getClientModel().getAvatar();
			long time = AndroidClientModel.getClientModel().world.getWorldTime();
			distanceFrom = (float) Math.max(1.0, avatar.getPosition().distanceFrom(position));
		} else {
			distanceFrom = 1;
		}
		if (distanceFrom < 100.0f) { // only play sounds fairly near (should be a world property)
			Integer soundId = soundMap.get(sound);
			if (soundId == null) {
				loadSound(sound);
				soundId = soundMap.get(sound);
			}
			if (soundId != null) {
				AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
				float currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC) / 15.0f;
				float level = FastMath.min(1.0f, volume * currentVolume / distanceFrom);
				if (AndroidClientModel.getClientModel().isPlaySoundEffects()) {
					if (loadingCount.intValue() <= 0) {
						soundPool.play(soundId, level, level, priority, 0, pitch);
						System.out.println("AndroidSoundGenerator.playSound " + sound + " playing");
					}
				}
			}
		}
	}

	@Override
	public int startPlayingSound(String sound, int priority, WWVector position, float volume, float pitch) {
		if (paused) {
			return 0;
		}
		if (AndroidClientModel.getClientModel().world == null) {
			return 0;
		}
		if (!AndroidClientModel.getClientModel().isPlaySoundEffects()) {
			return 0;
		}
		WWObject avatar = AndroidClientModel.getClientModel().getAvatar();
		long time = AndroidClientModel.getClientModel().world.getWorldTime();
		float distanceFrom = 1;
		if (position != null) {
			distanceFrom = FastMath.max(1.0f, avatar.getPosition().distanceFrom(position));
		}
//		if (distanceFrom < 100.0f) {
		Integer soundId = soundMap.get(sound);
		if (soundId != null) {
			AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			float currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC) / 15.0f;
			float level = FastMath.min(1.0f, volume * currentVolume / distanceFrom);
			pitch = FastMath.max(0.6f, FastMath.min(1.9f, pitch));
			if (loadingCount.intValue() <= 0) {
				int streamId = soundPool.play(soundId, level, level, priority, 10000, pitch);
				synchronized (playingStreams) {
					playingStreams.put(streamId, new StreamInfo(sound, streamId, level, priority, pitch));
				}
				System.out.println("AndroidSoundGenerator.startPlayingSound " + sound + " playing, stream id is " + streamId);
				return streamId;
			}
		}
//		}
		return 0;
	}

	@Override
	public int adjustPlayingSound(int streamId, WWVector position, float volume, float pitch) {
		if (paused) {
			return 0;
		}
		if (AndroidClientModel.getClientModel().world == null) {
			stopPlayingSound(streamId);
			return 0;
		}
		if (!AndroidClientModel.getClientModel().isPlaySoundEffects()) {
			stopPlayingSound(streamId);
			return 0;
		}
		WWObject avatar = AndroidClientModel.getClientModel().getAvatar();
		long time = AndroidClientModel.getClientModel().world.getWorldTime();
		float distanceFrom = 1;
		if (position != null) {
			distanceFrom = FastMath.max(1.0f, avatar.getPosition().distanceFrom(position));
		}
//		if (distanceFrom < 100.0f) {
		AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		float currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC) / 15.0f;
		float level = volume * currentVolume / distanceFrom;
		pitch = FastMath.max(0.6f, FastMath.min(1.9f, pitch));
		StreamInfo info = playingStreams.get(streamId);
		if (info != null) { // no stream to adjust
			info.volume = level;
			info.rate = pitch;
			if (!paused) {
				soundPool.setVolume(info.streamId, info.volume, info.volume);
				soundPool.setRate(info.streamId, info.rate);
			}
		}

//		} else {
//			soundPool.stop(streamId);
//		}
		return streamId;
	}

	@Override
	public void stopPlayingSound(int streamId) {
		System.out.println("AndroidSoundGenerator.stopPlayingSound " + streamId);
		synchronized (playingStreams) {
			soundPool.stop(streamId);
			StreamInfo info = playingStreams.get(new Integer(streamId));
			if (info != null) {
				// System.out.println("Stopping: " + info.soundName);
				playingStreams.remove(new Integer(streamId));
			}
		}
	}

	public boolean paused;

	@Override
	public void pause() {
		System.out.println(">AndroidSoundGenerator.pause");
		paused = true;
		soundPool.autoPause();
		if (song != null) {
			try {
				song.stop();
			} catch (IllegalStateException e) {
			}
		}
		System.out.println("<AndroidSoundGenerator.pause");
	}

	@Override
	public void resume() {
		System.out.println(">AndroidSoundGenerator.resume");
		soundPool.autoResume();
		if (song != null) {
			try {
				song.start();
			} catch (IllegalStateException e) {
			}
		}
		paused = false;
		System.out.println("<AndroidSoundGenerator.resume");
	}

	@Override
	public void reset() {
		System.out.println("AndroidSoundGenerator.reset");
// Note: this isn't working.. it causes hang in app		
//		synchronized (playingStreams) {
//			for (int i : playingStreams.keySet()) {
//				StreamInfo info = playingStreams.get(i);
//				if (info != null) {
//					System.out.println("Stopping: " + info.soundName);
//					soundPool.stop(info.streamId);
//				}
//			}
//			HashMap<Integer, StreamInfo> oldPlayingStreams = playingStreams;
//			playingStreams = new HashMap<Integer, StreamInfo>();
//			for (int i : oldPlayingStreams.keySet()) {
//				StreamInfo info = oldPlayingStreams.get(i);
//				if (info != null) {
//					System.out.println("Starting: " + info.soundName);
//					int newStreamId = soundPool.play(info.streamId, info.volume, info.volume, info.priority, 10000, info.rate);
//					playingStreams.put(newStreamId, info);
//				}
//			}
//		}
//		stopPlayingSong();
	}

	@Override
	public void stop() {
		System.out.println(">AndroidSoundGenerator.stop");
		synchronized (playingStreams) {
			for (int i : playingStreams.keySet()) {
				StreamInfo info = playingStreams.get(i);
				if (info != null) {
					System.out.println("AndroidSoundGenerator.stop: Stopping: " + info.soundName);
					soundPool.stop(info.streamId);
				}
			}
			playingStreams.clear();
		}
		stopPlayingSong();
		System.out.println("<AndroidSoundGenerator.stop");
	}

	@Override
	public void destroy() {
		System.out.println(">AndroidSoundGenerator.destroy");
		synchronized (playingStreams) {
			for (int i : playingStreams.keySet()) {
				StreamInfo info = playingStreams.get(i);
				if (info != null) {
					System.out.println("AndroidSoundGenerator.destroy: Stopping: " + info.soundName);
					soundPool.stop(info.streamId);
				}
			}
			playingStreams.clear();
		}
		stopPlayingSong();
		soundPool.release();
		soundPool = null;
		soundMap.clear();
		System.out.println("<AndroidSoundGenerator.destroy");
	}

	@Override
	public void playSong(String songname, float volume) {
		System.out.println("AndroidSoundGenerator.playSong "+ songname + " " + volume);
		stopPlayingSong();
		if (AndroidClientModel.getClientModel().isPlayMusic()) {
			try {
				if (songname.startsWith("file") || songname.startsWith("http")) { // an URL most likely
					song = new MediaPlayer();
					song.setDataSource(songname);
					song.prepare();
					song.setLooping(true);
					song.setVolume(volume, volume);
					song.start();
				} else {
					AssetFileDescriptor afd = context.getAssets().openFd(songname + ".ogg");
					song = new MediaPlayer();
					song.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
					song.prepare();
					song.setLooping(true);
					song.setVolume(volume, volume);
					song.start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stopPlayingSong() {
		System.out.println("AndroidSoundGenerator.stopPlayingSong");
		if (song != null) {
			try {
				if (song.isPlaying()) {
					song.stop();
				}
				song.reset();
				song.release();
			} catch (Exception e) {
			}
			song = null;
		}
	}

}
