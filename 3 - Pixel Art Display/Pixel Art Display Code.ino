#include <Adafruit_NeoPixel.h>
#include <SD.h>
#include <SPI.h>

#define PIN 5 //LED Pin
#define NUMPIXELS 256 //Number of Leds

#define SDssPin 10    //Pin on CS

#define animRuns 4

Adafruit_NeoPixel pixels = Adafruit_NeoPixel(NUMPIXELS, PIN, NEO_GRB + NEO_KHZ800);

byte x;
byte currentFrame;
char currentAnimation;
byte animCounter;
bool animRestart;

File dataFile;
String fileName;

void setup() {
  
  //Serial.begin(9600);
  //while (!Serial) {}

  if (!SD.begin(SDssPin)) {
    // don't do anything more:
    while (1);
  }

  currentFrame = 0;
  currentAnimation = 'a';
  animCounter = 0;
  animRestart = false;

  pixels.begin();
}

void loop() {
  
  fileName = currentAnimation + (String)currentFrame + ".bmp";

  if (SD.exists(fileName)) {
    animRestart = false;
    SendFile();
    currentFrame++;
  } else {
    currentFrame = 0;
    animCounter++;

    if (animRestart){
      currentAnimation = 'a';
      animCounter = 0;
    }

    if (animCounter >= animRuns) {
      currentAnimation++;
      animCounter = 0;
      animRestart = true;
    }
  }
  
}


void SendFile() {
  //char temp[14];
  //Filename.toCharArray(temp, 14);

  dataFile = SD.open(fileName);

  if (dataFile) {
    ReadTheFile();
    dataFile.close();
  }
  else {
    delay(1000);
    //setupSDcard();
    return;
  }
}


uint32_t readLong() {
  uint32_t retValue;
  byte incomingbyte;

  incomingbyte = readByte();
  retValue = (uint32_t)((byte)incomingbyte);

  incomingbyte = readByte();
  retValue += (uint32_t)((byte)incomingbyte) << 8;

  incomingbyte = readByte();
  retValue += (uint32_t)((byte)incomingbyte) << 16;

  incomingbyte = readByte();
  retValue += (uint32_t)((byte)incomingbyte) << 24;

  return retValue;
}


uint16_t readInt() {
  byte incomingbyte;
  uint16_t retValue;

  incomingbyte = readByte();
  retValue += (uint16_t)((byte)incomingbyte);

  incomingbyte = readByte();
  retValue += (uint16_t)((byte)incomingbyte) << 8;

  return retValue;
}


int readByte() {
  int retbyte = -1;
  while (retbyte < 0) retbyte = dataFile.read();
  return retbyte;
}


void ReadTheFile() {
#define MYBMP_BF_TYPE           0x4D42
#define MYBMP_BF_OFF_BITS       54
#define MYBMP_BI_SIZE           40
#define MYBMP_BI_RGB            0L
#define MYBMP_BI_RLE8           1L
#define MYBMP_BI_RLE4           2L
#define MYBMP_BI_BITFIELDS      3L

#define displayWidth    16
#define lineLength      48

  for (int y = 0; y < 16; y++) {
    int bufpos = 0;
    for (int x = 0; x < 16; x++) {
      uint32_t offset = (MYBMP_BF_OFF_BITS + ((y * lineLength) + (x * 3))) ;
      dataFile.seek(offset);

      //getRGBwithGamma();
      byte b = readByte();
      byte g = readByte();
      byte r = readByte();

      int activePixel;
      if (y%2 == 0) {
        activePixel = (y+1)*16 -x -1;
      } else {
        activePixel = y*16 + x;
      }

      pixels.setPixelColor(activePixel, pixels.Color(r,g,b));
      //printPixel(y);

    }
    
  }

  pixels.show();
}
