package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.AgregarServicioPortafolioCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult.PortafolioResult;
public interface AgregarServicioPortafolioUseCase {
    PortafolioResult ejecutar(AgregarServicioPortafolioCommand command);
}
