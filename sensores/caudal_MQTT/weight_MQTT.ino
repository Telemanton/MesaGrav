#include <HX711_ADC.h>
#include <WiFi.h>
#include <PubSubClient.h>

// === MANUAL CONFIGURATION OF THE CALIBRATION FACTOR ===
const float FACTOR_CALIBRACION_MANUAL = 900; 

// === Pin Configurations ===
const int HX711_dout = 4; // Data output pin of the HX711 amplifier board
const int HX711_sck = 5;  // Serial clock input pin of the HX711 amplifier board
const int LED_PIN = 2;    // Built-in status LED on most standard ESP32 development boards

HX711_ADC LoadCell(HX711_dout, HX711_sck);

// === WiFi Configuration ===
const char* ssid = "Antonio_Garrido_agb00094";
const char* password = "agb00094";

// === MQTT Configuration ===
const char* mqtt_server = "10.3.141.1"; 
const int mqtt_port = 1883;
const char* mqtt_topic = "sensor/weight"; 

WiFiClient espClient;
PubSubClient client(espClient);

unsigned long t_print = 0; // Execution timer used to pace data transmission

// Handles blocking Wi-Fi connection with visual LED feedback (fast toggle)
void conectarWiFi() {
  Serial.print("Conectando a WiFi");
  WiFi.begin(ssid, password);
  
  // Fast blinking sequence while waiting for network registration
  while (WiFi.status() != WL_CONNECTED) {
    digitalWrite(LED_PIN, !digitalRead(LED_PIN));
    delay(250);
    Serial.print(".");
  }
  
  digitalWrite(LED_PIN, HIGH); // Solid LED state indicates a successful Wi-Fi connection
  Serial.println("\nWiFi conectado");
  Serial.print("IP del ESP32: ");
  Serial.println(WiFi.localIP());
}

// Establishes connection to the central MQTT Broker and updates status LED
void conectarMQTT() {
  while (!client.connected()) {
    Serial.print("Conectando al broker MQTT para Peso...");
    
    // Connect using a clean, static Client ID to avoid network collisions
    if (client.connect("ESP32_Mesa_Gravimetrica_Fisica")) { 
      Serial.println("Conectado al Broker con éxito");
      
      // Fast double blink sequence to visually notify successful MQTT binding
      for(int i=0; i<4; i++){
        digitalWrite(LED_PIN, !digitalRead(LED_PIN));
        delay(100);
      }
      digitalWrite(LED_PIN, HIGH);
    } else {
      Serial.print("Fallo de conexión MQTT (rc=");
      Serial.print(client.state());
      Serial.println(") reintentando en 2s...");
      digitalWrite(LED_PIN, LOW); // Turning off the status LED indicates connection loss or error
      delay(2000);
    }
  }
}

// ----------------- Setup -----------------
void setup() {
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW); // Initialize status LED as turned off
  
  Serial.begin(115200); 
  delay(10);
  Serial.println("\n=== Nodo Físico Báscula Gravimétrica ===");

  // Initialize the load cell communication and run automatic tare (keep scale empty on startup)
  LoadCell.begin();
  unsigned long stabilizingtime = 2000; // Time allocation for sensor variance settling (ms)
  boolean _tare = true;                 // Automatically establish initial zero-point reference
  
  LoadCell.start(stabilizingtime, _tare);
  
  // Fatal exception check for physical wire disconnection or faulty amplification hardware
  if (LoadCell.getTareTimeoutFlag() || LoadCell.getSignalTimeoutFlag()) {
    Serial.println("Error de hardware: Revisa cables DOUT (4) y SCK (5)");
    while (1) {
      // Infinite panic/SOS loop blinking pattern due to critical hardware error
      digitalWrite(LED_PIN, !digitalRead(LED_PIN));
      delay(100);
    }
  }
  else {
    LoadCell.setCalFactor(FACTOR_CALIBRACION_MANUAL); // Inject calibration slope factor
    Serial.println("Celda HX711 calibrada y tarada correctamente.");
  }

  conectarWiFi();
  client.setServer(mqtt_server, mqtt_port);
  conectarMQTT();
}

// ----------------- Loop -----------------
void loop() {
  // Maintain active MQTT broker binding using a non-blocking check loop
  if (!client.connected()) {
    conectarMQTT();
  }
  client.loop();

  // Asynchronously poll for new internal ADC conversions from the load cell
  if (LoadCell.update()) {
    // Non-blocking timer matching the 250ms (4Hz) sample interval of your previous setup
    if (millis() > t_print + 250) { 
      float peso_lectura = LoadCell.getData();
      float peso_neto = abs(peso_lectura); // Handle absolute value to guarantee positive values
      
      // Convert raw floating-point value to a formatted C-style flat character array
      char payload[16];
      dtostrf(peso_neto, 1, 2, payload); // Converts float into string with 2 decimal places

      // Output local debug data string onto the Serial Monitor
      Serial.print("[MQTT] Enviando Peso: ");
      Serial.print(payload);
      Serial.println(" g");

      // Direct publication of raw string data over the designated MQTT topic
      client.publish(mqtt_topic, payload);

      t_print = millis(); // Refresh timestamp marker for the next cycle
    }
  }
}