/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.balanzasserie.logica;

import com.balanzasserie.logica.utils.NumeroUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * Esta salida permite conectar la balanza a una
computadora , el pin 2 es TXD (salida de datos) pin 5 GND (maza).
los parametros de transmision son fijos: 1200 Baud, 8 Bit, Sin Paridad, Un Bit de Stop.
Se envia continuamente un string de ocho caracteres con el siguiente formato:
"S P P P P P P CR"
Siendo S= Byte de estatus, P= Peso en ASCII, CR= Retorno de carro.(0Dh)
Byte de Estatus en Binario Byte de Estatus en ASCII
Bit 0= Neto @...C Peso positivo fuera de equilibrio
Bit 1= Centro de cero D Peso bruto mayor que cero.
Bit 2= Equilibrio E Peso neto mayor que cero.
Bit 3= Peso negativo G Centro de cero sin tara.
Bit 4= fuera de rango H...K Peso negativo fuera de equilibrio.
Bit 5= 0 L...O Peso negativo en equilibrio.
Bit 6= 1 P..._ Fuera de rango.
Bit 7= 2
 */
public class Medicion {

    private String puerto;
    private Balanza balanza;
    private Date fecha;
    private byte estatus;
    private int peso;
    private String pesoConUnidad;
    private byte retornoCarro;
    private byte[] bytes;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final static String SPACER = "  ";
    private final static String UNIDAD = " Kg";

    public Medicion(String puerto, byte[] bytes) {
        this.bytes = bytes;
        this.puerto = puerto;
        this.balanza = setBalanza(puerto);
        fecha = new Date();
        // "S P P P P P P CR" Siendo S= Byte de estatus, P= Peso en ASCII, CR= Retorno de carro.(0Dh)
        if (bytes.length == 8) {
            estatus = bytes[0];
            byte[] tempPeso = new byte[6];
            for (int i = 0; i < 6; i++) {
                tempPeso[i] = bytes[i + 1];
            }

            String tempPesoStr = new String(tempPeso);
            if (NumeroUtil.isEntero(tempPesoStr)) {
                pesoConUnidad = tempPesoStr + UNIDAD;
                peso = Integer.parseInt(tempPesoStr);
            } else {
                pesoConUnidad = "0" + UNIDAD;
                peso = 0;
            }
            retornoCarro = bytes[7];
        }
    }

    public Balanza getBalanza() {
        return balanza;
    }

    public void setBalanza(Balanza balanza) {
        this.balanza = balanza;
    }

    public int getPeso() {
        return peso;
    }

    public void setPeso(int peso) {
        this.peso = peso;
    }

    public boolean isValid() {
        return (bytes.length == 8);
    }

    private boolean isBitActive(byte b, int bitAVerificar) {
        return (b >> (bitAVerificar - 1) & 1) == 1;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public SimpleDateFormat getDateFormatter() {
        return dateFormatter;
    }

    public void setDateFormatter(SimpleDateFormat dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    public byte getEstatus() {
        return estatus;
    }

    public void setEstatus(byte estatus) {
        this.estatus = estatus;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getPesoConUnidad() {
        return pesoConUnidad;
    }

    public void setPesoConUnidad(String peso) {
        this.pesoConUnidad = peso;
    }

    public String getPuerto() {
        return puerto;
    }

    public void setPuerto(String puerto) {
        this.puerto = puerto;
    }

    public byte getRetornoCarro() {
        return retornoCarro;
    }

    public void setRetornoCarro(byte retornoCarro) {
        this.retornoCarro = retornoCarro;
    }

    @Override
    public String toString() {
        return balanza + SPACER + dateFormatter.format(fecha) + SPACER + pesoConUnidad;
    }

    private Balanza setBalanza(String puerto) {
        Balanza balanzaTmp = Balanza.BALANZA1;
        if (puerto.equalsIgnoreCase(PropertiesManager.getBalanza2SerialParameters().getPortName())) {
            balanzaTmp = Balanza.BALANZA2;
        }

        return balanzaTmp;
    }

    public String getFechaStr() {
        if (fecha != null) {
            return dateFormatter.format(fecha);
        }

        return "";
    }
}
