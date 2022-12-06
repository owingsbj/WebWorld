package com.gallantrealm.myworld.android.renderer;

import java.io.File;
import java.util.HashMap;
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

	final SoundGeneratorThread soundGeneratorThread = new SoundGeneratorThread();

	class StreamInfo {
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

	public AndroidSoundGenerator(Context context) {
		System.out.println("AndroidSoundGenerator.constructor");
		this.context = context;

		// Initialize the sound pool
		if (Build.VERSION.SDK_INT >= 21) {
		AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
		soundPool = new SoundPool.Builder().setMaxStreams(8).setAudioAttributes(audioAttributes).build();
		} else {
			soundPool = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
		}
		soundMap = new HashMap<String, Integer>();

		// load all the predefined sounds
		loadSound("wind", R.raw.sound_wind);
		loadSound("trickle", R.raw.sound_trickle);
		loadSound("splash", R.raw.sound_splash);
		loadSound("rush", R.raw.sound_rush);
		loadSound("underwater", R.raw.sound_underwater);
		loadSound("grass", R.raw.sound_grass);
		loadSound("movingGrass", R.raw.sound_moving_grass);
		loadSound("wood", R.raw.sound_wood);
		loadSound("concrete", R.raw.sound_concrete);
		loadSound("car", R.raw.sound_car);
		loadSound("carSlide", R.raw.sound_car_slide);
		loadSound("carCrash", R.raw.sound_car_crash);
		loadSound("carScrape", R.raw.sound_car_scrape);
		loadSound("skate", R.raw.sound_skate);
		loadSound("winningSound", R.raw.sound_winning);
		loadSound("winningSound2", R.raw.sound_winning2);
		loadSound("loosingSound", R.raw.sound_loosing);
		loadSound("saucer", R.raw.sound_saucer);
		loadSound("thruster", R.raw.sound_thruster);
		loadSound("formulaCar", R.raw.sound_formula_car);
		loadSound("paddle", R.raw.sound_paddle);
		loadSound("bonk1", R.raw.sound_bonk1);
		loadSound("bonk2", R.raw.sound_bonk2);
		loadSound("bonk3", R.raw.sound_bonk3);
		loadSound("bonk4", R.raw.sound_bonk4);
		loadSound("down", R.raw.sound_down);
		loadSound("bell", R.raw.sound_bell);
		loadSound("softbell", R.raw.sound_softbell);
		loadSound("beep", R.raw.sound_beep);
		loadSound("chords", R.raw.sound_chords);
		loadSound("bang", R.raw.sound_bang);
		loadSound("thud", R.raw.sound_thud);
		loadSound("trumpets", R.raw.sound_trumpets);
		loadSound("warp", R.raw.sound_warp);
		loadSound("phaser", R.raw.sound_phaser);
		loadSound("success", R.raw.sound_success);
		loadSound("honk", R.raw.sound_honk);
		loadSound("throw", R.raw.sound_throw);
		loadSound("whack", R.raw.sound_whack);
		loadSound("awful", R.raw.sound_awful);
		loadSound("timew", R.raw.sound_timew);
		loadSound("chainlink", R.raw.sound_chainlink);
		loadSound("piano", R.raw.sound_piano);
		loadSound("guitar", R.raw.sound_guitar);
		loadSound("musicbox", R.raw.sound_musicbox);

		// soundGeneratorThread.start();
	}

	final void loadSound(String soundName, int resId) {
		String usedSounds = context.getString(R.string.usedSounds);
		if (usedSounds.length() == 0 || usedSounds.contains(soundName)) {
			soundMap.put(soundName, soundPool.load(context, resId, 0));
		}
	}

	public void loadSound(String urlString) {
		System.out.println("loading sound " + urlString);
		File soundFile = HttpFileCache.getFile(urlString, context);
		int soundId = soundPool.load(soundFile.getPath(), 1);
		soundMap.put(urlString, soundId);
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
			distanceFrom = (float) Math.max(1.0, avatar.getPosition(time).distanceFrom(position));
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
					soundPool.play(soundId, level, level, priority, 0, pitch);
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
			distanceFrom = FastMath.max(1.0f, avatar.getPosition(time).distanceFrom(position));
		}
//		if (distanceFrom < 100.0f) {
		Integer soundId = soundMap.get(sound);
		if (soundId != null) {
			AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			float currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC) / 15.0f;
			float level = FastMath.min(1.0f, volume * currentVolume / distanceFrom);
			pitch = FastMath.max(0.6f, FastMath.min(1.9f, pitch));
			// System.out.println("Starting: " + sound);
			int streamId = soundPool.play(soundId, level, level, priority, 10000, pitch);
			synchronized (playingStreams) {
				playingStreams.put(streamId, new StreamInfo(sound, streamId, level, priority, pitch));
			}
			return streamId;
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
			distanceFrom = FastMath.max(1.0f, avatar.getPosition(time).distanceFrom(position));
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
		System.out.println("AndroidSoundGenerator.pause");
		paused = true;
		soundPool.autoPause();
		if (song != null) {
			try {
				song.stop();
			} catch (IllegalStateException e) {
			}
		}
	}

	@Override
	public void resume() {
		System.out.println("AndroidSoundGenerator.resume");
		soundPool.autoResume();
		if (song != null) {
			try {
				song.start();
			} catch (IllegalStateException e) {
			}
		}
		paused = false;
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
		System.out.println("AndroidSoundGenerator.stop");
		synchronized (playingStreams) {
			for (int i : playingStreams.keySet()) {
				StreamInfo info = playingStreams.get(i);
				if (info != null) {
					System.out.println("Stopping: " + info.soundName);
					soundPool.stop(info.streamId);
				}
			}
			playingStreams.clear();
		}
		stopPlayingSong();
	}

	@Override
	public void destroy() {
		System.out.println("AndroidSoundGenerator.destroy");
		synchronized (playingStreams) {
			for (int i : playingStreams.keySet()) {
				StreamInfo info = playingStreams.get(i);
				if (info != null) {
					System.out.println("Stopping: " + info.soundName);
					soundPool.stop(info.streamId);
				}
			}
			playingStreams.clear();
		}
		stopPlayingSong();
		soundPool.release();
		soundPool = null;
		soundMap.clear();
	}

	// Need a thread because android drags on soundpool calls
	class SoundGeneratorThread extends Thread {

		public SoundGeneratorThread() {
			super("SoundGeneratorThread");
		}

		@Override
		public void run() {
			try {
				while (true) {
					Thread.sleep(100);
					try {
						synchronized (playingStreams) {
							if (!paused) {
								for (int i : playingStreams.keySet()) {
									StreamInfo info = playingStreams.get(i);
									if (info != null) {
										soundPool.setVolume(info.streamId, info.volume, info.volume);
										soundPool.setRate(info.streamId, info.rate);
									}
								}
							}
						}
					} catch (Exception e) {
					}
				}
			} catch (InterruptedException e) {
			}
		}

	}

	@Override
	public void playSong(String songname, float volume) {
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
		if (song != null) {
			try {
				song.stop();
				song.release();
			} catch (Exception e) {
			}
			song = null;
		}
	}

}
