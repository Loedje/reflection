package loedje.reflection;

import loedje.reflection.commands.Deobfuscator;
import loedje.reflection.commands.JavaReflectionCommand;
import net.fabricmc.api.ModInitializer;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Reflection implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "reflection";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Map<String, String> INTERMEDIARY_NAMED_MAP = new HashMap<>();

	@Override
	public void onInitialize() {
		LOGGER.info(OkHttp.VERSION);
		YarnMappings.start();
		Deobfuscator.mapper();
		JavaReflectionCommand.register();
	}


}
