package org.nautilus.service;

public class JsonValidationService {
//    private final Schema schema;
//
//    public JsonValidationService(String schemaFilePath) throws IOException {
//        // Завантажуємо JSON-схему з файлу
//        String schemaContent = new String(Files.readAllBytes(Paths.get(schemaFilePath)));
//        JSONObject schemaJson = new JSONObject(schemaContent);
//        schema = SchemaLoader.load(schemaJson);
//    }
//
//    public Set<org.everit.json.schema.ValidationException> validate(String jsonData) throws IOException {
//        try {
//            // Парсимо JSON-дані за допомогою Jackson
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode jsonNode = objectMapper.readTree(jsonData);
//
//            // Валідовуємо JSON-дані за схемою
//            schema.validate(new JSONObject(jsonNode.toString()));
//            return null; // Якщо помилок немає
//        } catch (org.everit.json.schema.ValidationException e) {
//            return e.getCausingExceptions(); // Повертаємо список помилок валідації
//        }
//    }
}
