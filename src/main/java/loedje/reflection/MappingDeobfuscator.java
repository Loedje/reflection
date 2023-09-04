package loedje.reflection;

import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyTree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MappingDeobfuscator {
	private static final Map<String, String> intermediaryToNamedMethod = new HashMap<>();
	private static final Map<String, ClassDef> namedToClassDef = new HashMap<>();
	private static final Map<String, String> intermediaryToNamedField = new HashMap<>();
	public static final String INTERMEDIARY = "intermediary";
	public static final String NAMED = "named";
	private MappingDeobfuscator() {
		throw new AssertionError("This class should not be instantiated.");
	}

	public static void initializeMappings() {
		TinyTree mappings = YarnMappings.getMappings();
		Collection<ClassDef> classes = mappings.getClasses();
		classes.forEach(classDef -> {
			getNamedToClassDef().put(classDef.getName(NAMED).replace('/', '.'), classDef);
			classDef.getMethods().forEach(methodDef ->
					getIntermediaryToNamedMethod().put(
							methodDef.getName(INTERMEDIARY),
							methodDef.getName(NAMED)
					));
			classDef.getFields().forEach(fieldDef ->
					getIntermediaryToNamedField().put(
							fieldDef.getName(INTERMEDIARY),
							fieldDef.getName(NAMED)
					));

		});
	}

	public static Map<String, String> getIntermediaryToNamedMethod() {
		return intermediaryToNamedMethod;
	}

	public static Map<String, ClassDef> getNamedToClassDef() {
		return namedToClassDef;
	}

	public static Map<String, String> getIntermediaryToNamedField() {
		return intermediaryToNamedField;
	}
}
