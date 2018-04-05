package motor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.ArrayList;

public class MotorEventos {
	
	String[][] m; // vetor que simula a memória
	String[][] disk; // matriz que simula o disco da maquina
	int pc; // contador de instrucoes
	int acc; // acumulador
	boolean finish; // indicador de fim de simulacao
	boolean step; // indicador do passo-a-passo
	boolean indireto = false;
	boolean stop; // indicador de existencia de um breakpoint
	ArrayList<String> instrucao; // estrutura temporaria na qual sao carregadas as instrucoes (anterior a a memoria)
	ArrayList<Evento> listaEventos; // lista de eventos
	ArrayList<Programa> attackFunction;
	int thread;
	String blocoAtual; // armazena o bloco atual do disco que está na memória
	char instrucaoAtual;
	boolean instrucaoLida;
	String file;

	
	public MotorEventos() {
		this.m = new String[256][16];
		this.disk = new String[65536][16]; // 64kB x 16B
		this.instrucao = new ArrayList<String>();
		this.listaEventos = new ArrayList<Evento>();
		this.attackFunction = new ArrayList<Programa>();
		this.finish = false;
		this.step = false;
		this.stop = false;
		this.thread = 0;
		this.blocoAtual = "";
		this.instrucaoLida = false;
	}

	
	
	public int menu(boolean step, boolean stop) {
		int o = 0;
		System.out.println("Bem vinde mestre!");
		System.out.println("0) carregar lista inicial de eventos");
		if (!stop) { 
			System.out.println("1) adicionar interrupcao");
		} else {
			System.out.println("1) remover interrupcao");
		}
		if (!step) { 
			System.out.println("2) ligar o passo-a-passo");
		} else {
			System.out.println("2) desligar o passo-a-passo");
		}
		System.out.println("3) adicionar evento");
		System.out.println("4) iniciar a simulação");
		System.out.print("O que desejas fazer? ");
		Scanner scan = new Scanner(System.in);
		o = scan.nextInt();
		System.out.println("\n");
		return o;
	}

	
	
	// menu interno ao passo-a-passo
	public void subMenu() {
		int o = 0;
		if (!stop) {
			System.out.println("4) adicionar interrupcao");
		} else {
			System.out.println("4) remover interrupcao");
		}
		System.out.println("5) desligar o passo-a-passo");
		System.out.println("Manter");
		System.out.print("O que desejas fazer? ");
		Scanner scan = new Scanner(System.in);
		o = scan.nextInt();
		System.out.println("\n");
		switch (o) {
		case 4:
			Evento kabuki = new Evento("B", 0);
			if (!this.stop) {
				int now;
				System.out.print("Qual o instante da interrupcao? ");
				Scanner scanner = new Scanner(System.in);
				now = scanner.nextInt();
				this.addEvento("B", now);
			} else {
				this.removeEvento("B");
			}
			this.stop = !this.stop;
			break;
		case 5:
			this.step = !this.step;
			break;
		default:
			break;
		}
		
	}

	
	
	// menu apresentado ao se atingir um breakpoint
	public int breakMenu() {
		int o = 0;
		System.out.println("1) reiniciar");
		System.out.println("2) continuar simulacao");
		System.out.println("3) encerrar simulacao");
		if (!this.step) {
			System.out.println("4) ligar o passo-a-passo");
		} else {
			System.out.println("4) desligar o passo-a-passo");
		}
		System.out.print("O que desejas fazer? ");
		Scanner scan = new Scanner(System.in);
		o = scan.nextInt();
		System.out.println("\n");
		return o;
	}

	
	
	// imprime a matriz de memoria
	public void getM() {
		System.out.print("\n");
		for (int i = 0; i < 32; i++) { // this.m.length
			System.out.print(Integer.toHexString(0x100 | i).substring(1).toUpperCase()+"    ");
			for (int j = 0; j < 16; j++) { //this.m[i].length
				System.out.print("  " +String.format("%2S", this.m[i][j]).replace(' ', '0'));
			}
			System.out.print("\n");
		}
		System.out.print("\n");
	}

	
	
	// inicializa a memoria
	public void setM() {
		for (int i = 0; i < this.m.length; i++) {
			for (int j = 0; j < this.m[i].length; j++) {
				this.m[i][j] = "00";
			}
		}
	}

	
	
