import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MadLibGame {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([a-z_]+)\\}");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Welcome to the Mad Lib Game! ===");
        System.out.println("Answer the prompts to create a silly story.\n");

        List<String> templates = loadTemplates("madlib_templates.txt");
        if (templates.isEmpty()) {
            System.out.println("No templates were found. Please add templates to madlib_templates.txt.");
            scanner.close();
            return;
        }

        String selectedTemplate = templates.get(new Random().nextInt(templates.size()));
        Map<String, Integer> placeholderCounts = countPlaceholders(selectedTemplate);

        List<String> userWords = new ArrayList<>();
        int occurrence = 1;
        for (Map.Entry<String, Integer> entry : placeholderCounts.entrySet()) {
            String placeholderType = entry.getKey();
            int count = entry.getValue();
            for (int i = 1; i <= count; i++) {
                userWords.add(prompt(scanner, buildPromptLabel(placeholderType, i, count)));
                occurrence++;
            }
        }

        String completedStory = replacePlaceholders(selectedTemplate, userWords);
        System.out.println("\nYour completed story:\n");
        System.out.println(completedStory);
        System.out.println("\nThanks for playing!");

        scanner.close();
    }

    private static List<String> loadTemplates(String fileName) {
        List<String> templates = new ArrayList<>();
        Path path = Paths.get(fileName);

        if (!Files.exists(path)) {
            return templates;
        }

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            StringBuilder currentTemplate = new StringBuilder();

            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    if (currentTemplate.length() > 0) {
                        templates.add(currentTemplate.toString().trim());
                        currentTemplate.setLength(0);
                    }
                } else {
                    if (currentTemplate.length() > 0) {
                        currentTemplate.append("\n");
                    }
                    currentTemplate.append(line.trim());
                }
            }

            if (currentTemplate.length() > 0) {
                templates.add(currentTemplate.toString().trim());
            }
        } catch (IOException e) {
            System.out.println("Unable to read templates file: " + e.getMessage());
        }

        return templates;
    }

    private static Map<String, Integer> countPlaceholders(String template) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);

        while (matcher.find()) {
            String placeholderType = matcher.group(1);
            counts.put(placeholderType, counts.getOrDefault(placeholderType, 0) + 1);
        }

        return counts;
    }

    private static String replacePlaceholders(String template, List<String> userWords) {
        StringBuffer completedStory = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        int index = 0;

        while (matcher.find()) {
            String replacement = index < userWords.size() ? userWords.get(index) : "";
            matcher.appendReplacement(completedStory, Matcher.quoteReplacement(replacement));
            index++;
        }
        matcher.appendTail(completedStory);
        return completedStory.toString();
    }

    private static String prompt(Scanner scanner, String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }

    private static String buildPromptLabel(String placeholderType, int occurrence, int total) {
        String baseLabel;
        switch (placeholderType) {
            case "proper_noun":
                baseLabel = "Enter a proper noun";
                break;
            case "adjective":
                baseLabel = "Enter an adjective";
                break;
            case "verb":
                baseLabel = "Enter a verb";
                break;
            case "adverb":
                baseLabel = "Enter an adverb";
                break;
            case "noun":
                baseLabel = "Enter a common noun";
                break;
            case "improper_noun":
                baseLabel = "Enter an improper noun";
                break;
            case "place":
                baseLabel = "Enter a place";
                break;
            default:
                baseLabel = "Enter a " + placeholderType.replace('_', ' ');
                break;
        }

        return baseLabel + " (" + occurrence + " of " + total + ")";
    }
}
