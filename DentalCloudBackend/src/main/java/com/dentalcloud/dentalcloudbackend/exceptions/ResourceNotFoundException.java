package com.dentalcloud.dentalcloudbackend.handlers;

package com.dentalcloud.dentalcloudbackend.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
