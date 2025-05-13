package com.sunbeam.exception;

import java.io.IOException;

public class FileStorageException extends BaseException {
    public FileStorageException(String message) {
        super(message);
    }

	public FileStorageException(String message, IOException e) {
		super(message);
	}

	
}