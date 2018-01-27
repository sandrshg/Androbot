//motor one - right
int enA = 9;
int in1 = 2;
int in2 = 3;
// motor two - left
int enB = 8;
int in3 = 4;
int in4 = 5;
//ultrasonic sensors
/*int trig1=10;
int echo1=11;
int trig2=12;
int echo2=13;
int trig3=14;
int echo3=15;
float dfront,dright,dleft;*/

void setup() {
  pinMode(enA, OUTPUT);
  pinMode(enB, OUTPUT);
  pinMode(in1, OUTPUT);
  pinMode(in2, OUTPUT);
  pinMode(in3, OUTPUT);
  pinMode(in4, OUTPUT);

  /*pinMode(trig1,OUTPUT);
  pinMode(trig2,OUTPUT);
  pinMode(trig3,OUTPUT);
  pinMode(echo1,INPUT);
  pinMode(echo2,INPUT);
  pinMode(echo3,INPUT);*/

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
  analogWrite(enB,200);
}
void turnright() {
  digitalWrite(in1,HIGH);
  digitalWrite(in2,LOW);
  digitalWrite(in3,LOW);
  digitalWrite(in4,HIGH);
  analogWrite(enA,200);
}
void goforward() {
  digitalWrite(in1,HIGH);
  digitalWrite(in2,LOW);
  digitalWrite(in3,HIGH);
  digitalWrite(in4,LOW);
  analogWrite(enA,255);
  analogWrite(enB,255);
}
void movebackward() {
  digitalWrite(in1,LOW);
  digitalWrite(in2,HIGH);
  digitalWrite(in3,LOW);
  digitalWrite(in4,HIGH);
  analogWrite(enA,255);
  analogWrite(enB,255);
}

void stop() {
  digitalWrite(in1,LOW);
  digitalWrite(in2,LOW);
  digitalWrite(in3,LOW);
  digitalWrite(in4,LOW);
}

float discm(int trig, int echo) {
  float duration,dis;
  digitalWrite(trig,HIGH);
  delayMicroseconds(10);
  digitalWrite(trig,LOW);
  duration=pulseIn(echo,HIGH);
  dis=duration/58.0;
  return dis;
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

//obstacle avoid algorithm
/*  dfront=discm(trig1,echo1);
  dleft=discm(trig2,echo2);
  dright=discm(trig3,echo3);

  if(dfront<5&&dleft<5&&dright<5) {
    while(dleft<5&&dright<5) {
      movebackward();
      dleft=discm(trig2,echo2);
      dleft=discm(trig3,echo3);
    }
    turnright();
    delay(1000);
  }

  if(dfront<10&&dleft<10) {
    turnright();
    delay(1000);
  }
  else if(dfront<5&&dleft>=5&&dright>5) {
    turnleft();
    delay(1000);
  }
  else if(dfront<10) {
    turnright();
    delay(1000);
  }
  else {
    gofarward();
  }
  delay(1000);*/
}
