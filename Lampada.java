package smartlight;

import lombok.Data;

@Data
public class Lampada {
	public String name;
	public String status;
	
	public Lampada(String name, String status) {
		
		this.name = name;
		this.status = status;
	}
	public Lampada() {
			
			this.name = "";
			this.status = "";
		}
	
	
	
}
