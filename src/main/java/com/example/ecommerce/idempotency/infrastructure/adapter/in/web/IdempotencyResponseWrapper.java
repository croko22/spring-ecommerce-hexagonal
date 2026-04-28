package com.example.ecommerce.idempotency.infrastructure.adapter.in.web;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class IdempotencyResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream capture;
    private ServletOutputStream outputStream;
    private PrintWriter writer;

    public IdempotencyResponseWrapper(HttpServletResponse response) {
        super(response);
        this.capture = new ByteArrayOutputStream();
    }

    @Override
    public ServletOutputStream getOutputStream() {
        if (writer != null) {
            throw new IllegalStateException("getWriter() has already been called");
        }
        if (outputStream == null) {
            outputStream = new ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener listener) {
                }

                @Override
                public void write(int b) {
                    capture.write(b);
                }
            };
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() {
        if (outputStream != null) {
            throw new IllegalStateException("getOutputStream() has already been called");
        }
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(capture, StandardCharsets.UTF_8));
        }
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        }
        if (outputStream != null) {
            outputStream.flush();
        }
    }

    public byte[] getBody() {
        flushBufferSafely();
        return capture.toByteArray();
    }

    public String getBodyAsString() {
        byte[] body = getBody();
        return body.length > 0 ? new String(body, StandardCharsets.UTF_8) : null;
    }

    private void flushBufferSafely() {
        try {
            flushBuffer();
        } catch (IOException e) {
            // ignore
        }
    }
}