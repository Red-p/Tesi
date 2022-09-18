package smartlight;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public class Lampade {
 private List<Lampada> luci;
 
 
 
 public void loadLightStatus(String lightFile) throws JsonParseException, JsonMappingException, IOException {
	 
	 
	    ObjectMapper mapper = new ObjectMapper();

	    // convert JSON array to list of books
	   this.luci = Arrays.asList(mapper.readValue(Paths.get(lightFile).toFile(), Lampada[].class));

//	    // print books
//	    this.luci.forEach(System.out::println);
 }




}

