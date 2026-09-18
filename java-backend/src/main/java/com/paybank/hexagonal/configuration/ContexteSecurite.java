package com.paybank.hexagonal.configuration;

public class ContexteSecurite {

    private static final ThreadLocal<String> utilisateurConnecteIdHolder = new ThreadLocal<>();

    /**
     * Définit l'ID de l'utilisateur actuellement connecté dans le contexte courant.
     */
    public static void setUtilisateurConnecteId(String id) {
        utilisateurConnecteIdHolder.set(id);
    }

    /**
     * Récupère l'ID de l'utilisateur connecté.
     */
    public static String getUtilisateurConnecteId() {
        return utilisateurConnecteIdHolder.get();
    }

    /**
     * Nettoie le contexte (à appeler systématiquement après les tests dans un tearDown).
     */
    public static void clear() {
        utilisateurConnecteIdHolder.remove();
    }
}
