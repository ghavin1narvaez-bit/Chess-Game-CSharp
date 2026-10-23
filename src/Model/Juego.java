package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador central de la lógica del juego.
 * Gestiona los turnos, valida movimientos, detecta jaque, jaque mate,
 * ahogado, enroque y coronación del peón.
 */
public class Juego {

    // Estados posibles de la partida
    public enum EstadoJuego {
        JUGANDO,
        BLANCAS_EN_JAQUE,
        NEGRAS_EN_JAQUE,
        BLANCAS_GANAN,
        NEGRAS_GANAN,
        AHOGADO,
        TABLAS,
        BLANCAS_RENUNCIAN,
        NEGRAS_RENUNCIAN
    }

    private Tablero tablero;
    private ColorJugador turnoActual;
    private EstadoJuego estadoJuego;
    private List<Movimiento> historialMovimientos;

    public Juego() {
        tablero = new Tablero();
        turnoActual = ColorJugador.BLANCO;
        estadoJuego = EstadoJuego.JUGANDO;
        historialMovimientos = new ArrayList<>();
    }

    // ─── Reinicio ────────────────────────────────────────────────────────────

    public void reset() {
        tablero = new Tablero();
        turnoActual = ColorJugador.BLANCO;
        estadoJuego = EstadoJuego.JUGANDO;
        historialMovimientos = new ArrayList<>();
    }

    // ─── Getters públicos ─────────────────────────────────────────────────────

    public Tablero getTablero()             { return tablero; }
    public ColorJugador getTurnoActual() { return turnoActual; }
    public EstadoJuego getEstadoJuego()     { return estadoJuego; }
    public List<Movimiento> getHistorialMovimientos()  { return historialMovimientos; }

    public boolean isGameOver() {
        if (estadoJuego == EstadoJuego.BLANCAS_GANAN)    return true;
        if (estadoJuego == EstadoJuego.NEGRAS_GANAN)    return true;
        if (estadoJuego == EstadoJuego.AHOGADO)     return true;
        if (estadoJuego == EstadoJuego.TABLAS)          return true;
        if (estadoJuego == EstadoJuego.BLANCAS_RENUNCIAN) return true;
        if (estadoJuego == EstadoJuego.NEGRAS_RENUNCIAN) return true;
        return false;
    }

    // ─── Movimientos legales ──────────────────────────────────────────────────

    /**
     * Devuelve todos los destinos a los que puede moverse la pieza en 'desde'.
     * Solo incluye movimientos legales (que no dejan al propio rey en jaque).
     */
    public List<Posicion> getMovimientosLegales(Posicion desde) {
        Pieza pieza = tablero.getPieza(desde);
        if (pieza == null || pieza.getColor() != turnoActual) {
            return new ArrayList<>();
        }

        List<Posicion> pseudoLegales = pieza.getPossibleMoves(tablero, desde);
        List<Posicion> legales = new ArrayList<>();

        for (int i = 0; i < pseudoLegales.size(); i++) {
            Posicion destino = pseudoLegales.get(i);
            if (doesNotLeaveKingInCheck(desde, destino)) {
                legales.add(destino);
            }
        }

        // Añadir enroque si el rey puede hacerlo
        if (pieza.getType() == TipoPieza.REY) {
            if (canEnrocarLadoRey(turnoActual)) {
                legales.add(new Posicion(desde.getRow(), desde.getCol() + 2)); // Casilla final del rey
                legales.add(new Posicion(desde.getRow(), 7)); // Permitir clic en la torre
            }
            if (canEnrocarLadoReina(turnoActual)) {
                legales.add(new Posicion(desde.getRow(), desde.getCol() - 2)); // Casilla final del rey
                legales.add(new Posicion(desde.getRow(), 0)); // Permitir clic en la torre
            }
        }

        return legales;
    }

    /**
     * Simula el movimiento en una copia del tablero y comprueba
     * si el propio rey queda en jaque. Devuelve true si el movimiento ES legal.
     */
    private boolean doesNotLeaveKingInCheck(Posicion desde, Posicion hasta) {
        Tablero copia = tablero.copy();
        copia.movePieza(desde, hasta);
        return !isInCheck(turnoActual, copia);
    }

    // ─── Ejecución de movimiento ──────────────────────────────────────────────

