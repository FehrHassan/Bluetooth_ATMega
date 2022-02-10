/*
 * UART.h
 */  

#ifndef UART_H_
#define UART_H_

#include "micro_config.h"
#include "std_types.h"
#include "common_macros.h"



void UART_init(void);

void UART_sendByte(const uint8 data);

void UART_sendString(char* data);

uint8 UART_recieveByte(void);

#endif /* UART_H_ */