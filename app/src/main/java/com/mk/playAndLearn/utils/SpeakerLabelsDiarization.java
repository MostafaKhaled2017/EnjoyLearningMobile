package com.mk.playAndLearn.utils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Created by VMac on 15/05/17.
 */

public class SpeakerLabelsDiarization {
   /* public static class RecoToken {
        private Double startTime;
        private Double endTime;
        private Integer speaker;
        private String word;
        private Boolean spLabelIsFinal;

        RecoToken(SpeechTimestamp speechTimestamp) {
            startTime = speechTimestamp.getStartTime();
            endTime = speechTimestamp.getEndTime();
            word = speechTimestamp.getWord();
        }

        RecoToken(SpeakerLabel speakerLabel) {
            startTime = speakerLabel.getFrom();
            endTime = speakerLabel.getTo();
            speaker = speakerLabel.getSpeaker();
        }

        void updateFrom(SpeechTimestamp speechTimestamp) {
            word = speechTimestamp.getWord();
        }

        void updateFrom(SpeakerLabel speakerLabel) {
            speaker = speakerLabel.getSpeaker();
        }
    }

    public static class Utterance {
        private Integer speaker;
        private String transcript = "";

        public Utterance(final Integer speaker, final String transcript) {
            this.speaker = speaker;
            this.transcript = transcript;
        }
    }

    public static class RecoTokens {

        private Map<Double, RecoToken> recoTokenMap;

        public RecoTokens() {
            recoTokenMap = new LinkedHashMap<Double, RecoToken>();
        }

        public void add(SpeechResults speechResults) {
            if (speechResults.getResults() != null)
                for (int i = 0; i < speechResults.getResults().size(); i++) {
                    Transcript transcript = speechResults.getResults().get(i);
                    if (transcript.isFinal()) {
                        SpeechAlternative speechAlternative = transcript.getAlternatives().get(0);

                        for (int ts = 0; ts < speechAlternative.getTimestamps().size(); ts++) {
                            SpeechTimestamp speechTimestamp = speechAlternative.getTimestamps().get(ts);
                            add(speechTimestamp);
                        }
                    }
                }
            if (speechResults.getSpeakerLabels() != null)
                for (int i = 0; i < speechResults.getSpeakerLabels().size(); i++) {
                    add(speechResults.getSpeakerLabels().get(i));
                }

        }

        public void add(SpeechTimestamp speechTimestamp) {
            RecoToken recoToken = recoTokenMap.get(speechTimestamp.getStartTime());
            if (recoToken == null) {
                recoToken = new RecoToken(speechTimestamp);
                recoTokenMap.put(speechTimestamp.getStartTime(), recoToken);
            } else {
                recoToken.updateFrom(speechTimestamp);
            }
        }

        public void add(SpeakerLabel speakerLabel) {
            RecoToken recoToken = recoTokenMap.get(speakerLabel.getFrom());
            if (recoToken == null) {
                recoToken = new RecoToken(speakerLabel);
                recoTokenMap.put(speakerLabel.getFrom(), recoToken);
            } else {
                recoToken.updateFrom(speakerLabel);
            }

            if (speakerLabel.isFinal()) {
                markTokensBeforeAsFinal(speakerLabel.getFrom());
                report();
                cleanFinal();
            }
        }

        private void markTokensBeforeAsFinal(Double from) {
            Map<Double, RecoToken> recoTokenMap = new LinkedHashMap<>();

            for (RecoToken rt : recoTokenMap.values()) {
                if (rt.startTime <= from)
                    rt.spLabelIsFinal = true;
            }
        }

        public void report() {
            List<Utterance> uttterances = new ArrayList<Utterance>();
            Utterance currentUtterance = new Utterance(0, "");

            for (RecoToken rt : recoTokenMap.values()) {
                if (currentUtterance.speaker != rt.speaker) {
                    uttterances.add(currentUtterance);
                    currentUtterance = new Utterance(rt.speaker, "");
                }
                currentUtterance.transcript = currentUtterance.transcript + rt.word + " ";
            }
            uttterances.add(currentUtterance);

            String result = GsonSingleton.getGson().toJson(uttterances);
            System.out.println(result);
        }

        private void cleanFinal() {
            Set<Map.Entry<Double, RecoToken>> set = recoTokenMap.entrySet();
            for (Map.Entry<Double, RecoToken> e : set) {
                if (e.getValue().spLabelIsFinal) {
                    recoTokenMap.remove(e.getKey());
                }
            }
        }

    }


    private static CountDownLatch lock = new CountDownLatch(1);*/
}
