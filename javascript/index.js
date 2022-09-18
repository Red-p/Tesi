const fs = require('fs')
export default function(req,res){
    const sensori =JSON.parse(fs.readFileSync('/home/sec/Desktop/tesi/sensori_mqtt'));
    res.status(200).json(sensori)
}