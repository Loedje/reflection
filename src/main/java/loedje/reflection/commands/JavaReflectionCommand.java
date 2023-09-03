package loedje.reflection.commands;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static loedje.reflection.Reflection.LOGGER;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class JavaReflectionCommand {
	private static final Map<String, Object> STRING_OBJECT_MAP = new HashMap<>();
	private static final String ENTITY_STRING = "entity";
	private static final String INT_STRING = "int";
	private static final String LONG_STRING = "long";
	private static final String STRING_STRING = "string";
	private static final String BOOLEAN_STRING = "boolean";
	private static final String FLOAT_STRING = "float";
	private static final String DOUBLE_STRING = "double";
	public static final String CLASS_STRING = "Class";
	public static final String OBJECT_STRING = "object";
	public static final String FIELD_STRING = "field";
	public static final String METHOD_STRING = "method";
	public static final String KEY_STRING = "key";
	public static final String PARAMETERS_STRING = "parameter1 parameter2 ...";
	public static final String VECTOR_STRING = "vector";
	public static final String RUN_STRING = "run";
	public static final String STORED_AT = " stored at ";
	public static final String DIMENSION_STRING = "dimension";
	public static final String OBJECTIVE_STRING = "objective";
	public static final String VALUE_STRING = "value";

	private JavaReflectionCommand() {
		throw new IllegalStateException("Utility class");
	}

	private static void presetKeys() {
		for (int i = 0; i <= 100; i++) {
			STRING_OBJECT_MAP.put(Integer.toString(i), i);
			STRING_OBJECT_MAP.put(i + ".0", ((double) i));
			STRING_OBJECT_MAP.put(i + "f", ((float) i));
			STRING_OBJECT_MAP.put("true", true);
			STRING_OBJECT_MAP.put("false", false);
		}
	}
	public static void register() {
		presetKeys();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			var objectNode = literal("jr");
			// int, long, string, boolean, float, double
			// entity, dimension, objective, vector, source
			// method, cast, new, field (set and get)
			// run
			// TODO: private methods, remove cast, test new,

			var argEntity = argument(ENTITY_STRING, EntityArgumentType.entity())
					.executes(JavaReflectionCommand::entity);
			var argDimension = argument(DIMENSION_STRING, DimensionArgumentType.dimension())
					.executes(JavaReflectionCommand::dimension);
			var argObjective = argument(OBJECTIVE_STRING, ScoreboardObjectiveArgumentType.scoreboardObjective())
					.executes(JavaReflectionCommand::objective);

			var argVec3 = argument(VECTOR_STRING, Vec3ArgumentType.vec3())
					.executes(JavaReflectionCommand::vec3);
			var argInt = argument(INT_STRING, IntegerArgumentType.integer())
					.executes(JavaReflectionCommand::integer);
			var argLong = argument(LONG_STRING, LongArgumentType.longArg())
					.executes(JavaReflectionCommand::longInteger);
			var argString = argument(STRING_STRING, StringArgumentType.greedyString())
					.executes(JavaReflectionCommand::string);
			var argBoolean = argument(BOOLEAN_STRING, BoolArgumentType.bool())
					.executes(JavaReflectionCommand::bool);
			var argFloat = argument(FLOAT_STRING, FloatArgumentType.floatArg())
					.executes(JavaReflectionCommand::floatingPoint);
			var argDouble = argument(DOUBLE_STRING, DoubleArgumentType.doubleArg())
					.executes(JavaReflectionCommand::longFloat);

			var argFieldObjectSet = argument(OBJECT_STRING, StringArgumentType.word());
			var argFieldSet = argument(FIELD_STRING, StringArgumentType.word());
			var argFieldValue = argument(VALUE_STRING, StringArgumentType.greedyString())
					.executes(JavaReflectionCommand::fieldSet);

			var argFieldObjectGet = argument(OBJECT_STRING, StringArgumentType.word());
			var argFieldGet = argument(FIELD_STRING, StringArgumentType.word())
					.executes(JavaReflectionCommand::fieldGet);

			var argCastObject = argument(OBJECT_STRING, StringArgumentType.word());
			var argCastClass = argument(CLASS_STRING, StringArgumentType.word())
					.executes(JavaReflectionCommand::cast);

			var argNew = argument(CLASS_STRING, StringArgumentType.word())
					.executes(JavaReflectionCommand::constructorCommand);
			var argNewParameters = argument(PARAMETERS_STRING, StringArgumentType.greedyString())
					.executes(JavaReflectionCommand::constructorCommandWithParameters);

			var argMethodObject = argument(OBJECT_STRING, StringArgumentType.word());
			var argMethod = argument(METHOD_STRING, StringArgumentType.word())
					.executes(JavaReflectionCommand::methodCommand);
			var argMethodParameters = argument(PARAMETERS_STRING, StringArgumentType.greedyString())
					.executes(JavaReflectionCommand::methodCommandWithParameters);

			var argRun = argument(RUN_STRING, StringArgumentType.greedyString())
					.executes(JavaReflectionCommand::runCommand);



			dispatcher.register(objectNode
					.then(literal(ENTITY_STRING)
							.then(key()
									.then(argEntity)))
					.then(literal(DIMENSION_STRING)
							.then(key()
									.then(argDimension)))
					.then(literal(OBJECTIVE_STRING)
							.then(key()
									.then(argObjective)))
					.then(literal("source")
							.then(key()
									.executes(JavaReflectionCommand::source)))
					.then(literal(VECTOR_STRING)
							.then(key()
									.then(argVec3)))
					.then(literal("new")
							.then(key()
									.then(argNew
											.then(argNewParameters))))
					.then(literal("cast")
							.then(key()
									.then(argCastObject
											.then(argCastClass))))
					.then(literal(METHOD_STRING)
							.then(key()
									.then(argMethodObject
											.then(argMethod
													.then(argMethodParameters)))))
					.then(literal(FIELD_STRING)
							.then(literal("set")
									.then(argFieldObjectSet
											.then(argFieldSet
													.then(argFieldValue))))
							.then(literal("get")
									.then(key()
											.then(argFieldObjectGet
													.then(argFieldGet)))))
					.then(literal(INT_STRING)
							.then(key()
									.then(argInt)))
					.then(literal(LONG_STRING)
							.then(key()
									.then(argLong)))
					.then(literal(FLOAT_STRING)
							.then(key()
									.then(argFloat)))
					.then(literal(DOUBLE_STRING)
							.then(key()
									.then(argDouble)))
					.then(literal(STRING_STRING)
							.then(key()
									.then(argString)))
					.then(literal(BOOLEAN_STRING)
							.then(key()
									.then(argBoolean)))
					.then(literal(RUN_STRING)
							.then(argRun))
			);
		});
	}

	private static int runCommand(CommandContext<ServerCommandSource> context) {
		String input = StringArgumentType.getString(context, RUN_STRING);
		Pattern pattern = Pattern.compile("(%\\w+%)");
		Matcher matcher = pattern.matcher(input);

		StringBuilder output = new StringBuilder();
		while (matcher.find()) {
			String match = matcher.group(1);
			String replacementKey = match.substring(1, match.length() - 1); // Remove percentage signs
			String replacement = STRING_OBJECT_MAP.getOrDefault(replacementKey, match).toString();
			matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(output);
		String result = output.toString();
		context.getSource().getServer().getCommandManager().executeWithPrefix(
				context.getSource(),"/" + result);
		return 1; // Return any value indicating success
	}


	private static int constructorCommandWithParameters(CommandContext<ServerCommandSource> context) {
		return constructor(context, true);
	}

	private static int constructorCommand(CommandContext<ServerCommandSource> context) {
		return constructor(context, false);
	}

	private static int vec3(CommandContext<ServerCommandSource> context) {
		Vec3d position = Vec3ArgumentType.getVec3(context, VECTOR_STRING);
		String key = StringArgumentType.getString(context, KEY_STRING);
		STRING_OBJECT_MAP.put(key, position);
		context.getSource().sendFeedback(() -> Text.literal(position + STORED_AT + key),true);
		return 1;
	}

	private static int methodCommandWithParameters(CommandContext<ServerCommandSource> context) {
		return invokeMethod(context, true);
	}

	private static int methodCommand(CommandContext<ServerCommandSource> context) {
		return invokeMethod(context, false);
	}

	private static int fieldSet(CommandContext<ServerCommandSource> context) {
		String objectName = StringArgumentType.getString(context, OBJECT_STRING);
		String fieldName = StringArgumentType.getString(context, FIELD_STRING);
		String valueKey = StringArgumentType.getString(context, VALUE_STRING);
		Object object = STRING_OBJECT_MAP.get(objectName);
		Object value = STRING_OBJECT_MAP.get(valueKey);
		Field field = getFieldInClassHierarchy(object, fieldName);
		field.setAccessible(true);
		try {
			field.set(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		context.getSource().sendFeedback(() -> Text.literal(fieldName + " = " + value.toString()), true);
		return 1;
	}

	private static Field getFieldInClassHierarchy(Object object, String fieldName) {
		Class<?> c = object.getClass();
		while (c != null) {
			Field field;
			LOGGER.info("class: " + c.getName());
			Arrays.stream(c.getDeclaredFields()).forEach(field1 -> LOGGER.info(field1.getName()));
			field = Arrays.stream(c.getDeclaredFields()).filter(
							f -> f.getName().equals(fieldName)
									|| Deobfuscator.intermediaryToNamedField.containsKey(f.getName())
									&& Deobfuscator.intermediaryToNamedField.get(f.getName())
									.equals(fieldName))
					.findFirst().orElse(null);
			if (field != null) return field;
			c = c.getSuperclass();
		}
		return null;
	}

	private static Method getMethod(Class<?> targetClass, String targetMethodName, Class<?>... parameterTypes) {

		for (Method candidateMethod:targetClass.getMethods()) {
			if (!Deobfuscator.intermediaryToNamedMethod.containsKey(candidateMethod.getName())) continue;
			int targetParametersCount = parameterTypes.length;
			Class<?>[] candidateParameterTypes = candidateMethod.getParameterTypes();
			if (candidateParameterTypes.length == targetParametersCount &&
					Deobfuscator.intermediaryToNamedMethod.get(candidateMethod.getName()).equals(targetMethodName)) {
				int i;
				for (i = 0; i < candidateParameterTypes.length; i++) {
					if (!candidateParameterTypes[i].isAssignableFrom(parameterTypes[i])) break;
				}
				if (i == targetParametersCount) return candidateMethod;
			}
		}
		return null;
	}

	private static int fieldGet(CommandContext<ServerCommandSource> context) {
		String key = StringArgumentType.getString(context, KEY_STRING);
		String objectName = StringArgumentType.getString(context, OBJECT_STRING);
		String fieldName = StringArgumentType.getString(context, FIELD_STRING);
		Object object = STRING_OBJECT_MAP.get(objectName);
		Field field = getFieldInClassHierarchy(object, fieldName);
		field.setAccessible(true);
		try {
			Object value = field.get(object);
			STRING_OBJECT_MAP.put(key, value);
			context.getSource().sendFeedback(() -> Text.literal(value + STORED_AT + key), true);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return 1;
	}

	private static int longFloat(CommandContext<ServerCommandSource> context) {
		String key = StringArgumentType.getString(context, KEY_STRING);
		double value = DoubleArgumentType.getDouble(context, DOUBLE_STRING);
		STRING_OBJECT_MAP.put(key, value);
		context.getSource().sendFeedback(() -> Text.literal(value + STORED_AT + key),true);
		return 1;
	}

	private static int floatingPoint(CommandContext<ServerCommandSource> context) {
		String key = StringArgumentType.getString(context, KEY_STRING);
		float value = FloatArgumentType.getFloat(context, FLOAT_STRING);
		STRING_OBJECT_MAP.put(key, value);
		context.getSource().sendFeedback(() -> Text.literal(value + STORED_AT + key),true);
		return 1;
	}

	private static int bool(CommandContext<ServerCommandSource> context) {
		String key = StringArgumentType.getString(context, KEY_STRING);
		boolean value = BoolArgumentType.getBool(context, BOOLEAN_STRING);
		STRING_OBJECT_MAP.put(key, value);
		context.getSource().sendFeedback(() -> Text.literal(value + STORED_AT + key),true);
		return 1;
	}

	private static int string(CommandContext<ServerCommandSource> context) {
		String key = StringArgumentType.getString(context, KEY_STRING);
		String value = StringArgumentType.getString(context, STRING_STRING);
		STRING_OBJECT_MAP.put(key, value);
		context.getSource().sendFeedback(() -> Text.literal(value + STORED_AT + key),true);
		return 1;
	}

	private static int longInteger(CommandContext<ServerCommandSource> context) {
		String key = StringArgumentType.getString(context, KEY_STRING);
		long value = LongArgumentType.getLong(context, LONG_STRING);
		STRING_OBJECT_MAP.put(key, value);
		context.getSource().sendFeedback(() -> Text.literal(value + STORED_AT + key),true);
		return 1;
	}

	private static int integer(CommandContext<ServerCommandSource> context) {
		String key = StringArgumentType.getString(context, KEY_STRING);
		int value = IntegerArgumentType.getInteger(context, INT_STRING);
		STRING_OBJECT_MAP.put(key, value);
		context.getSource().sendFeedback(() -> Text.literal(value + STORED_AT + key),true);
		return 1;
	}


	private static RequiredArgumentBuilder<ServerCommandSource, String> key() {
		return argument(KEY_STRING, StringArgumentType.word());
	}

	private static int cast(CommandContext<ServerCommandSource> context) {
		String key = StringArgumentType.getString(context, KEY_STRING);
		String object = StringArgumentType.getString(context, OBJECT_STRING);
		String className = StringArgumentType.getString(context, CLASS_STRING);
		try {
			Object result = getClass(className).cast(STRING_OBJECT_MAP.get(object));
			STRING_OBJECT_MAP.put(key, result);
			context.getSource().sendFeedback(() -> Text.literal(result.toString() + STORED_AT + key),true);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return 1;
	}

	private static int entity(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		Entity target = EntityArgumentType.getEntity(context, ENTITY_STRING);
		String key = StringArgumentType.getString(context, KEY_STRING);
		STRING_OBJECT_MAP.put(key, target);
		context.getSource().sendFeedback(() -> Text.literal(target.getUuidAsString() + STORED_AT + key),true);
		return 1;
	}

	private static int dimension(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerWorld dimension = DimensionArgumentType.getDimensionArgument(context, DIMENSION_STRING);
		String key = StringArgumentType.getString(context, KEY_STRING);
		STRING_OBJECT_MAP.put(key, dimension);
		context.getSource().sendFeedback(() -> Text.literal(dimension + STORED_AT + key),true);
		return 1;
	}

	private static int objective(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ScoreboardObjective objective = ScoreboardObjectiveArgumentType.getObjective(context, OBJECTIVE_STRING);
		String key = StringArgumentType.getString(context, KEY_STRING);
		STRING_OBJECT_MAP.put(key, objective);
		context.getSource().sendFeedback(() -> Text.literal(objective + STORED_AT + key),true);
		return 1;
	}

	private static int source(CommandContext<ServerCommandSource> context) {
		String key = StringArgumentType.getString(context, KEY_STRING);
		STRING_OBJECT_MAP.put(key, context.getSource());
		context.getSource().sendFeedback(() -> Text.literal("Server command source" + STORED_AT + key),true);
		return 1;
	}

	private static int constructor(CommandContext<ServerCommandSource> context, boolean hasParameters) {
		String className = StringArgumentType.getString(context,CLASS_STRING);
		List<Object> parameters = new ArrayList<>();
		if (hasParameters) {
			Arrays.stream(StringArgumentType.getString(context, PARAMETERS_STRING).split(" "))
					.forEach(s -> parameters.add(STRING_OBJECT_MAP.get(s)));
		}
		try {
			Class<?>[] types = new Class[parameters.size()];
			for (int i = 0; i < parameters.size(); i++) {
				Class<?> c = parameters.get(i).getClass();
				Field[] fields = c.getFields();
				List<String> list = new LinkedList<>();
				Arrays.stream(fields).forEach(field -> list.add(field.getName()));
				types[i] = (list.contains("TYPE") ? (Class<?>) fields[list.indexOf("TYPE")].get(c) : c);
			}
			Class<?> aClass = getClass(className);
			Constructor<?> constructor = types.length == 0 ? aClass.getConstructor() :
					getConstructor(aClass, types);
			if (constructor == null) throw new NoSuchMethodException();
			Object result = constructor.newInstance(parameters);
			String key = StringArgumentType.getString(context, KEY_STRING);
			STRING_OBJECT_MAP.put(key, result);
			context.getSource().sendFeedback(() -> Text.literal(result + STORED_AT + key),true);
		} catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException |
				 InstantiationException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		return 1;
	}



	private static int invokeMethod(CommandContext<ServerCommandSource> context, boolean hasParameters) {
		String methodName = StringArgumentType.getString(context, METHOD_STRING);
		String objectKey = StringArgumentType.getString(context, OBJECT_STRING);
		List<Object> parameters = new ArrayList<>();
		if (hasParameters) {
			Arrays.stream(StringArgumentType.getString(context, PARAMETERS_STRING).split(" "))
					.forEach(s -> parameters.add(STRING_OBJECT_MAP.get(s)));
		}
		try {
			Class<?>[] types = new Class[parameters.size()];
			for (int i = 0; i < parameters.size(); i++) {
				Class<?> c = parameters.get(i).getClass();
				Field[] fields = c.getFields();
				List<String> list = new LinkedList<>();
				Arrays.stream(fields).forEach(field -> list.add(field.getName()));
				types[i] = (list.contains("TYPE") ? (Class<?>) fields[list.indexOf("TYPE")].get(c) : c);
			}
			Object obj = JavaReflectionCommand.STRING_OBJECT_MAP.get(objectKey);
			Method method = getMethod(obj.getClass(),methodName,types);

			if (method == null) throw new NoSuchMethodException(methodName);
			Object result = method.invoke(obj, parameters.toArray());
			if (result != null) {
				String key = StringArgumentType.getString(context, KEY_STRING);
				JavaReflectionCommand.STRING_OBJECT_MAP.put(key, result);
				context.getSource().sendFeedback(() -> Text.literal(result + STORED_AT + key),true);
			}
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return 1;
	}
	private static Constructor<?> getConstructor(Class<?> aClass, Class<?>... parameterTypes) {
		for (Constructor<?> constructor:aClass.getConstructors()) {
			Class<?>[] types;
			int parametersCount = parameterTypes.length;
			types = constructor.getParameterTypes();
			if (types.length == parametersCount) {
				for (int i = 0; i < types.length; i++) {
					if (!types[i].isAssignableFrom(parameterTypes[i])) continue;
					if (i == parametersCount - 1) return constructor;
				}
			}
		}
		return null;
	}

	private static Class<?> getClass(String targetClassName) throws ClassNotFoundException {
		return Class.forName(
				Deobfuscator.namedToClassDef.get(targetClassName).getName(Deobfuscator.INTERMEDIARY).replace('/', '.'));
	}
}
