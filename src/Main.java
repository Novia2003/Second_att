import java.math.BigDecimal;
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
        writeHuffmanCodesAndLength(indexesSortedList, huffmanCodes);

        BigDecimal averageLength =  findAverageLength(huffmanCodes, p);

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

        entropy = entropy.multiply(BigDecimal.valueOf(-1)).stripTrailingZeros();

        System.out.println();
        System.out.println("Энтропия H: " + entropy);

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

            ProbabilityAndEvents one;
            ProbabilityAndEvents zero;

            if (((last.events.size() == penultimate.events.size() && last.probability.compareTo(penultimate.probability) > 0) ||
                    penultimate.events.size() > last.events.size())) {
                one = penultimate;
                zero = last;
            } else {
                one = last;
                zero = penultimate;
            }

            for (int i : one.events)
                huffmanCodes[i] += "1";

            for (int i : zero.events)
                huffmanCodes[i] += "0";

            List<Integer> list = new ArrayList<>(zero.events);
            list.addAll(one.events);

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

    private static void writeHuffmanCodesAndLength(int[] indexesSortedList, String[] huffmanCodes) {
        System.out.println();
        System.out.println("Код Хаффмана: ");
        for (int index : indexesSortedList)
            System.out.println("z_" + (index + 1) + " = " + huffmanCodes[index] + "  L_" + (index + 1) + " = "
                    + huffmanCodes[index].length());
    }

    private static BigDecimal findAverageLength(String[] codes, BigDecimal[] probabilities) {
        BigDecimal length = BigDecimal.valueOf(0);

        for (int i = 0; i < codes.length; i++) {
            length = length.add(probabilities[i].multiply(BigDecimal.valueOf(codes[i].length())));
        }

        length = length.stripTrailingZeros();

        System.out.println();
        System.out.println("Средняя длина кода L: " + length);

        return length;
    }

    private static void findRedundancy(BigDecimal averageLength, BigDecimal entropy) {
        BigDecimal r = averageLength.add(entropy.multiply(BigDecimal.valueOf(-1)));
        System.out.println("Избыточность r: " + r.stripTrailingZeros());
    }
}

//0,208 0,33 0,115 0,115 0,01 0,059 0,037 0,042 0,03 0,054
//2 2 3 3 6 4 5 4 6 4

//0,279 0,08 0,07 0,055 0,25 0,038 0,11 0,03 0,034 0,054
//2 4 4 4 2 4 3 5 5 4

//0,049 0,26 0,055 0,025 0,13 0,145 0,028 0,046 0,11 0,152
//4 2 4 5 3 3 5 4 3 3

//0,147 0,27 0,025 0,1 0,16 0,024 0,028 0,146 0,038 0,062