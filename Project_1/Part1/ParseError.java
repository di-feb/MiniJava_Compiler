public class ParseError extends Exception {
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";

    public String getMessage() {
	    return (ANSI_RED + "Parse Error!" + ANSI_RESET);
    }
}