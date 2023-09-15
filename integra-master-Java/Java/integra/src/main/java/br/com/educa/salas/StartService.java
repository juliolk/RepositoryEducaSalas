package br.com.educa.salas;

import org.springframework.boot.loader.JarLauncher;
import org.springframework.boot.loader.jar.JarFile;

public class StartService extends JarLauncher {

	private static ClassLoader classLoader = null;
	private static StartService startService = null;

	protected void launch(String[] args, String mainClass, ClassLoader classLoader, boolean wait) throws Exception {
		Thread runnerThread = new Thread(() -> {
			try {
				createMainMethodRunner(mainClass, args, classLoader).run();
			} catch (Exception ex) {
			}
		});
		runnerThread.setContextClassLoader(classLoader);
		runnerThread.setName(Thread.currentThread().getName());
		runnerThread.start();
		if (wait == true) {
			runnerThread.join();
		}
	}

	public static void start(String[] args) {
		startService = new StartService();
		try {
			JarFile.registerUrlProtocolHandler();
			classLoader = startService.createClassLoader(startService.getClassPathArchives());
			startService.launch(args, startService.getMainClass(), classLoader, true);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	public static void stop(String[] args) {
		try {
			if (startService != null) {
				startService.launch(args, startService.getMainClass(), classLoader, true);
				startService = null;
				classLoader = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		String mode = args != null && args.length > 0 ? args[0] : null;
		if ("start".equals(mode)) {
			StartService.start(args);
		} else if ("stop".equals(mode)) {
			StartService.stop(args);
		}
	}

}
