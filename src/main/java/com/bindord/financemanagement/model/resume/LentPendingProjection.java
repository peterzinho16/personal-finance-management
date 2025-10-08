package com.bindord.financemanagement.model.resume;

public interface LentPendingProjection {
  String getPeriodo();       // e.g. '2025-10'
  String getLoanState();     // e.g. 'pending' or 'paid'
  String getLentTo();        // person name
  Double getMontoAcumulado(); // aggregated amount
}