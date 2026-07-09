import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShoppingCart {
    private static final double TAX_RATE = 0.06;
    private static final String SALES_FILE = "sales.json";
    private static final String INVENTORY_FILE = "inventory.json";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        InventoryState inventoryState = loadInventoryState();

        while (true) {
            System.out.println("\n=== Shopping Cart Program ===");
            System.out.println("1. Shopping Mode");
            System.out.println("2. Exit");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            switch (choice) {
                case 0:
                    if (authorizeStocking(scanner, inventoryState)) {
                        stockingMode(scanner, inventoryState);
                    }
                    break;
                case 1:
                    shoppingMode(scanner, inventoryState.items);
                    break;
                case 2:
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Please enter 0, 1, or 2.");
            }
        }
    }

    private static void stockingMode(Scanner scanner, InventoryState inventoryState) {
        ArrayList<Item> inventory = inventoryState.items;
        System.out.print("How many different items would you like to add? ");
        int count;
        try {
            count = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return;
        }

        for (int i = 1; i <= count; i++) {
            System.out.println("\nItem " + i + ":");
            System.out.print("Name: ");
            String name = scanner.nextLine().trim();

            int quantity;
            while (true) {
                System.out.print("Stocked quantity: ");
                try {
                    quantity = Integer.parseInt(scanner.nextLine().trim());
                    if (quantity >= 0) {
                        break;
                    }
                    System.out.println("Quantity cannot be negative.");
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid integer.");
                }
            }

            double price;
            while (true) {
                System.out.print("Price: ");
                try {
                    price = Double.parseDouble(scanner.nextLine().trim());
                    if (price >= 0) {
                        break;
                    }
                    System.out.println("Price cannot be negative.");
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid price.");
                }
            }

            boolean updated = false;
            for (Item existingItem : inventory) {
                if (existingItem.name.equalsIgnoreCase(name)) {
                    existingItem.quantity = quantity;
                    existingItem.price = price;
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                inventory.add(new Item(name, quantity, price));
            }

            System.out.println("Added or updated " + name + " in inventory.");
        }

        saveInventoryState(inventoryState);
        System.out.println("\nCurrent inventory:");
        displayInventory(inventory);
    }

    private static void shoppingMode(Scanner scanner, ArrayList<Item> inventory) {
        if (inventory.isEmpty()) {
            System.out.println("There are no stocked items yet. Please add items in stocking mode first.");
            return;
        }

        ArrayList<CartItem> cart = new ArrayList<>();
        System.out.println("\nAvailable inventory:");
        displayInventory(inventory);

        while (true) {
            System.out.print("\nEnter item number to add to your cart (0 to finish): ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            if (choice == 0) {
                break;
            }

            if (choice < 1 || choice > inventory.size()) {
                System.out.println("That item number is not valid.");
                continue;
            }

            Item selectedItem = inventory.get(choice - 1);
            int maxQuantity = selectedItem.quantity;
            if (maxQuantity <= 0) {
                System.out.println("That item is out of stock.");
                continue;
            }

            int quantity;
            while (true) {
                System.out.print("How many would you like to add? ");
                try {
                    quantity = Integer.parseInt(scanner.nextLine().trim());
                    if (quantity > 0 && quantity <= maxQuantity) {
                        break;
                    }
                    System.out.println("Please enter a quantity between 1 and " + maxQuantity + ".");
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid integer.");
                }
            }

            boolean alreadyInCart = false;
            for (CartItem cartItem : cart) {
                if (cartItem.item.name.equals(selectedItem.name)) {
                    cartItem.quantity += quantity;
                    alreadyInCart = true;
                    break;
                }
            }

            if (!alreadyInCart) {
                cart.add(new CartItem(selectedItem, quantity));
            }

            selectedItem.quantity -= quantity;
            System.out.println("Added " + quantity + " of " + selectedItem.name + " to your cart.");
        }

        printReceipt(cart);
    }

    private static void displayInventory(ArrayList<Item> inventory) {
        if (inventory.isEmpty()) {
            System.out.println("No items stocked.");
            return;
        }

        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            System.out.println((i + 1) + ". " + item.name + " | Qty: " + item.quantity + " | Price: $" + String.format("%.2f", item.price));
        }
    }

    private static void printReceipt(ArrayList<CartItem> cart) {
        System.out.println("\n=== Receipt ===");
        if (cart.isEmpty()) {
            System.out.println("You did not purchase anything.");
            System.out.println("Subtotal: $0.00");
            System.out.println("Tax: $0.00");
            System.out.println("Grand Total: $0.00");
            return;
        }

        double subtotal = 0.0;
        for (CartItem cartItem : cart) {
            double lineTotal = cartItem.quantity * cartItem.item.price;
            subtotal += lineTotal;
            System.out.println(cartItem.item.name + " x" + cartItem.quantity + " = $" + String.format("%.2f", lineTotal));
        }

        double tax = subtotal * TAX_RATE;
        double grandTotal = subtotal + tax;

        System.out.println("Subtotal: $" + String.format("%.2f", subtotal));
        System.out.println("Tax (6%): $" + String.format("%.2f", tax));
        System.out.println("Grand Total: $" + String.format("%.2f", grandTotal));
        saveSaleToJson(cart, subtotal, tax, grandTotal);
    }

    private static InventoryState loadInventoryState() {
        Path path = Paths.get(INVENTORY_FILE);
        if (!Files.exists(path)) {
            return new InventoryState(new ArrayList<>(), "");
        }

        try {
            String content = Files.readString(path, StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                return new InventoryState(new ArrayList<>(), "");
            }

            String passcode = extractStringValue(content, "passcode");
            ArrayList<Item> inventory = new ArrayList<>();
            String itemsJson = extractJsonArray(content, "items");
            if (itemsJson != null && !itemsJson.isEmpty()) {
                String[] items = itemsJson.replace("[", "").replace("]", "").split("},");
                for (String item : items) {
                    String cleanItem = item.replace("{", "").replace("}", "").trim();
                    if (cleanItem.isEmpty()) {
                        continue;
                    }

                    String[] parts = cleanItem.split(",");
                    String name = "";
                    int quantity = 0;
                    double price = 0.0;

                    for (String part : parts) {
                        String[] keyValue = part.split(":", 2);
                        if (keyValue.length != 2) {
                            continue;
                        }

                        String key = keyValue[0].trim().replace("\"", "");
                        String value = keyValue[1].trim().replace("\"", "");
                        if (key.equals("name")) {
                            name = value;
                        } else if (key.equals("quantity")) {
                            quantity = Integer.parseInt(value);
                        } else if (key.equals("price")) {
                            price = Double.parseDouble(value);
                        }
                    }

                    boolean updated = false;
                    for (Item existingItem : inventory) {
                        if (existingItem.name.equalsIgnoreCase(name)) {
                            existingItem.quantity = quantity;
                            existingItem.price = price;
                            updated = true;
                            break;
                        }
                    }

                    if (!updated) {
                        inventory.add(new Item(name, quantity, price));
                    }
                }
            }
            return new InventoryState(inventory, passcode);
        } catch (IOException e) {
            System.out.println("Could not load inventory from JSON file: " + e.getMessage());
            return new InventoryState(new ArrayList<>(), "");
        }
    }

    private static void saveInventoryState(InventoryState inventoryState) {
        Path path = Paths.get(INVENTORY_FILE);
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("{\n");
            builder.append("  \"passcode\": \"").append(escapeJson(inventoryState.passcode)).append("\",\n");
            builder.append("  \"items\": [\n");
            for (int i = 0; i < inventoryState.items.size(); i++) {
                Item item = inventoryState.items.get(i);
                builder.append("    {\"name\":\"").append(escapeJson(item.name)).append("\",\"quantity\":")
                        .append(item.quantity).append(",\"price\":")
                        .append(String.format(Locale.US, "%.2f", item.price)).append("}");
                if (i < inventoryState.items.size() - 1) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            builder.append("  ]\n");
            builder.append("}");
            Files.writeString(path, builder.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("Could not save inventory to JSON file: " + e.getMessage());
        }
    }

    private static boolean authorizeStocking(Scanner scanner, InventoryState inventoryState) {
        if (inventoryState.passcode == null || inventoryState.passcode.isEmpty()) {
            System.out.print("Set a numeric passcode for stocking: ");
            String passcode = readNumericPasscode(scanner);
            if (passcode == null) {
                return false;
            }
            inventoryState.passcode = passcode;
            saveInventoryState(inventoryState);
            return true;
        }

        System.out.print("Enter stocking passcode: ");
        String enteredPasscode = scanner.nextLine().trim();
        if (enteredPasscode.equals(inventoryState.passcode)) {
            return true;
        }

        System.out.println("Incorrect passcode. Access denied.");
        return false;
    }

    private static String readNumericPasscode(Scanner scanner) {
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.matches("\\d+")) {
                return input;
            }
            System.out.print("Passcode must be all numbers. Try again: ");
        }
    }

    private static String extractStringValue(String content, String key) {
        Pattern pattern = Pattern.compile("\\\"" + key + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String extractJsonArray(String content, String key) {
        Pattern pattern = Pattern.compile("\\\"" + key + "\\\"\\s*:\\s*\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return "";
        }
        return matcher.group(1);
    }

    private static class InventoryState {
        private ArrayList<Item> items;
        private String passcode;

        private InventoryState(ArrayList<Item> items, String passcode) {
            this.items = items;
            this.passcode = passcode;
        }
    }

    private static void saveSaleToJson(ArrayList<CartItem> cart, double subtotal, double tax, double grandTotal) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");
        jsonBuilder.append("  \"timestamp\": \"").append(escapeJson(timestamp)).append("\",\n");
        jsonBuilder.append("  \"items\": [\n");

        for (int i = 0; i < cart.size(); i++) {
            CartItem cartItem = cart.get(i);
            double lineTotal = cartItem.quantity * cartItem.item.price;
            jsonBuilder.append("    {\n");
            jsonBuilder.append("      \"name\": \"").append(escapeJson(cartItem.item.name)).append("\",\n");
            jsonBuilder.append("      \"quantity\": ").append(cartItem.quantity).append(",\n");
            jsonBuilder.append("      \"unitPrice\": ").append(String.format(Locale.US, "%.2f", cartItem.item.price)).append(",\n");
            jsonBuilder.append("      \"lineTotal\": ").append(String.format(Locale.US, "%.2f", lineTotal)).append("\n");
            jsonBuilder.append("    }");
            if (i < cart.size() - 1) {
                jsonBuilder.append(",");
            }
            jsonBuilder.append("\n");
        }

        jsonBuilder.append("  ],\n");
        jsonBuilder.append("  \"subtotal\": ").append(String.format(Locale.US, "%.2f", subtotal)).append(",\n");
        jsonBuilder.append("  \"tax\": ").append(String.format(Locale.US, "%.2f", tax)).append(",\n");
        jsonBuilder.append("  \"grandTotal\": ").append(String.format(Locale.US, "%.2f", grandTotal)).append("\n");
        jsonBuilder.append("}");

        Path path = Paths.get(SALES_FILE);
        try {
            String existingContent = "";
            if (Files.exists(path)) {
                existingContent = Files.readString(path, StandardCharsets.UTF_8).trim();
            }

            String newContent;
            if (existingContent.isEmpty() || existingContent.equals("[]")) {
                newContent = "[\n" + jsonBuilder + "\n]";
            } else if (existingContent.startsWith("[") && existingContent.endsWith("]")) {
                newContent = existingContent.substring(0, existingContent.length() - 1) + ",\n" + jsonBuilder + "\n]";
            } else {
                newContent = "[\n" + jsonBuilder + "\n]";
            }

            Files.writeString(path, newContent, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Sale saved to " + SALES_FILE);
        } catch (IOException e) {
            System.out.println("Could not save sale to JSON file: " + e.getMessage());
        }
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static class Item {
        private String name;
        private int quantity;
        private double price;

        public Item(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }
    }

    private static class CartItem {
        private Item item;
        private int quantity;

        public CartItem(Item item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }
    }
}
