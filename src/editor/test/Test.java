package editor.test;

public class Test {
	public static void main(String[]args){
		String str = "31.12.1999";
		String[]items = str.split("\\.");
		System.out.println(items[0]);
		System.out.println(items[1]);
		System.out.println(items[2]);
	}

}
