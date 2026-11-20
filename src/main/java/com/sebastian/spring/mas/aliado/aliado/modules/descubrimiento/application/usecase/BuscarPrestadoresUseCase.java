package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.dto.BuscarPrestadoresQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.dto.BusquedaPrestadoresResult;

public interface BuscarPrestadoresUseCase {
    BusquedaPrestadoresResult ejecutar(BuscarPrestadoresQuery query);
}

