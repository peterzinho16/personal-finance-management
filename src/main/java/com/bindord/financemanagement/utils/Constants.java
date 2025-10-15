package com.bindord.financemanagement.utils;

public class Constants {


  public static final String RESOURCE_NOT_FOUND = "Resource not found";
  public static final String DEFAULT_EXPENDITURE_CATEGORY = "Por definir";

  public static final String INBOX_FOLDER_ID =
      "AQMkADAwATNiZmYAZC1jNTRiLTg5N2EtMDACLTAwCgAuAAADoowy8RjcUkyj3Qv2gECIoAEAJZOEFSNNi02ZcdREwq9DeAAAAgEMAAAA";
  public static final String NOTIF_COMPRAS_SUB_FOLDER_ID =
      "AQMkADAwATNiZmYAZC1jNTRiLTg5N2EtMDACLTAwCgAuAAADoowy8RjcUkyj3Qv2gECIoAEAJZOEFSNNi02ZcdREwq9DeAAGx92vkwAAAA==";
  public static final String MSG_ERROR_INSTALLMENTS_NOT_MODIFICATION_ALLOWED = "The expenditure " +
      "was financed and <b>the installments can't be modified</b>";
  public static final String MSG_ERROR_SHARED_AND_LENT_AND_BORROWED = "The expenditure can be " +
      "submitted because two of these options, <b>shared/lent/borrowed</b>, can't be true at the" +
      " same" +
      " " +
      "time";
  public static final String MSG_ERROR_SHARED_ALREADY = "The expenditure is already stored as shared, you can't reverse it";

  public static final String MSG_ERROR_EXP_IMPORTED_ALREADY = "The expenditure was already imported";

  public static final String MSG_ERROR_EXP_NOT_SHARED_CANT_BE_IMPORTED = "Expenditures not shared, can't be imported!!";



}
