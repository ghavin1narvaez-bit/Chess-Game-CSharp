package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa el tablero de ajedrez 8x8.
 * Fila 0 = fila 8 (piezas negras), Fila 7 = fila 1 (piezas blancas).
 */
public class Tablero {

    private Pieza[][] squares;

    /** Constructor: crea un tablero con la disposición inicial estándar. */
    public Tablero() {
        squares = new Pieza[8][8];
        setupInitialPosition();
    }

    /** Constructor privado usado para copiar el tablero. */
    private Tablero(Pieza[][] original) {
        this.squares = new Pieza[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (original[r][c] != null) {
                    this.squares[r][c] = original[r][c].copy();
                }
            }
        }
    }

    /** Devuelve una copia profunda del tablero para simular movimientos. */
    public Tablero copy() {
        return new Tablero(this.squares);
    }

    // ─── Acceso a casillas ───────────────────────────────────────────────────

    public Pieza getPieza(Posicion pos) {
        if (pos == null || !pos.isValid()) return null;
        return squares[pos.getRow()][pos.getCol()];
    }

    public Pieza getPieza(int row, int col) {
        return squares[row][col];
    }

    public void setPieza(Posicion pos, Pieza pieza) {
        squares[pos.getRow()][pos.getCol()] = pieza;
    }

    public void removePieza(Posicion pos) {
        squares[pos.getRow()][pos.getCol()] = null;
    }

    /**
     * Mueve físicamente una pieza de 'from' a 'to'.
     * Marca la pieza como movida. No valida legalidad.
     */
    public void movePieza(Posicion desde, Posicion hasta) {
        Pieza pieza = getPieza(desde);
        if (pieza != null) {
            pieza.setHasMoved(true);
        }
        squares[hasta.getRow()][hasta.getCol()] = pieza;
        squares[desde.getRow()][desde.getCol()] = null;
    }

    // ─── Búsqueda ───────────────────────────────────────────────────────────

    /** Devuelve la posición del rey del color indicado, o null si no se encuentra. */
    public Posicion findRey(ColorJugador color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Pieza p = squares[r][c];
                if (p != null && p.getType() == TipoPieza.REY && p.getColor() == color) {
                    return new Posicion(r, c);
                }
            }
        }
        return null;
    }

    /** Devuelve una lista con las posiciones de todas las piezas del color dado. */
    public List<Posicion> getPiezasOfColor(ColorJugador color) {
        List<Posicion> posiciones = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Pieza p = squares[r][c];
                if (p != null && p.getColor() == color) {
                    posiciones.add(new Posicion(r, c));
                }
            }
        }
        return posiciones;
    }

    // ─── Disposición inicial ─────────────────────────────────────────────────

    private void setupInitialPosition() {
        // Piezas negras - fila 0 (rank 8)
        squares[0][0] = new Torre(ColorJugador.NEGRO);
        squares[0][1] = new Caballo(ColorJugador.NEGRO);
        squares[0][2] = new Alfil(ColorJugador.NEGRO);
        squares[0][3] = new Reina(ColorJugador.NEGRO);
        squares[0][4] = new Rey(ColorJugador.NEGRO);
        squares[0][5] = new Alfil(ColorJugador.NEGRO);
        squares[0][6] = new Caballo(ColorJugador.NEGRO);
        squares[0][7] = new Torre(ColorJugador.NEGRO);
        // Peones negros - fila 1 (rank 7)
        for (int c = 0; c < 8; c++) squares[1][c] = new Peon(ColorJugador.NEGRO);

        // Peones blancos - fila 6 (rank 2)
        for (int c = 0; c < 8; c++) squares[6][c] = new Peon(ColorJugador.BLANCO);
        // Piezas blancas - fila 7 (rank 1)
        squares[7][0] = new Torre(ColorJugador.BLANCO);
        squares[7][1] = new Caballo(ColorJugador.BLANCO);
        squares[7][2] = new Alfil(ColorJugador.BLANCO);
        squares[7][3] = new Reina(ColorJugador.BLANCO);
        squares[7][4] = new Rey(ColorJugador.BLANCO);
        squares[7][5] = new Alfil(ColorJugador.BLANCO);
        squares[7][6] = new Caballo(ColorJugador.BLANCO);
        squares[7][7] = new Torre(ColorJugador.BLANCO);
    }
}
