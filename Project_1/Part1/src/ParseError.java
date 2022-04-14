public class ParseError extends Exception {
    public static final String ANSI_RED = "\033[1;91m";
    public static final String ANSI_RESET = "\u001B[0m";

    public String getMessage() {
	    return (ANSI_RED + "Parse Error!" + ANSI_RESET);
    }
}