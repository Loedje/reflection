package loedje.reflection;

import net.fabricmc.api.ModInitializer;
import loedje.reflection.commands.ClassPaths;
import loedje.reflection.commands.JavaReflectionCommand;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reflection implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "reflection";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ClassPaths.mapper(FabricLoader.getInstance().getModContainer(MOD_ID).get().findPath("class_paths").get().toFile());
		JavaReflectionCommand.register();


	}


}
