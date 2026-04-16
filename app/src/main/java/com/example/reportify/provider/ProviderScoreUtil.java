//package com.example.reportify.provider;
//
//import com.example.reportify.models.Complaint;
//import com.example.reportify.models.ProviderMetrics;
//import com.example.reportify.utils.FirebaseManager;
//import com.google.firebase.firestore.DocumentSnapshot;
//
//public class ProviderScoreUtil {
//
//    public interface MetricsCallback {
//        void onMetricsCalculated(ProviderMetrics metrics);
//    }
//
//    public static void calculateMetrics(String providerId, MetricsCallback callback) {
//
//        FirebaseManager.getFirestore()
//                .collection("complaints")
//                .whereEqualTo("providerId", providerId)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//
//                    int total = 0;
//                    int resolved = 0;
//
//                    double totalRating = 0;
//                    int ratingCount = 0;
//
//                    long totalResponseTime = 0;
//                    int responseCount = 0;
//
//                    long totalResolutionTime = 0;
//                    int resolutionCount = 0;
//
//                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
//
//                        Complaint c = doc.toObject(Complaint.class);
//                        if (c == null) continue;
//
//                        total++;
//
//                        if ("RESOLVED".equals(c.getStatus())) {
//                            resolved++;
//                        }
//
//                        if (c.getRating() > 0) {
//                            totalRating += c.getRating();
//                            ratingCount++;
//                        }
//                    }
//
//                    ProviderMetrics metrics = new ProviderMetrics();
//
//                    metrics.avgRating = ratingCount == 0 ? 0 : totalRating / ratingCount;
//
//                    metrics.completionRate =
//                            total == 0 ? 0 : (resolved * 100.0 / total);
//
//                    metrics.avgResponseTime =
//                            responseCount == 0 ? 0 : totalResponseTime / responseCount;
//
//                    metrics.avgResolutionTime =
//                            resolutionCount == 0 ? 0 : totalResolutionTime / resolutionCount;
//
//                    double speedScore = 0;
//                    if (metrics.avgResponseTime > 0) {
//                        speedScore = 1000000.0 / metrics.avgResponseTime;
//                    }
//
//                    metrics.score =
//                    ...........................................
//                            (metrics.avgRating * 0.5) +
//                                    (metrics.completionRate * 0.3) +
//                                    (speedScore * 0.2);
//
//                    callback.onMetricsCalculated(metrics);
//                });
//    }
//}
