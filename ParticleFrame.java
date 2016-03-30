import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class ParticleFrame extends JFrame {

	protected ParticleThread[] threads; // null when not running

	protected int size = 400;

	protected ParticleComponent component = new ParticleComponent(size);

	protected Object lock = new Object();
	
	public ParticleFrame() {
		setContentPane(component);
	}

	public static void main(String[] args) {
		final ParticleFrame frame = new ParticleFrame();
		frame.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent arg0) {
				int c = arg0.getKeyChar();
				if (c == 's') {
					if (frame.threads == null) {
						frame.start();
					} else {
						frame.stopOrResume();
					}
				}
			}
		});
		frame.setFocusable(true);
		frame.requestFocus();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	protected static class ParticleThread extends Thread {
		// see example in
		// http://www.exampledepot.com/egs/java.lang/PauseThread.html
		boolean pleaseWait = false;

		Particle particle;

		JComponent component;

		ParticleThread(Particle p, JComponent component) {
			this.component = component;
			this.particle = p;
		}

		public void run() {
			try {
				for (;;) {
					synchronized (this) {
						while (pleaseWait) {
							try {
								wait();
							} catch (InterruptedException e) {
							}
						}
					}
					particle.move();
					component.repaint();
					Thread.sleep(100); // 100msec is arbitrary
				}
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	protected ParticleThread makeThread(final Particle p) { // utility
		return new ParticleThread(p, component);
	}

	public synchronized void start() {
		int n = 10; // just for demo

		if (threads == null) { // bypass if already started
			Particle[] particles = new Particle[n];
			for (int i = 0; i < n; ++i)
				particles[i] = new Particle(size / 2, size / 2);
			component.setParticles(particles);

			threads = new ParticleThread[n];
			for (int i = 0; i < n; ++i) {
				threads[i] = makeThread(particles[i]);
				threads[i].start();
			}
		}
	}

	public synchronized void stopOrResume() {
		if (threads != null) {
			for (int i = 0; i < threads.length; ++i) {
				synchronized (threads[i]) {
					if (threads[i].pleaseWait) {
						threads[i].pleaseWait = false;
						threads[i].notify();
					} else {
						threads[i].pleaseWait = true;
					}
				}
			}
		}
	}

}
