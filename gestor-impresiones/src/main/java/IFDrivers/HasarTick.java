
// Este modulo contiene el c�digo a disposicion por parte de IFDRIVERS
// en una base TAL CUAL. Todo receptor del Modulo se considera
// bajo licencia de los derechos de autor de IFDRIVERS para utilizar el
// codigo fuente siempre en modo que el o ella considere conveniente,
// incluida la copia, la compilacion, su modificacion o la redistribucion,
// con o sin modificaciones. Ninguna licencia o patentes de IFDRivers
// este implicita en la presente licencia.
//
// El usuario del codigo fuente debera entender que IFDRIVERS no puede
// Proporcionar apoyo tecnico para el modulo y no sera Responsable
// de las consecuencias del uso del programa.
//
// Todas las comunicaciones, incluida esta, no deben ser removidos
// del modulo sin el consentimiento previo por escrito de IFDRIVERS
// www: http://www.impresoras-fiscales.com.ar/
// email: soporte@impresoras-fiscales.com.ar

// Instrucciones para usar el driver y las funciones de alto nivel en Java:
//
// 1) Instale la libreria H71532Jv.dll. Para que Java pueda encuentrar
//    la libreria, esta debe estar instalada en un directorio que este incluido en 
//    la variable de entorno PATH. 
// 2) Agregue este archivo con la clase HasarTick a su proyecto.
// 3) Declare y cree la clase en su codigo. 
//    Todas las funciones de la clase HasarTick seran accesibles tambien 
//    desde esta clase: IF_OPEN, IF_CLOSE,etc. mas las funciones de alto nivel.
// 
// Por ejemplo:
//
// import IFDrivers.HasarTick;
//
// HasarTick m_objHasarTick = new HasarTick();
//
// int nError = m_objHasarTick.IF_OPEN("COM1",9600);
//
// ....etc
//
//IMPORTANTE: No debera renombrar ni el nombre del Package ni de la clase. De lo contrario,
//el driver dejara de funcionar.
//
package IFDrivers;

import com.southconqueror.impresiones.logica.utils.AppProperties;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Marcelo
 */
public class HasarTick {

    private static Logger logger = LogManager.getLogger(HasarTick.class);

    /**
     * Abrir el puerto de comunicaciones
     *
     * @param strPort Puerto de comunicaciones
     * @param nSpeed  Velocidad del puerto
     * @return 0 si no hay error, -1 si se produjo un error
     */
    public native int IF_OPEN(String strPort, int nSpeed);

    /**
     * Cerrar el puerto de comunicaciones
     *
     * @return 0 si no hay error, -1 si se produjo un error
     */
    public native int IF_CLOSE();

    /**
     * Leer un campo de la respuesta del controlador fiscal
     *
     * @param nField Nro del campo de la respuesta fiscal a recuperar
     * @return El valor del campo
     */
    public native String IF_READ(int nField);

    /**
     * Enviar un comando a la impresora fiscal
     *
     * @param strCommand Comando a enviar
     * @return 0 si no hay error, -1 si se produjo un error
     */
    public native int IF_WRITE(String strCommand);

    /**
     * Leer el c�digo de estado del mecanismo impresor
     *
     * @param nBit Nro del bit a leer (1 a 16)
     * @return 1 si esta en On, 0 si esta en Off
     */
    public native int IF_ERROR1(int nBit);

    /**
     * Leer el c�digo de estado del controlador fiscal
     *
     * @param nBit Nro del bit a leer (1 a 16)
     * @return 1 si esta en On, 0 si esta en Off
     */
    public native int IF_ERROR2(int nBit);

    /**
     * Habilitar o deshabilitar la depuraci�n de comandos
     *
     * @param nTrace 1 para habilitar, 0 para deshabilitar la depuracion
     */
    public native void IF_TRACE(int nTrace);

    public native void IF_SERIAL(String strSerial);

    private final static String SERIAL_ID = "27-0163848-435";

    private final static String DEFAULT_PRINTER_DLL = "H71532Jv";
    private final static String PRINTER_250F_DLL = "h25032Jv";

    public static void cargarLibreriaNativa() {
        if (AppProperties.getInstance().getModeloImpresora().contains("250F")) {
            logger.info("Cargando DLL para modelo [250F]");
            System.loadLibrary(PRINTER_250F_DLL);
        } else {
            logger.info("Cargando DLL para modelo [default]");
            System.loadLibrary(DEFAULT_PRINTER_DLL);
        }
    }

