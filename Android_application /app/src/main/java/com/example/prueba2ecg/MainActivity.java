
package com.example.prueba2ecg;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //Botones pantalla inicial
    private Button conectar;
    private Button desconectar;
    private Button electrodos;
    private Button obtenersenal;
    private TextView inferencia;


    //TexView pantalla inicial
    private TextView estadoBL;
    private TextView porcentajeBa;
    private TextView izquierdo;
    private TextView derecho;
    private TextView estadosenal;
    private TextView sana;
    //private TextView arritmia;
    //private TextView ruido;


    //Variables Bluetooth
    public String address = "30:AE:A4:6A:BE:8A";
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothDevice esp = bluetoothAdapter.getRemoteDevice(address);
    BluetoothSocket mmSocket = null;



    //Variables dentro de Android
    //Variables para el metodo leer
    InputStream mmInStream;
    int[] mensajeESG = {};
    //variables para el metodo escribir
    int entrada = 0;
    OutputStream mmOutStrim;
    float[] myIntArray = new float[2049];


    //Declaracion varoable lite

    float media = (float) 700;
    float desviacionEstandar = (float) 1700;
    Interpreter tflite;
    float[][][] Input = new float[1][2049][1];

    //Declaracion de variables para Grafica
    LineGraphSeries<DataPoint> series;
    GraphView graph;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Relacionado botones
        conectar = findViewById(R.id.conectar);
        desconectar = findViewById(R.id.desconectar);
        electrodos = findViewById(R.id.electrodos);
        obtenersenal = findViewById(R.id.obtenersenal);


        //Realacion viewText
        estadoBL = findViewById(R.id.viewEstado);
        porcentajeBa = findViewById(R.id.porcentaje);
        izquierdo = findViewById(R.id.iz);
        derecho = findViewById(R.id.de);
        estadosenal = findViewById(R.id.estadosenal);
        sana = findViewById(R.id.sana);
        //arritmia = findViewById(R.id.arritmia);
        //ruido = findViewById(R.id.ruido);

        graph = (GraphView) findViewById(R.id.graph);

        //Declaracion de lite
        try{
            tflite = new Interpreter(loadModuleFile());
        } catch (IOException e) {
            e.printStackTrace();
        }




        // Llamada al metodo Run Para conetar el Bluetooth
        conectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                run();
                entrada = 99;
                escribir();
                leer();

                float N_float = 0;
                String mensajeT = "";
                int contador = 0;

                for (int i = 0; i < mensajeESG.length; i++) {
                    if(mensajeESG[i] != 32){
                        mensajeT = mensajeT + (char) mensajeESG[i];
                    }
                    else{
                        N_float = Float.parseFloat(mensajeT);
                        myIntArray[contador] = N_float;
                        mensajeT = "";
                        contador++;
                    }
                }
                porcentajeBa.setText(Float.toString(myIntArray[0]));

            }
        });

        //Llamada al metodo desconectar
        desconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Desconectar();
            }
        });

        // Estado de electrodos
        electrodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entrada = 98;
                escribir();
                leer();

                float N_float = 0;
                String mensajeT = "";
                int contador = 0;

                for (int i = 0; i < mensajeESG.length; i++) {
                    if(mensajeESG[i] != 32){
                        mensajeT = mensajeT + (char) mensajeESG[i];
                    }
                    else{
                        N_float = Float.parseFloat(mensajeT);
                        myIntArray[contador] = N_float;
                        mensajeT = "";
                        contador++;
                    }
                }

                System.out.println(myIntArray[0]);
                System.out.println(myIntArray[1]);

                if(myIntArray[0] == 0){
                    izquierdo.setText("CONNECTED");
                }
                else{
                    izquierdo.setText("DISABLED");
                }

                if(myIntArray[1] == 0){
                    derecho.setText("CONNECTED");
                }
                else{
                    derecho.setText("DISABLED");
                }
            }




        });





        //Obtener senal
        obtenersenal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                estadosenal.setText("ACHIEVE SIGNAL ");

                graph.removeAllSeries();
                graph.getViewport().setMinX(0);
                graph.getViewport().setMaxX(2);
                //graph.getViewport().setMinY(-1);

                graph.getViewport().setScrollable(true);
                graph.getViewport().setScrollableY(true);
                graph.getViewport().setScalable(true);
                graph.setTitle("ACHIEVE SIGNAL ");


                entrada = 100;
                escribir();
                leer();

                entrada = 101;
                escribir();
                leer();
                float N_float = 0;
                String mensajeT = "";
                int contador = 0;

                for (int i = 0; i < mensajeESG.length; i++) {
                    if(mensajeESG[i] != 32){
                        mensajeT = mensajeT + (char) mensajeESG[i];
                    }
                    else{
                        N_float = Float.parseFloat(mensajeT);
                        myIntArray[contador] = N_float;
                        mensajeT = "";
                        contador++;
                    }
                }

                estadosenal.setText("SIGNAL OBTAINED");

                for(int i = 0; i < myIntArray.length; i++){
                    Input[0][i][0] = ((myIntArray[i])- desviacionEstandar)/media;

                    System.out.printf("%d  %.6f \n", i, Input[0][i][0]);
                }

                double x , y;
                x = 0;

                series = new LineGraphSeries<DataPoint>();
                for (int i = 0; i< 2049; i ++){
                    x = x + 0.004;
                    y = Input[0][i][0];
                    series.appendData(new DataPoint(x,y), true, 2049);
                }
                graph.addSeries(series);

                // imferencia de la red
                float[] prediction = inference(Input);

                System.out.println(prediction[0]);
                System.out.println(prediction[1]);
                System.out.println(prediction[2]);

                float mayor = 0;
                int posicion = 0;

                if (prediction[0] >= prediction[1] && prediction[0] >= prediction[2]){
                    mayor = prediction[0];
                }

                if (prediction[1] >= prediction[0] && prediction[1] >= prediction[2]){
                    mayor = prediction[1];
                }

                if (prediction[2] >= prediction[1] && prediction[2] >= prediction[0]){
                    mayor = prediction[2];
                }

                for (int i=0; i < prediction.length; i++){

                    if(mayor != prediction[i]){
                        posicion++;
                    }

                    else{
                        break;
                    }

                }

                System.out.println(posicion);
                if(posicion == 0){
                    sana.setText(" Normal Signal ");
                }
                if(posicion == 1){
                    sana.setText(" Arrhythmia signal ");
                }
                if(posicion == 2){
                    sana.setText(" Noise ");
                }

                //sana.setText(Float.toString(prediction[0]));
                //arritmia.setText(Float.toString(prediction[1]));
                //ruido.setText(Float.toString(prediction[2]));

                //Porcentaje Bateria
                entrada = 99;
                escribir();
                leer();

                N_float = 0;
                mensajeT = "";
                contador = 0;

                for (int i = 0; i < mensajeESG.length; i++) {
                    if(mensajeESG[i] != 32){
                        mensajeT = mensajeT + (char) mensajeESG[i];
                    }
                    else{
                        N_float = Float.parseFloat(mensajeT);
                        myIntArray[contador] = N_float;
                        mensajeT = "";
                        contador++;
                    }
                }
                porcentajeBa.setText(Float.toString(myIntArray[0]));

            }
        });

        //inferencia de la red

    }




    //Metodo para conectarce a bluetooth
    public void run() {
        bluetoothAdapter.cancelDiscovery();
        try {
            mmSocket = esp.createRfcommSocketToServiceRecord(mUUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mmSocket.connect();
            if(mmSocket.isConnected()){
                estadoBL.setText("CONNECTED");
            }
            else{
                estadoBL.setText("DISCONNECTED");
            }
        } catch (IOException connectException) {
        }
    }


    //Metodo para descoebtar BL
    public void Desconectar(){
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.close();

            estadoBL.setText("DESCONECTADO");

        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.

        }
    }


    //Metodo para leer los datos de entrada por bluetooth
    public void leer(){
        byte b = 0;
        int[] mensaje = {};
        int N =0;
        while (b != 35){
            b = 0;
            try {
                mmInStream = mmSocket.getInputStream();
                mmInStream.available();
                b = (byte) mmInStream.read();

                if(b != 35 ){
                    N = mensaje.length;
                    mensaje = Arrays.copyOf(mensaje,N+1);
                    mensaje[N] = (int) b;

                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

        mensajeESG = mensaje;

        String mensajeT = "";
        int T =0;
        for (int i = 0; i < mensaje.length; i++) {
            if(mensaje[i] != 10){
                mensajeT = mensajeT + (char) mensaje[i];
            }
        }
    }



    //Metodo para escribir datos por bluetooth
    public void escribir() {
        try {
            mmOutStrim = mmSocket.getOutputStream();
            mmOutStrim.write(entrada);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    //Tensorflow  lite

    public float[] inference(float[][][] s){
        float [][][] inputValues = new float[1][1500][1];
        inputValues = s;

        float[][] outputValue = new float[1][3];
        tflite.run(inputValues, outputValue);
        float[] inferedValue = outputValue[0];
        return inferedValue;
    }



    private MappedByteBuffer loadModuleFile() throws IOError, IOException {

        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("model.tflite");
        FileInputStream fileInputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffSets = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffSets,declaredLength);
    }

}