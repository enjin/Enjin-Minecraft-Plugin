package com.enjin.officialplugin.yaml;

public class InvalidYamlConfigurationException extends Exception {

    String message;

    public InvalidYamlConfigurationException(String message) {
        this.message = message;
    }
}
