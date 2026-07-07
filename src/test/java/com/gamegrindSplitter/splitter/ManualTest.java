package com.gamegrindSplitter.splitter;

import java.util.List;

public class ManualTest {

    public static void main(String[] args) {
        String sample =
                "package com.example.orders;\n\n" +
                "public class OrderService {\n\n" +
                "    private final OrderRepository repository;\n\n" +
                "    public OrderService(OrderRepository repository) {\n" +
                "        this.repository = repository;\n" +
                "    }\n\n" +
                "    public Order placeOrder(OrderRequest request) {\n" +
                "        validate(request);\n" +
                "        Order order = new Order(request.getItems(), request.getCustomerId());\n" +
                "        for (Item item : request.getItems()) {\n" +
                "            if (item.getQuantity() <= 0) {\n" +
                "                throw new IllegalArgumentException(\"Quantity must be positive\");\n" +
                "            }\n" +
                "        }\n" +
                "        return repository.save(order);\n" +
                "    }\n\n" +
                "    private void validate(OrderRequest request) {\n" +
                "        if (request.getCustomerId() == null) {\n" +
                "            throw new IllegalArgumentException(\"customerId required\");\n" +
                "        }\n" +
                "    }\n\n" +
                "    public static class Order {\n" +
                "        private final java.util.List<Item> items;\n" +
                "        private final String customerId;\n\n" +
                "        public Order(java.util.List<Item> items, String customerId) {\n" +
                "            this.items = items;\n" +
                "            this.customerId = customerId;\n" +
                "        }\n" +
                "    }\n" +
                "}\n";

        System.out.println("Source length: " + sample.length() + " chars\n");

        run(sample, 300, 30);
        run(sample, 600, 30);
    }

    private static void run(String sample, int chunkSize, int overlap) {
        RecursiveCharacterTextSplitter splitter =
                RecursiveCharacterTextSplitter.fromLanguage(Language.JAVA, chunkSize, overlap);

        List<String> chunks = splitter.splitText(sample);

        System.out.println("=== chunkSize=" + chunkSize + " overlap=" + overlap
                + " -> " + chunks.size() + " chunks ===");
        int i = 0;
        int maxLen = 0;
        for (String chunk : chunks) {
            maxLen = Math.max(maxLen, chunk.length());
            System.out.println("--- chunk " + (i++) + " (" + chunk.length() + " chars) ---");
            System.out.println(chunk);
            System.out.println();
        }
        System.out.println("Max chunk length: " + maxLen + " (limit " + chunkSize + ")\n");
    }
}
