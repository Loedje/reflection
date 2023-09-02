package loedje.reflection.commands;

import loedje.reflection.Reflection;
import loedje.reflection.YarnMappings;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyTree;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Deobfuscator {
	protected static final Map<String, String> intermediaryToNamedMethod = new HashMap<>();
	protected static final Map<String, ClassDef> namedToClassDef = new HashMap<>();
	public static final String INTERMEDIARY = "intermediary";
	public static final String NAMED = "named";
	private static final boolean DEBUG = true;

	public static void mapper() {


		TinyTree mappings = YarnMappings.mappings;
		Collection<ClassDef> classes = mappings.getClasses();
		classes.forEach(classDef -> Reflection.LOGGER.info(classDef.getName(NAMED)));
		classes.forEach(classDef -> namedToClassDef.put(classDef.getName(NAMED).replace('/', '.'), classDef));
		classes.forEach(classDef ->
				classDef.getMethods().forEach(methodDef ->
						intermediaryToNamedMethod.put(
								methodDef.getName(INTERMEDIARY),
								methodDef.getName(NAMED)
						)));
		if (DEBUG) {
			classes.forEach(classDef ->
					classDef.getMethods().forEach(methodDef ->
							intermediaryToNamedMethod.put(
									methodDef.getName(NAMED),
									methodDef.getName(NAMED)
							)));
		}
	}
}
