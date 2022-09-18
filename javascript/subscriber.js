var mqtt = require('mqtt')
var client = mqtt.connect('mqtt://192.168.56.109:1885')
var fs = require("fs");
var fileName='/home/sec/Desktop/tesi/sensori_mqtt'
var message_counter=+0;
var current_detection='['

client.on('connect', function () {
	client.subscribe('sensori')
})

client.on('message', function (topic, message) {
	context = message.toString();
	console.log('message:'+context)
	
	if(message_counter <=18){
		current_detection+=context +','+'\n'
	}else if(message_counter==19){
		current_detection+=context +'\n'
	}
	message_counter++;

	if(message_counter===20){
		current_detection+=']'
		console.log('5 rilevazioni fatte')
		fs.writeFile(fileName, current_detection,
			function(err) {
				if(err) {
					return console.log(err);
				}
				console.log("The file was saved!");
			});

	current_detection='['
	message_counter=0
	}

});
