package loedje.reflection.commands;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ClassPaths {
	public static final Map<String,String> classNamePathNameMap = new HashMap<>();
	public static void mapper(File input) {
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(input))) {
			bufferedReader.readLine();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				classNamePathNameMap.put(line,bufferedReader.readLine());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
