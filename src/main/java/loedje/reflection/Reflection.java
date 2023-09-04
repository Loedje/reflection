package loedje.reflection;

import loedje.reflection.commands.JavaReflectionCommand;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reflection implements ModInitializer {
	public static final String MOD_ID = "reflection";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		MappingDeobfuscator.initializeMappings();
		JavaReflectionCommand.register();
		JavaReflectionCommand.presetKeys();
	}


}
