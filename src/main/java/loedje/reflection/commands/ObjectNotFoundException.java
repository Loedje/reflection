package loedje.reflection.commands;

public class ObjectNotFoundException extends Exception {

	public ObjectNotFoundException() {
		super("Object not found for the specified key.");
	}

	public ObjectNotFoundException(String message) {
		super(message);
	}
}
