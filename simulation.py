import paho.mqtt.client as mqtt
import json
import time
import random
import math
import sys

# --- CONFIGURACIÓN DEL BROKER ---
BROKER = "10.3.141.1"  
PORT = 1883

# Tópicos (Deben coincidir exactamente con MqttListener.java)
TOPIC_ADXL = "sensor/adxl345"
TOPIC_FLOW = "sensor/flow"
TOPIC_WEIGHT = "sensor/weight"
TOPIC_SPEED = "sensor/speed"
TOPIC_ENGINE_GAUGE = "sensor/engine_gauge"
TOPIC_DROPPER_GAUGE = "sensor/dropper_gauge"
TOPIC_FRECUENCIA = "sensor/frecuency"

client = mqtt.Client()

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print(f"Simulador conectado y adaptado a MqttListener.java")
    else:
        print(f"Error de conexión, rc={rc}")
        sys.exit(1)

client.on_connect = on_connect
client.connect(BROKER, PORT, 60)
client.loop_start()

try:
    t = 0.0
    while True:
        t += 0.1
        
        # 1. ADXL345: Java usa objectMapper -> ENVIAR JSON
        adxl_data = {
            "x": round(math.sin(t) * 0.5, 2),
            "y": round(math.cos(t) * 0.3, 2),
            "z": round(9.8 + random.uniform(-0.1, 0.1), 2),
            "pitch": round(math.sin(t) * 10, 2),
            "roll": round(math.cos(t) * 5, 2)
        }
        client.publish(TOPIC_ADXL, json.dumps(adxl_data))

        # 2. FLOW: Java usa objectMapper con FlowWrapper -> ENVIAR JSON
        caudales_lista = []
        for i in range(1, 13):
            caudales_lista.append({
                "id": i, 
                "flowRate": round(random.uniform(1.5, 12.0), 2)
            })
        client.publish(TOPIC_FLOW, json.dumps({"sensores": caudales_lista}))

        # 3. FRECUENCIA: Java usa objectMapper -> ENVIAR JSON
        freq_data = {"frecuencia": round(50.0 + random.uniform(-2.0, 2.0), 2)}
        client.publish(TOPIC_FRECUENCIA, json.dumps(freq_data))

        # 4. PESO, VELOCIDAD Y GAUGES: 
        # Java usa Double.parseDouble(payload.trim()) -> ENVIAR SOLO EL NÚMERO (STRING)
        
        peso = round(random.uniform(0, 500), 2)
        client.publish(TOPIC_WEIGHT, str(peso)) # Envía "123.45"

        velocidad = round(random.uniform(0, 100), 2)
        client.publish(TOPIC_SPEED, str(velocidad))

        engine_gauge = round(random.uniform(0, 100), 2)
        client.publish(TOPIC_ENGINE_GAUGE, str(engine_gauge))

        dropper_gauge = round(random.uniform(0, 100), 2)
        client.publish(TOPIC_DROPPER_GAUGE, str(dropper_gauge))

        print(f"[{time.strftime('%H:%M:%S')}] Telemetría enviada (Mix JSON/Texto)")
        time.sleep(0.5)

except KeyboardInterrupt:
    print("\nSimulación detenida.")
finally:
    client.loop_stop()
    client.disconnect()