package com.beagledata.gaea.ruleengine.util;

import org.apache.commons.lang3.StringUtils;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 描述:
 * java源码动态加载
 *
 * @author 周庚新
 * @date 2020-09-15
 */
public class JavaSourceClassLoader extends ClassLoader {

	private String src;
	private String depJarPath;
	private byte[] classBytes;

	public JavaSourceClassLoader(String src) {
		this(src, null);
	}

	public JavaSourceClassLoader(String src, String depJarPath) {
		this.src = src;
	}

	/**
	 * 描述: 根据java源码，动态编译成class载入jvm
	 *
	 * @param: []
	 * @author: 周庚新
	 * @date: 2020/9/15
	 * @return: java.lang.Class<?>
	 */
	public Class<?> loadClass() throws ClassNotFoundException {
		String className = null;
		String packageName = null;
		Matcher matcher = Pattern.compile("public\\s+class\\s+(\\w+)").matcher(src);
		if (matcher.find()) {
			className = matcher.group(1);
			matcher = Pattern.compile("package\\s+(\\w+|.+);").matcher(src);
			if (matcher.find()) {
				packageName = matcher.group(1);
			}
		}

		if (StringUtils.isBlank(className)) {
			throw new IllegalArgumentException("没有ClassName");
		}
		classBytes = compile(className + ".java", src, depJarPath);
		if (StringUtils.isNotBlank(packageName)) {
			className = packageName + "." + className;
		}
		return loadClass(className);
	}


	/**
	* 描述:
	* @param: [javaName, javaSrc, depJarPath] public class  ,javaSrc 源码，
	* @author: 周庚新
	* @date: 2020/9/16
	* @return: byte[]
	*
	*/
	private byte[] compile(String javaName, String javaSrc, String depJarPath) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);
		try {
			MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager);
			JavaFileObject javaFileObject = MemoryJavaFileManager.makeStringSource(javaName, javaSrc);
			Iterable<String> options = null;
			if (StringUtils.isNotBlank(depJarPath)) {
				options = Arrays.asList("-classpath", getDepJarPath(depJarPath));
			}
			StringWriter sw = new StringWriter();
			JavaCompiler.CompilationTask task = compiler.getTask(
					sw,
					manager,
					null,
					options,
					null,
					Arrays.asList(javaFileObject)
			);
			if (task.call()) {
				return manager.getClassBytes();
			}
			throw new IllegalArgumentException(sw.toString());
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Compile error.", e);
		}
	}

	private String getDepJarPath(String depJarPath) {
		File sourceFile = new File(depJarPath);
		StringBuilder jars = new StringBuilder();
		if (sourceFile.exists()) {
			if (sourceFile.isDirectory()) {
				String os = System.getProperty("os.name").toLowerCase();
				String separate = os.indexOf("windows") != -1 ? ";" : ":";
				File[] jarFiles = sourceFile.listFiles();
				if (jarFiles != null && jarFiles.length > 0) {
					for (File jarFile : jarFiles) {
						if (jarFile.getName().endsWith(".jar")) {
							jars.append(jarFile.getPath()).append(separate);
						}
					}
				}
			}
		}
		return jars.toString();
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return defineClass(name, classBytes, 0, classBytes.length);
	}

	/**
	 * 描述: MemoryJavaFileManager that keeps compiled .class in memory
	 *
	 * @author: 周庚新
	 * @date: 2020/9/15
	 */
	@SuppressWarnings("unchecked")
	private static class MemoryJavaFileManager extends ForwardingJavaFileManager {

		/**
		 * 描述: java source file extension
		 */
		private final static String EXT = ".java";

		private byte[] classBytes;

		/**
		 * Creates a new instance of ForwardingJavaFileManager.
		 *
		 * @param fileManager delegate to this file manager
		 */
		public MemoryJavaFileManager(JavaFileManager fileManager) {
			super(fileManager);
		}

		public byte[] getClassBytes() {
			return classBytes;
		}

		@Override
		public void close() throws IOException {
			classBytes = null;
		}

		@Override
		public void flush() throws IOException {
		}

		private static class StringInputBuffer extends SimpleJavaFileObject {
			final String code;

			StringInputBuffer(String name, String code) {
				super(toURI(name), Kind.SOURCE);
				this.code = code;
			}

			@Override
			public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
				return CharBuffer.wrap(code);
			}

			public Reader openReader() {
				return new StringReader(code);
			}
		}

		private class ClassOutputBuffe extends SimpleJavaFileObject {
			private String name;

			ClassOutputBuffe(String name) {
				super(toURI(name), Kind.CLASS);
				this.name = name;
			}

			@Override
			public OutputStream openOutputStream() {
				return new FilterOutputStream(new ByteArrayOutputStream()) {
					@Override
					public void close() throws IOException {
						out.close();
						ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
						classBytes = bos.toByteArray();
					}
				};
			}
		}

		@Override
		public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location,
												   String className,
												   JavaFileObject.Kind kind,
												   FileObject sibling) throws IOException {
			if (kind == JavaFileObject.Kind.CLASS) {
				return new ClassOutputBuffe(className);
			} else {
				return super.getJavaFileForOutput(location, className, kind, sibling);
			}
		}

		static JavaFileObject makeStringSource(String name, String code) {
			return new StringInputBuffer(name, code);
		}

		static URI toURI(String name) {
			File file = new File(name);
			if (file.exists()) {
				return file.toURI();
			}

			try {
				final StringBuilder newUri = new StringBuilder();
				newUri.append("mfm:///");
				newUri.append(name.replace(".", "/"));
				if (name.endsWith(EXT)) {
					newUri.replace(newUri.length() - EXT.length(), newUri.length(), EXT);
				}
				return URI.create(newUri.toString());
			} catch (Exception exp) {
				return URI.create("mfm:///com/sun/script/java/java_source");
			}
		}
	}
}