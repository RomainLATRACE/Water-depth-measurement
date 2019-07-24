const int pwPin = 2;
long sensor, cm;


void setup() { 
  Serial.begin(9600);
  pinMode(pwPin, INPUT);
}


void loop() {

  sensor = pulseIn(pwPin, HIGH);
  cm = sensor/58;
  Serial.print("PWM ");
  Serial.print(cm);
  Serial.println("cm");
  
  delay(500);
}
