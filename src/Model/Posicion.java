package Model;

import java.util.Objects;

/**
 * Representa una casilla del tablero con coordenadas (fila, columna).
 * Fila 0 = fila superior (negras), Fila 7 = fila inferior (blancas).
 * Columna 0 = columna 'a', Columna 7 = columna 'h'.
 */
public class Posicion {
    private final int row;
    private final int col;

    public Posicion(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Posicion)) return false;
        Posicion p = (Posicion) o;
        return row == p.row && col == p.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        char colChar = (char) ('a' + col);
        int rowNum = 8 - row;
        return "" + colChar + rowNum;
    }
}
