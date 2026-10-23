package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Torre: se mueve en líneas rectas (arriba, abajo, izquierda, derecha).
 * Puede ir tantas casillas como quiera. No puede saltar piezas.
 */
public class Torre extends Pieza {

    public Torre(ColorJugador color) {
        super(color, TipoPieza.TORRE);
    }

    @Override
    public List<Posicion> getPossibleMoves(Tablero tablero, Posicion desde) {
        List<Posicion> movimientos = new ArrayList<>();
        // Las 4 direcciones ortogonales: arriba, abajo, izquierda, derecha
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int i = 0; i < directions.length; i++) {
            addSlidingMoves(tablero, desde, directions[i][0], directions[i][1], movimientos);
        }
        return movimientos;
    }

    @Override
    public Pieza copy() {
        Torre copia = new Torre(this.color);
        copia.hasMoved = this.hasMoved;
        return copia;
    }
}
