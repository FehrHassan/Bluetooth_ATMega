/*
 * ADC.h
 */ 

#ifndef ADC_H_
#define ADC_H_

#include "micro_config.h"
#include "std_types.h"
#include "common_macros.h"

/* 
 * Description :
 * Function responsible for initialize the ADC driver. 
 */
void ADC_init(void);

/* 
 * Description :
 * Function responsible for read analog data from a certain ADC channel 
 * and convert it to digital using the ADC driver. 
 */
uint16 ADC_read(uint8 channel_num);
void ADC_read_interrupt_driven(uint8 channel_num);

#endif /* ADC_H_ */