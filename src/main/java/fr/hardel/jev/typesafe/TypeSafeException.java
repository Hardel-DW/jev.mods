package fr.hardel.jev.typesafe;

/** The API said no, or could not be reached; the status tells whether waiting helps (429 and 529 do, the rest does not). */
public final class TypeSafeException extends RuntimeException {
    private final int status;

    public TypeSafeException(int status, String message) {
        super(message);
        this.status = status;
    }

    public boolean transient_() {
        return status == 429 || status == 529 || status == 0;
    }
}
