import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        int quantity = readNumber();
        BigDecimal[] p = readProductEnsembles(quantity);
        BigDecimal entropy = findEntropy(p);

        List<ProbabilityAndEvents> probabilityAndEvents = getListProbabilityAndEvents(p);

        int[] indexesSortedList = new int[quantity];

        for (int i = 0; i < probabilityAndEvents.size(); i++)
            indexesSortedList[i] = probabilityAndEvents.get(i).events.get(0);

        String[] huffmanCodes = findHuffmanCodes(quantity, probabilityAndEvents);
        writeCodesAndLength(indexesSortedList, huffmanCodes, "Хаффмана");

        BigDecimal averageLength = findAverageLength(huffmanCodes, p);
        findRedundancy(averageLength, entropy);

        String[] shannonFanoCode = findShannonFanoCodes(quantity, indexesSortedList, p);
        writeCodesAndLength(indexesSortedList, shannonFanoCode, "Шеннона-Фано");

        averageLength = findAverageLength(shannonFanoCode, p);
        findRedundancy(averageLength, entropy);
    }

    private static class ProbabilityAndEvents {
        public BigDecimal probability;
        public List<Integer> events;

        public ProbabilityAndEvents(BigDecimal probability, List<Integer> events) {
            this.probability = probability;
            this.events = events;
        }
    }

    private static int readNumber() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите количество вероятностей");
        return scanner.nextInt();
    }

    private static BigDecimal[] readProductEnsembles(int length) {
        System.out.println("Введите вероятности");

        Scanner scanner = new Scanner(System.in);
        BigDecimal[] p = new BigDecimal[length];

        for (int i = 0; i < length; i++)
            p[i] = scanner.nextBigDecimal();

        return p;
    }

    private static BigDecimal findEntropy(BigDecimal[] probabilities) {
        BigDecimal entropy = BigDecimal.valueOf(0);

        double ln2 = Math.log(2);

        for (BigDecimal probability : probabilities) {
            double value = probability.doubleValue();
            double ln = Math.log(value);
            entropy = entropy.add(probability.multiply(BigDecimal.valueOf(ln / ln2)));
        }

        entropy = entropy.negate().stripTrailingZeros();

        System.out.println();
        System.out.println("Энтропия H: " + entropy + " бит");

        return entropy;
    }

    private static List<ProbabilityAndEvents> getListProbabilityAndEvents(BigDecimal[] p) {
        List<ProbabilityAndEvents> probabilityAndEvents = new ArrayList<>();

        for (int i = 0; i < p.length; i++) {
            List<Integer> events = new ArrayList<>();
            events.add(i);
            probabilityAndEvents.add(new ProbabilityAndEvents(p[i], events));
        }

        sortListProbabilityAndEvents(probabilityAndEvents);

        probabilityAndEvents.forEach(a -> System.out.println("z_" + (a.events.get(0) + 1) + " " + a.probability));

        return probabilityAndEvents;
    }

    private static void sortListProbabilityAndEvents(List<ProbabilityAndEvents> probabilityAndEvents) {
        probabilityAndEvents.sort((a, b) -> {
            if (a.probability.equals(b.probability)) {
                return a.events.get(0) - b.events.get(0);
            } else {
                return b.probability.compareTo(a.probability);
            }
        });
    }

    private static String[] findHuffmanCodes(int quantity, List<ProbabilityAndEvents> probabilityAndEvents) {
        String[] huffmanCodes = new String[quantity];
        Arrays.fill(huffmanCodes, "");

        while (probabilityAndEvents.size() > 1) {
            int lastIndex = probabilityAndEvents.size() - 1;
            ProbabilityAndEvents last = probabilityAndEvents.get(lastIndex);
            ProbabilityAndEvents penultimate = probabilityAndEvents.get(lastIndex - 1);

            BigDecimal sum = last.probability.add(penultimate.probability);

            addCodeDigit(last, penultimate, huffmanCodes);

            List<Integer> list = new ArrayList<>(penultimate.events);
            list.addAll(last.events);

            ProbabilityAndEvents element = new ProbabilityAndEvents(sum, list);

            probabilityAndEvents.remove(lastIndex);
            probabilityAndEvents.remove(lastIndex - 1);
            probabilityAndEvents.add(element);

            sortListProbabilityAndEvents(probabilityAndEvents);
        }

        for (int i = 0; i < huffmanCodes.length; i++)
            huffmanCodes[i] = new StringBuilder(huffmanCodes[i]).reverse().toString();

        return huffmanCodes;
    }

    private static String[] findShannonFanoCodes(int quantity, int[] indexesSortedList, BigDecimal[] p) {
        String[] shannonFanoCodes = new String[quantity];
        Arrays.fill(shannonFanoCodes, "");

        List<Integer> allEvents = new ArrayList<>();

        for (int index : indexesSortedList) {
            allEvents.add(index);
        }

        Set<ProbabilityAndEvents> set = new HashSet<>();
        set.add(new ProbabilityAndEvents(BigDecimal.valueOf(1), allEvents));

        while (set.iterator().hasNext()) {
            ProbabilityAndEvents current = set.iterator().next();
            set.remove(current);
            List<Integer> events = current.events;

            BigDecimal half = current.probability.divide(BigDecimal.valueOf(2), new MathContext(8, RoundingMode.HALF_UP));
            BigDecimal sum = p[events.get(0)];
            BigDecimal difference = half.add(sum.negate()).abs().stripTrailingZeros();

            int border = 1;
            sum = sum.add(p[events.get(border)]).stripTrailingZeros();
            BigDecimal nextDifference = getDifference(half, sum);

            while (difference.compareTo(nextDifference) >= 0) {
                difference = nextDifference;
                border++;
                sum = sum.add(p[events.get(border)]).stripTrailingZeros();
                nextDifference = getDifference(half, sum);
            }

            sum = sum.add(p[events.get(border)].negate()).stripTrailingZeros();

            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < border; i++)
                list.add(events.get(i));
            ProbabilityAndEvents firstPart = new ProbabilityAndEvents(sum, list);

            list = new ArrayList<>();
            for (int i = border; i < events.size(); i++)
                list.add(events.get(i));
            ProbabilityAndEvents secondPart = new ProbabilityAndEvents(current.probability.add(sum.negate()), list);

            addCodeDigit(firstPart, secondPart, shannonFanoCodes);

            if (firstPart.events.size() > 1)
                set.add(firstPart);

            if (secondPart.events.size() > 1)
                set.add(secondPart);
        }

        return shannonFanoCodes;
    }

    private static BigDecimal getDifference(BigDecimal half, BigDecimal sum) {
        return half.add(sum.negate()).abs().stripTrailingZeros();
    }

    private static void addCodeDigit(ProbabilityAndEvents firstPart, ProbabilityAndEvents secondPart, String[] codes) {
        ProbabilityAndEvents one;
        ProbabilityAndEvents zero;
        if ((secondPart.events.size() == firstPart.events.size() && firstPart.probability.compareTo(secondPart.probability) >= 0)
                || (secondPart.events.size() > firstPart.events.size())) {
            zero = firstPart;
            one = secondPart;
        } else {
            zero = secondPart;
            one = firstPart;
        }

        for (int i : zero.events)
            codes[i] += "0";

        for (int i : one.events)
            codes[i] += "1";
    }

    private static void writeCodesAndLength(int[] indexesSortedList, String[] codes, String name) {
        System.out.println();
        System.out.println("Код " + name + ": ");
        for (int index : indexesSortedList)
            System.out.println("z_" + (index + 1) + " = " + codes[index] + "  L_" + (index + 1) + " = "
                    + codes[index].length());
    }

    private static BigDecimal findAverageLength(String[] codes, BigDecimal[] probabilities) {
        BigDecimal length = BigDecimal.valueOf(0);

        for (int i = 0; i < codes.length; i++) {
            length = length.add(probabilities[i].multiply(BigDecimal.valueOf(codes[i].length())));
        }

        length = length.stripTrailingZeros();

        System.out.println();
        System.out.println("Средняя длина кода L: " + length + " бит");

        return length;
    }

    private static void findRedundancy(BigDecimal averageLength, BigDecimal entropy) {
        BigDecimal r = averageLength.add(entropy.negate());
        System.out.println("Избыточность r: " + r.stripTrailingZeros() + " бит");
    }
}

//0,208 0,33 0,115 0,115 0,01 0,059 0,037 0,042 0,03 0,054

//0,279 0,08 0,07 0,055 0,25 0,038 0,11 0,03 0,034 0,054

//0,049 0,26 0,055 0,025 0,13 0,145 0,028 0,046 0,11 0,152

//0,147 0,27 0,025 0,1 0,16 0,024 0,028 0,146 0,038 0,062