package Model;

/**
 * Representa un movimiento en la partida: desde una posición origen hasta una destino.
 * Incluye metadatos para movimientos especiales (enroque, coronación).
 */
public class Movimiento {
    private final Posicion desde;
    private final Posicion hasta;
    private Pieza piezaMovida;
    private Pieza piezaCapturada;
    private boolean enroqueLadoRey;
    private boolean enroqueLadoReina;
    private boolean promocion;
    private TipoPieza piezaPromocion;

    public Movimiento(Posicion desde, Posicion hasta) {
        this.desde = desde;
        this.hasta = hasta;
    }

    public Posicion getDesde()            { return desde; }
    public Posicion getHasta()              { return hasta; }
    public Pieza getPiezaMovida()         { return piezaMovida; }
    public Pieza getPiezaCapturada()      { return piezaCapturada; }
    public boolean isEnroqueLadoRey()  { return enroqueLadoRey; }
    public boolean isEnroqueLadoReina() { return enroqueLadoReina; }
    public boolean isPromocion()         { return promocion; }
    public TipoPieza getPiezaPromocion() { return piezaPromocion; }

    public void setPiezaMovida(Pieza p)         { this.piezaMovida = p; }
    public void setPiezaCapturada(Pieza p)      { this.piezaCapturada = p; }
    public void setEnroqueLadoRey(boolean b) { this.enroqueLadoRey = b; }
    public void setEnroqueLadoReina(boolean b){ this.enroqueLadoReina = b; }
    public void setPromocion(boolean b)        { this.promocion = b; }
    public void setPiezaPromocion(TipoPieza t) { this.piezaPromocion = t; }

    @Override
    public String toString() {
        return desde + " -> " + hasta;
    }
}
