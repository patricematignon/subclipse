package pertemuan5;

public class Perhitungan {

	public int pertambahan5(int angka){
		if(angka <=0){
			return angka;
		}else if(angka >=10){
			return angka -10;
		}else{
			return angka *5;
		}
	}
	
	public int perkalian5(int angka){
		if(angka <=0){
			return angka;
		}else if(angka >=10){
			return angka - 10;
		}else{
			return angka +5;
		}
	}
	public static void main(String[]args){
		Perhitungan p = new Perhitungan();
		System.out.println(p.pertambahan5(5));
	}
}
