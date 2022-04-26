package com.southconqueror.impresiones.logica.impresoras;

import com.southconqueror.impresiones.entidades.Factura;

public interface ImpresoraFiscal {

     void configurar() throws Exception;

     String imprimirFactura(Factura factura) throws Exception;


}
