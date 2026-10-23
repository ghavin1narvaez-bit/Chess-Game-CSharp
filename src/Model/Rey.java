package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Rey: se mueve 1 casilla en cualquier dirección.
 * El enroque se valida y se añade desde la clase Game.
 */
public class Rey extends Pieza {

    public Rey(ColorJugador color) {
        super(color, TipoPieza.REY);
    }

    @Override
    public List<Posicion> getPossibleMoves(Tablero tablero, Posicion desde) {
        List<Posicion> movimientos = new ArrayList<>();
        int row = desde.getRow();
        int col = desde.getCol();
        // Las 8 casillas adyacentes
        int[][] directions = {
            {-1, -1}, {-1, 0}, {-1, 1},
            { 0, -1},          { 0, 1},
            { 1, -1}, { 1, 0}, { 1, 1}
        };
        for (int i = 0; i < directions.length; i++) {
            int r = row + directions[i][0];
            int c = col + directions[i][1];
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
        Rey copia = new Rey(this.color);
        copia.hasMoved = this.hasMoved;
        return copia;
    }
}
