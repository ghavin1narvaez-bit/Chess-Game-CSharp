package UI;

import Model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel gráfico principal del tablero de ajedrez.
 * Dibuja el tablero, las piezas y gestiona los clics del ratón.
 */
public class PanelTablero extends JPanel implements MouseListener {

    // Tamaño de cada casilla en píxeles
    private static final int CELL_SIZE    = 80;
    private static final int BOARD_SIZE   = CELL_SIZE * 8;
    private static final int LABEL_MARGIN = 24;

    // Colores del tablero y los resaltados
    private static final Color COLOR_LIGHT      = new Color(240, 217, 181);
    private static final Color COLOR_DARK       = new Color(181, 136,  99);
    private static final Color COLOR_SELECTED   = new Color( 20, 200,  80, 160);
    private static final Color COLOR_VALID_MOVE = new Color( 20, 200,  80,  90);
    private static final Color COLOR_CHECK      = new Color(220,  50,  50, 180);
    private static final Color COLOR_LAST_MOVE  = new Color(255, 220,  50,  90);

    // Lógica del juego
    private final Juego juego;

    // Referencia a la ventana principal para notificarle cambios
    private final GUIAjedrez ventanaPrincipal;

    // Casilla seleccionada y sus movimientos válidos
    private Posicion selectedPos = null;
    private List<Posicion> legalMoves = null;

    // Mapa de imágenes de piezas: "BLANCO_REY", "NEGRO_PEON", etc.
    private final Map<String, Image> pieceImages = new HashMap<>();

    public PanelTablero(Juego juego, GUIAjedrez ventanaPrincipal) {
        this.juego = juego;
        this.ventanaPrincipal = ventanaPrincipal;
        setPreferredSize(new Dimension(BOARD_SIZE + LABEL_MARGIN, BOARD_SIZE + LABEL_MARGIN));
        setBackground(new Color(30, 30, 46));
        addMouseListener(this);
        loadPieceImages();
    }

    // ─── Carga de imágenes ────────────────────────────────────────────────────

