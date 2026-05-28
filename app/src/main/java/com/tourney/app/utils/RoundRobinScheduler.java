package com.tourney.app.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Poprawny algorytm Round Robin metodą "circle/rotation".
 *
 * Dla n teamów (parzyste):
 *   - leg 1: n-1 matchday, każdy po n/2 meczów
 *   - leg 2: to samo z zamienionymi gospodarzami
 *   - łącznie: n*(n-1) meczów
 *
 * Dla n nieparzystego: dodajemy BYE (id=-1), traktujemy jak n+1.
 */
public class RoundRobinScheduler {

    public static class ScheduledMatch {
        public final int teamAId;
        public final int teamBId;
        public final int round;   // 1-based matchday numer
        public final boolean isSecondLeg;

        public ScheduledMatch(int teamAId, int teamBId, int round, boolean isSecondLeg) {
            this.teamAId = teamAId;
            this.teamBId = teamBId;
            this.round = round;
            this.isSecondLeg = isSecondLeg;
        }
    }

    /**
     * @param teamIds lista ID teamów w turnieju
     * @param doubleLegged true = double leg (każda para gra 2 razy, raz u siebie)
     * @return lista ScheduledMatch z poprawnymi rundami
     */
    public static List<ScheduledMatch> generate(List<Integer> teamIds, boolean doubleLegged) {
        List<Integer> teams = new ArrayList<>(teamIds);

        // Jeśli nieparzysta liczba teamów → dodaj BYE
        if (teams.size() % 2 != 0) {
            teams.add(-1); // -1 = BYE
        }

        int n = teams.size();
        int rounds = n - 1;         // liczba matchday w jednym leg
        int matchesPerRound = n / 2;

        List<ScheduledMatch> schedule = new ArrayList<>();

        // Algorytm "circle": team[0] stoi w miejscu, reszta rotuje
        // teams[0] jest "pinned", teams[1..n-1] rotują co rundę w lewo
        List<Integer> rotation = new ArrayList<>(teams);

        for (int round = 1; round <= rounds; round++) {
            List<int[]> pairs = new ArrayList<>();

            // Generuj pary dla tej rundy
            for (int i = 0; i < matchesPerRound; i++) {
                int a = rotation.get(i);
                int b = rotation.get(n - 1 - i);
                if (a != -1 && b != -1) { // pomijaj mecze z BYE
                    // Parzyste rundy — odwróć gospodarkę dla lepszego rozłożenia
                    if (round % 2 == 0) {
                        pairs.add(new int[]{b, a});
                    } else {
                        pairs.add(new int[]{a, b});
                    }
                }
            }

            for (int[] pair : pairs) {
                schedule.add(new ScheduledMatch(pair[0], pair[1], round, false));
            }

            // Rotacja: team[0] zostaje, teams[1] idzie na koniec, reszta przesuwa się
            // rotation: [fixed, t1, t2, t3, t4, t5] → [fixed, t2, t3, t4, t5, t1]
            Integer last = rotation.remove(n - 1);
            rotation.add(1, last);
        }

        // Leg 2: te same pary, zamienione strony (home/away), rundy n..2n-2
        if (doubleLegged) {
            // Zbierz mecze z leg 1 i odbij
            List<ScheduledMatch> leg1 = new ArrayList<>(schedule);
            for (ScheduledMatch m : leg1) {
                // W leg 2 zamień teamA ↔ teamB (rewanż)
                schedule.add(new ScheduledMatch(
                    m.teamBId,
                    m.teamAId,
                    m.round + rounds, // matchday = rounds+1 .. rounds*2
                    true
                ));
            }
        }

        return schedule;
    }

    /**
     * Walidacja — sprawdza czy harmonogram jest poprawny matematycznie.
     * Każdy team powinien zagrać dokładnie (n-1) razy w leg 1.
     * @return pusty string jeśli ok, opis błędu jeśli nie
     */
    public static String validate(List<ScheduledMatch> schedule, List<Integer> teamIds, boolean doubleLegged) {
        int n = teamIds.size();
        int expectedMatchesPerTeam = (n % 2 == 0) ? n - 1 : n - 1; // zawsze n-1 (BYE się nie liczy)
        if (doubleLegged) expectedMatchesPerTeam *= 2;

        java.util.Map<Integer, Integer> counts = new java.util.HashMap<>();
        for (Integer id : teamIds) counts.put(id, 0);

        for (ScheduledMatch m : schedule) {
            if (counts.containsKey(m.teamAId))
                counts.put(m.teamAId, counts.get(m.teamAId) + 1);
            if (counts.containsKey(m.teamBId))
                counts.put(m.teamBId, counts.get(m.teamBId) + 1);
        }

        for (java.util.Map.Entry<Integer, Integer> e : counts.entrySet()) {
            if (e.getValue() != expectedMatchesPerTeam) {
                return "Team " + e.getKey() + " ma " + e.getValue()
                    + " meczów zamiast " + expectedMatchesPerTeam;
            }
        }
        return "";
    }
}
