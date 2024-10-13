package org.nautilus;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.stream.Collectors;

@WebServlet("/edo/*")
public class EdoProxyServlet extends HttpServlet {
    private String edoServiceUrl;
    private String authHeader;

    @Override
    public void init() throws ServletException {
        // Завантаження конфігурації з .properties файлу
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            prop.load(input);
            edoServiceUrl = prop.getProperty("edoService.url");
            authHeader = prop.getProperty("edoService.authHeader");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo.equalsIgnoreCase("/task/thanks")){
            forwardRequestToInternalService("GET", "/task/thanks", null, resp);
        }
        else if (pathInfo.startsWith("/task/")) {
            String taskId = pathInfo.substring("/task/".length());
            resp.setStatus(HttpServletResponse.SC_OK);
            if (isValidGUID(taskId)) {
                forwardRequestToInternalService("GET", "/task/" + taskId, null, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if ("/task/approve".equals(pathInfo)) {
            req.setCharacterEncoding("UTF-8");
            String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            if (isValidJson(requestBody)) {
                forwardRequestToInternalService("POST", "/task/approve", requestBody, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void forwardRequestToInternalService(String method, String path, String body, HttpServletResponse resp) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpResponse httpResponse;

            // Виконання запиту на основі HTTP методу
            if ("GET".equalsIgnoreCase(method)) {
                HttpGet httpGet = new HttpGet(edoServiceUrl + path);
                httpGet.setHeader("Authorization", authHeader);
                httpResponse = httpClient.execute(httpGet);
            } else if ("POST".equalsIgnoreCase(method)) {
                HttpPost httpPost = new HttpPost(edoServiceUrl + path);
                httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8)); // Вказуємо тип вмісту
                httpPost.setHeader("Authorization", authHeader);
                httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
                httpResponse = httpClient.execute(httpPost);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported method: " + method);
                return;
            }

            // Отримання статус-коду
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            resp.setStatus(statusCode);
            resp.setContentType("text/html; charset=UTF-8");

            // Копіюємо заголовки з httpResponse
            for (Header header : httpResponse.getAllHeaders()) {
                resp.setHeader(header.getName(), header.getValue());
            }
            // Читання тіла відповіді
            try (InputStream inputStream = httpResponse.getEntity().getContent();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                StringBuilder responseBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line);
                }

                resp.getWriter().write(responseBody.toString());
                resp.getWriter().flush(); // Забезпечуємо, що вміст записується у відповідь

            } catch (IOException e) {
                throw new IOException("Failed to read response body", e);
            }

        } catch (Exception e) {
            throw new IOException("Failed to forward request", e);
        }
    }


    private boolean isValidGUID(String guid) {
        return guid.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");
    }
    private boolean isValidJson(String json) {
        return true;
    }
//        // Валідація JSON за схемою
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode schemaNode = mapper.readTree(new File("schema.json"));
//            JsonNode jsonNode = mapper.readTree(json);
//            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
//            JsonSchema schema = factory.getSchema(schemaNode);
//            Set<ValidationMessage> errors = schema.validate(jsonNode);
//            return errors.isEmpty();
//        } catch (Exception e) {
//            return false;
//        }
}
