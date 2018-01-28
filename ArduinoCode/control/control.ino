//motor one - right
int enA = 5;
int in1 = 6;
int in2 = 4;
// motor two - left
int enB = 7;
int in3 = 2;
int in4 = 3;

void setup() {
  pinMode(enA, OUTPUT);
  pinMode(enB, OUTPUT);
  pinMode(in1, OUTPUT);
  pinMode(in2, OUTPUT);
  pinMode(in3, OUTPUT);
  pinMode(in4, OUTPUT);

  digitalWrite(in1,LOW);
  digitalWrite(in2,LOW);
  digitalWrite(in3,LOW);
  digitalWrite(in4,LOW);
  Serial.begin(9600);
}
void turnleft() {
  digitalWrite(in1,LOW);
  digitalWrite(in2,HIGH);
  digitalWrite(in3,HIGH);
  digitalWrite(in4,LOW);
  analogWrite(enB,255);
  analogWrite(enA,255);
}
void turnright() {
  digitalWrite(in1,HIGH);
  digitalWrite(in2,LOW);
  digitalWrite(in3,LOW);
  digitalWrite(in4,HIGH);
  analogWrite(enA,255);
  analogWrite(enB,255);
}
void goforward() {
  digitalWrite(in1,LOW);
  digitalWrite(in2,HIGH);
  digitalWrite(in3,LOW);
  digitalWrite(in4,HIGH);
  analogWrite(enA,255);
  analogWrite(enB,255);
}
void movebackward() {
  digitalWrite(in1,HIGH);
  digitalWrite(in2,LOW);
  digitalWrite(in3,HIGH);
  digitalWrite(in4,LOW);
  analogWrite(enA,255);
  analogWrite(enB,255);
}

void stop() {
  digitalWrite(in1,LOW);
  digitalWrite(in2,LOW);
  digitalWrite(in3,LOW);
  digitalWrite(in4,LOW);
}

void loop() {
  char c;
  if(Serial.available()) {
    c=Serial.read();
  }
  if(c=='f')
  goforward();
  if(c=='b')
  movebackward();
  if(c=='l')
  turnleft();
  if(c=='r')
  turnright();
  if(c=='s')
  stop();  
}
