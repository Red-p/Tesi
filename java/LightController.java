package smartlight;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.javalite.http.Get;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;





public class LightController {
	
	public  String address;
	public  String port;
	public String base_route;
	public ObjectMapper mapper = new ObjectMapper();

	public static void main(String[] args) {
		
		String configFile = args[0];
		String lightFile=args[1];
		Sensore[] sensori;
		LightController c = new LightController();
		try {
        	
			c.init(configFile);
			
		} catch ( IOException e1) {
			System.out.println("init failed!");
		}
		try {
			
			for(int i=0; i<10;i++) {
				sensori=c.callAPI();
				// carico lo stato delle luci
				Lampade lampade= new Lampade();
				lampade.loadLightStatus(lightFile);
				//carico il nuovo stato
				c.updateLight(lightFile, sensori,lampade);
				//stampo il nuovo stato
				c.printLightStatus(lightFile);
				Thread.sleep(5000);
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        
		

	}
	
	
	public void init(String configFilePath) throws FileNotFoundException, IOException
    {
        Properties appProps = new Properties();
        appProps.load(new FileInputStream(configFilePath));
        address = appProps.get("address").toString();
        port = appProps.get("port").toString();
        base_route = appProps.get("base.route").toString();
        
        
    } 
	
	public double getMedia(List<Sensore> sensori) {
		double sum=0.0;
		
		for(Sensore s : sensori) {
			sum+=s.getBrightnessDetected();
		}
		
		return sum/sensori.size();
	}
	
	public String switchStatus(boolean isDark,String currentStatus) {
		String status=null;
		if(isDark && currentStatus.equals("spenta")) {
			
			status="accesa";
		}else if(isDark && currentStatus.equals("accesa")) {
			
			status="NO_SWITCH";
			
		}else if(!isDark && currentStatus.equals("accesa")) {
			
			status="spenta";
		}else if(!isDark && currentStatus.equals("spenta")) {
			status="NO_SWITCH";
			
		}
		return status;
		
	}
	
	public String[] checkCorrectStatus(String lightFile,Sensore[] sensori ,Lampade lampade) throws JsonProcessingException, IOException {
		
		boolean isDarkBathroom= this.isDarkBathroom(sensori);
		boolean isDarkKitchen= this.isDarkKitchen(sensori);
		boolean isDarkLivingRoom= this.isDarkLivingRoom(sensori);
		boolean isDarkBedroom= this.isDarkBedroom(sensori);
		
		
		//stampo lo stato corrente delle luci
		System.out.println("Stato delle luci prima dell'analisi dei sensori: ");
		for(Lampada l : lampade.getLuci()) {
			System.out.println(l.toString());
		}
		//analizzo lo stato delle luci
		
		//per ogni lampada chiamo la switchStatus per capire se e per quqle lampada cambiare lo stato
		String correctStatus[]= new String[4]; // sequenza Sala-Bagno-Cucina-Camera
//		System.out.println("sequenza luci:");
//		for(int i=0; i< lampade.getLuci().size(); i++) {
//			System.out.println(lampade.getLuci().get(i).toString());
//		}
		
		
		correctStatus[0]=(this.switchStatus(isDarkLivingRoom, lampade.getLuci().get(0).getStatus()).equalsIgnoreCase("NO_SWITCH") ) ? 
				lampade.getLuci().get(0).getStatus() :
				this.switchStatus(isDarkLivingRoom, lampade.getLuci().get(0).getStatus());
		
		correctStatus[1]=(this.switchStatus(isDarkBathroom, lampade.getLuci().get(1).getStatus()).equalsIgnoreCase("NO_SWITCH") ) ? 
				lampade.getLuci().get(1).getStatus() :
				this.switchStatus(isDarkBathroom, lampade.getLuci().get(1).getStatus());
		
		correctStatus[2]=(this.switchStatus(isDarkKitchen, lampade.getLuci().get(2).getStatus()).equalsIgnoreCase("NO_SWITCH") ) ? 
				lampade.getLuci().get(2).getStatus() :
				this.switchStatus(isDarkKitchen, lampade.getLuci().get(2).getStatus());
		
		correctStatus[3]= (this.switchStatus(isDarkBedroom, lampade.getLuci().get(3).getStatus()).equalsIgnoreCase("NO_SWITCH") ) ? 
				lampade.getLuci().get(3).getStatus() :
				this.switchStatus(isDarkBedroom, lampade.getLuci().get(3).getStatus());
		
//		for(int i=0; i< correctStatus.length; i++) {
//			System.out.println(correctStatus[i]);
//		}
		
		return correctStatus;
		
	}
	public void updateLight(String lightFile,Sensore[] sensori,Lampade lampade ) throws JsonProcessingException, IOException {
		
		String[] correctStatus=this.checkCorrectStatus(lightFile, sensori,lampade);
		
		//leggo il file json delle lampade e modifico lo stato delle stesse in virtù dei valori contenuti dentro correctstatus rispettando la sequenza  Sala-Bagno-Cucina-Camera
		List<Lampada> temp= new ArrayList<>();
		
		temp = Arrays.asList(mapper.readValue(Paths.get(lightFile).toFile(), Lampada[].class));
		
		//setto  i  nuovi stati
		temp.set(0, new Lampada("luceSala",correctStatus[0])); // sala
		temp.set(1, new Lampada("luceBagno",correctStatus[1])); // bagno
		temp.set(2, new Lampada("luceCucina",correctStatus[2])); //Cucina
		temp.set(3, new Lampada("luceCamera",correctStatus[3])); // Camera
		
		//modifico il file luci.json introducendo i nuovi stati delle luci,quindi scrivo sul file luci.json
		mapper.writeValue(new File(lightFile), temp);

		
		
		
		
		
		
	}
	
	public void printLightStatus(String lightFile) throws JsonParseException, JsonMappingException, IOException {
		
		List<Lampada> luciLastStatus= new ArrayList<>();
		//leggo il file luci.json
		luciLastStatus = Arrays.asList(mapper.readValue(Paths.get(lightFile).toFile(), Lampada[].class));
		
		//Stampo a video il valore delle luci
		System.out.println("Stato delle Luci dopo l'analisi dei sensori: ");
		
		luciLastStatus.forEach(System.out::println);
		
	}
	
	
	public Sensore[] callAPI() throws IOException {
		
		String line=null;
		String url_api="http://"+ this.address + this.port+ this.base_route ;
		
		//API call 
		URL url = new URL(url_api);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        
        // obtain API response
        InputStream responseStream = conn.getInputStream();
        StringBuilder builder= new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
        while((line=reader.readLine()) != null) {
        	builder.append(line);
        	
        }
        
        //parse API response
        String textJson=  builder.toString();
        JsonNode actualObj=mapper.readTree(textJson);
		String currentJson= mapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualObj);
		//System.out.println(currentJson);
		Sensore[] sensori = mapper.readValue(currentJson, Sensore[].class);
		
		return sensori;

	}
	
	public List<Sensore> filterSensor(String sensorType,Sensore[] sensori) {
		
		List<Sensore> result= new ArrayList<>();
		
		for(int i=0; i< sensori.length;i++) {
			if(sensori[i].getSensorName().equals(sensorType)) {
				
				result.add(sensori[i]);
			}
			
		}
		return result;
		
	}
	public boolean isDarkBathroom(Sensore[] sensori) throws JsonProcessingException, IOException {
		
		boolean isDark=false;
		double average=0;
		
		List<Sensore> sensoriBagno=this.filterSensor("sensoreBagno", sensori);
		
		average= this.getMedia(sensoriBagno);
		System.out.println("Valore Minimo Bagno: 40% ---->"+" Valore Corrente Bagno: "+ average +"%");
		if(average >=40)
			isDark=false;
		else
			isDark=true;
		
		return isDark;
		
	}
	public boolean isDarkKitchen(Sensore[] sensori) {
		boolean isDark=false;
		double average=0;
		
		List<Sensore> sensoriCucina=this.filterSensor("sensoreCucina", sensori);
		
		average= this.getMedia(sensoriCucina);
		System.out.println("Valore Minimo Cucina: 40% ---->"+" Valore Corrente Cucina: "+ average +"%");
		if(average >=40)
			isDark=false;
		else
			isDark=true;
		
		return isDark;
		
	}
	public boolean isDarkLivingRoom(Sensore[] sensori) {
		boolean isDark=false;
		double average=0;
		
		List<Sensore> sensoriSala=this.filterSensor("sensoreSala", sensori);
		
		average= this.getMedia(sensoriSala);
		
		System.out.println("Valore Minimo Sala: 35% ---->"+" Valore Corrente Sala: "+ average +"%");
		if(average >=35)
			isDark=false;
		else
			isDark=true;
		
		return isDark;
		
	}
	public boolean isDarkBedroom(Sensore[] sensori) {
		boolean isDark=false;
		double average=0;
		
		List<Sensore> sensoriCamera=this.filterSensor("sensoreCamera", sensori);
		
		average= this.getMedia(sensoriCamera);
		System.out.println("Valore Minimo Camera: 45% ---->"+" Valore Corrente Camera: "+ average +"%");
		if(average >=45)
			isDark=false;
		else
			isDark=true;
		
		return isDark;
		
	}

}
