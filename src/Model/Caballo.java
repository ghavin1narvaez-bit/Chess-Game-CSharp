package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Caballo: se mueve en forma de L (2 casillas en una dirección + 1 perpendicular).
 * Es la única pieza que puede saltar por encima de otras piezas.
 */
public class Caballo extends Pieza {

    public Caballo(ColorJugador color) {
        super(color, TipoPieza.CABALLO);
    }

    @Override
    public List<Posicion> getPossibleMoves(Tablero tablero, Posicion desde) {
        List<Posicion> movimientos = new ArrayList<>();
        int row = desde.getRow();
        int col = desde.getCol();
        // Los 8 posibles saltos en forma de L
        int[][] jumps = {
            {-2, -1}, {-2, 1},
            {-1, -2}, {-1, 2},
            { 1, -2}, { 1, 2},
            { 2, -1}, { 2, 1}
        };
        for (int i = 0; i < jumps.length; i++) {
            int r = row + jumps[i][0];
            int c = col + jumps[i][1];
            if (r >= 0 && r < 8 && c >= 0 && c < 8) {
                Pieza ocupante = tablero.getPieza(new Posicion(r, c));
                if (ocupante == null || ocupante.getColor() != this.color) {
                    movimientos.add(new Posicion(r, c));
                }
            }
        }
        return movimientos;
    }

    @Override
    public Pieza copy() {
        Caballo copia = new Caballo(this.color);
        copia.hasMoved = this.hasMoved;
        return copia;
    }
}
