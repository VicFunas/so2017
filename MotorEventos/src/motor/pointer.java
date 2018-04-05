package motor;

public class pointer {

	private int processo;
	private int endereco;
	
	public pointer(String doisBytes) {
		
		if (doisBytes.length()==4) {
			this.setProcesso(doisBytes.substring(0, 1));
			this.setEndereco(doisBytes.substring(1));
		} else {
			System.out.println("Erro! Tentiva de passar passar conteudo errado");
		}
		
	}

	public int getProcesso() {
		return processo;
	}
	public void setProcesso(String processo) {
		try {
			int num = Integer.parseInt(processo, 16);
			this.processo = num;
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Não existe esse processo");
		}
		
	}
	public int getEndereco() {
		return endereco;
	}
	public void setEndereco(String endereco) {
		try {
			int num = Integer.parseInt(endereco, 16);
			this.endereco = num;
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Não existe esse endereco");
		}
		
	}
	
}
