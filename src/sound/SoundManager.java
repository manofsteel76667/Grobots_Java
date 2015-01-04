package sound;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import support.FinePoint;

public class SoundManager {
	public enum SoundType {
		stBirth("Birth.wav"), stBeep("Beep.wav"), stBigBlaster("BigBlaster.wav"), stBigExplosion(
				"BigExplosion.wav"), stBlaster("Blaster.wav"), stEndRound(
				"EndRound.wav"), stExplosion("Explosion.wav"), stExtinction(
				"Extinction.wav"), stGrenade("Grenade.wav"), stSmallExplosion(
				"SmallExplosion.wav"), stTinyExplosion("TinyExplosion.wav");

		public final String filename;
		AudioInputStream[] stream;
		Clip[] clip;
		static final int numClips = 4;
		int nextClip = 0;

		SoundType(String _filename) {
			clip = new Clip[numClips];
			stream = new AudioInputStream[numClips];
			filename = _filename;
			for (nextClip = 0; nextClip < numClips; nextClip++) {
				stream[nextClip] = makeStream();
				clip[nextClip] = makeClip();
			}
			nextClip = 0;
		}

		public Clip getClip() {
			return clip[nextClip];
		}

		public void open() {
			try {
				if (!clip[nextClip].isOpen())
					clip[nextClip].open(stream[nextClip]);
			} catch (LineUnavailableException | IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Play the sound from the beginning.
		 */
		public void play() {
			clip[nextClip].start();
			nextClip = (nextClip + 1) % numClips;
			clip[nextClip].setFramePosition(0);
			try {
				stream[nextClip].reset();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Clip makeClip() {
			Clip clip = null;
			if (stream[nextClip] == null)
				return null;
			try {
				AudioFormat format = stream[nextClip].getFormat();
				DataLine.Info info = new DataLine.Info(Clip.class, format);
				clip = (Clip) AudioSystem.getLine(info);
				clip.addLineListener(new LineListener() {
					@Override
					public void update(LineEvent evt) {
						if (evt.getType() == LineEvent.Type.STOP)
							((Clip) evt.getSource()).close();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (stream[nextClip] != null)
					try {
						stream[nextClip].close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			return clip;
		}

		AudioInputStream makeStream() {
			AudioInputStream ais = null;
			AudioInputStream reusableAis = null;
			// Load each sound as a byte array. This is so the reset() command
			// will work, making
			// the sound reusable without reloading it.
			try {
				ais = AudioSystem.getAudioInputStream(getClass()
						.getResourceAsStream(filename));
				byte[] buffer = new byte[1024 * 32];
				int read = 0;
				ByteArrayOutputStream baos = new ByteArrayOutputStream(
						buffer.length);
				while ((read = ais.read(buffer, 0, buffer.length)) != -1) {
					baos.write(buffer, 0, read);
				}
				reusableAis = new AudioInputStream(new ByteArrayInputStream(
						baos.toByteArray()), ais.getFormat(),
						AudioSystem.NOT_SPECIFIED);
			} catch (UnsupportedAudioFileException | IOException e) {
				e.printStackTrace();
			} finally {
				if (ais != null) {
					try {
						ais.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return reusableAis;
		}
	}

	FinePoint viewpoint = new FinePoint();
	static SoundManager manager;
	static final float kSoundDistanceFadeFactor = 1.2f;

	public void setViewPoint(FinePoint point) {
		viewpoint = point;
	}

	public FinePoint getViewpoint() {
		return viewpoint;
	}

	public static SoundManager getManager() {
		return manager;
	}

	public static void setManager(SoundManager value) {
		if (manager == null)
			manager = value;
	}

	public static synchronized void playSound(final SoundType sound) {
		if (manager == null)
			return;
		new Runnable() {
			@Override
			public void run() {
				sound.open();
				sound.play();
			}
		}.run();
	}

	/**
	 * Play a sound at a given location in the world. Sound balance and volume
	 * are affected by its distance and direction from the viewpoint
	 * 
	 * @param sound
	 * @param location
	 * @param viewpoint
	 */
	public static synchronized void playSound(final SoundType sound,
			final FinePoint location) {
		if (manager == null)
			return;
		new Runnable() {
			@Override
			public void run() {
				sound.open();
				// Fade the sound based on how far it was from the viewpoint
				double distance = location.distance(manager.getViewpoint());
				float volume = Math.min(
						Math.max(7 - kSoundDistanceFadeFactor
								* (float) distance, -80), 6);
				if (volume > -50) {
					if (sound.getClip().isControlSupported(
							FloatControl.Type.MASTER_GAIN)) {
						FloatControl volumeControl = (FloatControl) sound
								.getClip().getControl(
										FloatControl.Type.MASTER_GAIN);
						volumeControl.setValue(volume);
					}
					sound.play();
				}
			}
		}.run();
	}

	public SoundManager() {

	}

	public static void main(String[] args) throws LineUnavailableException {
		SoundManager.setManager(new SoundManager());
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel pane = new JPanel();
		pane.setLayout(new FlowLayout());
		frame.setContentPane(pane);
		for (final SoundType type : SoundType.values()) {
			JButton button = new JButton(type.filename);
			button.addActionListener(new ActionListener() {
				int distance = 1;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						SoundManager.playSound(type, new FinePoint(distance,
								distance));
					} catch (Exception e) {
						e.printStackTrace();
					}
					distance = (distance + 1) % 40;
				}
			});
			pane.add(button);
		}
		frame.pack();
		frame.setVisible(true);
	}
}
