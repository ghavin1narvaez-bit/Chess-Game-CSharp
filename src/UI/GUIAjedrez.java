package UI;

import Model.Juego;
import Model.Juego.EstadoJuego;
import Model.ColorJugador;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Ventana principal de la aplicación de Ajedrez.
 * Contiene la barra de menú, el tablero y el panel de información.
 */
public class GUIAjedrez extends JFrame {

    private Juego juego;
    private PanelTablero panelTablero;
    private JLabel statusLabel;
    private JLabel turnLabel;
    private JLabel moveCountLabel;
    private Timer gameTimer;
    private int elapsedSeconds = 0;
    private JLabel timerLabel;
    private boolean isPaused = false;

    public GUIAjedrez() {
        juego = new Juego();
        initWindow();
        buildMenu();
        buildContent();
        initTimer();
        actualizarEstado();
        setVisible(true);
    }

    // ─── Configuración de la ventana ──────────────────────────────────────────

    private void initWindow() {
        setTitle("Ajedrez - Grup 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(30, 30, 46));
        setLayout(new BorderLayout());
    }

    // ─── Menú ─────────────────────────────────────────────────────────────────

    private void buildMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(24, 24, 37));
        menuBar.setBorder(BorderFactory.createEmptyBorder());

        // -- Menú "Partida" --
        JMenu menuGame = createStyledMenu("Partida");

