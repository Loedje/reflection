package loedje.reflection.commands;

import loedje.reflection.YarnMappings;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyTree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Deobfuscator {
	protected static final Map<String, String> intermediaryToNamedMethod = new HashMap<>();
	protected static final Map<String, ClassDef> namedToClassDef = new HashMap<>();
	protected static final Map<String, String> intermediaryToNamedField = new HashMap<>();
	public static final String INTERMEDIARY = "intermediary";
	public static final String NAMED = "named";
	private static final boolean DEBUG = false;

	public static void mapper() {
		TinyTree mappings = YarnMappings.mappings;
		Collection<ClassDef> classes = mappings.getClasses();
		classes.forEach(classDef -> {
			namedToClassDef.put(classDef.getName(NAMED).replace('/', '.'), classDef);
			classDef.getMethods().forEach(methodDef ->
					intermediaryToNamedMethod.put(
							methodDef.getName(INTERMEDIARY),
							methodDef.getName(NAMED)
					));
			classDef.getFields().forEach(fieldDef ->
					intermediaryToNamedField.put(
							fieldDef.getName(INTERMEDIARY),
							fieldDef.getName(NAMED)
					));

		});

		if (DEBUG) {
			intermediaryToNamedField.clear();
			intermediaryToNamedMethod.clear();
			classes.forEach(classDef -> {

				classDef.getMethods().forEach(methodDef ->
						intermediaryToNamedMethod.put(
								methodDef.getName(NAMED),
								methodDef.getName(NAMED)
						));

				classDef.getFields().forEach(fieldDef ->
						intermediaryToNamedField.put(
								fieldDef.getName(NAMED),
								fieldDef.getName(NAMED)
						));

			});

		}
	}
}