    /**
     * Ejecuta el movimiento desde 'desde' hasta 'hasta'.
     * Si es una coronación se necesita el tipo de pieza elegida.
     * Devuelve true si el movimiento se ejecutó con éxito.
     */
    public boolean makeMove(Posicion desde, Posicion hasta, TipoPieza eleccionPromocion) {
        if (isGameOver()) {
            return false;
        }

        Pieza pieza = tablero.getPieza(desde);

        // Si se hizo clic en la torre para enrocar, traducimos el destino a la casilla correcta del rey
        if (pieza != null && pieza.getType() == TipoPieza.REY) {
            if (hasta.getCol() == 7 && canEnrocarLadoRey(turnoActual)) {
                hasta = new Posicion(desde.getRow(), desde.getCol() + 2);
            } else if (hasta.getCol() == 0 && canEnrocarLadoReina(turnoActual)) {
                hasta = new Posicion(desde.getRow(), desde.getCol() - 2);
            }
        }

        List<Posicion> legales = getMovimientosLegales(desde);
        if (!legales.contains(hasta)) {
            return false;
        }
        Movimiento movimiento = new Movimiento(desde, hasta);
        movimiento.setPiezaMovida(pieza);
        movimiento.setPiezaCapturada(tablero.getPieza(hasta));

        int colDiff = hasta.getCol() - desde.getCol();

        // ── Enroque ──
        if (pieza.getType() == TipoPieza.REY && Math.abs(colDiff) == 2) {
            int row = desde.getRow();
            if (colDiff == 2) {
                // Enroque corto: mueve la torre de col 7 a col 5
                tablero.movePieza(new Posicion(row, 7), new Posicion(row, 5));
                movimiento.setEnroqueLadoRey(true);
            } else {
                // Enroque largo: mueve la torre de col 0 a col 3
                tablero.movePieza(new Posicion(row, 0), new Posicion(row, 3));
                movimiento.setEnroqueLadoReina(true);
            }
        }

        // ── Movimiento principal de la pieza ──
        tablero.movePieza(desde, hasta);

        // ── Coronación del peón ──
        if (pieza.getType() == TipoPieza.PEON) {
            int filaPromocion;
            if (pieza.getColor() == ColorJugador.BLANCO) {
                filaPromocion = 0;
            } else {
                filaPromocion = 7;
            }

            if (hasta.getRow() == filaPromocion) {
                TipoPieza eleccion;
                if (eleccionPromocion != null) {
                    eleccion = eleccionPromocion;
                } else {
                    eleccion = TipoPieza.REINA; // por defecto, reina
                }
                Pieza promocionada = createPieza(eleccion, pieza.getColor());
                promocionada.setHasMoved(true);
                tablero.setPieza(hasta, promocionada);
                movimiento.setPromocion(true);
                movimiento.setPiezaPromocion(eleccion);
            }
        }

        historialMovimientos.add(movimiento);
        turnoActual = turnoActual.opposite();
        updateGameState();
        return true;
    }

    /**
     * Crea una nueva pieza del tipo y color indicados.
     * Se usa para la coronación del peón.
     */
    private Pieza createPieza(TipoPieza tipo, ColorJugador color) {
        if (tipo == TipoPieza.REINA)  return new Reina(color);
        if (tipo == TipoPieza.TORRE)   return new Torre(color);
        if (tipo == TipoPieza.ALFIL) return new Alfil(color);
        if (tipo == TipoPieza.CABALLO) return new Caballo(color);
        return new Reina(color); // valor por defecto
    }

    // ─── Estado del juego ─────────────────────────────────────────────────────

    /**
     * Actualiza el estado del juego después de cada movimiento.
     * Comprueba si hay jaque, jaque mate, ahogado o tablas.
     */
    private void updateGameState() {
        boolean enJaque = isInCheck(turnoActual, tablero);
        boolean tieneMovimientosLegales = hasAnyLegalMove(turnoActual);

        if (enJaque && !tieneMovimientosLegales) {
            // Jaque mate: gana el jugador que acaba de mover
            if (turnoActual == ColorJugador.BLANCO) {
                estadoJuego = EstadoJuego.NEGRAS_GANAN;
            } else {
                estadoJuego = EstadoJuego.BLANCAS_GANAN;
            }
        } else if (!enJaque && !tieneMovimientosLegales) {
            // Ahogado: empate
            estadoJuego = EstadoJuego.AHOGADO;
        } else if (isMaterialInsuficiente()) {
            // Material insuficiente: empate
            estadoJuego = EstadoJuego.TABLAS;
        } else if (enJaque) {
            // Solo jaque (aún hay movimientos posibles)
            if (turnoActual == ColorJugador.BLANCO) {
                estadoJuego = EstadoJuego.BLANCAS_EN_JAQUE;
            } else {
                estadoJuego = EstadoJuego.NEGRAS_EN_JAQUE;
            }
        } else {
            estadoJuego = EstadoJuego.JUGANDO;
        }
    }

