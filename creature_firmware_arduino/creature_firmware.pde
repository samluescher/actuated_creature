/*
 * Relief Arduino Firmware
 * September 2009 by Daniel Leithinger
 */

#include <AFMotor.h>

AF_DCMotor motors[] = {AF_DCMotor(1, MOTOR12_64KHZ), AF_DCMotor(2, MOTOR12_64KHZ), AF_DCMotor(3, MOTOR34_64KHZ), AF_DCMotor(4, MOTOR34_64KHZ)};

int switches[] = {10, 13};
int TOTAL_SWITCHES = 2;

int potPin[] = {5, 4, 2, 0};    // select the input pins for the potentiometers
int val[] = {0, 0, 0, 0};       // variable to store the value coming from the potentiometers
int integralMotor1[] = {0, 0, 0, 0}; // the motor
int destinations[] = {512, 512, 512, 512};

// These are the main parameters for the feedback system...
float Gain_P = 1.2;   // proportional term
float Gain_I = 0.2;  // integral term
float Max_I  = 60;    // max amount of integral that can be added.
int gravityCompensation = 30; // offsets the weight of the pin.
int deadZone = 0;

// These are for the protocol control when reading data from serial. 
int protocolCounter = 0;
int TOTAL_POTS = 4;

void setup() {
  // over
  int prescalerVal = 0x07;
  // clear out the prescaler
  TCCR0B &= ~prescalerVal;
  TCCR1B &= ~prescalerVal;
  TCCR2B &= ~prescalerVal;
  prescalerVal = 1;
  TCCR0B |= prescalerVal;
  TCCR1B |= prescalerVal;
  TCCR2B |= prescalerVal;
  
  Serial.begin(9600); // set up Serial library
}

void loop() {
  //Serial.print(255, BYTE); // Start data byte...
  
  for (int i = 0; i < 4; i++)
  {
    val[i] = analogRead(potPin[i]);    // read the value from the sensor
    //Serial.print(0x7F&(byte)(val[i]>>3), BYTE);

    int diff = destinations[i] - val[i];
    // handle integral term
    integralMotor1[i] += (int)(diff*Gain_I);
    integralMotor1[i] = min(Max_I, max(-Max_I, integralMotor1[i]));
    int output = (int)(diff*Gain_P);
    output += integralMotor1[i];
    //output -= gravityCompensation; // apply a torque to offset.

    if(abs(diff) < deadZone) {
      motors[i].run(RELEASE);
    }
    else {
      motors[i].setSpeed(min(abs(output), 255));
    }
    if(output < 0) {
      motors[i].run(BACKWARD);
    }
    else {
      motors[i].run(FORWARD);
    }
  }
  
  for (int switchIndex = 0; switchIndex < TOTAL_SWITCHES; switchIndex++) {
    int switchVal = !digitalRead(switches[switchIndex]);
    Serial.print(switchVal, BYTE);
  }
  
  while (Serial.available()) {
    // read the most recent byte (which will be from 0 to 255)
    int sval = Serial.read();
    switch(protocolCounter){
      case 0:{
            if(sval == 255){
              protocolCounter++;
            }
            break;
        }
        case 1:
        case 2:
        case 3:
        case 4:{
          destinations[protocolCounter-1] = sval<<3;
          protocolCounter++;
          if(protocolCounter == TOTAL_POTS+1){
            protocolCounter = 0;
            //println("valid packet...");
          }
          break;
        }
        default:{
          //println("unknown protocol state :(");
        } 
    }
  }
}



