# Matriz de Trazabilidad — Entrega 2 (JiyJu Glamping)

Amplía la matriz de la Entrega 1 con **el endpoint que expone cada regla**, el código HTTP con que
responde cuando se viola y la petición de la colección de Postman `docs/api/sga.postman_collection.json` que lo demuestra
(columna Colección: número de la petición).
Todas las reglas se validan en el **dominio** (agregado o servicio de dominio) y la API las traduce
a **409** con `ErrorResponse` (`GlobalExceptionHandler`). La forma del mensaje la valida Bean
Validation en el DTO y responde **400**.

## Reglas invariantes (sección 9)

| Regla | Dónde se implementa (dominio) | Prueba unitaria | Endpoint que la expone | HTTP | Colección |
|---|---|---|---|---|---|
| RN-01 Sin reservas activas solapadas | `DisponibilidadDomoService.evaluar` | `ServiciosDeDominioTest.noPermitirSolapamientoReservaActiva`, `CicloDeVidaReservaUseCaseTest.crearReservaSolapadaEsRechazada` | `POST /reservas`, `PUT /reservas/{codigo}` | 409 | 5.5 |
| RN-02 Capacidad como tope rígido | `Reserva.crear` (validarComposicion), `DisponibilidadDomoService`, `CotizadorEstanciaService` | `ReservaTest.noAdmitirMasOcupantesQueLaCapacidad`, `ServiciosDeDominioTest.noAdmitirOcupantesCapacidad` | `POST /reservas`, `PUT /reservas/{codigo}`, `POST /domos/{id}/cotizacion` | 409 | 5.3 |
| RN-03 Salida posterior a la entrada | `Estancia` (constructor) | `ValoresDelDominioTest.verificarFechaEstancia` | `POST /reservas`, `GET /domos/disponibles`, `POST /domos/{id}/cotizacion` | 409 | 5.1 |
| RN-04 Entrada no anterior a hoy | `Estancia.verificarQueNoEmpiezaAntesDe`, `Reserva.crear` | `ReservaTest.noCrearReservaPasado`, `ValoresDelDominioTest.rechazarEntradaAnteriorAHoy` | `POST /reservas`, `GET /domos/disponibles` | 409 | 5.2 |
| RN-05 Valor noche por noche | `CotizadorEstanciaService.cotizar` | `ServiciosDeDominioTest.calcularValorEstanciaNochePorNoche` | `POST /domos/{id}/cotizacion`, `GET /domos/disponibles` | 200 | 3.2 |
| RN-06 Ocupante facturable por umbral | `Ocupante.esFacturableEn` | `OcupanteTest.evaluarUmbralEdadFacturable`, `ReservaTest.losNoFacturablesTambienCuentanParaLaCapacidad` | `POST /domos/{id}/cotizacion` (desglose), `GET /reservas/{codigo}` (`facturable`) | 200 | 3.2, 4.1 |
| RN-07 Bloqueo vigente impide vender | `DisponibilidadDomoService.evaluar`, `Bloqueo.afecta` | `ServiciosDeDominioTest.unBloqueoVigenteImpideVender` | `POST /reservas` | 409 | 5.9 |
| RN-08 Solo transiciones permitidas | `EstadoReserva.puedeTransitarA`, `Reserva.transitarA` | `ReservaTest.rechazarTransicionEstadoInvalida` | `POST /reservas/{codigo}/confirmar` · `/cancelar` · `/check-in` · `/check-out` · `/no-show` | 409 | 5.12 |
| RN-09 Hora estimada antes de confirmar | `Reserva.confirmar`, `ConfirmacionReservaService` | `ReservaTest.exigirHoraEstimadaDeLlegadaParaConfirmar` | `POST /reservas/{codigo}/confirmar` | 409 | 5.11 |
| RN-10 Registro solo CONFIRMADA y desde la entrada | `Reserva.registrarLlegada`, `EntregaDomoService` | `ReservaTest.noRegistrarLlegadaAntesDeLaFechaDeEntrada` | `POST /reservas/{codigo}/check-in` | 409 | 5.16 |
| RN-11 Recibir solo si el domo está PREPARADO | `Domo.puedeRecibirGrupo`, `EntregaDomoService` | `DomoYTemporadaTest.noPermitirEntregaSiNoEstaPreparado` | `POST /reservas/{codigo}/check-in` | 409 | 3.10 |
| RN-12 La reserva inactiva libera sus noches | `EstadoReserva.esActiva`, `DisponibilidadDomoService` | `ServiciosDeDominioTest.unaReservaQueDejoDeEstarActivaLiberaSusNoches`, `CicloDeVidaReservaUseCaseTest.laCancelacionLiberaLaDisponibilidadDeInmediato` | `POST /reservas/{codigo}/cancelar` · `/no-show` | 200 | 4.8 |
| RN-13 Retención con la política congelada | `RetencionService` | `ServiciosDeDominioTest.laRetencionSeCalculaConLaPoliticaCongelada` | `POST /reservas/{codigo}/cancelar`, `POST /politica-cancelacion/versiones` | 200 | 4.8, 6.10 |
| RN-14 La modificación revalida y ajusta | `Reserva.modificar`, `Folio.registrarAjuste`, `DisponibilidadDomoService` | `ReservaTest.modificarRecalculaYDevuelveElValorAnterior`, `FolioTest.ajustePositivoYNegativoPorModificacion` | `PUT /reservas/{codigo}` | 409 / 200 | 5.13, 5.14 |
| RN-15 Pago con medio y fecha; saldo | `Folio.registrarPago`, `Folio.saldo` | `FolioTest.seAbreConElCargoDeAlojamientoYAdmitePagosParcialesConMediosDistintos` | `POST /reservas/{codigo}/folio/pagos`, `GET /reservas/{codigo}/folio` | 201 | 3.5 |
| RN-16 Cargos y pagos inmutables | `Folio` (sin edición; `registrarMovimientoInverso`) | `FolioTest.noPermitirEdicionDirectaDeCargos` | No hay PUT ni DELETE sobre el folio; `POST /reservas/{codigo}/folio/deposito/devolucion` | — | 3.18 |
| RN-17 Cierre con saldo exige autorización | `Folio.cerrar`, `LiquidacionSalidaService` | `FolioTest.noCerrarConSaldoSinAutorizacion` | `POST /reservas/{codigo}/check-out` (autoriza solo ADMINISTRADOR: 403 si no) | 409 / 403 | 3.15, 3.16, 3.17 |
| RN-18 Conflicto de canal se rechaza y registra | `ConciliacionCanalExternoService.conciliar` | `ServiciosDeDominioTest.rechazarYRegistrarConflictoReservaExterna` | `POST /canales/externo/reservas`, `GET /canales/conflictos` | 409 | 7.5, 7.6 |
| RN-19 Canal + id externo únicos | `ConciliacionCanalExternoService`, restricción única `uk_reserva_canal_externo` | `CicloDeVidaReservaUseCaseTest.canalExternoAceptadaRepetidaYEnConflicto` | `POST /canales/externo/reservas` | 200 (no duplica) | 7.4 |
| RN-20 Tiempo de preparación | `TiempoPreparacion.seRespetaEntre`, `DisponibilidadDomoService` | `ServiciosDeDominioTest.tiempoDePreparacionEntreEstancias` | `POST /reservas`, `PUT /reservas/{codigo}` | 409 | — (con la Ficha, 4 h caben en la ventana 11:00–15:00) |
| RN-21 Vencimiento automático de pendientes | `Reserva.vencerPorPlazo`, `VencimientoReservasTarea` (@Scheduled) | `ReservaTest.pendienteSeVenceAlSuperarElPlazoDeConfirmacion`, `CicloDeVidaReservaUseCaseTest.lasPendientesSeVencenSinIntervencionManual` | Sin endpoint: tarea programada | — | — |
| RN-22 Valor y política congelados | `Reserva.crear`, `Reserva.reconstruir` | `ReservaTest.nacePendienteConValorYPoliticaCongelados`, `InventarioYCatalogoTest.reconstruirConservaElEstadoYElValorCongelado` | `POST /reservas`, `PUT /domos/{id}/tarifas` (no afecta reservas creadas) | 201 | 3.3, 6.4 |