    /**
     * Comprueba si el jugador del color dado tiene al menos un movimiento legal.
     */
    private boolean hasAnyLegalMove(ColorJugador color) {
        List<Posicion> piezas = tablero.getPiezasOfColor(color);
        ColorJugador guardado = turnoActual;
        turnoActual = color; // necesario para que getMovimientosLegales funcione
        for (int i = 0; i < piezas.size(); i++) {
            Posicion desde = piezas.get(i);
            if (!getMovimientosLegales(desde).isEmpty()) {
                turnoActual = guardado;
                return true;
            }
        }
        turnoActual = guardado;
        return false;
    }

    /**
     * Comprueba si el rey del color indicado está en jaque en el tablero 'b'.
     */
    public boolean isInCheck(ColorJugador color, Tablero b) {
        Posicion posRey = b.findRey(color);
        if (posRey == null) {
            return true; // Sin rey (no debería ocurrir)
        }
        return isSquareAttackedBy(posRey, color.opposite(), b);
    }

    /**
     * Comprueba si alguna pieza del color 'attackerColor' puede capturar la casilla 'pos'.
     */
    public boolean isSquareAttackedBy(Posicion pos, ColorJugador colorAtacante, Tablero b) {
        List<Posicion> piezasAtacantes = b.getPiezasOfColor(colorAtacante);
        for (int i = 0; i < piezasAtacantes.size(); i++) {
            Posicion pa = piezasAtacantes.get(i);
            Pieza p = b.getPieza(pa);
            
            // Tratamiento especial para peones: atacan en diagonal incluso si la casilla está vacía
            if (p.getType() == TipoPieza.PEON) {
                int direction = (p.getColor() == ColorJugador.BLANCO) ? -1 : 1;
                if (pos.getRow() == pa.getRow() + direction && Math.abs(pos.getCol() - pa.getCol()) == 1) {
                    return true;
                }
            } else {
                List<Posicion> movimientosPosibles = p.getPossibleMoves(b, pa);
                if (movimientosPosibles.contains(pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ─── Enroque ─────────────────────────────────────────────────────────────

    private boolean canEnrocarLadoRey(ColorJugador color) {
        int row;
        if (color == ColorJugador.BLANCO) {
            row = 7;
        } else {
            row = 0;
        }

        Pieza rey = tablero.getPieza(new Posicion(row, 4));
        Pieza torre = tablero.getPieza(new Posicion(row, 7));

        // El rey y la torre deben existir y no haberse movido
        if (rey == null || rey.hasMoved()) return false;
        if (torre == null || torre.hasMoved()) return false;
        if (rey.getType() != TipoPieza.REY) return false;
        if (torre.getType() != TipoPieza.TORRE) return false;

        // Las casillas intermedias deben estar vacías
        if (tablero.getPieza(new Posicion(row, 5)) != null) return false;
        if (tablero.getPieza(new Posicion(row, 6)) != null) return false;

        // El rey no puede estar en jaque ni pasar por una casilla atacada
        if (isInCheck(color, tablero)) return false;
        if (isSquareAttackedBy(new Posicion(row, 5), color.opposite(), tablero)) return false;
        if (isSquareAttackedBy(new Posicion(row, 6), color.opposite(), tablero)) return false;

        return true;
    }

    private boolean canEnrocarLadoReina(ColorJugador color) {
        int row;
        if (color == ColorJugador.BLANCO) {
            row = 7;
        } else {
            row = 0;
        }

        Pieza rey = tablero.getPieza(new Posicion(row, 4));
        Pieza torre = tablero.getPieza(new Posicion(row, 0));

        if (rey == null || rey.hasMoved()) return false;
        if (torre == null || torre.hasMoved()) return false;
        if (rey.getType() != TipoPieza.REY) return false;
        if (torre.getType() != TipoPieza.TORRE) return false;

        // Las casillas intermedias deben estar vacías
        if (tablero.getPieza(new Posicion(row, 1)) != null) return false;
        if (tablero.getPieza(new Posicion(row, 2)) != null) return false;
        if (tablero.getPieza(new Posicion(row, 3)) != null) return false;

        // El rey no puede pasar por casillas atacadas
        if (isInCheck(color, tablero)) return false;
        if (isSquareAttackedBy(new Posicion(row, 3), color.opposite(), tablero)) return false;
        if (isSquareAttackedBy(new Posicion(row, 2), color.opposite(), tablero)) return false;

        return true;
    }

    // ─── Material insuficiente ────────────────────────────────────────────────

    /**
     * Comprueba si ninguno de los dos jugadores puede dar jaque mate.
     * Casos: Rey vs Rey, Rey+Alfil vs Rey, Rey+Caballo vs Rey,
     * Rey+Alfil vs Rey+Alfil (misma casilla de color).
     */
    private boolean isMaterialInsuficiente() {
        List<Posicion> piezasBlancas = tablero.getPiezasOfColor(ColorJugador.BLANCO);
        List<Posicion> piezasNegras = tablero.getPiezasOfColor(ColorJugador.NEGRO);

        // Rey vs Rey
        if (piezasBlancas.size() == 1 && piezasNegras.size() == 1) {
            return true;
        }

        // Rey + Alfil/Caballo vs Rey
        if (piezasBlancas.size() == 1 && piezasNegras.size() == 2) {
            return hasOnlyKingAndMinor(piezasNegras);
        }
        if (piezasBlancas.size() == 2 && piezasNegras.size() == 1) {
            return hasOnlyKingAndMinor(piezasBlancas);
        }

        // Rey + Alfil vs Rey + Alfil (misma casilla de color)
        if (piezasBlancas.size() == 2 && piezasNegras.size() == 2) {
            return bothOnlyHaveKingAndBishopSameColor(piezasBlancas, piezasNegras);
        }

        return false;
    }

    /**
     * Comprueba si la lista de piezas solo contiene un rey y un alfil/caballo.
     */
    private boolean hasOnlyKingAndMinor(List<Posicion> piezas) {
        for (int i = 0; i < piezas.size(); i++) {
            Posicion p = piezas.get(i);
            TipoPieza t = tablero.getPieza(p).getType();
            if (t != TipoPieza.REY && t != TipoPieza.ALFIL && t != TipoPieza.CABALLO) {
                return false;
            }
        }
        return true;
    }

    /**
     * Comprueba si ambos jugadores solo tienen rey + alfil, y ambos alfiles
     * están en casillas del mismo color.
     */
    private boolean bothOnlyHaveKingAndBishopSameColor(List<Posicion> blancas, List<Posicion> negras) {
        Posicion alfilBlanco = null;
        Posicion alfilNegro = null;

        for (int i = 0; i < blancas.size(); i++) {
            Posicion p = blancas.get(i);
            if (tablero.getPieza(p).getType() == TipoPieza.ALFIL) {
                alfilBlanco = p;
            }
        }
        for (int i = 0; i < negras.size(); i++) {
            Posicion p = negras.get(i);
            if (tablero.getPieza(p).getType() == TipoPieza.ALFIL) {
                alfilNegro = p;
            }
        }

        if (alfilBlanco == null || alfilNegro == null) {
            return false;
        }

        // Misma casilla de color: (fila + columna) % 2 debe ser igual
        int colorCasillaBlancos = (alfilBlanco.getRow() + alfilBlanco.getCol()) % 2;
        int colorCasillaNegros  = (alfilNegro.getRow() + alfilNegro.getCol()) % 2;
        return colorCasillaBlancos == colorCasillaNegros;
    }

    // ─── Abandono ─────────────────────────────────────────────────────────────

    public void resign() {
        if (turnoActual == ColorJugador.BLANCO) {
            estadoJuego = EstadoJuego.BLANCAS_RENUNCIAN;
        } else {
            estadoJuego = EstadoJuego.NEGRAS_RENUNCIAN;
        }
    }
}
