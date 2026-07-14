import paho.mqtt.client as mqtt
import time
import random
import sys

# --- CONFIGURACIÓN DEL BROKER ---
BROKER = "localhost"  
PORT = 1883

INCREMENTO = 0.1

# Tópicos (Deben coincidir exactamente con MqttListener.java)
TOPIC_WEIGHT = "sensor/weight"

client = mqtt.Client()

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("====================================================")
        print(" SIMULADOR MESA GRAVIMÉTRICA ACTIVO (MODO ESP32)   ")
        print("====================================================")
        print("Conectado con éxito al broker MQTT.")
    else:
        print(f"Error crítico de conexión, rc={rc}")
        sys.exit(1)

client.on_connect = on_connect
client.connect(BROKER, PORT, 60)
client.loop_start()

# Forzamos la inicialización explícita como floats
peso = 0.0
peso_total_neto = 0.0
peso_con_variacion_irregular = 0.0
tara = 0.0
t = 0.0

try:
    # --- FASE 1: ESTADO INICIAL (0 a 10s) ---
    while t < 10.0:  
        variacion_irregular = round(random.uniform(0.2001, 0.2101), 4)
        client.publish(TOPIC_WEIGHT, str(variacion_irregular)) 
        print(f"Peso simulado (ruido vacío): {variacion_irregular} g")

        t += 0.25
        time.sleep(0.25)
        if t >= 10.0:
            tara = variacion_irregular

    # --- FASE 2: ENSAYO DE CAÍDA DE MATERIAL (10 a 60s) ---
    print(f"\n>>> Comenzando dosificación. Tara registrada: {tara} g\n")
    
    while t < 60.0:
        variacion_irregular = round(random.uniform(0.2001, 0.3001), 4)
        
        # Tu ecuación física de dosificación progresiva:
        peso = round(variacion_irregular + INCREMENTO, 4)
        peso_total_neto += INCREMENTO 
        peso_con_variacion_irregular = round(peso + peso_con_variacion_irregular, 4)
        
        client.publish(TOPIC_WEIGHT, str(peso)) 
        print(f"Peso neto instantáneo: {peso} g | Acumulado teórico: {peso_total_neto:.2f} g")

        t += 0.25
        time.sleep(0.25)

except KeyboardInterrupt:
    print("\nSimulación finalizada por el usuario.")

finally:
    client.loop_stop()
    client.disconnect()

print("\n====================================================")
print("             REPORTE FINAL DE ENSAYO                ")
print("====================================================")
print(f"La tara en vacío es de               : {tara:.4f} g")
print(f"Peso teórico añadido (sin ruido)     : {peso_total_neto:.4f} g")
print(f"Suma acumulada neta (con variación)  : {peso_con_variacion_irregular:.4f} g")
print("====================================================")