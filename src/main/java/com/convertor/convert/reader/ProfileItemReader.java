package com.convertor.convert.reader;

import org.springframework.batch.item.ItemReader;

public class ProfileItemReader implements ItemReader<String> {

    private final String rawResponse;
    private boolean read = false;

    public ProfileItemReader(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    @Override
    public String read() {
        if (!read) {
            read = true;
            return rawResponse; // entrega el JSON al Processor
        }
        return null; // fin del stream
    }
}