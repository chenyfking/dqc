package com.beagledata.gaea.workbench.util;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

/**
 * 描述:
 *
 * @author 周庚新
 * @date 2020-07-27
 */
public class MavenInstall {
	private final static Logger log = LoggerFactory.getLogger(MavenInstall.class);


	public static void main(String[] args) {
		Collection<File> listFiles = FileUtils.listFiles(new File("D:\\newMaven"), new String[]{"jar"}, true);
		if (listFiles == null) {
			return;
		}
		String pomPath = "E:\\beagledata\\gaea\\gaea-decision\\workbench";
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {

			for (File file : listFiles) {
				String path = file.getPath();
				String mavenCommand = getMavenCommand(path);
				//mavenCommand = "mvn install:install-file -DgroupId=com.beagledata.ai -DartifactId=model-predict -Dversion=2.0 -Dpackaging=jar -Dfile=./lib/ModelPredict-2.0.jar";
				process = runtime.exec("cmd /c cd " + pomPath + " && " + mavenCommand);
				process.waitFor();
				process.destroy();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getMavenCommand(String jarPath) throws Exception {
		String pomPath = jarPath.replace(".jar", ".pom");
		FileInputStream fis = new FileInputStream(new File(pomPath));

		MavenXpp3Reader reader = new MavenXpp3Reader();

		Model model = reader.read(fis);

		String artifactId = model.getArtifactId();

		String groupId = model.getGroupId();

		String version = model.getVersion();
		String command = "mvn install:install-file -DgroupId=" + groupId + " -DartifactId=" + artifactId + " -Dversion=" + version + " -Dpackaging=jar -Dfile="+jarPath;
		return command;
	}

}