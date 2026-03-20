import paho.mqtt.client as mqtt
import json
import time
import random
import math

# Configuración del Broker
BROKER = "192.168.1.14"
PORT = 1883
TOPIC_ADXL = "sensor/adxl345"
TOPIC_FRECUENCIA = "sensor/frecuency"
TOPIC_FLOW = "sensor/flow"

client = mqtt.Client()

try:
    client.connect(BROKER, PORT, 60)
    print(f"Simulador conectado al broker {BROKER}")
except Exception as e:
    print(f"Error conectando al broker: {e}")
    exit(1)

t = 0
while True:
    t += 0.1
    
    # 1. Simular ADXL345 (Oscilaciones suaves)
    adxl_data = {
        "x": round(math.sin(t) * 0.5, 2),
        "y": round(math.cos(t) * 0.3, 2),
        "z": round(9.8 + random.uniform(-0.1, 0.1), 2),
        "pitch": round(math.sin(t) * 10, 2),
        "roll": round(math.cos(t) * 5, 2)
    }
    client.publish(TOPIC_ADXL, json.dumps(adxl_data))

    # 2. Simular Frecuencia (Vibración)
    freq_data = {
        "frecuencia": round(50.0 + random.uniform(-2.0, 2.0), 2)
    }
    client.publish(TOPIC_FRECUENCIA, json.dumps(freq_data))

    # 3. Simular 12 Sensores de Caudal (Estructura FlowWrapper)
    # Generamos una lista de 12 sensores con IDs del 1 al 12
    caudales_lista = []
    for i in range(1, 13):
        caudales_lista.append({
            "id": i,
            "flowRate": round(random.uniform(1.5, 12.0), 2)
        })
    
    # El JSON debe llevar la clave "sensores" para que FlowWrapper.java lo entienda
    flow_wrapper = {
        "sensores": caudales_lista
    }
    client.publish(TOPIC_FLOW, json.dumps(flow_wrapper))

    print(f"[{time.strftime('%H:%M:%S')}] Datos enviados a los topics...")
    time.sleep(1) # Envío cada segundo