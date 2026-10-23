package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Alfil: se mueve en diagonal sin límite de casillas.
 * No puede saltar piezas.
 */
public class Alfil extends Pieza {

    public Alfil(ColorJugador color) {
        super(color, TipoPieza.ALFIL);
    }

    @Override
    public List<Posicion> getPossibleMoves(Tablero tablero, Posicion desde) {
        List<Posicion> movimientos = new ArrayList<>();
        // Las 4 diagonales
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int i = 0; i < directions.length; i++) {
            addSlidingMoves(tablero, desde, directions[i][0], directions[i][1], movimientos);
        }
        return movimientos;
    }

    @Override
    public Pieza copy() {
        Alfil copia = new Alfil(this.color);
        copia.hasMoved = this.hasMoved;
        return copia;
    }
}
