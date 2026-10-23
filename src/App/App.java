package App;

import UI.GUIAjedrez;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada de la aplicación de Ajedrez.
 * Lanza la interfaz gráfica en el hilo de eventos de Swing (EDT).
 */
public class App {
    public static void main(String[] args) {
        // Intentar usar el look and feel del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla, usa el L&F por defecto de Java
        }

        // Lanzar la GUI en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> new GUIAjedrez());
    }
}
