package loedje.reflection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class YarnMappings {
	private YarnMappings() {
		throw new AssertionError("This class should not be instantiated.");
	}
	public static TinyTree getMappings() {
		try {
			String latestYarnVersion = getLatestYarnVersion();
			Path yarnJarPath = downloadYarnJar(latestYarnVersion);
			return extractMappings(yarnJarPath);
		} catch (IOException e) {
			throw new RuntimeException("Error processing Yarn: " + e.getMessage(), e);
		}

	}

	private static String getLatestYarnVersion() throws IOException {
		String yarnVersionsUrl = "https://meta.fabricmc.net/v2/versions/yarn/1.20.1";

		Request request = new Request.Builder()
				.url(yarnVersionsUrl)
				.build();

		try (Response response = new OkHttpClient().newCall(request).execute()) {
			String responseBody = response.body().string();
			JsonArray versionArray = JsonParser.parseString(responseBody).getAsJsonArray();
			JsonObject latestVersionObject = versionArray.get(0).getAsJsonObject();
			return latestVersionObject.get("version").getAsString();
		}
	}

	private static Path downloadYarnJar(String version) throws IOException {
		String versionUrl = "https://maven.fabricmc.net/net/fabricmc/yarn/" +
				version + "/yarn-" + version + ".jar";

		Path baseDir = FabricLoader.getInstance().getConfigDir().resolve(Reflection.MOD_ID);
		Files.createDirectories(baseDir); // Ensure the directory exists

		Path path = baseDir.resolve(version + ".jar");

		if (path.toFile().exists()) return path;
		try (var inputStream = new URL(versionUrl).openStream()) {
			Files.copy(inputStream, path);
		}

		return path;
	}

	private static TinyTree extractMappings(Path jarPath) throws IOException {
		try (JarFile jarFile = new JarFile(jarPath.toFile())) {
			ZipEntry entry = jarFile.getEntry("mappings/mappings.tiny");
			try (BufferedReader br = new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry)))) {
				return TinyMappingFactory.loadWithDetection(br);
			}
		}
	}
}
