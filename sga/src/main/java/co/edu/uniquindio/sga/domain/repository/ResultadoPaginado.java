package co.edu.uniquindio.sga.domain.repository;

import java.util.List;
import java.util.function.Function;

/** Una página de resultados devuelta por un repositorio, con el total para calcular las páginas. */
public record ResultadoPaginado<T>(List<T> contenido, int pagina, int tamano, long totalElementos) {

    public ResultadoPaginado {
        contenido = contenido == null ? List.of() : List.copyOf(contenido);
    }

    /** Pagina en memoria una lista ya ordenada (listados pequeños como los domos). */
    public static <T> ResultadoPaginado<T> de(List<T> todos, Paginacion paginacion) {
        int desde = Math.min(paginacion.desplazamiento(), todos.size());
        int hasta = Math.min(desde + paginacion.tamano(), todos.size());
        return new ResultadoPaginado<>(todos.subList(desde, hasta), paginacion.pagina(), paginacion.tamano(),
                todos.size());
    }

    public int totalPaginas() {
        return (int) Math.ceil(totalElementos / (double) tamano);
    }

    public <R> ResultadoPaginado<R> mapear(Function<T, R> transformacion) {
        return new ResultadoPaginado<>(contenido.stream().map(transformacion).toList(), pagina, tamano, totalElementos);
    }
}
