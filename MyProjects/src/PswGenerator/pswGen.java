package PswGenerator;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class pswGen {
	
	public static String generatePsw(int laenge){
		if(0<laenge){
			String psw="";
			for(int i=0;i<laenge;i++){
			int rnd = (int)(Math.random()*94+33);
			psw = psw + (char) rnd;
			}
			return psw;
		}
		
		else{
			System.out.println("Error: length < 0");
			return null;
		}
	}
	
	public static void asci(){
		for(int i=0;i<256;i++){
			System.out.println(i + ": " + (char)i);
		}
	}
	
	public static void main(String[] args){
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String txt="";
		try{
			do{
				txt="";
			System.out.println("Enter psw_length (or type \"exit\"): ");
			String s = in.readLine();
			txt = s;
			int i = Integer.parseInt(s);
			System.out.println("Generated Password: "+generatePsw(i));
			}while(!txt.equals("exit"));
		}catch(Exception e){
			System.out.println("Error:" + e);
		}
	}
	
}
