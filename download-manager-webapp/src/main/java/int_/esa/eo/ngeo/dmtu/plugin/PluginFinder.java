package int_.esa.eo.ngeo.dmtu.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PluginFinder<T> {

	private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();
	// Since loaded classes (loaded URLs of jars) are in a static member
	// (currentClassLoader), the set must also be static.
	private static final Collection<URL> LOADED_JAR_URLS = new HashSet<URL>();

	private static final FilenameFilter JAR_FILTER = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".jar");
		}
	};

	private static ClassLoader currentClassLoader;

	private final Class<T> pluginClass;
	private final List<T> pluginCollection;

	public PluginFinder(Class<T> pluginClass) {
		this.pluginClass = pluginClass;
		this.pluginCollection = new ArrayList<T>();

		initClassLoader();

		reload();
	}

	private void initClassLoader() {
		// We should first acquire readLock then acquire writeLock if write is
		// needed, but the code will be hard to understand (double checked
		// locking will be needed).
		// Since we rarely create PluginFinder instances, it's reasonable to
		// use writeLock directly.
		LOCK.writeLock().lock();
		try {
			if (currentClassLoader == null) {
				currentClassLoader = this.getClass().getClassLoader();
			}
		} finally {
			LOCK.writeLock().unlock();
		}
	}

	public void search(String directory) throws MalformedURLException {
		final File dir = new File(directory);
		if (dir.isFile()) {
			return;
		}
		final File[] jarFiles = dir.listFiles(JAR_FILTER);
		loadJars(jarFiles);
	}

	public void loadJars(File... jarFiles) throws MalformedURLException {
		final List<URL> urls = new ArrayList<URL>();
		// The whole loading process is locked from different threads so that we
		// avoid loading the same jar twice in parallel.
		LOCK.readLock().lock();
		try {
			// Collect the URLs of the non-loaded jar files
			for (File jarFile : jarFiles) {
				final URL url = jarFile.toURI().toURL();
				// Avoid loading the same jar twice.
				if (LOADED_JAR_URLS.contains(url)) {
					continue;
				}
				urls.add(url);
			}
		} finally {
			LOCK.readLock().unlock();
		}

		if (urls.isEmpty()) {
			return;
		}

		loadUrls(urls);
	}

	private void loadUrls(final List<URL> urls) {
		LOCK.writeLock().lock();
		try {

			// Load non-loaded jar files using a new classloader
			currentClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), currentClassLoader);
			LOADED_JAR_URLS.addAll(urls);

			// The current class loader changed. It main contain new
			// plugins, so we must reload.
			reload();
		} finally {
			LOCK.writeLock().unlock();
		}
	}

	private void reload() {
		LOCK.writeLock().lock();
		try {
			final ServiceLoader<T> pluginLoader = ServiceLoader.load(
					pluginClass, currentClassLoader);

			pluginCollection.clear();
			for (Iterator<T> iterator = pluginLoader.iterator(); iterator
					.hasNext();) {
				final T plugin = iterator.next();
				pluginCollection.add(plugin);
			}
		} finally {
			LOCK.writeLock().unlock();
		}
	}

	public List<T> getPluginCollection() {
		LOCK.readLock().lock();
		try {
			return Collections.unmodifiableList(pluginCollection);
		} finally {
			LOCK.readLock().unlock();
		}
	}

}