package com.university.exam.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileRepository<T> {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private String dataPath = "./data";
    
    /**
     * Read all entities from file with thread-safe read lock
     */
    public List<T> readAll(String fileName, TypeReference<List<T>> typeRef) {
        lock.readLock().lock();
        try {
            File file = new File(dataPath, fileName);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            
            List<String> lines = Files.readAllLines(file.toPath());
            List<T> entities = new ArrayList<>();
            
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                // Read each line as the generic type T directly
                @SuppressWarnings("unchecked")
                Class<T> entityClass = (Class<T>) ((java.lang.reflect.ParameterizedType) typeRef.getType()).getRawType();
                T entity = objectMapper.readValue(line, entityClass);
                entities.add(entity);
            }
            
            return entities;
        } catch (IOException e) {
            System.err.println("Error reading from " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Write all entities to file with thread-safe write lock
     */
    public void writeAll(String fileName, List<T> entities) {
        lock.writeLock().lock();
        try {
            File file = new File(dataPath, fileName);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (T entity : entities) {
                    String json = objectMapper.writeValueAsString(entity);
                    writer.write(json);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing to " + fileName + ": " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Append single entity to file with thread-safe write lock
     */
    public void append(String fileName, T entity) {
        lock.writeLock().lock();
        try {
            File file = new File(dataPath, fileName);
            Files.createDirectories(file.toPath().getParent());
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                String json = objectMapper.writeValueAsString(entity);
                writer.write(json);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error appending to " + fileName + ": " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
}