	// inicializa o disco
	public void setDisk() {
		for (int i = 0; i < this.disk.length; i++) {
			for (int j = 0; j < this.disk[i].length; j++) {
				this.disk[i][j] = "..";
			}
		}
		for (int i = 0; i < this.m.length; i++) {
			for (int j = 0; j < this.m[i].length; j++) {
				this.disk[256+i][j] = "**";
			}
		}
	}
	
	
	
	// imprime o disco
	public void getDisk() {
		System.out.print("\n");
		for (int i = 0; i < 32; i++) { // this.m.length
			System.out.print(Integer.toHexString(0x1000 | i).substring(1).toUpperCase()+"    ");
			for (int j = 0; j < 16; j++) { //this.m[i].length
				System.out.print("  " +String.format("%2S", this.disk[i][j]).replace(' ', '0'));
			}
			System.out.print("\n");
		}
		System.out.print("\n");
		for (int i = 256; i < 288; i++) { // this.m.length
			System.out.print(Integer.toHexString(0x1000 | i).substring(1).toUpperCase()+"    ");
			for (int j = 0; j < 16; j++) { //this.m[i].length
				System.out.print("  " +String.format("%2S", this.disk[i][j]).replace(' ', '0'));
			}
			System.out.print("\n");
		}
		/*
		System.out.print("\n");
		for (int i = 512; i < 544; i++) { // this.m.length
			System.out.print(Integer.toHexString(0x1000 | i).substring(1).toUpperCase()+"    ");
			for (int j = 0; j < 16; j++) { //this.m[i].length
				System.out.print("  " +String.format("%2S", this.disk[i][j]).replace(' ', '0'));
			}
			System.out.print("\n");
		}
		*/
	}
	
	
	
	// inicializa as variaveis
	public void start() {
		this.setM();
		this.setDisk();
		this.pc = 0;
		this.acc = 0;
		this.finish = false;
		this.blocoAtual = "0";
	}

	
	
	// adiciona um evento a lista de eventos
	public void addEvento(String tipo, int tempoAtual) {
		boolean valido = false;
		Evento event = new Evento(tipo, tempoAtual);
		int posicao = 0;
		while (!valido && posicao<this.listaEventos.size()) {
//			System.out.println("tipo = "+this.listaEventos.get(0).tipo);
//			System.out.println("size = "+this.listaEventos.size());
			if (tempoAtual+event.tempo<this.listaEventos.get(posicao).tempo) {
				this.listaEventos.add(posicao, event);
				valido = true;
//				System.out.println("tipo = "+this.listaEventos.get(0).tipo);
//				System.out.println("size = "+this.listaEventos.size());
			}
			else {
				posicao = posicao + 1;
			}
		}
	}

	
	
	// tira um evento da lista de eventos
	public void removeEvento(String tipo) {
		int i = 0;
		while (tipo != this.listaEventos.get(i).tipo) {
			i = i + 1;
		}
		this.listaEventos.remove(i);
	}

	
	
