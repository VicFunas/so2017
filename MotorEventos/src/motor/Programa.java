package motor;

import java.util.ArrayList;

public class Programa {

	ArrayList<String> enderecoFisico;
	ArrayList<String> enderecoLogico;
	ArrayList<String> codeInstrucao;
	ArrayList<String> blocoDisco;

	private int pc;
	
	public Programa() {
		enderecoFisico = new ArrayList<String>();
		enderecoLogico = new ArrayList<String>();
		codeInstrucao  = new ArrayList<String>();
		blocoDisco = new ArrayList<String>();
	}
	
	public int getPc() {
		return pc;
	}

	public void setPc(int pc) {
		this.pc = pc;
	}
	
	public ArrayList<String> getEnderecoFisico() {
		return enderecoFisico;
	}
	
	public void setEnderecoFisico(String enderecoFisico) {
		this.enderecoFisico.add(enderecoFisico);
	}
	
	public ArrayList<String> getEnderecoLogico() {
		return enderecoLogico;
	}
	
	public void setEnderecoLogico(String enderecoLogico) {
		this.enderecoLogico.add(enderecoLogico);
	}
	
	public ArrayList<String> getCodeInstrucao() {
		return codeInstrucao;
	}
	
	public void setCodeInstrucao(String codeInstrucao) {
		this.codeInstrucao.add(codeInstrucao);
	}
	
	public String getFisico(String logic) {
		int i = 0;
		int drive = Integer.parseInt(logic, 16);
//		System.out.println("Logic = "+logic);
		int diff = Math.abs(drive-Integer.parseInt(this.enderecoLogico.get(0), 16));
		while (diff > 1) {
			
//			System.out.println("Drive = "+drive);
			i++;
//			System.out.println("Logic = "+Integer.parseInt(this.enderecoLogico.get(i), 16));
			
			diff = Math.abs(drive-Integer.parseInt(this.enderecoLogico.get(i), 16));
			while(diff >= 255) { // outra pagina
				diff = Math.abs(diff -= 256);
			}
//			System.out.println("diff = "+diff);
		}
		if(diff == 1) {
			int trance = Integer.parseInt(this.enderecoFisico.get(i))+1;
//			System.out.println("fisico = "+Integer.toString(trance));
			return Integer.toString(trance);
		}
		else {
//			System.out.println("fisico = "+this.enderecoFisico.get(i));
			return this.enderecoFisico.get(i);
		}
		
	}
	
	public String getBloco() {
		return this.enderecoFisico.get(0).substring(0, 1);
	}
	
	public int getTableIndexByInstruction(String instruction) {
		int i = 0;
		for (int j = 0; j < codeInstrucao.size(); j++) {
			if(codeInstrucao.get(j) == instruction) {
				i = j;
			}
		}
		return i;
	}
	
	public ArrayList<String> getBlocoDisco() {
		return blocoDisco;
	}

	public void setBlocoDisco(String blocoDisco) {
		this.blocoDisco.add(blocoDisco);
	}
	
}
