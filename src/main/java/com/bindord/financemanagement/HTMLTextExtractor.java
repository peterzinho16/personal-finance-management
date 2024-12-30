package com.bindord.financemanagement;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HTMLTextExtractor {
  public static String extractTextJsoup(String html) {
    // Using Jsoup - Most recommended approach

    Document doc = Jsoup.parse(html);
    return doc.text(); // Equivalent to textContent, removes all HTML tags
  }

  public static String extractTextRegex(String html) {
    // Using regex - Simple but not recommended for complex HTML
    return html.replaceAll("<[^>]*>", "")
        .replaceAll("\\s+", " ")
        .trim();
  }

  public static String extractTextApacheCommons(String html) {
    // Using Apache Commons Text
    String unescaped = StringEscapeUtils.unescapeHtml4(html);
    return unescaped.replaceAll("<[^>]*>", "")
        .replaceAll("\\s+", " ")
        .trim();
  }

  public static void main(String[] args) {
    //String htmlExample = "<div>Hello <b>World</b>! <p>This is a <i>test</i>.</p></div>";
    String htmlExample = "<html lang=\"en\"><head>\n" +
        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><meta name=\"x-apple-disable-message-reformatting\"></head><body style=\"margin:0; padding:0px; background-color:#f4f4f4; font-family:Roboto,Arial,sans-serif\"><table role=\"presentation\" style=\"width:100%; border-collapse:collapse; border:0; border-spacing:0; background-color:#f4f4f4; margin:30px 0\"><tbody><tr><td align=\"center\" style=\"padding:0\"><table role=\"presentation\" style=\"width:600px; border-collapse:collapse; border-spacing:0\"><tbody><tr><td align=\"center\" style=\"padding:10px; background-color:#eeeeee; border-radius:0px 0px 5px 5px\"><img src=\"https://s3.amazonaws.com/diners-img/diners_logo2.png\" width=\"170px\" alt=\"Diners\" style=\"height:auto; display:block; margin:0 auto\"> </td></tr><tr><td style=\"padding:20px; font-size:16px; background-color:#ffffff; color:#333333\"><p>Estimado Socio:</p><p style=\"text-align:justify\">Le informamos que usted realizó un consumo el 23/12/2024 a las 17:24:18 horas en el comercio E S APOLLO S MARKET PE por un importe de S/ 50.00. Si tuviera alguna consulta comuníquese con nosotros al (01)615.1111 o escríbanos a <b>socios@dinersclub.com.pe</b> </p><p style=\"text-align:justify\">Descargue la APP Diners Club Perú y disfrute de todos los beneficios que tenemos para usted.</p><p style=\"text-align:justify\">Atentamente,</p><p>DINERS CLUB</p></td></tr></tbody></table></td></tr></tbody></table><img alt=\"\" src=\"https://xwv3sgcr.r.us-east-1.awstrack.me/I0/01000193f5dd1cf6-96c1d706-7fac-44bd-87bd-b0276e9e261d-000000/dpE-1Fwen0MPCW1STpjdnDoI9tI=405\" style=\"display:none; width:1px; height:1px\"> </body></html>";
    // Using Jsoup
    System.out.println("Jsoup result: " + extractTextJsoup(htmlExample));

    // Using regex
    System.out.println("Regex result: " + extractTextRegex(htmlExample));

    // Using Apache Commons
    System.out.println("Apache Commons result: " + extractTextApacheCommons(htmlExample));

  }
}