import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Сортировка CSV-файла размером, превышающего размер оперативной памяти
 *
 * @author Danila Potapkin
 * @since 14.03.2024
 */
public class Main {

	private static final Logger logger = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {
		String inputFile = "файл.csv";
		String outputFile = "отсортированный_" + inputFile;
		try {
			List<File> sortedParts = sortingParts(inputFile);
			mergingParts(sortedParts, outputFile);
			logger.info("Файл успешно отсортирован. Отсортированный файл: " + outputFile);
		} catch (IOException e) {
			logger.severe("Ошибка чтения или записи файла: " + e.getMessage());
		}
	}

	/**
	 * Разбиение файла на части и сортировка этих частей, с последующей записью в файл
	 *
	 * @return список отсортированных файлов (частей)
	 */
	private static List<File> sortingParts(String filename) throws IOException {
		Scanner scanner = new Scanner(System.in);
		List<File> sortedParts = new ArrayList<>();
		logger.info("Введите максимальный размер части файла: ");
		int maxPartSize = scanner.nextInt();
		int partsCount = 0;

		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			List<String> part = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				part.add(line);
				if (part.size() >= maxPartSize) {
					Collections.sort(part);
					File partFile = new File("part" + partsCount + ".csv");
					writeFile(partFile, part);
					sortedParts.add(partFile);
					part.clear();
					partsCount++;
				}
			}
			if (!part.isEmpty()) {
				Collections.sort(part);
				File partFile = new File("part" + partsCount + ".csv");
				writeFile(partFile, part);
				sortedParts.add(partFile);
			}
		}

		return sortedParts;
	}

	/**
	Объединение отсортированных частей в один файл
	 */
	private static void mergingParts(Iterable<File> sortedParts, String outputFile) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
			List<BufferedReader> readers = new ArrayList<>();
			for (File partFile : sortedParts) {
				readers.add(new BufferedReader(new FileReader(partFile)));
			}

			while (!readers.isEmpty()) {
				String minLine = null;
				int minIndex = 0;
				for (int i = 0; i < readers.size(); i++) {
					BufferedReader br = readers.get(i);
					String line = br.readLine();
					if (line != null && (minLine == null || line.compareTo(minLine) < 0)) {
						minLine = line;
						minIndex = i;
					}
				}

				if (minLine == null) {
					break;
				}

				writer.write(minLine);
				writer.newLine();

				BufferedReader br = readers.get(minIndex);
				String nextLine = br.readLine();
				if (nextLine == null) {
					br.close();
					readers.remove(minIndex);
				}
			}
		}
	}

	/**
	 * Запись отсортированной части в файл
	 */
	private static void writeFile(File file, Iterable<String> lines) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			for (String line : lines) {
				writer.write(line);
				writer.newLine();
			}
		}
	}
}