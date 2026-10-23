package Model;

public enum ColorJugador {
    BLANCO,
    NEGRO;

    public ColorJugador opposite() {
        if (this == BLANCO) {
            return NEGRO;
        } else {
            return BLANCO;
        }
    }
}
