const fs = require('fs')
export default function handler(req,res){
    const { sensorName } = req.query
    const sensori = JSON.parse(fs.readFileSync('/home/sec/Desktop/tesi/sensori_mqtt'));
    const sensore = sensori.filter((item) =>item.sensorName === sensorName)
    res.status(200).json(sensore)
}