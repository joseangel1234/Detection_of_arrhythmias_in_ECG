
//Codigo para ESP32 definido para optener una senal ECG y enviar a un Smartphone via Bluetooth
//Fecha: 6 de Agosto del 2021
//Autore: Meliza Flores, Jose Duarte. Etudiantes Ing Electronica UIS


//Librerias a usar
#include "BluetoothSerial.h"
BluetoothSerial SerialBT;



//Puertos usados en la Esp32
const int input_senal_ecg = 27;
const int input_conection_electrode_left = 32;
const int input_conection_electrode_right = 35;
const int out_control_mode_ad = 25;
const int input_battery_charge = 33;





//Definicion de variables en memori45tga de la Esp32
int signal_ecg[2049];
int left_electrode = 1;
int right_electrode = 1;
int battery_charge = 0;
int estado_Electrodos[2];



void setup() {
  Serial.begin(115200);   //Definion de la celocidad del Puesto serial
  SerialBT.begin("ECG_UIS_E3T"); //Definicion del nombre del dispisitivo Bluetooth

  //Definicion de los modos ENTRADA-SALIDA de los puestos de la Esp32
  pinMode(input_senal_ecg, INPUT);
  pinMode(input_conection_electrode_left, INPUT);
  pinMode(input_conection_electrode_right, INPUT);
  pinMode(out_control_mode_ad, OUTPUT);
  pinMode(input_battery_charge, INPUT);
}


void loop() {

  if (Serial.available()) {
    SerialBT.write(Serial.read());
  }
  

  if (SerialBT.available()) {
    int a = SerialBT.read();
    
    if(a== 99){
        battery_charge = analogRead(input_battery_charge);
        digitalWrite(out_control_mode_ad,HIGH);

        int promedio = 0;
        for (int n = 0; n < 10; n++){
          promedio = promedio + battery_charge; 
          }

          battery_charge = promedio/10.0;

        int porcentaje = -0.2*(1985-battery_charge);
        
        if(porcentaje > 0){
          int numero = porcentaje;
          int c_numero = numero;
          int cifreas = 0;
         
          while(numero > 0){
            numero = numero / 10;
            cifreas++;
          }
        
          int numero_des[cifreas];
  
          for(int i = cifreas - 1; 0 <= i; i--){
            numero_des[i] = c_numero % 10;
            c_numero = c_numero / 10;
          }
    
          for(int j = 0; j < cifreas; j++){
            int  m =  numero_des[j];
            String r = String(m);
            char a = r[0];       
            SerialBT.write(a);
          }
          SerialBT.write(' ');
        }

        if(porcentaje == 0){
          SerialBT.write('0');
          SerialBT.write(' ');
        }
        
        Serial.println(porcentaje);
        SerialBT.write(35);
    }


    //Bloque de codigo para el estado de los electrodos
    if(a== 98){


        left_electrode = digitalRead(input_conection_electrode_left);
        right_electrode = digitalRead(input_conection_electrode_right);

        estado_Electrodos[1] = left_electrode;
        estado_Electrodos[0] = right_electrode;

        Serial.println(estado_Electrodos[0]);
        Serial.println(estado_Electrodos[1]);
        Serial.println();

        
      for(int k=0; k<2; k++){

        if(estado_Electrodos[k] > 0){
          int numero = estado_Electrodos[k];
          int c_numero = numero;
          int cifreas = 0;
         
          while(numero > 0){
            numero = numero / 10;
            cifreas++;
          }
        
          int numero_des[cifreas];
  
          for(int i = cifreas - 1; 0 <= i; i--){
            numero_des[i] = c_numero % 10;
            c_numero = c_numero / 10;
          }
    
          for(int j = 0; j < cifreas; j++){
            int  m =  numero_des[j];
            String r = String(m);
            char a = r[0];       
            SerialBT.write(a);
          }
          SerialBT.write(' ');
        }

        if(estado_Electrodos[k] == 0){
          SerialBT.write('0');
          SerialBT.write(' ');
        }               
    }
    SerialBT.write(35);
  }
    
    //Bloque de codigo para cacturar la senal
    if(a == 100){
        digitalWrite(out_control_mode_ad,HIGH);          
        for(int i=0; i<2049; i++){
          signal_ecg[i]= analogRead(input_senal_ecg);

          Serial.println(signal_ecg[i]);
          delay(4);        
        } 
        SerialBT.write(35);   
    }

    
    //Bloque de codigo para enviar la senal
    if(a == 101){

      for(int k=0; k<2049; k++){

        if(signal_ecg[k] > 0){
          int numero = signal_ecg[k];
          int c_numero = numero;
          int cifreas = 0;
         
          while(numero > 0){
            numero = numero / 10;
            cifreas++;
          }
        
          int numero_des[cifreas];
  
          for(int i = cifreas - 1; 0 <= i; i--){
            numero_des[i] = c_numero % 10;
            c_numero = c_numero / 10;
          }
    
          for(int j = 0; j < cifreas; j++){
            int  m =  numero_des[j];
            String r = String(m);
            char a = r[0];       
            SerialBT.write(a);
          }
          SerialBT.write(' ');
        }

        if(signal_ecg[k] == 0){
          SerialBT.write('0');
          SerialBT.write(' ');
        }               
    }
    SerialBT.write(35);
          
   }
        
  }
}