## Reglas propias del equipo (Ficha A.6)

| Regla | Dónde se implementa | Prueba unitaria | Endpoint | HTTP | Colección |
|---|---|---|---|---|---|
| L-20 Recargo por persona adicional (25 %) | `RecargoPersonaAdicional`, `CotizadorEstanciaService` | `ServiciosDeDominioTest.calcularValorEstanciaNochePorNoche` | `POST /domos/{id}/cotizacion`, `POST /reservas` | 200 | 4.1 |
| L-21 Alistamiento del Jacuzzi (3 h) | `Domo.verificarAlistamientoJacuzzi`, `ConfirmacionReservaService` | `DomoYTemporadaTest.alistamientoDelJacuzziAlConfirmar` | `POST /reservas/{codigo}/confirmar` | 409 | 5.6 |
| L-22 Depósito antes del check-in | `Folio.tieneDepositoRegistrado`, `EntregaDomoService` | `FolioTest.depositoRegistradoYDevuelto`, `ServiciosDeDominioTest.entregaExigeDomoPreparadoYDepositoEnElFolio` | `POST /reservas/{codigo}/check-in`, `POST /reservas/{codigo}/folio/deposito` | 409 | 3.7, 3.8 |
| L-11 Anticipo del 50 % para confirmar | `ConfirmacionReservaService.confirmar` | `CicloDeVidaReservaUseCaseTest.confirmarSinAnticipoEsRechazado` | `POST /reservas/{codigo}/confirmar` | 409 | 3.4 |

## Reglas del corte 2 (7.2, 7.3 y 7.4)

