/*
 * CProject.c
 *
 * Created: 09-Feb-22 01:04:38 AM
 * Author : fehrhassan
 */ 

#include "UART.h"
#include "ADC.h"


void init();
void init_timer();
void setPeriod(uint8 dutyCycle);
char* stringConcat (char* s1, char* s2);
void returnStatus();




ISR(INT0_vect)
{
	char * buttonState;
	CLEAR_BIT(GICR,INT0);
	if (BIT_IS_SET(PIND,PIND2)) {
		_delay_ms(200);
		if (BIT_IS_SET(PIND,PIND2)) {
			buttonState = "#BR#";
			UART_sendString(buttonState);
			}
		} else if (BIT_IS_CLEAR(PIND, PIND2)) {
			_delay_ms(200);
			if (BIT_IS_CLEAR(PIND,PIND2)) {
				buttonState = "#BP#";
				UART_sendString(buttonState);
			}
	}
	SET_BIT(GICR,INT0);
		
}

ISR(ADC_vect)
{
	_delay_ms(10);
	uint8 readingl = ADCL;
	uint8 readingh = ADCH ;
	uint16 reading = (readingh << 8) | readingl;
	reading = reading * 5.0 / 10.23;
	char msg[16];
	itoa(reading,msg,10);
	char * string = "T\0";
	string = stringConcat (string, msg);
	strcat(string, "#");
	UART_sendString(string);
}

ISR(USART_RXC_vect)
{
	uint8 receivedData = UART_recieveByte();
	switch (receivedData) {
		// Control the led.
		case 48:
		CLEAR_BIT(PORTA,PORTA0);
		break;
		case 49:
		SET_BIT(PORTA, PORTA0);
		break;
		
		// Get the temperature.
		case 51:
		ADC_read_interrupt_driven(1);
		break;
		
		// Control the motor speed.
		case 50:
		setPeriod(0);
		break;
		case 52:
		setPeriod(50);
		break;
		case 53:
		setPeriod(100);
		break;
		
		// Get the status.
		case 54:
		returnStatus();
		break;
		
		default:
		break;
	}
}

int main(void)
{
	init();
	UART_init();
	init_timer();
	setPeriod(0);
	ADC_init();
	ADC_read_interrupt_driven(1);
	
	/* Replace with your application code */
	while (1)
	{
		
	}
}

void init() {
	cli();
	SET_BIT(DDRA, DDA0); // LED is Connected.
	CLEAR_BIT(DDRA, DDA1); // used as ADC -- Connected to Temperature sensor LM35.
	
	CLEAR_BIT(DDRD, DDD2); // Button is connected to activate external interrupt.
	
	
	SET_BIT(GICR,INT0); // Allow Interrupt 
	SET_BIT(MCUCR,ISC00); // Any logical change on the INT0 pin generates an interrupt request (rising and falling edge).
	CLEAR_BIT(MCUCR,ISC01); // Any logical change on the INT0 pin generates an interrupt request (rising and falling edge).
	sei(); // Allow Global Interrupts
}

void init_timer()
{
	DDRB |= (1 << DDB3);
	// Mode = Fast PWM
	// Non inverting mode
	// Clock select F_CPU/1024 = 1MHZ/1024 = 1 KHZ
	TCCR0 |= (1 << WGM01) | (1 << WGM00) | (1 << COM01) | (1 << CS01) | (1 << CS00);
	
	
}

void setPeriod(uint8 dutyCycle)
{
	uint8 val = ((dutyCycle * 255)/100);
	OCR0 = val;
}

char* stringConcat (char* s1, char* s2){
	char* s3 = ""; //Text Holder
	for (uint8 n = 0; n <= 16; n++)
	s3[n] = '\0';
	 uint8 i,j=0,count=0;
	 for(i=0; ;i++)          //Copying string 1 to string 3
	 {
		 if(s1[i] == '\0') break;
		 s3[i]=s1[i];
	 }
	 for(;;i++)     //Copying string 2 to the end of string 3
	 {
		 s3[i]=s2[j];
		 j++;
		 if (s2[j] == '\0') break;
	 }
	 
	 return s3;
}

void returnStatus()
{
	
	if (BIT_IS_SET(PIND,PIND2)) 
	{
		char *buttonState = "#BR#";
	
		UART_sendString(buttonState);
	} 
	else 
	{
		char* buttonState = "#BP#";
		UART_sendString(buttonState);
	}
	_delay_ms(500);
	if (BIT_IS_SET(PORTA,PORTA0))
	{
		char* ledState = "#LN#";
		UART_sendString(ledState);
	}
	else
	{
		char* ledState = "#LF#";
		UART_sendString(ledState);
	}
}