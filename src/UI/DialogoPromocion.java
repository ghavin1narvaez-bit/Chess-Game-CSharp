package UI;

import Model.TipoPieza;
import Model.ColorJugador;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Diálogo modal que aparece cuando un peón llega a la última fila.
 * El jugador elige a qué pieza promocionar: Reina, Torre, Alfil o Caballo.
 */
public class DialogoPromocion extends JDialog {

    // Pieza elegida por el jugador (por defecto, Reina)
    private TipoPieza elegida = TipoPieza.REINA;
    private final Map<TipoPieza, Image> imagenesPiezas;
    private final ColorJugador color;

    public DialogoPromocion(JFrame parent, ColorJugador color, Map<TipoPieza, Image> images) {
        super(parent, "Coronacion del Peon", true);
        this.color = color;
        this.imagenesPiezas = images;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(30, 30, 46));

        JLabel title = new JLabel("Elige la pieza para coronar:", SwingConstants.CENTER);
        title.setForeground(new Color(205, 214, 244));
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setBorder(BorderFactory.createEmptyBorder(12, 10, 8, 10));
        add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(30, 30, 46));

        // Las 4 opciones de coronación
        TipoPieza[] opciones = { TipoPieza.REINA, TipoPieza.TORRE, TipoPieza.ALFIL, TipoPieza.CABALLO };
        String[] labels = { "Reina", "Torre", "Alfil", "Caballo" };

        for (int i = 0; i < opciones.length; i++) {
            final TipoPieza tipo = opciones[i]; // "final" para poder usarlo dentro del ActionListener
            Image img = imagenesPiezas.get(tipo);

            JButton btn = new JButton(labels[i]);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(69, 71, 90));
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(80, 80));
            btn.setVerticalTextPosition(SwingConstants.BOTTOM);
            btn.setHorizontalTextPosition(SwingConstants.CENTER);

            if (img != null) {
                btn.setIcon(new ImageIcon(img.getScaledInstance(52, 52, Image.SCALE_SMOOTH)));
            }

            // Al hacer clic, guardar la elección y cerrar el diálogo
            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    elegida = tipo;
                    dispose();
                }
            });

            // Efecto hover
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(new Color(137, 180, 130));
                }

                public void mouseExited(MouseEvent e) {
                    btn.setBackground(new Color(69, 71, 90));
                }
            });

            buttonPanel.add(btn);
        }

        add(buttonPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    /**
     * Muestra el diálogo y devuelve la pieza elegida por el jugador.
     * setVisible(true) bloquea hasta que el diálogo se cierra.
     */
    public TipoPieza getPiezaElegida() {
        setVisible(true);
        return elegida;
    }
}
