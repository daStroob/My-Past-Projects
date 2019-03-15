//Most of the code in this method comes from the Arduino and Java site:
//http://playground.arduino.cc/Interfacing/Java

const int NUMBER_OF_SHIFT_CHIPS = 2;

const int DATA_WIDTH = NUMBER_OF_SHIFT_CHIPS * 8;

/* You will need to change the "int" to "long" If the
 * NUMBER_OF_SHIFT_CHIPS is higher than 2.
*/

int ploadPin        = 8;  // Connects to Parallel load pin the 165
int clockEnablePin  = 9;  // Connects to Clock Enable pin the 165
int dataPin        = 11; // Connects to the Q7 pin the 165
int clockPin        = 12; // Connects to the Clock pin the 165

unsigned int pinValues;
unsigned int oldPinValues;

unsigned int read_shift_regs() {
    byte bitVal;
    unsigned int bytesVal = 0;

    /* Trigger a parallel Load to latch the state of the data lines,
    */
      digitalWrite(clockEnablePin, HIGH);
      digitalWrite(ploadPin, LOW);
      delayMicroseconds(5);
      digitalWrite(ploadPin, HIGH);
      digitalWrite(clockEnablePin, LOW);

    /* Loop to read each bit value from the serial out line
     * of the SN74HC165N.
    */
    for(int i = 0; i < DATA_WIDTH; i++) {
        bitVal = digitalRead(dataPin);

        /* Set the corresponding bit in bytesVal.
        */
        bytesVal |= (bitVal << ((DATA_WIDTH-1) - i));

        /* Pulse the Clock (rising edge shifts the next bit).
        */
        digitalWrite(clockPin, HIGH);
        delayMicroseconds(10);
        digitalWrite(clockPin, LOW);
    }

    return(bytesVal);
}

/*
void display_pin_values() {
    for(int i = 0; i < DATA_WIDTH; i++) {

        if((pinValues >> i) & 1)
            Serial.println(1);
        else
            Serial.println(0);
    }
}
*/

void setup() {
    Serial.begin(9600);

    /* Initialize our digital pins...
    */
    pinMode(ploadPin, OUTPUT);
    pinMode(clockEnablePin, OUTPUT);
    pinMode(clockPin, OUTPUT);
    pinMode(dataPin, INPUT);

    digitalWrite(clockPin, LOW);
    digitalWrite(ploadPin, HIGH);

    /* Read in and display the pin states at startup.
    */
    pinValues = read_shift_regs();
    oldPinValues = pinValues;
}

void loop() {
    /* Read the state of all zones.
    */
    pinValues = read_shift_regs();

    /* If there was a change in state, display which ones changed.
    */
    if(pinValues != oldPinValues) {
//        display_pin_values();
        Serial.println(pinValues);
        oldPinValues = pinValues;
    }

    delay(5);
}
