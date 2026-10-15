package co.edu.uniquindio.sga.application.dto;

import java.util.List;
import java.util.function.Function;

/**
 * 7.5 y 12.4: todos los listados usan paginación de 10 resultados por página.
 * La página es base 0.
 */
public record Pagina<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static final int TAMANO_POR_DEFECTO = 10;

    public static <T> Pagina<T> de(List<T> todos, int pagina) {
        int numero = Math.max(0, pagina);
        int desde = Math.min(numero * TAMANO_POR_DEFECTO, todos.size());
        int hasta = Math.min(desde + TAMANO_POR_DEFECTO, todos.size());
        int totalPaginas = (int) Math.ceil(todos.size() / (double) TAMANO_POR_DEFECTO);
        return new Pagina<>(List.copyOf(todos.subList(desde, hasta)), numero, TAMANO_POR_DEFECTO,
                todos.size(), totalPaginas);
    }

    public <R> Pagina<R> mapear(Function<T, R> transformacion) {
        return new Pagina<>(contenido.stream().map(transformacion).toList(), pagina, tamano, totalElementos, totalPaginas);
    }
}