| Regla | Dónde se implementa | Prueba unitaria | Endpoint | HTTP | Colección |
|---|---|---|---|---|---|
| 7.2 Correo único | `GestionUsuariosService.verificarRegistro` | `ServiciosDelCorte2Test.elCorreoEsUnicoYNoSeEliminaUnTitularConReservasActivas` | `POST /auth/registro`, `POST /usuarios` | 409 | 2.3 |
| 7.2 Contraseña segura | DTO (`ContrasenaSegura`, Bean Validation) | — (forma del mensaje) | `POST /auth/registro` | 400 | 2.1 |
| 7.2 Cambio con la contraseña actual | `Usuario.cambiarContrasena` | `UsuarioTest.cambiarContrasenaExigeLaActualYQueLaNuevaSeaDistinta` | `PUT /usuarios/me/contrasena` | 409 | 2.7 |
| 7.2 Código de recuperación: 15 min, un uso | `Usuario.restablecerContrasena`, `CodigoRecuperacion` | `UsuarioTest.elCodigoDeRecuperacionVenceYEsDeUnSoloUso` | `POST /auth/recuperacion/restablecer` | 409 | 2.10 |
| 7.2 No eliminar titular con reservas activas | `GestionUsuariosService.eliminar` | `ServiciosDelCorte2Test.elCorreoEsUnicoYNoSeEliminaUnTitularConReservasActivas` | `DELETE /usuarios/{id}` (eliminación lógica) | 409 | 5.20 |
| 7.2 Verificación por propietario | `AccesoReservas` (infraestructura, @PreAuthorize) | — (seguridad) | `GET /reservas/{codigo}`, `POST /reservas/{codigo}/cancelar`, `GET /reservas/{codigo}/folio` | 403 | 4.4, 4.5, 4.6 |
| L-04 Identificación única del domo | `InventarioDomoService.verificarIdentificacionDisponible` | `ServiciosDelCorte2Test.laIdentificacionDelDomoEsUnica` | `POST /domos` | 409 | 6.2 |
| 7.3 Retiro solo sin reservas activas | `InventarioDomoService.retirarDeVenta`, `Domo.retirarDeVenta` | `ServiciosDelCorte2Test.noSeRetiraUnDomoConReservasActivas` | `POST /domos/{id}/retirar` | 409 | 5.17 |
| 7.3 Bloqueo no sobre noches reservadas | `InventarioDomoService.registrarBloqueo` | `ServiciosDelCorte2Test.noSeBloqueanNochesConReservasActivas` | `POST /domos/{id}/bloqueos` | 409 | 5.7 |
| 7.3 Advertir reservas sobre la nueva capacidad | `InventarioDomoService.reservasQueSuperanCapacidad` | `ServiciosDelCorte2Test.reducirLaCapacidadAdvierteLasReservasQueQuedanPorEncima` | `PUT /domos/{id}` | 200 | 6.3 |
| 7.4 Activar solo con tarifas completas | `Domo.activar` | `InventarioYCatalogoTest.unDomoNuevoNaceInactivoYSoloSeActivaConTarifasCompletas` | `POST /domos`, `POST /domos/{id}/activar` | 409 | 6.1 |
| 7.4 Histórico de tarifas | `Domo.establecerTarifa` | `InventarioYCatalogoTest.cambiarUnaTarifaGuardaElValorAnteriorEnElHistorico` | `PUT /domos/{id}/tarifas`, `GET /domos/{id}/tarifas/historial` | 200 | 6.4, 6.5 |
| 7.4 Temporadas sin solaparse; base obligatoria | `CalendarioTemporadasService`, `Temporada` | `DomoYTemporadaTest.lasTemporadasNoSePuedenSolaparYLaBaseEsObligatoria` | `POST /temporadas`, `PUT /temporadas/{id}` | 409 | 5.19 |
| 7.4 Tarifa en todas las temporadas | `CalendarioTemporadasService.verificarTarifasCompletas` | `ServiciosDelCorte2Test.unaTemporadaNuevaExigeTarifaEnTodosLosDomosActivos` | `POST /temporadas` | 409 | 5.18, 6.8 |
| F-07 Política versionada | `PoliticaCancelacion.nuevaVersion` | `InventarioYCatalogoTest.unaNuevaVersionDeLaPoliticaConservaLaAnterior` | `POST /politica-cancelacion/versiones` | 201 | 6.10, 6.11 |
| 7.5 Calendario de ocupación | `CalendarioOcupacionService` | `ServiciosDelCorte2Test.elCalendarioMarcaNochesReservadasBloqueadasYLibres` | `GET /domos/{id}/calendario` | 200 | 6.7 |

## Persistencia

`PersistenciaMappersTest` comprueba que cada agregado (Reserva, Domo, Folio, Temporada, Bloqueo,
Novedad, Política, Usuario y Alojamiento) pasa a su entidad JPA y vuelve igual, sin que el
dominio tenga setters, constructor vacío ni anotaciones de JPA.
