package co.edu.uniquindio.sga.application.dto;

import co.edu.uniquindio.sga.domain.repository.Paginacion;
import co.edu.uniquindio.sga.domain.repository.ResultadoPaginado;

import java.util.List;
import java.util.function.Function;

/**
 * 7.5 y 12.4: todos los listados usan paginación de 10 resultados por página. La página es
 * base 0. El tamaño lo fija el enunciado para todos los equipos (12.4), no la Ficha.
 */
public record Pagina<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static final int TAMANO_POR_DEFECTO = 10;

    public static Paginacion solicitud(int pagina) {
        return new Paginacion(Math.max(0, pagina), TAMANO_POR_DEFECTO);
    }

    /** Pagina en memoria una lista ya ordenada (listados pequeños: domos, novedades, bloqueos). */
    public static <T> Pagina<T> de(List<T> todos, int pagina) {
        return desde(ResultadoPaginado.de(todos, solicitud(pagina)));
    }

    /** Página que ya llegó paginada desde el repositorio (consulta paginada en la base). */
    public static <T> Pagina<T> desde(ResultadoPaginado<T> resultado) {
        return new Pagina<>(resultado.contenido(), resultado.pagina(), resultado.tamano(), resultado.totalElementos(),
                resultado.totalPaginas());
    }

    public <R> Pagina<R> mapear(Function<T, R> transformacion) {
        return new Pagina<>(contenido.stream().map(transformacion).toList(), pagina, tamano, totalElementos, totalPaginas);
    }
}
