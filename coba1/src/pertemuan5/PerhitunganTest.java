package pertemuan5;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PerhitunganTest {
	
	Perhitungan p;
	
	@Before
	public void awal(){
		System.out.println("Mulai Test");
		p = new Perhitungan();
	}

	@Test
	public void test1(){
		assertEquals("Harusnya",5,p.pertambahan5(0));
	}
	
	@Test
	public void test2(){
		assertEquals("Harusnya",10,p.pertambahan5(5));
	}
	
	@Test
	public void test3(){
		assertEquals("Harusnya",5,p.pertambahan5(15));
	}
	
	@Test
	public void test4(){
		assertEquals("Harusnya",0,p.perkalian5(0));
	}
	
	@Test
	public void test5(){
		assertEquals("Harusnya",25,p.perkalian5(5));
	}
	
	@Test
	public void test6(){
		assertEquals("Harusnya",5,p.perkalian5(15));
	}
	
	@After
	public void akhir(){
		System.out.println("Akhir Test");
	}
	
}
