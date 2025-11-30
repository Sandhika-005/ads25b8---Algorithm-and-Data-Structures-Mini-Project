import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {
    // Map untuk menyimpan High Score (Nama -> Skor Tertinggi)
    private static final Map<String, Integer> scoreMap = new HashMap<>();

    // Map untuk menyimpan Jumlah Kemenangan (Nama -> Jumlah Menang)
    private static final Map<String, Integer> winMap = new HashMap<>();

    // Update Score: Jika nama sudah ada, ambil nilai yang lebih besar
    public static void updateScore(String name, int score) {
        scoreMap.merge(name, score, Integer::max);
    }

    // Update Win: Jika nama sudah ada, tambahkan jumlah kemenangannya (+1)
    public static void addWin(String name) {
        winMap.merge(name, 1, Integer::sum);
    }

    // Mengambil Top 3 High Score
    public static String getTop3Scores() {
        return getTop3FromMap(scoreMap, "Score");
    }

    // Mengambil Top 3 Wins
    public static String getTop3Wins() {
        return getTop3FromMap(winMap, "Wins");
    }

    // Helper method untuk sorting dan formatting output
    private static String getTop3FromMap(Map<String, Integer> map, String label) {
        if (map.isEmpty()) return "Belum ada data.";

        List<Map.Entry<String, Integer>> sortedList = map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        int rank = 1;
        for (Map.Entry<String, Integer> entry : sortedList) {
            sb.append(rank++).append(". ")
                    .append(entry.getKey()).append(" : ")
                    .append(entry.getValue()).append(" ").append(label)
                    .append("\n");
        }
        return sb.toString();
    }
}