        JMenuItem itemNew = createStyledMenuItem("Nueva Partida", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK);
        itemNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nuevaPartida();
            }
        });

        JMenuItem itemResign = createStyledMenuItem("Rendirse", KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);
        itemResign.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rendirse();
            }
        });

        JMenuItem itemExit = createStyledMenuItem("Salir", KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK);
        itemExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        menuGame.add(itemNew);
        menuGame.addSeparator();
        menuGame.add(itemResign);
        menuGame.addSeparator();
        menuGame.add(itemExit);

        // -- Menú "Ayuda" --
        JMenu menuHelp = createStyledMenu("Ayuda");
        JMenuItem itemAbout = createStyledMenuItem("Acerca de", 0, 0);
        itemAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mostrarAcercaDe();
            }
        });
        menuHelp.add(itemAbout);

        menuBar.add(menuGame);
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);
    }

    private JMenu createStyledMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setForeground(new Color(205, 214, 244));
        menu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return menu;
    }

    private JMenuItem createStyledMenuItem(String text, int keyCode, int modifiers) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.setForeground(new Color(205, 214, 244));
        item.setBackground(new Color(30, 30, 46));
        if (keyCode != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers));
        }
        return item;
    }

    // ─── Contenido principal ──────────────────────────────────────────────────

    private void buildContent() {
        JPanel infoPanel = buildInfoPanel();

        // Pasamos 'this' para que PanelTablero pueda notificarnos de movimientos
        panelTablero = new PanelTablero(juego, this);

        add(panelTablero, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(24, 24, 37));
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));

        // Título
        JLabel title = new JLabel("AJEDREZ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(137, 180, 130));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Grup 2");
        subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        subtitle.setForeground(new Color(108, 112, 134));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(69, 71, 90));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Etiqueta de turno
        turnLabel = new JLabel("Turno: BLANCAS");
        turnLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        turnLabel.setForeground(new Color(205, 214, 244));
        turnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Etiqueta de estado
        statusLabel = new JLabel("En juego");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(166, 227, 161));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Contador de movimientos
        moveCountLabel = new JLabel("Movimiento: 1");
        moveCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        moveCountLabel.setForeground(new Color(108, 112, 134));
        moveCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Temporizador
        timerLabel = new JLabel("Tiempo: 00:00");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        timerLabel.setForeground(new Color(205, 214, 244));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Botón "Nueva Partida"
        JButton btnNew = createActionButton("Nueva Partida", new Color(137, 180, 130));
        btnNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nuevaPartida();
            }
        });

        // Botón "Pausar / Reanudar"
        JButton btnPause = createActionButton("Pausa / Play", new Color(249, 226, 175));
        btnPause.setForeground(new Color(30, 30, 46));
        btnPause.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });

        // Botón "Rendirse"
        JButton btnResign = createActionButton("Rendirse", new Color(243, 139, 168));
        btnResign.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rendirse();
            }
        });

        JPanel legend = buildLegend();

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(16));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(16));
        panel.add(turnLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(moveCountLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(timerLabel);
        panel.add(Box.createVerticalStrut(24));
        panel.add(btnNew);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnPause);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnResign);
        panel.add(Box.createVerticalGlue());
        panel.add(legend);

        return panel;
    }

    private JButton createActionButton(String text, final Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color.darker());
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        // Efecto hover: cambia el color al pasar el ratón por encima
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(color.darker());
            }
        });
        return btn;
    }

    private JPanel buildLegend() {
        JPanel p = new JPanel(new GridLayout(4, 1, 0, 3));
        p.setBackground(new Color(24, 24, 37));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(legendItem(new Color(20, 200, 80, 160),  "Seleccionado"));
        p.add(legendItem(new Color(20, 200, 80, 90),   "Movimiento valido"));
        p.add(legendItem(new Color(220, 50, 50, 180),  "Jaque"));
        p.add(legendItem(new Color(255, 220, 50, 90),  "Ultimo movimiento"));
        return p;
    }

    private JPanel legendItem(Color color, String text) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setBackground(new Color(24, 24, 37));
        JLabel dot = new JLabel("●");
        dot.setForeground(color);
        dot.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(205, 214, 244));
        item.add(dot);
        item.add(lbl);
        return item;
    }

    // ─── Actualización del estado ─────────────────────────────────────────────

    /**
     * Actualiza las etiquetas de turno y estado.
     * Este método lo llama BoardPanel después de cada movimiento.
     */
    public void actualizarEstado() {
        EstadoJuego estado = juego.getEstadoJuego();
        int totalMovimientos = juego.getHistorialMovimientos().size();

        // Actualizar etiqueta de turno (solo si la partida sigue activa)
        if (!juego.isGameOver()) {
            if (juego.getTurnoActual() == ColorJugador.BLANCO) {
                turnLabel.setText("Turno: BLANCAS");
            } else {
                turnLabel.setText("Turno: NEGRAS");
            }
        }

        // Número de jugada (cada 2 medios movimientos = 1 jugada completa)
        moveCountLabel.setText("Movimiento: " + (totalMovimientos / 2 + 1));

        // Actualizar etiqueta de estado según el estado del juego
        if (estado == EstadoJuego.JUGANDO) {
            statusLabel.setForeground(new Color(166, 227, 161));
            statusLabel.setText("En juego");

        } else if (estado == EstadoJuego.BLANCAS_EN_JAQUE) {
            statusLabel.setForeground(new Color(250, 179, 135));
            statusLabel.setText("Blancas en JAQUE");

        } else if (estado == EstadoJuego.NEGRAS_EN_JAQUE) {
            statusLabel.setForeground(new Color(250, 179, 135));
            statusLabel.setText("Negras en JAQUE");

        } else if (estado == EstadoJuego.BLANCAS_GANAN) {
            turnLabel.setText("BLANCAS GANAN");
            statusLabel.setForeground(new Color(137, 180, 130));
            statusLabel.setText("Jaque mate!");
            mostrarDialogoFinPartida("Jaque mate! Ganan las BLANCAS");

        } else if (estado == EstadoJuego.NEGRAS_GANAN) {
            turnLabel.setText("NEGRAS GANAN");
            statusLabel.setForeground(new Color(137, 180, 130));
            statusLabel.setText("Jaque mate!");
            mostrarDialogoFinPartida("Jaque mate! Ganan las NEGRAS");

        } else if (estado == EstadoJuego.AHOGADO) {
            turnLabel.setText("EMPATE");
            statusLabel.setForeground(new Color(148, 226, 213));
            statusLabel.setText("Ahogado - Tablas");
            mostrarDialogoFinPartida("Ahogado! La partida termina en TABLAS.");

        } else if (estado == EstadoJuego.TABLAS) {
            turnLabel.setText("EMPATE");
            statusLabel.setForeground(new Color(148, 226, 213));
            statusLabel.setText("Tablas - Material insuf.");
            mostrarDialogoFinPartida("Material insuficiente. Partida en TABLAS.");

        } else if (estado == EstadoJuego.BLANCAS_RENUNCIAN) {
            turnLabel.setText("NEGRAS GANAN");
            statusLabel.setForeground(new Color(243, 139, 168));
            statusLabel.setText("Blancas se rindieron");
            mostrarDialogoFinPartida("Las BLANCAS se rindieron. Ganan las NEGRAS.");

        } else if (estado == EstadoJuego.NEGRAS_RENUNCIAN) {
            turnLabel.setText("BLANCAS GANAN");
            statusLabel.setForeground(new Color(243, 139, 168));
            statusLabel.setText("Negras se rindieron");
            mostrarDialogoFinPartida("Las NEGRAS se rindieron. Ganan las BLANCAS.");
        }

        if (juego.isGameOver() && gameTimer != null) {
            gameTimer.stop();
        }

        panelTablero.repaint();
    }

    private void mostrarDialogoFinPartida(String mensaje) {
        String[] opciones = {"Nueva Partida", "Salir"};
        int opcion = JOptionPane.showOptionDialog(
            this, mensaje, "Fin de la partida",
            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, opciones, "Nueva Partida"
        );
        if (opcion == JOptionPane.YES_OPTION) {
            nuevaPartida();
        } else {
            System.exit(0);
        }
    }

    // ─── Acciones ─────────────────────────────────────────────────────────────

    private void initTimer() {
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPaused && !juego.isGameOver()) {
                    elapsedSeconds++;
                    updateTimerLabel();
                }
            }
        });
        gameTimer.start();
    }

    private void updateTimerLabel() {
        int minutes = elapsedSeconds / 60;
        int seconds = elapsedSeconds % 60;
        timerLabel.setText(String.format("Tiempo: %02d:%02d", minutes, seconds));
    }

    private void togglePause() {
        if (juego.isGameOver()) return;
        isPaused = !isPaused;
        if (isPaused) {
            statusLabel.setText("Pausado");
            statusLabel.setForeground(new Color(249, 226, 175));
        } else {
            actualizarEstado();
        }
    }
    
    public boolean isPaused() {
        return isPaused;
    }

    private void nuevaPartida() {
        juego.reset();
        isPaused = false;
        elapsedSeconds = 0;
        updateTimerLabel();
        if (gameTimer != null) gameTimer.start();
        panelTablero.repaint();
        actualizarEstado();
    }

    private void rendirse() {
        if (juego.isGameOver()) {
            return;
        }
        String jugador;
        if (juego.getTurnoActual() == ColorJugador.BLANCO) {
            jugador = "Blancas";
        } else {
            jugador = "Negras";
        }
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "Seguro que " + jugador + " quieren rendirse?",
            "Rendirse",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirmacion == JOptionPane.YES_OPTION) {
            juego.resign();
            actualizarEstado();
        }
    }

    private void mostrarAcercaDe() {
        JOptionPane.showMessageDialog(this,
            "Ajedrez - Practica 06\n" +
            "CFGS DAM 1r Curs - Modul 0485 Programacio\n" +
            "Grup 2 - Curs 25-26\n\n" +
            "Desarrollado con Java Swing.",
            "Acerca de",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
