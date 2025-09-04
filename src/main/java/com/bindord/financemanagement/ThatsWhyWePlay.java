package com.bindord.financemanagement;

import com.bindord.financemanagement.utils.Utilities;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class ThatsWhyWePlay {

  @SneakyThrows
  public static void main(String[] args) {
    //System.out.println("Hola Petter Paul,\r\n\r\nRealizaste un consumo de S/ 52.30 con tu
      // Tarjeta de Débito BCP en PedidosYA NCVV.\r\n\r\nPor tu seguridad, te enviamos los datos
      // de tu operación.\r\n\r\n\r\n\r\nMonto\r\n\r\n\r\nTotal del consumo       S/ 52
      // .30\r\n\r\n\r\nDatos de la operación\r\n\r\n\r\nOpera\"");
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

    System.out.println(Utilities.generateSha256FromMailIdOrPayee(LocalDateTime.of(2025,
        Month.MAY, 5, 10, 10), "Trio Movistar"));

    System.out.println(Utilities.generateSha256FromMailIdOrPayee(LocalDateTime.of(2025,
        Month.MAY, 5, 10, 10), "Cuota Departamento Jesus Maria"));

    System.out.println(Utilities.generateSha256FromMailIdOrPayee(LocalDateTime.of(2025,
        Month.MAY, 5, 10, 10), "Internet Win"));

    System.out.println(Utilities.generateSha256FromMailIdOrPayee(LocalDateTime.of(2025,
        Month.MAY, 5, 10, 10), "Mantenimiento Depa, Luz y Agua"));

    System.out.println(Utilities.generateSha256FromMailIdOrPayee(LocalDateTime.of(2025,
        Month.MAY, 5, 10, 10), "Departamento Mader"));

    System.out.println(LocalDateTime.of(2025,
        Month.MAY, 6, 10, 10));

    LinkedList<String> paragraphs = new LinkedList<>();
    paragraphs.add("The price is S/ 1500.45 for the car");
    paragraphs.add("and $ 200 for additional accessories.");
    paragraphs.addFirst("Another item costs S/ 300.23.");
    System.out.println(paragraphs);

    ArrayList<String> arrayList = new ArrayList<>(paragraphs);

    Map<Integer, String> myMap = Map.of(1, "Petter", 2, "Rodrigo", 3, "Agustin");
    HashMap<Integer, String> myHashMap = new HashMap<>(myMap);
    myHashMap.put(4, "Francisco");
    myHashMap.forEach((key, val) -> {
      System.out.println("key: " + key + " - value: " + val);
    });

    ArrayList<String> arrayList1 = new ArrayList<>(List.of("Petter", "Rodrigo", "Agustin"));
    ArrayList<String> arrayList2 = new ArrayList<>(List.of("Ana", "Berta", "Cynthia"));
    IntStream.range(0, 3).peek(i -> {
      System.out.println(arrayList1.get(i));
    }).forEach(i -> {
      System.out.println(arrayList2.get(i));
    });

    InputStreamReader inputStreamReader = new InputStreamReader(System.in);
    FileReader fileReader = new FileReader(new File("/home/bin/bin.txt"));


  }
}
