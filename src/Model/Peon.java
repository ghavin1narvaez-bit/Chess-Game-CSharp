package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Peón: avanza 1 casilla hacia adelante (o 2 en el primer movimiento),
 * captura en diagonal.
 * Las blancas avanzan hacia la fila 0, las negras hacia la fila 7.
 */
public class Peon extends Pieza {

    public Peon(ColorJugador color) {
        super(color, TipoPieza.PEON);
    }

    @Override
    public List<Posicion> getPossibleMoves(Tablero tablero, Posicion desde) {
        List<Posicion> movimientos = new ArrayList<>();
        int row = desde.getRow();
        int col = desde.getCol();

        // Las blancas suben (-1), las negras bajan (+1)
        int direction;
        if (color == ColorJugador.BLANCO) {
            direction = -1;
        } else {
            direction = 1;
        }

        // Avance de 1 casilla (solo si está vacía)
        int nextRow = row + direction;
        if (nextRow >= 0 && nextRow < 8 && tablero.getPieza(new Posicion(nextRow, col)) == null) {
            movimientos.add(new Posicion(nextRow, col));

            // Avance doble desde la fila inicial (solo si no se ha movido)
            int startRow;
            if (color == ColorJugador.BLANCO) {
                startRow = 6;
            } else {
                startRow = 1;
            }

            int doubleRow = row + 2 * direction;
            if (!hasMoved && row == startRow && tablero.getPieza(new Posicion(doubleRow, col)) == null) {
                movimientos.add(new Posicion(doubleRow, col));
            }
        }

        // Capturas en diagonal (izquierda y derecha)
        int[] captureCol = {col - 1, col + 1};
        for (int i = 0; i < captureCol.length; i++) {
            int cc = captureCol[i];
            if (cc >= 0 && cc < 8 && nextRow >= 0 && nextRow < 8) {
                Pieza objetivo = tablero.getPieza(new Posicion(nextRow, cc));
                if (objetivo != null && objetivo.getColor() != this.color) {
                    movimientos.add(new Posicion(nextRow, cc));
                }
            }
        }
        return movimientos;
    }

    @Override
    public Pieza copy() {
        Peon copia = new Peon(this.color);
        copia.hasMoved = this.hasMoved;
        return copia;
    }
}