    /**
     * Intenta cargar imágenes PNG desde resources/pieces/.
     * Nomenclatura: white_king.png, black_pawn.png, etc.
     * Si no se encuentran, se usarán símbolos Unicode como alternativa.
     */
    private void loadPieceImages() {
        String[] colors = {"white", "black"};
        String[] types  = {"king", "queen", "rook", "bishop", "knight", "pawn"};
        String[] colorKeys = {"BLANCO", "NEGRO"};
        String[] typeKeys = {"REY", "REINA", "TORRE", "ALFIL", "CABALLO", "PEON"};

        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < types.length; j++) {
                String key  = colorKeys[i] + "_" + typeKeys[j];
                String path = "/pieces/" + colors[i] + "_" + types[j] + ".png";
                try {
                    java.net.URL url = getClass().getResource(path);
                    if (url != null) {
                        Image img = new ImageIcon(url).getImage();
                        pieceImages.put(key, img);
                    }
                } catch (Exception e) {
                    // Si la imagen no existe, se usa el símbolo Unicode
                }
            }
        }
    }

    /** Devuelve las imágenes de las piezas de un color para el diálogo de coronación. */
    public Map<TipoPieza, Image> getImagesForColor(ColorJugador color) {
        Map<TipoPieza, Image> map = new HashMap<>();
        String prefix = color.name() + "_";
        map.put(TipoPieza.REINA,  pieceImages.get(prefix + "REINA"));
        map.put(TipoPieza.TORRE,   pieceImages.get(prefix + "TORRE"));
        map.put(TipoPieza.ALFIL, pieceImages.get(prefix + "ALFIL"));
        map.put(TipoPieza.CABALLO, pieceImages.get(prefix + "CABALLO"));
        return map;
    }

    // ─── Pintado ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBoard(g2);
        drawHighlights(g2);
        drawCoordinates(g2);
        drawPieces(g2);
    }

    private void drawBoard(Graphics2D g) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 0) {
                    g.setColor(COLOR_LIGHT);
                } else {
                    g.setColor(COLOR_DARK);
                }
                g.fillRect(LABEL_MARGIN + c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private void drawHighlights(Graphics2D g) {
        // Resaltar el último movimiento realizado
        if (!juego.getHistorialMovimientos().isEmpty()) {
            Movimiento last = juego.getHistorialMovimientos().get(juego.getHistorialMovimientos().size() - 1);
            highlightCell(g, last.getDesde(), COLOR_LAST_MOVE);
            highlightCell(g, last.getHasta(),   COLOR_LAST_MOVE);
        }

        // Resaltar el rey en jaque en rojo
        Juego.EstadoJuego state = juego.getEstadoJuego();
        if (state == Juego.EstadoJuego.BLANCAS_EN_JAQUE || state == Juego.EstadoJuego.NEGRAS_EN_JAQUE) {
            ColorJugador inCheck;
            if (state == Juego.EstadoJuego.BLANCAS_EN_JAQUE) {
                inCheck = ColorJugador.BLANCO;
            } else {
                inCheck = ColorJugador.NEGRO;
            }
            Posicion kingPos = juego.getTablero().findRey(inCheck);
            if (kingPos != null) {
                highlightCell(g, kingPos, COLOR_CHECK);
            }
        }

        // Resaltar la pieza seleccionada y sus movimientos posibles
        if (selectedPos != null) {
            highlightCell(g, selectedPos, COLOR_SELECTED);
            if (legalMoves != null) {
                for (int i = 0; i < legalMoves.size(); i++) {
                    drawValidMoveDot(g, legalMoves.get(i));
                }
            }
        }
    }

    private void highlightCell(Graphics2D g, Posicion pos, Color color) {
        int x = LABEL_MARGIN + pos.getCol() * CELL_SIZE;
        int y = pos.getRow() * CELL_SIZE;
        g.setColor(color);
        g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
    }

    private void drawValidMoveDot(Graphics2D g, Posicion pos) {
        int x = LABEL_MARGIN + pos.getCol() * CELL_SIZE + CELL_SIZE / 2;
        int y = pos.getRow() * CELL_SIZE + CELL_SIZE / 2;
        Pieza occupant = juego.getTablero().getPieza(pos);
        if (occupant != null) {
            // Anillo para indicar que se puede capturar
            g.setColor(COLOR_VALID_MOVE);
            g.setStroke(new BasicStroke(5));
            int d = CELL_SIZE - 10;
            g.drawOval(LABEL_MARGIN + pos.getCol() * CELL_SIZE + 5, pos.getRow() * CELL_SIZE + 5, d, d);
            g.setStroke(new BasicStroke(1));
        } else {
            // Punto pequeño para movimiento a casilla vacía
            g.setColor(COLOR_VALID_MOVE);
            int d = 24;
            g.fillOval(x - d / 2, y - d / 2, d, d);
        }
    }

    private void drawCoordinates(Graphics2D g) {
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g.setColor(new Color(160, 160, 180));
        // Letras de columnas (A-H)
        for (int c = 0; c < 8; c++) {
            char letter = (char) ('A' + c);
            g.drawString(String.valueOf(letter),
                LABEL_MARGIN + c * CELL_SIZE + CELL_SIZE / 2 - 4,
                BOARD_SIZE + LABEL_MARGIN - 4);
        }
        // Números de filas (8-1)
        for (int r = 0; r < 8; r++) {
            int num = 8 - r;
            g.drawString(String.valueOf(num),
                6,
                r * CELL_SIZE + CELL_SIZE / 2 + 5);
        }
    }

    private void drawPieces(Graphics2D g) {
        Tablero board = juego.getTablero();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Pieza piece = board.getPieza(new Posicion(r, c));
                if (piece == null) {
                    continue;
                }
                int x = LABEL_MARGIN + c * CELL_SIZE;
                int y = r * CELL_SIZE;
                String key = piece.getColor().name() + "_" + piece.getType().name();
                Image img = pieceImages.get(key);

                if (img != null) {
                    int padding = 6;
                    g.drawImage(img, x + padding, y + padding,
                        CELL_SIZE - padding * 2, CELL_SIZE - padding * 2, null);
                } else {
                    drawUnicodePiece(g, piece, x, y);
                }
            }
        }
    }

    /**
     * Alternativa si no hay imágenes: dibuja el símbolo Unicode de la pieza.
     */
    private void drawUnicodePiece(Graphics2D g, Pieza piece, int x, int y) {
        String symbol = getUnicodeSymbol(piece);
        g.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 48));
        FontMetrics fm = g.getFontMetrics();
        int width = fm.stringWidth(symbol);
        int textX = x + (CELL_SIZE - width) / 2;
        int textY = y + (CELL_SIZE + fm.getAscent() - fm.getDescent()) / 2;

        // Sombra
        g.setColor(new Color(0, 0, 0, 80));
        g.drawString(symbol, textX + 2, textY + 2);

        // Pieza
        if (piece.getColor() == ColorJugador.BLANCO) {
            g.setColor(new Color(255, 255, 255));
        } else {
            g.setColor(new Color(20, 20, 30));
        }
        g.drawString(symbol, textX, textY);
    }

    private String getUnicodeSymbol(Pieza piece) {
        if (piece.getColor() == ColorJugador.BLANCO) {
            if (piece.getType() == TipoPieza.REY)   return "♔";
            if (piece.getType() == TipoPieza.REINA)  return "♕";
            if (piece.getType() == TipoPieza.TORRE)   return "♖";
            if (piece.getType() == TipoPieza.ALFIL) return "♗";
            if (piece.getType() == TipoPieza.CABALLO) return "♘";
            if (piece.getType() == TipoPieza.PEON)   return "♙";
        } else {
            if (piece.getType() == TipoPieza.REY)   return "♚";
            if (piece.getType() == TipoPieza.REINA)  return "♛";
            if (piece.getType() == TipoPieza.TORRE)   return "♜";
            if (piece.getType() == TipoPieza.ALFIL) return "♝";
            if (piece.getType() == TipoPieza.CABALLO) return "♞";
            if (piece.getType() == TipoPieza.PEON)   return "♟";
        }
        return "?";
    }

    // ─── Eventos de ratón ─────────────────────────────────────────────────────

    @Override
    public void mousePressed(MouseEvent e) {
        if (juego.isGameOver() || ventanaPrincipal.isPaused()) {
            return;
        }

        int col = (e.getX() - LABEL_MARGIN) / CELL_SIZE;
        int row = e.getY() / CELL_SIZE;

        if (col < 0 || col >= 8 || row < 0 || row >= 8) {
            return;
        }

        Posicion clicked = new Posicion(row, col);

        if (selectedPos == null) {
            // No hay pieza seleccionada: intentar seleccionar una pieza propia
            Pieza piece = juego.getTablero().getPieza(clicked);
            if (piece != null && piece.getColor() == juego.getTurnoActual()) {
                selectedPos = clicked;
                legalMoves  = juego.getMovimientosLegales(clicked);
            }
        } else {
            if (clicked.equals(selectedPos)) {
                // Clic sobre la misma pieza: deseleccionar
                clearSelection();
            } else if (legalMoves != null && legalMoves.contains(clicked)) {
                // Clic sobre un destino válido: ejecutar movimiento
                executeMove(selectedPos, clicked);
            } else {
                // Clic en otro sitio: intentar seleccionar otra pieza propia
                Pieza piece = juego.getTablero().getPieza(clicked);
                if (piece != null && piece.getColor() == juego.getTurnoActual()) {
                    selectedPos = clicked;
                    legalMoves  = juego.getMovimientosLegales(clicked);
                } else {
                    clearSelection();
                }
            }
        }
        repaint();
    }

    private void executeMove(Posicion desde, Posicion hasta) {
        TipoPieza eleccionPromocion = null;

        // Comprobar si el movimiento es una coronación de peón
        Pieza piece = juego.getTablero().getPieza(desde);
        if (piece != null && piece.getType() == TipoPieza.PEON) {
            int filaPromocion;
            if (piece.getColor() == ColorJugador.BLANCO) {
                filaPromocion = 0;
            } else {
                filaPromocion = 7;
            }
            if (hasta.getRow() == filaPromocion) {
                JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                DialogoPromocion dialog = new DialogoPromocion(
                    parentFrame, piece.getColor(), getImagesForColor(piece.getColor())
                );
                eleccionPromocion = dialog.getPiezaElegida();
            }
        }

        juego.makeMove(desde, hasta, eleccionPromocion);
        clearSelection();

        // Notificar a la ventana principal para que actualice el estado
        ventanaPrincipal.actualizarEstado();
    }

    private void clearSelection() {
        selectedPos = null;
        legalMoves  = null;
    }

    // Métodos de MouseListener que no necesitamos usar
    @Override public void mouseClicked(MouseEvent e)  { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e)  { }
    @Override public void mouseExited(MouseEvent e)   { }
}
