#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>

// --- CONFIGURACIÓN RED Y MQTT ---
const char* ssid = "Antonio_Garrido_agb00094";
const char* password = "agb00094";
const char* mqtt_server = "10.3.141.75";
const int mqtt_port = 1883;
const char* mqtt_topic = "sensor/flow";

WiFiClient espClient;
PubSubClient client(espClient);

// --- CONFIGURACIÓN SENSORES ---
const int sensorPins[] = {13, 12, 14};
volatile long pulseCounts[3] = {0, 0, 0};
const float calibrationFactor = 7.5; // pulsos por litro
unsigned long lastMillis = 0;
const unsigned long SEND_INTERVAL = 500;

void IRAM_ATTR pulseCounter1() { pulseCounts[0]++; }
void IRAM_ATTR pulseCounter2() { pulseCounts[1]++; }
void IRAM_ATTR pulseCounter3() { pulseCounts[2]++; }

void setup_wifi() {
  Serial.print("Conectando a ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nWiFi conectado.");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());
}

void reconnect() {
  while (!client.connected()) {
    String clientId = "ESP32_Flow_Station_" + String(random(0xffff), HEX);
    if (client.connect(clientId.c_str())) {
      Serial.println("Conectado al broker MQTT.");
    } else {
      Serial.print("Fallo al conectar MQTT, rc=");
      Serial.println(client.state());
      delay(3000);
    }
  }
}

void setup() {
  Serial.begin(115200);
  delay(500);

  for (int i = 0; i < 3; i++) {
    pinMode(sensorPins[i], INPUT_PULLUP);
  }

  attachInterrupt(digitalPinToInterrupt(sensorPins[0]), pulseCounter1, FALLING);
  attachInterrupt(digitalPinToInterrupt(sensorPins[1]), pulseCounter2, FALLING);
  attachInterrupt(digitalPinToInterrupt(sensorPins[2]), pulseCounter3, FALLING);

  setup_wifi();
  client.setServer(mqtt_server, mqtt_port);
  client.setBufferSize(1024);
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    setup_wifi();
  }

  if (!client.connected()) {
    reconnect();
  }

  client.loop();

  unsigned long currentMillis = millis();
  if (currentMillis - lastMillis >= SEND_INTERVAL) {
    unsigned long duration = currentMillis - lastMillis;
    lastMillis = currentMillis;

    StaticJsonDocument<512> doc;
    JsonArray sensores = doc.createNestedArray("sensores");

    for (int i = 0; i < 3; i++) {
      noInterrupts();
      long pulses = pulseCounts[i];
      pulseCounts[i] = 0;
      interrupts();

      float flowRate = 0.0;
      if (duration > 0) {
        flowRate = (pulses * 60000.0) / (duration * calibrationFactor);
      }

      if (isnan(flowRate) || isinf(flowRate)) {
        flowRate = 0.0;
      }

      JsonObject sensor = sensores.createNestedObject();
      sensor["id"] = i + 1;
      sensor["flowRate"] = round(flowRate * 100.0) / 100.0;
    }

    char buffer[512];
    serializeJson(doc, buffer);

    if (client.publish(mqtt_topic, buffer)) {
      Serial.print("MQTT publicado: ");
      Serial.println(buffer);
    } else {
      Serial.println("Error publicando MQTT");
    }
  }
}