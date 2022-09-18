package smartlight;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Sensore {
	@JsonProperty("sensorName")
	public String sensorName;
	@JsonProperty("brightnessDetected")
	public Double brightnessDetected;
	
	

}
