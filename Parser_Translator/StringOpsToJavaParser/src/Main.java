public class Main{
	public static void main(String[] args){
		System.out.println(findLangType("Java"));
		System.out.println(findLangType("Javascript"));
		System.out.println(findLangType("Typescript"));
	}

	public static String findLangType(String langName){
		 return (( langName ).startsWith( "Java" )?(( "Java" ).startsWith( langName )?"Static":(( (new StringBuilder(langName).reverse().toString()) ).startsWith( (new StringBuilder("script").reverse().toString()) )?"Dynamic":"Unknown")):(( (new StringBuilder(langName).reverse().toString()) ).startsWith( (new StringBuilder("script").reverse().toString()) )?"Probably Dynamic":"Unknown"));
	}
}