	// traduz o evento do motor de simulacao	
	public void interpretaEvento(int tempo) {
		String type = this.listaEventos.get(0).tipo;
		switch (type) {
		
		case "I": // início
			this.start();
			this.listaEventos.remove(0);
			break;
			
		case "L": // loader
			this.loadProgram();
			this.listaEventos.remove(0);
			break;	
			
		case "E": // execution
			String pcS = Integer.toString(0x1000 | this.pc, 16).toUpperCase().substring(1);
//			System.out.println("pcS = "+pcS);
			String inhai = pcS.substring(0, 2);
			String lumi = pcS.substring(2);
			String acao;
			try {
				acao = this.m[Integer.parseInt(inhai, 16)][Integer.parseInt(lumi, 16)]+this.m[Integer.parseInt(inhai, 16)][Integer.parseInt(lumi, 16)+1];
			} catch (ArrayIndexOutOfBoundsException e) {
				// TODO: handle exception
				acao = this.m[Integer.parseInt(inhai, 16)][Integer.parseInt(lumi, 16)]+this.m[Integer.parseInt(inhai, 16)+1][0];
			}
			
//			System.out.println("Luminous = "+this.m[Integer.parseInt(inhai, 16)][Integer.parseInt(lumi, 16)]);
//			System.out.println("Heartiel = "+this.m[Integer.parseInt(inhai, 16)][Integer.parseInt(lumi, 16)+1]);
//			System.out.println("action = "+acao);
			this.executa(acao);
			this.listaEventos.remove(0);
			break;	
			
		case "F": // finale
			System.out.println("Das Ende!");
			this.listaEventos.clear();
			this.getM();
			System.out.println("acc = "+Integer.toString(this.acc, 16));
			break;	
			
		case "B": // break
			System.out.println("\n"+"Chaos Break!");
			int ac = this.breakMenu();
			while (ac == 4) {
				this.step = !this.step;
				ac = this.breakMenu();
			}
			if (ac == 3) {
				this.listaEventos.clear();
				Evento finale = new Evento("F", tempo);
				this.listaEventos.add(finale);
			} else if (ac == 1) {
				this.listaEventos.clear();
				this.comeco();
			} else {
				this.listaEventos.remove(0);
			}
			break;
			
		case "T": // troca bloco
			this.getM();
			this.getDisk();
			this.troca();
			this.getM();
			this.getDisk();
			this.listaEventos.remove(0);
			break;
		
		default:
			System.out.println("Evento desconhecido");
			break;
		}
	}
	

	
	// carrega um programa na memoria
	private void loadProgram() {
		Programa charge = new Programa();
		System.out.print("Digite o nome do arquivo: ");
        Scanner scan = new Scanner(System.in);
        String directory = scan.nextLine();
        
        try (BufferedReader br = new BufferedReader(new FileReader(directory)))
        {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
            	this.instrucao.add(sCurrentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 	
        
		String instrucao = "";
		String linha = "";
		String coluna = "";
		String pagina = "";
		String blocoDisco = "";
		ArrayList<String[]> tabela = new ArrayList<String[]>();
		
		String[] map = new String[3];
		String fisico = String.format("%1S",Integer.toHexString(this.attackFunction.size()));
		int pImpar = 0;
		fisico += this.instrucao.get(0).substring(1, 3);
		pagina = this.instrucao.get(0).substring(0, 1);
//		System.out.println("pafina" +pagina);
		charge.setPc(Integer.parseInt(fisico, 16));
		
//		this.pc = Integer.parseInt(fisico, 16);
		for (int i = 1; this.instrucao.size() > i; i++) {
			fisico = fisico.substring(0, 1)+this.instrucao.get(i).substring(1, 3);
			instrucao = this.instrucao.get(i).substring(4,8);
			linha = fisico.substring(0, 2);
			coluna = fisico.substring(2, 3);
			charge.setEnderecoLogico(this.instrucao.get(i).substring(0, 3));
//			System.out.println("page" +this.instrucao.get(0).substring(0, 1));
			if (this.instrucao.get(i).substring(0, 1).equals(pagina)) {
				charge.setEnderecoFisico(fisico);
			} else {
				charge.setEnderecoFisico(null);
			}
			blocoDisco = this.instrucao.get(i).substring(0, 1);
//			System.out.println("bloco " +blocoDisco);
			
			charge.setCodeInstrucao(instrucao);
			charge.setBlocoDisco(blocoDisco);
			
			try {
				pImpar = Integer.parseInt(coluna, 16)+1;
				if (this.instrucao.get(i).substring(0, 1).equals(pagina)) {
					this.m[Integer.parseInt(linha, 16)][Integer.parseInt(coluna, 16)] = instrucao.substring(0, 2);
					this.m[Integer.parseInt(linha, 16)][pImpar] = instrucao.substring(2, 4);
				}
//				System.out.println("linha " +linha);
				linha = blocoDisco+linha;
//				System.out.println("linha " +linha);
//				System.out.println("coluna " +coluna);
				this.disk[Integer.parseInt(linha, 16)][Integer.parseInt(coluna, 16)] = instrucao.substring(0, 2);
				this.disk[Integer.parseInt(linha, 16)][pImpar] = instrucao.substring(2, 4);
			} catch (ArrayIndexOutOfBoundsException e) {
				// TODO: handle exception
				pImpar = 0;
				this.m[Integer.parseInt(linha, 16)+1][0] = instrucao.substring(0, 2);
				this.m[Integer.parseInt(linha, 16)+1][1] = instrucao.substring(2, 4);
				linha = blocoDisco+linha;
				this.disk[Integer.parseInt(linha, 16)+1][0] = instrucao.substring(0, 2);
				this.disk[Integer.parseInt(linha, 16)+1][1] = instrucao.substring(2, 4);
			}			
		}
		this.attackFunction.add(charge);
		this.instrucao.clear();
		this.pc = this.attackFunction.get(0).getPc();
		

	}
	
	

	//execucao das instrucoes da MVN	
	public void executa(String acao) { 
		// Separa o primeiro char do resto da string
		char instrucao;
		if(this.instrucaoLida) {
			instrucao = this.instrucaoAtual;
		}
		else instrucao = acao.charAt(0);
//		System.out.println("acao = "+acao);
		String operando = "";
		if(this.blocoAtual != this.attackFunction.get(0).blocoDisco.get(this.attackFunction.get(0).getTableIndexByInstruction(acao))) {
			Evento t = new Evento("T", this.listaEventos.get(0).tempo+1);
			Evento e = new Evento("E", this.listaEventos.get(0).tempo+2);
			this.listaEventos.add(t);
			this.listaEventos.add(e);
			this.instrucaoAtual = instrucao;
			this.instrucaoLida = true;
			return;
		}
		else this.instrucaoLida = false;
		if (((instrucao != 'D') && (instrucao != 'B') && (instrucao != 'E') && (instrucao != 'C')) && !this.indireto) {
			System.out.println("operando = "+acao.substring(1));
			operando = String.format("%3S", this.attackFunction.get(0).getFisico(acao.substring(1))).replace(' ', '0');
			
//			System.out.println("operando = "+operando);
		}
		else {
			operando = (acao.substring(1));
		}
//		System.out.println("local = "+Integer.toString(0x10000 | this.pc, 16).substring(1));
		
		int linha = Integer.parseInt(operando.substring(0, 2), 16);
		int coluna = Integer.parseInt(operando.substring(2), 16);
//		this.getM();
		String data;
		pointer ponteiro;
		
		switch(instrucao) {
		
		case '0': // Jump Unconditional
			
			if (this.indireto) {
				ponteiro = this.Ind();
			} else {
				this.pc = Integer.parseInt(operando, 16);
			}
			break;
		
		case '1': // Jump if Zero
			if (this.indireto) {
				ponteiro = this.Ind();
				this.pc = ponteiro.getEndereco();
				this.indireto = false;
			} else {
				if (this.acc == 0) {
					this.pc = Integer.parseInt(operando, 16);
				}
				else this.pc = this.pc+2;
			}
			break;
		
		case '2': // Jump if Negative
			if (this.indireto) {
				ponteiro = this.Ind();
				this.pc = ponteiro.getEndereco();
				this.indireto = false;
			} else {
				if (this.acc < 0) {
					this.pc = Integer.parseInt(operando, 16);
					
				}
				else this.pc = this.pc+2;
			}
			break;
		
		case '3': // Carrega valor
			if (this.indireto) {
				ponteiro = this.Ind();
				this.pc = ponteiro.getEndereco();
				this.indireto = false;
			} else {
				this.acc = Integer.valueOf(operando, 16).shortValue();
				this.pc = this.pc+2;
			}
			break;
		
		case '4': // soma
			if (this.indireto) {
//				System.out.println("Liebe ist musik! Musik, ist liebe");
				ponteiro = this.Ind();
				this.pc = ponteiro.getEndereco();
				this.indireto = false;
			} else {
				data = this.m[linha][coluna];
				this.acc = this.acc + Integer.valueOf(data, 16).shortValue();
				this.pc = this.pc+2;
			}
			break;
		
		case '5': // subtrai
			if (this.indireto) {
				ponteiro = this.Ind();
				this.pc = ponteiro.getEndereco();
				this.indireto = false;
			} else {
				data = this.m[linha][coluna];
				this.acc = this.acc - Integer.valueOf(data, 16).shortValue();
//				System.out.println("sub = "+this.acc);
				this.pc = this.pc+2;
			}
			break;
		
		case '6': // multiplica
			if (this.indireto) {
				ponteiro = this.Ind();
				this.pc = ponteiro.getEndereco();
				this.indireto = false;
			} else {
				data = this.m[linha][coluna];
				this.acc = this.acc * Integer.valueOf(data, 16).shortValue();
				this.pc = this.pc+2;
			}
			break;
		
		case '7': // divide
			if (this.indireto) {
				ponteiro = this.Ind();
				this.pc = ponteiro.getEndereco();
				this.indireto = false;
			} else {
				data = this.m[linha][coluna];
				this.acc = this.acc / Integer.valueOf(data, 16).shortValue();
				this.pc = this.pc+2;
			}
			break;
		
		case '8': // carrega dado da memória
			if (this.indireto) {
				ponteiro = this.Ind();
				this.pc = ponteiro.getEndereco();
				this.indireto = false;
			} else {
				data = this.m[linha][coluna];
//				System.out.println("data ="+data);
				this.acc = Integer.valueOf(data, 16).shortValue();
				this.pc = this.pc+2;
			}
			break;
		
		case '9': // coloca dado do acumulador na memória
			if (this.indireto) {
				ponteiro = this.Ind();
				this.pc = ponteiro.getEndereco();
				this.indireto = false;
			} else {
				String toPad = Integer.toString(this.acc, 16).toUpperCase();
				String datum = String.format("%2S", toPad).replace(' ', '0');
//				System.out.println("DAta = " + datum.substring(datum.length()-2));
				this.m[linha][coluna] = datum.substring(datum.length()-2);
				//if (linha == 196 && coluna == 0) System.out.println("checksum = "+this.m[linha][coluna]);
				this.pc = this.pc+2;
			}
			break;
		
		case 'A': // chamada de subrotina
			if (this.indireto) {
				ponteiro = this.Ind();
				this.pc = ponteiro.getEndereco();
				this.indireto = false;
			} else {
				this.pc = this.pc + 2;
				String address = Integer.toHexString(0x10000 | this.pc).substring(1);
				System.out.println("Address = " + address);
				//this.m[linha][coluna]
				this.m[linha][coluna] = address.substring(0, 2);
				try {
					this.m[linha][coluna+1] = address.substring(2);
				} catch (ArrayIndexOutOfBoundsException e) { // caso seja a ultima coluna (memoria é uma matriz 256x16)
					// TODO: handle exception
					this.m[linha+1][coluna] = address.substring(2);
				}
				this.pc = Integer.parseInt(operando, 16) + 2;
			}
			break;
		
		case 'B': // modo de endereçamento this.indireto
			if (this.indireto) {
				ponteiro = this.Ind();
				this.pc = ponteiro.getEndereco();
				this.indireto = false;
			} else {
				this.indireto = true;
				this.pc += 2;
			}
			break;
		
		case 'C': // para tudo
			if (this.indireto) {
				ponteiro = this.Ind();
				this.pc = ponteiro.getEndereco();
				this.indireto = false;
			} else {
				this.attackFunction.remove(0);
				if(this.attackFunction.isEmpty()) {
					this.finish = true;
					this.pc = Integer.parseInt(operando, 16);
				}
				else {
					this.pc = this.attackFunction.get(0).getPc();
				}
				
			}
			break;
		
		case 'D': // pega dado
			if (this.indireto) {
				ponteiro = this.Ind();
				this.pc = ponteiro.getEndereco();
				this.indireto = false;
			} else {
				int skip;
				char[] t = new char[8];
				if (operando.charAt(0) == '0') { // le valor do teclado
					if(operando.charAt(1) == '0') {
						System.out.print("Entre o dado: ");
						Scanner scan = new Scanner(System.in);
						this.acc = scan.nextInt();
					}
					else if (operando.charAt(1) == '1') {
						System.out.println("Digite");
						System.out.println("3 - Código binário");
						System.out.println("4 - Código hexadecimal");
						System.out.print("Qual a forma desejada: ");
						Scanner scan = new Scanner(System.in);
						this.acc = scan.nextInt();
					}
					
				}
				if (operando.charAt(0) == '1') { // le byte do teclado
					if (operando.charAt(1) == '0') {
						System.out.print("Entre o primeiro byte: ");
						Scanner scan = new Scanner(System.in);
						String tecla = scan.nextLine();
						this.acc = Integer.valueOf(tecla, 16);
					}
					else if (operando.charAt(1) == '1') {
						System.out.print("Entre o segundo byte: ");
						Scanner scan = new Scanner(System.in);
						String tecla = scan.nextLine();
						this.acc = Integer.valueOf(tecla, 16);
					}
					else if (operando.charAt(1) == '2') {
						System.out.print("Entre o tamanho total do programa (em hexadecimal): ");
						Scanner scan = new Scanner(System.in);
						String tecla = scan.nextLine();
						this.acc = Integer.valueOf(tecla, 16);
					}
				}
				else if (operando.charAt(0) == '3') { // le byte de arquivo
					skip = Integer.valueOf(operando.substring(1),16)*8;
					if (skip == 0) {
						System.out.print("Enter the directory: ");
				        Scanner scan = new Scanner(System.in);
				        this.file = scan.nextLine();
					}
			        try (BufferedReader br = new BufferedReader(new FileReader(this.file)))
			        {
			        	br.skip(skip);
			            br.read(t,0,8);
			            String bytes = String.valueOf(t);
			            this.acc = Integer.parseInt(bytes, 2);
			            //System.out.println(sCurrentLine+"\n");

			        } catch (IOException e) {
			            e.printStackTrace();
			        }
				}
				else if (operando.charAt(0) == 'F') { // le byte de arquivo
//					System.out.println("banana"+"\n");
					skip = Integer.valueOf(operando.substring(1),16)*8;
			        try (BufferedReader br = new BufferedReader(new FileReader(this.file)))
			        {
			        	br.skip(skip);
			            br.read(t,0,8);
			            String bytes = String.valueOf(t);
			            if (bytes.substring(0, 5).equals("00000")) { // ultima linha
			            	this.acc = 0;
			            }
			            else { // nao é ultima linha
			            	this.acc = 255;
			            }

			        } catch (IOException e) {
			            e.printStackTrace();
			        }
				}
				else {
					//erro
				}
				this.pc = this.pc+2;
			}
			break;
		
		case 'E': // sai dado
			if (this.indireto) {
				ponteiro = this.Ind();
				this.pc = ponteiro.getEndereco();
				this.indireto = false;
			} else {
				if (operando.charAt(0) == '1') { // imprime na tela
					System.out.println("O resultado e: " + this.acc);
				}
				else if (operando.charAt(0) == '3') { // escreve em arquivo
					BufferedWriter bw = null;
					FileWriter fw = null;
					
					if (operando.substring(1).equals("00")) {
						System.out.print("Enter the destiny file: ");
				        Scanner scan = new Scanner(System.in);
				        this.file = scan.nextLine();
					}
					
					try {

						fw = new FileWriter(this.file, true);
						bw = new BufferedWriter(fw);
						if (operando.charAt(2) == '2') { // fim de linha
							bw.write("111111");
							bw.newLine();
						}
						else if (operando.charAt(2) == '3') { // fim de programa
							bw.write("000000");
						}
						else { // dado
							String content = Integer.toString(0b100000000 | this.acc, 2).substring(1);
							bw.write(content);
						}
						
//						System.out.println("Done");

					} catch (IOException e) {

						e.printStackTrace();

					} finally {

						try {

							if (bw != null)
								bw.close();

							if (fw != null)
								fw.close();

						} catch (IOException ex) {

							ex.printStackTrace();

						}

					}
				}
				else {
					//erro
				}
				this.pc = this.pc+2;
			}
			break;
		
		case 'F': // chama OS
			if (this.indireto) {
				ponteiro = this.Ind();
				this.pc = ponteiro.getEndereco();
				this.indireto = false;
			} else {
				this.pc = this.pc+2;
			}
			break;
		
		default: // algum erro
			this.finish = true;
			System.out.println("Instrucao = "+instrucao);
			System.out.println("op = "+operando);
			System.out.println("Parada por erro! Instrucao invalida");
		}
		
	}
	
	
	
	// executa a simulacao do motor aplicado a MVN
	public void simulacao() {
		int time = 0;
		char t;
		while(!this.listaEventos.isEmpty()){
			if(time >= this.listaEventos.get(0).tempo) {
				if(this.listaEventos.get(0).tipo == "I") time = 0;
				time = time+this.listaEventos.get(0).intervalo;
				t = this.listaEventos.get(0).tipo.charAt(0);
//				System.out.println("tempo ="+time);
				this.interpretaEvento(time);
				if (this.step && !this.listaEventos.isEmpty()) {
					if (!this.attackFunction.isEmpty()) {
						System.out.println("fisico |  logic  |  value   | bloco");
						System.out.println("------ |  -----  |  -----   | -----");
						for (int j = 0; j < this.attackFunction.size(); j++) {
							for (int i = 0; i < this.attackFunction.get(j).enderecoFisico.size(); i++) {
								System.out.println(String.format("%4S",this.attackFunction.get(j).enderecoFisico.get(i))+"   |   "+this.attackFunction.get(j).enderecoLogico.get(i)+"   |   "+this.attackFunction.get(j).codeInstrucao.get(i)+"   |   "+this.attackFunction.get(j).blocoDisco.get(i));
							}
							System.out.println("------  ---------  -------  -------");
						}
						
					}
					
					System.out.println("\nMemória\n");
					this.getM();
					System.out.println("\nDisco\n");
					this.getDisk();
					System.out.print("acc = "+Integer.toString(this.acc, 16).toUpperCase());
					System.out.print("     |     ");
					System.out.println("PC = "+Integer.toString(0x1000 | this.pc, 16).toUpperCase().substring(1)+"\n");
					this.subMenu();
//					System.out.print("\n");
				}
				if (t == 'E' && !this.finish) {
//					this.getM();
					if (true) {
						this.addEvento("E", time);
					} else {
						this.addEvento("T", time);
					}
				}
				else if (t == 'B') {
					System.out.println("Tempo de parada forcada: "+time);
				}
			}
			else {
				time = time + 1;
			}
		}
	}
	
	
	
	//adiciona eventos a lista inicial de eventos conforme ordem determinada pelo usuario	
	public void comeco() {
		boolean start = false;
		int option;
		while (!start) {
			option = this.menu(this.step, this.stop);
			switch (option) {
			case 0:
				Evento owari = new Evento("F", 2147483647);
				this.listaEventos.add(owari);
				
				this.addEvento("I", 0);
				//this.addEvento("C", 4);
//				System.out.println("tipo = "+this.listaEventos.get(0).tipo);
				this.addEvento("L", 4); 
//				System.out.println("tipo = "+this.listaEventos.get(0).tipo);
//				this.addEvento("E", 24);
//				System.out.println("tipo = "+this.listaEventos.get(0).tipo);
				this.addEvento("L", 24);
//				System.out.println("tipo = "+this.listaEventos.get(0).tipo);
				this.addEvento("E", 44);
//				System.out.println("tipo = "+this.listaEventos.get(0).tipo);
 
				break;
			case 1:
				Evento kabuki = new Evento("B", 0);
				if (!this.stop) {
					int t = kabuki.timeJump();
					kabuki.tempo = t;
					this.addEvento("B",kabuki.tempo);
				} else {
					this.listaEventos.remove(kabuki);
				}
				this.stop = !this.stop;
				break;
			case 2:
				this.step = !this.step;
				break;
			case 3:
				Evento role = new Evento("B", 0);
				role.change();
				this.addEvento(role.tipo,role.tempo);
				System.out.println("size = "+this.listaEventos.size());
				break;
			case 4:
				this.simulacao();
				start = !start;
				break;
			default:
				break;
			}
		}
		
	}

	
	
	//pega o ponteiro
	protected pointer Ind() {
		
		String pcS = Integer.toString(0x1000 | this.pc, 16).toUpperCase().substring(1);
		int line = Integer.parseInt(pcS.substring(0, 2), 16);
		int column = Integer.parseInt(pcS.substring(2), 16);
		
		String relativo = this.m[line][column];
		relativo = relativo.substring(0, 1)+this.attackFunction.get(0).getBloco();
		try {
			relativo += this.m[line][column+1];
		} catch (ArrayIndexOutOfBoundsException e) { // caso seja a ultima coluna (memoria é uma matriz 256x16)
			// TODO: handle exception
			relativo += this.m[line+1][0];
		}
		pointer p = new pointer(relativo);
		return p;
	}
	

	
	// realiza a troca de blocos
	public void troca() {
		int n = 256*Integer.parseInt(this.blocoAtual, 16);
		System.out.println("\nBloco Atual = "+this.blocoAtual);
		for (int i = 0; i < this.m.length; i++) {
			for (int j = 0; j < this.m[i].length; j++) {
				this.disk[n+i][j] = this.m[i][j];
				this.m[i][j] = this.disk[256+i][j];
			}
		}
	}
	
	
	
	
	public static void main(String[] args) {
		
		MotorEventos MVN = new MotorEventos();
		MVN.comeco();

		
	}

}
