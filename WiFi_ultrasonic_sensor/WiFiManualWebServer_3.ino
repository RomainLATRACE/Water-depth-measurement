/*Ultrasonic sensor used: HC SR04*/


#include <ESP8266WiFi.h>

IPAddress ip(192, 168, 43, 101);
IPAddress gateway(192, 168, 43, 1);
IPAddress subnet(255, 255, 255, 0);
IPAddress dns(192, 168, 43, 1);

#define led 14
/* Constants for spindles */
const byte TRIGGER_PIN = 13; // PIN TRIGGER D7
const byte ECHO_PIN = 15;    // PIN ECHO D8
 
/* Constant for timeout */
const unsigned long MEASURE_TIMEOUT = 25000UL; // 25ms = ~8m à 340m/s

/* Sound velocity in air in mm/us */
const float SOUND_SPEED = 340.0 / 1000;


const char* ssid = "FAB"; //name of device which share the connexion
const char* password = "petitFab";  //password to access to this network 
int val;

/* Create an instance of the server
specify the port to listen on as an argument */
WiFiServer server(80);



void setup() {
  Serial.begin(115200);

   /* Initializes the spindles */
  pinMode(TRIGGER_PIN, OUTPUT);
  digitalWrite(TRIGGER_PIN, LOW); // The TRIGGER spindle must be at LOW at rest
  pinMode(ECHO_PIN, INPUT);
  // prepare gpio2
  pinMode(led, OUTPUT);
  digitalWrite(led, 0);

  // Connect to WiFi network
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.config(ip, dns, gateway, subnet);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");

  // Start the server
  server.begin();
  Serial.println("Server started");

  // Print the IP address
  Serial.println(WiFi.localIP());
}





void loop() {
  // Check if a client has connected
  WiFiClient client = server.available();
  if (!client) {
    return;
  }
  Serial.println("new client");

 while(!client.available()){
  delay(1);
 }

  // Read the first line of the request
  String req = client.readStringUntil('\r');
  Serial.println("request: ");
  Serial.println(req);
  client.flush();

    
  if (req.indexOf("led") != -1) {         //if the server received "led", the state of the led will reverse
    digitalWrite(led, !digitalRead(led));
  } else {
    Serial.println("invalid request");
    client.stop();
  }

  // Set gpioled according to the request




  
  /* Starts a distance measurement by sending a 10µs HIGH pulse to the TRIGGER pin */
  digitalWrite(TRIGGER_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIGGER_PIN, LOW);
  
  /* 2. Measures the time between the sending of the ultrasonic pulse and its echo (if any) */
  long measure = pulseIn(ECHO_PIN, HIGH, MEASURE_TIMEOUT);
   
  /* 3. Calculate the distance from the measured time */
  float distance_mm = measure / 2.0 * SOUND_SPEED;
  float distance_cm = distance_mm / 10.0; 





  // Send the response to the client
  // it is OK for multiple small client.print/write,
  // because nagle algorithm will group them into one single packet
  client.println ("HTTP/1.1 200 OK");
  client.println ("Content-Type: text/html");
  client.println ("");

  if (digitalRead(led)){    //1=ON - 0=OFF
    client.print("led is ON");
  }else {
    client.print("led is OFF");
  }

  client.print(" ------ sensor: ");
  client.print(distance_cm);

  delay(50);
}
