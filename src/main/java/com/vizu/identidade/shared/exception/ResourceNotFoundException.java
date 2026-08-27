package com.vizu.identidade.shared.exception;
public class ResourceNotFoundException extends RuntimeException { public ResourceNotFoundException(String resource) { super(resource + " não encontrado"); } }
