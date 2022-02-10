/*
 * UART.c
 */

#include "UART.h"
 
void UART_init(void)
{

	UCSRA = (1<<U2X); //double transmission speed
	
	UCSRB = (1<<RXCIE) | (1<<RXEN) | (1<<TXEN); //enable UART as transmitter and receiver.
	
	UCSRC = (1<<URSEL) | (1<<UCSZ0) | (1<<UCSZ1); //8-bit data, NO parity, one stop bit and asynch 
	
	/* baud rate=9600 & Fosc=1MHz -->  UBBR=( Fosc / (8 * baud rate) ) - 1 = 12 */  
	UBRRH = 0;
	UBRRL = 12;
}
	
void UART_sendByte(const uint8 data)
{
	while(BIT_IS_CLEAR(UCSRA,UDRE));
	UDR = data;
}

void UART_sendString(char * data)
{
	int i = 0;
	if (data[i] == '#')
	{
		UART_sendByte(data[0]);
		i++;	
	} else {
		UART_sendByte('#');
	}
	
	while(data[i] != '#')
	{
		UART_sendByte(data[i]);
		i++;
	}
	UART_sendByte(0x0A);
	
}
 

uint8 UART_recieveByte(void)
{
    return UDR;		
}