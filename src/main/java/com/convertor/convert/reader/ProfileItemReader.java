package com.convertor.convert.reader;

import com.convertor.convert.utils.HttpUtils;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

public class ProfileItemReader implements ItemReader<String> {

    private final String baseUrl;
    private final int initialPage;
    private final int pageSize;
    private final String authToken;
    
    private int currentPage;
    private List<Map<String, Object>> currentElements;
    private int currentIndex = 0;
    private int totalElements = 0;
    private int pagesProcessed = 0;

    public ProfileItemReader(
            @Value("#{jobParameters['baseUrl']}") String baseUrl,
            @Value("#{jobParameters['initialPage']}") int initialPage,
            @Value("#{jobParameters['pageSize']}") int pageSize,
            @Value("#{jobParameters['authToken']}") String authToken) {
        this.baseUrl = baseUrl;
        this.initialPage = initialPage;
        this.pageSize = pageSize;
        this.authToken = authToken;
        this.currentPage = initialPage;
    }

    @Override
    public String read() {
        // Si no hay elementos o hemos consumido todos los de la página actual
        if (currentElements == null || currentIndex >= currentElements.size()) {
            // Si ya hemos procesado todas las páginas, terminamos
            if (totalElements > 0 && pagesProcessed * pageSize >= totalElements) {
                return null;
            }
            
            // Solicitar nueva página
            Map<String, Object> response = HttpUtils.fetchPage(
                baseUrl, currentPage, pageSize, authToken);
            
            // Extraer elementos y metadatos de paginación
            currentElements = (List<Map<String, Object>>) response.get("elements");
            totalElements = (int) response.get("totalElements");
            currentPage = (int) response.get("page") + 1; // Preparamos para la próxima página
            pagesProcessed++;
            currentIndex = 0;
            
            // Si no hay elementos, terminamos
            if (currentElements.isEmpty()) {
                return null;
            }
        }
        
        // Obtener el siguiente elemento y convertirlo a JSON
        Map<String, Object> element = currentElements.get(currentIndex++);
        return HttpUtils.mapToJson(element);
    }
}