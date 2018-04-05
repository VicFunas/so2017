package motor;

import java.util.Scanner;

public class Evento {
	
	int tempo;
	int intervalo;
	String tipo;
	
	public Evento(String type, int tempo) {
		this.tipo = type;
		this.SetDelta(this.tipo);
		this.setTempo(tempo);
	}
	
	public void SetDelta(String mode) {
		switch (mode) {
		case "I":
			this.tempo = 0;
			this.intervalo = 4;
			break;
		case "B":
		case "F":
			this.intervalo = 0;
			break;
		case "L":
			this.intervalo = 20;
			break;
		case "E":
			this.intervalo = 32;
			break;
		case "T":
			this.intervalo = 42;
			break;
		default:
			break;
		}
	}
	
	public void setTempo(int tempo) {
		this.tempo = tempo + this.intervalo;
	}
	
	public int timeJump() {
		int now;
		if (this.tipo.equals('B')) {
			System.out.print("Qual o instante da interrupcao? ");
		}
		else {
			System.out.print("Qual o instante do evento? ");
		}
		
		Scanner scan = new Scanner(System.in);
		now = scan.nextInt();
		System.out.println("\n");
		return now;
	}

	public String lista() {
		String type;
		System.out.println("I - inicio");
		System.out.println("F - finale");
		System.out.println("L - carga");
		System.out.println("E - executa");
		System.out.print("Qual evento queres? ");
		Scanner scan = new Scanner(System.in);
		type = scan.nextLine();
		return type;
	}
	
	public void change() {
		this.tipo = this.lista();
		this.tempo = this.timeJump();
		this.SetDelta(this.tipo);
	}
	
	
}
