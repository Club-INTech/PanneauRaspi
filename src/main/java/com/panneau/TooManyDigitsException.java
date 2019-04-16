package com.panneau;

/**
 * Exception levée lorsque l'on tente d'afficher plus de chiffres que l'écran ne peut en contenir.
 * @author rene
 * @since ever
 * @version 1.0
 */
public class TooManyDigitsException extends Exception {
    TooManyDigitsException(int overSize, int max){
        super("Il y a "+ overSize+" chiffres en trop: le maximum affichable est "+max);
    }
}
