#include <Wire.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <math.h>

// === ADXL345 Sensor Configuration ===
#define ADXL345 0x53 // I2C address of the accelerometer

float X_out = 0.0f, Y_out = 0.0f, Z_out = 0.0f;
float offsetX = 0.0f, offsetY = 0.0f, offsetZ = 0.0f;

// === WiFi Configuration ===
const char* ssid = "Antonio_Garrido_agb00094";
const char* password = "agb00094";

// === MQTT Configuration ===
const char* mqtt_server = "10.3.141.1"; // Broker IP address
const int mqtt_port = 1883;
const char* mqtt_topic = "sensor/adxl345"; // Destination topic

WiFiClient espClient;
PubSubClient client(espClient);

// ----------------- Functions -----------------

// Establishes a blocking Wi-Fi connection to the configured access point
void conectarWiFi() {
  Serial.print("Conectando a WiFi");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi conectado");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());
}

// Blocks execution until a persistent session is established with the MQTT Broker
void conectarMQTT() {
  while (!client.connected()) {
    Serial.print("Conectando al broker MQTT...");
    if (client.connect("ESP32_ADXL345")) {
      Serial.println("Conectado");
    } else {
      Serial.print("Fallo (rc=");
      Serial.print(client.state());
      Serial.println(") reintentando en 2s...");
      delay(2000);
    }
  }
}

// Reads raw acceleration data via I2C and scales it to standard gravity (g)
void leerAcelerometro() {
  Wire.beginTransmission(ADXL345);
  Wire.write(0x32); // Data register 0x32 (DATAX0) initiates multi-byte read
  Wire.endTransmission(false); // Send a repeated start condition to keep I2C bus active
  Wire.requestFrom(ADXL345, (uint8_t)6, (uint8_t)true); // Request 6 bytes total (2 per axis)

  // Reconstruct 16-bit signed integers from pairs of 8-bit registers (Low/High byte)
  int16_t xr = Wire.read() | (Wire.read() << 8);
  int16_t yr = Wire.read() | (Wire.read() << 8);
  int16_t zr = Wire.read() | (Wire.read() << 8);

  // Convert raw LSB counts to 'g' forces using the default scale factor (approx. 256 LSB/g)
  X_out = xr / 256.0f;
  Y_out = yr / 256.0f;
  Z_out = zr / 256.0f;
}

// Samples the stationary sensor for 5 seconds to determine hardware offset errors
void calibrarSensor() {
  unsigned long start = millis();
  int samples = 0;
  float sumX = 0, sumY = 0, sumZ = 0;

  while (millis() - start < 5000UL) {
    leerAcelerometro();
    sumX += X_out;
    sumY += Y_out;
    sumZ += Z_out;
    samples++;
    delay(100);
  }
  if (samples == 0) samples = 1;
  
  // Calculate average sensor errors
  offsetX = sumX / samples;
  offsetY = sumY / samples;
  // Account for 1g of earth's gravity pointing straight down on Z-axis during static placement
  offsetZ = (sumZ / samples) - 1.0f;
}

// ----------------- Setup -----------------
void setup() {
  Serial.begin(115200);
  delay(200);
  Serial.println("=== ESP32 + ADXL345 + MQTT ===");

  Wire.begin(); // Initialize I2C master bus
  delay(200);

  // Wake up ADXL345 and enable measurement mode
  Wire.beginTransmission(ADXL345);
  Wire.write(0x2D); // POWER_CTL register
  Wire.write(0x08); // Set Measure bit to high
  Wire.endTransmission();

  Serial.println("Calibrando... coloca el sensor plano y quieto");
  calibrarSensor();
  Serial.printf("Offsets -> X: %.4f, Y: %.4f, Z: %.4f\n", offsetX, offsetY, offsetZ);

  conectarWiFi();
  client.setServer(mqtt_server, mqtt_port);
  conectarMQTT();
}

// ----------------- Loop -----------------
void loop() {
  if (!client.connected()) {
    conectarMQTT();
  }
  client.loop(); // Process background MQTT keep-alive pings and internal buffer cycles

  leerAcelerometro();

  // Apply calibration coefficients to extract clean acceleration values
  float Xc = X_out - offsetX;
  float Yc = Y_out - offsetY;
  float Zc = Z_out - offsetZ;

  // Trigonometric formulas converting linear acceleration vectors into angular degrees
  float roll  = atan2f(Yc, Zc) * 180.0f / M_PI;
  float pitch = atan2f(-Xc, sqrtf(Yc*Yc + Zc*Zc)) * 180.0f / M_PI;

  // Manually construct raw JSON string formatting values to two decimal precision
  String payload = "{";
  payload += "\"x\":" + String(Xc,2) + ",";
  payload += "\"y\":" + String(Yc,2) + ",";
  payload += "\"z\":" + String(Zc,2) + ",";
  payload += "\"pitch\":" + String(pitch,2) + ",";
  payload += "\"roll\":" + String(roll,2);
  payload += "}";

  Serial.println(payload);

  // Cast wrapper string to a C-style char array and transmit via MQTT protocol
  client.publish(mqtt_topic, payload.c_str());

  delay(250); // Hardcoded sampling throttle yielding an execution rate of 4 Hz
}