    //***************************************************************
    // 1. Comandos de inicializaci�n

    /**
     * Configuraci�n del controlador en bloque
     *
     * @param dblVar1 L�mite de ingreso datos consumidor (nnnnnn.nn)
     * @param dblVar2 L�mite ticket factura (nnnnnn.nn)
     * @param dblVar3 Porcentaje IVA responsable inscripto (nn.nn)
     * @param byVar4  Cantidad de copias
     * @param byVar5  Impresi�n CAMBIO {PO}
     * @param byVar6  Impresion leyendas opcionales {PO}
     * @param byVar7  Tipo de corte del papel de ticket {FPN}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int ConfigureControllerByBlock(Double dblVar1, Double dblVar2, Double dblVar3,
                                          String byVar4, String byVar5,
                                          String byVar6, String byVar7) {
        setSerial();

        String strBuff = "@ConfigureControllerByBlock" + "|" + dblVar1.toString() + "|" +
                dblVar2.toString() + "|" + dblVar3.toString() + "|" +
                byVar4 + "|" + byVar5 + "|" + byVar6 + "|" + byVar7;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Config del controlador por par�metros
     *
     * @param byVar1  Par�metro a modificar {456}
     * @param strVar2 Valor del par�metro
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int ConfigureControllerByOne(String byVar1, String strVar2) {
        setSerial();

        String strBuff = "@ConfigureControllerByOne" + "|" + byVar1 + "|" + strVar2;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Cambio de responsabilidad frente al IVA
     *
     * @param byVar1 Responsabilidad frente al IVA {INEAMS}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int ChangeIVAResponsability(String byVar1) {
        setSerial();

        String strBuff = "@ChangeIVAResponsability" + "|" + byVar1;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Cambio de nro de ingresos brutos
     *
     * @param strVar1 Nro de ingresos brutos (max 20 bytes)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int ChangeIBNumber(String strVar1) {
        setSerial();

        String strBuff = "@ChangeIBNumber" + "|" + strVar1;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    //***************************************************************
    // 2. Comandos de diagn�stico

    /**
     * Consulta de estado
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int StatusRequest() {
        setSerial();

        String strBuff = "@StatusRequest";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Consulta de configuraci�n
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int GetConfigurationData() {
        setSerial();

        String strBuff = "@GetConfigurationData";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Consulta de datos de inicializaci�n
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int GetInitData() {
        setSerial();

        String strBuff = "@GetInitData";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Consulta de versi�n de controlador fiscal
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int GetPrinterVersion() {
        setSerial();

        String strBuff = "@GetPrinterVersion";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    //***************************************************************
    // 3. Comandos de control fiscal

    /**
     * Capacidad restante
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int HistoryCapacity() {
        setSerial();

        String strBuff = "@HistoryCapacity";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Cierre de jornada fiscal
     *
     * @param byVar1 Z: Cierre de jornada fiscal; X: Informe X {XZ}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int DailyClose(String byVar1) {
        setSerial();

        String strBuff = "@DailyClose" + "|" + byVar1;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Reporte de auditoria por fechas
     *
     * @param strVar1 Fecha inicial del per�odo (formato AAMMDD) (max 6 bytes)
     * @param strVar2 Fecha final del per�odo (formato AAMMDD) (max 6 bytes)
     * @param byVar3  T: datos globales; otro caracter: datos por Z {TO}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int DailyCloseByDate(String strVar1, String strVar2, String byVar3) {
        setSerial();

        String strBuff = "@DailyCloseByDate" + "|" + strVar1 + "|" + strVar2 + "|" +
                byVar3;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Reporte de auditoria por n�mero de Z
     *
     * @param nVar1  N�mero de Z inicial del per�odo (nnnn)
     * @param nVar2  N�mero de Z final del per�odo (nnnn)
     * @param byVar3 T: datos globales; otro caracter: datos por Z {TO}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int DailyCloseByNumber(Integer nVar1, Integer nVar2, String byVar3) {
        setSerial();

        String strBuff = "@DailyCloseByNumber" + "|" + nVar1.toString() + "|" + nVar2.toString() + "|" +
                byVar3;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Reporte de registro diario
     *
     * @param strVar1 N�mero de Z o fecha (campo de longitud variable) (max 6 bytes)
     * @param byVar2  Z: n�mero de Z; T: fecha {ZT}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int GetDailyReport(String strVar1, String byVar2) {
        setSerial();

        String strBuff = "@GetDailyReport" + "|" + strVar1 + "|" + byVar2;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Consulta de memoria de trabajo
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int GetWorkingMemory() {
        setSerial();

        String strBuff = "@GetWorkingMemory";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Iniciar informaci�n de IVA
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int SendFirstIVA() {
        setSerial();

        String strBuff = "@SendFirstIVA";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Continuar informaci�n de IVA
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int NextIVATransmission() {
        setSerial();

        String strBuff = "@NextIVATransmission";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    //***************************************************************
    // 4. Emisi�n de documentos fiscales y notas de cr�dito

    /**
     * Abrir comprobante fiscal
     *
     * @param byVar1 Tipo de documento {TABDE}
     * @param byVar2 T � S (valor fijo) {TS}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int OpenFiscalReceipt(String byVar1, String byVar2) {
        setSerial();

        String strBuff = "@OpenFiscalReceipt" + "|" + byVar1 + "|" + byVar2;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Imprimir texto fiscal
     *
     * @param strVar1 Texto fiscal (max 30 bytes)
     * @param byVar2  Par�metro display: 0, 1 o 2
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int PrintFiscalText(String strVar1, String byVar2) {
        setSerial();

        String strBuff = "@PrintFiscalText" + "|" + strVar1 + "|" + byVar2;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Imprimir �tem
     *
     * @param strVar1 Texto descripci�n del item (max 20 bytes)
     * @param dblVar2 Cantidad (nnnn.nnnnnnnnnn)
     * @param dblVar3 Precio unitario (nnnnnn.nn)
     * @param strVar4 Porcentaje IVA (nn.nn)/(**.**) (max 5 bytes)
     * @param byVar5  Calificador de la operaci�n {Mm}
     * @param strVar6 Impuestos internos
     * @param byVar7  Par�metro display: 0, 1 o 2 {012}
     * @param byVar8  T: precio total; otro car�cter: precio base {TBO}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int PrintLineItem(String strVar1, Double dblVar2, Double dblVar3, String strVar4,
                             String byVar5, String strVar6, String byVar7,
                             String byVar8) {
        setSerial();

        String strBuff = "@PrintLineItem" + "|" + strVar1 + "|" + dblVar2.toString() + "|" +
                dblVar3.toString() + "|" + strVar4 + "|" + byVar5 + "|" +
                strVar6 + "|" + byVar7 + "|" + byVar8;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Descuento/Recargo sobre �ltimo �tem vendido
     *
     * @param strVar1 Texto descripci�n (max 20 bytes)
     * @param dblVar2 Monto (nnnnnnn.nn)
     * @param byVar3  Imputaci�n {Mm}
     * @param byVar4  Par�metro display: 0, 1 o 2 {012}
     * @param byVar5  Calificador de monto {BTO}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int LastItemDiscount(String strVar1, Double dblVar2, String byVar3, String byVar4,
                                String byVar5) {
        setSerial();

        String strBuff = "@LastItemDiscount" + "|" + strVar1 + "|" + dblVar2.toString() + "|" +
                byVar3 + "|" + byVar4 + "|" + byVar5;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Descuento general
     *
     * @param strVar1 Texto descripci�n (max 20 bytes)
     * @param dblVar2 Monto (nnnnnnn.nn)
     * @param byVar3  Imputaci�n {Mm}
     * @param byVar4  Par�metro display: 0, 1 o 2 {012}
     * @param byVar5  Calificador de monto {BTO}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int GeneralDiscount(String strVar1, Double dblVar2, String byVar3, String byVar4,
                               String byVar5) {
        setSerial();

        String strBuff = "@GeneralDiscount" + "|" + strVar1 + "|" + dblVar2.toString() + "|" +
                byVar3 + "|" + byVar4 + "|" + byVar5;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Devoluci�n de envases, Bonificaciones y Recargos
     *
     * @param strVar1 Texto descripci�n (max 20 bytes)
     * @param dblVar2 Monto (nnnnnnn.nn)
     * @param strVar3 Porcentaje IVA (nn.nn)/(**.**) (max 5 bytes)
     * @param byVar4  Imputaci�n {Mm}
     * @param strVar5 Impuestos internos
     * @param byVar6  Par�metro display: 0, 1 o 2 {012}
     * @param byVar7  T:precio total, otro caracter precio base {BTO}
     * @param byVar8  Calificador de operaci�n {BO}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int ReturnRecharge(String strVar1, Double dblVar2, String strVar3, String byVar4,
                              String strVar5, String byVar6, String byVar7,
                              String byVar8) {
        setSerial();

        String strBuff = "@ReturnRecharge" + "|" + strVar1 + "|" + dblVar2.toString() + "|" +
                strVar3 + "|" + byVar4 + "|" + strVar5 + "|" +
                byVar6 + "|" + byVar7 + "|" + byVar8;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Recargo IVA a Responsable no Inscripto
     *
     * @param dblVar1 Monto (nnnnnnn.nn)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int ChargeNonRegisteredTax(Double dblVar1) {
        setSerial();

        String strBuff = "@ChargeNonRegisteredTax" + "|" + dblVar1.toString();

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Percepciones
     *
     * @param strVar1 Al�cuota IVA (nn.nn / **.**) (max 5 bytes)
     * @param strVar2 Texto descripci�n (max 20 bytes)
     * @param dblVar3 Monto (nnnnnnn.nn)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int Perceptions(String strVar1, String strVar2, Double dblVar3) {
        setSerial();

        String strBuff = "@Perceptions" + "|" + strVar1 + "|" + strVar2 + "|" + dblVar3.toString();

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Subtotal
     *
     * @param byVar1  Par�metro impresi�n
     * @param strVar2 Reservado (llenar con un caracter cualquiera) (max 26 bytes)
     * @param byVar3  Par�metro display: 0, 1 o 2 {012}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int Subtotal(String byVar1, String strVar2, String byVar3) {
        setSerial();

        String strBuff = "@Subtotal" + "|" + byVar1 + "|" + strVar2 + "|" + byVar3;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Total
     *
     * @param strVar1 Texto de descripci�n (max 30 bytes)
     * @param dblVar2 Monto pagado (nnnnnnn.nn)
     * @param byVar3  Calificador operaci�n {CT}
     * @param byVar4  Par�metro display: 0, 1 o 2 {012}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int TotalTender(String strVar1, Double dblVar2, String byVar3, String byVar4) {
        setSerial();

        String strBuff = "@TotalTender" + "|" + strVar1 + "|" + dblVar2.toString() + "|" +
                byVar3 + "|" + byVar4;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Cerrar comprobante fiscal
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int CloseFiscalReceipt() {
        setSerial();

        String strBuff = "@CloseFiscalReceipt";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    //***************************************************************
    // 5. Comandos de Comprobantes no fiscal

    /**
     * Abrir comprobante no fiscal
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int OpenNonFiscalReceipt() {
        setSerial();

        String strBuff = "@OpenNonFiscalReceipt";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Abrir comprobante no fiscal en impresora slip
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int OpenNonFiscalSlip() {
        setSerial();

        String strBuff = "@OpenNonFiscalSlip";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Imprimir texto no fiscal
     *
     * @param strVar1 Texto no fiscal (max 40 bytes)
     * @param byVar2  Par�metro display: 0, 1 o 2 {012}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int PrintNonFiscalText(String strVar1, String byVar2) {
        setSerial();

        String strBuff = "@PrintNonFiscalText" + "|" + strVar1 + "|" + byVar2;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Cerrar comprobante no fiscal
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int CloseNonFiscalReceipt() {
        setSerial();

        String strBuff = "@CloseNonFiscalReceipt";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    //***************************************************************
    // 6. Comandos de documentos no fiscales homologados

    /**
     * Abrir documento no fiscal homologado
     *
     * @param byVar1 Tipo de documento {RS}
     * @param byVar2 T � S (valor fijo) {TS}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int OpenDNFH(String byVar1, String byVar2) {
        setSerial();

        String strBuff = "@OpenDNFH" + "|" + byVar1 + "|" + byVar2;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Cerrar documento no fiscal homologado
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int CloseDNFH() {
        setSerial();

        String strBuff = "@CloseDNFH";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Documento No Fiscal Homologado para Farmacias
     *
     * @param nVar1 Cantidad de ejemplares a imprimir (n)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int DNFHFarmacias(Integer nVar1) {
        setSerial();

        String strBuff = "@DNFHFarmacias" + "|" + nVar1.toString();

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Documento No Fiscal Homologado, ticket de reparto
     *
     * @param nVar1 Cantidad de ejemplares a imprimir (n)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int DNFHReparto(Integer nVar1) {
        setSerial();

        String strBuff = "@DNFHReparto" + "|" + nVar1.toString();

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Datos del voucher de tarjeta de cr�dito 1
     *
     * @param strVar1 Nombre del cliente (max 30 bytes)
     * @param strVar2 Nombre tarjeta de cr�dito (max 20 bytes)
     * @param byVar3  Calificador de operaci�n {CVDA}
     * @param strVar4 N�mero de tarjeta (max 16 bytes)
     * @param strVar5 Fecha vencimiento tarjeta (AAMM) (max 4 bytes)
     * @param byVar6  Tipo de tarjeta usada {DC}
     * @param nVar7   Cantidad de cuotas (2 d�gitos) (nn)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int SetVoucherData1(String strVar1, String strVar2, String byVar3, String strVar4,
                               String strVar5, String byVar6, Integer nVar7) {
        setSerial();

        String strBuff = "@SetVoucherData1" + "|" + strVar1 + "|" + strVar2 + "|" + byVar3 + "|" +
                strVar4 + "|" + strVar5 + "|" + byVar6 + "|" +
                nVar7.toString();

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Datos del voucher de tarjeta de cr�dito 2
     *
     * @param strVar1  C�digo de comercio (max 15 bytes)
     * @param strVar2  N�mero de terminal (max 8 bytes)
     * @param strVar3  N�mero de lote (max 3 bytes)
     * @param strVar4  N�mero de cup�n (max 4 bytes)
     * @param byVar5   Ingreso de datos tarjeta { *}
     * @param byVar6   Tipo de operaci�n {NF}
     * @param strVar7  N�mero de autorizaci�n (max 6 bytes)
     * @param strVar8  Importe (max 15 bytes)
     * @param strVar9  N�mero de comprobante fiscal (max 20 bytes)
     * @param strVar10 Nombre del vendedor (max 20 bytes)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int SetVoucherData2(String strVar1, String strVar2, String strVar3, String strVar4,
                               String byVar5, String byVar6, String strVar7,
                               String strVar8, String strVar9, String strVar10) {
        setSerial();

        String strBuff = "@SetVoucherData2" + "|" + strVar1 + "|" + strVar2 + "|" + strVar3 + "|" +
                strVar4 + "|" + byVar5 + "|" + byVar6 + "|" +
                strVar7 + "|" + strVar8 + "|" + strVar9 + "|" + strVar10;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Imprimir voucher
     *
     * @param byVar1 Cantidad ejemplares a imprimir (m�ximo: 3) {0123}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int PrintVoucher(String byVar1) {
        setSerial();

        String strBuff = "@PrintVoucher" + "|" + byVar1;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    //***************************************************************
    // 7. Comandos de control de la impresora

    /**
     * Cancelaci�n
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int Cancel() {
        setSerial();

        String strBuff = "@Cancel";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * C�digo de barras
     *
     * @param byVar1  Tipo de barras {1234}
     * @param strVar2 Datos EAN 8 / UPCA / EAN 13 / ITS 2 de 5 (max 32 bytes)
     * @param byVar3  N: imprime n�meros; otro: no imprime n�meros {NO}
     * @param byVar4  Impresi�n (P: imprime en el momento; O: imprime al final {PO}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int BarCode(String byVar1, String strVar2, String byVar3, String byVar4) {
        setSerial();

        String strBuff = "@BarCode" + "|" + byVar1 + "|" + strVar2 + "|" + byVar3 + "|" +
                byVar4;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Avanzar papel de tickets
     *
     * @param nVar1 Cantidad de l�neas a avanzar (nn)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int FeedReceipt(Integer nVar1) {
        setSerial();

        String strBuff = "@FeedReceipt" + "|" + nVar1.toString();

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Avanzar papel cinta de auditor�a
     *
     * @param nVar1 Cantidad de l�neas a avanzar (nn)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int FeedJournal(Integer nVar1) {
        setSerial();

        String strBuff = "@FeedJournal" + "|" + nVar1.toString();

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Avanzar papeles de tickets y cinta de auditor�a
     *
     * @param nVar1 Cantidad de l�neas a avanzar (nn)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int FeedReceiptJournal(Integer nVar1) {
        setSerial();

        String strBuff = "@FeedReceiptJournal" + "|" + nVar1.toString();

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    //***************************************************************
    // 8. Comandos de fecha, hora, encabezamiento y cola de documentos

    /**
     * Ingresar fecha y hora
     *
     * @param strVar1 Fecha (formato AAMMDD) (max 6 bytes)
     * @param strVar2 Hora (formato HHMMSS) (max 6 bytes)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int SetDateTime(String strVar1, String strVar2) {
        setSerial();

        String strBuff = "@SetDateTime" + "|" + strVar1 + "|" + strVar2;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Consultar fecha y hora
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int GetDateTime() {
        setSerial();

        String strBuff = "@GetDateTime";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Programar texto de encabezamiento y cola de documentos
     *
     * @param nVar1   Nro de l�nea de encabezamiento (1-10) o cola (11-20) (nn)
     * @param strVar2 Texto de descripci�n (max 40 bytes)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int SetHeaderTrailer(Integer nVar1, String strVar2) {
        setSerial();

        String strBuff = "@SetHeaderTrailer" + "|" + nVar1.toString() + "|" + strVar2;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Reportar texto de encabezamiento y cola de documentos
     *
     * @param nVar1 Nro de l�nea de encabezamiento (1-10) o cola (11-20) (nn)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int GetHeaderTrailer(Integer nVar1) {
        setSerial();

        String strBuff = "@GetHeaderTrailer" + "|" + nVar1.toString();

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Datos comprador factura
     *
     * @param strVar1 Nombre (max 30 bytes)
     * @param strVar2 CUIT / Nro documento (max 11 bytes)
     * @param byVar3  Responsabilidad frente al IVA {INEACBMST}
     * @param byVar4  Tipo de documento {CL1234}
     * @param strVar5 Domicilio comercial (max 40 bytes)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int SetCustomerData(String strVar1, String strVar2, String byVar3, String byVar4,
                               String strVar5) {
        setSerial();

        String strBuff = "@SetCustomerData" + "|" + strVar1 + "|" + strVar2 + "|" + byVar3 + "|" +
                byVar4 + "|" + strVar5;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Programar texto del nombre de fantas�a del propietario
     *
     * @param byVar1  Nro de l�nea del nombre de fantas�a (1-2) {12}
     * @param strVar2 Texto de descripci�n (max 40 bytes)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int SetFantasyName(String byVar1, String strVar2) {
        setSerial();

        String strBuff = "@SetFantasyName" + "|" + byVar1 + "|" + strVar2;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Reportar texto del nombre de fantas�a del propietario
     *
     * @param byVar1 Nro de l�nea a reportar (1-2) {12}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int GetFantasyName(String byVar1) {
        setSerial();

        String strBuff = "@GetFantasyName" + "|" + byVar1;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Cargar informaci�n remito / comprobante original
     *
     * @param byVar1  Nro de l�nea de remito / comprobante original (1-2) {12}
     * @param strVar2 Texto de descripci�n (max 20 bytes)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int SetEmbarkNumber(String byVar1, String strVar2) {
        setSerial();

        String strBuff = "@SetEmbarkNumber" + "|" + byVar1 + "|" + strVar2;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Reportar informaci�n remito / comprobante original
     *
     * @param byVar1 Nro de l�nea a reportar (1-2) {12}
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int GetEmbarkNumber(String byVar1) {
        setSerial();

        String strBuff = "@GetEmbarkNumber" + "|" + byVar1;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Abrir gaveta
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int OpenDrawer() {
        setSerial();

        String strBuff = "@OpenDrawer";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Escribir en display
     *
     * @param byVar1  Campo sobre el que se escribe {KLN}
     * @param strVar2 Mensaje a exhibir (max 16 bytes)
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int WriteDisplay(String byVar1, String strVar2) {
        setSerial();

        String strBuff = "@WriteDisplay" + "|" + byVar1 + "|" + strVar2;

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    /**
     * Cancela cualquier documento fiscal o no fiscal abierto
     *
     * @return 0 si no hay error y != 0 si hay un error
     */
    public int Sincro() {
        setSerial();

        String strBuff = "@Sincro";

        int nError = IF_WRITE(strBuff);

        return (nError);
    }

    public void setSerial() {
        IF_SERIAL(SERIAL_ID);
    }

}
