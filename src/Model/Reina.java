package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Reina (Dama): combina los movimientos de la Torre y el Alfil.
 * Se mueve en las 8 direcciones sin límite de casillas. No puede saltar piezas.
 */
public class Reina extends Pieza {

    public Reina(ColorJugador color) {
        super(color, TipoPieza.REINA);
    }

    @Override
    public List<Posicion> getPossibleMoves(Tablero tablero, Posicion desde) {
        List<Posicion> movimientos = new ArrayList<>();
        // Las 8 direcciones: 4 ortogonales (torre) + 4 diagonales (alfil)
        int[][] directions = {
            {-1,  0}, { 1,  0}, { 0, -1}, { 0,  1},
            {-1, -1}, {-1,  1}, { 1, -1}, { 1,  1}
        };
        for (int i = 0; i < directions.length; i++) {
            addSlidingMoves(tablero, desde, directions[i][0], directions[i][1], movimientos);
        }
        return movimientos;
    }

    @Override
    public Pieza copy() {
        Reina copia = new Reina(this.color);
        copia.hasMoved = this.hasMoved;
        return copia;
    }
}
