package com.bindord.financemanagement;

import com.bindord.financemanagement.utils.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThatsWhyWePlay {

    public static void main(String[] args) {
        //System.out.println("Hola Petter Paul,\r\n\r\nRealizaste un consumo de S/ 52.30 con tu Tarjeta de Débito BCP en PedidosYA NCVV.\r\n\r\nPor tu seguridad, te enviamos los datos de tu operación.\r\n\r\n\r\n\r\nMonto\r\n\r\n\r\nTotal del consumo       S/ 52.30\r\n\r\n\r\nDatos de la operación\r\n\r\n\r\nOpera\"");
        List<String> paragraphParts = List.of(
            "The price is S/ 1500.45 for the car",
            "and $ 200 for additional accessories.",
            "Another item costs S/ 300.23."
        );

        // Pattern to find numbers after "S/" or "$"
        Pattern pattern = Pattern.compile("(S/|\\$)\\s*(\\d+(\\.\\d+)?)");

        List<String> foundNumbers = new ArrayList<>();

        for (String part : paragraphParts) {
            Matcher matcher = pattern.matcher(part);
            while (matcher.find()) {
                // Extract the matched number (group 2)
                foundNumbers.add(matcher.group(2));
            }
        }

        // Print the results
        System.out.println("Numbers found: " + foundNumbers);
        System.out.println(Utilities.convertDatetimeToUTCMinusFive("2024-12-28T20:19:20Z"));

    }
}
