package Model;

import java.util.List;

/**
 * Clase base abstracta para todas las piezas del ajedrez.
 * Cada subclase implementa sus propias reglas de movimiento (pseudo-legales).
 */
public abstract class Pieza {
    protected final ColorJugador color;
    protected final TipoPieza type;
    protected boolean hasMoved;

    public Pieza(ColorJugador color, TipoPieza type) {
        this.color = color;
        this.type = type;
        this.hasMoved = false;
    }

    public ColorJugador getColor() { return color; }
    public TipoPieza getType()    { return type; }
    public boolean hasMoved()     { return hasMoved; }
    public void setHasMoved(boolean hasMoved) { this.hasMoved = hasMoved; }

    /**
     * Devuelve una lista de posiciones a las que la pieza puede moverse (pseudo-legal).
     * No tiene en cuenta si el movimiento deja al propio rey en jaque.
     */
    public abstract List<Posicion> getPossibleMoves(Tablero tablero, Posicion desde);

    /**
     * Crea una copia profunda de la pieza (necesario para simular movimientos).
     */
    public abstract Pieza copy();

    /**
     * Añade posiciones válidas en una dirección de deslizamiento (torre, alfil, reina).
     * Se detiene al encontrar una pieza, y si es enemiga la incluye como captura.
     */
    protected void addSlidingMoves(Tablero tablero, Posicion desde, int dRow, int dCol, List<Posicion> movimientos) {
        int r = desde.getRow() + dRow;
        int c = desde.getCol() + dCol;
        while (r >= 0 && r < 8 && c >= 0 && c < 8) {
            Posicion objetivo = new Posicion(r, c);
            Pieza ocupante = tablero.getPieza(objetivo);
            if (ocupante == null) {
                movimientos.add(objetivo);
            } else {
                if (ocupante.getColor() != this.color) {
                    movimientos.add(objetivo); // captura enemiga
                }
                break; // bloqueado
            }
            r += dRow;
            c += dCol;
        }
    }

    @Override
    public String toString() {
        return color + "_" + type;
    }
}
