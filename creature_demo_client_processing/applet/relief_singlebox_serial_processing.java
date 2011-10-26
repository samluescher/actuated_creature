import processing.core.*; 
import processing.xml.*; 

import processing.serial.*; 
import processing.opengl.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class relief_singlebox_serial_processing extends PApplet {

/**
 * Simple single box control program.
 */




Serial myPort;  // Create object from Serial class
int val;      // Data received from the serial port
int[] potDestinations = {64, 64, 64, 64};

public void setup() 
{
  size(210, 200, OPENGL);
  frameRate(30);
  // I know that the first port in the serial list on my mac
  // is always my  FTDI adaptor, so I open Serial.list()[0].
  // On Windows machines, this generally opens COM1.
  // Open whatever port is the one you're using.
  delay(1000);
  println(Serial.list());
  String portName = Serial.list()[0];
  myPort = new Serial(this, portName, 9600);
}

int protocolCounter = 0;
int[] potValues = new int[4];
final int TOTAL_POTS = 4;
public void draw()
{
  //background(64);
  fill(64,64,64,52);
  rect(0, 0, width, height);
  for(int i=0; i<TOTAL_POTS; i++){
      fill(2*potValues[i]);
      rect(i*50+10, 25+potValues[i], 40, 20);
      fill(255, 190, 220);
      rect(i*50+10, 30+potDestinations[i], 40, 10);
  }
  if(doSin){
    float speed = 0.25f;
    potDestinations[0] = (int)(64+40*sin((float)frameCount*speed+0.0f));
    potDestinations[1] = (int)(64+40*sin((float)frameCount*speed+PI/4.0f));
    potDestinations[2] = (int)(64+40*sin((float)frameCount*speed+PI/2));
    potDestinations[3] = (int)(64+40*sin((float)frameCount*speed+3.0f*PI/4.0f));
    sendPotDestinations();
  }
}


public void serialEvent(Serial myPort) {
while ( myPort.available() > 0) {  // If data is available,
    byte b = (byte)myPort.read();
    //println("? "+ b);
    switch(protocolCounter){
        case 0:{
            if(b == -1){
              protocolCounter++;
            }
            break;
        }
        case 1:
        case 2:
        case 3:
        case 4:{
          potValues[protocolCounter-1] = b;
          protocolCounter++;
          if(protocolCounter == TOTAL_POTS+1){
            protocolCounter = 0;
            //println("valid packet...");
          }
          break;
        }
        default:{
          println("unknown protocol state :(");
        }
    }
    //print((char)(myPort.read()));         // read it and store it in val
  }
}

public void mouseClicked(){
  int pot = min(TOTAL_POTS-1, (int)((mouseX*4) / width));
  potDestinations[pot] = (mouseY)*127/height;
  sendPotDestinations();
}

public void sendPotDestinations(){
  myPort.write((byte)255);
  for(int i=0; i<TOTAL_POTS; i++){
   myPort.write((byte)0x7F&(byte)potDestinations[i]);
  }
}

boolean doSin = false;
public void keyPressed(){
  if(key == 's'){
    doSin = !doSin;
  }
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#FFFFFF", "relief_singlebox_serial_processing" });
  }
}
