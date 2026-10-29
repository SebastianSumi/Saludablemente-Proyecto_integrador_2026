package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service;

import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.*;

public interface AsociacionClinicaService {
    AlertaEtiquetasResponse etiquetasDeAlerta(Long id);
    AlertaEtiquetasResponse guardarEtiquetasDeAlerta(Long id, GuardarEtiquetasRequest request);
    EtiquetaAlertasResponse alertasDeEtiqueta(Long id);
    EtiquetaAlertasResponse guardarAlertasDeEtiqueta(Long id, GuardarAlertasRequest request);
    ParametroEtiquetasResponse etiquetasDeParametro(Long id);
    ParametroEtiquetasResponse guardarEtiquetasDeParametro(Long id, GuardarEtiquetasRequest request);
    EtiquetaParametrosResponse parametrosDeEtiqueta(Long id);
    EtiquetaParametrosResponse guardarParametrosDeEtiqueta(Long id, GuardarParametrosRequest request);
}
